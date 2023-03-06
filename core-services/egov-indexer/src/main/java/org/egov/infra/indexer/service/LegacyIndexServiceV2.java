package org.egov.infra.indexer.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.IndexerApplicationRunnerImpl;
import org.egov.infra.indexer.custom.pgr.PGRCustomDecorator;
import org.egov.infra.indexer.custom.pgr.ServiceResponse;
import org.egov.infra.indexer.custom.pt.PTCustomDecorator;
import org.egov.infra.indexer.custom.pt.PropertyResponse;
import org.egov.infra.indexer.models.IndexJob;
import org.egov.infra.indexer.models.IndexJob.StatusEnum;
import org.egov.infra.indexer.models.IndexJobWrapper;
import org.egov.infra.indexer.models.LegacyIndexJob;
import org.egov.infra.indexer.producer.IndexerProducer;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.util.ResponseInfoFactory;
import org.egov.infra.indexer.web.contract.Index;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
import org.egov.infra.indexer.web.contract.LegacyIndexResponse;
import org.egov.infra.indexer.web.contract.Mapping;
import org.egov.infra.indexer.web.contract.Mapping.ConfigKeyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.egov.infra.indexer.util.IndexerConstants.*;

@Service
@Slf4j
public class LegacyIndexServiceV2 {

    @Autowired
    private IndexerApplicationRunnerImpl runner;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IndexerUtils indexerUtils;

    @Autowired
    private ResponseInfoFactory factory;

    @Autowired
    private IndexerProducer indexerProducer;

    @Value("${egov.core.reindex.topic.name}")
    private String reindexTopic;

    @Value("${egov.core.legacyindex.topic.name}")
    private String legacyIndexTopic;

    @Value("${egov.indexer.persister.create.topic}")
    private String persisterCreate;

    @Value("${egov.indexer.persister.update.topic}")
    private String persisterUpdate;

    @Value("${reindex.pagination.size.default}")
    private Integer defaultPageSizeForReindex;

    @Value("${legacyindex.pagination.size.default}")
    private Integer defaultPageSizeForLegacyindex;

    @Value("${egov.service.host}")
    private String serviceHost;

    @Value("${egov.indexer.pgr.legacyindex.topic.name}")
    private String pgrLegacyTopic;

    @Value("${egov.indexer.pt.legacyindex.topic.name}")
    private String ptLegacyTopic;

    @Value("${egov.infra.indexer.host}")
    private String esHostUrl;

    @Autowired
    private PGRCustomDecorator pgrCustomDecorator;

    @Autowired
    private PTCustomDecorator ptCustomDecorator;

    @Value("${egov.core.no.of.index.threads}")
    private Integer noOfIndexThreads;

    @Value("${egov.core.index.thread.poll.ms}")
    private Long indexThreadPollInterval;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ScheduledExecutorService schedulerofChildThreads = Executors.newScheduledThreadPool(1);

    @Autowired
    private IndexerService indexerService;

    /**
     * Creates a legacy index job by making an entry into the eg_indexer_job and returns response with job identifiers.
     *
     * @param legacyIndexRequest
     * @return
     */
    public LegacyIndexResponse createLegacyIndexJob(LegacyIndexRequest legacyIndexRequest) {
        Map<String, Mapping> mappingsMap = runner.getMappingMaps();
        StringBuilder url = new StringBuilder();
        Index index = mappingsMap.get(legacyIndexRequest.getLegacyIndexTopic()).getIndexes().get(0);
        url.append(esHostUrl).append(index.getName()).append("/").append(index.getType()).append("/_search");

        // Initialize legacy index job
        LegacyIndexJob job = (LegacyIndexJob) LegacyIndexJob.builder()
                .jobId(UUID.randomUUID().toString())
                .jobStatus(StatusEnum.INPROGRESS)
                .typeOfJob(ConfigKeyEnum.LEGACYINDEX)
                .requesterId(legacyIndexRequest.getRequestInfo().getUserInfo().getUuid())
                .newIndex(index.getName() + "/" + index.getType())
                .tenantId(legacyIndexRequest.getTenantId())
                .totalRecordsIndexed(0).totalTimeTakenInMS(0L)
                .auditDetails(indexerUtils.getAuditDetails(legacyIndexRequest.getRequestInfo().getUserInfo().getUuid(), true))
                .build();

        // Publish legacy index job to be persisted on database
        indexerProducer.producer("save-legacy-index-job", job);

        // Enrich legacy index request with the assigned job id
        legacyIndexRequest.setJobId(job.getJobId());
        legacyIndexRequest.setStartTime(new Date().getTime());

        // Invoke method to create chunk jobs once main job is initialized
        createLegacyIndexChunkJobs(legacyIndexRequest);

        // Prepare legacy index response
        LegacyIndexResponse legacyindexResponse = LegacyIndexResponse.builder()
                .message("Please hit the 'url' after the legacy index job is complete.")
                .url(url.toString())
                .responseInfo(factory.createResponseInfoFromRequestInfo(legacyIndexRequest.getRequestInfo(), true))
                .jobId(job.getJobId())
                .build();

        return legacyindexResponse;
    }

    /**
     * Index thread which performs the indexing job. It operates as follows: 1.
     * Based on the Request, it makes API calls in batches to the external service
     * 2. With every batch fetched, data is sent to child threads for processing 3.
     * Child threads perform primary data transformation if required and then hand
     * it over to another esIndexer method 4. The esIndexer method performs checks
     * and transformations pas per the config and then posts the data to es in bulk
     * 5. The process repeats until all the records are indexed.
     *
     * @param legacyIndexRequest
     */
    public void createLegacyIndexChunkJobs(LegacyIndexRequest legacyIndexRequest) {

        log.info("Job Started: " + legacyIndexRequest.getJobId());
        ObjectMapper mapper = indexerUtils.getObjectMapper();

        // Initialize offset and count
        Integer offset = legacyIndexRequest.getApiDetails().getPaginationDetails().getStartingOffset();
        offset = offset == null ? 0 : offset;

        // Make module _count call to fetch total number of records for a given tenantId
        Integer totalCount = 75;
        // #############################################################################

        Integer size = null != legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxPageSize()
                ? legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxPageSize()
                : defaultPageSizeForLegacyindex;

        while (offset < totalCount) {
            legacyIndexRequest.getApiDetails().getPaginationDetails().setStartingOffset(offset);
            indexerProducer.producer("legacy-index-chunk-jobs-topic", legacyIndexRequest);
            offset += size;
        }

        // Mark the legacy index job as completed once all chunks have been emitted
        IndexJob job = prepareIndexJobEventForUpdatingStatus(legacyIndexRequest, StatusEnum.COMPLETED);

        // Emit update event to mark the legacy index job as COMPLETED
        indexerProducer.producer("update-legacy-index-job", job);

    }

    public void processChunkJob(LegacyIndexRequest legacyIndexRequest) {
        Integer offset = legacyIndexRequest.getApiDetails().getPaginationDetails().getStartingOffset();
        Integer size = legacyIndexRequest.getApiDetails().getPaginationDetails().getMaxPageSize();

        String uri = indexerUtils.buildPagedUriForLegacyIndex(legacyIndexRequest.getApiDetails(), offset, size);

        try {
            // Get request body for external uri call to fetch legacy data.
            Object request = prepareRequestForExternalUriCall(legacyIndexRequest);

            // Make a paged call to the concerned service to fetch legacy data.
            Object response = restTemplate.postForObject(uri, request, Map.class);

            // If response is empty, emit event to mark job as failed.
            if (ObjectUtils.isEmpty(response)) {
                log.info("Request: " + request);
                log.info("URI: " + uri);

                // If exception occurs while fetching legacy data, emit event to mark job as failed.
                IndexJob job = prepareIndexJobEventForUpdatingStatus(legacyIndexRequest, StatusEnum.FAILED);
                indexerProducer.producer("update-legacy-index-job", job);

                // Persist failed chunk job details in database.
                indexerProducer.producer("save-failed-chunk-job", legacyIndexRequest);

                return;
            } else {
                List<Object> searchResponse = JsonPath.read(response, legacyIndexRequest.getApiDetails().getResponseJsonPath());
                if (!CollectionUtils.isEmpty(searchResponse)) {
                    prepareAndEmitLegacyDataToKafka(legacyIndexRequest, indexerUtils.getObjectMapper(), response);
                }
            }
        } catch (Exception e) {
            log.info("JOB FAILED!!! Offset: " + offset + " Size: " + size);
            log.error("Legacy Index Exception: ", e);

            // If exception occurs while fetching legacy data, emit event to mark job as failed.
            IndexJob job = prepareIndexJobEventForUpdatingStatus(legacyIndexRequest, StatusEnum.FAILED);
            indexerProducer.producer("update-legacy-index-job", job);

            // Persist failed chunk job details in database.
            indexerProducer.producer("save-failed-chunk-job", legacyIndexRequest);

            return;
        }

    }

    public void prepareAndEmitLegacyDataToKafka(LegacyIndexRequest legacyIndexRequest, ObjectMapper mapper, Object response) {
        try {
            if (legacyIndexRequest.getLegacyIndexTopic().equals(pgrLegacyTopic)) {
                ServiceResponse serviceResponse = mapper.readValue(mapper.writeValueAsString(response),
                        ServiceResponse.class);
                //PGRIndexObject indexObject = pgrCustomDecorator.dataTransformationForPGR(serviceResponse);
                //log.info("childThreadExecutor + indexObject----"+mapper.writeValueAsString(indexObject));
                indexerProducer.producer(legacyIndexRequest.getLegacyIndexTopic(), serviceResponse);
            } else {
                if (legacyIndexRequest.getLegacyIndexTopic().equals(ptLegacyTopic)) {
                    PropertyResponse propertyResponse = mapper.readValue(mapper.writeValueAsString(response), PropertyResponse.class);
                    propertyResponse.setProperties(ptCustomDecorator.transformData(propertyResponse.getProperties()));
                    indexerProducer.producer(legacyIndexRequest.getLegacyIndexTopic(), propertyResponse);
                } else {
                    indexerService.esIndexer(legacyIndexRequest.getLegacyIndexTopic(), mapper.writeValueAsString(response));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while processing legacy index", e);
        }
    }

    private Object prepareRequestForExternalUriCall(LegacyIndexRequest legacyIndexRequest) {
        Object request = legacyIndexRequest.getApiDetails().getRequest();

        // Prepare request if it is not provided in legacy index request
        if (ObjectUtils.isEmpty(request)) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(REQUEST_INFO_IN_PASCAL_CASE, legacyIndexRequest.getRequestInfo());
            request = map;
        }

        return request;
    }

    private IndexJob prepareIndexJobEventForUpdatingStatus(LegacyIndexRequest legacyIndexRequest, StatusEnum status) {
        return IndexJob.builder().jobId(legacyIndexRequest.getJobId())
                .totalTimeTakenInMS(new Date().getTime() - legacyIndexRequest.getStartTime())
                .jobStatus(status)
                .build();
    }
}


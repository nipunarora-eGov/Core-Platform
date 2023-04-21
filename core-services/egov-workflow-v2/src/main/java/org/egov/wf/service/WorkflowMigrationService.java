package org.egov.wf.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.WorKflowRepository;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkflowMigrationService {

    @Autowired
    private WorKflowRepository workflowRepository;
    @Autowired
    private WorkflowConfig config;
    @Autowired
    private Producer producer;


    /**
     * Function migrates current processInstances from existing history table to current table.
     * @param migrationRequest
     * @return
     */

    public void migrate(MigrationRequest migrationRequest) {
        Integer count = migrationRequest.getCriteria().getOffset() == null ? 0 : migrationRequest.getCriteria().getOffset();
        migrationRequest.getCriteria().setOffset(count);
        migrationRequest.getCriteria().setLimit(config.getMaxSearchLimit());

        while(true) {
            try {
                migrationRequest.getCriteria().setOffset(count);
                List<ProcessInstance> processInstances = workflowRepository.getProcessInstancesForMigration(migrationRequest.getCriteria());

                if (CollectionUtils.isEmpty(processInstances)) {
                    updateMigrationJob(migrationRequest, count, MigrationStatus.COMPLETE);
                    break;
                }

                count += processInstances.size();
                ProcessInstanceRequest processInstanceRequest = ProcessInstanceRequest.builder().processInstances(processInstances).build();
                producer.push(processInstances.get(0).getTenantId(), config.getPersisterMigrationTopic(), processInstanceRequest);
                log.info("Total number of records migrated: " + count);
                updateMigrationJob(migrationRequest, count, MigrationStatus.INPROGRESS);
            }
            catch (Exception exception){
                log.error(exception.toString());
                exception.printStackTrace();
                updateMigrationJob(migrationRequest, count, MigrationStatus.FAILED);
                break;
            }
        }

    }

    public MigrationResponse migrateToActiveDB(MigrationRequest migrationRequest) {
        if(ObjectUtils.isEmpty(migrationRequest.getCriteria().getTenantId()))
            throw new CustomException("EG_WF_CRITERIA_ERR", "TenantId is mandatory for searching workflow");

//      search for migration job, and if not already present set a new migration job.
        getMigrationJobId(migrationRequest);
//      Getting the Migration Job started
        producer.push(migrationRequest.getCriteria().getTenantId(), config.getMigrationTopic(), migrationRequest);
//      Initializing Migration Job Status
        updateMigrationJob(migrationRequest, 0 , MigrationStatus.INPROGRESS);

        MigrationResponse migrationResponse = MigrationResponse.builder().id(migrationRequest.getJobId())
                .message("Migration started successfully with job ID " + migrationRequest.getJobId())
                .tenantId(migrationRequest.getCriteria().getTenantId()).build();

        return migrationResponse;
    }

    public void updateMigrationJob(MigrationRequest migrationRequest, Integer count, MigrationStatus migrationStatus){
        MigrationJob migrationJob = MigrationJob.builder().id(migrationRequest.getJobId())
                .tenantId(migrationRequest.getCriteria().getTenantId())
                .startOffset(migrationRequest.getCriteria().getOffset())
                .endOffset(count)
                .createdTime(System.currentTimeMillis())
                .totalNumberOfRecordsMigrated(count)
                .migrationStatus(migrationStatus).build();
        log.info(migrationJob.toString());
        producer.push(migrationRequest.getCriteria().getTenantId(), config.getMigrationJob(), migrationJob);
    }

    public void getMigrationJobId(MigrationRequest migrationRequest){
        List<MigrationJob> existingMigrationJobs = workflowRepository.searchMigrationJob(migrationRequest.getCriteria().getTenantId());
        Optional<MigrationJob> latestFailedJob = existingMigrationJobs.stream().filter(job -> job.getMigrationStatus().compareTo(MigrationStatus.FAILED) == 0).sorted(Comparator.comparing(job -> -job.getCreatedTime())).findFirst();

        if(!latestFailedJob.isPresent()){
            migrationRequest.setJobId(UUID.randomUUID().toString());
        } else {
            MigrationJob latestMigrationJob = latestFailedJob.get();
            migrationRequest.setJobId(latestMigrationJob.getId());
            migrationRequest.getCriteria().setOffset(latestMigrationJob.getTotalNumberOfRecordsMigrated());
        }

    }
}



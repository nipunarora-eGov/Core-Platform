package org.egov.errorretryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.errorretryservice.models.ErrorDetailSearchRequest;
import org.egov.errorretryservice.models.ErrorRetryRequest;
import org.egov.errorretryservice.models.ErrorRetryResponse;
import org.egov.errorretryservice.producer.Producer;
import org.egov.errorretryservice.repository.ServiceRequestRepository;
import org.egov.errorretryservice.repository.querybuilder.QueryBuilder;
import org.egov.errorretryservice.utils.ResponseInfoFactory;
import org.egov.errorretryservice.validators.ErrorRetryValidator;
import org.egov.tracer.model.ErrorDetailDTO;
import org.egov.tracer.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.errorretryservice.constants.ERConstants.*;

@Service
@Slf4j
public class ErrorRetryService {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ErrorRetryValidator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Producer producer;

    @Autowired
    private QueryBuilder queryBuilder;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Value("${error.queue.kafka.topic}")
    private String errorTopic;

    /**
     * This method fetches error detail stored in database, validates retry attempt and
     * tries to re-consume the API resource corresponding to the error request.
     *
     * @param errorRetryRequest
     * @return
     */
    public ResponseEntity<ErrorRetryResponse> attemptErrorRetry(ErrorRetryRequest errorRetryRequest){

        // Route request to ES to search for the error entry
        Object request = queryBuilder.prepareRequestBodyForESSearch(errorRetryRequest.getId());
        Object response = serviceRequestRepository.fetchResult(queryBuilder.getErrorIndexEsUri(), request);

        // Parse ES response to get error detail object.
        List<ErrorDetailDTO> listOfErrorObjects = objectMapper.convertValue(JsonPath.read(response, DATA_JSONPATH), List.class);
        ErrorDetailDTO errorObject = objectMapper.convertValue(listOfErrorObjects.get(0), ErrorDetailDTO.class);

        // Validate retry attempt.
        Map<String, Object> responseMap = validator.validateRetryAttempt(errorObject);

        if(CollectionUtils.isEmpty(responseMap)){
            incrementRetryCount(errorObject);
            try {
                // Attempt retrying request
                Object apiBody = objectMapper.readValue(errorObject.getApiDetails().getRequestBody(), Map.class);
                serviceRequestRepository.fetchResult(new StringBuilder(errorObject.getApiDetails().getUrl()), apiBody);

                // Update status if request goes through successfully
                errorObject.setStatus(Status.SUCCESSFUL);
                producer.push(errorTopic, Collections.singletonList(errorObject));
            } catch (Exception ex) {
                // Update error object in the index with incremented retry count in case request fails
                errorObject.setStatus(Status.UNSUCCESSFUL);
                producer.push(errorTopic, Collections.singletonList(errorObject));
            }
        } else{
            ErrorRetryResponse errorRetryResponse = prepareErrorRetryResponse(errorRetryRequest.getRequestInfo(), errorRetryRequest.getId(), ERROR_RETRY_ATTEMPT_FAILURE_MSG, responseMap);
            return new ResponseEntity<>(errorRetryResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        responseMap.put(ERROR_RETRY_ATTEMPT_SUCCESSFUL_CODE, ERROR_RETRY_ATTEMPT_SUCCESSFUL_MSG);
        ErrorRetryResponse errorRetryResponse = prepareErrorRetryResponse(errorRetryRequest.getRequestInfo(), errorRetryRequest.getId(), ERROR_RETRY_ATTEMPT_SUCCESSFUL_MSG, responseMap);

        return new ResponseEntity<>(errorRetryResponse, HttpStatus.ACCEPTED);
    }

    /**
     * This method prepares request body for searching error details based on the provided
     * search criteria and returns them.
     *
     * @param errorDetailSearchRequest
     * @return
     */
    public List<ErrorDetailDTO> search(ErrorDetailSearchRequest errorDetailSearchRequest){

        // If search criteria is empty, return empty list.
        if(ObjectUtils.isEmpty(errorDetailSearchRequest.getErrorDetailSearchCriteria().getId()) && ObjectUtils.isEmpty(errorDetailSearchRequest.getErrorDetailSearchCriteria().getErrorDetailUuid()))
            return new ArrayList<>();

        // If search criteria is not empty, prepare request body with required filters to fetch error details from ES.
        Object request = queryBuilder.prepareRequestForErrorDetailsSearch(errorDetailSearchRequest);

        // REST call to ES database.
        Object response = serviceRequestRepository.fetchResult(queryBuilder.getErrorIndexEsUri(), request);

        // Convert search response to list of error details to be returned.
        List<ErrorDetailDTO> listOfErrorObjects = objectMapper.convertValue(JsonPath.read(response, DATA_JSONPATH), List.class);

        return listOfErrorObjects;
    }

    /**
     * This method increments retry count for incoming error object.
     *
     * @param errorObject
     */
    private void incrementRetryCount(ErrorDetailDTO errorObject) {
        Integer retryCount = errorObject.getRetryCount();
        retryCount = retryCount + 1;
        errorObject.setRetryCount(retryCount);
    }

    private ErrorRetryResponse prepareErrorRetryResponse(RequestInfo requestInfo, String id, String message, Map<String, Object> responseMap) {
        return ErrorRetryResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo))
                .id(id)
                .message(message)
                .responseMap(responseMap)
                .build();
    }
}

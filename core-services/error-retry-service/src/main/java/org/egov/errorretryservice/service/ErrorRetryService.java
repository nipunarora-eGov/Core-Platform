package org.egov.errorretryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.errorretryservice.models.ErrorDetailSearchRequest;
import org.egov.errorretryservice.models.ErrorRetryRequest;
import org.egov.errorretryservice.producer.Producer;
import org.egov.errorretryservice.repository.ServiceRequestRepository;
import org.egov.errorretryservice.repository.querybuilder.QueryBuilder;
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

    @Value("${error.queue.kafka.topic}")
    private String errorTopic;

    public ResponseEntity attemptErrorRetry(ErrorRetryRequest errorRetryRequest){

        // Route request to ES to search for the error entry
        Object request = queryBuilder.prepareRequestBodyForESSearch(errorRetryRequest.getId());
        Object response = serviceRequestRepository.fetchResult(queryBuilder.getErrorIndexEsUri(), request);
        
        List<ErrorDetailDTO> listOfErrorObjects = objectMapper.convertValue(JsonPath.read(response, DATA_JSONPATH), List.class);
        ErrorDetailDTO errorObject = objectMapper.convertValue(listOfErrorObjects.get(0), ErrorDetailDTO.class);

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
            return new ResponseEntity(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        responseMap.put("EG_RETRY_ATTEMPT_SUCCESSFUL", "Error retry attempted successfully.");
        return new ResponseEntity(responseMap, HttpStatus.ACCEPTED);
    }

    public List<ErrorDetailDTO> search(ErrorDetailSearchRequest errorDetailSearchRequest){

        if(ObjectUtils.isEmpty(errorDetailSearchRequest.getErrorDetailSearchCriteria().getId()) && ObjectUtils.isEmpty(errorDetailSearchRequest.getErrorDetailSearchCriteria().getErrorDetailUuid()))
            return new ArrayList<>();

        Object request = queryBuilder.prepareRequestForErrorDetailsSearch(errorDetailSearchRequest);
        log.info(request.toString());
        Object response = serviceRequestRepository.fetchResult(queryBuilder.getErrorIndexEsUri(), request);

        List<ErrorDetailDTO> listOfErrorObjects = objectMapper.convertValue(JsonPath.read(response, DATA_JSONPATH), List.class);

        return listOfErrorObjects;
    }

    private void incrementRetryCount(ErrorDetailDTO errorObject) {
        Integer retryCount = errorObject.getRetryCount();
        retryCount = retryCount + 1;
        errorObject.setRetryCount(retryCount);
    }
}

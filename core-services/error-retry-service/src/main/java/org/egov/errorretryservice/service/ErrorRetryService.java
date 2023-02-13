package org.egov.errorretryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.errorretryservice.models.ErrorRetryRequest;
import org.egov.errorretryservice.producer.Producer;
import org.egov.errorretryservice.repository.ServiceRequestRepository;
import org.egov.errorretryservice.repository.querybuilder.QueryBuilder;
import org.egov.errorretryservice.validators.ErrorRetryValidator;
import org.egov.tracer.model.ErrorQueueContractDTO;
import org.egov.tracer.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        
        List<ErrorQueueContractDTO> listOfErrorObjects = objectMapper.convertValue(JsonPath.read(response, DATA_JSONPATH), List.class);
        ErrorQueueContractDTO errorObject = objectMapper.convertValue(listOfErrorObjects.get(0), ErrorQueueContractDTO.class);

        Map<String, Object> responseMap = validator.validateRetryAttempt(errorObject);

        if(CollectionUtils.isEmpty(responseMap)){
            incrementRetryCount(errorObject);
            try {
                // Attempt retrying request
                serviceRequestRepository.fetchResult(new StringBuilder(errorObject.getSource()), errorObject.getBody());

                // Update status if request goes through successfully
                errorObject.setStatus(Status.SUCCESSFUL);
                producer.push(errorTopic, errorObject);
            } catch (Exception ex) {
                // Update error object in the index with incremented retry count in case request fails
                errorObject.setStatus(Status.UNSUCCESSFUL);
                producer.push(errorTopic, errorObject);
            }
        } else{
            return new ResponseEntity(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        responseMap.put("EG_RETRY_ATTEMPT_SUCCESSFUL", "Error retry attempted successfully.");
        return new ResponseEntity(responseMap, HttpStatus.ACCEPTED);
    }

    private void incrementRetryCount(ErrorQueueContractDTO errorObject) {
        Integer retryCount = errorObject.getRetryCount();
        retryCount = retryCount + 1;
        errorObject.setRetryCount(retryCount);
    }
}

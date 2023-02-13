package org.egov.errorretryservice.validators;

import org.egov.tracer.model.ErrorQueueContractDTO;
import org.egov.tracer.model.ErrorType;
import org.egov.tracer.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorRetryValidator {

    @Value("${max.retries.allowed}")
    private Integer maxRetries;

    public Map validateRetryAttempt(ErrorQueueContractDTO errorObject){
        Map<String, Object> responseMap = new HashMap<>();

        if(!ObjectUtils.isEmpty(errorObject.getStatus()) && errorObject.getStatus().equals(Status.SUCCESSFUL)){
            responseMap.put("EG_RETRY_ATTEMPT_FAILURE", "Cannot attempt to retry successful error entry.");
        }

        if(errorObject.getErrorType().equals(ErrorType.NON_RECOVERABLE)){
            responseMap.put("EG_RETRY_ATTEMPT_FAILURE", "Cannot retry NON RECOVERABLE errors.");
        }

        if(errorObject.getRetryCount() > maxRetries){
            responseMap.put("EG_RETRY_ATTEMPT_FAILURE", "Cannot attempt to retry error beyond - " + maxRetries + " times.");
        }

        return responseMap;

    }
}

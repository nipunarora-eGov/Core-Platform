package org.egov.errorretryservice.validators;

import org.egov.tracer.model.ErrorDetailDTO;
import org.egov.tracer.model.ErrorType;
import org.egov.tracer.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static org.egov.errorretryservice.constants.ERConstants.*;

@Component
public class ErrorRetryValidator {

    @Value("${max.retries.allowed}")
    private Integer maxRetries;

    public Map validateRetryAttempt(ErrorDetailDTO errorObject){
        Map<String, Object> responseMap = new HashMap<>();

        if(!ObjectUtils.isEmpty(errorObject.getStatus()) && errorObject.getStatus().equals(Status.SUCCESS)){
            responseMap.put(ERROR_RETRY_ATTEMPT_FAILURE_CODE, ERROR_RETRY_ATTEMPT_STATUS_VALIDATION_FAILURE_MSG);
        }

        errorObject.getErrors().forEach(errorEntity -> {
            if(errorEntity.getErrorType().equals(ErrorType.NON_RECOVERABLE)){
                responseMap.put(ERROR_RETRY_ATTEMPT_FAILURE_CODE, ERROR_RETRY_ATTEMPT_ERROR_TYPE_VALIDATION_FAILURE_MSG);
            }
        });

        if(errorObject.getRetryCount() >= maxRetries){
            responseMap.put(ERROR_RETRY_ATTEMPT_FAILURE_CODE, "Cannot attempt to retry error beyond - " + maxRetries + " times.");
        }

        return responseMap;

    }
}

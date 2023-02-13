package org.egov.errorretryconsumerpipeline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.errorretryconsumerpipeline.models.ErrorRetryRequest;
import org.egov.errorretryconsumerpipeline.service.ErrorRetryService;
import org.egov.errorretryconsumerpipeline.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/retry")
public class ErrorRetryController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private ErrorRetryService errorRetryService;


    @RequestMapping(value="/_attempt", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody @Valid ErrorRetryRequest errorRetryRequest) {
        //log.info(errorRetryRequest.getId());
        ResponseEntity responseEntity = errorRetryService.attemptErrorRetry(errorRetryRequest);
        return responseEntity;
    }
}

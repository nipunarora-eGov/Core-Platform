package org.egov.errorretryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.errorretryservice.models.ErrorDetailSearchRequest;
import org.egov.errorretryservice.models.ErrorDetailSearchResponse;
import org.egov.errorretryservice.models.ErrorRetryRequest;
import org.egov.errorretryservice.service.ErrorRetryService;
import org.egov.errorretryservice.utils.ResponseInfoFactory;
import org.egov.tracer.model.ErrorDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

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
        ResponseEntity responseEntity = errorRetryService.attemptErrorRetry(errorRetryRequest);
        return responseEntity;
    }

    @RequestMapping(value="/_search", method = RequestMethod.POST)
    public ResponseEntity<?> search(@RequestBody @Valid ErrorDetailSearchRequest errorDetailSearchRequest) {
        List<ErrorDetailDTO> errorDetailsList = errorRetryService.search(errorDetailSearchRequest);
        ErrorDetailSearchResponse errorDetailSearchResponse = ErrorDetailSearchResponse.builder().errorDetailList(errorDetailsList).build();
        return new ResponseEntity(errorDetailSearchResponse, HttpStatus.ACCEPTED);
    }
}

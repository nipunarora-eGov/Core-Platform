package org.egov.infra.indexer.controller;

import org.egov.infra.indexer.producer.IndexerProducer;
import org.egov.infra.indexer.service.LegacyIndexService;
import org.egov.infra.indexer.service.LegacyIndexServiceV2;
import org.egov.infra.indexer.service.ReindexService;
import org.egov.infra.indexer.validator.Validator;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
import org.egov.infra.indexer.web.contract.LegacyIndexResponse;
import org.egov.infra.indexer.web.contract.ReindexRequest;
import org.egov.infra.indexer.web.contract.ReindexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/index-operations")
public class IndexerControllerV2 {

    public static final Logger logger = LoggerFactory.getLogger(IndexerController.class);

    @Autowired
    private IndexerProducer indexerProducer;

    @Autowired
    private LegacyIndexServiceV2 legacyIndexServiceV2;

    @Autowired
    private Validator validator;

    @PostMapping("/v2/_legacyindex")
    @ResponseBody
    public ResponseEntity<?> legacyIndexDataV2(@Valid @RequestBody LegacyIndexRequest legacyIndexRequest){
        validator.validaterLegacyindexRequest(legacyIndexRequest);
        LegacyIndexResponse response = legacyIndexServiceV2.createLegacyIndexJob(legacyIndexRequest);
        return new ResponseEntity<>(response ,HttpStatus.OK);

    }
}

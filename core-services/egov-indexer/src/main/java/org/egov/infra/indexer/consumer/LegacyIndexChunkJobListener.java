package org.egov.infra.indexer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.indexer.service.LegacyIndexServiceV2;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class LegacyIndexChunkJobListener {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LegacyIndexServiceV2 legacyIndexServiceV2;

    @KafkaListener(topics = {"${egov.ss.document.create.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {

            LegacyIndexRequest request = mapper.convertValue(record, LegacyIndexRequest.class);
            //log.info(request.toString());
            legacyIndexServiceV2.processChunkJob(request);

        } catch (final Exception e) {

            log.error("Error while listening to value: " + record + " on topic: " + topic + ": ", e);
        }
    }

}

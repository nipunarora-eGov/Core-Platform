package org.egov.errorretryconsumerpipeline.repository.querybuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.egov.errorretryconsumerpipeline.constants.ERConstants.*;
import static org.egov.errorretryconsumerpipeline.constants.ERConstants.SEARCH_ENDPOINT;

@Component
public class QueryBuilder {

    @Value("${egov.es.host}")
    private String esHost;

    @Value("${egov.error.index}")
    private String errorIndex;

    public Object prepareRequestBodyForESSearch(String id) {
        Map<String, Object> request = new HashMap<>();

        request.put(QUERY, new HashMap<>());
        Map<String, Object> queryClause = (Map<String, Object>) request.get(QUERY);
        queryClause.put(IDS, new HashMap<>());
        Map<String, Object> idsClause = (Map<String, Object>) queryClause.get(IDS);
        idsClause.put(VALUES, Collections.singletonList(id));

        return  request;
    }

    public StringBuilder getErrorIndexEsUri(){
        return new StringBuilder(esHost).append(errorIndex).append(SEARCH_ENDPOINT);
    }
}

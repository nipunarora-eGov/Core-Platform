package org.egov.errorretryservice.repository.querybuilder;

import lombok.extern.slf4j.Slf4j;
import org.egov.errorretryservice.config.ErrorRetryPipelineConfiguration;
import org.egov.errorretryservice.models.ErrorDetailSearchCriteria;
import org.egov.errorretryservice.models.ErrorDetailSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static org.egov.errorretryservice.constants.ERConstants.*;
import static org.egov.errorretryservice.constants.ERConstants.SEARCH_ENDPOINT;

@Slf4j
@Component
public class QueryBuilder {

    @Value("${egov.es.host}")
    private String esHost;

    @Value("${egov.error.index}")
    private String errorIndex;

    @Autowired
    private ErrorRetryPipelineConfiguration configuration;

    public Object prepareRequestBodyForESSearch(String id) {
        Map<String, Object> request = new HashMap<>();

        request.put(QUERY, new HashMap<>());
        Map<String, Object> queryClause = (Map<String, Object>) request.get(QUERY);
        queryClause.put(IDS, new HashMap<>());
        Map<String, Object> idsClause = (Map<String, Object>) queryClause.get(IDS);
        idsClause.put(VALUES, Collections.singletonList(id));

        return  request;
    }

    public Object prepareRequestForErrorDetailsSearch(ErrorDetailSearchRequest errorDetailSearchRequest){
        Map<String, Object> baseEsQuery = getBaseESQueryBody(errorDetailSearchRequest, Boolean.TRUE);
        Map<String, Object> innerBoolClause = (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);

        addErrorDetailSearchCriteriaToBaseQuery(errorDetailSearchRequest.getErrorDetailSearchCriteria(), mustClauseList);

        innerBoolClause.put(MUST_KEY, mustClauseList);

        return baseEsQuery;

    }

    public StringBuilder getErrorIndexEsUri(){
        return new StringBuilder(esHost).append(errorIndex).append(SEARCH_ENDPOINT);
    }

    private Map<String, Object> getBaseESQueryBody(ErrorDetailSearchRequest searchRequest, Boolean isPaginationRequired){
        Map<String, Object> baseEsQuery = new HashMap<>();
        Map<String, Object> boolQuery = new HashMap<>();

        // Prepare bool query
        boolQuery.put(BOOL_KEY, new HashMap<>());
        Map<String, Object> innerBoolBody = (Map<String, Object>) boolQuery.get(BOOL_KEY);
        innerBoolBody.put(MUST_KEY, new ArrayList<>());

        // Prepare base ES query
        if(isPaginationRequired) {
            baseEsQuery.put(FROM_KEY, ObjectUtils.isEmpty(searchRequest.getErrorDetailSearchCriteria().getOffset()) ? configuration.getDefaultOffset() : searchRequest.getErrorDetailSearchCriteria().getOffset());
            baseEsQuery.put(SIZE_KEY, ObjectUtils.isEmpty(searchRequest.getErrorDetailSearchCriteria().getLimit()) ? configuration.getDefaultLimit() : searchRequest.getErrorDetailSearchCriteria().getLimit());
        }
        baseEsQuery.put(QUERY, boolQuery);

        return baseEsQuery;
    }

    private Object prepareMustClauseChild(String path, Object param) {

        // Add terms clause in case the search criteria has a list of values
        if (param instanceof List) {
            Map<String, Object> termsClause = new HashMap<>();
            termsClause.put(TERMS_KEY, new HashMap<>());
            Map<String, Object> innerTermsClause = (Map<String, Object>) termsClause.get(TERMS_KEY);
            innerTermsClause.put(path, param);
            return termsClause;
        }
        // Add term clause in case the search criteria has a single value
        else {
            Map<String, Object> termClause = new HashMap<>();
            termClause.put(TERM_KEY, new HashMap<>());
            Map<String, Object> innerTermClause = (Map<String, Object>) termClause.get(TERM_KEY);
            innerTermClause.put(path, param);
            return termClause;
        }

    }

    private void addErrorDetailSearchCriteriaToBaseQuery(ErrorDetailSearchCriteria errorDetailSearchCriteria, List<Object> mustClauseList) {
        if(!ObjectUtils.isEmpty(errorDetailSearchCriteria.getId())){
            String key = "id";
            Map<String, Object> mustClauseChild = null;
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild("Data.apiDetails.id.keyword", errorDetailSearchCriteria.getId());
            if(CollectionUtils.isEmpty(mustClauseChild)){
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            }else {
                mustClauseList.add(mustClauseChild);
            }
        }

        if(!ObjectUtils.isEmpty(errorDetailSearchCriteria.getErrorDetailUuid())){
            String key = "errorDetailUuid";
            Map<String, Object> mustClauseChild = null;
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild("Data.uuid.keyword", errorDetailSearchCriteria.getErrorDetailUuid());
            if(CollectionUtils.isEmpty(mustClauseChild)){
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            }else {
                mustClauseList.add(mustClauseChild);
            }
        }

    }


}

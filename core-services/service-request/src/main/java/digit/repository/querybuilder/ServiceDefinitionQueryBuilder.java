package digit.repository.querybuilder;

import digit.web.models.ServiceDefinitionCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class ServiceDefinitionQueryBuilder {

    private static final String SELECT = " SELECT ";
    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String LEFT_JOIN  =  " LEFT OUTER JOIN ";
    private static final String AND_QUERY = " AND ";

    private static final String IDS_WRAPPER_QUERY = " SELECT uuid FROM ({HELPER_TABLE}) temp ";
    private final String ORDERBY_CREATEDTIME = " ORDER BY sd.createdtime DESC ";

    public String getServiceDefinitionsIdsQuery(ServiceDefinitionCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(SELECT + " DISTINCT(sd.id) ");
        query.append(" FROM eg_service_definition sd ");

        if(!ObjectUtils.isEmpty(criteria.getTenantId())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" sd.tenantid = ? ");
            preparedStmtList.add(criteria.getTenantId());
        }

        if(!CollectionUtils.isEmpty(criteria.getCode())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" sd.code IN ( ").append(createQuery(criteria.getCode())).append(" )");
            addToPreparedStatement(preparedStmtList, criteria.getCode());
        }

        if(!CollectionUtils.isEmpty(criteria.getIds())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" sd.id IN ( ").append(createQuery(criteria.getIds())).append(" )");
            addToPreparedStatement(preparedStmtList, criteria.getIds());
        }

        // Fetch service definitions which have NOT been soft deleted
        addClauseIfRequired(query, preparedStmtList);
        query.append(" sd.isActive = ? ");
        preparedStmtList.add(Boolean.TRUE);

        // order service definitions based on their createdtime in latest first manner
        query.append(ORDERBY_CREATEDTIME);

        // Pagination to limit results
        addPagination(query, preparedStmtList, criteria);

        return IDS_WRAPPER_QUERY.replace("{HELPER_TABLE}", query.toString());
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList){
        if(preparedStmtList.isEmpty()){
            query.append(" WHERE ");
        }else{
            query.append(" AND ");
        }
    }

    private String createQuery(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        ids.forEach(id -> {
            preparedStmtList.add(id);
        });
    }

    private void addPagination(StringBuilder query,List<Object> preparedStmtList,ServiceDefinitionCriteria criteria){
        int limit = config.getDefaultLimit();
        int offset = config.getDefaultOffset();
        query.append(" OFFSET ? ");
        query.append(" LIMIT ? ");

        if(criteria.getgetLimit()!=null && criteria.getLimit()<=config.getMaxSearchLimit())
            limit = criteria.getLimit();

        if(criteria.getLimit()!=null && criteria.getLimit()>config.getMaxSearchLimit())
            limit = config.getMaxSearchLimit();

        if(criteria.getOffset()!=null)
            offset = criteria.getOffset();

        preparedStmtList.add(offset);
        preparedStmtList.add(limit);

    }


}

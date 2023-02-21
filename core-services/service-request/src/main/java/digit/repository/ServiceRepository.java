package digit.repository;

import digit.repository.querybuilder.ServiceDefinitionQueryBuilder;
import digit.repository.rowmapper.ServiceDefinitionRowMapper;
import digit.web.models.Service;
import digit.web.models.ServiceCriteria;
import digit.web.models.ServiceDefinition;
import digit.web.models.ServiceDefinitionCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ServiceRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*@Autowired
    private ServiceDefinitionRowMapper serviceDefinitionRowMapper;*/

    @Autowired
    private ServiceDefinitionQueryBuilder serviceDefinitionQueryBuilder;


    public List<ServiceDefinition> getServiceDefinitions(ServiceDefinitionCriteria criteria) {


        List<Object> preparedStmtList = new ArrayList<>();

        if(CollectionUtils.isEmpty(criteria.getIds()) && ObjectUtils.isEmpty(criteria.getTenantId()) && CollectionUtils.isEmpty(criteria.getCode()))
            return new ArrayList<>();

        String query = serviceDefinitionQueryBuilder.getServiceDefinitionSearchQuery(criteria, preparedStmtList);
        log.info("query for search: " + query + " params: " + preparedStmtList);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), serviceDefinitionRowMapper);*/
        return new ArrayList<>();

    }

    public List<Service> getService(ServiceCriteria criteria) {
        return new ArrayList<>();
    }
}

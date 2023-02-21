package digit.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.AuditDetails;
import digit.web.models.AttributeDefinition;
import digit.web.models.ServiceDefinition;
import org.apache.commons.lang.StringUtils;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class ServiceDefinitionRowMapper implements ResultSetExtractor<List<ServiceDefinition>> {

    /**
     * Rowmapper that maps every column of the search result set to a key in the model.
     */

    @Autowired
    private ObjectMapper mapper;

    @Override
    public List<ServiceDefinition> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, ServiceDefinition> serviceDefinitionMap = new HashMap<>();
        while (rs.next()) {
            String currentId = rs.getString("id");
            ServiceDefinition currentServiceDefinition = serviceDefinitionMap.get(currentId);
            if (!StringUtils.isBlank(currentId)) {

                if (currentServiceDefinition == null) {

                    AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("createdby"))
                            .createdTime(rs.getLong("createdtime")).lastModifiedBy(rs.getString("lastmodifiedby"))
                            .lastModifiedTime(rs.getLong("lastmodifiedtime")).build();
                    currentServiceDefinition = ServiceDefinition.builder()
                            .id(rs.getString("id"))
                            .tenantId(rs.getString("tenantid"))
                            .code(rs.getString("code"))
                            .isActive(rs.getBoolean("isactive"))
                            .auditDetails(auditDetails)
                            .additionalDetails((PGobject) rs.getObject("additionaldetails"))
                            .build();

                }

                addAttributeDefinitions(rs, currentServiceDefinition);
                serviceDefinitionMap.put(currentId, currentServiceDefinition);
            }

        }
        return new ArrayList<>(serviceDefinitionMap.values());
    }

    private void addAttributeDefinitions(ResultSet rs, ServiceDefinition serviceDefinition) throws SQLException, DataAccessException {
        String values = rs.getString("avalues");
        AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("acreatedby"))
                .createdTime(rs.getLong("acreatedtime")).lastModifiedBy(rs.getString("alastmodifiedby"))
                .lastModifiedTime(rs.getLong("alastmodifiedtime")).build();
        AttributeDefinition attributeDefinition = AttributeDefinition.builder().id(rs.getString("aid"))
                .referenceId(rs.getString("areferenceid"))
                .tenantId(rs.getString("atenantid"))
                .code(rs.getString("acode"))
                .dataType(AttributeDefinition.DataTypeEnum.fromValue(rs.getString("adatatype")))
                .values(StringUtils.isEmpty(values) ? Arrays.asList() : Arrays.asList(values.split(",")))
                .isActive(rs.getBoolean("aisactive"))
                .required(rs.getBoolean("arequired"))
                .regEx(rs.getString("aregex"))
                .order(rs.getString("aorder"))
                .additionalDetails((PGobject) rs.getObject("aadditionaldetails"))
                .build();

        if (CollectionUtils.isEmpty(serviceDefinition.getAttributes())) {
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(attributeDefinition);
            serviceDefinition.setAttributes(attributeDefinitions);
        } else {
            serviceDefinition.getAttributes().add(attributeDefinition);
        }


    }

    private JsonNode getJsonValue(PGobject pGobject) {
        try {
            if (Objects.isNull(pGobject) || Objects.isNull(pGobject.getValue()))
                return null;
            else
                return mapper.readTree(pGobject.getValue());
        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "Exception occurred while parsing the additionalDetail json : " + e
                    .getMessage());
        }
    }

    private List<String> nodeToList(PGobject pGobject) {
        try {
            if (Objects.isNull(pGobject) || Objects.isNull(pGobject.getValue()))
                return null;
            else
                return mapper.readValue(pGobject.getValue(), new TypeReference<List<String>>() {
                });

        } catch (IOException e) {
            throw new CustomException("SERVER_ERROR", "Exception occurred while parsing the receiver json : " + e
                    .getMessage());
        }
    }

}

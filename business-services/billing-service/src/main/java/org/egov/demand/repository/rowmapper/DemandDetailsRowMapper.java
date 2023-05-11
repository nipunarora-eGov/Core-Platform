package org.egov.demand.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.web.contract.User;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DemandDetailsRowMapper implements ResultSetExtractor<List<DemandDetail>> {

    @Override
    public List<DemandDetail> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, DemandDetail> demandDetailMap = new LinkedHashMap<>();
        String demandDetailIdRsName = "dlid";

        while (rs.next()) {

            String demandDetailId = rs.getString(demandDetailIdRsName);
            DemandDetail demandDetail = demandDetailMap.get(demandDetailId);

            if (demandDetail == null) {

                demandDetail = new DemandDetail();
                demandDetail.setId(demandDetailId);
                demandDetail.setDemandId(rs.getString("dldemandid"));
                demandDetail.setTaxHeadMasterCode(rs.getString("dltaxheadcode"));
                demandDetail.setTenantId(rs.getString("dltenantid"));
                demandDetail.setTaxAmount(rs.getBigDecimal("dltaxamount"));
                demandDetail.setCollectionAmount(rs.getBigDecimal("dlcollectionamount"));
                AuditDetails dlauditDetail = new AuditDetails();
                dlauditDetail.setCreatedBy(rs.getString("dlcreatedby"));
                dlauditDetail.setCreatedTime(rs.getLong("dlcreatedtime"));
                dlauditDetail.setLastModifiedBy(rs.getString("dllastModifiedby"));
                dlauditDetail.setLastModifiedTime(rs.getLong("dllastModifiedtime"));
                demandDetail.setAuditDetails(dlauditDetail);

                demandDetailMap.put(demandDetail.getId(), demandDetail);
            }
        }
        return new ArrayList<>(demandDetailMap.values());
    }
}

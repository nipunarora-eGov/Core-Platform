package org.egov.wf.repository.rowmapper;

import org.egov.wf.web.models.BusinessService;
import org.egov.wf.web.models.MigrationJob;
import org.egov.wf.web.models.MigrationStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MigrationJobRowMapper implements ResultSetExtractor<List<MigrationJob>> {
    @Override
    public List<MigrationJob> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<MigrationJob> migrationJobs = new ArrayList<>();
        while (rs.next()) {
            MigrationJob migrationJob = MigrationJob.builder().id(rs.getString("id"))
                    .tenantId(rs.getString("tenantId"))
                    .startOffset(rs.getInt("startOffset"))
                    .endOffset(rs.getInt("endOffset"))
                    .totalNumberOfRecordsMigrated(rs.getInt("totalNumberOfRecordsMigrated"))
                    .migrationStatus(MigrationStatus.fromValue(rs.getString("status")))
                    .createdTime(rs.getLong("createdTime")).build();

            migrationJobs.add(migrationJob);
        }
        return migrationJobs;
    }
}

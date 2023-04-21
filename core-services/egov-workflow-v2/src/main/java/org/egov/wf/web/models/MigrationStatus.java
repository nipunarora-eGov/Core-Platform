package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MigrationStatus {
    INPROGRESS, COMPLETE, FAILED;

    @JsonCreator
    public static MigrationStatus fromValue(String text) {
        for (MigrationStatus migrationStatus : MigrationStatus.values()) {
            if (String.valueOf(migrationStatus).equalsIgnoreCase(text)) {
                return migrationStatus;
            }
        }
        return null;
    }
}

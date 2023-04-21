CREATE TABLE eg_wf_migration_job (
    id character varying(64),
    tenantId character varying(128),
    status character varying(128),
    startOffset SMALLINT,
    endOffset SMALLINT,
    totalNumberOfRecordsMigrated SMALLINT,
    createdTime bigint,
    PRIMARY KEY (id)
);
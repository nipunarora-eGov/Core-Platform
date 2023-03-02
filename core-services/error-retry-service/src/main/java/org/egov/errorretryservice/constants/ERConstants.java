package org.egov.errorretryservice.constants;

public class ERConstants {

    public static final String SEARCH_ENDPOINT = "/_search";

    public static final String DATA_JSONPATH = "$.hits.hits.*._source.Data";

    public static final String QUERY = "query";

    public static final String IDS = "ids";

    public static final String VALUES = "values";

    public static final String BOOL_KEY = "bool";

    public static final String MUST_KEY = "must";

    public static final String FROM_KEY = "from";

    public static final String SIZE_KEY = "size";

    public static final String TERMS_KEY = "terms";

    public static final String TERM_KEY = "term";

    public static final String ERROR_RETRY_ATTEMPT_SUCCESSFUL_CODE = "EG_RETRY_ATTEMPT_SUCCESSFUL";

    public static final String ERROR_RETRY_ATTEMPT_SUCCESSFUL_MSG = "Error retry attempted successfully.";

    public static final String ERROR_RETRY_ATTEMPT_FAILURE_CODE = "EG_RETRY_ATTEMPT_FAILURE";

    public static final String ERROR_RETRY_ATTEMPT_FAILURE_MSG = "Cannot attempt to retry successful error entry.";


}

package org.egov.filters.pre;

import static java.util.Objects.isNull;
import static org.egov.Utils.Utils.isRequestBodyCompatible;
import static org.egov.constants.RequestContextConstants.*;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.egov.Utils.Utils;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.exceptions.CustomException;
import org.egov.model.RequestBodyInspector;
import org.egov.wrapper.CustomRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 *  1st pre filter to get executed.
 *  Sets the context and MDC with the newly generated correlation id.
 */
public class CorrelationIdFilter extends ZuulFilter {
	
    private static final String RECEIVED_REQUEST_MESSAGE = "Received request for: {}";
    private static final String FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE = "Failed to enrich request body";
    private static final String SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE =
        "Skipped enriching request body since request info field is not present.";
    private static final String BODY_ENRICHED_MESSAGE = "Enriched request payload.";
    public static final String REQUEST_URI_FIELD_NAME = "requestUri";
    public static final String CONTENT_TYPE_FIELD_NAME = "contentType";

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private ObjectMapper objectMapper;
    private List<String> openEndpointsWhitelist;
    private List<String> mixedModeEndpointsWhitelist;
    
    @Autowired
    private Utils utils;
    
    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;
    
	@Autowired
	public CorrelationIdFilter(List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist,
			ObjectMapper objectMapper) {
		super();
		this.openEndpointsWhitelist = openEndpointsWhitelist;
		this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
		this.objectMapper = objectMapper;
	}

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws CustomException {
        RequestContext ctx = RequestContext.getCurrentContext();
        
        String requestURI = ctx.getRequest().getRequestURI();
        String contentType = ctx.getRequest().getContentType();
        Boolean isOpenRequest = openEndpointsWhitelist.contains(requestURI);
        Boolean isMixModeRequest = mixedModeEndpointsWhitelist.contains(requestURI);
        
		if (centralInstanceUtil.getIsEnvironmentCentralInstance() && (isOpenRequest || isMixModeRequest)
				&& !requestURI.equalsIgnoreCase("/user/oauth/token")) {
			/*
			 * Adding tenantid to header for open urls, authorized urls will get ovverrided
			 * in RBAC filter
			 */
			Set<String> tenantIds = getTenantIdsFromRequest();
			if (tenantIds.size() == 0 && isOpenRequest) {
				throw new CustomException("Unique value of tenantId must be given for open URI requests", 400,
						"multiple or No tenantids found in Request body");
			}
			String tenantId = utils.getLowLevelTenatFromSet(tenantIds);
			MDC.put(TENANTID_MDC, tenantId);
			ctx.set(TENANTID_MDC, tenantId);

		}

        final String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        ctx.set(CORRELATION_ID_KEY, correlationId);
        logger.debug(RECEIVED_REQUEST_MESSAGE, ctx.getRequest().getRequestURI());
        enrichApiDetailsInRequest(requestURI, contentType);
        return null;
    }

    private void enrichApiDetailsInRequest(String requestUri, String contentType) {
        if (!isRequestBodyCompatible(RequestContext.getCurrentContext().getRequest())) {
            return;
        }
        try {
            enrichRequestBody(requestUri, contentType);
        } catch (IOException e) {
            logger.error(FAILED_TO_ENRICH_REQUEST_BODY_MESSAGE, e);
            throw new org.egov.tracer.model.CustomException("FAILED_TO_ENRICH_REQUEST_BODY", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void enrichRequestBody(String requestUri, String contentType) throws IOException {
        RequestContext ctx = RequestContext.getCurrentContext();
        final RequestBodyInspector requestBodyInspector = getRequestBodyInspector(ctx);
        HashMap<String, Object> requestInfo = requestBodyInspector.getRequestInfo();
        if (requestInfo == null) {
            logger.info(SKIPPED_BODY_ENRICHMENT_DUE_TO_NO_KNOWN_FIELD_MESSAGE);
            return;
        }
        setRequestUri(requestInfo, requestUri);
        setContentType(requestInfo, contentType);
        requestBodyInspector.updateRequestInfo(requestInfo);
        CustomRequestWrapper requestWrapper = new CustomRequestWrapper(ctx.getRequest());
        requestWrapper.setPayload(objectMapper.writeValueAsString(requestBodyInspector.getRequestBody()));
        logger.info(BODY_ENRICHED_MESSAGE);
        ctx.setRequest(requestWrapper);
    }

    private RequestBodyInspector getRequestBodyInspector(RequestContext ctx) throws IOException {
        HashMap<String, Object> requestBody = getRequestBody(ctx);
        return new RequestBodyInspector(requestBody);
    }

    private HashMap<String, Object> getRequestBody(RequestContext ctx) throws IOException {
        String payload = IOUtils.toString(ctx.getRequest().getInputStream());
        return objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() { });
    }

    private void setRequestUri(HashMap<String, Object> requestInfo, String requestUri) {
        requestInfo.put(REQUEST_URI_FIELD_NAME, requestUri);
    }

    private void setContentType(HashMap<String, Object> requestInfo, String contentType) {
	    requestInfo.put(CONTENT_TYPE_FIELD_NAME, contentType);
    }

    private Set<String> getTenantIdsFromRequest() throws CustomException {

		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		Map<String, String[]> queryParams = request.getParameterMap();
		

		Set<String> tenantIds = new HashSet<>();

		if (Utils.isRequestBodyCompatible(request)) {

			try {
				ObjectNode requestBody = (ObjectNode) objectMapper.readTree(request.getInputStream());

				if (requestBody.has(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))
					requestBody.remove(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);

				else if (requestBody.has(REQUEST_INFO_FIELD_NAME_CAMEL_CASE))
					requestBody.remove(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);

				List<String> tenants = new LinkedList<>();

				for (JsonNode node : requestBody.findValues(REQUEST_TENANT_ID_KEY)) {
					if (node.getNodeType() == JsonNodeType.ARRAY) {
						node.elements().forEachRemaining(n -> tenants.add(n.asText()));
					} else if (node.getNodeType() == JsonNodeType.STRING) {
						tenants.add(node.asText());
					}
				}
				
				if (!tenants.isEmpty()) {
					// Filtering null tenantids will be removed once fix is done in TL service.
					tenants.forEach(tenant -> {
						if (tenant != null && !tenant.equalsIgnoreCase("null"))
							tenantIds.add(tenant);
					});
				} else {
					setTenantIdsFromQueryParams(queryParams, tenantIds);
				}

			} catch (IOException e) {
				throw new RuntimeException(new CustomException("REQUEST_PARSE_FAILED", HttpStatus.UNAUTHORIZED.value(),
						"Failed to parse request at" + " API gateway"));
			}
		} else {
			setTenantIdsFromQueryParams(queryParams, tenantIds);
		}

		return tenantIds;
	}

	private void setTenantIdsFromQueryParams(Map<String, String[]> queryParams, Set<String> tenantIds) throws CustomException {
		
		if (!isNull(queryParams) && queryParams.containsKey(REQUEST_TENANT_ID_KEY)
				&& queryParams.get(REQUEST_TENANT_ID_KEY).length > 0) {
			String tenantId = queryParams.get(REQUEST_TENANT_ID_KEY)[0];
			if (tenantId.contains(",")) {
				tenantIds.addAll(Arrays.asList(tenantId.split(",")));
			} else
				tenantIds.add(tenantId);

		}else {
			throw new CustomException("tenantId is mandatory in URL for non json requests", 400, "");
		}
	}
	
}
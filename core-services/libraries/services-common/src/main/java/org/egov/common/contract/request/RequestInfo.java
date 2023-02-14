package org.egov.common.contract.request;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RequestInfo {

    private String apiId;

    private String ver;

    private Long ts;

    private String action;

    private String did;

    private String key;

    private String msgId;

    private String authToken;

    private String correlationId;

    private PlainAccessRequest plainAccessRequest;

    private User userInfo;

    // Request URI is being added to requestInfo to aid in understanding the request source in case of async consumers like persister and indexer
    private String requestUri;

    // Content type is being added to requestInfo to aid in understanding the content type in case of async consumers like persister and indexer
    private String contentType;
}
package org.egov.errorretryconsumerpipeline.models;

import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ErrorRetryRequest {

    private String id;

    private RequestInfo requestInfo;

}

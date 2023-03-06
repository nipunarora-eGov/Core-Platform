package org.egov.errorretryservice.models;

import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ErrorRetryResponse {

    private String id;

    private ResponseInfo responseInfo;

    private String message;

    private Map<String, Object> responseMap;

}

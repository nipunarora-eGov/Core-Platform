package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiDetails {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("contentType")
    private String contentType = null;

    @JsonProperty("requestBody")
    private String requestBody = null;

    @JsonProperty("requestHeaders")
    private String requestHeaders = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

}

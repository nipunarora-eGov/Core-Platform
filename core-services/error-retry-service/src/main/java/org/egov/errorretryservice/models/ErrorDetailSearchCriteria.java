package org.egov.errorretryservice.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ErrorDetailSearchCriteria {

    @JsonProperty("id")
    private String id;

    @JsonProperty("errorDetailUuid")
    private String errorDetailUuid;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    @Max(value = 300)
    private Integer limit;

}

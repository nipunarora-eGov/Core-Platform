package org.egov.errorretryservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.tracer.model.ErrorDetailDTO;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorDetailSearchResponse {

    @JsonProperty("errorDetails")
    private List<ErrorDetailDTO> errorDetailList;

}

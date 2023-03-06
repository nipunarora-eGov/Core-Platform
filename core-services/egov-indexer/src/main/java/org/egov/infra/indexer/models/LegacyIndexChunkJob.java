package org.egov.infra.indexer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LegacyIndexChunkJob {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("referenceId")
    private String referenceId;


}

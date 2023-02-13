package org.egov.tracer.model;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorQueueContractDTO extends ErrorQueueContract{

    private Integer retryCount;
    private ErrorType errorType;
    private Status status;

}

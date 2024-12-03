package com.sawdust.controller.registration.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@RequiredArgsConstructor
@NoArgsConstructor
@Builder
@Data
@DynamoDbBean
public class ComputeRegistrationDTO {
    @NonNull
    @Getter(onMethod=@__({@DynamoDbPartitionKey}))
    String computeId;

    @NonNull
    String workflowName;

    @NonNull
    String certificate;
}

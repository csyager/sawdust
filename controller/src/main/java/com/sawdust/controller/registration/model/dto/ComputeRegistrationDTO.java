package com.sawdust.controller.registration.model.dto;

import com.sawdust.controller.registration.model.ComputeRegistrationStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import static com.sawdust.controller.registration.ComputeRegistrationRepository.WORKFLOW_INDEX;

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
    @Getter(onMethod=@__({
            @DynamoDbSecondaryPartitionKey(indexNames = WORKFLOW_INDEX)
    }))
    String workflowId;

    @NonNull
    String certificate;

    @NonNull
    ComputeRegistrationStatus computeRegistrationStatus;
}

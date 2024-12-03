package com.sawdust.controller.activities.model.dto;

import com.sawdust.controller.activities.model.WorkflowState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import static com.sawdust.controller.activities.ActivityRepository.INCOMPLETE_ACTIVITY_INDEX_NAME;

@AllArgsConstructor
@Builder
@Data
@DynamoDbBean
public class ActivityDTO {
    @NonNull
    @Getter(onMethod=@__({@DynamoDbPartitionKey}))
    String activityId;

    @NonNull
    String workflowName;

    @NonNull
    WorkflowState workflowState;

    @Getter(onMethod=@__({
        @DynamoDbSecondaryPartitionKey(indexNames = INCOMPLETE_ACTIVITY_INDEX_NAME)
    }))
    WorkflowState incompleteState;
}

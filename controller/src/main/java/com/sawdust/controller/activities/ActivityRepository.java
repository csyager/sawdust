package com.sawdust.controller.activities;

import com.sawdust.controller.activities.model.WorkflowState;
import com.sawdust.controller.activities.model.dto.ActivityDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivityRepository {
    public static final String INCOMPLETE_ACTIVITY_INDEX_NAME="IncompleteActivityIndex";

    @NonNull
    @Autowired
    private DynamoDbTable<ActivityDTO> table;

    public List<ActivityDTO> getIncompleteActivities(String workflowName) {
        return table.index(INCOMPLETE_ACTIVITY_INDEX_NAME).query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue(String.valueOf(WorkflowState.IN_PROGRESS))
                        .build()))
                .build()).
    }
}

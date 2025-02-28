package com.sawdust.controller.activities;

import com.sawdust.controller.activities.model.WorkflowState;
import com.sawdust.controller.activities.model.dto.ActivityDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivityRepository {
    public static final String INCOMPLETE_ACTIVITY_INDEX_NAME="IncompleteActivityIndex";

    @NonNull
    @Autowired
    private DynamoDbTable<ActivityDTO> table;

    /**
     * Gets one page of pending activities for the requested workflow.
     * @return list of {@link ActivityDTO}
     */
    public List<ActivityDTO> getPendingActivities() {
        final SdkIterable<Page<ActivityDTO>> queryResults = table.index(INCOMPLETE_ACTIVITY_INDEX_NAME).query(
                QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                            .partitionValue(String.valueOf(WorkflowState.PENDING))
                            .build()))
                    .build());
        if (!queryResults.iterator().hasNext()) {
            return Collections.emptyList();
        } else {
            return queryResults.iterator().next().items();
        }
    }
}

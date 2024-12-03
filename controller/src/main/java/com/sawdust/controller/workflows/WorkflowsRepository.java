package com.sawdust.controller.workflows;

import com.sawdust.controller.workflows.exceptions.WorkflowConflictException;
import com.sawdust.controller.workflows.model.dto.WorkflowDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Repository
@RequiredArgsConstructor
public class WorkflowsRepository {
    @NonNull
    @Autowired
    private DynamoDbTable<WorkflowDTO> table;

    public void createWorkflow(WorkflowDTO dto) {
        try {
            table.putItem(builder -> builder
                    .item(dto)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(workflowId)")
                            .build()));
        } catch (ConditionalCheckFailedException exception) {
            throw new WorkflowConflictException(dto.getWorkflowId());
        }
    }

    public WorkflowDTO getWorkflow(String workflowId) {
        return table.getItem(Key.builder()
                .partitionValue(workflowId)
                .build()
        );
    }

}

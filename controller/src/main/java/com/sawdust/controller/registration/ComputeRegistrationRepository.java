package com.sawdust.controller.registration;

import com.sawdust.controller.registration.exceptions.ComputeRegistrationConflictException;
import com.sawdust.controller.registration.model.ComputeRegistrationStatus;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ComputeRegistrationRepository {

    @NonNull
    @Autowired
    private DynamoDbTable<ComputeRegistrationDTO> table;

    public static final String WORKFLOW_INDEX = "WorkflowIndex";

    public ComputeRegistrationDTO createComputeRegistration(String workflowId, String certificate) {
        ComputeRegistrationDTO dto = new ComputeRegistrationDTO(
                UUID.randomUUID().toString(), workflowId, certificate, ComputeRegistrationStatus.FREE);
        try {
            table.putItem(builder -> builder
                    .item(dto)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(computeId)")
                            .build()));
            return dto;
        } catch (ConditionalCheckFailedException exception) {
            throw new ComputeRegistrationConflictException(dto.getComputeId());
        }
    }

    public List<ComputeRegistrationDTO> getAvailableComputeRegistrations(String workflowId) {
        final SdkIterable<Page<ComputeRegistrationDTO>> queryResults = table.index(WORKFLOW_INDEX).query(
                QueryEnhancedRequest.builder()
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                                .partitionValue(workflowId)
                                .build()))
                        .build());
        if (!queryResults.iterator().hasNext()) {
            return Collections.emptyList();
        } else {
            return queryResults.stream()
                    .flatMap(page -> page.items().stream())
                    .filter(dto -> dto.getComputeRegistrationStatus() == ComputeRegistrationStatus.FREE)
                    .collect(Collectors.toList());
        }
    }

    public void setComputeRegistrationStatus(String computeRegistrationId, ComputeRegistrationStatus status) {
        ComputeRegistrationDTO dto = getComputeRegistration(computeRegistrationId);
        dto.setComputeRegistrationStatus(status);
        table.putItem(dto);
    }

    public void deleteComputeRegistration(String computeRegistrationId) {
        table.deleteItem(Key.builder()
                .partitionValue(computeRegistrationId)
                .build()
        );
    }

    public ComputeRegistrationDTO getComputeRegistration(String computeId) {
        return table.getItem(Key.builder()
                .partitionValue(computeId)
                .build()
        );
    }
}

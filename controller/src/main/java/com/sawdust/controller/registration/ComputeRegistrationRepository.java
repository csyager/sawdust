package com.sawdust.controller.registration;

import com.sawdust.controller.registration.exceptions.ComputeRegistrationConflictException;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ComputeRegistrationRepository {

    @NonNull
    @Autowired
    private DynamoDbTable<ComputeRegistrationDTO> table;

    public ComputeRegistrationDTO createComputeRegistration(String workflowName, String certificate) {
        ComputeRegistrationDTO dto = new ComputeRegistrationDTO(UUID.randomUUID().toString(), workflowName, certificate);
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

    public ComputeRegistrationDTO getComputeRegistration(String computeId) {
        return table.getItem(Key.builder()
                .partitionValue(computeId)
                .build()
        );
    }
}

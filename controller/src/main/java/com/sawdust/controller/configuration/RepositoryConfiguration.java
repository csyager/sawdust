package com.sawdust.controller.configuration;

import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import com.sawdust.controller.workflows.model.dto.WorkflowDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RepositoryConfiguration {
    @NonNull
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Bean
    public DynamoDbTable<WorkflowDTO> workflowsTable(
            @Value("${dynamodb.workflows.table.name}")
            @NonNull final String tableName
    ) {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(WorkflowDTO.class));
    }

    @Bean
    public DynamoDbTable<ComputeRegistrationDTO> computeRegistrationTable(
            @Value("${dynamodb.compute_registrations.table.name}")
            @NonNull final String tableName
    ) {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(ComputeRegistrationDTO.class));
    }

    @Bean
    public DynamoDbTable<ActivityDTO> activityTable(
            @Value("${dynamodb.activites.table.name}")
            @NonNull final String tableName
    ) {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(ActivityDTO.class));
    }
}

package com.sawdust.controller.workflows.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateWorkflowRequest {
    @NotNull
    String workflowId;
}

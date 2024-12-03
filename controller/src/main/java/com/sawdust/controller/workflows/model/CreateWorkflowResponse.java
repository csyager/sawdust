package com.sawdust.controller.workflows.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CreateWorkflowResponse {
    @NonNull
    String workflowId;

    @NonNull
    String workflowSecret;
}

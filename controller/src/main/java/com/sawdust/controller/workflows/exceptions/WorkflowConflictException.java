package com.sawdust.controller.workflows.exceptions;

import com.sawdust.controller.exceptions.ConflictException;

public class WorkflowConflictException extends ConflictException {
    public WorkflowConflictException(String workflowId) {
        super(String.format("Workflow with workflowId %s already exists.", workflowId));
    }
}

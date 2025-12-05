package com.sawdust.controller.activities.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateActivityRequest {
    @NotNull
    private String workflowId;

    private String activityState;
}

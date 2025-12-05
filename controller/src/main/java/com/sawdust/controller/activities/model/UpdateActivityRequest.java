package com.sawdust.controller.activities.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateActivityRequest {
    @NotNull
    private String computeId;

    private String activityState;
    private Boolean activityComplete;

    public boolean getActivityComplete() {
        return activityComplete != null && activityComplete;
    }
}

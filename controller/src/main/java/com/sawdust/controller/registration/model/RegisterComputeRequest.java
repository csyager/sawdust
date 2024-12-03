package com.sawdust.controller.registration.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RegisterComputeRequest {
    @NotNull
    String workflowId;

    @NotNull
    String workflowToken;

    @NotNull
    String certificate;
}

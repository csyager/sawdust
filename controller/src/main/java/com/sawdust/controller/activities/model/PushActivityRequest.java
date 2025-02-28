package com.sawdust.controller.activities.model;

import com.sawdust.controller.activities.model.dto.ActivityDTO;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PushActivityRequest {
    @NonNull
    private String computeId;
    @NonNull
    private ActivityDTO activity;
}

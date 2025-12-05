package com.sawdust.controller.activities.model;

import com.sawdust.controller.activities.model.dto.ActivityDTO;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CreateActivityResponse {
    @NonNull
    private ActivityDTO activity;
}

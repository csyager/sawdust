package com.sawdust.controller.activities;

import com.sawdust.controller.activities.model.CreateActivityResponse;
import com.sawdust.controller.activities.model.UpdateActivityResponse;
import com.sawdust.controller.activities.model.WorkflowState;
import com.sawdust.controller.activities.model.dto.ActivityDTO;
import com.sawdust.controller.registration.ComputeRegistrationRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ComputeRegistrationRepository computeRegistrationRepository;

    public CreateActivityResponse createActivity(
            @NonNull String workflowId,
            @NonNull String activityState

    ) {
        final ActivityDTO activity = ActivityDTO.builder()
                .activityId(String.valueOf(UUID.randomUUID()))
                .workflowId(workflowId)
                .workflowState(WorkflowState.PENDING)
                .incompleteState(WorkflowState.PENDING)
                .activityState(activityState)
                .build();

        activityRepository.putActivity(activity);
        return new CreateActivityResponse(activity);
    }

    public UpdateActivityResponse updateActivity(
            @NonNull String activityId,
            String activityState,
            boolean activityComplete
    ) {
        ActivityDTO activity = activityRepository.getActivity(activityId);
        activity.setActivityState(activityState);
        if (activityComplete) {
            activity.setIncompleteState(null);
            activity.setWorkflowState(WorkflowState.SUCCEEDED);
        }
        activityRepository.putActivity(activity);
        return new UpdateActivityResponse(activity);
    }

    public String getComputeRegistrationCertificate(@NonNull final String computeId) {
        return computeRegistrationRepository.getComputeRegistration(computeId).getCertificate();
    }

}

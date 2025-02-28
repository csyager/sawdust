package com.sawdust.controller.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sawdust.controller.activities.ActivityPusher;
import com.sawdust.controller.activities.ActivityRepository;
import com.sawdust.controller.registration.ComputeRegistrationRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;

public class ThreadingConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ActivityPusher activityPusher(
            @NonNull final ComputeRegistrationRepository computeRegistrationRepository,
            @NonNull final ActivityRepository activityRepository,
            @NonNull final ObjectMapper objectMapper
    ) {
        return new ActivityPusher(computeRegistrationRepository, activityRepository, objectMapper);
    }
}

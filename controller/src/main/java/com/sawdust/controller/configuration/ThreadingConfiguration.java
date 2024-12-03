package com.sawdust.controller.configuration;

import com.sawdust.controller.activities.ActivityPusher;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class ThreadingConfiguration {
    @Bean
    public ActivityPusher activityPusher(
            @NonNull HttpClient httpClient
    ) {
        return new ActivityPusher(httpClient);
    }
}

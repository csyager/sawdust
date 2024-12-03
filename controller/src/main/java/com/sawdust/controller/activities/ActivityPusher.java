package com.sawdust.controller.activities;

import com.sawdust.controller.registration.ComputeRegistrationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class ActivityPusher {
    @NonNull
    private final HttpClient httpClient;
    @NonNull
    private final ComputeRegistrationRepository computeRegistrationRepository;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void run() throws IOException, InterruptedException {
        log.info("Running activity pusher");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:9000"))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to communicate with remote agent: " + e);
        }
    }
}

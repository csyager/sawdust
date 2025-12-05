package com.sawdust.controller.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sawdust.controller.activities.model.PushActivityRequest;
import com.sawdust.controller.activities.model.WorkflowState;
import com.sawdust.controller.activities.model.dto.ActivityDTO;
import com.sawdust.controller.clients.SSLConnectionManager;
import com.sawdust.controller.registration.ComputeRegistrationRepository;
import com.sawdust.controller.registration.model.ComputeRegistrationStatus;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class ActivityPusher {
    @NonNull
    private final ComputeRegistrationRepository computeRegistrationRepository;
    @NonNull
    private final ActivityRepository activityRepository;
    @NonNull
    private final ObjectWriter objectWriter;
    @NonNull
    private final SSLConnectionManager sslConnectionManager;

    @Autowired
    public ActivityPusher(
            final ComputeRegistrationRepository computeRegistrationRepository,
            final ActivityRepository activityRepository,
            final ObjectMapper objectMapper,
            final SSLConnectionManager sslConnectionManager) {
        this.computeRegistrationRepository = computeRegistrationRepository;
        this.activityRepository = activityRepository;
        this.objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        this.sslConnectionManager = sslConnectionManager;
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void run() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        log.info("Running activity pusher");

        // getting set of pending activities
//        final List<ActivityDTO> pendingActivities = activityRepository.getPendingActivities();
        final List<ActivityDTO> pendingActivities = List.of(
                ActivityDTO.builder()
                        .workflowId("test")
                        .incompleteState(WorkflowState.PENDING)
                        .activityId("test")
                        .workflowState(WorkflowState.PENDING)
                        .activityState("{\"state\": \"STARTING\"}")
                        .build()
        );
        log.info("Got {} pending activities from the db.", pendingActivities.size());

        for (ActivityDTO pendingActivity: pendingActivities) {
            // get available compute registrations for the relevant workflow
            final List<ComputeRegistrationDTO> computeRegistrations = computeRegistrationRepository.getAvailableComputeRegistrations(pendingActivity.getWorkflowId());
            log.info("Got {} elligible compute nodes from the db.", computeRegistrations.size());
            for (ComputeRegistrationDTO computeRegistration: computeRegistrations) {
                try {
                    HttpResponse<String> response = sendActivityPushRequest(computeRegistration, pendingActivity);
                    log.info("agent response headers: {}", response.headers());
                    log.info("agent response body: {}", response.body());
                    log.info("Setting compute registration {} to IN_USE.", computeRegistration.getComputeId());
//                    computeRegistrationRepository.setComputeRegistrationStatus(computeRegistration.getComputeId(), ComputeRegistrationStatus.IN_USE);
                } catch (Exception e) {
                    // assume that an exception means that the node has disconnected and is now unreachable
                    log.info("Node {} unreachable due to error {}.  Deleting existing compute registration.", computeRegistration.getComputeId(), e.getMessage());
                    log.debug(e.toString());
                    computeRegistrationRepository.deleteComputeRegistration(computeRegistration.getComputeId());
                }
            }
        }
    }

    public HttpResponse<String> sendActivityPushRequest(ComputeRegistrationDTO computeRegistrationDTO, ActivityDTO activityDTO)
            throws IOException, InterruptedException, CertificateException, KeyStoreException, KeyManagementException {
        log.info("Pushing activity to compute registration {}.", computeRegistrationDTO.getComputeId());
        sslConnectionManager.addCertificate(computeRegistrationDTO.getComputeId(), computeRegistrationDTO.getCertificate());
        PushActivityRequest pushActivityRequest = new PushActivityRequest(
                computeRegistrationDTO.getComputeId(),
                activityDTO
        );
        String jsonBody = this.objectWriter.writeValueAsString(pushActivityRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:9000/activity-push"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sslConnectionManager.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}

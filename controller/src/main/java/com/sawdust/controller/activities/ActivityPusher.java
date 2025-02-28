package com.sawdust.controller.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sawdust.controller.activities.model.PushActivityRequest;
import com.sawdust.controller.activities.model.WorkflowState;
import com.sawdust.controller.activities.model.dto.ActivityDTO;
import com.sawdust.controller.registration.ComputeRegistrationRepository;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

    @Autowired
    public ActivityPusher(
            final ComputeRegistrationRepository computeRegistrationRepository,
            final ActivityRepository activityRepository,
            final ObjectMapper objectMapper) {
        this.computeRegistrationRepository = computeRegistrationRepository;
        this.activityRepository = activityRepository;
        this.objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void run() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        log.info("Running activity pusher");

        // getting set of pending activities
        final List<ActivityDTO> pendingActivities = activityRepository.getPendingActivities();
        log.info("Got {} pending activities from the db.", pendingActivities.size());

        // get available compute registrations for the relevant workflow
        // TODO:  this should get a list of eligible compute registrations, instead of just one.
        final ComputeRegistrationDTO computeRegistrationDTO = computeRegistrationRepository.getComputeRegistration("6f40f455-e36b-43af-bdba-2146e167a7ed");

        // setup SSL context
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(computeRegistrationDTO.getCertificate().getBytes()));

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry(computeRegistrationDTO.getComputeId(), cert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, tmf.getTrustManagers(), null);

        HttpClient httpClient = HttpClient.newBuilder().sslContext(sc).build();

        final PushActivityRequest pushActivityRequest = new PushActivityRequest(
                computeRegistrationDTO.getComputeId(),
                new ActivityDTO("testActivity", computeRegistrationDTO.getWorkflowName(), WorkflowState.IN_PROGRESS)
        );
        final String jsonBody = objectWriter.writeValueAsString(pushActivityRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:9000"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("agent response headers: {}", response.headers());
            log.info("agent response body: {}", response.body());
        } catch (Exception e) {
            log.error("Failed to communicate with remote agent: " + e);
        }
    }
}

package com.sawdust.controller.activities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sawdust.controller.activities.model.CreateActivityRequest;
import com.sawdust.controller.activities.model.CreateActivityResponse;
import com.sawdust.controller.activities.model.UpdateActivityRequest;
import com.sawdust.controller.activities.model.UpdateActivityResponse;
import com.sawdust.controller.clients.SignatureVerifier;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(
        value = "/activity",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Slf4j
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping({"", "/"})
    @ResponseBody
    public ResponseEntity<CreateActivityResponse> createActivity(
            @NonNull @Valid @RequestBody final CreateActivityRequest request
    ) {
        log.info("Received create activity request.");

        return new ResponseEntity<> (
            activityService.createActivity(request.getWorkflowId(), request.getActivityState()),
            HttpStatus.CREATED
        );
    }

    @PutMapping("/{activityId}")
    @ResponseBody
    public ResponseEntity<UpdateActivityResponse> updateActiivty(
            @PathVariable @NonNull final String activityId,
            @NonNull @Valid @RequestBody final UpdateActivityRequest request,
            @RequestHeader("X-Signature") final String signature
    ) throws JsonProcessingException {
        log.info("Received update activity request for activity {}", activityId);

        // first, verify request signature comes from a registered
        final String jsonBody = mapper.writeValueAsString(request);
        final String publicKey = activityService.getComputeRegistrationCertificate(request.getComputeId());

        if (SignatureVerifier.verifySignature(jsonBody, signature, publicKey)) {
            return new ResponseEntity<>(
                    activityService.updateActivity(activityId, request.getActivityState(), request.getActivityComplete()),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    HttpStatus.FORBIDDEN
            );
        }
    }
}

package com.sawdust.controller.registration;

import com.sawdust.controller.registration.model.RegisterComputeRequest;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping(
        value = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping(value = "/compute")
    @ResponseBody
    public ResponseEntity<ComputeRegistrationDTO> registerCompute(
            @NonNull @Valid @RequestBody final RegisterComputeRequest request
    ) throws NoSuchAlgorithmException {
        log.info("Received registration request for application {}.", request.getWorkflowId());

        return new ResponseEntity<>(
                registrationService.registerCompute(request.getWorkflowId(), request.getWorkflowToken(), request.getCertificate()),
                HttpStatus.CREATED
        );
    }
}

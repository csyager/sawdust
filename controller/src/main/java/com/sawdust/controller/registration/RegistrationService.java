package com.sawdust.controller.registration;

import com.sawdust.controller.registration.exceptions.InvalidWorkflowTokenException;
import com.sawdust.controller.registration.model.dto.ComputeRegistrationDTO;
import com.sawdust.controller.workflows.WorkflowsRepository;
import com.sawdust.controller.workflows.model.dto.WorkflowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class RegistrationService {

    @Autowired
    private ComputeRegistrationRepository computeRegistrationRepository;

    @Autowired
    private WorkflowsRepository workflowsRepository;

    public ComputeRegistrationDTO registerCompute(
            final String workflowName,
            final String workflowToken,
            final String certificate
    ) throws NoSuchAlgorithmException {
        WorkflowDTO workflow = workflowsRepository.getWorkflow(workflowName);

        // validate token
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(workflowToken.getBytes());
        String stringHash = new String(messageDigest.digest());

        if (stringHash.equals(workflow.getSecret())) {
            // register compute in db
            return computeRegistrationRepository.createComputeRegistration(workflowName, certificate);
        } else {
            throw new InvalidWorkflowTokenException("Invalid workflow token.");
        }
    }
}

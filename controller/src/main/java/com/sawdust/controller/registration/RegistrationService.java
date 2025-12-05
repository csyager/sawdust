package com.sawdust.controller.registration;

import com.sawdust.controller.clients.SignatureVerifier;
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
            final String workflowId,
            final String workflowToken,
            final String certificate
    ) throws NoSuchAlgorithmException {
        WorkflowDTO workflow = workflowsRepository.getWorkflow(workflowId);

        // validate bootstrap token
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(workflowToken.getBytes());
        String stringHash = new String(messageDigest.digest());

        if (!stringHash.equals(workflow.getSecret())) {
            throw new InvalidWorkflowTokenException("Invalid workflow token.");
        }

        // TODO: verify worker certificate

        // register compute in db
        return computeRegistrationRepository.createComputeRegistration(workflowId, certificate);
    }
}

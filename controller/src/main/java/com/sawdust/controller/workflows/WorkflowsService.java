package com.sawdust.controller.workflows;

import com.sawdust.controller.workflows.model.CreateWorkflowResponse;
import com.sawdust.controller.workflows.model.dto.WorkflowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@Slf4j
public class WorkflowsService {
    private static final String SECRET_NAME_PATTERN = "WorkflowSecret-%s";
    private static final String SECRET_DESCRIPTION_PATTERN = "Secret key for workflow %s";

    @Autowired
    private WorkflowsRepository workflowsRepository;

    @Autowired
    private SecretsManagerClient secretsManagerClient;

    public CreateWorkflowResponse createWorkflow(String workflowId) throws NoSuchAlgorithmException {
        log.info("Received workflow creation request.");
        final SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        final String keyAllowedChars = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String secretString = secureRandom
                .ints(64, 0, keyAllowedChars.length())
                .mapToObj(keyAllowedChars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(secretString.getBytes());
        String stringHash = new String(messageDigest.digest());

        workflowsRepository.createWorkflow(WorkflowDTO.builder()
                .workflowId(workflowId)
                .secret(stringHash)
                .build()
        );

        return new CreateWorkflowResponse(workflowId, secretString);
    }
}

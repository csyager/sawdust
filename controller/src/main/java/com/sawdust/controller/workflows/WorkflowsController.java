package com.sawdust.controller.workflows;

import com.sawdust.controller.workflows.model.CreateWorkflowRequest;
import com.sawdust.controller.workflows.model.CreateWorkflowResponse;
import com.sawdust.controller.workflows.model.dto.WorkflowDTO;
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
        value = "/workflow",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Slf4j
public class WorkflowsController {

    @Autowired
    private WorkflowsService workflowService;

    @PostMapping({"", "/"})
    @ResponseBody
    public ResponseEntity<CreateWorkflowResponse> createWorkflow(
            @NonNull @Valid @RequestBody final CreateWorkflowRequest request
    ) throws NoSuchAlgorithmException {
        log.info("Received workflow creation request.");

        return new ResponseEntity<>(
                workflowService.createWorkflow(request.getWorkflowId()),
                HttpStatus.CREATED
        );
    }
}

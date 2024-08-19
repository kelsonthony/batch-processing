package com.kelsonthony.batchprocessing.controller;


import com.kelsonthony.batchprocessing.dto.JobResultDTO;
import com.kelsonthony.batchprocessing.listener.CustomJobExecutionListener;
import com.kelsonthony.batchprocessing.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class CustomerController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final CustomJobExecutionListener customJobExecutionListener;
    private final ApiService apiService;

    public CustomerController(JobLauncher jobLauncher, Job job, CustomJobExecutionListener customJobExecutionListener, ApiService apiService) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.customJobExecutionListener = customJobExecutionListener;
        this.apiService = apiService;
    }

    @PostMapping(path = "/importCustomers")
    @Operation(summary = "Start Batch Job", description = "Starts the batch job to import customers and return the result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Batch job started successfully."),
            @ApiResponse(responseCode = "500", description = "Failed to start batch job.")
    })
    public ResponseEntity<?> startBatch() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            // Wait for job to finish
            while (jobExecution.isRunning()) {
                Thread.sleep(1000); // Sleep for a while before checking job status again
                jobExecution = jobLauncher.run(job, jobParameters); // Re-run to check the updated state
            }

            // Retrieve results from the listener
            JobResultDTO jobResult = customJobExecutionListener.getJobResult(jobExecution.getId());

            // Call the external API and handle response
            try {
                String externalApiResponse = apiService.callExternalApi("request data");
                // Process the external API response if needed
                // ...
            } catch (Exception e) {
                e.printStackTrace();
                return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
            }


            // Process the external API response if needed
            // ...

            return ResponseEntity.status(HttpStatus.CREATED).body(jobResult);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | InterruptedException e) {
            e.printStackTrace();
            return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception e, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

}
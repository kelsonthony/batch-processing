package com.kelsonthony.batchprocessing.controller;


import com.kelsonthony.batchprocessing.dto.JobResultDTO;
import com.kelsonthony.batchprocessing.listener.CustomJobExecutionListener;
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

import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class CustomerController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final CustomJobExecutionListener customJobExecutionListener;

    public CustomerController(JobLauncher jobLauncher, Job job, CustomJobExecutionListener customJobExecutionListener) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.customJobExecutionListener = customJobExecutionListener;
    }

    @PostMapping(path = "/importCustomers")
    public ResponseEntity<JobResultDTO> startBatch() {
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
            return ResponseEntity.status(HttpStatus.CREATED).body(jobResult);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
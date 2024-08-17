package com.kelsonthony.batchprocessing.listener;

import com.kelsonthony.batchprocessing.dto.JobResultDTO;
import com.kelsonthony.batchprocessing.dto.StepExecutionDTO;
import lombok.Getter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Getter
@Component
public class CustomJobExecutionListener implements JobExecutionListener {

    private Map<Long, StepExecutionDTO> results = new HashMap<>();

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Initialization logic before job starts
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        results.clear(); // Clear previous results if needed
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            results.put(stepExecution.getId(), convertToDTO(stepExecution));
        }
        // Print results for debugging purposes
        System.out.println("Job completed. Results: " + results);
    }

    // This method allows access to results from outside the listener
    public JobResultDTO getJobResult(Long executionId) {
        return new JobResultDTO(executionId, results);
    }

    private StepExecutionDTO convertToDTO(StepExecution stepExecution) {
        return new StepExecutionDTO(
                stepExecution.getId(),
                stepExecution.getStepName(),
                stepExecution.getStatus().toString(),
                stepExecution.getExitStatus().getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getFilterCount(),
                stepExecution.getWriteCount(),
                stepExecution.getReadSkipCount(),
                stepExecution.getWriteSkipCount(),
                stepExecution.getProcessSkipCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount()
        );
    }
}
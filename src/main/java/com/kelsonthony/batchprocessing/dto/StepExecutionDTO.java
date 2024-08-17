package com.kelsonthony.batchprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepExecutionDTO {
    private Long id;
    private String name;
    private String status;
    private String exitStatus;
    private int readCount;
    private int filterCount;
    private int writeCount;
    private int readSkipCount;
    private int writeSkipCount;
    private int processSkipCount;
    private int commitCount;
    private int rollbackCount;
}
package com.kelsonthony.batchprocessing.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelsonthony.batchprocessing.entity.Customer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class StepSkipListener implements SkipListener<Customer, Number> {
    Logger logger = LoggerFactory.getLogger(StepSkipListener.class);

    @Override //item Reader
    public void onSkipInRead(Throwable throwable) {
        logger.info("A failure on read {} ", throwable.getMessage());
    }

    @Override // item Writer
    public void onSkipInWrite(Number item, Throwable throwable) {
        logger.info("A failure on write {} , {} ", throwable.getMessage(), item);
    }

    @SneakyThrows
    @Override //item Process
    public void onSkipInProcess(Customer customer, Throwable throwable) {
        logger.info("Process Item {} was skipped to the exception {}",
                new ObjectMapper().writeValueAsString(customer), throwable.getMessage());
    }
}

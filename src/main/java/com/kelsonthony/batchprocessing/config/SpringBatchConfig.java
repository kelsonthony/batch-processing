package com.kelsonthony.batchprocessing.config;

import com.kelsonthony.batchprocessing.entity.Customer;
import com.kelsonthony.batchprocessing.listener.StepSkipListener;
import com.kelsonthony.batchprocessing.partition.ColumnRangePartitioner;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.step.skip.NonSkippableReadException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {
    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private CustomerItemWriter customerWriter;

    @Bean
    public ItemReader<Customer> reader() {
        return new ItemReader<Customer>() {
            private Iterator<Row> rowIterator;

            {
                try {
                    FileInputStream file = new FileInputStream("src/main/resources/customers.xlsx");
                    Workbook workbook = new XSSFWorkbook(file);
                    Sheet sheet = workbook.getSheetAt(0);
                    rowIterator = sheet.iterator();
                    rowIterator.next(); // Skip header row
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read Excel file", e);
                }
            }

            @Override
            public Customer read() {
                if (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Customer customer = new Customer();
                    customer.setId(getNumericCellValue(row.getCell(0)));
                    customer.setFirstname(getStringCellValue(row.getCell(1)));
                    customer.setLastName(getStringCellValue(row.getCell(2)));
                    customer.setEmail(getStringCellValue(row.getCell(3)));
                    customer.setGender(getStringCellValue(row.getCell(4)));
                    customer.setContactNo(getStringCellValue(row.getCell(5)));
                    customer.setCountry(getStringCellValue(row.getCell(6)));
                    customer.setDob(String.valueOf(row.getCell(7).getDateCellValue()));
                    customer.setAge(String.valueOf(getNumericCellValue(row.getCell(8))));
                    return customer;
                } else {
                    return null;
                }
            }

            private int getNumericCellValue(Cell cell) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    return (int) cell.getNumericCellValue();
                } else if (cell.getCellType() == CellType.STRING) {
                    return Integer.parseInt(cell.getStringCellValue());
                } else {
                    throw new IllegalArgumentException("Cannot get numeric value from cell type: " + cell.getCellType());
                }
            }

            private String getStringCellValue(Cell cell) {
                if (cell.getCellType() == CellType.STRING) {
                    return cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    return String.valueOf(cell.getNumericCellValue());
                } else {
                    throw new IllegalArgumentException("Cannot get string value from cell type: " + cell.getCellType());
                }
            }
        };
    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(2);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep());

        return taskExecutorPartitionHandler;
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep").<Customer, Customer>chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(customerWriter)
                .faultTolerant()
                .listener(skipListener())
                .skipPolicy(skipPolicy())
                .build();
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers")
                .flow(masterStep()).end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(4);
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setQueueCapacity(4);

        return threadPoolTaskExecutor;
    }

    @Bean
    public SkipPolicy skipPolicy() {
        return new SkipPolicy() {
            @Override
            public boolean shouldSkip(Throwable t, int skipCount) {
                // Defina as exceções que podem ser ignoradas
                if (t instanceof NonSkippableReadException) {
                    return false; // Não ignorar esta exceção
                }
                return true; // Ignorar outras exceções
            }
        };
    }

    @Bean
    public SkipListener skipListener() {
        return new StepSkipListener();
    }
}

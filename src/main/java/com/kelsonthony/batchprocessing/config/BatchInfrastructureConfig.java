package com.kelsonthony.batchprocessing.config;

import com.kelsonthony.batchprocessing.listener.StepSkipListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.step.skip.NonSkippableReadException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchInfrastructureConfig {

    @Bean(name = "taskExecutor")
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
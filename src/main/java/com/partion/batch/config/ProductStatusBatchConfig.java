package com.partion.batch.config;

import com.partion.batch.tasklet.CloseExpiredFundingProductsTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ProductStatusBatchConfig {

    private final CloseExpiredFundingProductsTasklet closeExpiredFundingProductsTasklet;

    @Bean
    public Job closeExpiredFundingProductsJob(
            JobRepository jobRepository,
            Step closeExpiredFundingProductsStep
    ) {
        return new JobBuilder("closeExpiredFundingProductsJob", jobRepository)
                .start(closeExpiredFundingProductsStep)
                .build();
    }

    @Bean
    public Step closeExpiredFundingProductsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("closeExpiredFundingProductsStep", jobRepository)
                .tasklet(closeExpiredFundingProductsTasklet, transactionManager)
                .build();
    }
}
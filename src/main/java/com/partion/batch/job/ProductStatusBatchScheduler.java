package com.partion.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductStatusBatchScheduler {

    private final JobOperator jobOperator;
    private final Job closeExpiredFundingProductsJob;

    // @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runCloseExpiredFundingProductsJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(closeExpiredFundingProductsJob, jobParameters);
    }
}
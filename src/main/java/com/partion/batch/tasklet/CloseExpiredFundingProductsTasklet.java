package com.partion.batch.tasklet;

import com.partion.batch.service.FundingRefundBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CloseExpiredFundingProductsTasklet implements Tasklet {

    private final FundingRefundBatchService fundingRefundBatchService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int updatedCount = fundingRefundBatchService.closeExpiredFundingProductsAndRefund();

        contribution.incrementWriteCount(updatedCount);

        return RepeatStatus.FINISHED;
    }
}
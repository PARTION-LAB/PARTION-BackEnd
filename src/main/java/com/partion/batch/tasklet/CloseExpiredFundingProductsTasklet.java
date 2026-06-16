package com.partion.batch.tasklet;

import com.partion.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class CloseExpiredFundingProductsTasklet implements Tasklet {

    private final ProductMapper productMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int updatedCount = productMapper.closeExpiredFundingProducts(LocalDate.now());

        contribution.incrementWriteCount(updatedCount);

        return RepeatStatus.FINISHED;
    }
}
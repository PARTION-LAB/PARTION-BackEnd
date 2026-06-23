package com.partion.matching.producer;

import com.partion.matching.config.KafkaTopicConfig;
import com.partion.matching.event.OrderCommandEvent;
import com.partion.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class OrderCommandPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreateAfterCommit(Order order) {
        OrderCommandEvent event = OrderCommandEvent.create(order);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(event);
                }
            });
            return;
        }

        publish(event);
    }

    public void publishCancelAfterCommit(Order order) {
        OrderCommandEvent event = OrderCommandEvent.cancel(order);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(event);
                }
            });
            return;
        }

        publish(event);
    }

    public void publishResync(Order order) {
        OrderCommandEvent event = OrderCommandEvent.resync(order);
        publish(event);
    }

    private void publish(OrderCommandEvent event) {
        kafkaTemplate.send(
                KafkaTopicConfig.ORDER_COMMANDS,
                String.valueOf(event.productId()),
                event
        );
    }
}
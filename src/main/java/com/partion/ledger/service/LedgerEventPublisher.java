package com.partion.ledger.service;

import com.partion.ledger.event.LedgerEvent;
import com.partion.matching.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerEventPublisher {

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    @Value("${partion.matching.kafka.enabled:true}")
    private boolean kafkaEnabled;

    public void publishAfterCommit(LedgerEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }

    private void publish(LedgerEvent event) {
        if (!kafkaEnabled) {
            log.debug("Kafka ledger is disabled. Skip ledger event. eventId={}", event.eventId());
            return;
        }

        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate is unavailable. Skip ledger event. eventId={}", event.eventId());
            return;
        }

        kafkaTemplate.send(
                KafkaTopicConfig.LEDGER_EVENTS,
                event.referenceId().toString(),
                event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to publish ledger event. eventId={}", event.eventId(), exception);
            }
        });
    }
}
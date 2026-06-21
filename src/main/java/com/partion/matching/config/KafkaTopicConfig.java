package com.partion.matching.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "partion.matching.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    public static final String ORDER_COMMANDS = "partion.order.commands";
    public static final String TRADE_EVENTS = "partion.trade.events";
    public static final String LEDGER_EVENTS = "partion.ledger.events";

    @Bean
    public NewTopic orderCommandsTopic() {
        return TopicBuilder.name(ORDER_COMMANDS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tradeEventsTopic() {
        return TopicBuilder.name(TRADE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ledgerEventsTopic() {
        return TopicBuilder.name(LEDGER_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
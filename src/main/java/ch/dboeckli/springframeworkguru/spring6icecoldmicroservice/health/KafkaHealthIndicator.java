package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;
    private final String kafkaBootstrapServers;

    private boolean wasDownLastCheck = true;

    public KafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate,
                                KafkaAdmin kafkaAdmin,
                                @Value("${spring.kafka.bootstrap-servers}") String kafkaBootstrapServers) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    @Override
    public Health health() {
        try {
            // Check producer connection
            Future<SendResult<String, String>> future = kafkaTemplate.send("health-check", "health-check-message");
            SendResult<String, String> result = future.get(5, TimeUnit.SECONDS);

            String responseInfo = String.format("Topic: %s, Partition: %d, Offset: %d",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());

            // Check consumer groups
            Collection<ConsumerGroupListing> groups;
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                ListConsumerGroupsResult consumerGroups = adminClient.listConsumerGroups();
                 groups = consumerGroups.all().get(5, TimeUnit.SECONDS);
            }

            if (wasDownLastCheck) {
                log.info("### Kafka Server connection successfully established to {}. Response: {}. Consumer groups: {}",
                    kafkaBootstrapServers, responseInfo, groups.size());
                wasDownLastCheck = false;
            }

            return Health.up()
                .withDetail("kafkaResponse", responseInfo)
                .withDetail("consumerGroups", groups.size())
                .build();
        } catch (Exception ex) {
            wasDownLastCheck = true;
            log.warn("Kafka Server connection down to {}", kafkaBootstrapServers, ex);
            return Health.down(ex)
                .withDetail("kafkaBootstrapServers", kafkaBootstrapServers)
                .build();
        }
    }
}

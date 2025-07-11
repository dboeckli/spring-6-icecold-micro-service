package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.listener.DrinkRequestListener.GROUP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Slf4j
@ActiveProfiles("it")
class KafkaConfigIT {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Test
    void shouldHaveRequiredTopics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
                ListTopicsResult topicsResult = adminClient.listTopics();
                Collection<TopicListing> topics = topicsResult.listings().get(5, TimeUnit.SECONDS);

                assertThat(topics)
                    .extracting(TopicListing::name)
                    .contains(KafkaConfig.DRINK_REQUEST_ICE_COLD_TOPIC);
            });

            await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
                ListConsumerGroupsResult consumerGroupsResult = adminClient.listConsumerGroups();
                Collection<ConsumerGroupListing> groups = consumerGroupsResult.all().get(5, TimeUnit.SECONDS);

                assertThat(groups)
                    .extracting(ConsumerGroupListing::groupId)
                    .contains(GROUP_ID);
            });
        }
    }

}
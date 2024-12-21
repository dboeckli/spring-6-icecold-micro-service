package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.listener;

import ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.config.KafkaConfig;
import ch.guru.springframework.spring6restmvcapi.dto.BeerDTO;
import ch.guru.springframework.spring6restmvcapi.dto.BeerOrderLineDTO;
import ch.guru.springframework.spring6restmvcapi.dto.BeerStyle;
import ch.guru.springframework.spring6restmvcapi.events.DrinkRequestEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(controlledShutdown = true, topics = {KafkaConfig.DRINK_REQUEST_ICE_COLD_TOPIC, KafkaConfig.DRINK_PREPARED_TOPIC}, partitions = 1)
public class DrinkRequestListenerTest {

    @Autowired
    DrinkRequestListener drinkRequestListener;

    @Autowired
    DrinkPreparedListener drinkPreparedListener;

    @Test
    void listenDrinkRequest() {
        drinkRequestListener.listenDrinkRequest(DrinkRequestEvent.builder()
            .beerOrderLineDTO(createDto())
            .build());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(1, drinkPreparedListener.iceColdMessageCounter.get());
        });
    }


    public BeerOrderLineDTO createDto(){
        return BeerOrderLineDTO.builder()
            .beer(BeerDTO.builder()
                .id(UUID.randomUUID())
                .beerStyle(BeerStyle.IPA)
                .beerName("Test Beer")
                .build())
            .build();
    }
    
}

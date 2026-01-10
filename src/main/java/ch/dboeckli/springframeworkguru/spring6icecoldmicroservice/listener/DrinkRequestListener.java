package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.listener;

import ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.config.KafkaConstants;
import ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.services.DrinkRequestProcessor;
import ch.guru.springframework.spring6restmvcapi.events.DrinkPreparedEvent;
import ch.guru.springframework.spring6restmvcapi.events.DrinkRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DrinkRequestListener {

    public static final String GROUP_ID = "IceColdListener";
    private final DrinkRequestProcessor drinkRequestProcessor;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(groupId = GROUP_ID, topics = KafkaConstants.DRINK_REQUEST_ICE_COLD_TOPIC)
    public void listenDrinkRequest(DrinkRequestEvent event) {
        log.info("### I am listening - Ice Cold drink request" + event);

        drinkRequestProcessor.processDrinkRequest(event);

        kafkaTemplate.send(KafkaConstants.DRINK_PREPARED_TOPIC, DrinkPreparedEvent.builder()
            .beerOrderLineDTO(event.getBeerOrderLineDTO())
            .build());

    }

}

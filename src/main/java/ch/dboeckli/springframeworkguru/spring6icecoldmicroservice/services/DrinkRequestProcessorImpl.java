package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice.services;

import ch.guru.springframework.spring6restmvcapi.events.DrinkRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DrinkRequestProcessorImpl implements DrinkRequestProcessor {
    
    @Override
    public void processDrinkRequest(DrinkRequestEvent event) {
        log.info("### Processing Ice Cold drink request..." + event);
        try {
            Thread.sleep(50);
            log.info("### Processing Ice Cold drink done...");
        } catch (InterruptedException e) {
            log.error("Error processing drink request", e);
        }
    }
}

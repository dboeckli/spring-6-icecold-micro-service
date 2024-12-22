package ch.dboeckli.springframeworkguru.spring6icecoldmicroservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Spring6IcecoldMicroServiceApplication {

    public static void main(String[] args) {
        log.info("Starting Spring 6 Icecold Microservice...");
        SpringApplication.run(Spring6IcecoldMicroServiceApplication.class, args);
    }

}

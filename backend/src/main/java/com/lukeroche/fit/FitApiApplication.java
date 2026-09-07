package com.lukeroche.fit;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Log
public class FitApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitApiApplication.class, args);
    }

}

package com.example.fbgrpc.server;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Configuration {
    @Bean
    ExampleServer provideSS() {
        return new ExampleServer();
    }
}

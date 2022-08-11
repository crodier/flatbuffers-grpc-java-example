package com.example.fbgrpc.stupidserver;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Configuration {
    @Bean
    StupidServer provideSS() {
        return new StupidServer();
    }
}

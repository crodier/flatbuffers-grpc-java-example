package com.example.fbgrpc;

import io.grpc.BindableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class FbGrpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(FbGrpcApplication.class, args);
    }

    @Bean
    GrpcServerLifecycle provideGrpcSL(List<BindableService> services, @Value("${grpc.port}") int port) {
        return new GrpcServerLifecycle(services, port);
    }
}

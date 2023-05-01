package com.example.fbgrpc;

import io.grpc.BindableService;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;

@SpringBootApplication
public class FbGrpcApplication {

    public static void main(String[] args) {
        try {
            // TODO:  Add libnetty_transport_native_epoll_x86_64.so to PATH
            File nettyEpoll = new File("./libnetty_transport_native_epoll_x86_64.so");
            System.out.println("Epoll library location: "+nettyEpoll);
            System.load(nettyEpoll.getAbsolutePath());
//
            // System.load("/home/crodier/coding/flatbuffers-grpc-java-example/grpc-server/libnetty_transport_native_epoll_x86_64.so");
//            File workingDir2 = new File("./libnetty_transport_native_epoll.so");
//            System.load(workingDir2.getAbsolutePath());

            boolean isEpollAvailNow = Epoll.isAvailable();
            if (isEpollAvailNow == false) {
                Epoll.unavailabilityCause().printStackTrace();
                throw new Exception(Epoll.unavailabilityCause());
            }

            SpringApplication.run(FbGrpcApplication.class, args);

        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Bean
    GrpcServerLifecycle provideGrpcSL(List<BindableService> services, @Value("${grpc.port}") int port) {
        return new GrpcServerLifecycle(services, port);
    }
}

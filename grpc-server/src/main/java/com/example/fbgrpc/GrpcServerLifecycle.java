package com.example.fbgrpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class GrpcServerLifecycle implements SmartLifecycle {
    public static Logger logger = LoggerFactory.getLogger(GrpcServerLifecycle.class);
    private final List<BindableService> services;
    private final int port;
    private Server server;

    public GrpcServerLifecycle(List<BindableService> services, int port) {
        this.services = requireNonNull(services);
        this.port = port;
    }

    @Override
    public synchronized void start() {
        logger.info("Starting grpcServer");
        if (!isRunning()) {
            createServer();

            try {
                server.start();
                // Prevent the JVM from shutting down while the server is running
                final Thread awaitThread = new Thread(() -> {
                    try {
                        logger.info("grpcServer is listening port {}", server.getPort());
                        server.awaitTermination();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                awaitThread.setName("grpc-server-awaiter");
                awaitThread.setDaemon(false);
                awaitThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createServer() {
        NettyServerBuilder nettyServerBuilder = NettyServerBuilder.forPort(port);

        // Add http server specific configuration here

        services.forEach(nettyServerBuilder::addService);

        server = nettyServerBuilder.build();
    }

    @Override
    public synchronized void stop() {
        logger.info("Stopping grpcServer");
        if (server != null) {
            server.shutdownNow();
            this.server = null;
        }
    }

    @Override
    public synchronized boolean isRunning() {
        if (server != null) {
            return !server.isShutdown() && !server.isTerminated();
        }
        return false;
    }

}

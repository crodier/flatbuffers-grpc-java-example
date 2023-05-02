package com.example.fbgrpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityStrategy;
import net.openhft.affinity.AffinityThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadFactory;

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

        // TODO:  Optimize such that these are to cores on one socket, and re-test
        // one acceptor thread, is enough indeed
        final int acceptorThreads = 1;
        // for my test, one worker thread
        final int workerThreads = 1;
        // this will put workers on different cores, with affinity
        // ThreadFactory threadFactory = new AffinityThreadFactory("atf_wrk", AffinityStrategies.DIFFERENT_SOCKET);

        // pin the acceptor to core 3, which is 'isolcpu' isolated
        PinnedCoreAffinityThreadFactory pinnedThree = new PinnedCoreAffinityThreadFactory("acceptor", 4);
        EventLoopGroup acceptorGroup = new EpollEventLoopGroup(acceptorThreads, pinnedThree);

        // pin the worker to core 4, which is 'isolcpu' isolated
        PinnedCoreAffinityThreadFactory pinnedFour = new PinnedCoreAffinityThreadFactory("worker", 3);
        EventLoopGroup workerGroup = new EpollEventLoopGroup(workerThreads, pinnedFour);

        try {
            Class<? extends ServerChannel> serverSocketChannel =
                    Class.forName("io.netty.channel.epoll.EpollServerSocketChannel")
                            .asSubclass(ServerChannel.class);

            NettyServerBuilder nettyServerBuilder =
                    NettyServerBuilder.forPort(port)
                            .channelFactory(new ReflectiveChannelFactory<>(serverSocketChannel))
                            // direct executor with 1 thread should be quickest
                            .directExecutor()
                            .workerEventLoopGroup(workerGroup)
                            .bossEventLoopGroup(acceptorGroup)
                            // .withOption(ChannelOption.MAX_MESSAGES_PER_WRITE, 64)
                            // TODO:  Further tunings, carefully, on a big machine
                            .withOption(ChannelOption.SO_KEEPALIVE, true)
                            .withOption(ChannelOption.TCP_NODELAY, true);

            // Direct Executor makes sense, single threaded
            services.forEach(nettyServerBuilder::addService);

            server = nettyServerBuilder.build();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load EpollServerSocketChannel", e);
        }


        // for higher throughput, at a cost of latency
        // nettyServerBuilder.executor(Executors.newCachedThreadPool());

        // Add http server specific configuration here

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

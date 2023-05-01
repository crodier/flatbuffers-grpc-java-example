package com.example.fbgrpc.server.client;

import com.example.fbgrpc.flatbuffers.ExampleServerGrpc;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.fbgrpc.flatbuffers.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ExampleClient {

    private ExampleServerGrpc.ExampleServerBlockingStub blockingStub;
    private ExampleServerGrpc.ExampleServerFutureStub nonBlockingStub;
    private ExampleServerGrpc.ExampleServerStub asyncStub;

    private final String host;
    private final int port;

    public ExampleClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() //SSL actually as default
                //More http client configuration should be here
                .build();

        blockingStub = ExampleServerGrpc.newBlockingStub(channel);
        nonBlockingStub = ExampleServerGrpc.newFutureStub(channel);
        asyncStub = ExampleServerGrpc.newStub(channel);
    }

    ConcurrentHashMap<Long, Long> correlatedStartTime = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, Long> correlatedFinishTime = new ConcurrentHashMap<>();

    public void recordRoute() throws InterruptedException {
        System.out.println("Start="+new Date());
        long start = System.nanoTime();
        // info("*** RecordRoute");
        final AtomicLong totalTook = new AtomicLong();
        final AtomicLong totalReceipt = new AtomicLong();

        int LATCH_SIZE = 10_000;
        final CountDownLatch finishLatch = new CountDownLatch(LATCH_SIZE);
        StreamObserver<Response> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Response response) {
                correlatedFinishTime.put(response.id(), System.nanoTime());
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                System.out.println("error on client+"+t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
//                System.out.println("on completed here");
//                System.out.println("here Total took="+totalTook.get());
//                System.out.println("Total="+totalReceipt.get());
                finishLatch.countDown();

            }
        };

        StreamObserver<Request> requestObserver = asyncStub.doWork(responseObserver);
        try
        {

            for (long i=0; i<LATCH_SIZE; i++) {
                FlatBufferBuilder builder = new FlatBufferBuilder();

                int requestOffset = Request.createRequest(builder,
                        // builder.createString(request),
                        123L, i);
                builder.finish(requestOffset);

                Request req = Request.getRootAsRequest(builder.dataBuffer());

                long time = System.nanoTime();

                correlatedStartTime.put(i, time);

                requestObserver.onNext(req);

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    System.out.println("Done requests");
                    break;
                }
            }

        } catch (RuntimeException e) {
            // Cancel RPC
            System.out.println("Cancel on runtime="+e.getMessage());
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);

        long end = System.nanoTime();
        long totalNanos = (end-start);
        System.out.println("Took Nanos="+totalNanos);
        System.out.println("Avg Nanos="+(totalNanos/LATCH_SIZE));
//        System.out.println("Total took="+totalTook.get());
//        System.out.println("Total="+totalReceipt.get());
        System.out.println("Avg MICS="+(totalNanos/LATCH_SIZE/1000));
        long milliTime = (totalNanos/1_000_000);
        System.out.println("Took millis="+milliTime+", count="+LATCH_SIZE);
        System.out.println("Per milli="+(LATCH_SIZE/milliTime)+", count="+LATCH_SIZE);

        System.out.println("End="+new Date());

        // make a report

        long totalCorrelated  = 0;
        long maxCorrelated = 0;
        long minCorrelated = Long.MAX_VALUE;
        for (long i=0; i<LATCH_SIZE; i++) {
            long startCorrelated = correlatedStartTime.get(i);
            long endCorrelated = correlatedFinishTime.get(i);
            long correlatedTook = endCorrelated-startCorrelated;
            if (i % 500 == 0)
                System.out.println("Correlated ms="+correlatedTook/1_000_000);
            totalCorrelated += correlatedTook;
            if (correlatedTook > maxCorrelated)
                maxCorrelated = totalCorrelated;
            if (totalCorrelated < minCorrelated)
                minCorrelated = totalCorrelated;
        }

        double avgCorrelatedMics = (double) totalCorrelated / LATCH_SIZE / 1000.0d;
        System.out.println("Avg correlated mics="+avgCorrelatedMics);
        System.out.println("Min correlated mics="+minCorrelated/1000);

    }
}

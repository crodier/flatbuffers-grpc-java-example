package com.example.fbgrpc.server.client;

import com.example.fbgrpc.flatbuffers.ExampleServerGrpc;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.fbgrpc.flatbuffers.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ExampleClient {

    public int LATCH_SIZE = 10_000;

    private ExampleServerGrpc.ExampleServerBlockingStub blockingStub;
    private ExampleServerGrpc.ExampleServerFutureStub nonBlockingStub;
    private ExampleServerGrpc.ExampleServerStub asyncStub;

    private final String host;
    private final int port;

    public ExampleClient(String host, int port, int experimentSize) {
        this.host = host;
        this.port = port;
        this.LATCH_SIZE = experimentSize;
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

    Map<Long, Long> correlatedStartTime = new ConcurrentHashMap<>();
    Map<Long, Long> correlatedFinishTime = new ConcurrentHashMap<>();

    public void clearMaps() {
        correlatedStartTime = new ConcurrentHashMap<>();
        correlatedFinishTime = new ConcurrentHashMap<>();
    }


    public void recordRouteAsync() throws InterruptedException {
        clearMaps();
        long start = System.nanoTime();
        runExperimentAsync();
        printStats(start);
        calcAndPrintCorrelation();
    }

    public void recordRouteBlocking() throws InterruptedException {
        clearMaps();
        long start = System.nanoTime();
        runExperimentBlocking();
        printStats(start);
        calcAndPrintCorrelation();
    }

    private void printStats(long start) {
        long end = System.nanoTime();
        long totalNanos = (end- start);
        System.out.println("Took Nanos="+totalNanos);
        System.out.println("Avg Nanos="+(totalNanos/LATCH_SIZE));
//        System.out.println("Total took="+totalTook.get());
//        System.out.println("Total="+totalReceipt.get());
        System.out.println("Avg MICS="+(totalNanos/LATCH_SIZE/1000));
        long milliTime = (totalNanos/1_000_000)+1;
        System.out.println("Took millis="+milliTime+", count="+LATCH_SIZE);
        System.out.println("Per milli="+(LATCH_SIZE/milliTime)+", count="+LATCH_SIZE);
    }

    final CountDownLatch finishLatch = new CountDownLatch(LATCH_SIZE);
    StreamObserver<Response> responseObserver = new StreamObserver<>() {
        @Override
        public void onNext(Response response) {
            long time = System.nanoTime();
//            if (finishLatch.getCount() < 2)
//                System.out.println("Time is="+time);

            correlatedFinishTime.put(response.id(), time);
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

    private void runExperimentBlocking() throws InterruptedException {
        for (long i=0; i<LATCH_SIZE; i++) {
            Request req = makeFlatBufferRequest(i);

            long startTime = System.nanoTime();
            Response response = blockingStub.doWork(req);

            correlatedStartTime.put(i, startTime);
            correlatedFinishTime.put(response.id(), System.nanoTime());
        }
        calcAndPrintCorrelation();

    }

    private void runExperimentAsync() throws InterruptedException {
        StreamObserver<Request> requestObserver = asyncStub.doWorkBidi(responseObserver);
        runExperimentInner(requestObserver);
    }

    private void runExperimentInner(StreamObserver<Request> requestObserver) throws InterruptedException {

        try
        {
            for (long i=0; i<LATCH_SIZE; i++) {
                Request req = makeFlatBufferRequest(i);

                long time = System.nanoTime();
                if (i < 2)
                    System.out.println("Time start is="+time);

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
    }

    private void calcAndPrintCorrelation() {
        long totalCorrelated  = 0;
        long maxCorrelated = 0;
        long minCorrelated = Long.MAX_VALUE;
        for (long i = 0; i< LATCH_SIZE; i++) {
            long startCorrelated = correlatedStartTime.get(i);
            long endCorrelated = correlatedFinishTime.get(i);
            long correlatedTook = endCorrelated-startCorrelated;
//            if (i % 500 == 0)
//                System.out.println("Correlated ms="+correlatedTook/1_000_000);
            totalCorrelated += correlatedTook;
            if (correlatedTook > maxCorrelated)
                maxCorrelated = totalCorrelated;
            if (correlatedTook < minCorrelated)
                minCorrelated = correlatedTook;
        }

        double avgCorrelatedMics = (double) totalCorrelated / LATCH_SIZE / 1000.0d;
        System.out.println("Avg correlated mics="+avgCorrelatedMics);
        System.out.println("Min correlated mics="+minCorrelated/1000);
    }

    private static Request makeFlatBufferRequest(long i) {
        FlatBufferBuilder builder = new FlatBufferBuilder(16);

        int requestOffset = Request.createRequest(builder,
                // builder.createString(request),
                123L, i);
        builder.finish(requestOffset);

        Request req = Request.getRootAsRequest(builder.dataBuffer());
        return req;
    }

}

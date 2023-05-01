package com.example.fbgrpc.server.client;

import com.example.fbgrpc.flatbuffers.ExampleServerGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.fbgrpc.flatbuffers.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

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


    /**
     * Human friendly client api with all Flatbuffers stuff hidden
     *
     * @param request
     * @return
     */
    public String work(String request) {

        long totalTime = 0;
        int count = 1_000;
        long minNanos = Long.MAX_VALUE;
        long minRecipt = Long.MAX_VALUE;

        FlatBufferBuilder builder = new FlatBufferBuilder();

        for (int i=0; i<count; i++) {
            long start = System.nanoTime();

            int requestOffset = Request.createRequest(builder,
                    // builder.createString(request),
                    start);
            builder.finish(requestOffset);

            Request req = Request.getRootAsRequest(builder.dataBuffer());


            // Response response = blockingStub.doWork(req);
            // ListenableFuture<Response> respFuture = nonBlockingStub.doWork(req);
//            Response response = null;
//            try {
//                response = respFuture.get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
            long finish = System.nanoTime();
            long tookNanos = finish - start;

//            long receipt = response.receipt();
//            if (receipt < minRecipt)
//                minRecipt = receipt;

            if (tookNanos < minNanos)
                minNanos = tookNanos;
            totalTime += tookNanos;
        }

        double avgMics = totalTime/count/1000;
        System.out.println("Took avg mics="+avgMics+
                ", min="+minNanos/1000+
                ", receipt="+minRecipt/1000);
        return "Done";
    }

    public void recordRoute() throws InterruptedException {
        long start = System.nanoTime();
        // info("*** RecordRoute");
        final AtomicLong totalTook = new AtomicLong();
        final AtomicLong totalReceipt = new AtomicLong();

        int LATCH_SIZE = 100_000;
        final CountDownLatch finishLatch = new CountDownLatch(LATCH_SIZE);
        StreamObserver<Response> responseObserver = new StreamObserver<Response>() {
            @Override
            public void onNext(Response response) {
                finishLatch.countDown();
                long now = System.nanoTime();
                long resTime = response.time();
                // these are meaningless.. streams...
                // TODO: need to correlate request IDs
//                totalReceipt.getAndAdd(response.receipt());
//                totalTook.getAndAdd(now-resTime);
                // System.out.println("OnNext="+response.time());
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                System.out.println("error on client+"+t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed here");
                System.out.println("here Total took="+totalTook.get());
                System.out.println("Total="+totalReceipt.get());
                finishLatch.countDown();

            }
        };

        StreamObserver<Request> requestObserver = asyncStub.doWork(responseObserver);
        try
        {

            for (int i=0; i<LATCH_SIZE; i++) {
                FlatBufferBuilder builder = new FlatBufferBuilder();

                int requestOffset = Request.createRequest(builder,
                        // builder.createString(request),
                        System.nanoTime());
                builder.finish(requestOffset);

                Request req = Request.getRootAsRequest(builder.dataBuffer());

                requestObserver.onNext(req);

                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    System.out.println("Done requests");
                    return;
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

    }
}

package com.example.fbgrpc.server.client;

import com.example.fbgrpc.flatbuffers.ExampleServerGrpc;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.fbgrpc.flatbuffers.*;

public class ExampleClient {

    private ExampleServerGrpc.ExampleServerBlockingStub blockingStub;
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
    }

    /**
     * Human friendly client api with all Flatbuffers stuff hidden
     *
     * @param request
     * @return
     */
    public String work(String request) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int requestOffset = Request.createRequest(builder, builder.createString(request));
        builder.finish(requestOffset);

        Response response = blockingStub.doWork(Request.getRootAsRequest(builder.dataBuffer()));
        return response.result();
    }
}

package com.example.fbgrpc.server;

import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.stub.StreamObserver;
import com.example.fbgrpc.flatbuffers.*;

public class ExampleServer extends ExampleServerGrpc.ExampleServerImplBase {


    @Override
    public StreamObserver<Request> doWork(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Request request) {
                Response response = createResponseOnWork(request);
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // logger.log(Level.WARNING, "Encountered error in routeChat", t);
                System.out.println("Error");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private Response createResponseOnWork(Request request) {
        // NOTE:  THE BUILDER CANT BE SHARED !!!!
        FlatBufferBuilder builder = new FlatBufferBuilder();
        long now = System.nanoTime();
        int responseOffset =
                Response.createResponse(builder,
                        // builder.createString(work + " meh..."),
                        now, request.id());
        builder.finish(responseOffset);

        return Response.getRootAsResponse(builder.dataBuffer());
    }
}

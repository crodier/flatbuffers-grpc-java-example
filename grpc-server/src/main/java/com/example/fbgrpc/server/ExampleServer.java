package com.example.fbgrpc.server;

import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.stub.StreamObserver;
import com.example.fbgrpc.flatbuffers.*;

import java.util.logging.Level;

public class ExampleServer extends ExampleServerGrpc.ExampleServerImplBase {


    @Override
    public StreamObserver<Request> doWork(StreamObserver<Response> responseObserver) {
        return new StreamObserver<Request>() {
            @Override
            public void onNext(Request request) {
                Response response = createResponseOnWork(request.time());
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

    // @Override
    public void doWork(Request request, StreamObserver<Response> responseObserver) {
        long time = request.time();

        Response response = createResponseOnWork(time);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Response createResponseOnWork(long time) {
        // NOTE:  THE BUILDER CANT BE SHARED !!!!
        FlatBufferBuilder builder = new FlatBufferBuilder();
        long now = System.nanoTime();
        long receipt = now -time;
        int responseOffset =
                Response.createResponse(builder,
                        // builder.createString(work + " meh..."),
                        now, receipt);
        builder.finish(responseOffset);

        return Response.getRootAsResponse(builder.dataBuffer());
    }
}

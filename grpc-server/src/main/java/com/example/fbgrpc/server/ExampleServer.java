package com.example.fbgrpc.server;

import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.stub.StreamObserver;
import com.example.fbgrpc.flatbuffers.*;

public class ExampleServer extends ExampleServerGrpc.ExampleServerImplBase {
    @Override
    public void doWork(Request request, StreamObserver<Response> responseObserver) {
        String work = request.work();

        Response response = createResponseOnWork(work);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Response createResponseOnWork(String work) {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int responseOffset = Response.createResponse(builder, builder.createString(work + " meh..."));
        builder.finish(responseOffset);

        return Response.getRootAsResponse(builder.dataBuffer());
    }
}

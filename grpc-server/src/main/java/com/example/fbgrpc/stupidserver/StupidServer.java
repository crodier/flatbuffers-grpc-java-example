package com.example.fbgrpc.stupidserver;

import com.exmaple.stupidserver.flatbuffers.Request;
import com.exmaple.stupidserver.flatbuffers.Response;
import com.exmaple.stupidserver.flatbuffers.StupidServerGrpc;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.stub.StreamObserver;

public class StupidServer extends StupidServerGrpc.StupidServerImplBase {
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

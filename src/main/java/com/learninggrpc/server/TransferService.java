package com.learninggrpc.server;

import com.learninggrpc.models.TransferRequest;
import com.learninggrpc.models.TransferResponse;
import com.learninggrpc.models.TransferServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferStreamingRequest(responseObserver);
    }
}

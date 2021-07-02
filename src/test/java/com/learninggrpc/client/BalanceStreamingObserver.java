package com.learninggrpc.client;

import com.learninggrpc.models.Balance;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class BalanceStreamingObserver implements StreamObserver<Balance> {

    private CountDownLatch latch;

    public BalanceStreamingObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(Balance balance) {
        System.out.println(
                "Final Balance : " + balance.getAmount()
        );
    }

    @Override
    public void onError(Throwable throwable) {
        this.latch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println(
                "Server is done!"
        );
        this.latch.countDown();
    }
}

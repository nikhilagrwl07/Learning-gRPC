package com.learninggrpc.client.metadata;

import com.learninggrpc.client.BalanceStreamingObserver;
import com.learninggrpc.client.MoneyStreamingResponse;
import com.learninggrpc.client.deadline.DeadlineInterceptor;
import com.learninggrpc.models.*;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    void setUp() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(channel);
        this.bankServiceStub = BankServiceGrpc.newStub(channel);
    }

    @Test
    public void balanceTest() {
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(10)
                .build();

        for (int i = 0; i < 20; i++) {
            try {
                int random = ThreadLocalRandom.current().nextInt(1, 4);
                System.out.println("Random : " + random);
                Balance balance = this.blockingStub
                        .withCallCredentials(new UserSessionToken("user-secret-" + random + ":standard"))
                        .getBalance(balanceCheckRequest);
                System.out.println(
                        "Received : " + balance.getAmount()
                );
            } catch (StatusRuntimeException e) {
                e.printStackTrace();
                // go with default value
            }
        }
    }

    @Test
    public void withdrawTest() {
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(6).setAmount(50).build();
        try {
            this.blockingStub
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                    .withdraw(withdrawRequest)
                    .forEachRemaining(money -> System.out.println(
                            "Received : " + money.getValue()
                    ));
        } catch (StatusRuntimeException e) {

        }
    }

    @Test
    public void withdrawAsyncTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(10).setAmount(37).build();
        WithdrawRequest withdrawRequest2 = WithdrawRequest.newBuilder().setAccountNumber(10).setAmount(100).build();
        this.bankServiceStub.withdraw(withdrawRequest, new MoneyStreamingResponse(latch));
        this.bankServiceStub.withdraw(withdrawRequest2, new MoneyStreamingResponse(latch));
        latch.await();
    }

    @Test
    public void cashStreamingRequest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<DepositRequest> requestStreamObserver = this.bankServiceStub.cashDeposit(new BalanceStreamingObserver(latch));
        for (int i = 0; i < 10; i++) {
            DepositRequest depositRequest = DepositRequest.newBuilder().setAccountNumber(8).setAmount(10).build();
            requestStreamObserver.onNext(depositRequest);
        }
        requestStreamObserver.onCompleted();
        latch.await();
    }
}

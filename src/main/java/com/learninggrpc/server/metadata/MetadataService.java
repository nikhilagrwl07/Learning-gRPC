package com.learninggrpc.server.metadata;

import com.google.common.util.concurrent.Uninterruptibles;
import com.learninggrpc.models.*;
import com.learninggrpc.server.AccountDatabase;
import com.learninggrpc.server.CashStreamingRequest;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);
        UserRole userRole = ServerConstants.CTX_USER_TOKEN.get();
        UserRole userRole1 = ServerConstants.CTX_USER_TOKEN1.get();
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);
        System.out.println(
                "User role " + userRole +
                        " User role1 " + userRole1 +
                        " Amount " + amount
        );
        Balance balance = Balance.newBuilder()
                .setAmount(amount)
                .build();

        // simulate time consuming call
//        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();   // Assume: multiple of 10
        int balance = AccountDatabase.getBalance(accountNumber);

        if (amount % 10 != 0) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLE)
                    .build();

            metadata.put(errorKey, withdrawalError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }

        if (amount > balance) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE)
                    .build();

            metadata.put(errorKey, withdrawalError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }

        for (int i = 0; i < (amount / 10); i++) {
            Money money = Money.newBuilder().setValue(10).build();
            if (!Context.current().isCancelled()) {
                responseObserver.onNext(money);
                AccountDatabase.deductBalance(accountNumber, 10);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }


}

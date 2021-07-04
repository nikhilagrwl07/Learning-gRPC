package com.learninggrpc.client.metadata;

import com.learninggrpc.models.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;

public class ClientConstants {

    private static final Metadata METADATA = new Metadata();
    public static final Metadata.Key<String> USER_TOKEN =
            Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<WithdrawalError> WITHDRAWAL_ERROR_KEY =
            ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());

    static {
        METADATA.put(
                Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER),
                "bank-client-secret-wrong"
        );
    }

    public static Metadata getClientToken() {
        return METADATA;
    }
}

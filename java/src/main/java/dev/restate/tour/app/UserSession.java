package dev.restate.tour.app;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.tour.generated.UserSessionGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

public class UserSession extends UserSessionGrpc.UserSessionImplBase implements RestateBlockingService {
    @Override
    public void addTicket(ReserveTicket request, StreamObserver<BoolValue> responseObserver) {
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }

    @Override
    public void expireTicket(ExpireTicketRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void checkout(CheckoutRequest request, StreamObserver<BoolValue> responseObserver) {
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }
}

package dev.restate.tour.part1;

import com.google.protobuf.BoolValue;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.tour.generated.CheckoutGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

public class Checkout extends CheckoutGrpc.CheckoutImplBase implements RestateBlockingService {
    @Override
    public void checkout(CheckoutFlowRequest request, StreamObserver<BoolValue> responseObserver) {
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }
}

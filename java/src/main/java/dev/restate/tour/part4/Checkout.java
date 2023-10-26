package dev.restate.tour.part4;

import com.google.protobuf.BoolValue;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.sdk.blocking.RestateContext;
import dev.restate.sdk.core.TypeTag;
import dev.restate.tour.auxiliary.EmailClient;
import dev.restate.tour.auxiliary.PaymentClient;
import dev.restate.tour.generated.CheckoutGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.UUID;

public class Checkout extends CheckoutGrpc.CheckoutImplBase implements RestateBlockingService {

    PaymentClient paymentClient = PaymentClient.get();
    EmailClient emailClient = EmailClient.get();

    @Override
    public void checkout(CheckoutFlowRequest request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();

        // Generate idempotency key for the stripe client
        var idempotencyKey = ctx.sideEffect(TypeTag.STRING_UTF8, () -> UUID.randomUUID().toString());

        // We are a uniform shop where everything costs 40 USD
        var totalPrice = request.getTicketsList().size() * 40.0;

        boolean success = ctx.sideEffect(TypeTag.ofClass(boolean.class), () -> paymentClient.failingCall(idempotencyKey, totalPrice));

        if(success) {
            ctx.sideEffect(()-> emailClient.notifyUserOfPaymentSuccess(request.getUserId()));
        } else {
            ctx.sideEffect(() -> emailClient.notifyUserOfPaymentFailure(request.getUserId()));
        }

        responseObserver.onNext(BoolValue.of(success));
        responseObserver.onCompleted();
    }
}

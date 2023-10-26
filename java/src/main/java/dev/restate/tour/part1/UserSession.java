package dev.restate.tour.part1;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.sdk.blocking.RestateContext;
import dev.restate.tour.generated.CheckoutGrpc;
import dev.restate.tour.generated.TicketServiceGrpc;
import dev.restate.tour.generated.Tour.*;
import dev.restate.tour.generated.UserSessionGrpc;
import io.grpc.stub.StreamObserver;

public class UserSession extends UserSessionGrpc.UserSessionImplBase implements RestateBlockingService {
    @Override
    public void addTicket(ReserveTicket request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();
        ctx.oneWayCall(
                TicketServiceGrpc.getReserveMethod(),
                Ticket.newBuilder().setTicketId(request.getTicketId()).build()
        );

        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }

    @Override
    public void expireTicket(ExpireTicketRequest request, StreamObserver<Empty> responseObserver) {
        RestateContext ctx = restateContext();
        ctx.oneWayCall(
                TicketServiceGrpc.getUnreserveMethod(),
                Ticket.newBuilder().setTicketId(request.getTicketId()).build()
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void checkout(CheckoutRequest request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();
        var req = CheckoutFlowRequest.newBuilder()
                .setUserId(request.getUserId())
                .addTickets("456")
                .build();
        var success = ctx.call(CheckoutGrpc.getCheckoutMethod(), req).await();

        responseObserver.onNext(success);
        responseObserver.onCompleted();
    }

}

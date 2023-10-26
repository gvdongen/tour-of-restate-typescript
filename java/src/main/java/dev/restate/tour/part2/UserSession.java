package dev.restate.tour.part2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.sdk.blocking.RestateContext;
import dev.restate.sdk.core.StateKey;
import dev.restate.sdk.core.serde.jackson.JacksonSerde;
import dev.restate.tour.generated.CheckoutGrpc;
import dev.restate.tour.generated.TicketServiceGrpc;
import dev.restate.tour.generated.UserSessionGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class UserSession extends UserSessionGrpc.UserSessionImplBase implements RestateBlockingService {

    public static final StateKey<Set<String>> STATE_KEY = StateKey.of("tickets", JacksonSerde.typeRef(new TypeReference<>() {}));

    @Override
    public void addTicket(ReserveTicket request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();

        var reservationSuccess = ctx.call(
            TicketServiceGrpc.getReserveMethod(),
            Ticket.newBuilder().setTicketId(request.getTicketId()).build()
        ).await();

        if(reservationSuccess.getValue()) {
            var tickets = ctx.get(STATE_KEY).orElseGet(HashSet::new);
            tickets.add(request.getTicketId());
            ctx.set(STATE_KEY, tickets);

            ctx.delayedCall(UserSessionGrpc.getExpireTicketMethod(),
                    ExpireTicketRequest.newBuilder().setTicketId(request.getTicketId()).setUserId(request.getUserId()).build(),
                    Duration.ofMinutes(15));
        }

        responseObserver.onNext(reservationSuccess);
        responseObserver.onCompleted();
    }

    @Override
    public void expireTicket(ExpireTicketRequest request, StreamObserver<Empty> responseObserver) {
        RestateContext ctx = restateContext();
        var tickets = ctx.get(STATE_KEY).orElseGet(HashSet::new);

        var removed = tickets.removeIf(s -> s.equals(request.getTicketId()));

        if(removed) {
            ctx.set(STATE_KEY, tickets);

            ctx.oneWayCall(
                    TicketServiceGrpc.getUnreserveMethod(),
                    Ticket.newBuilder().setTicketId(request.getTicketId()).build()
            );
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void checkout(CheckoutRequest request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();
        var tickets = ctx.get(STATE_KEY).orElseGet(HashSet::new);

        if(tickets.isEmpty()){
            responseObserver.onNext(BoolValue.of(false));
        }

        var checkoutSuccess = ctx.call(
                CheckoutGrpc.getCheckoutMethod(),
                CheckoutFlowRequest
                        .newBuilder()
                        .setUserId(request.getUserId())
                        .addAllTickets(tickets)
                        .build()
        ).await();

        if(checkoutSuccess.getValue()){
            ctx.clear(STATE_KEY);
        }

        responseObserver.onNext(checkoutSuccess);
        responseObserver.onCompleted();
    }
}

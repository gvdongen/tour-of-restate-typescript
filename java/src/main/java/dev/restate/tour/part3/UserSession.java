package dev.restate.tour.part3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.sdk.blocking.RestateContext;
import dev.restate.sdk.core.StateKey;
import dev.restate.sdk.core.serde.jackson.JacksonSerde;
import dev.restate.tour.generated.CheckoutGrpc;
import dev.restate.tour.generated.TicketServiceGrpc;
import dev.restate.tour.generated.Tour.*;
import dev.restate.tour.generated.UserSessionGrpc;
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
        ).await().getValue();

        if(reservationSuccess) {
            var tickets = ctx.get(STATE_KEY).orElseGet(HashSet::new);
            tickets.add(request.getTicketId());
            ctx.set(STATE_KEY, tickets);

            ctx.delayedCall(UserSessionGrpc.getExpireTicketMethod(),
                    ExpireTicketRequest.newBuilder().setTicketId(request.getTicketId()).setUserId(request.getUserId()).build(),
                    Duration.ofMinutes(15));
        }

        responseObserver.onNext(BoolValue.of(reservationSuccess));
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

        // 1. Retrieve the tickets from state
        var tickets = ctx.get(STATE_KEY).orElseGet(HashSet::new);

        // 2. If there are no tickets, return `false`
        if(tickets.isEmpty()){
            responseObserver.onNext(BoolValue.of(false));
        }

        // 3. Call the `checkout` function of the checkout service with the tickets
        var checkoutSuccess = ctx.call(
                CheckoutGrpc.getCheckoutMethod(),
                CheckoutFlowRequest
                        .newBuilder()
                        .setUserId(request.getUserId())
                        .addAllTickets(tickets)
                        .build()
        ).await();

        // 4. If this was successful, empty the tickets.
        // Otherwise, let the user try again.
        if(checkoutSuccess.getValue()){
            ctx.clear(STATE_KEY);
        }

        responseObserver.onNext(checkoutSuccess);
        responseObserver.onCompleted();
    }
}

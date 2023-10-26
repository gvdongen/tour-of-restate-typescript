package dev.restate.tour.part4;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.sdk.blocking.RestateContext;
import dev.restate.sdk.core.StateKey;
import dev.restate.sdk.core.serde.jackson.JacksonSerde;
import dev.restate.tour.auxiliary.TicketStatus;
import dev.restate.tour.generated.TicketServiceGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

public class TicketService extends TicketServiceGrpc.TicketServiceImplBase implements RestateBlockingService {

    public static final StateKey<TicketStatus> STATE_KEY = StateKey.of("status", JacksonSerde.typeRef(new TypeReference<>() {}));

    @Override
    public void reserve(Ticket request, StreamObserver<BoolValue> responseObserver) {
        RestateContext ctx = restateContext();

        var status = ctx.get(STATE_KEY).orElse(TicketStatus.Available);

        if(status.equals(TicketStatus.Available)){
            ctx.set(STATE_KEY, TicketStatus.Reserved);
            responseObserver.onNext(BoolValue.of(true));
        } else {
            responseObserver.onNext(BoolValue.of(false));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void unreserve(Ticket request, StreamObserver<Empty> responseObserver) {
        RestateContext ctx = restateContext();

        var status = ctx.get(STATE_KEY).orElse(TicketStatus.Available);

        if(!status.equals(TicketStatus.Sold)){
            ctx.clear(STATE_KEY);
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void markAsSold(Ticket request, StreamObserver<Empty> responseObserver) {
        RestateContext ctx = restateContext();

        var status = ctx.get(STATE_KEY).orElse(TicketStatus.Available);

        if(status.equals(TicketStatus.Reserved)){
            ctx.set(STATE_KEY, TicketStatus.Sold);
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}

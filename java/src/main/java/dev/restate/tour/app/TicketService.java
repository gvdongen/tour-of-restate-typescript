package dev.restate.tour.app;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import dev.restate.sdk.blocking.RestateBlockingService;
import dev.restate.tour.generated.TicketServiceGrpc;
import dev.restate.tour.generated.Tour.*;
import io.grpc.stub.StreamObserver;

public class TicketService extends TicketServiceGrpc.TicketServiceImplBase implements RestateBlockingService {
    @Override
    public void reserve(Ticket request, StreamObserver<BoolValue> responseObserver) {
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }

    @Override
    public void unreserve(Ticket request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void markAsSold(Ticket request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}

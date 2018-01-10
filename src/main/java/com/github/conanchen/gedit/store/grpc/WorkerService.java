package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.config.AuthInterceptor;
import com.github.conanchen.gedit.store.config.LogInterceptor;
import com.github.conanchen.gedit.store.search.grpc.*;
import com.github.conanchen.gedit.store.worker.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class WorkerService extends StoreWorkerApiGrpc.StoreWorkerApiImplBase {

    public WorkerService() {
        super();
    }

    @Override
    public void add(AddRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        super.add(request, responseObserver);
    }

    @Override
    public void listByStore(ListByStoreRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        super.listByStore(request, responseObserver);
    }

    @Override
    public void listByWorker(ListByWorkerRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        super.listByWorker(request, responseObserver);
    }

    @Override
    public void ban(BanRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        super.ban(request, responseObserver);
    }
}

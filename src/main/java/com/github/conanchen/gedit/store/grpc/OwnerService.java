package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.grpc.interceptor.LogInterceptor;
import com.github.conanchen.gedit.store.owner.grpc.ListByOwnerRequest;
import com.github.conanchen.gedit.store.owner.grpc.OwnershipResponse;
import com.github.conanchen.gedit.store.owner.grpc.StoreOwnerApiGrpc;
import com.github.conanchen.gedit.store.owner.grpc.TransferRequest;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class OwnerService extends StoreOwnerApiGrpc.StoreOwnerApiImplBase {

    public OwnerService() {
        super();
    }

    @Override
    public void transfer(TransferRequest request, StreamObserver<OwnershipResponse> responseObserver) {
        super.transfer(request, responseObserver);
    }

    @Override
    public void listByOwner(ListByOwnerRequest request, StreamObserver<OwnershipResponse> responseObserver) {
        super.listByOwner(request, responseObserver);
    }
}

package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.grpc.interceptor.LogInterceptor;
import com.github.conanchen.gedit.store.owner.grpc.*;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import javax.annotation.Resource;

@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class OwnerService extends StoreOwnerApiGrpc.StoreOwnerApiImplBase {
    @Resource
    private StoreProfileRepository profileRepository;
    @Override
    public void transfer(TransferOwnershipRequest request, StreamObserver<OwnershipResponse> responseObserver) {

    }

    @Override
    public void listByOwner(ListOwnershipByOwnerRequest request, StreamObserver<OwnershipResponse> responseObserver) {
    }

    @Override
    public void listMyStore(ListMyStoreRequest request, StreamObserver<OwnershipResponse> responseObserver) {
    }

}

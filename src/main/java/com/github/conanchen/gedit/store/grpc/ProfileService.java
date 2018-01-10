package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.config.AuthInterceptor;
import com.github.conanchen.gedit.store.config.LogInterceptor;
import com.github.conanchen.gedit.store.owner.grpc.ListByOwnerRequest;
import com.github.conanchen.gedit.store.owner.grpc.OwnershipResponse;
import com.github.conanchen.gedit.store.owner.grpc.StoreOwnerApiGrpc;
import com.github.conanchen.gedit.store.owner.grpc.TransferRequest;
import com.github.conanchen.gedit.store.profile.grpc.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.text.DateFormat;

@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class ProfileService extends StoreProfileApiGrpc.StoreProfileApiImplBase {
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
    public ProfileService() {
        super();
    }

    @Override
    public void create(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {
        // Access to identity.
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("get start claims.getId()=%s, request=[%s]", claims.getId(), gson.toJson(request)));

    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void get(GetRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        super.get(request, responseObserver);
    }

    @Override
    public void list(ListRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        super.list(request, responseObserver);
    }

    @Override
    public void findByName(FindByNameRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        super.findByName(request, responseObserver);
    }

    @Override
    public void ban(BanRequest request, StreamObserver<BanResponse> responseObserver) {
        super.ban(request, responseObserver);
    }
}

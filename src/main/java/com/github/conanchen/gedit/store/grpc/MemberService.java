package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.config.LogInterceptor;
import com.github.conanchen.gedit.store.config.AuthInterceptor;
import com.github.conanchen.gedit.store.member.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class MemberService extends StoreMemberApiGrpc.StoreMemberApiImplBase {

    /**
     * add member of store
     */
    @Override
    public void add(AddRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.add(request, responseObserver);
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void listByStore(ListByStoreRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.listByStore(request, responseObserver);
    }

    @Override
    public void listByMember(ListByMemberRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.listByMember(request, responseObserver);
    }

    @Override
    public void ban(BanRequest request, StreamObserver<BanResponse> responseObserver) {
        super.ban(request, responseObserver);
    }
}

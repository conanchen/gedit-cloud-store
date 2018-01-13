package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.grpc.interceptor.LogInterceptor;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.member.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
@Slf4j
@GRpcService(interceptors = {LogInterceptor.class ,AuthInterceptor.class,})
public class MemberService extends StoreMemberApiGrpc.StoreMemberApiImplBase {

    @Override
    public void add(AddMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.add(request, responseObserver);
    }

    @Override
    public void update(UpdateMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void listByStore(ListMembershipByStoreRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.listByStore(request, responseObserver);
    }

    @Override
    public void listByMember(ListMembershipByMemberRequest request, StreamObserver<MembershipResponse> responseObserver) {
        super.listByMember(request, responseObserver);
    }

    @Override
    public void ban(BanMembershipRequest request, StreamObserver<BanMembershipResponse> responseObserver) {
        super.ban(request, responseObserver);
    }
}

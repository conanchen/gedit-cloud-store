package com.github.conanchen.gedit.store.grpc.client;

import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.user.fans.grpc.FanshipResponse;
import com.github.conanchen.gedit.user.fans.grpc.FindParentFanshipRequest;
import com.github.conanchen.gedit.user.fans.grpc.UserFansApiGrpc;
import com.github.conanchen.gedit.user.profile.grpc.FindByMobileRequest;
import com.github.conanchen.gedit.user.profile.grpc.GetRequest;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileApiGrpc;
import com.github.conanchen.gedit.user.profile.grpc.UserProfileResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class UserClient {
    @Value("${user.uri}")
    private String uri;
    //channel
    private ManagedChannel channel;
    // search stub
    private UserProfileApiGrpc.UserProfileApiStub userProfileApiStub;
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(uri,9980)
                .usePlaintext(true)
                .build();
        userProfileApiStub = UserProfileApiGrpc.newStub(channel);
    }
    public interface FindByMobileCallBack{
        void onFindByMobileResponse(UserProfileResponse response);
    }

    public void findByMobile(String mobile, FindByMobileCallBack callBack){
        userProfileApiStub = MetadataUtils.attachHeaders(userProfileApiStub, AuthInterceptor.HEADERS.get());
        userProfileApiStub.findByMobile(FindByMobileRequest.newBuilder()
                        .setMobile(mobile)
                        .build(),
                new StreamObserver<UserProfileResponse>() {
                    @Override
                    public void onNext(UserProfileResponse value) {
                        callBack.onFindByMobileResponse(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        callBack.onFindByMobileResponse(
                                UserProfileResponse.newBuilder()
                                .setStatus(Status.newBuilder()
                                        .setCode(Status.Code.UNKNOWN)
                                        .setDetails("远程调用错误")
                                        .build())
                                .build()
                        );
                    }

                    @Override
                    public void onCompleted() {
                        log.info("******** complete *********");
                    }
                }
        );
    }
}

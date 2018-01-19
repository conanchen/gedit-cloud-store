package com.github.conanchen.gedit.client;

import com.github.conanchen.gedit.hello.grpc.HelloGrpc;
import com.github.conanchen.gedit.store.profile.grpc.ListStoreRequest;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileApiGrpc;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import com.github.conanchen.gedit.user.auth.grpc.UserAuthApiGrpc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class TestClient {
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
    private StoreProfileApiGrpc.StoreProfileApiBlockingStub blockingStub;
    private ManagedChannel channel;
    private static final String local = "127.0.0.1";
    private static final String remote = "dev.jifenpz.com";
    @Before
    public void init(){
        channel = ManagedChannelBuilder.forAddress(remote,8980)
                .usePlaintext(true)
                .build();

        blockingStub = StoreProfileApiGrpc.newBlockingStub(channel);
    }
    @After
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
    }

    public  void list(int from){
        ListStoreRequest request = ListStoreRequest.newBuilder().setFrom(from).setType("0").setSize(5).build();
        Iterator<StoreProfileResponse> responses = blockingStub.list(request);
        responses.forEachRemaining(n -> System.out.println("row " + n.getStoreProfile().getFrom()+ " :" + gson.toJson(n)));
    }

    public  void list(int from,int size){
        ListStoreRequest request = ListStoreRequest.newBuilder().setFrom(from).setType("0").setSize(size).build();
        Iterator<StoreProfileResponse> responses = blockingStub.list(request);
        responses.forEachRemaining(n -> System.out.println("row " + n.getStoreProfile().getFrom()+ " :" + gson.toJson(n)));
    }

    //测试时删除给profileService @grpcService设置applyGlobalInterceptors false并注释掉下面list方法中的用户访问日志
    @Test
    public void list(){
        list(0,1);
    }
}

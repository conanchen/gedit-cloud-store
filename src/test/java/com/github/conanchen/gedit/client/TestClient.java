package com.github.conanchen.gedit.client;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.hello.grpc.HelloGrpc;
import com.github.conanchen.gedit.store.profile.grpc.*;
import com.github.conanchen.gedit.user.auth.grpc.UserAuthApiGrpc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class TestClient {
    private static final Logger log = LoggerFactory.getLogger(TestClient.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
        //access token
        String accessToken = "BearereyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWykwsUbIyNDU0MzY1MjYw0lEqLk1SslIyMTCysDCwMDYzNEixNEhLNjAEsRKNU1MsTQ0MDIyVdJSySjLxK0wDKjQBKkytKEC2ohYAkjAow3UAAAA.OQ3rlz5NTv4aV5DvsWbbVKE6Ow7BFy4_P51W7ci6X6a68WSu-qgJ2sAlbw9qWNunMgPZiW1dYGv_HdnlqzYOdA";
        // create a custom header
        Metadata header=new Metadata();
        Metadata.Key<String> key =
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, accessToken);
        blockingStub = MetadataUtils.attachHeaders(blockingStub, header);
    }
    @After
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
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
    @Test
    public void create(){
        CreateStoreResponse response =  blockingStub.create(CreateStoreRequest.newBuilder()
                .setName("haige")
                .setDetailAddress("chengdu")
                .setDistrictUuid("110000")
                .setDetailAddress("wuhou")
                .setLocation(Location.newBuilder().setLon(12.121212D).setLat(12.121213D).build())
                .build());
        log.info(gson.toJson(response));
    }
}

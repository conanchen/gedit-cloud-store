package com.github.conanchen.gedit.client;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.store.profile.grpc.*;
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
        channel = ManagedChannelBuilder.forAddress(local,8980)
                .usePlaintext(true)
                .build();

        blockingStub = StoreProfileApiGrpc.newBlockingStub(channel);
        //access token
        String accessToken = "BearereyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAF3NwQqAIBCE4XfZs4fRdUV9GzUDOwUZBNG7l9SpOf2HD-akljpFLdqxWCtG0bZnimRhvIdnpzEFzAV6VOI6BQHApGjp7QcLmw8Wmytj7IH1WN-LIMEIXzeBEd8hdQAAAA.WpD5DxFWFEQOj8CdPELyq8xLYu5T8xSkK-PAPk_QzVjUIqM4XZJr4e7XD6aw5f0dYqU13k5hIZ1K0wfOncZI6A";
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
        list(0,10);
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
    @Test
    public void get(){
        StoreProfileResponse response = blockingStub.get(GetStoreRequest.newBuilder().setUuid("40288084610dd68901610dd9ab880001").build());
        log.info(gson.toJson(response));
    }
    @Test
    public void update(){
        UpdateStoreResponse response = blockingStub.update(UpdateStoreRequest.newBuilder()
                .setActive(true)
                .setUuid("40288084610dd68901610dd9ab880001")
                .setDesc("好吃一匹") //update
                .setName("haige")
                .setDetailAddress("chengdu")
                .setDistrictUuid("110000")
                .setDetailAddress("wuhou")
                .setLocation(Location.newBuilder().setLon(12.121212D).setLat(12.121213D).build())
                .build());
        log.info(gson.toJson(response));
    }
}

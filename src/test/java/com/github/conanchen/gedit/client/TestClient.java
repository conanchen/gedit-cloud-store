package com.github.conanchen.gedit.client;

import com.github.conanchen.gedit.store.profile.grpc.ListRequest;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileApiGrpc;
import com.github.conanchen.gedit.store.profile.grpc.StoreProfileResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class TestClient {
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
    private StoreProfileApiGrpc.StoreProfileApiBlockingStub blockingStub;
    private final ManagedChannel channel;
    public TestClient(String host,int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();

        blockingStub = StoreProfileApiGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public  void list(int from){
        ListRequest request = ListRequest.newBuilder().setFrom(from).setType("0").setSize(5).build();
        Iterator<StoreProfileResponse> responses = blockingStub.list(request);
        responses.forEachRemaining(n -> System.out.println("row " + n.getStoreProfile().getFrom()+ " :" + gson.toJson(n)));
    }
    public static void main(String[] args) throws InterruptedException {
        TestClient client = new TestClient("127.0.0.1",8980);
        for(int i=0;i<5;i++){
            System.out.println(String.format("第%s调用",i + 1));
            client.list(i * 5);
        }
        client.shutdown();

    }
}

package com.github.conanchen.gedit.hello.grpc;

import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.hello.grpc.HelloGrpc;
import com.github.conanchen.gedit.hello.grpc.HelloReply;
import com.github.conanchen.gedit.hello.grpc.HelloRequest;
import com.github.conanchen.gedit.store.grpc.interceptor.LogInterceptor;
import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.text.SimpleDateFormat;
import java.util.UUID;

@Slf4j
@GRpcService(interceptors = {LogInterceptor.class},applyGlobalInterceptors = false)
public class HelloService extends HelloGrpc.HelloImplBase {
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        final HelloReply.Builder replyBuilder = HelloReply.newBuilder()
                .setStatus(Status.newBuilder()
                        .setCode(Status.Code.OK)
                        .setDetails("Hello很高兴回复你，你的hello很温暖。")
                        .build())
                .setUuid(UUID.randomUUID().toString())
                .setMessage(String.format("Hello %s@%s ", request.getName(), dateFormat.format(System.currentTimeMillis())))
                .setCreated(System.currentTimeMillis())
                .setLastUpdated(System.currentTimeMillis());
        HelloReply helloReply = replyBuilder.build();
        responseObserver.onNext(helloReply);
        log.info(String.format("HelloService.sayHello() %s:%s gson=%s", helloReply.getUuid(), helloReply.getMessage(), gson.toJson(helloReply)));
        responseObserver.onCompleted();
    }
}
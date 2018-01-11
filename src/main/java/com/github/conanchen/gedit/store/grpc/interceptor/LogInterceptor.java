package com.github.conanchen.gedit.store.grpc.interceptor;

import com.google.gson.Gson;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;

/**
 * Created by conanchen on 9/7/16.
 */
@Slf4j
@Order(10)
@GRpcGlobalInterceptor
public class LogInterceptor implements ServerInterceptor {
    private static final Gson gson = new Gson();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        log.info(String.format(
                "call.getMethodDescriptor().getFullMethodName()=[%s]\n headers.keys=[%s],\n remote=%s",
                call.getMethodDescriptor().getFullMethodName(),
                "-" + gson.toJson(headers.keys()),
                call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)
                )
        );
        //TODO: https://stackoverflow.com/questions/40112374/how-do-i-access-request-metadata-for-a-java-grpc-service-i-am-defining
        return next.startCall(call, headers);
    }
}

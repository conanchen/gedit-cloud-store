package com.github.conanchen.gedit.store.grpc.callback;

import com.github.conanchen.gedit.common.grpc.Status;

public interface GrpcApiCallback {
    void onGrpcApiError(Status status);

    void onGrpcApiCompleted();
}

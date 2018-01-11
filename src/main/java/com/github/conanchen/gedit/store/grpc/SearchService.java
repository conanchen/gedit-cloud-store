package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.grpc.interceptor.LogInterceptor;
import com.github.conanchen.gedit.store.search.grpc.*;
import com.github.conanchen.gedit.store.search.grpc.DeleteRequest;
import com.github.conanchen.gedit.store.search.grpc.DeleteResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@Slf4j
@GRpcService(interceptors = {AuthInterceptor.class, LogInterceptor.class})
public class SearchService extends StoreSearchApiGrpc.StoreSearchApiImplBase {

    public SearchService() {
        super();
    }

    @Override
    public void index(IndexRequest request, StreamObserver<IndexResponse> responseObserver) {
        super.index(request, responseObserver);
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    @Override
    public void search(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        super.search(request, responseObserver);
    }
}

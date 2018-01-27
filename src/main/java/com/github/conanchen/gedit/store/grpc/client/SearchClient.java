package com.github.conanchen.gedit.store.grpc.client;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.search.grpc.IndexStoreRequest;
import com.github.conanchen.gedit.store.search.grpc.StoreSearchApiGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SearchClient {
    private static final String EMPTY_STR = "";
    @Value("${search.uri}")
    private String uri;
    //channel
    private ManagedChannel channel;
    // search stub
    private StoreSearchApiGrpc.StoreSearchApiBlockingStub searchApiBlockingStub;
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(uri,9985)
                .usePlaintext(true)
                .build();
        searchApiBlockingStub = StoreSearchApiGrpc.newBlockingStub(channel);
    }

    public void index(StoreProfile storeProfile){
        searchApiBlockingStub.index(IndexStoreRequest.newBuilder()
                .setDesc(storeProfile.getDescr() == null ? EMPTY_STR : storeProfile.getDescr())
                .setPointsRate(storeProfile.getPointsRate() == null ? 0.0D : storeProfile.getPointsRate())
                .setLocation(Location.newBuilder()
                        .setLat(storeProfile.getLat() ==  null ? 0.0D : storeProfile.getLat())
                        .setLon(storeProfile.getLon() ==  null ? 0.0D : storeProfile.getLon())
                        .build())
                .setLogo(storeProfile.getLogo() == null ? "" : storeProfile.getLogo())
                .setName(storeProfile.getName())
                .setType(storeProfile.getType() == null ? "" : storeProfile.getType())
                .setUuid(storeProfile.getUuid())
                .build());
    }
}

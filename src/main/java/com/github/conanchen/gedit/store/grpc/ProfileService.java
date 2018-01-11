package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.profile.grpc.*;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import com.github.conanchen.gedit.store.repository.page.OffsetBasedPageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.martiansoftware.validation.Hope;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@GRpcService
public class ProfileService extends StoreProfileApiGrpc.StoreProfileApiImplBase {

    private static final Type listStrType = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
    @Resource
    private StoreProfileRepository profileRepository;

    public ProfileService() {
        super();
    }

    @Override
    public void create(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        StoreProfile storeProfile = StoreProfile.builder()
                .ownerId(claims.getSubject())
                .active(true)
                .detailAddress(request.getDetailAddress())
                .districtUuid(request.getDistrictId())
                .name(request.getName())
                .lat(request.getLocation().getLat())
                .lon(request.getLocation().getLon())
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();
        profileRepository.save(storeProfile);
        CreateResponse response = CreateResponse.newBuilder()
                .setName(storeProfile.getName())
                .setUuid(storeProfile.getUuid())
                .setOwnerId(storeProfile.getOwnerId())
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode())).setDetails("新增成功").build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("store create access success");
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        StoreProfile profile = (StoreProfile) profileRepository.findOne(request.getUuid());
        BeanUtils.copyProperties(request,profile);
        profile.setDescr(request.getDesc());
        profile.setLon(request.getLocation().getLon());
        profile.setLat(request.getLocation().getLat());
        profile.setImages(request.getImages().toString());
        profile.setUpdatedDate(new Date());
        profileRepository.save(profile);

        UpdateResponse response = UpdateResponse.newBuilder()
                .setUuid(profile.getUuid())
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode())).setDetails("更新成功").build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("store update access success");
    }

    @Override
    public void get(GetRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        String uuid = Hope.that(request.getUuid()).isNotNullOrEmpty().value();
        StoreProfile profile = (StoreProfile) profileRepository.findOne(uuid);
        responseObserver.onNext(modelToRep(profile,0));
        responseObserver.onCompleted();
        log.info("store get access success");
    }

    @Override
    public void list(ListRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Integer from = Hope.that(request.getFrom()).isPresent().value();

        Integer size = Hope.that(request.getSize()).isPresent().value();

        final int tempForm = from == 0 ? 0 : from + 1;
        Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
        List<StoreProfile> list;
        if (!StringUtils.isEmpty(request.getType())) {
            list = profileRepository.findByType(request.getType(),pageable);
        }else{
            list = profileRepository.findAll(pageable).getContent();
        }
        for (StoreProfile profile : list){
            responseObserver.onNext(modelToRep(profile,tempForm));
        }
        responseObserver.onCompleted();
        log.info("store list access success");
    }

    @Override
    public void findByName(FindByNameRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        String name = Hope.that(request.getName()).isNotNullOrEmpty().value();
        StoreProfile profile = profileRepository.findByName(name);
        responseObserver.onNext(modelToRep(profile,0));
        responseObserver.onCompleted();
        log.info("store get access success");
    }

    @Override
    public void ban(BanRequest request, StreamObserver<BanResponse> responseObserver) {
        //ban
    }

    private StoreProfileResponse modelToRep(StoreProfile profile,Integer from){
        com.github.conanchen.gedit.store.profile.grpc.StoreProfile grpcStoreProfile = com.github.conanchen.gedit.store.profile.grpc.StoreProfile.newBuilder().build();
        //copy properties
        BeanUtils.copyProperties(profile,grpcStoreProfile);
        com.github.conanchen.gedit.store.profile.grpc.StoreProfile newGrpcStoreProfile= com.github.conanchen.gedit.store.profile.grpc.StoreProfile.newBuilder(grpcStoreProfile)
                .setDesc(profile.getDescr())
                .addAllImages(gson.fromJson(profile.getImages(),listStrType))
                .setLocation(Location.newBuilder().setLat(profile.getLat()).setLon(profile.getLon()).build())
                .setFrom(from++)
                .build();
        StoreProfileResponse response = StoreProfileResponse.newBuilder()
                .setStoreProfile(newGrpcStoreProfile)
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode())).setDetails("success")).build();
        return response;
    }
}

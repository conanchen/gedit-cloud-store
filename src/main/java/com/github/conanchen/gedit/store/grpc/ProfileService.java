package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.store.grpc.client.SearchClient;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.profile.grpc.*;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import com.github.conanchen.gedit.store.repository.page.OffsetBasedPageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.martiansoftware.validation.Hope;
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static io.grpc.Status.Code.*;

@Slf4j
@GRpcService
public class ProfileService extends StoreProfileApiGrpc.StoreProfileApiImplBase {

    private static final Type listStrType = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Gson gson = new GsonBuilder().create();

    private static final String EMPTY_STRING = "";

    @Resource
    private StoreProfileRepository profileRepository;

    @Autowired
    private SearchClient searchClient;

    @Override
    public void create(CreateStoreRequest request, StreamObserver<CreateStoreResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        try {
            responseObserver.onNext(checkCreate(request));
        }catch (UncheckedValidationException e){
            CreateStoreResponse response = CreateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(String.valueOf(INVALID_ARGUMENT.value()))
                            .setDetails(e.getMessage())
                            .build())
                    .build();
            responseObserver.onNext(response);
        }finally {
            responseObserver.onCompleted();
            log.info("store create access success");
        }
    }

    @Override
    public void update(UpdateStoreRequest request, StreamObserver<UpdateStoreResponse> responseObserver) {
        try {
            responseObserver.onNext(checkUpdate(request));
            log.info("store update access success");
        }catch (UncheckedValidationException e){
            UpdateStoreResponse response = UpdateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(String.valueOf(INVALID_ARGUMENT.value()))
                            .setDetails(e.getMessage()).build())
                    .build();
            responseObserver.onNext(response);
        }catch (StatusRuntimeException e){
            io.grpc.Status.Code code = e.getStatus().getCode();
            UpdateStoreResponse response = UpdateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(String.valueOf(code.value()))
                            .setDetails(e.getMessage())
                            .build())
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetStoreRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        try {
            String uuid = Hope.that(request.getUuid()).isNotNullOrEmpty().value();
            StoreProfile profile = (StoreProfile) profileRepository.findOne(uuid);
            responseObserver.onNext(modelToRep(profile,0));
            log.info("store update access success");
        }catch (UncheckedValidationException e){
            StoreProfileResponse response = StoreProfileResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                    .setCode(String.valueOf(INVALID_ARGUMENT.value()))
                    .setDetails(e.getMessage()))
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void list(ListStoreRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        try {
            Integer from = Hope.that(request.getFrom()).named("from").isNotNull()
                    .isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).named("size")
                    .isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
            List<StoreProfile> list;
            if (!StringUtils.isEmpty(request.getType())) {
                list = profileRepository.findByType(request.getType(),pageable);
            }else{
                list = profileRepository.findAll(pageable).getContent();
            }
            for (StoreProfile profile : list){
                responseObserver.onNext(modelToRep(profile, tempForm++));
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }catch (UncheckedValidationException e){
            StoreProfileResponse response = StoreProfileResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(String.valueOf(INVALID_ARGUMENT.value()))
                            .setDetails(e.getMessage()))
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void ban(BanStoreRequest request, StreamObserver<BanStoreResponse> responseObserver) {
        //ban
    }

    private StoreProfileResponse modelToRep(StoreProfile profile,Integer from){
        com.github.conanchen.gedit.store.profile.grpc.StoreProfile grpcStoreProfile = com.github.conanchen.gedit.store.profile.grpc.StoreProfile.newBuilder().build();
        //copy properties
        BeanUtils.copyProperties(profile,grpcStoreProfile);
        com.github.conanchen.gedit.store.profile.grpc.StoreProfile newGrpcStoreProfile= com.github.conanchen.gedit.store.profile.grpc.StoreProfile.newBuilder(grpcStoreProfile)
                .setDesc(Hope.that(profile.getDescr()).orElse("").value())
                .addAllImages((Iterable<String>) Hope
                        .that(gson.fromJson(profile.getImages(),listStrType))
                        .orElse(Collections.EMPTY_LIST)
                        .value())
                .setLocation(Location.newBuilder()
                        .setLat(Hope.that(profile.getLat()).orElse(0.0D).value())
                        .setLon(Hope.that(profile.getLon()).orElse(0.0D).value())
                        .build())
                .setFrom(from)
                .build();
        StoreProfileResponse response = StoreProfileResponse.newBuilder()
                .setStoreProfile(newGrpcStoreProfile)
                .setStatus(Status.newBuilder()
                        .setCode(String.valueOf(OK.value()))
                        .setDetails("success"))
                .build();
        return response;
    }

    private CreateStoreResponse checkCreate(CreateStoreRequest req) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(req)));
        //common check
        createCheck(req.getName(), req.getDetailAddress(), req.getDistrictUuid(), req.getLocation());
        Date now = new Date();
        StoreProfile storeProfile = StoreProfile.builder()
                .ownerId(claims.getSubject())
                .active(false) //默认 false
                .detailAddress(req.getDetailAddress())
                .districtUuid(req.getDistrictUuid())
                .name(req.getName())
                .lat(req.getLocation().getLat())
                .lon(req.getLocation().getLon())
                .createdDate(now)
                .updatedDate(now)
                .build();
        StoreProfile profile = (StoreProfile)profileRepository.save(storeProfile);
        searchClient.index(profile);
        return CreateStoreResponse.newBuilder()
                .setName(storeProfile.getName())
                .setUuid(storeProfile.getUuid())
                .setOwnerUuid(storeProfile.getOwnerId())
                .setStatus(Status.newBuilder().setCode(String.valueOf(OK.value())).setDetails("新增成功").build())
                .build();
    }

    private UpdateStoreResponse checkUpdate(UpdateStoreRequest req){
        //uuid
        String uuid = Hope.that(req.getUuid()).named("uuid").isNotNullOrEmpty().value();
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(req)));
        StoreProfile storeProfile = (StoreProfile) profileRepository.findOne(uuid);
        if (!storeProfile.getOwnerId().equals(claims.getSubject())){
            log.info("user [{}] not owned the store [{}]",claims.getSubject(),req.getUuid());
            UpdateStoreResponse response = UpdateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(String.valueOf(PERMISSION_DENIED.value()))
                            .setDetails("permission_denied"))
                    .build();
            return response;
        }
        StoreProfile profile = (StoreProfile) profileRepository.findOne(req.getUuid());
        switch (req.getPropertyCase()){
            case DESC:
                String desc = Hope.that(req.getDesc())
                        .isTrue(n -> n.length() <= 255,"描述小于%s字",255)
                        .value();
                profile.setDescr(desc);
                break;
            case LOGO:
                String logo = Hope.that(req.getLogo())
                        .isTrue(n -> n.length() <= 255,"logo小于%s字",255)
                        .value();
                profile.setLogo(logo);
                break;
            case TYPE:
                String type = Hope.that(req.getType())
                        .isTrue(n -> n.length() <= 16,"商户类型小于%s字",16)
                        .value();
                profile.setLogo(type);
                break;
            case ACTIVE:
                profile.setActive(req.getActive());
                break;
            case NAME:
                String name = Hope.that(req.getName())
                        .isTrue(n -> n.length() <= 64,"商户名称小于%s字",64)
                        .value();
                profile.setName(name);
                break;
            case IMAGES:
                String iamges = Hope.that(req.getImages().toString())
                        .isTrue(n -> n.length() <= 4096,"图片介绍小于%s字",4096)
                        .value();
                profile.setImages(iamges);
                break;
            case LOCATION:
                checkLocation(req.getLocation());
                profile.setLat(req.getLocation().getLat());
                profile.setLon(req.getLocation().getLon());
                break;
            case POINTSRATE:
                Integer pointRate  = Hope.that(req.getPointsRate())
                        .isTrue(n -> n <= 100,"积分比例小%%s",100)
                        .value();
                profile.setPointsRate(Double.valueOf(pointRate));
                break;
            case DISTRICTUUID:
                profile.setDistrictUuid(req.getDistrictUuid());
                break;
            case DETAILADDRESS:
                String detailAddress = Hope.that(req.getDetailAddress())
                        .isTrue(n -> n.length() <= 512,"详细地址不的超过%s字",512)
                        .value();
                profile.setDetailAddress(detailAddress);
                break;
            case PROPERTY_NOT_SET:
                throw new UncheckedValidationException("property not set");
        }
        profile.setUpdatedDate(new Date());
        profileRepository.save(profile);
        searchClient.index(profile);
        return UpdateStoreResponse.newBuilder()
                .setUuid(profile.getUuid())
                .setLastUpdated(profile.getUpdatedDate().getTime())
                .setStatus(Status.newBuilder().setCode(String.valueOf(OK.value())).setDetails("更新成功").build())
                .build();
    }

    private void checkBan(BanStoreRequest req) {
        //supper user check
    }


    private void createCheck(String name,String detailAddress, String districtId,Location location){

        //name 长度限制
        Hope.that(name).named("name").isNotNullOrEmpty()
                .isTrue(n -> n.length() <= 64,"商户名称不能超过%s个字",64);
        //detailAddresss
        Hope.that(detailAddress).orElse(EMPTY_STRING).isNotNullOrEmpty()
                .isTrue(n -> n.length() <= 512,"详细地址不能超过%s个字",512);
        //districtUuid
        Hope.that(districtId).orElse(EMPTY_STRING).isNotNullOrEmpty()
                .isTrue(n -> n.length() <= 6,"地区码为%s位数字,如有必要请联系工程师",6);
        //location
        checkLocation(location);
    }

    private void checkLocation(Location location){
        if ((location.getLat() != 0.0D && location.getLon() == 0.0D) || (location.getLat() == 0.0D && location.getLon() != 0.0D)){
            // ignore
            throw new UncheckedValidationException("location may lat or lon not exists");
        }
    }
}

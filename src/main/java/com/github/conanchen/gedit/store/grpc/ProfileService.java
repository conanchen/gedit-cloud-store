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
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static io.grpc.Status.ALREADY_EXISTS;
import static io.grpc.Status.INVALID_ARGUMENT;

@Slf4j
@GRpcService(applyGlobalInterceptors = false)
public class ProfileService extends StoreProfileApiGrpc.StoreProfileApiImplBase {

    private static final Type listStrType = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();

    private static final String EMPTY_STRING = "";

    private static final Pattern URL_REGEX = Pattern
            .compile(
                    "(?i)^([a-z](?:[-a-z0-9\\+\\.])*)" + // protocol
                            ":(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?" + // auth
                            "((?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*))" + // host/ip
                            "(?::([0-9]*))?" + // port
                            "(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}|\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?$"
            );

    @Resource
    private StoreProfileRepository profileRepository;

    @Override
    public void create(CreateStoreRequest request, StreamObserver<CreateStoreResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        try {
            responseObserver.onNext(checkCreate(request));
        }catch (Exception e){
            CreateStoreResponse response = CreateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder().setCode(String.valueOf(INVALID_ARGUMENT.getCode().value())).setDetails(e.getMessage()).build())
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
        }catch (UncheckedValidationException e){
            UpdateStoreResponse response = UpdateStoreResponse.newBuilder()
                    .setStatus(Status.newBuilder().setCode(String.valueOf(INVALID_ARGUMENT.getCode().value())).setDetails(e.getMessage()).build())
                    .build();
            responseObserver.onNext(response);
        }finally {

            responseObserver.onCompleted();
            log.info("store update access success");
        }
    }

    @Override
    public void get(GetStoreRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));

        responseObserver.onCompleted();
        try {
            String uuid = Hope.that(request.getUuid()).isNotNullOrEmpty().value();
            StoreProfile profile = (StoreProfile) profileRepository.findOne(uuid);
            responseObserver.onNext(modelToRep(profile,0));
        }catch (UncheckedValidationException e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }finally {
            responseObserver.onCompleted();
            log.info("store update access success");
        }
        log.info("store get access success");
    }

    @Override
    public void list(ListRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        try {
            Integer from = Hope.that(request.getFrom()).isNotNull().isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
            List<StoreProfile> list = null;
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
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }finally {
            responseObserver.onCompleted();
        }



    }

    @Override
    public void findByName(FindByNameRequest request, StreamObserver<StoreProfileResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        try {
            String name = Hope.that(request.getName()).isNotNullOrEmpty().value();
            StoreProfile profile = profileRepository.findByName(name);
            responseObserver.onNext(modelToRep(profile, 0));
        }catch (UncheckedValidationException e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }finally {
            responseObserver.onCompleted();
        }
        log.info("store get access success");
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
                .addAllImages((Iterable<String>) Hope.that(gson.fromJson(profile.getImages(),listStrType)).orElse(Collections.EMPTY_LIST).value())
                .setLocation(Location.newBuilder().setLat(Hope.that(profile.getLat()).orElse(0.0D).value()).setLon(Hope.that(profile.getLon()).orElse(0.0D).value()).build())
                .setFrom(from)
                .build();
        StoreProfileResponse response = StoreProfileResponse.newBuilder()
                .setStoreProfile(newGrpcStoreProfile)
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode().value())).setDetails("success")).build();
        return response;
    }

    private CreateStoreResponse checkCreate(CreateStoreRequest req) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(req)));
        //common check
        createOrUpdateCommonCheck(req.getName(), req.getDetailAddress(), req.getDistrictUuid(), req.getLocation());
        if (profileRepository.findByName(req.getName()) != null) {
            throw new StatusRuntimeException(ALREADY_EXISTS.withDescription("商户名已存在"));
        }
        StoreProfile storeProfile = StoreProfile.builder()
                .ownerId(claims.getSubject())
                .active(true)
                .detailAddress(req.getDetailAddress())
                .districtUuid(req.getDistrictUuid())
                .name(req.getName())
                .lat(req.getLocation().getLat())
                .lon(req.getLocation().getLon())
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();
        profileRepository.save(storeProfile);

        return CreateStoreResponse.newBuilder()
                .setName(storeProfile.getName())
                .setUuid(storeProfile.getUuid())
                .setOwnerId(storeProfile.getOwnerId())
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode().value())).setDetails("新增成功").build())
                .build();
    }

    private UpdateStoreResponse checkUpdate(UpdateStoreRequest req){
        //uuid
        Hope.that(req.getUuid()).isNotNullOrEmpty();
        //logo
        Hope.that(req.getLogo()).isTrue(n -> n == null || URL_REGEX.matcher(n).matches()).orElse(EMPTY_STRING)
                .isTrue(n -> n.length() <= 255,"logo不能超过%s个字,如有必要请联系工程师",255);
        //common check
        createOrUpdateCommonCheck(req.getName(),req.getDetailAddress(),req.getDistrictUuid(),req.getLocation());

        //check th current user is the store owner
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(req)));
        StoreProfile storeProfile = (StoreProfile) profileRepository.findOne(claims.getSubject());
        if (!storeProfile.getOwnerId().equals(req.getUuid())){
            log.info("user [{}] not owned the store [{}]",claims.getSubject(),req.getUuid());
            throw new UncheckedValidationException("No authority");
        }
        //check store name reuse
        boolean exist = profileRepository.existsByNameAndOwnerIdNotIn(req.getName(),claims.getSubject());
        if (exist){
            throw new StatusRuntimeException(ALREADY_EXISTS.withDescription("商户名已存在"));
        }
        StoreProfile profile = (StoreProfile) profileRepository.findOne(req.getUuid());
        BeanUtils.copyProperties(req,profile);
        profile.setDescr(req.getDesc());
        profile.setLon(req.getLocation().getLon());
        profile.setLat(req.getLocation().getLat());
        profile.setImages(req.getImages().toString());
        profile.setUpdatedDate(new Date());
        profileRepository.save(profile);

        return UpdateStoreResponse.newBuilder()
                .setUuid(profile.getUuid())
                .setLastUpdated(profile.getUpdatedDate().getTime())
                .setStatus(Status.newBuilder().setCode(String.valueOf(io.grpc.Status.OK.getCode().value())).setDetails("更新成功").build())
                .build();
    }

    private void checkBan(BanStoreRequest req) {
        //supper user check
    }


    private void createOrUpdateCommonCheck(String name,String detailAddress, String districtId,Location location){

        //name 长度限制
        Hope.that(name).isNotNullOrEmpty()
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

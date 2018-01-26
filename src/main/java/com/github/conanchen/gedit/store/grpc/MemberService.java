package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.member.grpc.*;
import com.github.conanchen.gedit.store.model.StoreMember;
import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.repository.StoreMemberRepository;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import com.github.conanchen.gedit.store.repository.page.OffsetBasedPageRequest;
import com.github.conanchen.gedit.store.utils.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.martiansoftware.validation.Hope;
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@GRpcService
public class MemberService extends StoreMemberApiGrpc.StoreMemberApiImplBase {
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private StoreMemberRepository memberRepository;
    @Autowired
    private StoreProfileRepository profileRepository;

    @Override
    public void add(AddMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        try {
            String storeUuid = Hope.that(request.getStoreUuid())
                    .named("storeUuid")
                    .isNotNullOrEmpty()
                    .value();
            String memeberUuid = Hope.that(request.getUserUuid())
                    .named("memberUuid")
                    .isNotNullOrEmpty()
                    .value();
            //check:the current user is the owner of this store,need?
            Claims claims = AuthInterceptor.USER_CLAIMS.get();
            log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
            Date date = new Date();
            StoreMember member = StoreMember.builder()
                    .memberUuid(memeberUuid)
                    .active(true)
                    .storeUuid(storeUuid)
                    .createdDate(date)
                    .updatedDate(date)
                    .build();
            memberRepository.save(member);
            MembershipResponse response = MembershipResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(Status.Code.OK)
                            .setDetails("新增成功").build())
                    .build();
            responseObserver.onNext(response);
        }catch (UncheckedValidationException e){
            MembershipResponse response = MembershipResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(Status.Code.INVALID_ARGUMENT)
                            .setDetails(e.getMessage()).build())
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        try {
            Claims claims = AuthInterceptor.USER_CLAIMS.get();
            log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
            String storeUuid = Hope.that(request.getStoreUuid())
                    .named("storeUuid")
                    .isNotNullOrEmpty()
                    .value();
            String memeberUuid = Hope.that(request.getUserUuid())
                    .named("memberUuid")
                    .isNotNullOrEmpty()
                    .value();
            StoreMember storeMember = memberRepository.findByStoreUuidAndMemberUuid(storeUuid,memeberUuid);
            Status status;
            if (storeMember == null){
                status = Status.newBuilder()
                        .setCode(Status.Code.FAILED_PRECONDITION)
                        .setDetails("关系不存在")
                        .build();
            }else{
                //check:the current user is the owner of this store,need?
                switch (request.getPropertyCase()){
                    case ENDTIME:
                        long endTime = Hope.that(request.getEndTime()).named("endTime")
                                .isNotEqualTo(0L)
                                .value();
                        storeMember.setEndTime(new Date(endTime));
                        break;
                    case STARTTIME:
                        long startTime = Hope.that(request.getStartTime()).named("endTime")
                                .isNotEqualTo(0L)
                                .value();
                        storeMember.setStartTime(new Date(startTime));
                        break;
                    case MEMBERTYPE:
                        String memberType = Hope.that(request.getMemberType())
                                .named("memberType")
                                .isNotNullOrEmpty()
                                .isTrue(n -> n.length() <= 16 ,"会员类型不能超过%s个字",16)
                                .value();
                        storeMember.setMemberType(memberType);
                    case PROPERTY_NOT_SET:
                        throw new UncheckedValidationException("property not set");
                }
                storeMember.setUpdatedDate(new Date());
                memberRepository.save(storeMember);
                status = Status.newBuilder()
                        .setCode(Status.Code.INVALID_ARGUMENT)
                        .setDetails("新增成功")
                        .build();
            }

            MembershipResponse response = MembershipResponse.newBuilder()
                    .setStatus(status)
                    .build();
            responseObserver.onNext(response);
        }catch (UncheckedValidationException e){
            MembershipResponse response = MembershipResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(Status.Code.INVALID_ARGUMENT)
                            .setDetails(e.getMessage()).build())
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();

    }

    @Override
    public void listByStore(ListMembershipByStoreRequest request, StreamObserver<MembershipResponse> responseObserver) {
        try {
            String storeUuid = Hope.that(request.getStoreUuid())
                    .named("storeUuid")
                    .isNotNullOrEmpty()
                    .value();
            Integer from = Hope.that(request.getFrom()).named("from").isNotNull()
                    .isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).named("size")
                    .isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size);
            List<StoreMember> list = memberRepository.findByStoreUuid(storeUuid,pageable);
            if (!CollectionUtils.isEmpty(list)){
                StoreProfile  storeProfile = (StoreProfile)profileRepository.findOne(storeUuid);
                for (StoreMember member : list){
                    responseObserver.onNext(modelToRep(member,storeProfile, tempForm++));
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
            log.info("store get access success");
        }catch (UncheckedValidationException e){
            MembershipResponse response = MembershipResponse.newBuilder().setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage()))
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listByMember(ListMembershipByMemberRequest request, StreamObserver<MembershipResponse> responseObserver) {

        try {
            Claims claims = AuthInterceptor.USER_CLAIMS.get();
            log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
            Integer from = Hope.that(request.getFrom()).named("from").isNotNull()
                    .isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).named("size")
                    .isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size);
            List<StoreMember> list = memberRepository.findByMemberUuid(claims.getSubject(),pageable);
            if (!CollectionUtils.isEmpty(list)){
                List<String> storeUuids = EntityUtils.createFieldList(list,"storeUuid");
                List<StoreProfile> storeProfiles = profileRepository.findAll(storeUuids);
                Map<String,StoreProfile> storeProfileMap = EntityUtils.createEntityMapByString(storeProfiles,"uuid");
                for (StoreMember member : list){
                    responseObserver.onNext(modelToRep(member,storeProfileMap.get(member.getStoreUuid()), tempForm++));
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
            log.info("store get access success");
        }catch (UncheckedValidationException e){
            MembershipResponse response = MembershipResponse.newBuilder().setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage()))
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void ban(BanMembershipRequest request, StreamObserver<BanMembershipResponse> responseObserver) {

    }

    @Override
    public void listMyMemberStore(ListMyMemberStoreRequest request, StreamObserver<MembershipResponse> responseObserver) {
        Claims claims = AuthInterceptor.USER_CLAIMS.get();
        log.info(String.format("user [%s], request [%s]", claims.getSubject(), gson.toJson(request)));
        List<StoreProfile> storeProfiles = profileRepository.findByOwnerUuid(claims.getSubject());
        if (!CollectionUtils.isEmpty(storeProfiles)){
            /*List<String> storeUuids = EntityUtils.createFieldList(storeProfiles,"uuid");
            if (request.getLastUpdated() == 0L){
                List<StoreMember> members = memberRepository.findAllByStoreUuid(storeUuids);
            }else{

            }*/
        }
        responseObserver.onCompleted();

    }

    private MembershipResponse modelToRep(StoreMember member,StoreProfile profile, Integer from){
        MembershipResponse response = MembershipResponse.newBuilder()
                .setFrom(from)
                .setMembership(Membership.newBuilder()
                        .setActive(member.getActive())
                        .setCreated(member.getCreatedDate().getTime())
                        .setLastUpdated(member.getUpdatedDate().getTime())
                        .setMemberType(member.getMemberType())
                        .setStoreLogo(profile.getLogo())
                        .setLocation(Location.newBuilder()
                                .setLat(profile.getLon() == null ? 0.0D : profile.getLon())
                                .setLat(profile.getLat() == null ? 0.0D : profile.getLat())
                                .build())
                        .build())
                .build();
        return response;
    }
}

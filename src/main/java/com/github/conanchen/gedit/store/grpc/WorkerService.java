package com.github.conanchen.gedit.store.grpc;

import com.github.conanchen.gedit.common.grpc.Location;
import com.github.conanchen.gedit.common.grpc.Status;
import com.github.conanchen.gedit.store.grpc.interceptor.AuthInterceptor;
import com.github.conanchen.gedit.store.model.StoreProfile;
import com.github.conanchen.gedit.store.model.StoreWorker;
import com.github.conanchen.gedit.store.repository.StoreProfileRepository;
import com.github.conanchen.gedit.store.repository.StoreWorkerRepository;
import com.github.conanchen.gedit.store.repository.page.OffsetBasedPageRequest;
import com.github.conanchen.gedit.store.utils.EntityUtils;
import com.github.conanchen.gedit.store.worker.grpc.*;
import com.martiansoftware.validation.Hope;
import com.martiansoftware.validation.UncheckedValidationException;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@GRpcService
public class WorkerService extends StoreWorkerApiGrpc.StoreWorkerApiImplBase {
    @Resource
    private StoreWorkerRepository workerRepository;
    @Resource
    private StoreProfileRepository profileRepository;
    @Override
    public void add(AddWorkershipRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        WorkshipResponse.Builder builder;
        try{
            String workerUuid = Hope.that(request.getWorkerUuid()).named("workerUuid").isNotNullOrEmpty().value();
            String storeUuid = Hope.that(request.getStoreUuid()).named("storeUuid").isNotNullOrEmpty().value();
            StoreProfile storeProfile = (StoreProfile)profileRepository.findOne(storeUuid);
            //查询在激活
            Optional<StoreWorker> optionalWorker = workerRepository.findByWorkerUuidAndActiveIsTrue(workerUuid);
            Date date = new Date();
            if (optionalWorker.isPresent()){
                StoreWorker worker  =  optionalWorker.get();
                worker.setActive(false);
                worker.setUpdatedDate(date);
                workerRepository.save(worker);
            }
            StoreWorker worker = StoreWorker.builder()
                    .workerUuid(workerUuid)
                    .storeUuid(storeUuid)
                    .active(true)
                    .createdDate(date)
                    .updatedDate(date)
                    .build();
            StoreWorker newWorker = (StoreWorker)workerRepository.save(worker);
            builder = modelToResult(storeProfile,newWorker,0,"新增成功");
        }catch (UncheckedValidationException e){
            builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public void listByStore(ListWorkshipByStoreRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        try{
            String storeUuid = Hope.that(request.getStoreUuid()).named("storeUuid").isNotNullOrEmpty().value();
            Integer from = Hope.that(request.getFrom()).named("from").isNotNull()
                    .isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).named("size")
                    .isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
            StoreProfile storeProfile = (StoreProfile)profileRepository.findOne(storeUuid);
            List<StoreWorker> workerList = workerRepository.findByStoreUuid(storeUuid,pageable);
            for (StoreWorker worker : workerList){
                responseObserver.onNext(modelToResult(storeProfile,worker,tempForm++,"success").build());
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }catch (UncheckedValidationException e){
            WorkshipResponse.Builder builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listByWorker(ListWorkshipByWorkerRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        try{

            String workerUuid = Hope.that(request.getWorkerUuid()).named("workerUuid").isNotNullOrEmpty().value();
            Integer from = Hope.that(request.getFrom()).named("from").isNotNull()
                    .isTrue(n -> n >= 0,"from must be greater than or equals：%s",0).value();
            Integer size = Hope.that(request.getSize()).named("size")
                    .isNotNull().isTrue(n -> n > 0,"size must be greater than %s",0).value();
            int tempForm = from == 0 ? 0 : from + 1;
            Pageable pageable = new OffsetBasedPageRequest(tempForm,size,new Sort(Sort.Direction.ASC,"createdDate"));
            List<StoreWorker> workerList = workerRepository.findByWorkerUuid(workerUuid,pageable);
            List<String> storeUuids = EntityUtils.createFieldList(workerList,"storeUuid");
            List<StoreProfile> storeProfileList = profileRepository.findAll(storeUuids);
            Map<String,StoreProfile> storeProfileMap = EntityUtils.createEntityMapByString(storeProfileList,"uuid");
            for (StoreWorker worker : workerList){
                responseObserver.onNext(modelToResult(storeProfileMap.get(worker.getStoreUuid()),worker,tempForm++,"success").build());
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }catch (UncheckedValidationException e){
            WorkshipResponse.Builder builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void ban(BanWorkshipRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        WorkshipResponse.Builder builder;
        try {
            String workerUuid = Hope.that(request.getWorkerUuid()).named("workerUuid").isNotNullOrEmpty().value();
            String storeUuid = Hope.that(request.getStoreUuid()).named("storeUuid").isNotNullOrEmpty().value();
            Optional<StoreWorker> worker = workerRepository.findByStoreUuidAndWorkerUuidAndActiveIsTrue(storeUuid,workerUuid);
            if (worker.isPresent()){
                builder =  WorkshipResponse.newBuilder();
                builder.setStatus(Status.newBuilder()
                        .setCode(Status.Code.FAILED_PRECONDITION)
                        .setDetails("关系不存在")
                        .build());
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }
            StoreProfile storeProfile = (StoreProfile)profileRepository.findOne(storeUuid);
            builder = modelToResult(storeProfile,worker.get(),0,"禁用成功");
        }catch (UncheckedValidationException e){
            builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listMyWorkinStore(ListMyWorkinStoreRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        try{
            Claims claims = AuthInterceptor.USER_CLAIMS.get();
            String workerUuid = claims.getSubject();
            List<StoreWorker> workerList = null;
            if (request.getLastUpdated() == 0L){
                workerList = workerRepository.findByWorkerUuid(workerUuid);
            }else{
                workerList = workerRepository.findByWorkerUuidAndCreatedDateGreaterThan(workerUuid,new Date(request.getLastUpdated()));
            }
            List<String> storeUuids = EntityUtils.createFieldList(workerList,"storeUuid");
            List<StoreProfile> storeProfileList = profileRepository.findAll(storeUuids);
            Map<String,StoreProfile> storeProfileMap = EntityUtils.createEntityMapByString(storeProfileList,"uuid");
            int index = 0;
            for (StoreWorker worker : workerList){
                responseObserver.onNext(modelToResult(storeProfileMap.get(worker.getStoreUuid()),worker,index++,"success").build());
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }catch (UncheckedValidationException e){
            WorkshipResponse.Builder builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();

    }

    @Override
    public void getMyCurrentWorkinStore(GetMyCurrentWorkinStoreRequest request, StreamObserver<WorkshipResponse> responseObserver) {
        WorkshipResponse.Builder builder;
        try{
            Claims claims = AuthInterceptor.USER_CLAIMS.get();
            String workerUuid = claims.getSubject();
            Optional<StoreWorker> worker = workerRepository.findByWorkerUuidAndActiveIsTrue(workerUuid);
            if (worker.isPresent()){
                Optional<StoreProfile> storeProfile = (Optional<StoreProfile>) profileRepository.findOne(worker.get().getStoreUuid());
                if (storeProfile.isPresent()) {
                    builder = modelToResult(storeProfile.get(), worker.get(), 0, "success");
                }else{
                    builder =  WorkshipResponse.newBuilder();
                    builder.setStatus(Status.newBuilder()
                            .setCode(Status.Code.NOT_FOUND)
                            .setDetails("店铺不存在")
                            .build());
                }
            }else{
                builder =  WorkshipResponse.newBuilder();
                builder.setStatus(Status.newBuilder()
                        .setCode(Status.Code.NOT_FOUND)
                        .setDetails("目前没有工作的店铺")
                        .build());
            }
        }catch (UncheckedValidationException e){
            builder =  WorkshipResponse.newBuilder();
            builder.setStatus(Status.newBuilder()
                    .setCode(Status.Code.INVALID_ARGUMENT)
                    .setDetails(e.getMessage())
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private WorkshipResponse.Builder modelToResult(StoreProfile storeProfile,StoreWorker worker,int from,String details){
        WorkshipResponse.Builder builder =  WorkshipResponse.newBuilder();
        return builder.setStatus(Status.newBuilder()
                .setCode(Status.Code.OK)
                .setDetails(details)
                .build())
                .setFrom(from)
                .setOwnership(Workship.newBuilder()
                        .setActive(true)
                        .setCreated(worker.getCreatedDate().getTime())
                        .setLastUpdated(worker.getUpdatedDate().getTime())
                        .setLocation(Location.newBuilder()
                                .setLat(storeProfile.getLat())
                                .setLon(storeProfile.getLon())
                                .build())
                        .setStoreLogo(storeProfile.getLogo())
                        .setStoreName(storeProfile.getName())
                        .setUserUuid(worker.getWorkerUuid())
                        .setStoreUuid(worker.getStoreUuid())
                        .setUuid(worker.getUuid())
                        .build());
    }
}

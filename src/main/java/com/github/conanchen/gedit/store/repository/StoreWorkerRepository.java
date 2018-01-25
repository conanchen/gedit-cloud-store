package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreWorker;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StoreWorkerRepository<T,String extends Serializable> extends JpaRepository<StoreWorker,String> {
    List<StoreWorker> findByStoreUuid(String storeUuid, Pageable pageable);
    List<StoreWorker> findByWorkerUuid(String workerUuid, Pageable pageable);
    List<StoreWorker> findByWorkerUuidAndCreatedDateGreaterThan(String workerUuid, Date lastUpdateDate);
    List<StoreWorker> findByWorkerUuid(String workerUuid);

    Optional<StoreWorker> findByStoreUuidAndWorkerUuidAndActiveIsTrue(String storeUuid,String workerUuid);

    Optional<StoreWorker> findByWorkerUuidAndActiveIsTrue(String workerUuid);


}

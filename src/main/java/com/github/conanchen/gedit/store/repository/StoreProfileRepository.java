package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreProfile;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface StoreProfileRepository<T,String extends Serializable> extends JpaRepository<StoreProfile,String> {
    List<StoreProfile> findByName(String name, Pageable request);

    List<StoreProfile> findByType(String type, Pageable request);

    boolean existsByNameAndOwnerIdNotIn(String name,String ownerId);

    List<StoreProfile> findByOwnerUuid(String ownerUuid);
}

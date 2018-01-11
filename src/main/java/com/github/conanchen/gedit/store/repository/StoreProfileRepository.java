package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreProfile;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface StoreProfileRepository<T,String extends Serializable> extends JpaRepository<StoreProfile,String> {
    StoreProfile findByName(String name);

    List<StoreProfile> findByType(String type, Pageable request);
}
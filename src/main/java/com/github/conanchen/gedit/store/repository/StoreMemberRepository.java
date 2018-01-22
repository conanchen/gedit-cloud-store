package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface StoreMemberRepository<T,String extends Serializable> extends JpaRepository<StoreMember,String>{
    StoreMember  findByStoreUuidAndMemberUuid(String storeUuid,String memberUuid);

    List<StoreMember> findByStoreUuid(String storeUuid, Pageable request);

    List<StoreMember> findByMemberUuid(String memberUuid, Pageable request);

    List<StoreMember> findAllByStoreUuidAndCreateDate(List<String> storeUuid);
}

package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

public interface StoreMemberRepository<T,String extends Serializable> extends JpaRepository<StoreMember,String>{

}

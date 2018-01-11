package com.github.conanchen.gedit.store.repository;

import com.github.conanchen.gedit.store.model.StoreWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public interface StoreWorkerRepository<T,String extends Serializable> extends JpaRepository<StoreWorker,String> {
}

package com.yumyum.sns.infra.repository;

import com.yumyum.sns.infra.entity.StorageDeleteOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageDeleteOutboxRepository extends JpaRepository<StorageDeleteOutbox,Long> {

    List<StorageDeleteOutbox> findByStatus(StorageDeleteOutbox.OutboxStatus status);
}

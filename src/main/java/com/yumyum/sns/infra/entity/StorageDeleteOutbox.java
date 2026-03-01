package com.yumyum.sns.infra.entity;

import com.yumyum.sns.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StorageDeleteOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private int retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    public enum OutboxStatus {
        PENDING, FAILED
    }

    public StorageDeleteOutbox(String fileName) {
        this.fileName = fileName;
        this.status = OutboxStatus.PENDING;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        if(this.retryCount >= 3) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
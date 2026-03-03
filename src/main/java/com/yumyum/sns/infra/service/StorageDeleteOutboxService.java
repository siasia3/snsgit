package com.yumyum.sns.infra.service;

import com.yumyum.sns.infra.entity.StorageDeleteOutbox;
import com.yumyum.sns.infra.repository.StorageDeleteOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class StorageDeleteOutboxService {

    private final StorageDeleteOutboxRepository outboxRepository;

    // 실패한 파일 저장
    @Transactional
    public void save(String fileName) {
        try {
            outboxRepository.save(new StorageDeleteOutbox(fileName));
        } catch (Exception e) {
            log.error("[CRITICAL] Outbox 저장 실패. 수동 처리 필요. fileName={}", fileName, e);
        }
    }

    // PENDING 목록 조회
    @Transactional(readOnly = true)
    public List<StorageDeleteOutbox> getPendingList() {
        return outboxRepository.findByStatus(StorageDeleteOutbox.OutboxStatus.PENDING);
    }

    //outbox 삭제
    @Transactional
    public void delete(StorageDeleteOutbox outbox) {
        outboxRepository.delete(outbox);
    }

    //재시도 실패횟수 증가
    @Transactional
    public void handleRetryFailed(Long outboxId) {
        StorageDeleteOutbox findOutbox = outboxRepository.findById(outboxId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 outbox: " + outboxId));
        findOutbox.incrementRetryCount();
    }
}

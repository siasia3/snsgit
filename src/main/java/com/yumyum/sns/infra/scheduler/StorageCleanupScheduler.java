package com.yumyum.sns.infra.scheduler;


import com.yumyum.sns.infra.StorageService;
import com.yumyum.sns.infra.entity.StorageDeleteOutbox;
import com.yumyum.sns.infra.service.StorageDeleteOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageCleanupScheduler {

    private final StorageDeleteOutboxService outboxService;
    private final StorageService storageService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void cleanOrphanFiles() {
        List<StorageDeleteOutbox> pendingList = outboxService.getPendingList();

        for (StorageDeleteOutbox outbox : pendingList) {
            boolean success = storageService.deleteFile(outbox.getFileName());

            if (success) {
                outboxService.delete(outbox); // 성공시 삭제
            } else {
                log.error("고아 파일 재시도 실패: " + outbox.getFileName());
                outboxService.handleRetryFailed(outbox.getId()); // 실패시 count 올려줌
            }
        }
    }
}

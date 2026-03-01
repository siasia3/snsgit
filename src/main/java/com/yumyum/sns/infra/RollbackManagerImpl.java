package com.yumyum.sns.infra;

import com.yumyum.sns.infra.service.StorageDeleteOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RollbackManagerImpl implements RollbackManager{

    private final StorageService storageService;
    private final StorageDeleteOutboxService storageDeleteOutboxService;

    @Override
    public void deleteIfTransactionRollback(List<String> fileNames) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            for (String fileName : fileNames) {
                                if (!storageService.deleteFile(fileName)) {
                                    storageDeleteOutboxService.save(fileName); // 여기
                                }
                            }
                        }
                    }
                }
        );
    }
}

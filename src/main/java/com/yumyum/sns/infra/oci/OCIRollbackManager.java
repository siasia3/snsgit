package com.yumyum.sns.infra.oci;

import com.yumyum.sns.infra.RollbackManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OCIRollbackManager implements RollbackManager {

    private final OciStorageService ociStorageService;

    //트랜잭션 롤백시 업로드 된 OCI 파일 삭제
    @Override
    public void deleteIfTransactionRollback(List<String> fileNames){
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            ociStorageService.deleteFiles(fileNames);
                        }
                    }
                }
        );
    }
}

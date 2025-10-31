package com.yumyum.sns.infra;

import java.util.List;

public interface RollbackManager {

    //트랜잭션 롤백시 업로드 된 파일 삭제;
    void deleteIfTransactionRollback(List<String> fileNames);
}

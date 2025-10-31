package com.yumyum.sns.infra.oci;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

@SpringBootTest
class OciS3StorageServiceTest {


    @Autowired
    private OciStorageService ociStorageService;


    @Value("${oci.bucket.namespace}")
    private String namespace;

    @Value("${oci.bucket.name}")
    private String bucketName;

    @Test
    void testUploadFile() throws Exception {
        // 1️⃣ 테스트용 파일 생성 (메모리에서)
        String fileName = "test-file.txt";
        String content = "OCI Object Storage upload test!";
        InputStream inputStream = new java.io.ByteArrayInputStream(content.getBytes());
        long size = content.getBytes().length;

        // 2️⃣ 업로드 호출
        //ociStorageService.uploadFile(fileName, inputStream, size);

        // 3️⃣ 콘솔 확인용 출력
        System.out.println("테스트 업로드 완료: " + fileName);
    }

    @Test
    void testDeleteFile() throws Exception {
        // 1️⃣ 삭제 대상 파일명
        String fileName = "test-file.txt"; // 방금 업로드한 파일 이름

        // 2️⃣ 삭제 호출
        ociStorageService.deleteFile(fileName);

        System.out.println("🗑️ 삭제 완료: " + fileName);
    }
}
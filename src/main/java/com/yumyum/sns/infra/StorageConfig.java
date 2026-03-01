package com.yumyum.sns.infra;

import com.yumyum.sns.infra.oci.OciStorageService;
import com.yumyum.sns.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    @Value("${cloud.provider}")
    private String cloudProvider;

    private final S3StorageService s3StorageService;
    private final OciStorageService ociStorageService;

    @Bean
    public StorageService storageService() {
        if ("oci".equalsIgnoreCase(cloudProvider)) {
            return ociStorageService;
        }else if("s3".equalsIgnoreCase(cloudProvider)){
            return s3StorageService;
        }else{
            //오타나 설정 누락의 경우
            throw new IllegalStateException("Unsupported cloud provider: " + cloudProvider);
        }
    }
}

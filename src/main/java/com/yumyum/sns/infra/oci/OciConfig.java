package com.yumyum.sns.infra.oci;


import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class OciConfig {
    @Value("${oci.config.user}")
    private String user;

    @Value("${oci.config.fingerprint}")
    private String fingerprint;

    @Value("${oci.config.tenancy}")
    private String tenancy;

    @Value("${oci.config.region}")
    private String region;

    @Value("${oci.config.key-file}")
    private String keyFilePath;

    @Bean
    public ObjectStorageClient objectStorageClient(){
        SimpleAuthenticationDetailsProvider provider =
                SimpleAuthenticationDetailsProvider.builder()
                        .userId(user)
                        .fingerprint(fingerprint)
                        .tenantId(tenancy)
                        .region(Region.fromRegionId(region))
                        .privateKeySupplier(() -> {
                            try {
                                return new FileInputStream(keyFilePath.replace("classpath:", "src/main/resources/"));
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to load OCI private key", e);
                            }
                        })
                        .build();

        return ObjectStorageClient.builder()
                .build(provider);
    }
}

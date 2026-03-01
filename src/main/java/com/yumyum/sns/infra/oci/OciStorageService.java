package com.yumyum.sns.infra.oci;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.error.exception.FileUploadException;
import com.yumyum.sns.error.exception.OCIDeleteException;
import com.yumyum.sns.error.exception.OCIUploadException;
import com.yumyum.sns.infra.StorageService;
import com.yumyum.sns.infra.service.StorageDeleteOutboxService;
import io.awspring.cloud.s3.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciStorageService implements StorageService {

    private final ObjectStorage objectStorage;

    @Value("${oci.bucket.namespace}")
    private String namespace;

    @Value("${oci.bucket.name}")
    private String bucketName;

    @Value("${oci.storage.url-prefix}")
    private String urlPrefix;

    //OCI Storage 다중 파일 업로드
    @Override
    public List<AttachDto> uploadFiles(List<MultipartFile> files) {
        List<AttachDto> attachDtos = new ArrayList<>();
        String fileName = "";

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("업로드된 파일 중 빈 파일이 존재합니다.");
                }
                fileName = file.getOriginalFilename();
                String uniqueFileName = "media/"+UUID.randomUUID().toString() + "_" + fileName;

                PutObjectRequest request = PutObjectRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .objectName(uniqueFileName)
                        .putObjectBody(file.getInputStream())
                        .contentLength(file.getSize())
                        .build();

                objectStorage.putObject(request);

                String url = urlPrefix + namespace + "/b/" + bucketName + "/o/" + uniqueFileName;
                AttachDto attachDto = new AttachDto(file, uniqueFileName, url);
                attachDtos.add(attachDto);
            }
        } catch (IOException e) {
            deleteFiles(toSavedFileNames(attachDtos));
            throw new FileUploadException("파일 업로드 실패: "+ fileName);
        } catch (S3Exception e){
            log.error("OCI 파일 업로드 실패");
            deleteFiles(toSavedFileNames(attachDtos));
            throw new OCIUploadException("OCI 파일 업로드 실패");
        }
        return attachDtos;
    }


    //OCI Storage 단일 파일 업로드
    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일 중 빈 파일이 존재합니다.");
        }
        String fileName = file.getOriginalFilename();
        String uniqueFileName = "media/"+ UUID.randomUUID().toString() + "_" + fileName;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(uniqueFileName)
                    .putObjectBody(file.getInputStream())
                    .contentLength(file.getSize())
                    .build();

            objectStorage.putObject(request);

            return urlPrefix + namespace + "/b/" + bucketName + "/o/" + uniqueFileName;
        }catch (IOException e) {
            log.error("storage 파일 업로드 실패");
            throw new FileUploadException("파일 업로드 실패: "+ fileName);
        }catch (BmcException e){
            throw new OCIUploadException("OCI 파일 업로드 실패");
        }
    }

    //OCI Storage 파일 삭제
    @Override
    public boolean deleteFile(String objectName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectName)
                .build();

        try {
            objectStorage.deleteObject(request);
            return true;
        } catch (BmcException e) {
            log.error("storage 파일 삭제 실패");
            if (e.getStatusCode() == 404) {
                log.warn("이미 존재하지 않는 객체: " + objectName);
                return true;
            } else {
                log.error("고아 파일 발생 - 삭제 필요: " + objectName);
                return false;
            }

        }
    }

    //다중 파일 삭제
    @Override
    public void deleteFiles(List<String> objectNames){
        objectNames.stream().forEach(objectName -> deleteFile(objectName));
    }

    //List<dto> -> List<String> 파일들의 저장된 경로 반환
    @Override
    public List<String> toSavedFileNames(List<AttachDto> attachDtos){
        return attachDtos.stream().map(AttachDto::getSavedFileName).collect(Collectors.toList());
    }
}

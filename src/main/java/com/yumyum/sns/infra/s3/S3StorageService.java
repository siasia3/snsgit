package com.yumyum.sns.infra.s3;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.error.exception.FileUploadException;
import com.yumyum.sns.error.exception.S3DeleteException;
import com.yumyum.sns.error.exception.S3UploadException;
import com.yumyum.sns.infra.StorageService;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
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
public class S3StorageService implements StorageService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;
    private final S3Operations s3Operations;


    //s3 다중파일 저장
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
                S3Resource upload = s3Operations.upload(bucket, uniqueFileName, file.getInputStream());
                String url = cloudFrontDomain + "/" + uniqueFileName;
                AttachDto attachDto = new AttachDto(file, uniqueFileName, url);
                attachDtos.add(attachDto);
            }
        } catch (IOException e) {
            deleteFiles(toSavedFileNames(attachDtos));
            throw new FileUploadException("파일 업로드 실패: "+ fileName);
        } catch (S3Exception e){
            log.error("s3 파일 업로드 실패");
            deleteFiles(toSavedFileNames(attachDtos));
            throw new S3UploadException("S3 저장 실패");
        }
        return attachDtos;
    }

    //s3 다중파일 삭제
    @Override
    public void deleteFiles(List<String> fileNames){
        try {
            fileNames.stream()
                    .forEach(savedFileName -> s3Operations.deleteObject(bucket,savedFileName));
        }catch(S3Exception e){
            log.error("s3 파일 삭제 실패");
            throw new S3DeleteException("S3 파일 삭제 실패");
        }
    }

    //s3 단일파일 저장
    @Override
    public String uploadFile(MultipartFile file){
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일 중 빈 파일이 존재합니다.");
        }
        String fileName = file.getOriginalFilename();
        String uniqueFileName = "media/"+UUID.randomUUID().toString() + "_" + fileName;
        try {
            S3Resource upload = s3Operations.upload(bucket, uniqueFileName, file.getInputStream());
            return cloudFrontDomain + "/" + uniqueFileName;
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 실패: "+ fileName);
        } catch (S3Exception e){
            log.error("s3 파일 업로드 실패");
            throw new S3UploadException("S3 파일 저장 실패: "+ fileName);
        }
    }


    //s3 단일파일 삭제
    @Override
    public boolean deleteFile(String fileName){
        try{
            s3Operations.deleteObject(bucket,fileName);
            return true;
        }catch (S3Exception e){
            log.error("s3 파일 삭제 실패");
            return false;
        }

    }

    //List<dto> -> List<String> 파일들의 저장된 경로 반환
    @Override
    public List<String> toSavedFileNames(List<AttachDto> attachDtos){
        return attachDtos.stream().map(AttachDto::getSavedFileName).collect(Collectors.toList());
    }


}

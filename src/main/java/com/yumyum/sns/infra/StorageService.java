package com.yumyum.sns.infra;

import com.yumyum.sns.attachment.dto.AttachDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    //다중 파일 업로드
    List<AttachDto> uploadFiles(List<MultipartFile> files);
    //단일 파일 업로드
    String uploadFile(MultipartFile file);
    //단일 파일 삭제
    boolean deleteFile(String objectName);
    //다중 파일 삭제
    void deleteFiles(List<String> objectNames);

    //List<dto> -> List<String> 파일들의 저장된 경로 반환
    List<String> toSavedFileNames(List<AttachDto> attachDtos);
}

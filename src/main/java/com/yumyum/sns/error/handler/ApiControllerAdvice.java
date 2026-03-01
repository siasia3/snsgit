package com.yumyum.sns.error.handler;

import com.yumyum.sns.error.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(BaseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(BaseNotFoundException e) {
        log.error("NotFoundException: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<String> handleInvalidLogin(InvalidLoginException ex) {
        log.warn("InvalidLoginException: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(FileUploadException e) {
        log.error("FileUploadException: ", e);
        // 파일 이름을 메시지에 포함
        String errorMessage = "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", errorMessage));
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<Map<String, String>> handleS3UploadException(S3UploadException e) {
        log.error("S3UploadException: ", e);
        // 파일 이름을 메시지에 포함
        String errorMessage = "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message",errorMessage));
    }

    @ExceptionHandler(OCIUploadException.class)
    public ResponseEntity<Map<String, String>> handleOCIUploadException(OCIUploadException e) {
        log.error("OCIUploadException: ", e);
        // 파일 이름을 메시지에 포함
        String errorMessage = "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message",errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: ", e);
        return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateException e) {
        log.warn("DuplicateException: ", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

}

package com.yumyum.sns.error.handler;

import com.yumyum.sns.error.exception.ApiResponse;
import com.yumyum.sns.error.exception.FileUploadException;
import com.yumyum.sns.error.exception.InvalidLoginException;
import com.yumyum.sns.error.exception.OCIUploadException;
import com.yumyum.sns.error.exception.S3UploadException;
import com.yumyum.sns.error.exception.custom.BusinessException;
import com.yumyum.sns.error.exception.errorcode.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: [{}] {}", errorCode, e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<String> handleInvalidLogin(InvalidLoginException ex) {
        log.warn("InvalidLoginException: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(FileUploadException e) {
        log.error("FileUploadException: ", e);
        String errorMessage = "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", errorMessage));
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<Map<String, String>> handleS3UploadException(S3UploadException e) {
        log.error("S3UploadException: ", e);
        String errorMessage = "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message",errorMessage));
    }

    @ExceptionHandler(OCIUploadException.class)
    public ResponseEntity<Map<String, String>> handleOCIUploadException(OCIUploadException e) {
        log.error("OCIUploadException: ", e);
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

}
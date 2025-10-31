package com.yumyum.sns.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (data 포함)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    // 성공 응답 (data 없이)
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // 실패 응답
    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
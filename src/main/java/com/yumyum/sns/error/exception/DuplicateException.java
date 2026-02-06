package com.yumyum.sns.error.exception;

public abstract class DuplicateException extends RuntimeException{
    public DuplicateException(String message) {
        super(message);
    }
}

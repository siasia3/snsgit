package com.yumyum.sns.error.exception;

public class InvalidLoginException extends RuntimeException{
    public InvalidLoginException() {
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}

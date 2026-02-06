package com.yumyum.sns.error.exception;

public class DuplicateUserIdException extends DuplicateException{
    public DuplicateUserIdException() {
        super("이미 사용중인 회원 아이디입니다.");
    }
}

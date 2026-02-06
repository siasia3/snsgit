package com.yumyum.sns.error.exception;

public class DuplicateNicknameException extends DuplicateException{
    public DuplicateNicknameException() {
        super("이미 사용중인 회원 닉네임입니다.");
    }
}

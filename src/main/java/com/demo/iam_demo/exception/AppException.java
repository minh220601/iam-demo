package com.demo.iam_demo.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private ErrorCode errorCode;
    public AuthException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

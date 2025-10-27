package com.demo.iam_demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // AUTHENTICATION & AUTHORIZATION
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),

    // USER RELATED
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "User already exists", HttpStatus.CONFLICT),
    INVALID_PASSWORD(2003, "Invalid password", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(2004, "Role not found", HttpStatus.NOT_FOUND),

    // VALIDATION
    INVALID_OUTPUT(3001, "Invalid output", HttpStatus.BAD_REQUEST),

    // IMAGE
    IMAGE_UPLOAD_FAILED(4001, "Upload image failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // SYSTEM
    INTERNAL_SERVER_ERROR(9000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status){
        this.code = code;
        this.message = message;
        this.status = status;
    }
}

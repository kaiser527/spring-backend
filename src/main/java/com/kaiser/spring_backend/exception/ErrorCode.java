package com.kaiser.spring_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "User is already exist", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1006, "User is not exist", HttpStatus.NOT_FOUND),
    UNCATEGORZIED_EXCEPTION(999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1002, "Invalid message key", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1003, "Invalid email format", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "Username must be atleast {min} letters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Password must be at least {min} letters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1007,"Missing token at header or token is expired", HttpStatus.UNAUTHORIZED)
    ;

    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

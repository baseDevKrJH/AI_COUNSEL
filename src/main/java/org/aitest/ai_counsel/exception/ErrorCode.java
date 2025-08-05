package org.aitest.ai_counsel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", " Invalid Input Value"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "Access is Denied"),

    // Counsel
    COUNSEL_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "Counsel is not found."),

    // Analysis
    ANALYSIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "Analysis error."),

    // Prediction
    PREDICTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "P001", "Prediction error.");


    private final HttpStatus status;
    private final String code;
    private final String message;

}

package org.aitest.ai_counsel.exception;

public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }

    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}

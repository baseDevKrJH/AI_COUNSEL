package org.aitest.ai_counsel.exception;

/**
 * 잘못된 요청 데이터로 인해 발생하는 예외
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

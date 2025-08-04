package org.aitest.ai_counsel.exception;

/**
 * 상담 정보를 찾을 수 없을 때 발생하는 예외
 */
public class CounselNotFoundException extends RuntimeException {

    public CounselNotFoundException(String message) {
        super(message);
    }

    public CounselNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CounselNotFoundException(Long counselId) {
        super("상담을 찾을 수 없습니다. ID: " + counselId);
    }
}

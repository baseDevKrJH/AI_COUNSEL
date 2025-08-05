package org.aitest.ai_counsel.exception;

public class CounselNotFoundException extends BusinessException {

    public CounselNotFoundException(String message) {
        super(message, ErrorCode.COUNSEL_NOT_FOUND);
    }

    public CounselNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

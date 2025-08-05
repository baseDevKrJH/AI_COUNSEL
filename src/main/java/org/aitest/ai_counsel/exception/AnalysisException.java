package org.aitest.ai_counsel.exception;

public class AnalysisException extends BusinessException {

    public AnalysisException(String message) {
        super(message, ErrorCode.ANALYSIS_ERROR);
    }

    public AnalysisException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, ErrorCode.ANALYSIS_ERROR, cause);
    }
}

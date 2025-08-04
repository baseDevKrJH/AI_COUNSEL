package org.aitest.ai_counsel.exception;

/**
 * 분석 처리 중 발생하는 예외
 */
public class AnalysisException extends RuntimeException {

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.certification.exception;

/**
 * AI服务异常
 * AI判断、智能审核相关异常
 */
public class AIServiceException extends BaseException {

    public AIServiceException(String message) {
        super("AI_SERVICE_ERROR", message, 500);
    }

    public AIServiceException(String message, Throwable cause) {
        super("AI_SERVICE_ERROR", message, 500, cause);
    }

    /**
     * AI API调用失败
     */
    public static class APICallException extends AIServiceException {
        public APICallException(String apiName, String reason) {
            super(String.format("AI API [%s] 调用失败: %s", apiName, reason));
        }
    }

    /**
     * AI响应解析失败
     */
    public static class ResponseParseException extends AIServiceException {
        public ResponseParseException(String reason) {
            super("AI响应解析失败: " + reason);
        }
    }

    /**
     * AI服务超时
     */
    public static class TimeoutException extends AIServiceException {
        public TimeoutException(String taskType) {
            super(String.format("AI服务超时，任务类型: %s", taskType));
        }
    }
}

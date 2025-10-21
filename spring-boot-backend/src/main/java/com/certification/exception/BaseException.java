package com.certification.exception;

import lombok.Getter;

/**
 * 业务异常基类
 * 所有自定义异常的父类
 *
 * @author System
 * @since 2025-01-14
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误消息
     */
    private final String errorMessage;

    /**
     * HTTP状态码（可选）
     */
    private final Integer httpStatus;

    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = null;
    }

    public BaseException(String errorCode, String errorMessage, Integer httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = null;
    }

    public BaseException(String errorCode, String errorMessage, Integer httpStatus, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}

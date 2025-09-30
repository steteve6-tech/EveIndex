package com.certification.exception;

/**
 * 所有数据重复异常
 * 当爬取的数据全部为重复数据时抛出此异常
 * 这是一个业务异常，不是系统错误
 */
public class AllDataDuplicateException extends RuntimeException {
    
    public AllDataDuplicateException(String message) {
        super(message);
    }
    
    public AllDataDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.certification.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各类异常，返回标准化的错误响应
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException ex) {
        log.warn("业务异常: errorCode={}, message={}", ex.getErrorCode(), ex.getErrorMessage(), ex);

        Map<String, Object> response = buildErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                null
        );

        int httpStatus = ex.getHttpStatus() != null ? ex.getHttpStatus() : 500;
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * 处理爬虫异常
     */
    @ExceptionHandler(CrawlerException.class)
    public ResponseEntity<Map<String, Object>> handleCrawlerException(CrawlerException ex) {
        log.error("爬虫执行异常: {}", ex.getErrorMessage(), ex);

        Map<String, Object> response = buildErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                "爬虫执行失败，请稍后重试或联系管理员"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理AI服务异常
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAIServiceException(AIServiceException ex) {
        log.error("AI服务异常: {}", ex.getErrorMessage(), ex);

        Map<String, Object> response = buildErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                "AI服务暂时不可用，请稍后重试"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<Map<String, Object>> handleDataValidationException(DataValidationException ex) {
        log.warn("数据验证失败: {}", ex.getErrorMessage());

        Map<String, Object> response = buildErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                "请检查输入数据是否正确"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("资源未找到: {}", ex.getErrorMessage());

        Map<String, Object> response = buildErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理参数绑定异常（Spring Validation）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("参数验证失败: {}", ex.getMessage());

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        Map<String, Object> response = buildErrorResponse(
                "VALIDATION_ERROR",
                "参数验证失败",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        log.warn("参数绑定失败: {}", ex.getMessage());

        String errors = ex.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        Map<String, Object> response = buildErrorResponse(
                "BIND_ERROR",
                "参数绑定失败: " + errors,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配: {} = {}", ex.getName(), ex.getValue());

        String message = String.format("参数 [%s] 类型错误，值: %s", ex.getName(), ex.getValue());

        Map<String, Object> response = buildErrorResponse(
                "TYPE_MISMATCH_ERROR",
                message,
                "请检查参数类型是否正确"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());

        Map<String, Object> response = buildErrorResponse(
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                "请检查请求参数是否合法"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex) {
        log.error("空指针异常", ex);

        Map<String, Object> response = buildErrorResponse(
                "NULL_POINTER_ERROR",
                "系统内部错误",
                "请联系管理员"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常", ex);

        Map<String, Object> response = buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "系统内部错误: " + ex.getMessage(),
                "请稍后重试或联系管理员"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 构建标准化的错误响应
     */
    private Map<String, Object> buildErrorResponse(String errorCode, String errorMessage, String suggestion) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("error", errorMessage);
        response.put("message", errorMessage);

        if (suggestion != null) {
            response.put("suggestion", suggestion);
        }

        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }
}

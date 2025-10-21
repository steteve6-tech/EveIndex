package com.certification.exception;

/**
 * 数据验证异常
 * 数据校验失败时抛出
 */
public class DataValidationException extends BaseException {

    public DataValidationException(String message) {
        super("DATA_VALIDATION_ERROR", message, 400);
    }

    public DataValidationException(String field, String reason) {
        super("DATA_VALIDATION_ERROR", "字段 [" + field + "] 验证失败: " + reason, 400);
    }

    /**
     * 必填字段缺失异常
     */
    public static class RequiredFieldMissingException extends DataValidationException {
        public RequiredFieldMissingException(String fieldName) {
            super(fieldName, "必填字段不能为空");
        }
    }

    /**
     * 字段格式错误异常
     */
    public static class InvalidFormatException extends DataValidationException {
        public InvalidFormatException(String fieldName, String expectedFormat) {
            super(fieldName, "格式错误，期望格式: " + expectedFormat);
        }
    }

    /**
     * 数据重复异常
     */
    public static class DuplicateDataException extends DataValidationException {
        public DuplicateDataException(String entityType, String uniqueField, String value) {
            super(String.format("%s 数据重复，%s = %s 已存在", entityType, uniqueField, value));
        }
    }
}

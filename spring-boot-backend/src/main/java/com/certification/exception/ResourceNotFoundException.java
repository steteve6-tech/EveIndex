package com.certification.exception;

/**
 * 资源未找到异常
 * 查询的资源不存在时抛出
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("RESOURCE_NOT_FOUND",
              String.format("资源 [%s] 不存在，标识符: %s", resourceName, identifier),
              404);
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, 404);
    }

    /**
     * 设备数据未找到
     */
    public static class DeviceDataNotFoundException extends ResourceNotFoundException {
        public DeviceDataNotFoundException(String entityType, Long id) {
            super(entityType, id.toString());
        }
    }

    /**
     * 爬虫未找到
     */
    public static class CrawlerNotFoundException extends ResourceNotFoundException {
        public CrawlerNotFoundException(String crawlerName) {
            super("爬虫", crawlerName);
        }
    }

    /**
     * 任务配置未找到
     */
    public static class TaskConfigNotFoundException extends ResourceNotFoundException {
        public TaskConfigNotFoundException(Long taskId) {
            super("任务配置", taskId.toString());
        }
    }
}

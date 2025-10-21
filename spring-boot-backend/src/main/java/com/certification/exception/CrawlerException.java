package com.certification.exception;

/**
 * 爬虫异常
 * 爬虫执行过程中的异常
 */
public class CrawlerException extends BaseException {

    public CrawlerException(String message) {
        super("CRAWLER_ERROR", message, 500);
    }

    public CrawlerException(String message, Throwable cause) {
        super("CRAWLER_ERROR", message, 500, cause);
    }

    /**
     * 网络超时异常
     */
    public static class NetworkTimeoutException extends CrawlerException {
        public NetworkTimeoutException(String crawlerName) {
            super("爬虫 [" + crawlerName + "] 网络超时");
        }
    }

    /**
     * 解析失败异常
     */
    public static class ParseException extends CrawlerException {
        public ParseException(String crawlerName, String reason) {
            super("爬虫 [" + crawlerName + "] 解析失败: " + reason);
        }
    }

    /**
     * 目标网站无响应异常
     */
    public static class TargetSiteException extends CrawlerException {
        public TargetSiteException(String url, int statusCode) {
            super("目标网站 [" + url + "] 返回异常状态码: " + statusCode);
        }
    }
}

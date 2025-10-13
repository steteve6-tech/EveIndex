package com.certification.service.crawler;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 爬虫执行结果统一封装
 * 用于标准化不同爬虫的返回结果
 */
@Data
@Accessors(chain = true)
public class CrawlerResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 爬取数据量
     */
    private Integer crawledCount = 0;

    /**
     * 成功保存数量
     */
    private Integer savedCount = 0;

    /**
     * 跳过/重复数量
     */
    private Integer skippedCount = 0;

    /**
     * 失败数量
     */
    private Integer failedCount = 0;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（秒）
     */
    private Long durationSeconds;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误堆栈
     */
    private String errorStack;

    /**
     * 详细结果数据（JSON格式）
     */
    private String detailData;

    /**
     * 创建成功结果
     */
    public static CrawlerResult success(String message) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建成功结果（带数据统计）
     */
    public static CrawlerResult success(String message, int savedCount, int skippedCount) {
        CrawlerResult result = success(message);
        result.setSavedCount(savedCount);
        result.setSkippedCount(skippedCount);
        result.setCrawledCount(savedCount + skippedCount);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static CrawlerResult failure(String errorMessage) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(false);
        result.setMessage("执行失败");
        result.setErrorMessage(errorMessage);
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败结果（带异常）
     */
    public static CrawlerResult failure(String errorMessage, Exception exception) {
        CrawlerResult result = failure(errorMessage);
        if (exception != null) {
            result.setErrorStack(getStackTrace(exception));
        }
        return result;
    }

    /**
     * 创建部分成功结果
     */
    public static CrawlerResult partialSuccess(String message, int savedCount, int failedCount) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(true); // 部分成功也算成功
        result.setMessage(message);
        result.setSavedCount(savedCount);
        result.setFailedCount(failedCount);
        result.setCrawledCount(savedCount + failedCount);
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    /**
     * 设置开始时间
     */
    public CrawlerResult markStart() {
        this.startTime = LocalDateTime.now();
        return this;
    }

    /**
     * 设置结束时间并计算耗时
     */
    public CrawlerResult markEnd() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.durationSeconds = java.time.Duration.between(this.startTime, this.endTime).getSeconds();
        }
        return this;
    }

    /**
     * 解析字符串结果
     * 从爬虫返回的字符串中提取数据量信息
     */
    public static CrawlerResult fromString(String resultString) {
        CrawlerResult result = new CrawlerResult();
        result.setSuccess(true);
        result.setMessage(resultString);
        
        // 尝试从字符串中提取数字信息
        // 例如: "保存成功：10条新记录，跳过重复：5条"
        try {
            String[] patterns = {
                "保存成功[:：]?\\s*(\\d+)\\s*条",
                "新增[:：]?\\s*(\\d+)\\s*条",
                "入库[:：]?\\s*(\\d+)\\s*条",
                "saved[:：]?\\s*(\\d+)",
                "成功[:：]?\\s*(\\d+)"
            };
            
            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(resultString);
                if (m.find()) {
                    result.setSavedCount(Integer.parseInt(m.group(1)));
                    break;
                }
            }
            
            // 尝试提取跳过数量
            String[] skipPatterns = {
                "跳过[:：]?\\s*(\\d+)\\s*条",
                "重复[:：]?\\s*(\\d+)\\s*条",
                "skipped[:：]?\\s*(\\d+)"
            };
            
            for (String pattern : skipPatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(resultString);
                if (m.find()) {
                    result.setSkippedCount(Integer.parseInt(m.group(1)));
                    break;
                }
            }
            
            result.setCrawledCount(result.getSavedCount() + result.getSkippedCount());
            
        } catch (Exception e) {
            // 解析失败不影响结果
        }
        
        return result;
    }

    /**
     * 获取异常堆栈信息
     */
    private static String getStackTrace(Exception exception) {
        if (exception == null) return null;
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 是否完全成功（没有失败记录）
     */
    public boolean isFullySuccessful() {
        return success && (failedCount == null || failedCount == 0);
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (crawledCount == null || crawledCount == 0) {
            return 0.0;
        }
        return (double) (savedCount != null ? savedCount : 0) / crawledCount * 100;
    }

    /**
     * 获取简短摘要
     */
    public String getSummary() {
        if (success) {
            return String.format("成功：保存%d条，跳过%d条", 
                savedCount != null ? savedCount : 0, 
                skippedCount != null ? skippedCount : 0);
        } else {
            return "失败：" + (errorMessage != null ? errorMessage : message);
        }
    }
}


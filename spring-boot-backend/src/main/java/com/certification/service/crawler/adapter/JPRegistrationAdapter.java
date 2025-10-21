package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.jp.JpRegistration;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 日本PMDA设备注册爬虫适配器
 */
@Slf4j
@Component("JP_Registration_Adapter")
public class JPRegistrationAdapter implements ICrawlerExecutor {

    @Autowired
    private JpRegistration crawler;

    @Override
    public String getCrawlerName() {
        return "JP_Registration";
    }

    @Override
    public String getCountryCode() {
        return "JP";
    }

    @Override
    public String getCrawlerType() {
        return "REGISTRATION";
    }

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行JP_Registration爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            // 从fieldKeywords中提取参数
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();

            // 合并keywords和deviceNames作为关键词搜索（两者等效）
            List<String> keywords = new java.util.ArrayList<>();
            if (fieldKeywords.containsKey("keywords")) {
                keywords.addAll(fieldKeywords.get("keywords"));
            }
            if (fieldKeywords.containsKey("deviceNames")) {
                keywords.addAll(fieldKeywords.get("deviceNames"));
            }

            // 获取公司名称
            List<String> companyNames = fieldKeywords.getOrDefault("companyNames", List.of());

            // 获取最大记录数
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;

            String resultMsg;

            // 判断使用哪种搜索方式
            if (!companyNames.isEmpty()) {
                // 按公司名称搜索
                if (companyNames.size() == 1) {
                    // 单个公司
                    resultMsg = crawler.crawlByCompany(companyNames.get(0), maxRecords);
                } else {
                    // 多个公司，批量处理
                    StringBuilder batchResult = new StringBuilder();
                    int totalNew = 0;
                    int totalDuplicate = 0;

                    for (String companyName : companyNames) {
                        log.info("处理公司: {}", companyName);
                        String msg = crawler.crawlByCompany(companyName, maxRecords);
                        batchResult.append(msg).append("\n");

                        // 解析结果统计
                        if (msg.contains("新增:") && msg.contains("重复:")) {
                            try {
                                int newCount = extractCount(msg, "新增:");
                                int dupCount = extractCount(msg, "重复:");
                                totalNew += newCount;
                                totalDuplicate += dupCount;
                            } catch (Exception e) {
                                log.warn("解析统计失败: {}", msg);
                            }
                        }
                    }

                    resultMsg = String.format("✅ 日本注册记录批量爬取完成！处理公司数: %d，总新增: %d 条，总重复: %d 条",
                            companyNames.size(), totalNew, totalDuplicate);
                }
            } else if (!keywords.isEmpty()) {
                // 按关键词搜索
                if (keywords.size() == 1) {
                    // 单个关键词
                    resultMsg = crawler.crawlByKeyword(keywords.get(0), maxRecords);
                } else {
                    // 多个关键词，批量处理
                    StringBuilder batchResult = new StringBuilder();
                    int totalNew = 0;
                    int totalDuplicate = 0;

                    for (String keyword : keywords) {
                        log.info("处理关键词: {}", keyword);
                        String msg = crawler.crawlByKeyword(keyword, maxRecords);
                        batchResult.append(msg).append("\n");

                        // 解析结果统计
                        if (msg.contains("新增:") && msg.contains("重复:")) {
                            try {
                                int newCount = extractCount(msg, "新增:");
                                int dupCount = extractCount(msg, "重复:");
                                totalNew += newCount;
                                totalDuplicate += dupCount;
                            } catch (Exception e) {
                                log.warn("解析统计失败: {}", msg);
                            }
                        }
                    }

                    resultMsg = String.format("✅ 日本注册记录批量爬取完成！处理关键词数: %d，总新增: %d 条，总重复: %d 条",
                            keywords.size(), totalNew, totalDuplicate);
                }
            } else {
                throw new IllegalArgumentException("必须提供关键词(keywords)或公司名称(companyNames)");
            }

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("JP_Registration爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从结果消息中提取数字
     */
    private int extractCount(String message, String prefix) {
        int startIdx = message.indexOf(prefix);
        if (startIdx == -1) return 0;

        startIdx += prefix.length();
        int endIdx = message.indexOf("条", startIdx);
        if (endIdx == -1) endIdx = message.indexOf(",", startIdx);
        if (endIdx == -1) endIdx = message.indexOf("，", startIdx);

        String countStr = message.substring(startIdx, endIdx).trim();
        return Integer.parseInt(countStr);
    }

    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;

        Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
        if (fieldKeywords == null || fieldKeywords.isEmpty()) {
            return false;
        }

        // 合并检查keywords和deviceNames（两者等效）
        boolean hasKeywords = (fieldKeywords.containsKey("keywords") && !fieldKeywords.get("keywords").isEmpty()) ||
                (fieldKeywords.containsKey("deviceNames") && !fieldKeywords.get("deviceNames").isEmpty());
        boolean hasCompanyNames = fieldKeywords.containsKey("companyNames") && !fieldKeywords.get("companyNames").isEmpty();

        // 必须提供关键词或公司名称至少一个
        return hasKeywords || hasCompanyNames;
    }

    @Override
    public String getDescription() {
        return "日本PMDA医疗器械注册记录爬虫 - 支持关键词和公司名称搜索";
    }
}

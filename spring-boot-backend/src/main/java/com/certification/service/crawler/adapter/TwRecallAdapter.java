package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.tw.TwRecall;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 台湾召回记录爬虫适配器
 */
@Slf4j
@Component
public class TwRecallAdapter implements ICrawlerExecutor {

    @Autowired
    private TwRecall crawler;

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行TW_Recall爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取设备名称列表
            List<String> deviceNames = fieldKeywords.getOrDefault("deviceNames", List.of());
            
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;
            
            String resultMsg;

            if (!deviceNames.isEmpty()) {
                // 按设备名称搜索
                if (deviceNames.size() == 1) {
                    resultMsg = crawler.crawlByDeviceName(deviceNames.get(0), maxRecords);
                } else {
                    // 批量搜索
                    int successCount = 0;
                    for (String deviceName : deviceNames) {
                        try {
                            String msg = crawler.crawlByDeviceName(deviceName, maxRecords);
                            if (msg.contains("保存完成")) {
                                successCount++;
                            }
                        } catch (Exception e) {
                            log.error("设备名称搜索失败: {}", deviceName, e);
                        }
                    }
                    
                    resultMsg = String.format("批量设备搜索完成，成功: %d/%d", 
                            successCount, deviceNames.size());
                }
            } else if (params.getDateFrom() != null && params.getDateTo() != null) {
                // 按日期范围搜索
                resultMsg = crawler.crawlByDateRange(params.getDateFrom(), params.getDateTo(), maxRecords);
            } else {
                // 默认：爬取最新召回记录
                resultMsg = crawler.crawlLatestRecalls(maxRecords);
            }

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            return CrawlerResult.fromString(resultMsg)
                    .setStartTime(result.getStartTime())
                    .setEndTime(result.getEndTime())
                    .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("TW_Recall爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCrawlerName() {
        return "TW_Recall";
    }

    @Override
    public String getCrawlerType() {
        return "RECALL";
    }

    @Override
    public String getCountryCode() {
        return "TW";
    }

    @Override
    public String getDescription() {
        return "台湾召回记录爬虫 - 爬取台湾食品药物管理署医疗器材回收警讯资料";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}


package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.tw.TwGuidance;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 台湾法规文档爬虫适配器
 */
@Slf4j
@Component
public class TwGuidanceAdapter implements ICrawlerExecutor {

    @Autowired
    private TwGuidance crawler;

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行TW_Guidance爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;
            
            // 默认爬取所有医疗器材法规
            // 类别55：药品、医疗器材及化粧品类
            // 子类别59：医疗器材管理
            String resultMsg = crawler.crawlAllMedicalDeviceLaws(maxRecords);

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            return CrawlerResult.fromString(resultMsg)
                    .setStartTime(result.getStartTime())
                    .setEndTime(result.getEndTime())
                    .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("TW_Guidance爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCrawlerName() {
        return "TW_Guidance";
    }

    @Override
    public String getCrawlerType() {
        return "GUIDANCE";
    }

    @Override
    public String getCountryCode() {
        return "TW";
    }

    @Override
    public String getDescription() {
        return "台湾法规文档爬虫 - 爬取台湾食品药物管理署医疗器材相关法规";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}


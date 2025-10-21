package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.jp.JpRecall;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 日本召回记录爬虫适配器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JPRecallAdapter implements ICrawlerExecutor {

    private final JpRecall jpRecallCrawler;

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        try {
            log.info("执行JP_Recall爬虫，参数: {}", params);

            // 提取参数
            List<String> sellers = params.getFieldKeywords("sellers");
            List<String> manufacturers = params.getFieldKeywords("manufacturers");
            List<String> years = params.getFieldKeywords("years");
            
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;
            int batchSize = params.getBatchSize() != null ? params.getBatchSize() : 20;
            String dateFrom = params.getDateFrom();
            String dateTo = params.getDateTo();

            // 执行爬虫
            String result = jpRecallCrawler.crawlWithMultipleFields(
                sellers,
                manufacturers,
                years,
                maxRecords,
                batchSize,
                dateFrom,
                dateTo
            );

            return CrawlerResult.success(result);

        } catch (Exception e) {
            log.error("JP_Recall爬虫执行失败", e);
            return CrawlerResult.failure("爬虫执行失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validate(CrawlerParams params) {
        return true;
    }

    @Override
    public String getUniqueKey() {
        return "JP_RECALL";
    }

    @Override
    public String getCrawlerName() {
        return "JP_Recall";
    }

    @Override
    public String getCountryCode() {
        return "JP";
    }

    @Override
    public String getCrawlerType() {
        return "RECALL";
    }

    @Override
    public String getDescription() {
        return "日本PMDA医疗器械召回记录爬虫";
    }
}


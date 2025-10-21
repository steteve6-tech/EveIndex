package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.tw.TwCustomsCase;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 台湾海关判例爬虫适配器
 */
@Slf4j
@Component
public class TwCustomsCaseAdapter implements ICrawlerExecutor {

    @Autowired
    private TwCustomsCase crawler;

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行TW_CustomsCase爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取CCC号列列表
            List<String> cccCodes = fieldKeywords.getOrDefault("cccCodes", List.of());
            
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;
            
            String resultMsg;

            if (!cccCodes.isEmpty()) {
                // 按CCC号列搜索
                if (cccCodes.size() == 1) {
                    resultMsg = crawler.crawlByGoodCode(cccCodes.get(0), maxRecords);
                } else {
                    // 批量搜索
                    int successCount = 0;
                    
                    for (String cccCode : cccCodes) {
                        try {
                            String msg = crawler.crawlByGoodCode(cccCode, maxRecords);
                            if (msg.contains("保存完成")) {
                                successCount++;
                            }
                        } catch (Exception e) {
                            log.error("CCC号列搜索失败: {}", cccCode, e);
                        }
                    }
                    
                    resultMsg = String.format("批量CCC号列搜索完成，成功: %d/%d", 
                            successCount, cccCodes.size());
                }
            } else if (params.getDateFrom() != null && params.getDateTo() != null) {
                // 按日期范围搜索
                resultMsg = crawler.crawlByDateRange(params.getDateFrom(), params.getDateTo(), maxRecords);
            } else {
                // 默认：最近30天
                String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                resultMsg = crawler.crawlByDateRange(startDate, endDate, maxRecords);
            }

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            return CrawlerResult.fromString(resultMsg)
                    .setStartTime(result.getStartTime())
                    .setEndTime(result.getEndTime())
                    .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("TW_CustomsCase爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCrawlerName() {
        return "TW_CustomsCase";
    }

    @Override
    public String getCrawlerType() {
        return "CUSTOMS_CASE";
    }

    @Override
    public String getCountryCode() {
        return "TW";
    }

    @Override
    public String getDescription() {
        return "台湾海关判例爬虫 - 爬取台湾经济部国际贸易局的货品输出入规定公告异动资料";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}


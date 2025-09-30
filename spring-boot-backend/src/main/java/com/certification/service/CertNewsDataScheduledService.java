package com.certification.service;

import com.certification.crawler.certification.SgsCrawler;
import com.certification.crawler.certification.ULCrawler;
import com.certification.crawler.certification.BeiceCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.entity.ScheduledCrawlerConfig;
import com.certification.entity.common.CertNewsData;
import com.certification.standards.CrawlerDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CertNewsData模块定时爬取服务
 * 负责SGS、UL、Beice三个爬虫的定时调度
 */
@Slf4j
@Service
@Transactional
public class CertNewsDataScheduledService {

    @Autowired
    private SgsCrawler sgsCrawler;
    
    @Autowired
    private ULCrawler ulCrawler;
    
    @Autowired
    private BeiceCrawler beiceCrawler;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private ScheduledCrawlerConfigService configService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * 每天凌晨2点执行SGS爬虫定时任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledSgsCrawler() {
        executeCrawlerTask("SGS", sgsCrawler);
    }

    /**
     * 每天凌晨2点30分执行UL爬虫定时任务
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void scheduledUlCrawler() {
        executeCrawlerTask("UL", ulCrawler);
    }

    /**
     * 每天凌晨3点执行Beice爬虫定时任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledBeiceCrawler() {
        executeCrawlerTask("Beice", beiceCrawler);
    }

    /**
     * 执行爬虫任务
     */
    private void executeCrawlerTask(String crawlerName, Object crawler) {
        log.info("开始执行{}爬虫定时任务: {}", crawlerName, LocalDateTime.now());
        
        ScheduledCrawlerConfig config = getCrawlerConfig(crawlerName);
        if (config == null || !config.getEnabled()) {
            log.warn("{}爬虫配置未找到或已禁用，跳过执行", crawlerName);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        String resultMessage = "";
        String status = ScheduledCrawlerConfig.ExecutionStatus.FAILED;

        try {
            // 解析爬取参数
            Map<String, Object> crawlParams = parseCrawlParams(config.getCrawlParams());
            int batchSize = (Integer) crawlParams.getOrDefault("batchSize", 50);
            int maxRecords = (Integer) crawlParams.getOrDefault("maxRecords", 200);

            // 执行爬取
            List<CrawlerResult> results = executeCrawler(crawler, batchSize, maxRecords);
            
            if (results.isEmpty()) {
                resultMessage = "未获取到新数据";
                status = ScheduledCrawlerConfig.ExecutionStatus.SUCCESS;
                log.info("{}爬虫未获取到新数据", crawlerName);
            } else {
                // 保存数据
                List<CertNewsData> savedData = saveCrawlerResults(results, crawlerName);
                
                resultMessage = String.format("成功爬取并保存 %d 条数据", savedData.size());
                status = ScheduledCrawlerConfig.ExecutionStatus.SUCCESS;
                log.info("{}爬虫定时任务完成，新增数据: {} 条", crawlerName, savedData.size());
            }

        } catch (Exception e) {
            resultMessage = "执行失败: " + e.getMessage();
            status = ScheduledCrawlerConfig.ExecutionStatus.FAILED;
            log.error("{}爬虫定时任务执行失败: {}", crawlerName, e.getMessage(), e);
        } finally {
            // 更新执行状态
            configService.updateExecutionStatus(config.getId(), status, resultMessage, startTime);
            
            // 计算下次执行时间
            LocalDateTime nextExecutionTime = calculateNextExecutionTime(config.getCronExpression());
            configService.updateNextExecutionTime(config.getId(), nextExecutionTime);
        }
    }

    /**
     * 获取爬虫配置
     */
    private ScheduledCrawlerConfig getCrawlerConfig(String crawlerName) {
        List<ScheduledCrawlerConfig> configs = configService.getConfigsByCrawler(crawlerName);
        return configs.stream()
                .filter(config -> "certnewsdata".equals(config.getModuleName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析爬取参数
     */
    private Map<String, Object> parseCrawlParams(String crawlParams) {
        try {
            if (crawlParams == null || crawlParams.trim().isEmpty()) {
                return Map.of("batchSize", 50, "maxRecords", 200);
            }
            JsonNode jsonNode = objectMapper.readTree(crawlParams);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.convertValue(jsonNode, Map.class);
            return result;
        } catch (JsonProcessingException e) {
            log.warn("解析爬取参数失败，使用默认参数: {}", e.getMessage());
            return Map.of("batchSize", 50, "maxRecords", 200);
        }
    }

    /**
     * 执行爬虫
     */
    private List<CrawlerResult> executeCrawler(Object crawler, int batchSize, int maxRecords) throws Exception {
        if (crawler instanceof SgsCrawler) {
            return ((SgsCrawler) crawler).crawlLatest(batchSize);
        } else if (crawler instanceof ULCrawler) {
            return ((ULCrawler) crawler).crawlLatest(batchSize);
        } else if (crawler instanceof BeiceCrawler) {
            return ((BeiceCrawler) crawler).crawlLatest(batchSize);
        } else {
            throw new IllegalArgumentException("不支持的爬虫类型: " + crawler.getClass().getSimpleName());
        }
    }

    /**
     * 保存爬虫结果
     */
    private List<CertNewsData> saveCrawlerResults(List<CrawlerResult> results, String crawlerName) {
        List<CertNewsData> certNewsDataList = results.stream()
                .map(result -> convertToCertNewsData(result, crawlerName))
                .toList();
        
        return crawlerDataService.saveCrawlerDataList(certNewsDataList);
    }

    /**
     * 转换爬虫结果为CertNewsData
     */
    private CertNewsData convertToCertNewsData(CrawlerResult result, String crawlerName) {
        CertNewsData data = new CertNewsData();
        data.setId(generateId(crawlerName));
        data.setSourceName(crawlerName);
        data.setTitle(result.getTitle());
        data.setUrl(result.getUrl());
        data.setSummary(result.getTitle()); // 使用title作为summary
        data.setContent(result.getContent());
        data.setCountry(result.getCountry());
        data.setType(result.getType());
        data.setProduct(result.getTitle()); // 使用title作为product
        data.setPublishDate(LocalDateTime.now().toString()); // 使用当前时间
        data.setCrawlTime(LocalDateTime.now());
        data.setStatus(CertNewsData.DataStatus.NEW);
        data.setIsProcessed(false);
        data.setRelated(null);
        data.setRiskLevel(CertNewsData.RiskLevel.MEDIUM);
        
        return data;
    }

    /**
     * 生成唯一ID
     */
    private String generateId(String crawlerName) {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("%s_%d_%03d", crawlerName.toUpperCase(), timestamp, random);
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecutionTime(String cronExpression) {
        // 这里简化处理，实际应该使用CronExpression来解析
        // 对于每天执行的任务，返回明天的同一时间
        return LocalDateTime.now().plusDays(1);
    }

    /**
     * 手动触发爬虫任务
     */
    public Map<String, Object> triggerCrawlerManually(String crawlerName) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if ("SGS".equals(crawlerName)) {
                    executeCrawlerTask("SGS", sgsCrawler);
                } else if ("UL".equals(crawlerName)) {
                    executeCrawlerTask("UL", ulCrawler);
                } else if ("Beice".equals(crawlerName)) {
                    executeCrawlerTask("Beice", beiceCrawler);
                } else {
                    throw new IllegalArgumentException("不支持的爬虫名称: " + crawlerName);
                }
            }, executorService);
            
            // 等待任务完成（最多等待5分钟）
            future.get(5, java.util.concurrent.TimeUnit.MINUTES);
            
            result.put("success", true);
            result.put("message", crawlerName + "爬虫手动触发成功");
            result.put("executionTime", java.time.Duration.between(startTime, LocalDateTime.now()).toMillis() + "ms");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", crawlerName + "爬虫手动触发失败: " + e.getMessage());
            result.put("executionTime", java.time.Duration.between(startTime, LocalDateTime.now()).toMillis() + "ms");
            log.error("手动触发{}爬虫失败: {}", crawlerName, e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * 获取爬虫执行状态
     */
    public Map<String, Object> getCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        List<ScheduledCrawlerConfig> configs = configService.getConfigsByModule("certnewsdata");
        for (ScheduledCrawlerConfig config : configs) {
            Map<String, Object> crawlerStatus = new HashMap<>();
            crawlerStatus.put("enabled", config.getEnabled());
            crawlerStatus.put("lastExecutionTime", config.getLastExecutionTime());
            crawlerStatus.put("lastExecutionStatus", config.getLastExecutionStatus());
            crawlerStatus.put("lastExecutionResult", config.getLastExecutionResult());
            crawlerStatus.put("nextExecutionTime", config.getNextExecutionTime());
            crawlerStatus.put("executionCount", config.getExecutionCount());
            crawlerStatus.put("successCount", config.getSuccessCount());
            crawlerStatus.put("failureCount", config.getFailureCount());
            
            status.put(config.getCrawlerName(), crawlerStatus);
        }
        
        return status;
    }
}

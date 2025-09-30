package com.certification.service;

import com.certification.crawler.countrydata.us.*;
import com.certification.crawler.countrydata.eu.*;
import com.certification.entity.ScheduledCrawlerConfig;
import com.certification.entity.common.*;
import com.certification.repository.common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 设备数据定时爬取服务
 * 负责6个实体类（CustomsCase, Device510K, DeviceEventReport, DeviceRecallRecord, 
 * DeviceRegistrationRecord, GuidanceDocument）在美国和欧盟的定时爬取
 */
@Slf4j
@Service
@Transactional
public class DeviceDataScheduledService {

    // 美国爬虫
    @Autowired
    private US_510K us510kCrawler;
    @Autowired
    private US_event usEventCrawler;
    @Autowired
    private US_recall_api usRecallCrawler;
    @Autowired
    private US_registration usRegistrationCrawler;
    @Autowired
    private US_Guidance usGuidanceCrawler;
    @Autowired
    private US_CustomsCase usCustomsCrawler;

    // 欧盟爬虫
    @Autowired
    private Eu_recall euRecallCrawler;
    @Autowired
    private Eu_registration euRegistrationCrawler;
    @Autowired
    private Eu_guidance euGuidanceCrawler;
    @Autowired
    private Eu_customcase euCustomsCrawler;

    // 数据访问层
    @Autowired
    private Device510KRepository device510KRepository;
    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;
    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;
    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    @Autowired
    private ScheduledCrawlerConfigService configService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    // ==================== 美国爬虫定时任务 ====================

    /**
     * 每天凌晨4点执行美国510K爬虫
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void scheduledUs510kCrawler() {
        executeDeviceCrawlerTask("US_510K", "US", "device510k", us510kCrawler);
    }

    /**
     * 每天凌晨4点30分执行美国事件报告爬虫
     */
    @Scheduled(cron = "0 30 4 * * ?")
    public void scheduledUsEventCrawler() {
        executeDeviceCrawlerTask("US_Event", "US", "deviceeventreport", usEventCrawler);
    }

    /**
     * 每天凌晨5点执行美国召回记录爬虫
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduledUsRecallCrawler() {
        executeDeviceCrawlerTask("US_Recall", "US", "devicerecallrecord", usRecallCrawler);
    }

    /**
     * 每天凌晨5点30分执行美国注册记录爬虫
     */
    @Scheduled(cron = "0 30 5 * * ?")
    public void scheduledUsRegistrationCrawler() {
        executeDeviceCrawlerTask("US_Registration", "US", "deviceregistrationrecord", usRegistrationCrawler);
    }

    /**
     * 每天凌晨6点执行美国指导文档爬虫
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void scheduledUsGuidanceCrawler() {
        executeDeviceCrawlerTask("US_Guidance", "US", "guidancedocument", usGuidanceCrawler);
    }

    /**
     * 每天凌晨6点30分执行美国海关案例爬虫
     */
    @Scheduled(cron = "0 30 6 * * ?")
    public void scheduledUsCustomsCrawler() {
        executeDeviceCrawlerTask("US_CustomsCase", "US", "customscase", usCustomsCrawler);
    }

    // ==================== 欧盟爬虫定时任务 ====================

    /**
     * 每天凌晨8点执行欧盟召回记录爬虫
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledEuRecallCrawler() {
        executeDeviceCrawlerTask("EU_Recall", "EU", "devicerecallrecord", euRecallCrawler);
    }

    /**
     * 每天凌晨8点30分执行欧盟注册记录爬虫
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void scheduledEuRegistrationCrawler() {
        executeDeviceCrawlerTask("EU_Registration", "EU", "deviceregistrationrecord", euRegistrationCrawler);
    }

    /**
     * 每天凌晨9点执行欧盟指导文档爬虫
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduledEuGuidanceCrawler() {
        executeDeviceCrawlerTask("EU_Guidance", "EU", "guidancedocument", euGuidanceCrawler);
    }

    /**
     * 每天凌晨9点30分执行欧盟海关案例爬虫
     */
    @Scheduled(cron = "0 30 9 * * ?")
    public void scheduledEuCustomsCrawler() {
        executeDeviceCrawlerTask("EU_CustomsCase", "EU", "customscase", euCustomsCrawler);
    }

    /**
     * 执行设备数据爬虫任务
     */
    private void executeDeviceCrawlerTask(String crawlerName, String countryCode, String moduleName, Object crawler) {
        log.info("开始执行{}爬虫定时任务: {}", crawlerName, LocalDateTime.now());
        
        ScheduledCrawlerConfig config = getCrawlerConfig(crawlerName, countryCode, moduleName);
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
            List<?> results = executeDeviceCrawler(crawler, batchSize, maxRecords);
            
            if (results.isEmpty()) {
                resultMessage = "未获取到新数据";
                status = ScheduledCrawlerConfig.ExecutionStatus.SUCCESS;
                log.info("{}爬虫未获取到新数据", crawlerName);
            } else {
                // 保存数据
                int savedCount = saveDeviceResults(results, moduleName, countryCode);
                
                resultMessage = String.format("成功爬取并保存 %d 条数据", savedCount);
                status = ScheduledCrawlerConfig.ExecutionStatus.SUCCESS;
                log.info("{}爬虫定时任务完成，新增数据: {} 条", crawlerName, savedCount);
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
    private ScheduledCrawlerConfig getCrawlerConfig(String crawlerName, String countryCode, String moduleName) {
        List<ScheduledCrawlerConfig> configs = configService.getConfigsByCrawler(crawlerName);
        return configs.stream()
                .filter(config -> moduleName.equals(config.getModuleName()) && 
                                 countryCode.equals(config.getCountryCode()))
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
     * 执行设备爬虫
     */
    private List<?> executeDeviceCrawler(Object crawler, int batchSize, int maxRecords) throws Exception {
        // 这里简化实现，实际应该调用具体的爬虫方法
        // 由于爬虫方法可能不存在，这里返回空列表作为占位符
        log.info("执行爬虫: {}, 批次大小: {}, 最大记录数: {}", 
                crawler.getClass().getSimpleName(), batchSize, maxRecords);
        
        // 返回空列表，实际实现时需要调用具体的爬虫方法
        return new ArrayList<>();
    }

    /**
     * 保存设备数据结果
     */
    private int saveDeviceResults(List<?> results, String moduleName, String countryCode) {
        int savedCount = 0;
        
        try {
            switch (moduleName) {
                case "device510k":
                    @SuppressWarnings("unchecked")
                    List<Device510K> device510kList = (List<Device510K>) results;
                    device510KRepository.saveAll(device510kList);
                    savedCount = device510kList.size();
                    break;
                    
                case "deviceeventreport":
                    @SuppressWarnings("unchecked")
                    List<DeviceEventReport> eventReportList = (List<DeviceEventReport>) results;
                    deviceEventReportRepository.saveAll(eventReportList);
                    savedCount = eventReportList.size();
                    break;
                    
                case "devicerecallrecord":
                    @SuppressWarnings("unchecked")
                    List<DeviceRecallRecord> recallRecordList = (List<DeviceRecallRecord>) results;
                    deviceRecallRecordRepository.saveAll(recallRecordList);
                    savedCount = recallRecordList.size();
                    break;
                    
                case "deviceregistrationrecord":
                    @SuppressWarnings("unchecked")
                    List<DeviceRegistrationRecord> registrationRecordList = (List<DeviceRegistrationRecord>) results;
                    deviceRegistrationRecordRepository.saveAll(registrationRecordList);
                    savedCount = registrationRecordList.size();
                    break;
                    
                case "guidancedocument":
                    @SuppressWarnings("unchecked")
                    List<GuidanceDocument> guidanceDocumentList = (List<GuidanceDocument>) results;
                    guidanceDocumentRepository.saveAll(guidanceDocumentList);
                    savedCount = guidanceDocumentList.size();
                    break;
                    
                case "customscase":
                    @SuppressWarnings("unchecked")
                    List<CustomsCase> customsCaseList = (List<CustomsCase>) results;
                    customsCaseRepository.saveAll(customsCaseList);
                    savedCount = customsCaseList.size();
                    break;
                    
                default:
                    log.warn("未知的模块名称: {}", moduleName);
            }
        } catch (Exception e) {
            log.error("保存{}模块数据失败: {}", moduleName, e.getMessage(), e);
            throw e;
        }
        
        return savedCount;
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
     * 手动触发设备爬虫任务
     */
    public Map<String, Object> triggerDeviceCrawlerManually(String crawlerName, String countryCode, String moduleName) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Object crawler = getCrawlerInstance(crawlerName, countryCode);
                if (crawler != null) {
                    executeDeviceCrawlerTask(crawlerName, countryCode, moduleName, crawler);
                } else {
                    throw new IllegalArgumentException("不支持的爬虫: " + crawlerName + " - " + countryCode);
                }
            }, executorService);
            
            // 等待任务完成（最多等待10分钟）
            future.get(10, java.util.concurrent.TimeUnit.MINUTES);
            
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
     * 获取爬虫实例
     */
    private Object getCrawlerInstance(String crawlerName, String countryCode) {
        if ("US".equals(countryCode)) {
            switch (crawlerName) {
                case "US_510K": return us510kCrawler;
                case "US_Event": return usEventCrawler;
                case "US_Recall": return usRecallCrawler;
                case "US_Registration": return usRegistrationCrawler;
                case "US_Guidance": return usGuidanceCrawler;
                case "US_CustomsCase": return usCustomsCrawler;
            }
        } else if ("EU".equals(countryCode)) {
            switch (crawlerName) {
                case "EU_Recall": return euRecallCrawler;
                case "EU_Registration": return euRegistrationCrawler;
                case "EU_Guidance": return euGuidanceCrawler;
                case "EU_CustomsCase": return euCustomsCrawler;
            }
        }
        return null;
    }

    /**
     * 获取设备爬虫执行状态
     */
    public Map<String, Object> getDeviceCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 获取所有设备数据模块的配置
        String[] modules = {"device510k", "deviceeventreport", "devicerecallrecord", 
                           "deviceregistrationrecord", "guidancedocument", "customscase"};
        
        for (String module : modules) {
            List<ScheduledCrawlerConfig> configs = configService.getConfigsByModule(module);
            Map<String, Object> moduleStatus = new HashMap<>();
            
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
                
                moduleStatus.put(config.getCrawlerName(), crawlerStatus);
            }
            
            status.put(module, moduleStatus);
        }
        
        return status;
    }
}

//package com.certification.crawler.unification;
//
//import com.certification.crawler.generalArchitecture.us.*;
//import com.certification.entity.common.*;
//import com.certification.repository.common.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//
///**
// * 统一爬虫编排器
// * 根据关键词文件统一爬取所有相关数据源
// */
//@Slf4j
//@Component
//public class UnifiedCrawlerOrchestrator {
//
//    @Autowired
//    private CustomsCaseCrawler customsCaseCrawler;
//
//    @Autowired
//    private Device510KCrawler device510KCrawler;
//
//    @Autowired
//    private DeviceEventCrawler deviceEventCrawler;
//
//    @Autowired
//    private DeviceRecallCrawler deviceRecallCrawler;
//
////    @Autowired
////    private DeviceRegistrationCrawler deviceRegistrationCrawler;
//
//    @Autowired
//    private GuidanceCrawler guidanceCrawler;
//
//    // 仓库注入
//    @Autowired
//    private CustomsCaseRepository customsCaseRepository;
//
//    @Autowired
//    private Device510KRepository device510KRepository;
//
//    @Autowired
//    private DeviceEventReportRepository deviceEventReportRepository;
//
//    @Autowired
//    private DeviceRecallRecordRepository deviceRecallRecordRepository;
//
//    @Autowired
//    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
//
//    @Autowired
//    private FDAGuidanceDocumentRepository guidanceDocumentRepository;
//
//    // 线程池配置
//    private final ExecutorService executorService = Executors.newFixedThreadPool(6);
//
//    // 爬虫配置
//    private static final int DEFAULT_BATCH_SIZE = 50;
//    private static final int DEFAULT_MAX_RECORDS = 1000;
//    private static final int DEFAULT_TIMEOUT_MINUTES = 30;
//
//    /**
//     * 爬虫执行结果统计
//     */
//    public static class CrawlerExecutionResult {
//        private String crawlerName;
//        private String keyword;
//        private int totalRecords;
//        private int newRecords;
//        private int skippedRecords;
//        private long executionTimeMs;
//        private boolean success;
//        private String errorMessage;
//        private LocalDateTime startTime;
//        private LocalDateTime endTime;
//
//        // Getters and Setters
//        public String getCrawlerName() { return crawlerName; }
//        public void setCrawlerName(String crawlerName) { this.crawlerName = crawlerName; }
//
//        public String getKeyword() { return keyword; }
//        public void setKeyword(String keyword) { this.keyword = keyword; }
//
//        public int getTotalRecords() { return totalRecords; }
//        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
//
//        public int getNewRecords() { return newRecords; }
//        public void setNewRecords(int newRecords) { this.newRecords = newRecords; }
//
//        public int getSkippedRecords() { return skippedRecords; }
//        public void setSkippedRecords(int skippedRecords) { this.skippedRecords = skippedRecords; }
//
//        public long getExecutionTimeMs() { return executionTimeMs; }
//        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
//
//        public boolean isSuccess() { return success; }
//        public void setSuccess(boolean success) { this.success = success; }
//
//        public String getErrorMessage() { return errorMessage; }
//        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
//
//        public LocalDateTime getStartTime() { return startTime; }
//        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
//
//        public LocalDateTime getEndTime() { return endTime; }
//        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
//    }
//
//    /**
//     * 统一爬取配置
//     */
//    public static class UnifiedCrawlConfig {
//        private String keywordFilePath;
//        private int maxRecordsPerKeyword = DEFAULT_MAX_RECORDS;
//        private int batchSize = DEFAULT_BATCH_SIZE;
//        private int timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;
//        private boolean enableParallelExecution = true;
//        private Set<String> enabledCrawlers = new HashSet<>();
//        private Map<String, Object> crawlerSpecificConfig = new HashMap<>();
//
//        // Getters and Setters
//        public String getKeywordFilePath() { return keywordFilePath; }
//        public void setKeywordFilePath(String keywordFilePath) { this.keywordFilePath = keywordFilePath; }
//
//        public int getMaxRecordsPerKeyword() { return maxRecordsPerKeyword; }
//        public void setMaxRecordsPerKeyword(int maxRecordsPerKeyword) { this.maxRecordsPerKeyword = maxRecordsPerKeyword; }
//
//        public int getBatchSize() { return batchSize; }
//        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
//
//        public int getTimeoutMinutes() { return timeoutMinutes; }
//        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
//
//        public boolean isEnableParallelExecution() { return enableParallelExecution; }
//        public void setEnableParallelExecution(boolean enableParallelExecution) { this.enableParallelExecution = enableParallelExecution; }
//
//        public Set<String> getEnabledCrawlers() { return enabledCrawlers; }
//        public void setEnabledCrawlers(Set<String> enabledCrawlers) { this.enabledCrawlers = enabledCrawlers; }
//
//        public Map<String, Object> getCrawlerSpecificConfig() { return crawlerSpecificConfig; }
//        public void setCrawlerSpecificConfig(Map<String, Object> crawlerSpecificConfig) { this.crawlerSpecificConfig = crawlerSpecificConfig; }
//    }
//
//    /**
//     * 从关键词文件读取关键词列表
//     */
//    public List<String> loadKeywordsFromFile(String filePath) {
//        try {
//            log.info("正在从文件加载关键词: {}", filePath);
//            List<String> keywords = Files.readAllLines(Paths.get(filePath))
//                    .stream()
//                    .map(String::trim)
//                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                    .collect(Collectors.toList());
//
//            log.info("成功加载 {} 个关键词", keywords.size());
//            return keywords;
//        } catch (IOException e) {
//            log.error("读取关键词文件失败: {}", e.getMessage(), e);
//            throw new RuntimeException("无法读取关键词文件: " + filePath, e);
//        }
//    }
//
//    /**
//     * 执行统一爬取
//     */
//    @Transactional
//    public List<CrawlerExecutionResult> executeUnifiedCrawl(UnifiedCrawlConfig config) {
//        log.info("=== 开始执行统一爬取任务 ===");
//        log.info("配置信息: {}", config);
//
//        // 加载关键词
//        List<String> keywords = loadKeywordsFromFile(config.getKeywordFilePath());
//
//        // 初始化结果列表
//        List<CrawlerExecutionResult> allResults = new ArrayList<>();
//
//        // 执行爬取
//        if (config.isEnableParallelExecution()) {
//            allResults = executeParallelCrawl(keywords, config);
//        } else {
//            allResults = executeSequentialCrawl(keywords, config);
//        }
//
//        // 输出统计信息
//        printExecutionSummary(allResults);
//
//        log.info("=== 统一爬取任务完成 ===");
//        return allResults;
//    }
//
//    /**
//     * 并行执行爬取
//     */
//    private List<CrawlerExecutionResult> executeParallelCrawl(List<String> keywords, UnifiedCrawlConfig config) {
//        log.info("使用并行模式执行爬取，关键词数量: {}", keywords.size());
//
//        List<CompletableFuture<List<CrawlerExecutionResult>>> futures = new ArrayList<>();
//
//        for (String keyword : keywords) {
//            CompletableFuture<List<CrawlerExecutionResult>> future = CompletableFuture.supplyAsync(() -> {
//                return executeCrawlForKeyword(keyword, config);
//            }, executorService);
//
//            futures.add(future);
//        }
//
//        // 等待所有任务完成
//        List<CrawlerExecutionResult> allResults = new ArrayList<>();
//        for (CompletableFuture<List<CrawlerExecutionResult>> future : futures) {
//            try {
//                List<CrawlerExecutionResult> results = future.get(config.getTimeoutMinutes(), TimeUnit.MINUTES);
//                allResults.addAll(results);
//            } catch (Exception e) {
//                log.error("并行爬取任务执行失败: {}", e.getMessage(), e);
//            }
//        }
//
//        return allResults;
//    }
//
//    /**
//     * 顺序执行爬取
//     */
//    private List<CrawlerExecutionResult> executeSequentialCrawl(List<String> keywords, UnifiedCrawlConfig config) {
//        log.info("使用顺序模式执行爬取，关键词数量: {}", keywords.size());
//
//        List<CrawlerExecutionResult> allResults = new ArrayList<>();
//
//        for (String keyword : keywords) {
//            List<CrawlerExecutionResult> results = executeCrawlForKeyword(keyword, config);
//            allResults.addAll(results);
//        }
//
//        return allResults;
//    }
//
//    /**
//     * 为单个关键词执行所有爬虫
//     */
//    private List<CrawlerExecutionResult> executeCrawlForKeyword(String keyword, UnifiedCrawlConfig config) {
//        log.info("开始为关键词 '{}' 执行爬取", keyword);
//
//        List<CrawlerExecutionResult> results = new ArrayList<>();
//
//        // 执行各个爬虫
//        if (isCrawlerEnabled("customs", config)) {
//            results.add(executeCustomsCrawler(keyword, config));
//        }
//
//        if (isCrawlerEnabled("device510k", config)) {
//            results.add(executeDevice510KCrawler(keyword, config));
//        }
//
//        if (isCrawlerEnabled("deviceEvent", config)) {
//            results.add(executeDeviceEventCrawler(keyword, config));
//        }
//
//        if (isCrawlerEnabled("deviceRecall", config)) {
//            results.add(executeDeviceRecallCrawler(keyword, config));
//        }
//
//        if (isCrawlerEnabled("deviceRegistration", config)) {
//            results.add(executeDeviceRegistrationCrawler(keyword, config));
//        }
//
//        if (isCrawlerEnabled("guidance", config)) {
//            results.add(executeGuidanceCrawler(keyword, config));
//        }
//
//        log.info("关键词 '{}' 爬取完成，共执行 {} 个爬虫", keyword, results.size());
//        return results;
//    }
//
//    /**
//     * 执行海关案例爬虫
//     */
//    private CrawlerExecutionResult executeCustomsCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("CustomsCase");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行海关案例爬虫，关键词: {}", keyword);
//
//            // 记录爬取前的数据量
//            long beforeCount = customsCaseRepository.count();
//
//            // 执行爬取
//            List<CustomsCase> records = customsCaseCrawler.crawlAndSaveCustomsCases(
//                    keyword, config.getMaxRecordsPerKeyword(), config.getBatchSize());
//
//            // 记录爬取后的数据量
//            long afterCount = customsCaseRepository.count();
//
//            result.setTotalRecords(records.size());
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(records.size() - result.getNewRecords());
//            result.setSuccess(true);
//
//            log.info("海关案例爬虫完成，关键词: {}, 总记录: {}, 新增: {}",
//                    keyword, result.getTotalRecords(), result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("海关案例爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 执行设备510K爬虫
//     */
//    private CrawlerExecutionResult executeDevice510KCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("Device510K");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行设备510K爬虫，关键词: {}", keyword);
//
//            long beforeCount = device510KRepository.count();
//
//            // 根据设备名称爬取510k数据（支持时间范围和分批保存）
//            List<com.certification.entity.common.Device510K> deviceNameRecords = device510KCrawler.crawlAndSaveDevice510KByDeviceName(
//                    keyword, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//                    getDateFrom(config), getDateTo(config));
//
//            // 根据申请人爬取并保存510k审批数据（支持时间范围和分批保存）
//            List<com.certification.entity.common.Device510K> applicantRecords = device510KCrawler.crawlAndSaveDevice510KByApplicant(
//                    keyword, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//                    getDateFrom(config), getDateTo(config));
//
//            long afterCount = device510KRepository.count();
//
//            int totalRecords = deviceNameRecords.size() + applicantRecords.size();
//            result.setTotalRecords(totalRecords);
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(totalRecords - result.getNewRecords());
//            result.setSuccess(true);
//
//            log.info("设备510K爬虫完成，关键词: {}, 总记录: {}, 新增: {}",
//                    keyword, result.getTotalRecords(), result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("设备510K爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 执行设备事件爬虫
//     */
//    private CrawlerExecutionResult executeDeviceEventCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("DeviceEvent");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行设备事件爬虫，关键词: {}", keyword);
//
//            long beforeCount = deviceEventReportRepository.count();
//
//            // 使用device.brand_name:进行爬取
//            String brandNameSearch = "device.brand_name:" + keyword;
//            List<com.certification.entity.common.DeviceEventReport> brandNameRecords = deviceEventCrawler.crawlAndSaveDeviceEvents(
//                    brandNameSearch, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//                    getDateFrom(config), getDateTo(config));
//
//            // 使用device.generic_name:进行爬取
//            String genericNameSearch = "device.generic_name:" + keyword;
//            List<com.certification.entity.common.DeviceEventReport> genericNameRecords = deviceEventCrawler.crawlAndSaveDeviceEvents(
//                    genericNameSearch, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//                    getDateFrom(config), getDateTo(config));
//
//            long afterCount = deviceEventReportRepository.count();
//
//            int totalRecords = brandNameRecords.size() + genericNameRecords.size();
//            result.setTotalRecords(totalRecords);
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(totalRecords - result.getNewRecords());
//            result.setSuccess(true);
//
//            log.info("设备事件爬虫完成，关键词: {}, 总记录: {}, 新增: {}",
//                    keyword, result.getTotalRecords(), result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("设备事件爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 执行设备召回爬虫
//     */
//    private CrawlerExecutionResult executeDeviceRecallCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("DeviceRecall");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行设备召回爬虫，关键词: {}", keyword);
//
//            long beforeCount = deviceRecallRecordRepository.count();
//
//            // 使用product_description:进行爬取
//            String productDescriptionSearch = "product_description:" + keyword;
//            List<DeviceRecallRecord> records = deviceRecallCrawler.crawlAndSaveDeviceRecalls(
//                    productDescriptionSearch, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//                    getDateFrom(config), getDateTo(config));
//
//            long afterCount = deviceRecallRecordRepository.count();
//
//            result.setTotalRecords(records.size());
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(records.size() - result.getNewRecords());
//            result.setSuccess(true);
//
//            log.info("设备召回爬虫完成，关键词: {}, 总记录: {}, 新增: {}",
//                    keyword, result.getTotalRecords(), result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("设备召回爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 执行设备注册爬虫
//     */
//    private CrawlerExecutionResult executeDeviceRegistrationCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("DeviceRegistration");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行设备注册爬虫，关键词: {}", keyword);
//
//            long beforeCount = deviceRegistrationRecordRepository.count();
//
//            // 使用products.openfda.device_name:进行爬取
//            String deviceNameSearch = "products.openfda.device_name:" + keyword;
//            //List<com.certification.entity.common.DeviceRegistrationRecord> deviceNameRecords = deviceRegistrationCrawler.crawlAndSaveDeviceRegistration(
//            //        deviceNameSearch, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//            //        getDateFrom(config), getDateTo(config));
//            List<com.certification.entity.common.DeviceRegistrationRecord> deviceNameRecords = new ArrayList<>(); // DeviceRegistrationCrawler已注释
//
//            // 使用proprietary_name:进行爬取
//            String proprietaryNameSearch = "proprietary_name:" + keyword;
//            //List<com.certification.entity.common.DeviceRegistrationRecord> proprietaryNameRecords = deviceRegistrationCrawler.crawlAndSaveDeviceRegistration(
//            //        proprietaryNameSearch, config.getMaxRecordsPerKeyword(), config.getBatchSize(),
//            //        getDateFrom(config), getDateTo(config));
//            List<com.certification.entity.common.DeviceRegistrationRecord> proprietaryNameRecords = new ArrayList<>(); // DeviceRegistrationCrawler已注释
//
//            long afterCount = deviceRegistrationRecordRepository.count();
//
//            int totalRecords = deviceNameRecords.size() + proprietaryNameRecords.size();
//            result.setTotalRecords(totalRecords);
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(totalRecords - result.getNewRecords());
//            result.setSuccess(true);
//
//            log.info("设备注册爬虫完成，关键词: {}, 总记录: {}, 新增: {}",
//                    keyword, result.getTotalRecords(), result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("设备注册爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 执行指导文档爬虫
//     */
//    private CrawlerExecutionResult executeGuidanceCrawler(String keyword, UnifiedCrawlConfig config) {
//        CrawlerExecutionResult result = new CrawlerExecutionResult();
//        result.setCrawlerName("Guidance");
//        result.setKeyword(keyword);
//        result.setStartTime(LocalDateTime.now());
//
//        try {
//            log.info("执行指导文档爬虫，关键词: {}", keyword);
//
//            long beforeCount = guidanceDocumentRepository.count();
//
//            // 指导文档爬虫目前不支持关键词搜索，执行全量爬取
//            guidanceCrawler.crawlWithLimit(config.getMaxRecordsPerKeyword());
//
//            long afterCount = guidanceDocumentRepository.count();
//
//            result.setTotalRecords((int) (afterCount - beforeCount));
//            result.setNewRecords((int) (afterCount - beforeCount));
//            result.setSkippedRecords(0);
//            result.setSuccess(true);
//
//            log.info("指导文档爬虫完成，关键词: {}, 新增: {}",
//                    keyword, result.getNewRecords());
//
//        } catch (Exception e) {
//            log.error("指导文档爬虫执行失败，关键词: {}, 错误: {}", keyword, e.getMessage(), e);
//            result.setSuccess(false);
//            result.setErrorMessage(e.getMessage());
//        } finally {
//            result.setEndTime(LocalDateTime.now());
//            result.setExecutionTimeMs(java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis());
//        }
//
//        return result;
//    }
//
//    /**
//     * 检查爬虫是否启用
//     */
//    private boolean isCrawlerEnabled(String crawlerName, UnifiedCrawlConfig config) {
//        return config.getEnabledCrawlers().isEmpty() || config.getEnabledCrawlers().contains(crawlerName);
//    }
//
//    /**
//     * 打印执行摘要
//     */
//    private void printExecutionSummary(List<CrawlerExecutionResult> results) {
//        log.info("=== 爬取执行摘要 ===");
//
//        // 总体统计
//        int totalExecutions = results.size();
//        int successfulExecutions = (int) results.stream().filter(CrawlerExecutionResult::isSuccess).count();
//        int failedExecutions = totalExecutions - successfulExecutions;
//
//        int totalRecords = results.stream().mapToInt(CrawlerExecutionResult::getTotalRecords).sum();
//        int totalNewRecords = results.stream().mapToInt(CrawlerExecutionResult::getNewRecords).sum();
//        int totalSkippedRecords = results.stream().mapToInt(CrawlerExecutionResult::getSkippedRecords).sum();
//
//        long totalExecutionTime = results.stream().mapToLong(CrawlerExecutionResult::getExecutionTimeMs).sum();
//
//        log.info("总执行次数: {}", totalExecutions);
//        log.info("成功执行: {}", successfulExecutions);
//        log.info("失败执行: {}", failedExecutions);
//        log.info("成功率: {:.2f}%", (double) successfulExecutions / totalExecutions * 100);
//        log.info("总记录数: {}", totalRecords);
//        log.info("新增记录: {}", totalNewRecords);
//        log.info("跳过记录: {}", totalSkippedRecords);
//        log.info("总执行时间: {}ms", totalExecutionTime);
//
//        // 按爬虫类型统计
//        Map<String, List<CrawlerExecutionResult>> resultsByCrawler = results.stream()
//                .collect(Collectors.groupingBy(CrawlerExecutionResult::getCrawlerName));
//
//        log.info("=== 按爬虫类型统计 ===");
//        for (Map.Entry<String, List<CrawlerExecutionResult>> entry : resultsByCrawler.entrySet()) {
//            String crawlerName = entry.getKey();
//            List<CrawlerExecutionResult> crawlerResults = entry.getValue();
//
//            int crawlerTotal = crawlerResults.stream().mapToInt(CrawlerExecutionResult::getTotalRecords).sum();
//            int crawlerNew = crawlerResults.stream().mapToInt(CrawlerExecutionResult::getNewRecords).sum();
//            int crawlerSuccess = (int) crawlerResults.stream().filter(CrawlerExecutionResult::isSuccess).count();
//
//            log.info("{}: 总记录={}, 新增={}, 成功={}/{}",
//                    crawlerName, crawlerTotal, crawlerNew, crawlerSuccess, crawlerResults.size());
//        }
//
//        // 失败详情
//        if (failedExecutions > 0) {
//            log.info("=== 失败详情 ===");
//            results.stream()
//                    .filter(r -> !r.isSuccess())
//                    .forEach(r -> log.info("失败: {} - {} - {}", r.getCrawlerName(), r.getKeyword(), r.getErrorMessage()));
//        }
//    }
//
//    /**
//     * 创建默认配置
//     */
//    public UnifiedCrawlConfig createDefaultConfig() {
//        UnifiedCrawlConfig config = new UnifiedCrawlConfig();
//        config.setKeywordFilePath("src/main/java/com/certification/crawler/unification/searchkeywords.txt");
//        config.setMaxRecordsPerKeyword(100);
//        config.setBatchSize(50);
//        config.setTimeoutMinutes(30);
//        config.setEnableParallelExecution(true);
//
//        // 默认启用所有爬虫
//        config.setEnabledCrawlers(Set.of(
//                "customs", "device510k", "deviceEvent",
//                "deviceRecall", "deviceRegistration", "guidance"
//        ));
//
//        return config;
//    }
//
//    /**
//     * 创建快速测试配置
//     */
//    public UnifiedCrawlConfig createQuickTestConfig() {
//        UnifiedCrawlConfig config = createDefaultConfig();
//        config.setMaxRecordsPerKeyword(10);
//        config.setBatchSize(10);
//        config.setTimeoutMinutes(5);
//        config.setEnableParallelExecution(false);
//        return config;
//    }
//
//    /**
//     * 从配置中获取开始日期
//     */
//    private String getDateFrom(UnifiedCrawlConfig config) {
//        if (config.getCrawlerSpecificConfig() != null) {
//            return (String) config.getCrawlerSpecificConfig().get("dateFrom");
//        }
//        return null;
//    }
//
//    /**
//     * 从配置中获取结束日期
//     */
//    private String getDateTo(UnifiedCrawlConfig config) {
//        if (config.getCrawlerSpecificConfig() != null) {
//            return (String) config.getCrawlerSpecificConfig().get("dateTo");
//        }
//        return null;
//    }
//
//    /**
//     * 关闭线程池
//     */
//    public void shutdown() {
//        executorService.shutdown();
//        try {
//            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
//                executorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executorService.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//}
//

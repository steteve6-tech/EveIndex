package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.tw.TwRegistration;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 台湾注册记录爬虫适配器
 * 集成TwRegistration爬虫到统一爬虫管理系统
 */
@Slf4j
@Component
public class TwRegistrationAdapter implements ICrawlerExecutor {

    @Autowired
    private TwRegistration crawler;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行TW_Registration爬虫，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取4个搜索参数
            List<String> applicantNames = fieldKeywords.getOrDefault("applicantNames", List.of());
            List<String> factoryNames = fieldKeywords.getOrDefault("factoryNames", List.of());
            List<String> prodNameCList = fieldKeywords.getOrDefault("prodNameC", List.of());
            List<String> prodNameEList = fieldKeywords.getOrDefault("prodNameE", List.of());
            
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;

            // 检查是否至少提供了一个参数
            if (applicantNames.isEmpty() && factoryNames.isEmpty() && 
                prodNameCList.isEmpty() && prodNameEList.isEmpty()) {
                throw new IllegalArgumentException("必须提供至少一个搜索参数: applicantNames, factoryNames, prodNameC 或 prodNameE");
            }

            String resultMsg;

            // 如果只有一个参数列表有值且只有一个元素，直接搜索
            if (getTotalKeywordCount(applicantNames, factoryNames, prodNameCList, prodNameEList) == 1) {
                String applicant = getFirstOrEmpty(applicantNames);
                String factory = getFirstOrEmpty(factoryNames);
                String nameC = getFirstOrEmpty(prodNameCList);
                String nameE = getFirstOrEmpty(prodNameEList);
                
                resultMsg = crawler.crawl(applicant, factory, nameC, nameE, maxRecords);
            } else {
                // 批量搜索
                resultMsg = executeBatchSearch(applicantNames, factoryNames, prodNameCList, prodNameEList, maxRecords);
            }

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);

            return CrawlerResult.fromString(resultMsg)
                    .setStartTime(result.getStartTime())
                    .setEndTime(result.getEndTime())
                    .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("TW_Registration爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有关键词列表的总数量
     */
    private int getTotalKeywordCount(List<String>... lists) {
        int total = 0;
        for (List<String> list : lists) {
            total += list.size();
        }
        return total;
    }
    
    /**
     * 获取列表的第一个元素，如果为空则返回空字符串
     */
    private String getFirstOrEmpty(List<String> list) {
        return list.isEmpty() ? "" : list.get(0);
    }

    /**
     * 批量搜索（按参数分组依次执行，每次只使用一个参数的关键词列表）
     * 执行逻辑：
     * 1. 先用applicantNames列表依次搜索（其他参数为null）
     * 2. 再用factoryNames列表依次搜索（其他参数为null）
     * 3. 再用prodNameCList列表依次搜索（其他参数为null）
     * 4. 最后用prodNameEList列表依次搜索（其他参数为null）
     */
    private String executeBatchSearch(List<String> applicantNames, List<String> factoryNames,
                                      List<String> prodNameCList, List<String> prodNameEList, int maxRecords) {
        log.info("开始批量搜索（按参数分组模式）");
        log.info("  - 申请人数量: {}", applicantNames.size());
        log.info("  - 制造商数量: {}", factoryNames.size());
        log.info("  - 中文产品名数量: {}", prodNameCList.size());
        log.info("  - 英文产品名数量: {}", prodNameEList.size());

        List<CompletableFuture<String>> futures = new java.util.ArrayList<>();
        int totalSearches = 0;

        // 第1组：使用申请人列表搜索，其他参数为null
        if (!applicantNames.isEmpty()) {
            log.info("执行第1组搜索：申请人列表（{}个关键词）", applicantNames.size());
            for (String applicant : applicantNames) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("🔍 搜索 [申请人]: {}", applicant);
                        String result = crawler.crawl(applicant, "", "", "", maxRecords);
                        log.info("✅ 搜索结果 [申请人={}]: {}", applicant, result);
                        return result;
                    } catch (Exception e) {
                        log.error("❌ 搜索失败 [申请人={}]", applicant, e);
                        return "搜索失败: " + e.getMessage();
                    }
                }, executorService));
                totalSearches++;
            }
        }

        // 第2组：使用制造商列表搜索，其他参数为null
        if (!factoryNames.isEmpty()) {
            log.info("执行第2组搜索：制造商列表（{}个关键词）", factoryNames.size());
            for (String factory : factoryNames) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("🔍 搜索 [制造商]: {}", factory);
                        String result = crawler.crawl("", factory, "", "", maxRecords);
                        log.info("✅ 搜索结果 [制造商={}]: {}", factory, result);
                        return result;
                    } catch (Exception e) {
                        log.error("❌ 搜索失败 [制造商={}]", factory, e);
                        return "搜索失败: " + e.getMessage();
                    }
                }, executorService));
                totalSearches++;
            }
        }

        // 第3组：使用中文产品名列表搜索，其他参数为null
        if (!prodNameCList.isEmpty()) {
            log.info("执行第3组搜索：中文产品名列表（{}个关键词）", prodNameCList.size());
            for (String nameC : prodNameCList) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("🔍 搜索 [中文产品名]: {}", nameC);
                        String result = crawler.crawl("", "", nameC, "", maxRecords);
                        log.info("✅ 搜索结果 [中文产品名={}]: {}", nameC, result);
                        return result;
                    } catch (Exception e) {
                        log.error("❌ 搜索失败 [中文产品名={}]", nameC, e);
                        return "搜索失败: " + e.getMessage();
                    }
                }, executorService));
                totalSearches++;
            }
        }

        // 第4组：使用英文产品名列表搜索，其他参数为null
        if (!prodNameEList.isEmpty()) {
            log.info("执行第4组搜索：英文产品名列表（{}个关键词）", prodNameEList.size());
            for (String nameE : prodNameEList) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("🔍 搜索 [英文产品名]: {}", nameE);
                        String result = crawler.crawl("", "", "", nameE, maxRecords);
                        log.info("✅ 搜索结果 [英文产品名={}]: {}", nameE, result);
                        return result;
                    } catch (Exception e) {
                        log.error("❌ 搜索失败 [英文产品名={}]", nameE, e);
                        return "搜索失败: " + e.getMessage();
                    }
                }, executorService));
                totalSearches++;
            }
        }

        log.info("总共将执行 {} 次搜索", totalSearches);

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // 详细统计每个搜索结果
        int successCount = 0;
        int failedCount = 0;
        int noDataCount = 0;

        for (String result : results) {
            if (result.contains("保存完成")) {
                successCount++;
            } else if (result.contains("未找到任何注册记录")) {
                noDataCount++;
            } else if (result.contains("搜索失败") || result.contains("爬取失败")) {
                failedCount++;
            }
        }

        log.info("📊 搜索统计: 成功={}, 无数据={}, 失败={}", successCount, noDataCount, failedCount);

        return String.format("批量搜索完成（按参数分组），总搜索次数: %d, 成功: %d, 无数据: %d, 失败: %d",
                           totalSearches, successCount, noDataCount, failedCount);
    }
    
    /**
     * 批量公司搜索（兼容旧方法）
     */
    @Deprecated
    private String executeBatchCompanySearch(List<String> companyNames, int maxRecords) {
        log.info("开始批量公司搜索，公司数量: {}", companyNames.size());

        List<CompletableFuture<String>> futures = companyNames.stream()
                .map(companyName -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("搜索公司: {}", companyName);
                        return crawler.crawlByApplicant(companyName, maxRecords);
                    } catch (Exception e) {
                        log.error("搜索公司失败: {}", companyName, e);
                        return "搜索失败: " + companyName;
                    }
                }, executorService))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        int successCount = (int) results.stream()
                .filter(resultMsg -> resultMsg.contains("保存完成"))
                .count();

        return String.format("批量公司搜索完成，成功: %d/%d", successCount, companyNames.size());
    }

    /**
     * 批量关键词搜索
     */
    private String executeBatchKeywordSearch(List<String> keywords, int maxRecords) {
        log.info("开始批量关键词搜索，关键词数量: {}", keywords.size());

        List<CompletableFuture<String>> futures = keywords.stream()
                .map(keyword -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("搜索关键词: {}", keyword);
                        if (isEnglishKeyword(keyword)) {
                            return crawler.crawlByProductNameEnglish(keyword, maxRecords);
                        } else {
                            return crawler.crawlByProductNameChinese(keyword, maxRecords);
                        }
                    } catch (Exception e) {
                        log.error("搜索关键词失败: {}", keyword, e);
                        return "搜索失败: " + keyword;
                    }
                }, executorService))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        int successCount = (int) results.stream()
                .filter(resultMsg -> resultMsg.contains("保存完成"))
                .count();

        return String.format("批量关键词搜索完成，成功: %d/%d", successCount, keywords.size());
    }

    /**
     * 判断是否为英文关键词
     */
    private boolean isEnglishKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        
        // 如果包含中文字符，认为是中文关键词
        return !keyword.matches(".*[\\u4e00-\\u9fa5].*");
    }

    /**
     * 组合搜索（申请人 + 产品名称）
     */
    public CrawlerResult executeCombinedSearch(CrawlerParams params) {
        log.info("执行TW_Registration组合搜索，参数: {}", params);

        CrawlerResult result = new CrawlerResult().markStart();

        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            List<String> companyNames = fieldKeywords.getOrDefault("companyNames", List.of());
            List<String> keywords = new java.util.ArrayList<>();
            if (fieldKeywords.containsKey("keywords")) {
                keywords.addAll(fieldKeywords.get("keywords"));
            }
            if (fieldKeywords.containsKey("deviceNames")) {
                keywords.addAll(fieldKeywords.get("deviceNames"));
            }
            
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : 100;

            if (companyNames.isEmpty() || keywords.isEmpty()) {
                throw new IllegalArgumentException("组合搜索需要同时提供公司名称和关键词");
            }

            // 执行组合搜索
            List<CompletableFuture<String>> futures = new java.util.ArrayList<>();
            
            for (String companyName : companyNames) {
                for (String keyword : keywords) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            if (isEnglishKeyword(keyword)) {
                                return crawler.crawlByCombined(companyName, keyword, "", maxRecords);
                            } else {
                                return crawler.crawlByCombined(companyName, "", keyword, maxRecords);
                            }
                        } catch (Exception e) {
                            log.error("组合搜索失败: 公司={}, 关键词={}", companyName, keyword, e);
                            return "组合搜索失败: " + companyName + " + " + keyword;
                        }
                    }, executorService));
                }
            }

            List<String> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            int successCount = (int) results.stream()
                    .filter(resultMsg -> resultMsg.contains("保存完成"))
                    .count();

            result.markEnd();
            result.setSuccess(true);
            result.setMessage(String.format("组合搜索完成，成功: %d/%d", successCount, results.size()));

            return CrawlerResult.fromString(result.getMessage())
                    .setStartTime(result.getStartTime())
                    .setEndTime(result.getEndTime())
                    .setDurationSeconds(result.getDurationSeconds());

        } catch (Exception e) {
            log.error("TW_Registration组合搜索失败", e);
            result.markEnd();
            return CrawlerResult.failure("组合搜索失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCrawlerName() {
        return "TW_Registration";
    }

    @Override
    public String getCrawlerType() {
        return "REGISTRATION";
    }

    @Override
    public String getCountryCode() {
        return "TW";
    }

    @Override
    public String getDescription() {
        return "台湾FDA医疗器械注册记录爬虫 - 爬取台湾食品药物管理署的医疗器械许可数据";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    // 移除@Override注解，因为接口中没有这个方法
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("maxRecords", 100);
        params.put("searchMode", "applicant"); // applicant, product, combined
        params.put("language", "auto"); // auto, chinese, english
        return params;
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

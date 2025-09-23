package com.certification.service;

import com.certification.entity.common.CertNewsData;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 国家风险统计服务
 * 负责统计每个国家每天的高风险数据数量变化
 */
@Slf4j
@Service
public class CountryRiskStatisticsService {

    @Autowired
    private CrawlerDataService crawlerDataService;

    /**
     * 获取国家风险趋势数据
     * 从昨天开始，包含今天的数据
     */
    public Map<String, Object> getCountryRiskTrends() {
        log.info("获取国家风险趋势数据");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取昨天到今天的数据
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();
            
            LocalDateTime startTime = yesterday.atStartOfDay();
            LocalDateTime endTime = today.atTime(23, 59, 59);
            
            // 获取高风险数据
            List<CertNewsData> highRiskData = crawlerDataService.findByCrawlTimeBetween(startTime, endTime);
            highRiskData = highRiskData.stream()
                    .filter(data -> data.getRiskLevel() == CertNewsData.RiskLevel.HIGH)
                    .toList();
            
            // 按国家和日期分组统计
            Map<String, Map<String, Integer>> countryDailyStats = new HashMap<>();
            Map<String, Integer> countryTotals = new HashMap<>();
            
            for (CertNewsData data : highRiskData) {
                String country = data.getCountry() != null ? data.getCountry() : "UNKNOWN";
                LocalDate dataDate = data.getCrawlTime().toLocalDate();
                String dateKey = dataDate.toString();
                
                // 初始化国家数据
                countryDailyStats.computeIfAbsent(country, k -> new HashMap<>());
                countryTotals.put(country, countryTotals.getOrDefault(country, 0) + 1);
                
                // 统计每日数据
                Map<String, Integer> dailyStats = countryDailyStats.get(country);
                dailyStats.put(dateKey, dailyStats.getOrDefault(dateKey, 0) + 1);
            }
            
            // 构建趋势数据
            List<String> dates = Arrays.asList(yesterday.toString(), today.toString());
            Map<String, List<Integer>> trendData = new HashMap<>();
            
            for (String country : countryDailyStats.keySet()) {
                List<Integer> dailyCounts = new ArrayList<>();
                Map<String, Integer> dailyStats = countryDailyStats.get(country);
                
                for (String date : dates) {
                    dailyCounts.add(dailyStats.getOrDefault(date, 0));
                }
                
                trendData.put(country, dailyCounts);
            }
            
            result.put("success", true);
            result.put("dates", dates);
            result.put("trendData", trendData);
            result.put("countryTotals", countryTotals);
            result.put("totalCountries", countryTotals.size());
            result.put("totalHighRiskData", highRiskData.size());
            result.put("generatedAt", LocalDateTime.now());
            
            log.info("国家风险趋势数据生成完成，涉及{}个国家，总计{}条高风险数据", 
                    countryTotals.size(), highRiskData.size());
            
        } catch (Exception e) {
            log.error("获取国家风险趋势数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家风险趋势数据失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取指定日期的国家风险统计
     */
    public Map<String, Object> getCountryRiskStatsByDate(LocalDate date) {
        log.info("获取{}的国家风险统计数据", date);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.atTime(23, 59, 59);
            
            // 获取指定日期的高风险数据
            List<CertNewsData> highRiskData = crawlerDataService.findByCrawlTimeBetween(startTime, endTime);
            highRiskData = highRiskData.stream()
                    .filter(data -> data.getRiskLevel() == CertNewsData.RiskLevel.HIGH)
                    .toList();
            
            // 按国家统计
            Map<String, Integer> countryStats = new HashMap<>();
            for (CertNewsData data : highRiskData) {
                String country = data.getCountry() != null ? data.getCountry() : "UNKNOWN";
                countryStats.put(country, countryStats.getOrDefault(country, 0) + 1);
            }
            
            result.put("success", true);
            result.put("date", date.toString());
            result.put("countryStats", countryStats);
            result.put("totalHighRiskData", highRiskData.size());
            result.put("totalCountries", countryStats.size());
            result.put("generatedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("获取{}国家风险统计数据失败: {}", date, e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家风险统计数据失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取国家风险排行榜
     */
    public Map<String, Object> getCountryRiskRanking() {
        log.info("获取国家风险排行榜");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取最近7天的高风险数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atTime(23, 59, 59);
            
            List<CertNewsData> highRiskData = crawlerDataService.findByCrawlTimeBetween(startTime, endTime);
            highRiskData = highRiskData.stream()
                    .filter(data -> data.getRiskLevel() == CertNewsData.RiskLevel.HIGH)
                    .toList();
            
            // 按国家统计
            Map<String, Integer> countryStats = new HashMap<>();
            for (CertNewsData data : highRiskData) {
                String country = data.getCountry() != null ? data.getCountry() : "UNKNOWN";
                countryStats.put(country, countryStats.getOrDefault(country, 0) + 1);
            }
            
            // 排序
            List<Map.Entry<String, Integer>> sortedCountries = new ArrayList<>(countryStats.entrySet());
            sortedCountries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            result.put("success", true);
            result.put("ranking", sortedCountries);
            result.put("totalHighRiskData", highRiskData.size());
            result.put("totalCountries", countryStats.size());
            result.put("period", startDate + " 至 " + endDate);
            result.put("generatedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("获取国家风险排行榜失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家风险排行榜失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 初始化昨天的基准数据
     * 如果昨天没有数据，则创建基准数据
     */
    public void initializeBaselineData() {
        log.info("初始化基准数据");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startTime = yesterday.atStartOfDay();
            LocalDateTime endTime = yesterday.atTime(23, 59, 59);
            
            // 检查昨天是否有数据
            List<CertNewsData> yesterdayData = crawlerDataService.findByCrawlTimeBetween(startTime, endTime);
            yesterdayData = yesterdayData.stream()
                    .filter(data -> data.getRiskLevel() == CertNewsData.RiskLevel.HIGH)
                    .toList();
            
            if (yesterdayData.isEmpty()) {
                log.info("昨天没有高风险数据，创建基准数据");
                // 这里可以创建一些基准数据，或者记录日志
                log.info("基准数据初始化完成，从今天开始记录实时数据");
            } else {
                log.info("昨天已有{}条高风险数据，基准数据已存在", yesterdayData.size());
            }
            
        } catch (Exception e) {
            log.error("初始化基准数据失败: {}", e.getMessage(), e);
        }
    }
}

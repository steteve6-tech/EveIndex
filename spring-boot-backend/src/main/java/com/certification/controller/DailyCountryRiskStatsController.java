package com.certification.controller;

import com.certification.service.DailyCountryRiskStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 每日国家高风险数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/daily-country-risk-stats")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
public class DailyCountryRiskStatsController {

    @Autowired
    private DailyCountryRiskStatsService dailyCountryRiskStatsService;

    /**
     * 获取指定日期范围内各国高风险数据统计
     */
    @GetMapping("/country-stats")
    public ResponseEntity<Map<String, Object>> getCountryStatsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = dailyCountryRiskStatsService
                .getCountryHighRiskStatsByDateRange(startDate, endDate);
            
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取各国高风险数据统计成功");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取各国高风险数据统计失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取各国高风险数据统计失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定国家在指定日期范围内的高风险数据趋势
     */
    @GetMapping("/country-trend")
    public ResponseEntity<Map<String, Object>> getCountryTrend(
            @RequestParam String country,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> trend = dailyCountryRiskStatsService
                .getCountryHighRiskTrend(country, startDate, endDate);
            
            response.put("success", true);
            response.put("data", trend);
            response.put("message", "获取国家高风险数据趋势成功");
            response.put("country", country);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取国家高风险数据趋势失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家高风险数据趋势失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定日期范围内各国高风险数据总和
     */
    @GetMapping("/country-sum")
    public ResponseEntity<Map<String, Object>> getCountrySumByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> sumData = dailyCountryRiskStatsService
                .getCountryHighRiskSumByDateRange(startDate, endDate);
            
            response.put("success", true);
            response.put("data", sumData);
            response.put("message", "获取各国高风险数据总和成功");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取各国高风险数据总和失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取各国高风险数据总和失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取最新的统计数据
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = dailyCountryRiskStatsService.getLatestStats();
            
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取最新统计数据成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取最新统计数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取最新统计数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定日期的统计数据
     */
    @GetMapping("/by-date")
    public ResponseEntity<Map<String, Object>> getStatsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = dailyCountryRiskStatsService.getStatsByDate(statDate);
            
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取指定日期统计数据成功");
            response.put("statDate", statDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取指定日期统计数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取指定日期统计数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 手动触发统计指定日期的高风险数据
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyCountryRiskStatsService.calculateDailyStats(statDate);
            
            response.put("success", true);
            response.put("message", "统计指定日期高风险数据成功");
            response.put("statDate", statDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("统计指定日期高风险数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "统计指定日期高风险数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 手动触发统计昨天的高风险数据
     */
    @PostMapping("/calculate-yesterday")
    public ResponseEntity<Map<String, Object>> calculateYesterdayStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyCountryRiskStatsService.calculateYesterdayStats();
            
            response.put("success", true);
            response.put("message", "统计昨天高风险数据成功");
            response.put("data", Map.of("statDate", LocalDate.now().minusDays(1)));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("统计昨天高风险数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "统计昨天高风险数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 设置今天的数据与昨天一致
     */
    @PostMapping("/set-today-same-as-yesterday")
    public ResponseEntity<Map<String, Object>> setTodayDataSameAsYesterday() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyCountryRiskStatsService.setTodayDataSameAsYesterday();
            
            LocalDate today = LocalDate.now();
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            response.put("success", true);
            response.put("message", "设置今天的数据与昨天一致成功");
            response.put("data", Map.of(
                "today", today,
                "yesterday", yesterday
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置今天的数据与昨天一致失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "设置今天的数据与昨天一致失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 设置指定日期的数据与前一天一致
     */
    @PostMapping("/set-date-same-as-previous")
    public ResponseEntity<Map<String, Object>> setDateDataSameAsPreviousDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyCountryRiskStatsService.setDateDataSameAsPreviousDay(targetDate);
            
            LocalDate previousDay = targetDate.minusDays(1);
            
            response.put("success", true);
            response.put("message", "设置指定日期的数据与前一天一致成功");
            response.put("data", Map.of(
                "targetDate", targetDate,
                "previousDay", previousDay
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置指定日期的数据与前一天一致失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "设置指定日期的数据与前一天一致失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检测今天的数据是否发生变动
     */
    @GetMapping("/check-today-changed")
    public ResponseEntity<Map<String, Object>> checkTodayDataChanged() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasChanged = dailyCountryRiskStatsService.hasTodayDataChanged();
            LocalDate today = LocalDate.now();
            
            response.put("success", true);
            response.put("hasChanged", hasChanged);
            response.put("today", today);
            response.put("message", hasChanged ? "今天数据已发生变动" : "今天数据未发生变动，与昨天一致");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检测今天数据变动失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "检测今天数据变动失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取智能图表数据
     * 如果今天数据有变动，显示实际数据；如果没有变动，显示与昨天一致的数据
     */
    @GetMapping("/smart-chart-data")
    public ResponseEntity<Map<String, Object>> getSmartChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> smartData = dailyCountryRiskStatsService.getSmartChartData(startDate, endDate);
            
            return ResponseEntity.ok(smartData);
            
        } catch (Exception e) {
            log.error("获取智能图表数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取智能图表数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定国家的智能趋势数据
     */
    @GetMapping("/smart-country-trend")
    public ResponseEntity<Map<String, Object>> getSmartCountryTrendData(
            @RequestParam String country,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> smartTrendData = dailyCountryRiskStatsService.getSmartCountryTrendData(country, startDate, endDate);
            
            return ResponseEntity.ok(smartTrendData);
            
        } catch (Exception e) {
            log.error("获取国家{}智能趋势数据失败: {}", country, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家智能趋势数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有有数据的国家列表
     */
    @GetMapping("/countries")
    public ResponseEntity<Map<String, Object>> getAllCountries() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> countries = dailyCountryRiskStatsService.getAllCountries();
            
            response.put("success", true);
            response.put("countries", countries);
            response.put("count", countries.size());
            response.put("message", "获取国家列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取国家列表失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定国家的风险变化数据
     */
    @GetMapping("/country-risk-change")
    public ResponseEntity<Map<String, Object>> getCountryRiskChangeData(
            @RequestParam String country,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> riskChangeData = dailyCountryRiskStatsService
                .getCountryRiskChangeData(country, startDate, endDate);
            
            return ResponseEntity.ok(riskChangeData);
            
        } catch (Exception e) {
            log.error("获取国家{}风险变化数据失败: {}", country, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家风险变化数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取多个国家的对比数据
     */
    @PostMapping("/multi-country-comparison")
    public ResponseEntity<Map<String, Object>> getMultiCountryComparisonData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestBody List<String> countries) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (countries == null || countries.isEmpty()) {
                response.put("success", false);
                response.put("message", "国家列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> comparisonData = dailyCountryRiskStatsService
                .getMultiCountryComparisonData(countries, startDate, endDate);
            
            return ResponseEntity.ok(comparisonData);
            
        } catch (Exception e) {
            log.error("获取多国家对比数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取多国家对比数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取国家风险等级分布数据
     */
    @GetMapping("/country-risk-distribution")
    public ResponseEntity<Map<String, Object>> getCountryRiskLevelDistribution(
            @RequestParam String country,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> distributionData = dailyCountryRiskStatsService
                .getCountryRiskLevelDistribution(country, statDate);
            
            return ResponseEntity.ok(distributionData);
            
        } catch (Exception e) {
            log.error("获取国家{}风险等级分布失败: {}", country, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家风险等级分布失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取国家风险趋势摘要
     */
    @GetMapping("/country-trend-summary")
    public ResponseEntity<Map<String, Object>> getCountryRiskTrendSummary(
            @RequestParam String country,
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (days <= 0 || days > 365) {
                response.put("success", false);
                response.put("message", "天数必须在1-365之间");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> trendSummary = dailyCountryRiskStatsService
                .getCountryRiskTrendSummary(country, days);
            
            return ResponseEntity.ok(trendSummary);
            
        } catch (Exception e) {
            log.error("获取国家{}风险趋势摘要失败: {}", country, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家风险趋势摘要失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取国家筛选的智能图表数据
     */
    @GetMapping("/filtered-smart-chart-data")
    public ResponseEntity<Map<String, Object>> getFilteredSmartChartData(
            @RequestParam(required = false) List<String> countries,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 如果没有指定国家，获取所有国家
            final List<String> finalCountries;
            if (countries == null || countries.isEmpty()) {
                finalCountries = dailyCountryRiskStatsService.getAllCountries();
            } else {
                finalCountries = countries;
            }
            
            // 获取智能图表数据
            Map<String, Object> smartData = dailyCountryRiskStatsService.getSmartChartData(startDate, endDate);
            
            // 筛选指定国家的数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allData = (List<Map<String, Object>>) smartData.get("data");
            
            List<Map<String, Object>> filteredData = allData.stream()
                .filter(data -> finalCountries.contains(data.get("country")))
                .collect(Collectors.toList());
            
            // 构建响应
            smartData.put("data", filteredData);
            smartData.put("filteredCountries", finalCountries);
            smartData.put("filteredCount", filteredData.size());
            
            return ResponseEntity.ok(smartData);
            
        } catch (Exception e) {
            log.error("获取筛选的智能图表数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取筛选的智能图表数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取预定义的国家列表（用于下拉选择器）
     */
    @GetMapping("/predefined-countries")
    public ResponseEntity<Map<String, Object>> getPredefinedCountries() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> countries = dailyCountryRiskStatsService.getPredefinedCountries();
            
            response.put("success", true);
            response.put("countries", countries);
            response.put("count", countries.size());
            response.put("message", "获取预定义国家列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取预定义国家列表失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取预定义国家列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取预定义国家的智能趋势数据
     */
    @GetMapping("/predefined-country-trend")
    public ResponseEntity<Map<String, Object>> getPredefinedCountryTrendData(
            @RequestParam String displayCountryName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> trendData = dailyCountryRiskStatsService
                .getPredefinedCountryTrendData(displayCountryName, startDate, endDate);
            
            return ResponseEntity.ok(trendData);
            
        } catch (Exception e) {
            log.error("获取预定义国家{}智能趋势数据失败: {}", displayCountryName, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取预定义国家智能趋势数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取预定义国家的筛选智能图表数据
     */
    @PostMapping("/predefined-country-filtered-smart-chart-data")
    public ResponseEntity<Map<String, Object>> getPredefinedCountryFilteredSmartChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestBody List<String> displayCountryNames) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (displayCountryNames == null || displayCountryNames.isEmpty()) {
                response.put("success", false);
                response.put("message", "国家列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> filteredData = dailyCountryRiskStatsService
                .getPredefinedCountryFilteredSmartChartData(displayCountryNames, startDate, endDate);
            
            return ResponseEntity.ok(filteredData);
            
        } catch (Exception e) {
            log.error("获取预定义国家筛选的智能图表数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取预定义国家筛选的智能图表数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取国家名称映射信息
     */
    @GetMapping("/country-name-mapping")
    public ResponseEntity<Map<String, Object>> getCountryNameMapping() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取预定义国家列表
            List<String> predefinedCountries = dailyCountryRiskStatsService.getPredefinedCountries();
            
            // 构建映射信息
            Map<String, Object> mappingInfo = new HashMap<>();
            mappingInfo.put("predefinedCountries", predefinedCountries);
            mappingInfo.put("totalCount", predefinedCountries.size());
            
            response.put("success", true);
            response.put("mappingInfo", mappingInfo);
            response.put("message", "获取国家名称映射信息成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取国家名称映射信息失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取国家名称映射信息失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新数据库中的国家数据为预定义列表
     */
    @PostMapping("/update-countries-to-predefined")
    public ResponseEntity<Map<String, Object>> updateCountriesToPredefinedList() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.updateCountriesToPredefinedList();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新国家数据为预定义列表失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "更新国家数据为预定义列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前数据库中的国家列表
     */
    @GetMapping("/current-countries")
    public ResponseEntity<Map<String, Object>> getCurrentCountriesInDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.getCurrentCountriesInDatabase();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取当前数据库国家列表失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取当前数据库国家列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 清理非预定义国家的数据
     */
    @PostMapping("/cleanup-non-predefined-countries")
    public ResponseEntity<Map<String, Object>> cleanupNonPredefinedCountries() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.cleanupNonPredefinedCountries();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("清理非预定义国家数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "清理非预定义国家数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 重新设置昨天和今天的数据为指定值
     */
    @PostMapping("/reset-yesterday-today-data")
    public ResponseEntity<Map<String, Object>> resetYesterdayAndTodayData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.resetYesterdayAndTodayData();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("重置昨天和今天数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "重置昨天和今天数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取昨天和今天的数据摘要
     */
    @GetMapping("/yesterday-today-summary")
    public ResponseEntity<Map<String, Object>> getYesterdayAndTodayDataSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.getYesterdayAndTodayDataSummary();
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取昨天和今天数据摘要失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取昨天和今天数据摘要失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取近7天各国高风险数据趋势（用于图表展示）
     */
    @GetMapping("/recent-7days-trend")
    public ResponseEntity<Map<String, Object>> getRecent7DaysTrend() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = dailyCountryRiskStatsService.getRecent7DaysTrendData();
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "获取近7天趋势数据成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取近7天趋势数据失败: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取近7天趋势数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取指定天数内各国高风险数据趋势
     */
    @GetMapping("/trend-data")
    public ResponseEntity<Map<String, Object>> getTrendData(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (days <= 0 || days > 365) {
                response.put("success", false);
                response.put("message", "天数必须在1-365之间");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = dailyCountryRiskStatsService.getTrendDataByDays(days);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "获取趋势数据成功");
            response.put("days", days);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取{}天趋势数据失败: {}", days, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取趋势数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有国家的趋势数据（用于图表展示）
     */
    @GetMapping("/all-countries-trend")
    public ResponseEntity<Map<String, Object>> getAllCountriesTrend(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (days <= 0 || days > 365) {
                response.put("success", false);
                response.put("message", "天数必须在1-365之间");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = dailyCountryRiskStatsService.getAllCountriesTrendData(days);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "获取所有国家趋势数据成功");
            response.put("days", days);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取所有国家{}天趋势数据失败: {}", days, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "获取所有国家趋势数据失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

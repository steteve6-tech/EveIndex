package com.certification.service;

import com.certification.entity.common.CrawlerData;
import com.certification.entity.common.CertNewsDailyCountryRiskStats;
import com.certification.repository.CrawlerDataRepository;
import com.certification.repository.DailyCountryRiskStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日国家高风险数据统计服务
 */
@Slf4j
@Service
public class DailyCountryRiskStatsService {

    @Autowired
    private DailyCountryRiskStatsRepository dailyCountryRiskStatsRepository;

    @Autowired
    private CrawlerDataRepository crawlerDataRepository;

    /**
     * 统计指定日期的高风险数据
     */
    @Transactional
    public void calculateDailyStats(LocalDate statDate) {
        log.info("开始统计日期 {} 的高风险数据", statDate);

        try {
            // 查询指定日期的所有数据
            LocalDateTime startOfDay = statDate.atStartOfDay();
            LocalDateTime endOfDay = statDate.plusDays(1).atStartOfDay();

            List<CrawlerData> allData = crawlerDataRepository.findByCrawlTimeBetweenAndDeletedFalse(startOfDay, endOfDay);

            // 按国家分组统计
            Map<String, Map<CrawlerData.RiskLevel, Long>> countryRiskStats = allData.stream()
                .filter(data -> data.getCountry() != null && !data.getCountry().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    data -> data.getCountry().trim(),
                    Collectors.groupingBy(
                        CrawlerData::getRiskLevel,
                        Collectors.counting()
                    )
                ));

            // 保存或更新统计数据
            for (Map.Entry<String, Map<CrawlerData.RiskLevel, Long>> countryEntry : countryRiskStats.entrySet()) {
                String country = countryEntry.getKey();
                Map<CrawlerData.RiskLevel, Long> riskCounts = countryEntry.getValue();

                // 查找是否已存在该日期的统计记录
                CertNewsDailyCountryRiskStats existingStats = dailyCountryRiskStatsRepository
                    .findByStatDateAndCountryAndDeletedFalse(statDate, country);

                CertNewsDailyCountryRiskStats stats;
                if (existingStats != null) {
                    stats = existingStats;
                } else {
                    stats = new CertNewsDailyCountryRiskStats();
                    stats.setStatDate(statDate);
                    stats.setCountry(country);
                }

                // 设置各风险等级的数量
                stats.setHighRiskCount(riskCounts.getOrDefault(CrawlerData.RiskLevel.HIGH, 0L));
                stats.setMediumRiskCount(riskCounts.getOrDefault(CrawlerData.RiskLevel.MEDIUM, 0L));
                stats.setLowRiskCount(riskCounts.getOrDefault(CrawlerData.RiskLevel.LOW, 0L));
                stats.setNoRiskCount(riskCounts.getOrDefault(CrawlerData.RiskLevel.NONE, 0L));

                // 计算总数
                long totalCount = stats.getHighRiskCount() + stats.getMediumRiskCount() + 
                                stats.getLowRiskCount() + stats.getNoRiskCount();
                stats.setTotalCount(totalCount);

                // 设置时间戳
                if (existingStats == null) {
                    stats.setCreatedAt(LocalDateTime.now());
                }
                stats.setUpdatedAt(LocalDateTime.now());

                dailyCountryRiskStatsRepository.save(stats);
                log.debug("保存国家 {} 在日期 {} 的统计数据: 高风险={}, 中风险={}, 低风险={}, 无风险={}, 总计={}", 
                    country, statDate, stats.getHighRiskCount(), stats.getMediumRiskCount(), 
                    stats.getLowRiskCount(), stats.getNoRiskCount(), stats.getTotalCount());
            }

            log.info("完成日期 {} 的高风险数据统计，共处理 {} 个国家", statDate, countryRiskStats.size());

        } catch (Exception e) {
            log.error("统计日期 {} 的高风险数据失败: {}", statDate, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 统计昨天和今天的高风险数据（使用所有历史数据）
     */
    @Transactional
    public void calculateYesterdayStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        
        log.info("开始统计昨天({})和今天({})的高风险数据，使用所有历史数据", yesterday, today);
        
        try {
            // 查询所有数据（不管什么时候创建的）
            List<CrawlerData> allData = crawlerDataRepository.findByDeleted(0);
            log.info("找到所有数据 {} 条", allData.size());

            // 按国家分组统计高风险数据
            Map<String, Long> countryHighRiskStats = allData.stream()
                .filter(data -> data.getCountry() != null && !data.getCountry().trim().isEmpty())
                .filter(data -> data.getRiskLevel() == CrawlerData.RiskLevel.HIGH)
                .collect(Collectors.groupingBy(
                    data -> data.getCountry().trim(),
                    Collectors.counting()
                ));

            log.info("按国家统计的高风险数据: {}", countryHighRiskStats);

            // 保存或更新统计数据（同时保存昨天和今天的记录）
            for (Map.Entry<String, Long> countryEntry : countryHighRiskStats.entrySet()) {
                String country = countryEntry.getKey();
                Long highRiskCount = countryEntry.getValue();

                // 保存昨天的记录
                CertNewsDailyCountryRiskStats yesterdayStats = dailyCountryRiskStatsRepository
                    .findByStatDateAndCountryAndDeletedFalse(yesterday, country);
                
                if (yesterdayStats != null) {
                    yesterdayStats.setUpdatedAt(LocalDateTime.now());
                } else {
                    yesterdayStats = new CertNewsDailyCountryRiskStats();
                    yesterdayStats.setStatDate(yesterday);
                    yesterdayStats.setCountry(country);
                    yesterdayStats.setCreatedAt(LocalDateTime.now());
                    yesterdayStats.setUpdatedAt(LocalDateTime.now());
                }
                yesterdayStats.setHighRiskCount(highRiskCount);
                dailyCountryRiskStatsRepository.save(yesterdayStats);
                
                // 保存今天的记录
                CertNewsDailyCountryRiskStats todayStats = dailyCountryRiskStatsRepository
                    .findByStatDateAndCountryAndDeletedFalse(today, country);
                
                if (todayStats != null) {
                    todayStats.setUpdatedAt(LocalDateTime.now());
                } else {
                    todayStats = new CertNewsDailyCountryRiskStats();
                    todayStats.setStatDate(today);
                    todayStats.setCountry(country);
                    todayStats.setCreatedAt(LocalDateTime.now());
                    todayStats.setUpdatedAt(LocalDateTime.now());
                }
                todayStats.setHighRiskCount(highRiskCount);
                dailyCountryRiskStatsRepository.save(todayStats);
                
                log.info("保存国家 {} 昨天({})和今天({})的高风险数据统计: {} 条", country, yesterday, today, highRiskCount);
            }

            log.info("昨天({})和今天({})的高风险数据统计完成，共统计 {} 个国家", yesterday, today, countryHighRiskStats.size());

        } catch (Exception e) {
            log.error("统计昨天({})和今天({})的高风险数据失败: {}", yesterday, today, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取指定日期范围内各国高风险数据统计
     */
    public List<Map<String, Object>> getCountryHighRiskStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<CertNewsDailyCountryRiskStats> stats = dailyCountryRiskStatsRepository
            .findByDateRangeOrderByDateAndHighRiskCount(startDate, endDate);

        return stats.stream()
            .map(stat -> {
                Map<String, Object> result = new HashMap<>();
                result.put("country", stat.getCountry());
                result.put("highRiskCount", stat.getHighRiskCount());
                result.put("mediumRiskCount", stat.getMediumRiskCount());
                result.put("lowRiskCount", stat.getLowRiskCount());
                result.put("noRiskCount", stat.getNoRiskCount());
                result.put("totalCount", stat.getTotalCount());
                result.put("statDate", stat.getStatDate());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取指定国家在指定日期范围内的高风险数据趋势
     */
    public List<Map<String, Object>> getCountryHighRiskTrend(String country, LocalDate startDate, LocalDate endDate) {
        List<Object[]> trendData = dailyCountryRiskStatsRepository
            .getCountryHighRiskTrend(country, startDate, endDate);

        return trendData.stream()
            .map(row -> {
                Map<String, Object> result = new HashMap<>();
                result.put("date", row[0]);
                result.put("highRiskCount", row[1]);
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取指定日期范围内各国高风险数据总和
     */
    public List<Map<String, Object>> getCountryHighRiskSumByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> sumData = dailyCountryRiskStatsRepository
            .getCountryHighRiskSumByDateRange(startDate, endDate);

        return sumData.stream()
            .map(row -> {
                Map<String, Object> result = new HashMap<>();
                result.put("country", row[0]);
                result.put("totalHighRisk", row[1]);
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取最新的统计数据
     */
    public List<Map<String, Object>> getLatestStats() {
        List<CertNewsDailyCountryRiskStats> stats = dailyCountryRiskStatsRepository.findLatestStats();

        return stats.stream()
            .map(stat -> {
                Map<String, Object> result = new HashMap<>();
                result.put("country", stat.getCountry());
                result.put("highRiskCount", stat.getHighRiskCount());
                result.put("mediumRiskCount", stat.getMediumRiskCount());
                result.put("lowRiskCount", stat.getLowRiskCount());
                result.put("noRiskCount", stat.getNoRiskCount());
                result.put("totalCount", stat.getTotalCount());
                result.put("statDate", stat.getStatDate());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取指定日期的统计数据
     */
    public List<Map<String, Object>> getStatsByDate(LocalDate statDate) {
        List<CertNewsDailyCountryRiskStats> stats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(statDate);

        return stats.stream()
            .map(stat -> {
                Map<String, Object> result = new HashMap<>();
                result.put("country", stat.getCountry());
                result.put("highRiskCount", stat.getHighRiskCount());
                result.put("mediumRiskCount", stat.getMediumRiskCount());
                result.put("lowRiskCount", stat.getLowRiskCount());
                result.put("noRiskCount", stat.getNoRiskCount());
                result.put("totalCount", stat.getTotalCount());
                result.put("statDate", stat.getStatDate());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 设置今天的数据与昨天一致
     */
    @Transactional
    public void setTodayDataSameAsYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        log.info("设置今天({})的数据与昨天({})一致", today, yesterday);
        
        // 获取昨天的数据
        List<CertNewsDailyCountryRiskStats> yesterdayStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(yesterday);
        
        if (yesterdayStats.isEmpty()) {
            log.warn("昨天({})没有统计数据", yesterday);
            return;
        }
        
        // 为今天创建相同的数据
        for (CertNewsDailyCountryRiskStats yesterdayStat : yesterdayStats) {
            CertNewsDailyCountryRiskStats todayStat = dailyCountryRiskStatsRepository
                .findByStatDateAndCountryAndDeletedFalse(today, yesterdayStat.getCountry());
            
            if (todayStat == null) {
                // 创建新记录
                todayStat = new CertNewsDailyCountryRiskStats();
                todayStat.setStatDate(today);
                todayStat.setCountry(yesterdayStat.getCountry());
                todayStat.setCreatedAt(LocalDateTime.now());
                todayStat.setDeleted(false);
            }
            
            // 复制昨天的数据
            todayStat.setHighRiskCount(yesterdayStat.getHighRiskCount());
            todayStat.setMediumRiskCount(yesterdayStat.getMediumRiskCount());
            todayStat.setLowRiskCount(yesterdayStat.getLowRiskCount());
            todayStat.setNoRiskCount(yesterdayStat.getNoRiskCount());
            todayStat.setTotalCount(yesterdayStat.getTotalCount());
            todayStat.setUpdatedAt(LocalDateTime.now());
            
            dailyCountryRiskStatsRepository.save(todayStat);
        }
        
        log.info("成功设置今天的数据与昨天一致，共处理{}个国家", yesterdayStats.size());
    }

    /**
     * 设置指定日期的数据与前一天一致
     */
    @Transactional
    public void setDateDataSameAsPreviousDay(LocalDate targetDate) {
        LocalDate previousDay = targetDate.minusDays(1);
        
        log.info("设置日期({})的数据与前一天({})一致", targetDate, previousDay);
        
        // 获取前一天的数据
        List<CertNewsDailyCountryRiskStats> previousStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(previousDay);
        
        if (previousStats.isEmpty()) {
            log.warn("前一天({})没有统计数据", previousDay);
            return;
        }
        
        // 为目标日期创建相同的数据
        for (CertNewsDailyCountryRiskStats previousStat : previousStats) {
            CertNewsDailyCountryRiskStats targetStat = dailyCountryRiskStatsRepository
                .findByStatDateAndCountryAndDeletedFalse(targetDate, previousStat.getCountry());
            
            if (targetStat == null) {
                // 创建新记录
                targetStat = new CertNewsDailyCountryRiskStats();
                targetStat.setStatDate(targetDate);
                targetStat.setCountry(previousStat.getCountry());
                targetStat.setCreatedAt(LocalDateTime.now());
                targetStat.setDeleted(false);
            }
            
            // 复制前一天的数据
            targetStat.setHighRiskCount(previousStat.getHighRiskCount());
            targetStat.setMediumRiskCount(previousStat.getMediumRiskCount());
            targetStat.setLowRiskCount(previousStat.getLowRiskCount());
            targetStat.setNoRiskCount(previousStat.getNoRiskCount());
            targetStat.setTotalCount(previousStat.getTotalCount());
            targetStat.setUpdatedAt(LocalDateTime.now());
            
            dailyCountryRiskStatsRepository.save(targetStat);
        }
        
        log.info("成功设置日期({})的数据与前一天一致，共处理{}个国家", targetDate, previousStats.size());
    }

    /**
     * 检查今天的数据是否发生变化
     */
    public boolean hasTodayDataChanged() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 获取今天和昨天的高风险数据总数
        Long todayTotal = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(today)
            .stream()
            .mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount)
            .sum();
        
        Long yesterdayTotal = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(yesterday)
            .stream()
            .mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount)
            .sum();
        
        boolean changed = !Objects.equals(todayTotal, yesterdayTotal);
        log.info("今天数据变化检查: 今天={}, 昨天={}, 是否变化={}", todayTotal, yesterdayTotal, changed);
        
        return changed;
    }

    /**
     * 获取智能图表数据
     */
    public Map<String, Object> getSmartChartData(LocalDate startDate, LocalDate endDate) {
        log.info("获取智能图表数据: {} 到 {}", startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查今天的数据是否发生变化
        boolean todayChanged = hasTodayDataChanged();
        result.put("todayChanged", todayChanged);
        
        if (todayChanged) {
            // 如果今天数据有变化，返回实际数据
            result.put("data", getTodayActualData());
            result.put("dataType", "actual");
        } else {
            // 如果今天数据没有变化，返回与昨天一致的数据
            result.put("data", getTodayDataSameAsYesterday());
            result.put("dataType", "consistent");
        }
        
        return result;
    }

    /**
     * 获取今天实际数据
     */
    private Map<String, Object> getTodayActualData() {
        LocalDate today = LocalDate.now();
        List<CertNewsDailyCountryRiskStats> todayStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(today);
        
        return todayStats.stream()
            .collect(Collectors.toMap(
                CertNewsDailyCountryRiskStats::getCountry,
                stat -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("highRiskCount", stat.getHighRiskCount());
                    data.put("mediumRiskCount", stat.getMediumRiskCount());
                    data.put("lowRiskCount", stat.getLowRiskCount());
                    data.put("noRiskCount", stat.getNoRiskCount());
                    data.put("totalCount", stat.getTotalCount());
                    return data;
                }
            ));
    }

    /**
     * 获取今天与昨天一致的数据
     */
    private Map<String, Object> getTodayDataSameAsYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<CertNewsDailyCountryRiskStats> yesterdayStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(yesterday);
        
        return yesterdayStats.stream()
            .collect(Collectors.toMap(
                CertNewsDailyCountryRiskStats::getCountry,
                stat -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("highRiskCount", stat.getHighRiskCount());
                    data.put("mediumRiskCount", stat.getMediumRiskCount());
                    data.put("lowRiskCount", stat.getLowRiskCount());
                    data.put("noRiskCount", stat.getNoRiskCount());
                    data.put("totalCount", stat.getTotalCount());
                    return data;
                }
            ));
    }

    /**
     * 获取智能国家趋势数据
     */
    public Map<String, Object> getSmartCountryTrendData(String country, LocalDate startDate, LocalDate endDate) {
        log.info("获取国家 {} 的智能趋势数据: {} 到 {}", country, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("country", country);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        // 获取趋势数据
        List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(country, startDate, endDate);
        
        List<Map<String, Object>> trendList = trendData.stream()
            .map(row -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", row[0]);
                data.put("highRiskCount", row[1]);
                return data;
            })
            .collect(Collectors.toList());
        
        result.put("trendData", trendList);
        
        // 计算变化统计
        Map<String, Object> changeStats = calculateCountryChangeStats(country, startDate, endDate);
        result.put("changeStats", changeStats);
        
        return result;
    }

    /**
     * 获取所有国家列表
     */
    public List<String> getAllCountries() {
        List<String> countries = dailyCountryRiskStatsRepository
            .findByStatDateBetweenAndDeletedFalseOrderByStatDateDescCountryAsc(
                LocalDate.now().minusDays(30), LocalDate.now())
            .stream()
            .map(CertNewsDailyCountryRiskStats::getCountry)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        log.info("获取到 {} 个国家", countries.size());
        return countries;
    }

    /**
     * 获取国家风险变化数据
     */
    public Map<String, Object> getCountryRiskChangeData(String country, LocalDate startDate, LocalDate endDate) {
        log.info("获取国家 {} 的风险变化数据: {} 到 {}", country, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("country", country);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        // 获取趋势数据
        List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(country, startDate, endDate);
        
        List<Map<String, Object>> trendList = trendData.stream()
            .map(row -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", row[0]);
                data.put("highRiskCount", row[1]);
                return data;
            })
            .collect(Collectors.toList());
        
        result.put("trendData", trendList);
        
        // 计算变化统计
        Map<String, Object> changeStats = calculateCountryChangeStats(country, startDate, endDate);
        result.put("changeStats", changeStats);
        
        return result;
    }

    /**
     * 计算国家变化统计
     */
    private Map<String, Object> calculateCountryChangeStats(String country, LocalDate startDate, LocalDate endDate) {
        List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(country, startDate, endDate);
        
        if (trendData.isEmpty()) {
            return Map.of(
                "totalChange", 0,
                "changeRate", 0.0,
                "maxValue", 0,
                "minValue", 0,
                "averageValue", 0.0
            );
        }
        
        List<Long> values = trendData.stream()
            .map(row -> (Long) row[1])
            .collect(Collectors.toList());
        
        Long firstValue = values.get(0);
        Long lastValue = values.get(values.size() - 1);
        Long totalChange = lastValue - firstValue;
        Double changeRate = firstValue > 0 ? (double) totalChange / firstValue * 100 : 0.0;
        
        Long maxValue = values.stream().mapToLong(Long::longValue).max().orElse(0L);
        Long minValue = values.stream().mapToLong(Long::longValue).min().orElse(0L);
        Double averageValue = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        return Map.of(
            "totalChange", totalChange,
            "changeRate", changeRate,
            "maxValue", maxValue,
            "minValue", minValue,
            "averageValue", averageValue
        );
    }

    /**
     * 获取多国家对比数据
     */
    public Map<String, Object> getMultiCountryComparisonData(List<String> countries, LocalDate startDate, LocalDate endDate) {
        log.info("获取多国家对比数据: {} 从 {} 到 {}", countries, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("countries", countries);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        Map<String, List<Map<String, Object>>> countryTrends = new HashMap<>();
        
        for (String country : countries) {
            List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(country, startDate, endDate);
            
            List<Map<String, Object>> trendList = trendData.stream()
                .map(row -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", row[0]);
                    data.put("highRiskCount", row[1]);
                    return data;
                })
                .collect(Collectors.toList());
            
            countryTrends.put(country, trendList);
        }
        
        result.put("countryTrends", countryTrends);
        
        // 计算对比统计
        Map<String, Object> comparisonStats = new HashMap<>();
        for (String country : countries) {
            List<Map<String, Object>> trends = countryTrends.get(country);
            if (!trends.isEmpty()) {
                Map<String, Object> changeStats = calculateCountryChangeStats(country, startDate, endDate);
                comparisonStats.put(country, changeStats);
            }
        }
        result.put("comparisonStats", comparisonStats);
        
        return result;
    }

    /**
     * 获取国家风险等级分布
     */
    public Map<String, Object> getCountryRiskLevelDistribution(String country, LocalDate statDate) {
        log.info("获取国家 {} 在 {} 的风险等级分布", country, statDate);
        
        CertNewsDailyCountryRiskStats stat = dailyCountryRiskStatsRepository
            .findByStatDateAndCountryAndDeletedFalse(statDate, country);
        
        Map<String, Object> result = new HashMap<>();
        result.put("country", country);
        result.put("statDate", statDate);
        
        if (stat == null) {
            result.put("distribution", Map.of(
                "high", 0,
                "medium", 0,
                "low", 0,
                "no", 0,
                "total", 0
            ));
            result.put("message", "该日期没有统计数据");
        } else {
            Map<String, Long> distribution = new HashMap<>();
            distribution.put("high", stat.getHighRiskCount());
            distribution.put("medium", stat.getMediumRiskCount());
            distribution.put("low", stat.getLowRiskCount());
            distribution.put("no", stat.getNoRiskCount());
            distribution.put("total", stat.getTotalCount());
            
            result.put("distribution", distribution);
        }
        
        return result;
    }

    /**
     * 获取国家风险趋势摘要
     */
    public Map<String, Object> getCountryRiskTrendSummary(String country, int days) {
        log.info("获取国家 {} 最近 {} 天的风险趋势摘要", country, days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        Map<String, Object> result = new HashMap<>();
        result.put("country", country);
        result.put("days", days);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        // 获取趋势数据
        List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(country, startDate, endDate);
        
        if (trendData.isEmpty()) {
            result.put("summary", Map.of(
                "totalRecords", 0,
                "averageValue", 0.0,
                "maxValue", 0,
                "minValue", 0,
                "trend", "stable"
            ));
            result.put("message", "该时间段没有统计数据");
        } else {
            List<Long> values = trendData.stream()
                .map(row -> (Long) row[1])
                .collect(Collectors.toList());
            
            Long totalRecords = values.stream().mapToLong(Long::longValue).sum();
            Double averageValue = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
            Long maxValue = values.stream().mapToLong(Long::longValue).max().orElse(0L);
            Long minValue = values.stream().mapToLong(Long::longValue).min().orElse(0L);
            
            // 计算趋势方向
            String trend = "stable";
            if (values.size() >= 2) {
                Long firstValue = values.get(0);
                Long lastValue = values.get(values.size() - 1);
                if (lastValue > firstValue) {
                    trend = "increasing";
                } else if (lastValue < firstValue) {
                    trend = "decreasing";
                }
            }
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRecords", totalRecords);
            summary.put("averageValue", averageValue);
            summary.put("maxValue", maxValue);
            summary.put("minValue", minValue);
            summary.put("trend", trend);
            
            result.put("summary", summary);
        }
        
        return result;
    }

    // 预定义国家列表
    private static final List<String> PREDEFINED_COUNTRIES = Arrays.asList(
        "美国", "欧盟", "中国", "韩国", "日本", "阿联酋", "印度", "泰国",
        "新加坡", "台湾", "澳大利亚", "智利", "马来西亚", "秘鲁", "南非",
        "以色列", "印尼", "其它国家", "未确定"
    );

    /**
     * 获取预定义国家列表
     */
    public List<String> getPredefinedCountries() {
        return new ArrayList<>(PREDEFINED_COUNTRIES);
    }

    /**
     * 获取预定义国家趋势数据
     */
    public Map<String, Object> getPredefinedCountryTrendData(String displayCountryName, LocalDate startDate, LocalDate endDate) {
        log.info("获取预定义国家 {} 的趋势数据: {} 到 {}", displayCountryName, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("displayCountryName", displayCountryName);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        // 获取该显示名称对应的所有数据库国家名称
        List<String> dbCountryNames = mapDisplayNameToDbNames(displayCountryName);
        
        List<Map<String, Object>> allTrendData = new ArrayList<>();
        
        for (String dbCountryName : dbCountryNames) {
            List<Object[]> trendData = dailyCountryRiskStatsRepository.getCountryHighRiskTrend(dbCountryName, startDate, endDate);
            
            List<Map<String, Object>> trendList = trendData.stream()
                .map(row -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", row[0]);
                    data.put("highRiskCount", row[1]);
                    data.put("dbCountryName", dbCountryName);
                    return data;
                })
                .collect(Collectors.toList());
            
            allTrendData.addAll(trendList);
        }
        
        // 按日期聚合数据
        Map<LocalDate, Long> aggregatedData = allTrendData.stream()
            .collect(Collectors.groupingBy(
                data -> (LocalDate) data.get("date"),
                Collectors.summingLong(data -> (Long) data.get("highRiskCount"))
            ));
        
        List<Map<String, Object>> finalTrendData = aggregatedData.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", entry.getKey());
                data.put("highRiskCount", entry.getValue());
                return data;
            })
            .collect(Collectors.toList());
        
        result.put("trendData", finalTrendData);
        
        // 计算变化统计
        Map<String, Object> changeStats = calculateCountryChangeStats(finalTrendData);
        result.put("changeStats", changeStats);
        
        return result;
    }

    /**
     * 将显示名称映射到数据库名称列表
     */
    private List<String> mapDisplayNameToDbNames(String displayName) {
        // 这里可以根据实际需要实现映射逻辑
        // 暂时返回显示名称本身
        return Arrays.asList(displayName);
    }

    /**
     * 计算国家变化统计（重载方法，接受趋势数据）
     */
    private Map<String, Object> calculateCountryChangeStats(List<Map<String, Object>> trendData) {
        if (trendData.isEmpty()) {
            return Map.of(
                "totalChange", 0,
                "changeRate", 0.0,
                "maxValue", 0,
                "minValue", 0,
                "averageValue", 0.0
            );
        }
        
        List<Long> values = trendData.stream()
            .map(data -> (Long) data.get("highRiskCount"))
            .collect(Collectors.toList());
        
        Long firstValue = values.get(0);
        Long lastValue = values.get(values.size() - 1);
        Long totalChange = lastValue - firstValue;
        Double changeRate = firstValue > 0 ? (double) totalChange / firstValue * 100 : 0.0;
        
        Long maxValue = values.stream().mapToLong(Long::longValue).max().orElse(0L);
        Long minValue = values.stream().mapToLong(Long::longValue).min().orElse(0L);
        Double averageValue = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        return Map.of(
            "totalChange", totalChange,
            "changeRate", changeRate,
            "maxValue", maxValue,
            "minValue", minValue,
            "averageValue", averageValue
        );
    }

    /**
     * 获取预定义国家筛选的智能图表数据
     */
    public Map<String, Object> getPredefinedCountryFilteredSmartChartData(List<String> displayCountryNames, LocalDate startDate, LocalDate endDate) {
        log.info("获取预定义国家筛选的智能图表数据: {} 从 {} 到 {}", displayCountryNames, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("displayCountryNames", displayCountryNames);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        // 检查今天的数据是否发生变化
        boolean todayChanged = hasTodayDataChanged();
        result.put("todayChanged", todayChanged);
        
        Map<String, Object> filteredData = new HashMap<>();
        
        for (String displayCountryName : displayCountryNames) {
            // 获取该显示名称对应的所有数据库国家名称
            List<String> dbCountryNames = mapDisplayNameToDbNames(displayCountryName);
            
            Map<String, Object> countryData = new HashMap<>();
            Long totalHighRisk = 0L;
            Long totalMediumRisk = 0L;
            Long totalLowRisk = 0L;
            Long totalNoRisk = 0L;
            Long totalCount = 0L;
            
            for (String dbCountryName : dbCountryNames) {
                CertNewsDailyCountryRiskStats stat = dailyCountryRiskStatsRepository
                    .findByStatDateAndCountryAndDeletedFalse(LocalDate.now(), dbCountryName);
                
                if (stat != null) {
                    totalHighRisk += stat.getHighRiskCount();
                    totalMediumRisk += stat.getMediumRiskCount();
                    totalLowRisk += stat.getLowRiskCount();
                    totalNoRisk += stat.getNoRiskCount();
                    totalCount += stat.getTotalCount();
                }
            }
            
            countryData.put("highRiskCount", totalHighRisk);
            countryData.put("mediumRiskCount", totalMediumRisk);
            countryData.put("lowRiskCount", totalLowRisk);
            countryData.put("noRiskCount", totalNoRisk);
            countryData.put("totalCount", totalCount);
            
            filteredData.put(displayCountryName, countryData);
        }
        
        result.put("data", filteredData);
        result.put("dataType", todayChanged ? "actual" : "consistent");
        
        return result;
    }

    /**
     * 更新国家到预定义列表
     */
    @Transactional
    public Map<String, Object> updateCountriesToPredefinedList() {
        log.info("开始更新国家到预定义列表");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            int updatedCount = 0;
            int createdCount = 0;
            
            // 为预定义国家创建或更新数据
            for (String predefinedCountry : PREDEFINED_COUNTRIES) {
                // 检查是否存在该国家的数据
                List<CertNewsDailyCountryRiskStats> existingStats = dailyCountryRiskStatsRepository
                    .findByCountryAndDeletedFalse(predefinedCountry);
                
                if (existingStats.isEmpty()) {
                    // 创建新的统计数据
                    CertNewsDailyCountryRiskStats newStat = new CertNewsDailyCountryRiskStats();
                    newStat.setCountry(predefinedCountry);
                    newStat.setStatDate(LocalDate.now());
                    newStat.setHighRiskCount(0L);
                    newStat.setMediumRiskCount(0L);
                    newStat.setLowRiskCount(0L);
                    newStat.setNoRiskCount(0L);
                    newStat.setTotalCount(0L);
                    newStat.setCreatedAt(LocalDateTime.now());
                    newStat.setUpdatedAt(LocalDateTime.now());
                    newStat.setDeleted(false);
                    
                    dailyCountryRiskStatsRepository.save(newStat);
                    createdCount++;
                } else {
                    // 更新现有数据
                    for (CertNewsDailyCountryRiskStats stat : existingStats) {
                        stat.setUpdatedAt(LocalDateTime.now());
                        dailyCountryRiskStatsRepository.save(stat);
                        updatedCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", "国家数据更新完成");
            result.put("updatedCount", updatedCount);
            result.put("createdCount", createdCount);
            result.put("totalProcessed", updatedCount + createdCount);
            
            log.info("国家数据更新完成: 更新{}条, 创建{}条", updatedCount, createdCount);
            
        } catch (Exception e) {
            log.error("更新国家数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新国家数据失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取当前数据库中的国家信息
     */
    public Map<String, Object> getCurrentCountriesInDatabase() {
        log.info("获取当前数据库中的国家信息");
        
        Map<String, Object> result = new HashMap<>();
        
        List<String> currentCountries = getAllCountries();
        
        List<String> predefinedCountries = new ArrayList<>();
        List<String> nonPredefinedCountries = new ArrayList<>();
        
        for (String country : currentCountries) {
            if (PREDEFINED_COUNTRIES.contains(country)) {
                predefinedCountries.add(country);
            } else {
                nonPredefinedCountries.add(country);
            }
        }
        
        result.put("totalCountries", currentCountries.size());
        result.put("predefinedCountries", predefinedCountries);
        result.put("nonPredefinedCountries", nonPredefinedCountries);
        result.put("predefinedCount", predefinedCountries.size());
        result.put("nonPredefinedCount", nonPredefinedCountries.size());
        
        return result;
    }

    /**
     * 清理非预定义国家数据
     */
    @Transactional
    public Map<String, Object> cleanupNonPredefinedCountries() {
        log.info("开始清理非预定义国家数据");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> currentCountries = getAllCountries();
            List<String> countriesToDelete = new ArrayList<>();
            
            for (String country : currentCountries) {
                if (!PREDEFINED_COUNTRIES.contains(country)) {
                    countriesToDelete.add(country);
                }
            }
            
            int deletedCount = 0;
            for (String country : countriesToDelete) {
                List<CertNewsDailyCountryRiskStats> statsToDelete = dailyCountryRiskStatsRepository
                    .findByCountryAndDeletedFalse(country);
                
                for (CertNewsDailyCountryRiskStats stat : statsToDelete) {
                    stat.setDeleted(true);
                    stat.setUpdatedAt(LocalDateTime.now());
                    dailyCountryRiskStatsRepository.save(stat);
                    deletedCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "非预定义国家数据清理完成");
            result.put("deletedCountries", countriesToDelete);
            result.put("deletedCount", deletedCount);
            
            log.info("非预定义国家数据清理完成: 删除{}个国家, {}条记录", countriesToDelete.size(), deletedCount);
            
        } catch (Exception e) {
            log.error("清理非预定义国家数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "清理非预定义国家数据失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 重新设置昨天和今天的数据
     */
    @Transactional
    public Map<String, Object> resetYesterdayAndTodayData() {
        log.info("开始重新设置昨天和今天的数据");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            // 定义国家数据映射
            Map<String, Map<String, Long>> countryDataMap = new HashMap<>();
            
            // 美国
            countryDataMap.put("美国", createCountryData(60L, 0L, 0L, 0L, 60L));
            // 欧盟
            countryDataMap.put("欧盟", createCountryData(63L, 0L, 0L, 0L, 63L));
            // 中国
            countryDataMap.put("中国", createCountryData(11L, 0L, 0L, 0L, 11L));
            // 韩国
            countryDataMap.put("韩国", createCountryData(7L, 0L, 0L, 0L, 7L));
            // 日本
            countryDataMap.put("日本", createCountryData(4L, 0L, 0L, 0L, 4L));
            // 阿联酋
            countryDataMap.put("阿联酋", createCountryData(3L, 0L, 0L, 0L, 3L));
            // 印度
            countryDataMap.put("印度", createCountryData(90L, 0L, 0L, 0L, 90L));
            // 泰国
            countryDataMap.put("泰国", createCountryData(23L, 0L, 0L, 0L, 23L));
            // 新加坡
            countryDataMap.put("新加坡", createCountryData(0L, 0L, 0L, 0L, 0L));
            // 台湾
            countryDataMap.put("台湾", createCountryData(0L, 0L, 0L, 0L, 0L));
            // 澳大利亚
            countryDataMap.put("澳大利亚", createCountryData(5L, 0L, 0L, 0L, 5L));
            // 智利
            countryDataMap.put("智利", createCountryData(5L, 0L, 0L, 0L, 5L));
            // 马来西亚
            countryDataMap.put("马来西亚", createCountryData(1L, 0L, 0L, 0L, 1L));
            // 秘鲁
            countryDataMap.put("秘鲁", createCountryData(1L, 0L, 0L, 0L, 1L));
            // 南非
            countryDataMap.put("南非", createCountryData(1L, 0L, 0L, 0L, 1L));
            // 以色列
            countryDataMap.put("以色列", createCountryData(2L, 0L, 0L, 0L, 2L));
            // 印尼
            countryDataMap.put("印尼", createCountryData(1L, 0L, 0L, 0L, 1L));
            // 其它国家
            countryDataMap.put("其它国家", createCountryData(26L, 0L, 0L, 0L, 26L));
            // 未确定
            countryDataMap.put("未确定", createCountryData(2L, 0L, 0L, 0L, 2L));
            
            int updatedCount = 0;
            int createdCount = 0;
            
            // 为昨天和今天设置数据
            for (LocalDate date : Arrays.asList(yesterday, today)) {
                for (Map.Entry<String, Map<String, Long>> entry : countryDataMap.entrySet()) {
                    String country = entry.getKey();
                    Map<String, Long> data = entry.getValue();
                    
                    // 查找是否已存在该记录
                    CertNewsDailyCountryRiskStats existingStat = dailyCountryRiskStatsRepository
                        .findByStatDateAndCountryAndDeletedFalse(date, country);
                    
                    if (existingStat != null) {
                        // 更新现有记录
                        existingStat.setHighRiskCount(data.get("highRisk"));
                        existingStat.setMediumRiskCount(data.get("mediumRisk"));
                        existingStat.setLowRiskCount(data.get("lowRisk"));
                        existingStat.setNoRiskCount(data.get("noRisk"));
                        existingStat.setTotalCount(data.get("total"));
                        existingStat.setUpdatedAt(LocalDateTime.now());
                        
                        dailyCountryRiskStatsRepository.save(existingStat);
                        updatedCount++;
                    } else {
                        // 创建新记录
                        CertNewsDailyCountryRiskStats newStat = new CertNewsDailyCountryRiskStats();
                        newStat.setStatDate(date);
                        newStat.setCountry(country);
                        newStat.setHighRiskCount(data.get("highRisk"));
                        newStat.setMediumRiskCount(data.get("mediumRisk"));
                        newStat.setLowRiskCount(data.get("lowRisk"));
                        newStat.setNoRiskCount(data.get("noRisk"));
                        newStat.setTotalCount(data.get("total"));
                        newStat.setCreatedAt(LocalDateTime.now());
                        newStat.setUpdatedAt(LocalDateTime.now());
                        newStat.setDeleted(false);
                        
                        dailyCountryRiskStatsRepository.save(newStat);
                        createdCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", "昨天和今天数据重置完成");
            result.put("yesterday", yesterday);
            result.put("today", today);
            result.put("countries", countryDataMap.keySet());
            result.put("updatedRecords", updatedCount);
            result.put("createdRecords", createdCount);
            result.put("totalRecords", updatedCount + createdCount);
            
            log.info("昨天和今天数据重置完成: 更新{}条, 创建{}条, 涉及{}个国家", 
                updatedCount, createdCount, countryDataMap.size());
            
        } catch (Exception e) {
            log.error("重置昨天和今天数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "重置昨天和今天数据失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建国家数据映射
     */
    private Map<String, Long> createCountryData(Long highRisk, Long mediumRisk, Long lowRisk, Long noRisk, Long total) {
        Map<String, Long> data = new HashMap<>();
        data.put("highRisk", highRisk);
        data.put("mediumRisk", mediumRisk);
        data.put("lowRisk", lowRisk);
        data.put("noRisk", noRisk);
        data.put("total", total);
        return data;
    }

    /**
     * 获取昨天和今天的数据摘要
     */
    public Map<String, Object> getYesterdayAndTodayDataSummary() {
        log.info("获取昨天和今天的数据摘要");
        
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 获取昨天的数据
        List<CertNewsDailyCountryRiskStats> yesterdayStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(yesterday);
        
        // 获取今天的数据
        List<CertNewsDailyCountryRiskStats> todayStats = dailyCountryRiskStatsRepository
            .findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(today);
        
        result.put("yesterday", buildDateSummary(yesterday, yesterdayStats));
        result.put("today", buildDateSummary(today, todayStats));
        
        // 计算总体变化
        Long yesterdayTotal = yesterdayStats.stream().mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount).sum();
        Long todayTotal = todayStats.stream().mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount).sum();
        Long totalChange = todayTotal - yesterdayTotal;
        Double changeRate = yesterdayTotal > 0 ? (double) totalChange / yesterdayTotal * 100 : 0.0;
        
        result.put("overallChange", Map.of(
            "yesterdayTotal", yesterdayTotal,
            "todayTotal", todayTotal,
            "totalChange", totalChange,
            "changeRate", changeRate,
            "trend", totalChange > 0 ? "increasing" : (totalChange < 0 ? "decreasing" : "stable")
        ));
        
        return result;
    }

    /**
     * 构建日期摘要
     */
    private Map<String, Object> buildDateSummary(LocalDate date, List<CertNewsDailyCountryRiskStats> stats) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalCountries", stats.size());
        summary.put("totalHighRisk", stats.stream().mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount).sum());
        summary.put("totalMediumRisk", stats.stream().mapToLong(CertNewsDailyCountryRiskStats::getMediumRiskCount).sum());
        summary.put("totalLowRisk", stats.stream().mapToLong(CertNewsDailyCountryRiskStats::getLowRiskCount).sum());
        summary.put("totalNoRisk", stats.stream().mapToLong(CertNewsDailyCountryRiskStats::getNoRiskCount).sum());
        summary.put("totalCount", stats.stream().mapToLong(CertNewsDailyCountryRiskStats::getTotalCount).sum());
        
        // 获取前5个高风险国家
        List<Map<String, Object>> topCountries = stats.stream()
            .limit(5)
            .map(stat -> {
                Map<String, Object> countryData = new HashMap<>();
                countryData.put("country", stat.getCountry());
                countryData.put("highRiskCount", stat.getHighRiskCount());
                countryData.put("totalCount", stat.getTotalCount());
                countryData.put("riskLevel", determineRiskLevel(stat.getHighRiskCount(), stat.getTotalCount()));
                return countryData;
            })
            .collect(Collectors.toList());
        
        summary.put("topCountries", topCountries);
        
        return summary;
    }

    /**
     * 确定风险等级
     */
    private String determineRiskLevel(Long highRisk, Long total) {
        if (total == 0) return "无数据";
        double ratio = (double) highRisk / total;
        if (ratio >= 0.7) return "高风险";
        if (ratio >= 0.4) return "中风险";
        if (ratio >= 0.1) return "低风险";
        return "极低风险";
    }

    /**
     * 获取近7天各国高风险数据趋势（用于图表展示）
     */
    public Map<String, Object> getRecent7DaysTrendData() {
        return getTrendDataByDays(7);
    }

    /**
     * 获取指定天数内各国高风险数据趋势
     */
    public Map<String, Object> getTrendDataByDays(int days) {
        log.info("获取近{}天各国高风险数据趋势", days);
        
        LocalDate endDate = LocalDate.now().minusDays(1); // 昨天
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // 设置最小日期为2025年9月7日
        LocalDate minDate = LocalDate.of(2025, 9, 7);
        if (startDate.isBefore(minDate)) {
            startDate = minDate;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("days", days);
        
        // 获取所有国家的数据
        List<CertNewsDailyCountryRiskStats> allStats = dailyCountryRiskStatsRepository
            .findByStatDateBetweenAndDeletedFalseOrderByStatDateAscCountryAsc(startDate, endDate);
        
        // 按国家分组
        Map<String, List<CertNewsDailyCountryRiskStats>> countryStatsMap = allStats.stream()
            .collect(Collectors.groupingBy(CertNewsDailyCountryRiskStats::getCountry));
        
        // 生成日期序列
        List<LocalDate> dateSequence = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            dateSequence.add(startDate.plusDays(i));
        }
        
        // 构建趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        List<String> countries = new ArrayList<>();
        
        for (Map.Entry<String, List<CertNewsDailyCountryRiskStats>> entry : countryStatsMap.entrySet()) {
            String country = entry.getKey();
            List<CertNewsDailyCountryRiskStats> countryStats = entry.getValue();
            
            countries.add(country);
            
            // 为每个日期创建数据点
            List<Map<String, Object>> countryData = new ArrayList<>();
            for (LocalDate date : dateSequence) {
                CertNewsDailyCountryRiskStats stat = countryStats.stream()
                    .filter(s -> s.getStatDate().equals(date))
                    .findFirst()
                    .orElse(null);
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", date);
                dataPoint.put("dateStr", date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
                dataPoint.put("highRiskCount", stat != null ? stat.getHighRiskCount() : 0L);
                dataPoint.put("mediumRiskCount", stat != null ? stat.getMediumRiskCount() : 0L);
                dataPoint.put("lowRiskCount", stat != null ? stat.getLowRiskCount() : 0L);
                dataPoint.put("noRiskCount", stat != null ? stat.getNoRiskCount() : 0L);
                dataPoint.put("totalCount", stat != null ? stat.getTotalCount() : 0L);
                
                countryData.add(dataPoint);
            }
            
            Map<String, Object> countryTrend = new HashMap<>();
            countryTrend.put("country", country);
            countryTrend.put("data", countryData);
            
            // 计算国家统计信息
            Long totalHighRisk = countryStats.stream()
                .mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount)
                .sum();
            Long totalCount = countryStats.stream()
                .mapToLong(CertNewsDailyCountryRiskStats::getTotalCount)
                .sum();
            
            countryTrend.put("totalHighRisk", totalHighRisk);
            countryTrend.put("totalCount", totalCount);
            countryTrend.put("averageHighRisk", countryStats.size() > 0 ? totalHighRisk / countryStats.size() : 0L);
            
            trendData.add(countryTrend);
        }
        
        // 按总高风险数量排序
        trendData.sort((a, b) -> {
            Long aTotal = (Long) a.get("totalHighRisk");
            Long bTotal = (Long) b.get("totalHighRisk");
            return bTotal.compareTo(aTotal);
        });
        
        result.put("trendData", trendData);
        result.put("countries", countries);
        result.put("totalCountries", countries.size());
        result.put("dateSequence", dateSequence.stream()
            .map(date -> date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")))
            .collect(Collectors.toList()));
        
        // 计算总体统计
        Long totalHighRisk = allStats.stream()
            .mapToLong(CertNewsDailyCountryRiskStats::getHighRiskCount)
            .sum();
        Long totalCount = allStats.stream()
            .mapToLong(CertNewsDailyCountryRiskStats::getTotalCount)
            .sum();
        
        result.put("overallStats", Map.of(
            "totalHighRisk", totalHighRisk,
            "totalCount", totalCount,
            "averageHighRiskPerDay", days > 0 ? totalHighRisk / days : 0L,
            "averageHighRiskPerCountry", countries.size() > 0 ? totalHighRisk / countries.size() : 0L
        ));
        
        log.info("获取近{}天趋势数据完成，共{}个国家，总计{}条高风险数据", 
            days, countries.size(), totalHighRisk);
        
        return result;
    }

    /**
     * 获取所有国家的趋势数据（用于图表展示）
     */
    public Map<String, Object> getAllCountriesTrendData(int days) {
        log.info("获取所有国家{}天趋势数据", days);
        
        LocalDate endDate = LocalDate.now().minusDays(1); // 昨天
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // 设置最小日期为2025年9月7日
        LocalDate minDate = LocalDate.of(2025, 9, 7);
        if (startDate.isBefore(minDate)) {
            startDate = minDate;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("days", days);
        
        // 获取预定义国家列表
        List<String> predefinedCountries = getPredefinedCountries();
        
        // 生成日期序列
        List<LocalDate> dateSequence = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            dateSequence.add(startDate.plusDays(i));
        }
        
        // 构建每个国家的趋势数据
        List<Map<String, Object>> countriesTrendData = new ArrayList<>();
        
        for (String country : predefinedCountries) {
            // 获取该国家在指定日期范围内的数据
            List<CertNewsDailyCountryRiskStats> countryStats = dailyCountryRiskStatsRepository
                .findByCountryAndStatDateBetweenAndDeletedFalseOrderByStatDateAsc(country, startDate, endDate);
            
            // 为每个日期创建数据点
            List<Map<String, Object>> countryData = new ArrayList<>();
            Long totalHighRisk = 0L;
            Long totalCount = 0L;
            
            for (LocalDate date : dateSequence) {
                CertNewsDailyCountryRiskStats stat = countryStats.stream()
                    .filter(s -> s.getStatDate().equals(date))
                    .findFirst()
                    .orElse(null);
                
                Long highRiskCount = stat != null ? stat.getHighRiskCount() : 0L;
                Long count = stat != null ? stat.getTotalCount() : 0L;
                
                totalHighRisk += highRiskCount;
                totalCount += count;
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", date);
                dataPoint.put("dateStr", date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
                dataPoint.put("highRiskCount", highRiskCount);
                dataPoint.put("mediumRiskCount", stat != null ? stat.getMediumRiskCount() : 0L);
                dataPoint.put("lowRiskCount", stat != null ? stat.getLowRiskCount() : 0L);
                dataPoint.put("noRiskCount", stat != null ? stat.getNoRiskCount() : 0L);
                dataPoint.put("totalCount", count);
                
                countryData.add(dataPoint);
            }
            
            Map<String, Object> countryTrend = new HashMap<>();
            countryTrend.put("country", country);
            countryTrend.put("data", countryData);
            countryTrend.put("totalHighRisk", totalHighRisk);
            countryTrend.put("totalCount", totalCount);
            countryTrend.put("averageHighRisk", days > 0 ? totalHighRisk / days : 0L);
            countryTrend.put("hasData", totalHighRisk > 0);
            
            countriesTrendData.add(countryTrend);
        }
        
        // 按总高风险数量排序
        countriesTrendData.sort((a, b) -> {
            Long aTotal = (Long) a.get("totalHighRisk");
            Long bTotal = (Long) b.get("totalHighRisk");
            return bTotal.compareTo(aTotal);
        });
        
        result.put("countriesTrendData", countriesTrendData);
        result.put("predefinedCountries", predefinedCountries);
        result.put("totalCountries", predefinedCountries.size());
        result.put("dateSequence", dateSequence.stream()
            .map(date -> date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")))
            .collect(Collectors.toList()));
        
        // 计算总体统计
        Long totalHighRisk = countriesTrendData.stream()
            .mapToLong(country -> (Long) country.get("totalHighRisk"))
            .sum();
        Long totalCount = countriesTrendData.stream()
            .mapToLong(country -> (Long) country.get("totalCount"))
            .sum();
        
        result.put("overallStats", Map.of(
            "totalHighRisk", totalHighRisk,
            "totalCount", totalCount,
            "averageHighRiskPerDay", days > 0 ? totalHighRisk / days : 0L,
            "averageHighRiskPerCountry", predefinedCountries.size() > 0 ? totalHighRisk / predefinedCountries.size() : 0L,
            "countriesWithData", countriesTrendData.stream()
                .filter(country -> (Boolean) country.get("hasData"))
                .count()
        ));
        
        log.info("获取所有国家{}天趋势数据完成，共{}个国家，总计{}条高风险数据", 
            days, predefinedCountries.size(), totalHighRisk);
        
        return result;
    }
}

package com.certification.service;

import com.certification.entity.common.Standard;
import com.certification.repository.StandardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准服务类 - 使用common包中的Standard实体
 */
@Slf4j
@Service
@Transactional
public class StandardService {
    
    @Autowired
    private StandardRepository standardRepository;
    
    /**
     * 保存标准
     */
    public Standard saveStandard(Standard standard) {
        log.info("保存标准: {}", standard.getStandardNumber());
        return standardRepository.save(standard);
    }
    
    /**
     * 根据ID获取标准
     */
    public Standard getStandardById(Long id) {
        log.info("根据ID获取标准: {}", id);
        return standardRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据标准编号获取标准
     */
    public Standard getStandardByNumber(String standardNumber) {
        log.info("根据标准编号获取标准: {}", standardNumber);
        return standardRepository.findByStandardNumberAndDeleted(standardNumber, 0).orElse(null);
    }
    
    /**
     * 搜索标准 - 使用JPA Repository
     */
    public Page<Standard> searchStandards(String keyword, String riskLevel, String country, String status, Pageable pageable) {
        log.info("搜索标准: keyword={}, riskLevel={}, country={}, status={}, pageable={}", keyword, riskLevel, country, status, pageable);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            return standardRepository.searchByKeyword(keyword, pageable);
        } else if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            try {
                Standard.RiskLevel risk = Standard.RiskLevel.valueOf(riskLevel.toUpperCase());
                return standardRepository.findByRiskLevelAndDeleted(risk, 0, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("无效的风险等级: {}", riskLevel);
                return Page.empty(pageable);
            }
        } else if (country != null && !country.trim().isEmpty()) {
            // 增强国家搜索：同时搜索主要国家和适用国家列表
            return standardRepository.findByCountryOrCountriesContainingAndDeleted(country, 0, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            try {
                Standard.StandardStatus standardStatus = Standard.StandardStatus.valueOf(status.toUpperCase());
                return standardRepository.findByStandardStatusAndDeleted(standardStatus, 0, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("无效的标准状态: {}", status);
                return Page.empty(pageable);
            }
        } else {
            return standardRepository.findByDeleted(0, pageable);
        }
    }
    
    /**
     * 根据国家查询标准
     */
    public List<Standard> getStandardsByCountry(String country) {
        log.info("根据国家查询标准: {}", country);
        return standardRepository.findByCountryAndDeleted(country, 0);
    }
    
    /**
     * 根据多个国家查询标准
     */
    public List<Standard> getStandardsByCountries(List<String> countries) {
        log.info("根据多个国家查询标准: {}", countries);
        if (countries == null || countries.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String countriesJson = mapper.writeValueAsString(countries);
            return standardRepository.findByCountriesAndDeleted(countriesJson, 0);
        } catch (Exception e) {
            log.error("序列化国家列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 删除标准
     */
    public void deleteStandard(Long id) {
        log.info("删除标准: {}", id);
        standardRepository.softDelete(id);
    }
    
    /**
     * 获取高风险标准
     */
    public List<Standard> getHighRiskStandards() {
        log.info("获取高风险标准");
        return standardRepository.findByRiskLevelAndDeleted(Standard.RiskLevel.HIGH, 0);
    }
    
    /**
     * 获取监控中的标准
     */
    public List<Standard> getMonitoredStandards() {
        log.info("获取监控中的标准");
        return standardRepository.findByIsMonitoredAndDeletedOrderByUpdatedAtDesc(Boolean.TRUE, 0);
    }
    
    /**
     * 获取即将到期的标准
     */
    public List<Standard> getExpiringStandards() {
        log.info("获取即将到期的标准");
        String endDate = LocalDate.now().plusDays(30).toString(); // 30天内到期
        return standardRepository.findExpiringStandards(endDate);
    }
    
    /**
     * 获取即将生效的标准
     */
    public List<Standard> getUpcomingStandards(int days, int limit) {
        log.info("获取即将生效的标准: days={}, limit={}", days, limit);
        String currentDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(days).toString();
        Pageable pageable = PageRequest.of(0, limit);
        Page<Standard> page = standardRepository.findUpcomingStandards(currentDate, endDate, pageable);
        return page.getContent();
    }
    
    /**
     * 获取即将生效标准的数量
     */
    public long getUpcomingStandardsCount(int days) {
        log.info("获取即将生效标准的数量: days={}", days);
        String currentDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(days).toString();
        return standardRepository.countUpcomingStandards(currentDate, endDate);
    }
    
    /**
     * 获取即将生效的标准（分页）
     */
    public Page<Standard> getUpcomingStandardsPage(int days, Pageable pageable) {
        log.info("获取即将生效的标准（分页）: days={}, page={}, size={}", days, pageable.getPageNumber(), pageable.getPageSize());
        String currentDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(days).toString();
        return standardRepository.findUpcomingStandards(currentDate, endDate, pageable);
    }
    
    /**
     * 获取最近更新的标准
     */
    public List<Standard> getRecentlyUpdatedStandards(int limit) {
        log.info("获取最近更新的标准: limit={}", limit);
        Pageable pageable = PageRequest.of(0, limit);
        Page<Standard> page = standardRepository.findRecentlyUpdatedStandards(pageable);
        return page.getContent();
    }
    
    /**
     * 获取按发布时间排序的最新标准
     */
    public List<Standard> getLatestStandardsByPublishedDate(int limit) {
        log.info("获取按发布时间排序的最新标准: limit={}", limit);
        Pageable pageable = PageRequest.of(0, limit);
        Page<Standard> page = standardRepository.findLatestStandardsByPublishedDate(pageable);
        return page.getContent();
    }
    
    /**
     * 获取按生效时间排序的即将生效标准（未来生效的标准）
     */
    public List<Standard> getUpcomingStandardsByEffectiveDate(int limit) {
        log.info("获取按生效时间排序的即将生效标准（未来生效的标准）: limit={}", limit);
        
        // 获取所有有效的标准，然后在Java代码中排序
        List<Standard> allStandards = standardRepository.findByDeleted(0, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        
        // 过滤掉生效时间为null的标准，并且只保留生效时间大于当前时间的标准
        LocalDate today = LocalDate.now();
        List<Standard> standardsWithEffectiveDate = allStandards.stream()
            .filter(s -> s.getEffectiveDate() != null)
            .filter(s -> {
                try {
                    LocalDate effectiveDate = LocalDate.parse(s.getEffectiveDate());
                    return effectiveDate.isAfter(today);
                } catch (Exception e) {
                    // 如果日期解析失败，排除该标准
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        // 按生效时间升序排序（即将生效的标准）
        standardsWithEffectiveDate.sort((s1, s2) -> {
            try {
                LocalDate date1 = LocalDate.parse(s1.getEffectiveDate());
                LocalDate date2 = LocalDate.parse(s2.getEffectiveDate());
                return date1.compareTo(date2);
            } catch (Exception e) {
                // 如果日期解析失败，按字符串排序
                return s1.getEffectiveDate().compareTo(s2.getEffectiveDate());
            }
        });
        
        // 返回前limit个
        return standardsWithEffectiveDate.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 获取风险等级统计
     */
    public Map<String, Object> getRiskLevelStats() {
        log.info("获取风险等级统计");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取各风险等级的数量
        long highRiskCount = standardRepository.countByRiskLevelAndDeleted(Standard.RiskLevel.HIGH, 0);
        long mediumRiskCount = standardRepository.countByRiskLevelAndDeleted(Standard.RiskLevel.MEDIUM, 0);
        long lowRiskCount = standardRepository.countByRiskLevelAndDeleted(Standard.RiskLevel.LOW, 0);
        
        // 获取总数
        long totalCount = standardRepository.countByDeleted(0);
        
        // 计算百分比
        double highRiskPercentage = totalCount > 0 ? (double) highRiskCount / totalCount * 100 : 0;
        double mediumRiskPercentage = totalCount > 0 ? (double) mediumRiskCount / totalCount * 100 : 0;
        double lowRiskPercentage = totalCount > 0 ? (double) lowRiskCount / totalCount * 100 : 0;
        
        // 构建统计数据
        Map<String, Object> highRisk = new HashMap<>();
        highRisk.put("count", highRiskCount);
        highRisk.put("percentage", Math.round(highRiskPercentage * 100.0) / 100.0);
        highRisk.put("color", "#ff4d4f");
        
        Map<String, Object> mediumRisk = new HashMap<>();
        mediumRisk.put("count", mediumRiskCount);
        mediumRisk.put("percentage", Math.round(mediumRiskPercentage * 100.0) / 100.0);
        mediumRisk.put("color", "#faad14");
        
        Map<String, Object> lowRisk = new HashMap<>();
        lowRisk.put("count", lowRiskCount);
        lowRisk.put("percentage", Math.round(lowRiskPercentage * 100.0) / 100.0);
        lowRisk.put("color", "#52c41a");
        
        stats.put("highRisk", highRisk);
        stats.put("mediumRisk", mediumRisk);
        stats.put("lowRisk", lowRisk);
        stats.put("total", totalCount);
        
        return stats;
    }
    
    /**
     * 执行数据更新
     */
    public void performDataUpdate() {
        log.info("执行数据更新");
        // 这里可以添加数据更新的逻辑
        // 例如：从外部API获取最新数据并更新到数据库
    }
    
    /**
     * 获取风险统计
     */
    public Map<String, Object> getRiskStatistics() {
        log.info("获取风险统计");
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各风险等级的数量
        List<Map<String, Object>> riskStats = standardRepository.countByRiskLevel();
        stats.put("risk_levels", riskStats);
        
        // 统计各监管影响的数量
        List<Map<String, Object>> impactStats = standardRepository.countByRegulatoryImpact();
        stats.put("regulatory_impacts", impactStats);
        
        // 统计各标准状态的数量
        List<Map<String, Object>> statusStats = standardRepository.countByStandardStatus();
        stats.put("standard_statuses", statusStats);
        
        return stats;
    }
    
    /**
     * 获取标准统计信息
     */
    public Map<String, Object> getStandardStatistics() {
        log.info("获取标准统计信息");
        Map<String, Object> stats = new HashMap<>();
        
        // 总数量
        long totalCount = standardRepository.countByDeleted(0);
        stats.put("total_count", totalCount);
        
        // 高风险标准数量
        long highRiskCount = standardRepository.countByRiskLevelAndDeleted(Standard.RiskLevel.HIGH, 0);
        stats.put("high_risk_count", highRiskCount);
        
        // 监控中的标准数量
        long monitoredCount = standardRepository.countByIsMonitoredAndDeleted(Boolean.TRUE, 0);
        stats.put("monitored_count", monitoredCount);
        
        // 即将到期的标准数量
        String endDate = LocalDate.now().plusDays(30).toString();
        long expiringCount = standardRepository.countExpiringStandards(endDate);
        stats.put("expiring_count", expiringCount);
        
        // 各国家统计
        List<Map<String, Object>> countryStats = standardRepository.countByCountry();
        stats.put("country_stats", countryStats);
        
        // 各产品类型统计
        List<Map<String, Object>> productTypeStats = standardRepository.countByProductTypes();
        stats.put("product_type_stats", productTypeStats);
        
        return stats;
    }
    
    /**
     * 获取国家统计信息
     */
    public Map<String, Object> getCountryStatistics() {
        log.info("获取国家统计信息");
        List<Map<String, Object>> countryStats = standardRepository.getCountryStatistics();
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> stat : countryStats) {
            String country = (String) stat.get("country");
            Long count = (Long) stat.get("count");
            if (country != null) {
                result.put(country, count);
            }
        }
        return result;
    }
    
    /**
     * 更新监控状态
     */
    public void updateMonitoringStatus(Long id, Boolean isMonitored) {
        log.info("更新监控状态: id={}, isMonitored={}", id, isMonitored);
        standardRepository.updateMonitoringStatus(id, isMonitored);
    }
}



package com.certification.standards;

import com.certification.entity.common.Standard;
import com.certification.dto.StandardCreateRequest;
import com.certification.dto.StandardUpdateRequest;
import com.certification.dto.StandardQueryRequest;
import com.certification.repository.StandardManagementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 标准管理服务类
 * 提供标准的增删改查功能
 */
@Slf4j
@Service
@Transactional
public class StandardManagementService {

    @Autowired
    private StandardManagementRepository standardRepository;

    @Autowired
    private KeywordConfig keywordConfig;

    /**
     * 创建新标准
     */
    public Standard createStandard(StandardCreateRequest request) {
        log.info("创建新标准: {}", request.getStandardNumber());
        
        // 检查标准编号是否已存在
        Standard existingStandard = standardRepository.findByStandardNumberAndDeleted(
                request.getStandardNumber(), 0);
        if (existingStandard != null) {
            throw new IllegalArgumentException("标准编号已存在: " + request.getStandardNumber());
        }

        Standard standard = new Standard();
        standard.setStandardNumber(request.getStandardNumber());
        standard.setVersion(request.getVersion());
        standard.setTitle(request.getTitle());
        standard.setDescription(request.getDescription());
        standard.setPublishedDate(request.getPublishedDate());
        standard.setEffectiveDate(request.getEffectiveDate());
        standard.setDownloadUrl(request.getDownloadUrl());
        standard.setKeywords(request.getKeywords());
        standard.setRiskLevel(request.getRiskLevel());
        standard.setRegulatoryImpact(request.getRegulatoryImpact());
        standard.setStandardStatus(request.getStandardStatus());
        // 设置主要国家（如果countries列表不为空，取第一个作为主要国家）
        if (request.getCountries() != null && !request.getCountries().isEmpty()) {
            standard.setCountry(request.getCountries().get(0));
        } else {
            standard.setCountry(request.getCountry());
        }
        standard.setCountryList(request.getCountries());
        standard.setScope(request.getScope());
        standard.setProductTypes(request.getProductTypes());
        standard.setFrequencyBands(request.getFrequencyBands());
        standard.setPowerLimits(request.getPowerLimits());
        standard.setTestMethods(request.getTestMethods());
        standard.setComplianceDeadline(request.getComplianceDeadline());
        standard.setTransitionEnd(request.getTransitionEnd());
        standard.setMatchedProfiles(request.getMatchedProfiles());
        standard.setRawExcerpt(request.getRawExcerpt());
        standard.setIsMonitored(request.getIsMonitored());
        standard.setSourceUrl(request.getSourceUrl());
        standard.setDeleted(0);

        Standard savedStandard = standardRepository.save(standard);
        log.info("标准创建成功: ID={}, 编号={}", savedStandard.getId(), savedStandard.getStandardNumber());
        
        return savedStandard;
    }

    /**
     * 更新标准
     */
    public Standard updateStandard(Long id, StandardUpdateRequest request) {
        log.info("更新标准: ID={}", id);
        
        Optional<Standard> optionalStandard = standardRepository.findById(id);
        if (optionalStandard.isEmpty()) {
            throw new IllegalArgumentException("标准不存在: ID=" + id);
        }

        Standard standard = optionalStandard.get();
        if (standard.getDeleted() == 1) {
            throw new IllegalArgumentException("标准已删除: ID=" + id);
        }

        // 检查标准编号是否与其他标准重复
        if (StringUtils.hasText(request.getStandardNumber()) && 
            !request.getStandardNumber().equals(standard.getStandardNumber())) {
            Standard existingStandard = standardRepository.findByStandardNumberAndDeleted(
                    request.getStandardNumber(), 0);
            if (existingStandard != null) {
                throw new IllegalArgumentException("标准编号已存在: " + request.getStandardNumber());
            }
        }

        // 更新字段
        if (StringUtils.hasText(request.getStandardNumber())) {
            standard.setStandardNumber(request.getStandardNumber());
        }
        if (StringUtils.hasText(request.getVersion())) {
            standard.setVersion(request.getVersion());
        }
        if (StringUtils.hasText(request.getTitle())) {
            standard.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDescription())) {
            standard.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getPublishedDate())) {
            standard.setPublishedDate(request.getPublishedDate());
        }
        if (StringUtils.hasText(request.getEffectiveDate())) {
            standard.setEffectiveDate(request.getEffectiveDate());
        }
        if (StringUtils.hasText(request.getDownloadUrl())) {
            standard.setDownloadUrl(request.getDownloadUrl());
        }
        if (StringUtils.hasText(request.getKeywords())) {
            standard.setKeywords(request.getKeywords());
        }
        if (request.getRiskLevel() != null) {
            standard.setRiskLevel(request.getRiskLevel());
        }
        if (request.getRegulatoryImpact() != null) {
            standard.setRegulatoryImpact(request.getRegulatoryImpact());
        }
        if (request.getStandardStatus() != null) {
            standard.setStandardStatus(request.getStandardStatus());
        }
        // 更新国家信息
        if (request.getCountries() != null && !request.getCountries().isEmpty()) {
            // 如果提供了countries列表，取第一个作为主要国家
            standard.setCountry(request.getCountries().get(0));
            standard.setCountryList(request.getCountries());
        } else if (StringUtils.hasText(request.getCountry())) {
            // 如果只提供了单个国家，更新主要国家
            standard.setCountry(request.getCountry());
        }
        if (StringUtils.hasText(request.getScope())) {
            standard.setScope(request.getScope());
        }
        if (StringUtils.hasText(request.getProductTypes())) {
            standard.setProductTypes(request.getProductTypes());
        }
        if (StringUtils.hasText(request.getFrequencyBands())) {
            standard.setFrequencyBands(request.getFrequencyBands());
        }
        if (StringUtils.hasText(request.getPowerLimits())) {
            standard.setPowerLimits(request.getPowerLimits());
        }
        if (StringUtils.hasText(request.getTestMethods())) {
            standard.setTestMethods(request.getTestMethods());
        }
        if (request.getComplianceDeadline() != null) {
            standard.setComplianceDeadline(request.getComplianceDeadline());
        }
        if (StringUtils.hasText(request.getTransitionEnd())) {
            standard.setTransitionEnd(request.getTransitionEnd());
        }
        if (StringUtils.hasText(request.getMatchedProfiles())) {
            standard.setMatchedProfiles(request.getMatchedProfiles());
        }
        if (StringUtils.hasText(request.getRawExcerpt())) {
            standard.setRawExcerpt(request.getRawExcerpt());
        }
        if (request.getIsMonitored() != null) {
            standard.setIsMonitored(request.getIsMonitored());
        }
        if (StringUtils.hasText(request.getSourceUrl())) {
            standard.setSourceUrl(request.getSourceUrl());
        }

        Standard updatedStandard = standardRepository.save(standard);
        log.info("标准更新成功: ID={}, 编号={}", updatedStandard.getId(), updatedStandard.getStandardNumber());
        
        return updatedStandard;
    }

    /**
     * 删除标准（逻辑删除）
     */
    public void deleteStandard(Long id) {
        log.info("删除标准: ID={}", id);
        
        Optional<Standard> optionalStandard = standardRepository.findById(id);
        if (optionalStandard.isEmpty()) {
            throw new IllegalArgumentException("标准不存在: ID=" + id);
        }

        Standard standard = optionalStandard.get();
        if (standard.getDeleted() == 1) {
            throw new IllegalArgumentException("标准已删除: ID=" + id);
        }

        standard.setDeleted(1);
        standardRepository.save(standard);
        log.info("标准删除成功: ID={}", id);
    }

    /**
     * 根据ID获取标准
     */
    public Standard getStandardById(Long id) {
        log.info("获取标准: ID={}", id);
        
        Optional<Standard> optionalStandard = standardRepository.findById(id);
        if (optionalStandard.isEmpty() || optionalStandard.get().getDeleted() == 1) {
            return null;
        }
        
        return optionalStandard.get();
    }

    /**
     * 根据标准编号获取标准
     */
    public Standard getStandardByNumber(String standardNumber) {
        log.info("根据编号获取标准: {}", standardNumber);
        
        return standardRepository.findByStandardNumberAndDeleted(standardNumber, 0);
    }

    /**
     * 分页查询标准
     */
    public Page<Standard> getStandards(StandardQueryRequest request, Pageable pageable) {
        log.info("分页查询标准: {}", request);
        
        String keyword = request.getKeyword();
        String risk = request.getRisk();
        String country = request.getCountry();
        Standard.StandardStatus status = request.getStatus();
        Boolean isMonitored = request.getIsMonitored();

        if (StringUtils.hasText(keyword)) {
            return standardRepository.searchByKeyword(keyword, pageable);
        } else if (StringUtils.hasText(risk)) {
            try {
                Standard.RiskLevel riskLevel = Standard.RiskLevel.valueOf(risk.toUpperCase());
                return standardRepository.findByRiskLevelAndDeleted(riskLevel, 0, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("无效的风险等级: {}", risk);
                return Page.empty(pageable);
            }
        } else if (StringUtils.hasText(country)) {
            return standardRepository.findByCountry(country, pageable);
        } else if (status != null) {
            return standardRepository.findByStandardStatusAndDeleted(status, 0, pageable);
        } else if (isMonitored != null) {
            return standardRepository.findByIsMonitoredAndDeleted(isMonitored, 0, pageable);
        } else {
            return standardRepository.findAll(pageable);
        }
    }

    /**
     * 根据国家查询标准
     */
    public List<Standard> getStandardsByCountry(String country) {
        log.info("根据国家查询标准: {}", country);
        return standardRepository.findByCountry(country);
    }

    /**
     * 根据多个国家查询标准
     */
    public List<Standard> getStandardsByCountries(List<String> countries) {
        log.info("根据多个国家查询标准: {}", countries);
        if (countries == null || countries.isEmpty()) {
            return List.of();
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String countriesJson = mapper.writeValueAsString(countries);
            return standardRepository.findByCountries(countriesJson);
        } catch (Exception e) {
            log.error("序列化国家列表失败: {}", e.getMessage());
            return List.of();
        }
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
    public List<Standard> getExpiringStandards(java.time.LocalDate deadline) {
        log.info("获取即将到期的标准: {}", deadline);
        return standardRepository.findByComplianceDeadlineLessThanEqualAndDeletedOrderByComplianceDeadlineAsc(deadline, 0);
    }

    /**
     * 统计各风险等级的数量
     */
    public List<java.util.Map<String, Object>> getRiskStatistics() {
        log.info("获取风险统计");
        return standardRepository.countByRiskLevel();
    }

    /**
     * 统计各状态的数量
     */
    public List<java.util.Map<String, Object>> getStatusStatistics() {
        log.info("获取状态统计");
        return standardRepository.countByStatus();
    }
}

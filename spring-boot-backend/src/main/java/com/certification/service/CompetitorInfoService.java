package com.certification.service;

import com.certification.entity.CompetitorInfo;
import com.certification.repository.CompetitorInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorInfoService {

    private final CompetitorInfoRepository competitorInfoRepository;

    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        try {
            statistics.put("totalRecords", competitorInfoRepository.countTotalRecords());
            statistics.put("activeCompetitors", competitorInfoRepository.countActiveCompetitors());
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1);
            statistics.put("monthlyNew", competitorInfoRepository.countMonthlyNew(startOfMonth));
            statistics.put("riskAlerts", competitorInfoRepository.countRiskAlerts());
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            statistics.put("totalRecords", 0L);
            statistics.put("activeCompetitors", 0L);
            statistics.put("monthlyNew", 0L);
            statistics.put("riskAlerts", 0L);
        }
        return statistics;
    }

    public Page<CompetitorInfo> getCompetitorList(String keyword, String status, String dataSource, 
                                                 String riskLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        
        CompetitorInfo.Status statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = CompetitorInfo.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的状态值: {}", status);
            }
        }
        
        CompetitorInfo.RiskLevel riskLevelEnum = null;
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            try {
                riskLevelEnum = CompetitorInfo.RiskLevel.valueOf(riskLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的风险等级值: {}", riskLevel);
            }
        }
        
        return competitorInfoRepository.findByKeywordAndFilters(keyword, statusEnum, dataSource, riskLevelEnum, pageable);
    }

    public Optional<CompetitorInfo> getCompetitorInfoById(Long id) {
        return competitorInfoRepository.findById(id);
    }

    @Transactional
    public CompetitorInfo createCompetitorInfo(CompetitorInfo competitorInfo) {
        competitorInfo.setCreatedAt(LocalDateTime.now());
        competitorInfo.setUpdatedAt(LocalDateTime.now());
        return competitorInfoRepository.save(competitorInfo);
    }

    @Transactional
    public CompetitorInfo updateCompetitorInfo(Long id, CompetitorInfo competitorInfo) {
        CompetitorInfo existing = competitorInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("竞品信息不存在，ID: " + id));
        
        existing.setDeviceName(competitorInfo.getDeviceName());
        existing.setManufacturerBrand(competitorInfo.getManufacturerBrand());
        existing.setDeviceCode(competitorInfo.getDeviceCode());
        existing.setUsageScope(competitorInfo.getUsageScope());
        existing.setDeviceDescription(competitorInfo.getDeviceDescription());
        existing.setDataSource(competitorInfo.getDataSource());
        existing.setCertificationType(competitorInfo.getCertificationType());
        existing.setStatus(competitorInfo.getStatus());
        existing.setRiskLevel(competitorInfo.getRiskLevel());
        existing.setCertificationDate(competitorInfo.getCertificationDate());
        existing.setExpiryDate(competitorInfo.getExpiryDate());
        existing.setRemarks(competitorInfo.getRemarks());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return competitorInfoRepository.save(existing);
    }

    @Transactional
    public void deleteCompetitorInfo(Long id) {
        if (!competitorInfoRepository.existsById(id)) {
            throw new IllegalArgumentException("竞品信息不存在，ID: " + id);
        }
        competitorInfoRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> pushDataToCompetitorInfo(List<CompetitorInfo> competitorInfoList) {
        int successCount = 0;
        int skipCount = 0;
        
        for (CompetitorInfo competitorInfo : competitorInfoList) {
            try {
                if (competitorInfo.getDeviceName() == null || competitorInfo.getDeviceName().trim().isEmpty()) {
                    skipCount++;
                    continue;
                }
                
                competitorInfo.setCreatedAt(LocalDateTime.now());
                competitorInfo.setUpdatedAt(LocalDateTime.now());
                competitorInfoRepository.save(competitorInfo);
                successCount++;
            } catch (Exception e) {
                log.error("保存竞品信息失败: {}", competitorInfo.getDeviceName(), e);
                skipCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("totalCount", competitorInfoList.size());
        return result;
    }

    @Transactional
    public void clearAllCompetitorData() {
        competitorInfoRepository.deleteAllData();
    }
}

package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 高风险数据服务类
 * 实现关键词筛选和国家筛选功能
 */
@Service
public class HighRiskDataService {

    @Autowired
    private Device510KRepository device510KRepository;
    
    @Autowired
    private RecallRepository recallRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private GuidanceRepository guidanceRepository;
    
    @Autowired
    private CustomsRepository customsRepository;

    /**
     * 获取高风险数据统计
     */
    public Map<String, Object> getHighRiskStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalHighRisk", 
            device510KRepository.countByRiskLevel("HIGH") +
            recallRepository.countByRiskLevel("HIGH") +
            eventRepository.countByRiskLevel("HIGH") +
            registrationRepository.countByRiskLevel("HIGH") +
            guidanceRepository.countByRiskLevel("HIGH") +
            customsRepository.countByRiskLevel("HIGH")
        );
        
        statistics.put("device510KHighRisk", device510KRepository.countByRiskLevel("HIGH"));
        statistics.put("recallHighRisk", recallRepository.countByRiskLevel("HIGH"));
        statistics.put("eventHighRisk", eventRepository.countByRiskLevel("HIGH"));
        statistics.put("registrationHighRisk", registrationRepository.countByRiskLevel("HIGH"));
        statistics.put("guidanceHighRisk", guidanceRepository.countByRiskLevel("HIGH"));
        statistics.put("customsHighRisk", customsRepository.countByRiskLevel("HIGH"));
        
        return statistics;
    }

    /**
     * 按国家获取高风险数据统计
     */
    public Map<String, Object> getHighRiskStatisticsByCountry() {
        Map<String, Object> countryStatistics = new HashMap<>();
        
        // 获取所有国家的统计数据
        List<String> countries = Arrays.asList("US", "CN", "EU", "JP", "KR", "CA", "AU", "GB", "DE", "FR");
        
        for (String country : countries) {
            Map<String, Object> countryData = new HashMap<>();
            countryData.put("device510K", device510KRepository.countByCountryAndRiskLevel(country, "HIGH"));
            countryData.put("recall", recallRepository.countByCountryAndRiskLevel(country, "HIGH"));
            countryData.put("event", eventRepository.countByCountryAndRiskLevel(country, "HIGH"));
            countryData.put("registration", registrationRepository.countByCountryAndRiskLevel(country, "HIGH"));
            countryData.put("guidance", guidanceRepository.countByCountryAndRiskLevel(country, "HIGH"));
            countryData.put("customs", customsRepository.countByCountryAndRiskLevel(country, "HIGH"));
            
            // 计算总数
            int total = (Integer) countryData.get("device510K") + (Integer) countryData.get("recall") + 
                       (Integer) countryData.get("event") + (Integer) countryData.get("registration") + 
                       (Integer) countryData.get("guidance") + (Integer) countryData.get("customs");
            countryData.put("total", total);
            
            countryStatistics.put(country, countryData);
        }
        
        return countryStatistics;
    }

    /**
     * 获取关键词统计
     */
    public List<Map<String, Object>> getKeywordStatistics() {
        List<Map<String, Object>> keywords = new ArrayList<>();
        
        // 从数据库中获取所有唯一的关键词及其计数
        // 这里需要根据实际的数据结构来实现
        // 示例数据：
        keywords.add(Map.of("keyword", "Skin Analysis", "count", 15));
        keywords.add(Map.of("keyword", "Skin Scanner", "count", 8));
        keywords.add(Map.of("keyword", "3D skin imaging", "count", 12));
        keywords.add(Map.of("keyword", "Facial Imaging", "count", 6));
        keywords.add(Map.of("keyword", "Skin pigmentation", "count", 9));
        keywords.add(Map.of("keyword", "skin elasticity", "count", 4));
        
        return keywords;
    }

    /**
     * 根据数据类型获取数据（支持关键词和国家筛选）
     */
    public Page<?> getDataByType(String dataType, Pageable pageable, String country, String keyword) {
        switch (dataType.toLowerCase()) {
            case "device510k":
                return getDevice510KData(pageable, country, keyword);
            case "recall":
                return getRecallData(pageable, country, keyword);
            case "event":
                return getEventData(pageable, country, keyword);
            case "registration":
                return getRegistrationData(pageable, country, keyword);
            case "guidance":
                return getGuidanceData(pageable, country, keyword);
            case "customs":
                return getCustomsData(pageable, country, keyword);
            default:
                throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
    }

    /**
     * 获取510K设备数据
     */
    private Page<?> getDevice510KData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return device510KRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return device510KRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return device510KRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return device510KRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 获取召回数据
     */
    private Page<?> getRecallData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return recallRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return recallRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return recallRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return recallRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 获取事件数据
     */
    private Page<?> getEventData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return eventRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return eventRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return eventRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return eventRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 获取注册数据
     */
    private Page<?> getRegistrationData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return registrationRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return registrationRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return registrationRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return registrationRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 获取指导文档数据
     */
    private Page<?> getGuidanceData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return guidanceRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return guidanceRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return guidanceRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return guidanceRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 获取海关案例数据
     */
    private Page<?> getCustomsData(Pageable pageable, String country, String keyword) {
        if (country != null && keyword != null) {
            return customsRepository.findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                country, keyword, "HIGH", pageable);
        } else if (country != null) {
            return customsRepository.findByCountryAndRiskLevel(country, "HIGH", pageable);
        } else if (keyword != null) {
            return customsRepository.findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
                keyword, "HIGH", pageable);
        } else {
            return customsRepository.findByRiskLevel("HIGH", pageable);
        }
    }

    /**
     * 更新风险等级
     */
    public boolean updateRiskLevel(String dataType, Long id, String riskLevel) {
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                    return device510KRepository.updateRiskLevel(id, riskLevel) > 0;
                case "recall":
                    return recallRepository.updateRiskLevel(id, riskLevel) > 0;
                case "event":
                    return eventRepository.updateRiskLevel(id, riskLevel) > 0;
                case "registration":
                    return registrationRepository.updateRiskLevel(id, riskLevel) > 0;
                case "guidance":
                    return guidanceRepository.updateRiskLevel(id, riskLevel) > 0;
                case "customs":
                    return customsRepository.updateRiskLevel(id, riskLevel) > 0;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量更新风险等级
     */
    public boolean batchUpdateRiskLevel(List<Integer> ids, String riskLevel) {
        try {
            // 实现批量更新逻辑
            for (Integer id : ids) {
                // 这里需要根据实际需求确定数据类型
                // 暂时使用device510k作为示例
                device510KRepository.updateRiskLevel(id.longValue(), riskLevel);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新关键词
     */
    public boolean updateKeywords(String dataType, Long id, String oldKeyword, String newKeyword) {
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                    return device510KRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                case "recall":
                    return recallRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                case "event":
                    return eventRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                case "registration":
                    return registrationRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                case "guidance":
                    return guidanceRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                case "customs":
                    return customsRepository.updateKeywords(id, oldKeyword, newKeyword) > 0;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

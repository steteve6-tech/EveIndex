package com.certification.analysis;

import com.certification.entity.common.*;
import com.certification.repository.common.Device510KRepository;
import com.certification.repository.common.DeviceRecallRecordRepository;
import com.certification.repository.common.DeviceEventReportRepository;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import com.certification.repository.common.GuidanceDocumentRepository;
import com.certification.repository.common.CustomsCaseRepository;
import com.certification.repository.DeviceMatchKeywordsRepository;
// import com.certification.repository.DailyCountryRiskStatsRepository; // 暂时未使用，未来可用于历史数据查询
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备数据分析服务
 * 整合高风险数据管理功能，提供风险等级为HIGH的数据的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDataanalysis {

    private final Device510KRepository device510KRepository;
    private final DeviceRecallRecordRepository deviceRecallRecordRepository;
    private final DeviceEventReportRepository deviceEventReportRepository;
    private final DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    private final GuidanceDocumentRepository guidanceDocumentRepository;
    private final CustomsCaseRepository customsCaseRepository;
    private final DeviceMatchKeywordsRepository deviceMatchKeywordsRepository;
    // private final DailyCountryRiskStatsRepository dailyCountryRiskStatsRepository; // 暂时未使用，未来可用于历史数据查询

    /**
     * 获取高风险数据统计
     */
    public Map<String, Object> getHighRiskStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 统计各类型高风险数据数量
            long device510KCount = device510KRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            long recallCount = deviceRecallRecordRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            long eventCount = deviceEventReportRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            long registrationCount = deviceRegistrationRecordRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            long guidanceCount = guidanceDocumentRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            long customsCount = customsCaseRepository.countByRiskLevel(CertNewsData.RiskLevel.HIGH);
            
            // 计算总数
            long totalHighRisk = device510KCount + recallCount + eventCount + 
                                registrationCount + guidanceCount + customsCount;
            
            statistics.put("totalHighRisk", totalHighRisk);
            statistics.put("device510KHighRisk", device510KCount);
            statistics.put("recallHighRisk", recallCount);
            statistics.put("eventHighRisk", eventCount);
            statistics.put("registrationHighRisk", registrationCount);
            statistics.put("guidanceHighRisk", guidanceCount);
            statistics.put("customsHighRisk", customsCount);
            
            // 添加时间戳
            statistics.put("timestamp", new Date());
            
        } catch (Exception e) {
            log.error("获取高风险数据统计失败", e);
            statistics.put("error", "获取统计数据失败");
        }
        
        return statistics;
    }

    /**
     * 获取按国家分类的高风险数据统计
     */
    public Map<String, Object> getHighRiskStatisticsByCountry() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 按国家统计各类型高风险数据数量
            Map<String, Long> device510KByCountry = getDevice510KHighRiskByCountry();
            Map<String, Long> recallByCountry = getDeviceRecallHighRiskByCountry();
            Map<String, Long> eventByCountry = getDeviceEventHighRiskByCountry();
            Map<String, Long> registrationByCountry = getDeviceRegistrationHighRiskByCountry();
            Map<String, Long> guidanceByCountry = getGuidanceDocumentHighRiskByCountry();
            Map<String, Long> customsByCountry = getCustomsCaseHighRiskByCountry();
            
            // 合并所有国家的数据
            Set<String> allCountries = new HashSet<>();
            allCountries.addAll(device510KByCountry.keySet());
            allCountries.addAll(recallByCountry.keySet());
            allCountries.addAll(eventByCountry.keySet());
            allCountries.addAll(registrationByCountry.keySet());
            allCountries.addAll(guidanceByCountry.keySet());
            allCountries.addAll(customsByCountry.keySet());
            
            // 构建按国家分类的统计结果
            Map<String, Map<String, Object>> countryStats = new HashMap<>();
            for (String country : allCountries) {
                Map<String, Object> countryData = new HashMap<>();
                countryData.put("device510K", device510KByCountry.getOrDefault(country, 0L));
                countryData.put("recall", recallByCountry.getOrDefault(country, 0L));
                countryData.put("event", eventByCountry.getOrDefault(country, 0L));
                countryData.put("registration", registrationByCountry.getOrDefault(country, 0L));
                countryData.put("guidance", guidanceByCountry.getOrDefault(country, 0L));
                countryData.put("customs", customsByCountry.getOrDefault(country, 0L));
                
                // 计算该国家的总数
                long countryTotal = countryData.values().stream()
                    .mapToLong(value -> (Long) value)
                    .sum();
                countryData.put("total", countryTotal);
                
                countryStats.put(country, countryData);
            }
            
            statistics.put("countryStatistics", countryStats);
            statistics.put("totalCountries", countryStats.size());
            statistics.put("timestamp", new Date());
            
        } catch (Exception e) {
            log.error("获取按国家分类的高风险数据统计失败", e);
            statistics.put("error", "获取统计数据失败");
        }
        
        return statistics;
    }

    /**
     * 根据数据类型获取高风险数据
     */
    public Page<Map<String, Object>> getHighRiskDataByType(String dataType, Pageable pageable) {
        return getHighRiskDataByType(dataType, pageable, null, null);
    }

    /**
     * 根据数据类型获取高风险数据（支持关键词和国家筛选）
     */
    public Page<Map<String, Object>> getHighRiskDataByType(String dataType, Pageable pageable, String keyword, String country) {
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    return getDevice510KHighRiskData(pageable, keyword, country);
                case "recall":
                case "devicerecallrecord":
                    return getDeviceRecallHighRiskData(pageable, keyword, country);
                case "event":
                case "deviceeventreport":
                    return getDeviceEventHighRiskData(pageable, keyword, country);
                case "registration":
                case "deviceregistrationrecord":
                    return getDeviceRegistrationHighRiskData(pageable, keyword, country);
                case "guidance":
                case "guidancedocument":
                    return getGuidanceDocumentHighRiskData(pageable, keyword, country);
                case "customs":
                case "customscase":
                    return getCustomsCaseHighRiskData(pageable, keyword, country);
                default:
                    throw new IllegalArgumentException("不支持的数据类型: " + dataType);
            }
        } catch (Exception e) {
            log.error("获取{}类型高风险数据失败", dataType, e);
            throw new RuntimeException("获取数据失败", e);
        }
    }

    /**
     * 更新数据风险等级
     */
    @Transactional
    public boolean updateRiskLevel(String dataType, Long id, String newRiskLevel) {
        try {
            CertNewsData.RiskLevel riskLevel = CertNewsData.RiskLevel.valueOf(newRiskLevel.toUpperCase());
            
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    return updateDevice510KRiskLevel(id, riskLevel);
                case "recall":
                case "devicerecallrecord":
                    return updateDeviceRecallRiskLevel(id, riskLevel);
                case "event":
                case "deviceeventreport":
                    return updateDeviceEventRiskLevel(id, riskLevel);
                case "registration":
                case "deviceregistrationrecord":
                    return updateDeviceRegistrationRiskLevel(id, riskLevel);
                case "guidance":
                case "guidancedocument":
                    return updateGuidanceDocumentRiskLevel(id, riskLevel);
                case "customs":
                case "customscase":
                    return updateCustomsCaseRiskLevel(id, riskLevel);
                default:
                    log.warn("不支持的数据类型: {}", dataType);
                    return false;
            }
        } catch (Exception e) {
            log.error("更新风险等级失败", e);
            return false;
        }
    }

    /**
     * 批量更新风险等级
     */
    @Transactional
    public int batchUpdateRiskLevel(String dataType, List<Long> ids, String newRiskLevel) {
        try {
            CertNewsData.RiskLevel riskLevel = CertNewsData.RiskLevel.valueOf(newRiskLevel.toUpperCase());
            int updatedCount = 0;
            
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    updatedCount = batchUpdateDevice510KRiskLevel(ids, riskLevel);
                    break;
                case "recall":
                case "devicerecallrecord":
                    updatedCount = batchUpdateDeviceRecallRiskLevel(ids, riskLevel);
                    break;
                case "event":
                case "deviceeventreport":
                    updatedCount = batchUpdateDeviceEventRiskLevel(ids, riskLevel);
                    break;
                case "registration":
                case "deviceregistrationrecord":
                    updatedCount = batchUpdateDeviceRegistrationRiskLevel(ids, riskLevel);
                    break;
                case "guidance":
                case "guidancedocument":
                    updatedCount = batchUpdateGuidanceDocumentRiskLevel(ids, riskLevel);
                    break;
                case "customs":
                case "customscase":
                    updatedCount = batchUpdateCustomsCaseRiskLevel(ids, riskLevel);
                    break;
                default:
                    log.warn("不支持的数据类型: {}", dataType);
                    return 0;
            }
            
            return updatedCount;
        } catch (Exception e) {
            log.error("批量更新风险等级失败", e);
            return 0;
        }
    }

    /**
     * 搜索高风险数据
     */
    public Page<Map<String, Object>> searchHighRiskData(Map<String, Object> searchCriteria, Pageable pageable) {
        try {
            String dataType = (String) searchCriteria.get("dataType");
            if (dataType != null && !dataType.isEmpty()) {
                return getHighRiskDataByType(dataType, pageable);
            }
            
            // 如果没有指定数据类型，返回所有类型的高风险数据
            // 这里可以实现更复杂的搜索逻辑
            return getDevice510KHighRiskData(pageable);
        } catch (Exception e) {
            log.error("搜索高风险数据失败", e);
            throw new RuntimeException("搜索失败", e);
        }
    }

    /**
     * 获取高风险数据详情
     */
    public Map<String, Object> getHighRiskDataDetail(String dataType, Long id) {
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    return device510KRepository.findById(id)
                            .map(this::convertDevice510KToMap)
                            .orElse(null);
                case "recall":
                case "devicerecallrecord":
                    return deviceRecallRecordRepository.findById(id)
                            .map(this::convertDeviceRecallToMap)
                            .orElse(null);
                case "event":
                case "deviceeventreport":
                    return deviceEventReportRepository.findById(id)
                            .map(this::convertDeviceEventToMap)
                            .orElse(null);
                case "registration":
                case "deviceregistrationrecord":
                    return deviceRegistrationRecordRepository.findById(id)
                            .map(this::convertDeviceRegistrationToMap)
                            .orElse(null);
                case "guidance":
                case "guidancedocument":
                    return guidanceDocumentRepository.findById(id)
                            .map(this::convertGuidanceDocumentToMap)
                            .orElse(null);
                case "customs":
                case "customscase":
                    return customsCaseRepository.findById(id)
                            .map(this::convertCustomsCaseToMap)
                            .orElse(null);
                default:
                    log.warn("不支持的数据类型: {}", dataType);
                    return null;
            }
        } catch (Exception e) {
            log.error("获取高风险数据详情失败", e);
            return null;
        }
    }

    /**
     * 获取带趋势信息的高风险数据统计
     */
    public Map<String, Object> getHighRiskStatisticsWithTrend() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 获取当前统计数据
            Map<String, Object> currentStats = getHighRiskStatistics();
            
            // 获取昨天的统计数据（这里需要实现历史数据查询逻辑）
            Map<String, Object> yesterdayStats = getYesterdayHighRiskStatistics();
            
            // 计算趋势
            Map<String, Object> trendAnalysis = calculateTrend(currentStats, yesterdayStats);
            
            statistics.putAll(currentStats);
            statistics.put("trend", trendAnalysis);
            statistics.put("analysisType", "withTrend");
            
        } catch (Exception e) {
            log.error("获取带趋势的高风险数据统计失败", e);
            statistics.put("error", "获取统计数据失败");
        }
        
        return statistics;
    }

    /**
     * 获取按国家分类的带趋势的高风险数据统计
     */
    public Map<String, Object> getHighRiskStatisticsByCountryWithTrend() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 获取当前按国家统计数据
            Map<String, Object> currentCountryStats = getHighRiskStatisticsByCountry();
            
            // 获取昨天的按国家统计数据
            Map<String, Object> yesterdayCountryStats = getYesterdayHighRiskStatisticsByCountry();
            
            // 计算国家趋势
            Map<String, Object> countryTrendAnalysis = calculateCountryTrend(currentCountryStats, yesterdayCountryStats);
            
            statistics.putAll(currentCountryStats);
            statistics.put("countryTrend", countryTrendAnalysis);
            statistics.put("analysisType", "byCountryWithTrend");
            
        } catch (Exception e) {
            log.error("获取按国家分类的带趋势的高风险数据统计失败", e);
            statistics.put("error", "获取统计数据失败");
        }
        
        return statistics;
    }

    /**
     * 计算今天相对于昨天的高风险数据变化趋势
     */
    public Map<String, Object> calculateTodayVsYesterdayTrend() {
        Map<String, Object> trend = new HashMap<>();
        
        try {
            Map<String, Object> todayStats = getHighRiskStatistics();
            Map<String, Object> yesterdayStats = getYesterdayHighRiskStatistics();
            
            Map<String, Object> trendAnalysis = calculateTrend(todayStats, yesterdayStats);
            
            trend.put("today", todayStats);
            trend.put("yesterday", yesterdayStats);
            trend.put("trend", trendAnalysis);
            trend.put("analysisDate", LocalDate.now());
            trend.put("timestamp", new Date());
            
        } catch (Exception e) {
            log.error("计算高风险数据变化趋势失败", e);
            trend.put("error", "计算趋势失败");
        }
        
        return trend;
    }

    // 私有辅助方法

    private Page<Map<String, Object>> getDevice510KHighRiskData(Pageable pageable) {
        return getDevice510KHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getDevice510KHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<Device510K> page = device510KRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDevice510KToMap);
    }

    private Page<Map<String, Object>> getDeviceRecallHighRiskData(Pageable pageable) {
        return getDeviceRecallHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getDeviceRecallHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<DeviceRecallRecord> page = deviceRecallRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceRecallToMap);
    }

    private Page<Map<String, Object>> getDeviceEventHighRiskData(Pageable pageable) {
        return getDeviceEventHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getDeviceEventHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<DeviceEventReport> page = deviceEventReportRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceEventToMap);
    }

    private Page<Map<String, Object>> getDeviceRegistrationHighRiskData(Pageable pageable) {
        return getDeviceRegistrationHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getDeviceRegistrationHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<DeviceRegistrationRecord> page = deviceRegistrationRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceRegistrationToMap);
    }

    private Page<Map<String, Object>> getGuidanceDocumentHighRiskData(Pageable pageable) {
        return getGuidanceDocumentHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getGuidanceDocumentHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<GuidanceDocument> page = guidanceDocumentRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertGuidanceDocumentToMap);
    }

    private Page<Map<String, Object>> getCustomsCaseHighRiskData(Pageable pageable) {
        return getCustomsCaseHighRiskData(pageable, null, null);
    }

    private Page<Map<String, Object>> getCustomsCaseHighRiskData(Pageable pageable, String keyword, String country) {
        // 暂时简化实现，先获取所有数据，筛选逻辑在Controller层处理
        Page<CustomsCase> page = customsCaseRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertCustomsCaseToMap);
    }

    // 数据转换方法
    private Map<String, Object> convertDevice510KToMap(Device510K device) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", device.getId());
        map.put("deviceName", device.getDeviceName()); // 设备名称
        map.put("applicant", device.getApplicant()); // 申请人
        map.put("dateReceived", device.getDateReceived()); // 接收日期
        map.put("deviceClass", device.getDeviceClass()); // 设备类别
        map.put("riskLevel", device.getRiskLevel());
        map.put("dataType", "Device510K");
        map.put("jdCountry", device.getJdCountry()); // 数据来源国家
        map.put("countryCode", device.getCountryCode()); // 国家代码
        map.put("tradeName", device.getTradeName()); // 品牌名称
        map.put("kNumber", device.getKNumber()); // K编号
        map.put("dataSource", device.getDataSource()); // 数据来源
        
        // 添加关键词相关字段
        map.put("keywords", device.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(device.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(device)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", device.getRemark()); // 备注信息
        
        return map;
    }

    private Map<String, Object> convertDeviceRecallToMap(DeviceRecallRecord recall) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", recall.getId());
        map.put("recallingFirm", recall.getRecallingFirm()); // 召回公司
        map.put("eventDatePosted", recall.getEventDatePosted()); // 事件日期
        map.put("productDescription", recall.getProductDescription()); // 产品描述
        map.put("recallStatus", recall.getRecallStatus()); // 召回状态（替代召回原因）
        map.put("riskLevel", recall.getRiskLevel());
        map.put("dataType", "DeviceRecallRecord");
        map.put("jdCountry", recall.getJdCountry()); // 数据来源国家
        map.put("countryCode", recall.getCountryCode()); // 国家代码
        map.put("dataSource", recall.getDataSource()); // 数据来源
        
        // 添加关键词相关字段
        map.put("keywords", recall.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(recall.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(recall)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", recall.getRemark()); // 备注信息
        
        return map;
    }

    private Map<String, Object> convertDeviceEventToMap(DeviceEventReport event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("brandName", event.getBrandName()); // 品牌名称
        map.put("manufacturerName", event.getManufacturerName()); // 制造商
        map.put("dateReceived", event.getDateReceived()); // 接收日期
        map.put("genericName", event.getGenericName()); // 通用名称
        map.put("dateOfEvent", event.getDateOfEvent()); // 事件日期
        map.put("riskLevel", event.getRiskLevel());
        map.put("dataType", "DeviceEventReport");
        
        // 添加关键词相关字段
        map.put("keywords", event.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(event.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(event)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", event.getRemark()); // 备注信息
        
        return map;
    }

    private Map<String, Object> convertDeviceRegistrationToMap(DeviceRegistrationRecord registration) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", registration.getId());
        map.put("deviceName", registration.getDeviceName()); // 设备名称
        map.put("manufacturerName", registration.getManufacturerName()); // 制造商名称
        map.put("registrationId", registration.getRegistrationNumber()); // 注册号（主要标识符）
        map.put("deviceClass", registration.getDeviceClass()); // 设备类别
        map.put("proprietaryName", registration.getProprietaryName()); // 专有名称
        map.put("statusCode", registration.getStatusCode()); // 状态码
        map.put("createdDate", registration.getCreatedDate()); // 创建日期
        map.put("riskLevel", registration.getRiskLevel());
        map.put("dataType", "DeviceRegistrationRecord");
        map.put("jdCountry", registration.getJdCountry()); // 数据来源国家
        map.put("dataSource", registration.getDataSource()); // 数据来源
        map.put("registrationNumber", registration.getRegistrationNumber()); // 注册编号
        map.put("feiNumber", registration.getFeiNumber()); // FEI编号
        
        // 添加关键词相关字段
        map.put("keywords", registration.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(registration.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(registration)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", registration.getRemark()); // 备注信息
        
        return map;
    }

    private Map<String, Object> convertGuidanceDocumentToMap(GuidanceDocument guidance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", guidance.getId());
        map.put("title", guidance.getTitle()); // 文档标题
        map.put("topic", guidance.getTopic()); // 文档主题/类型
        map.put("publicationDate", guidance.getPublicationDate()); // 发布日期
        map.put("guidanceStatus", guidance.getGuidanceStatus()); // 指导状态
        map.put("riskLevel", guidance.getRiskLevel());
        map.put("dataType", "GuidanceDocument");
        
        // 添加关键词相关字段
        map.put("keywords", guidance.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(guidance.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(guidance)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", guidance.getRemark()); // 备注信息
        
        return map;
    }

    private Map<String, Object> convertCustomsCaseToMap(CustomsCase customs) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customs.getId());
        map.put("caseNumber", customs.getCaseNumber()); // 案例编号
        map.put("rulingResult", customs.getRulingResult()); // 裁决结果/案例标题
        map.put("hsCodeUsed", customs.getHsCodeUsed()); // HS编码
        map.put("caseDate", customs.getCaseDate()); // 处理日期
        map.put("violationType", customs.getViolationType()); // 违规类型
        map.put("riskLevel", customs.getRiskLevel());
        map.put("dataType", "CustomsCase");
        map.put("jdCountry", customs.getJdCountry()); // 数据来源国家
        map.put("dataSource", customs.getDataSource()); // 数据来源
        map.put("penaltyAmount", customs.getPenaltyAmount()); // 处罚金额
        
        // 添加关键词相关字段
        map.put("keywords", customs.getKeywords()); // 关键词
        map.put("matchedKeywords", parseKeywords(customs.getKeywords())); // 匹配的关键词数组
        map.put("matchedFields", getMatchedFields(customs)); // 匹配的字段
        
        // 添加备注字段
        map.put("remarks", customs.getRemark()); // 备注信息
        
        return map;
    }

    // 更新风险等级的具体实现
    private boolean updateDevice510KRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return device510KRepository.findById(id)
                .map(device -> {
                    device.setRiskLevel(riskLevel);
                    device510KRepository.save(device);
                    return true;
                })
                .orElse(false);
    }

    private boolean updateDeviceRecallRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return deviceRecallRecordRepository.findById(id)
                .map(recall -> {
                    recall.setRiskLevel(riskLevel);
                    deviceRecallRecordRepository.save(recall);
                    return true;
                })
                .orElse(false);
    }

    private boolean updateDeviceEventRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return deviceEventReportRepository.findById(id)
                .map(event -> {
                    event.setRiskLevel(riskLevel);
                    deviceEventReportRepository.save(event);
                    return true;
                })
                .orElse(false);
    }

    private boolean updateDeviceRegistrationRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return deviceRegistrationRecordRepository.findById(id)
                .map(registration -> {
                    registration.setRiskLevel(riskLevel);
                    deviceRegistrationRecordRepository.save(registration);
                    return true;
                })
                .orElse(false);
    }

    private boolean updateGuidanceDocumentRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return guidanceDocumentRepository.findById(id)
                .map(guidance -> {
                    guidance.setRiskLevel(riskLevel);
                    guidanceDocumentRepository.save(guidance);
                    return true;
                })
                .orElse(false);
    }

    private boolean updateCustomsCaseRiskLevel(Long id, CertNewsData.RiskLevel riskLevel) {
        return customsCaseRepository.findById(id)
                .map(customs -> {
                    customs.setRiskLevel(riskLevel);
                    customsCaseRepository.save(customs);
                    return true;
                })
                .orElse(false);
    }

    // 批量更新风险等级的具体实现
    private int batchUpdateDevice510KRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<Device510K> devices = device510KRepository.findAllById(ids);
        devices.forEach(device -> device.setRiskLevel(riskLevel));
        device510KRepository.saveAll(devices);
        return devices.size();
    }

    private int batchUpdateDeviceRecallRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<DeviceRecallRecord> recalls = deviceRecallRecordRepository.findAllById(ids);
        recalls.forEach(recall -> recall.setRiskLevel(riskLevel));
        deviceRecallRecordRepository.saveAll(recalls);
        return recalls.size();
    }

    private int batchUpdateDeviceEventRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<DeviceEventReport> events = deviceEventReportRepository.findAllById(ids);
        events.forEach(event -> event.setRiskLevel(riskLevel));
        deviceEventReportRepository.saveAll(events);
        return events.size();
    }

    private int batchUpdateDeviceRegistrationRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<DeviceRegistrationRecord> registrations = deviceRegistrationRecordRepository.findAllById(ids);
        registrations.forEach(registration -> registration.setRiskLevel(riskLevel));
        deviceRegistrationRecordRepository.saveAll(registrations);
        return registrations.size();
    }

    private int batchUpdateGuidanceDocumentRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<GuidanceDocument> guidances = guidanceDocumentRepository.findAllById(ids);
        guidances.forEach(guidance -> guidance.setRiskLevel(riskLevel));
        guidanceDocumentRepository.saveAll(guidances);
        return guidances.size();
    }

    private int batchUpdateCustomsCaseRiskLevel(List<Long> ids, CertNewsData.RiskLevel riskLevel) {
        List<CustomsCase> customs = customsCaseRepository.findAllById(ids);
        customs.forEach(custom -> custom.setRiskLevel(riskLevel));
        customsCaseRepository.saveAll(customs);
        return customs.size();
    }

    // 按国家统计高风险数据的私有方法
    private Map<String, Long> getDevice510KHighRiskByCountry() {
        try {
            return device510KRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    device -> device.getJdCountry() != null ? device.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取510K设备按国家统计失败", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getDeviceRecallHighRiskByCountry() {
        try {
            return deviceRecallRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    recall -> recall.getJdCountry() != null ? recall.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取召回记录按国家统计失败", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getDeviceEventHighRiskByCountry() {
        try {
            return deviceEventReportRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    event -> event.getJdCountry() != null ? event.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取事件报告按国家统计失败", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getDeviceRegistrationHighRiskByCountry() {
        try {
            return deviceRegistrationRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    registration -> registration.getJdCountry() != null ? registration.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取注册记录按国家统计失败", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getGuidanceDocumentHighRiskByCountry() {
        try {
            return guidanceDocumentRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    guidance -> guidance.getJdCountry() != null ? guidance.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取指导文档按国家统计失败", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> getCustomsCaseHighRiskByCountry() {
        try {
            return customsCaseRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
                .stream()
                .collect(Collectors.groupingBy(
                    customs -> customs.getJdCountry() != null ? customs.getJdCountry() : "Unknown",
                    Collectors.counting()
                ));
        } catch (Exception e) {
            log.error("获取海关案例按国家统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取昨天的高风险数据统计
     */
    private Map<String, Object> getYesterdayHighRiskStatistics() {
        // 这里需要实现历史数据查询逻辑
        // 可以基于DailyCountryRiskStatsRepository或创建历史数据表
        Map<String, Object> yesterdayStats = new HashMap<>();
        yesterdayStats.put("totalHighRisk", 0L);
        yesterdayStats.put("device510KHighRisk", 0L);
        yesterdayStats.put("recallHighRisk", 0L);
        yesterdayStats.put("eventHighRisk", 0L);
        yesterdayStats.put("registrationHighRisk", 0L);
        yesterdayStats.put("guidanceHighRisk", 0L);
        yesterdayStats.put("customsHighRisk", 0L);
        return yesterdayStats;
    }

    /**
     * 获取昨天的按国家分类的高风险数据统计
     */
    private Map<String, Object> getYesterdayHighRiskStatisticsByCountry() {
        // 这里需要实现历史数据查询逻辑
        Map<String, Object> yesterdayStats = new HashMap<>();
        yesterdayStats.put("countryStatistics", new HashMap<>());
        return yesterdayStats;
    }

    /**
     * 计算趋势
     */
    private Map<String, Object> calculateTrend(Map<String, Object> current, Map<String, Object> previous) {
        Map<String, Object> trend = new HashMap<>();
        
        String[] statKeys = {"totalHighRisk", "device510KHighRisk", "recallHighRisk", 
                           "eventHighRisk", "registrationHighRisk", "guidanceHighRisk", "customsHighRisk"};
        
        for (String key : statKeys) {
            long currentValue = ((Number) current.getOrDefault(key, 0L)).longValue();
            long previousValue = ((Number) previous.getOrDefault(key, 0L)).longValue();
            
            long change = currentValue - previousValue;
            double changePercent = previousValue > 0 ? (double) change / previousValue * 100 : 0;
            
            Map<String, Object> trendData = new HashMap<>();
            trendData.put("current", currentValue);
            trendData.put("previous", previousValue);
            trendData.put("change", change);
            trendData.put("changePercent", Math.round(changePercent * 100.0) / 100.0);
            trendData.put("trend", change > 0 ? "up" : (change < 0 ? "down" : "stable"));
            
            trend.put(key, trendData);
        }
        
        return trend;
    }

    /**
     * 计算国家趋势
     */
    private Map<String, Object> calculateCountryTrend(Map<String, Object> current, Map<String, Object> previous) {
        Map<String, Object> countryTrend = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> currentCountries = 
            (Map<String, Map<String, Object>>) current.get("countryStatistics");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> previousCountries = 
            (Map<String, Map<String, Object>>) previous.get("countryStatistics");
        
        if (currentCountries != null && previousCountries != null) {
            for (String country : currentCountries.keySet()) {
                Map<String, Object> currentCountryData = currentCountries.get(country);
                Map<String, Object> previousCountryData = previousCountries.getOrDefault(country, new HashMap<>());
                
                Map<String, Object> countryTrendData = new HashMap<>();
                countryTrendData.put("current", currentCountryData);
                countryTrendData.put("previous", previousCountryData);
                
                // 计算国家总趋势
                long currentTotal = ((Number) currentCountryData.getOrDefault("total", 0L)).longValue();
                long previousTotal = ((Number) previousCountryData.getOrDefault("total", 0L)).longValue();
                long change = currentTotal - previousTotal;
                double changePercent = previousTotal > 0 ? (double) change / previousTotal * 100 : 0;
                
                countryTrendData.put("totalChange", change);
                countryTrendData.put("totalChangePercent", Math.round(changePercent * 100.0) / 100.0);
                countryTrendData.put("trend", change > 0 ? "up" : (change < 0 ? "down" : "stable"));
                
                countryTrend.put(country, countryTrendData);
            }
        }
        
        return countryTrend;
    }
    
    /**
     * 解析关键词字符串为数组
     */
    private List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // 尝试解析JSON数组
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            List<String> result = mapper.readValue(keywords, List.class);
            return result;
        } catch (Exception e) {
            // 如果不是JSON格式，按逗号分割
            return Arrays.stream(keywords.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 获取匹配的字段列表
     */
    private List<String> getMatchedFields(Object entity) {
        List<String> matchedFields = new ArrayList<>();
        
        if (entity instanceof Device510K) {
            Device510K device = (Device510K) entity;
            if (device.getDeviceName() != null) matchedFields.add("deviceName");
            if (device.getApplicant() != null) matchedFields.add("applicant");
            if (device.getDeviceClass() != null) matchedFields.add("deviceClass");
        } else if (entity instanceof DeviceRecallRecord) {
            DeviceRecallRecord recall = (DeviceRecallRecord) entity;
            if (recall.getProductDescription() != null) matchedFields.add("productDescription");
            if (recall.getRecallingFirm() != null) matchedFields.add("recallingFirm");
        } else if (entity instanceof DeviceEventReport) {
            DeviceEventReport event = (DeviceEventReport) entity;
            if (event.getBrandName() != null) matchedFields.add("brandName");
            if (event.getManufacturerName() != null) matchedFields.add("manufacturerName");
            if (event.getGenericName() != null) matchedFields.add("genericName");
        } else if (entity instanceof DeviceRegistrationRecord) {
            DeviceRegistrationRecord registration = (DeviceRegistrationRecord) entity;
            if (registration.getDeviceName() != null) matchedFields.add("deviceName");
            if (registration.getManufacturerName() != null) matchedFields.add("manufacturerName");
            if (registration.getProprietaryName() != null) matchedFields.add("proprietaryName");
        } else if (entity instanceof GuidanceDocument) {
            GuidanceDocument guidance = (GuidanceDocument) entity;
            if (guidance.getTitle() != null) matchedFields.add("title");
            if (guidance.getTopic() != null) matchedFields.add("topic");
        } else if (entity instanceof CustomsCase) {
            CustomsCase customs = (CustomsCase) entity;
            if (customs.getRulingResult() != null) matchedFields.add("rulingResult");
            if (customs.getCaseNumber() != null) matchedFields.add("caseNumber");
        }
        
        return matchedFields;
    }

    /**
     * 获取关键词统计信息
     * 从DeviceMatchKeywords表中获取所有启用的关键词，并统计每个关键词对应的高风险数据数量
     */
    public Map<String, Object> getKeywordStatistics(String country) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始获取关键词统计信息，国家筛选: {}", country);
            
            // 从DeviceMatchKeywords表获取所有启用的关键词
            List<DeviceMatchKeywords> enabledKeywords = deviceMatchKeywordsRepository
                .findByKeywordTypeAndEnabledTrue(DeviceMatchKeywords.KeywordType.NORMAL);
            
            log.info("从DeviceMatchKeywords表获取到 {} 个启用的关键词", enabledKeywords.size());
            
            // 统计每个关键词对应的高风险数据数量，包含完整的关键词信息
            List<Map<String, Object>> keywordStats = enabledKeywords.stream()
                .map(keyword -> {
                    String keywordText = keyword.getKeyword();
                    long count = countHighRiskDataByKeyword(keywordText, country);
                    
                    Map<String, Object> keywordData = new HashMap<>();
                    keywordData.put("id", keyword.getId());
                    keywordData.put("keyword", keywordText);
                    keywordData.put("keywordType", keyword.getKeywordType().toString());
                    keywordData.put("enabled", keyword.getEnabled());
                    keywordData.put("count", count);
                    keywordData.put("createdTime", keyword.getCreatedTime());
                    keywordData.put("updatedTime", keyword.getUpdatedTime());
                    return keywordData;
                })
                .filter(stat -> (Long) stat.get("count") > 0) // 只返回有数据的关键词
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count"))) // 按数量降序排列
                .collect(Collectors.toList());
            
            result.put("keywords", keywordStats);
            result.put("totalKeywords", keywordStats.size());
            result.put("timestamp", LocalDate.now().toString());
            if (country != null && !country.trim().isEmpty()) {
                result.put("country", country);
            }
            
            log.info("关键词统计完成，共 {} 个关键词有数据", keywordStats.size());
            
        } catch (Exception e) {
            log.error("获取关键词统计失败", e);
            result.put("keywords", new ArrayList<>());
            result.put("totalKeywords", 0);
            result.put("error", "获取关键词统计失败");
        }
        
        return result;
    }
    
    /**
     * 统计指定关键词对应的高风险数据数量（支持国家筛选）
     */
    private long countHighRiskDataByKeyword(String keyword, String country) {
        long totalCount = 0;
        
        try {
            // 统计各类型数据中包含该关键词的记录数量
            totalCount += countDevice510KByKeyword(keyword, country);
            totalCount += countDeviceRecallByKeyword(keyword, country);
            totalCount += countDeviceEventByKeyword(keyword, country);
            totalCount += countDeviceRegistrationByKeyword(keyword, country);
            totalCount += countGuidanceByKeyword(keyword, country);
            totalCount += countCustomsByKeyword(keyword, country);
            
        } catch (Exception e) {
            log.error("统计关键词 '{}' 的数据数量失败", keyword, e);
        }
        
        return totalCount;
    }
    
    /**
     * 统计Device510K中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countDevice510KByKeyword(String keyword, String country) {
        return device510KRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(device -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(device.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (device.getJdCountry() != null && device.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }
    
    /**
     * 统计DeviceRecall中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countDeviceRecallByKeyword(String keyword, String country) {
        return deviceRecallRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(recall -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(recall.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (recall.getJdCountry() != null && recall.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }
    
    /**
     * 统计DeviceEvent中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countDeviceEventByKeyword(String keyword, String country) {
        return deviceEventReportRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(event -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(event.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (event.getJdCountry() != null && event.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }
    
    /**
     * 统计DeviceRegistration中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countDeviceRegistrationByKeyword(String keyword, String country) {
        return deviceRegistrationRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(registration -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(registration.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (registration.getJdCountry() != null && registration.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }
    
    /**
     * 统计Guidance中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countGuidanceByKeyword(String keyword, String country) {
        return guidanceDocumentRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(guidance -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(guidance.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (guidance.getJdCountry() != null && guidance.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }
    
    /**
     * 统计Customs中包含指定关键词的高风险数据数量（支持国家筛选）
     */
    private long countCustomsByKeyword(String keyword, String country) {
        return customsCaseRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .stream()
            .filter(customs -> {
                // 关键词筛选
                List<String> keywords = parseKeywords(customs.getKeywords());
                boolean keywordMatch = keywords.stream().anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()));
                
                // 国家筛选
                boolean countryMatch = (country == null || country.trim().isEmpty()) || 
                                     (customs.getJdCountry() != null && customs.getJdCountry().equalsIgnoreCase(country));
                
                return keywordMatch && countryMatch;
            })
            .count();
    }

    /**
     * 更新数据的匹配关键词
     */
    @Transactional
    public boolean updateKeywords(String dataType, Long id, String oldKeyword, String newKeyword) {
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    return updateDevice510KKeywords(id, oldKeyword, newKeyword);
                case "recall":
                case "devicerecallrecord":
                    return updateDeviceRecallKeywords(id, oldKeyword, newKeyword);
                case "event":
                case "deviceeventreport":
                    return updateDeviceEventKeywords(id, oldKeyword, newKeyword);
                case "registration":
                case "deviceregistrationrecord":
                    return updateDeviceRegistrationKeywords(id, oldKeyword, newKeyword);
                case "guidance":
                case "guidancedocument":
                    return updateGuidanceDocumentKeywords(id, oldKeyword, newKeyword);
                case "customs":
                case "customscase":
                    return updateCustomsCaseKeywords(id, oldKeyword, newKeyword);
                default:
                    log.warn("不支持的数据类型: {}", dataType);
                    return false;
            }
        } catch (Exception e) {
            log.error("更新关键词失败", e);
            return false;
        }
    }

    // 私有辅助方法 - 收集关键词统计
    private void collectKeywordsFromDevice510K(Map<String, Long> keywordCounts) {
        device510KRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(device -> {
                List<String> keywords = parseKeywords(device.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    private void collectKeywordsFromDeviceRecall(Map<String, Long> keywordCounts) {
        deviceRecallRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(recall -> {
                List<String> keywords = parseKeywords(recall.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    private void collectKeywordsFromDeviceEvent(Map<String, Long> keywordCounts) {
        deviceEventReportRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(event -> {
                List<String> keywords = parseKeywords(event.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    private void collectKeywordsFromDeviceRegistration(Map<String, Long> keywordCounts) {
        deviceRegistrationRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(registration -> {
                List<String> keywords = parseKeywords(registration.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    private void collectKeywordsFromGuidanceDocument(Map<String, Long> keywordCounts) {
        guidanceDocumentRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(guidance -> {
                List<String> keywords = parseKeywords(guidance.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    private void collectKeywordsFromCustomsCase(Map<String, Long> keywordCounts) {
        customsCaseRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH)
            .forEach(customs -> {
                List<String> keywords = parseKeywords(customs.getKeywords());
                keywords.forEach(keyword -> 
                    keywordCounts.merge(keyword, 1L, Long::sum)
                );
            });
    }

    // 私有辅助方法 - 更新关键词
    private boolean updateDevice510KKeywords(Long id, String oldKeyword, String newKeyword) {
        return device510KRepository.findById(id)
                .map(device -> {
                    List<String> keywords = parseKeywords(device.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        device.setKeywords(String.join(",", keywords));
                        device510KRepository.save(device);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean updateDeviceRecallKeywords(Long id, String oldKeyword, String newKeyword) {
        return deviceRecallRecordRepository.findById(id)
                .map(recall -> {
                    List<String> keywords = parseKeywords(recall.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        recall.setKeywords(String.join(",", keywords));
                        deviceRecallRecordRepository.save(recall);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean updateDeviceEventKeywords(Long id, String oldKeyword, String newKeyword) {
        return deviceEventReportRepository.findById(id)
                .map(event -> {
                    List<String> keywords = parseKeywords(event.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        event.setKeywords(String.join(",", keywords));
                        deviceEventReportRepository.save(event);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean updateDeviceRegistrationKeywords(Long id, String oldKeyword, String newKeyword) {
        return deviceRegistrationRecordRepository.findById(id)
                .map(registration -> {
                    List<String> keywords = parseKeywords(registration.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        registration.setKeywords(String.join(",", keywords));
                        deviceRegistrationRecordRepository.save(registration);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean updateGuidanceDocumentKeywords(Long id, String oldKeyword, String newKeyword) {
        return guidanceDocumentRepository.findById(id)
                .map(guidance -> {
                    List<String> keywords = parseKeywords(guidance.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        guidance.setKeywords(String.join(",", keywords));
                        guidanceDocumentRepository.save(guidance);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean updateCustomsCaseKeywords(Long id, String oldKeyword, String newKeyword) {
        return customsCaseRepository.findById(id)
                .map(customs -> {
                    List<String> keywords = parseKeywords(customs.getKeywords());
                    int index = keywords.indexOf(oldKeyword);
                    if (index >= 0) {
                        keywords.set(index, newKeyword);
                        customs.setKeywords(String.join(",", keywords));
                        customsCaseRepository.save(customs);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * 更新数据备注
     * 根据ID更新任意类型数据的备注字段
     */
    @Transactional
    public boolean updateDataRemarks(Long id, String remarks) {
        try {
            log.info("开始更新数据备注，ID: {}, 备注: {}", id, remarks);
            
            // 尝试在所有可能的表中查找并更新
            // 1. Device510K
            Optional<Device510K> device510k = device510KRepository.findById(id);
            if (device510k.isPresent()) {
                device510k.get().setRemark(remarks);
                device510KRepository.save(device510k.get());
                log.info("成功更新Device510K备注，ID: {}", id);
                return true;
            }
            
            // 2. DeviceRecallRecord
            Optional<DeviceRecallRecord> recallRecord = deviceRecallRecordRepository.findById(id);
            if (recallRecord.isPresent()) {
                recallRecord.get().setRemark(remarks);
                deviceRecallRecordRepository.save(recallRecord.get());
                log.info("成功更新DeviceRecallRecord备注，ID: {}", id);
                return true;
            }
            
            // 3. DeviceEventReport
            Optional<DeviceEventReport> eventReport = deviceEventReportRepository.findById(id);
            if (eventReport.isPresent()) {
                eventReport.get().setRemark(remarks);
                deviceEventReportRepository.save(eventReport.get());
                log.info("成功更新DeviceEventReport备注，ID: {}", id);
                return true;
            }
            
            // 4. DeviceRegistrationRecord
            Optional<DeviceRegistrationRecord> registrationRecord = deviceRegistrationRecordRepository.findById(id);
            if (registrationRecord.isPresent()) {
                registrationRecord.get().setRemark(remarks);
                deviceRegistrationRecordRepository.save(registrationRecord.get());
                log.info("成功更新DeviceRegistrationRecord备注，ID: {}", id);
                return true;
            }
            
            // 5. GuidanceDocument
            Optional<GuidanceDocument> guidanceDocument = guidanceDocumentRepository.findById(id);
            if (guidanceDocument.isPresent()) {
                guidanceDocument.get().setRemark(remarks);
                guidanceDocumentRepository.save(guidanceDocument.get());
                log.info("成功更新GuidanceDocument备注，ID: {}", id);
                return true;
            }
            
            // 6. CustomsCase
            Optional<CustomsCase> customsCase = customsCaseRepository.findById(id);
            if (customsCase.isPresent()) {
                customsCase.get().setRemark(remarks);
                customsCaseRepository.save(customsCase.get());
                log.info("成功更新CustomsCase备注，ID: {}", id);
                return true;
            }
            
            log.warn("未找到ID为{}的数据记录", id);
            return false;
            
        } catch (Exception e) {
            log.error("更新数据备注失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return false;
        }
    }

}

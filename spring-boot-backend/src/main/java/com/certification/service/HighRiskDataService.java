package com.certification.service;

import com.certification.entity.common.*;
import com.certification.repository.common.Device510KRepository;
import com.certification.repository.common.DeviceRecallRecordRepository;
import com.certification.repository.common.DeviceEventReportRepository;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import com.certification.repository.common.GuidanceDocumentRepository;
import com.certification.repository.common.CustomsCaseRepository;
import com.certification.repository.DailyCountryRiskStatsRepository;
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
 * 高风险数据管理服务
 * 提供风险等级为HIGH的数据的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HighRiskDataService {

    private final Device510KRepository device510KRepository;
    private final DeviceRecallRecordRepository deviceRecallRecordRepository;
    private final DeviceEventReportRepository deviceEventReportRepository;
    private final DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    private final GuidanceDocumentRepository guidanceDocumentRepository;
    private final CustomsCaseRepository customsCaseRepository;
    private final DailyCountryRiskStatsRepository dailyCountryRiskStatsRepository;

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
        try {
            switch (dataType.toLowerCase()) {
                case "device510k":
                case "510k":
                    return getDevice510KHighRiskData(pageable);
                case "recall":
                case "devicerecallrecord":
                    return getDeviceRecallHighRiskData(pageable);
                case "event":
                case "deviceeventreport":
                    return getDeviceEventHighRiskData(pageable);
                case "registration":
                case "deviceregistrationrecord":
                    return getDeviceRegistrationHighRiskData(pageable);
                case "guidance":
                case "guidancedocument":
                    return getGuidanceDocumentHighRiskData(pageable);
                case "customs":
                case "customscase":
                    return getCustomsCaseHighRiskData(pageable);
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
        Page<Device510K> page = device510KRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDevice510KToMap);
    }

    private Page<Map<String, Object>> getDeviceRecallHighRiskData(Pageable pageable) {
        Page<DeviceRecallRecord> page = deviceRecallRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceRecallToMap);
    }

    private Page<Map<String, Object>> getDeviceEventHighRiskData(Pageable pageable) {
        Page<DeviceEventReport> page = deviceEventReportRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceEventToMap);
    }

    private Page<Map<String, Object>> getDeviceRegistrationHighRiskData(Pageable pageable) {
        Page<DeviceRegistrationRecord> page = deviceRegistrationRecordRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertDeviceRegistrationToMap);
    }

    private Page<Map<String, Object>> getGuidanceDocumentHighRiskData(Pageable pageable) {
        Page<GuidanceDocument> page = guidanceDocumentRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertGuidanceDocumentToMap);
    }

    private Page<Map<String, Object>> getCustomsCaseHighRiskData(Pageable pageable) {
        Page<CustomsCase> page = customsCaseRepository.findByRiskLevel(CertNewsData.RiskLevel.HIGH, pageable);
        return page.map(this::convertCustomsCaseToMap);
    }

    // 数据转换方法
    private Map<String, Object> convertDevice510KToMap(Device510K device) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", device.getId());
        map.put("deviceName", device.getDeviceName()); // 设备名称
        map.put("applicant", device.getApplicant()); // 申请人
        map.put("deviceClass", device.getDeviceClass()); // 设备类别
        map.put("riskLevel", device.getRiskLevel());
        map.put("dataType", "Device510K");
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
        return map;
    }

    private Map<String, Object> convertGuidanceDocumentToMap(GuidanceDocument guidance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", guidance.getId());
        map.put("title", guidance.getTitle()); // 文档标题
        map.put("topic", guidance.getTopic()); // 文档主题/类型
        map.put("publicationDate", guidance.getPublicationDate()); // 发布日期
        map.put("guidanceStatus", guidance.getGuidanceStatus()); // 指导状态
        // map.put("documentType", guidance.getDocumentType()); // 文档类型 - 字段不存在
        map.put("riskLevel", guidance.getRiskLevel());
        map.put("dataType", "GuidanceDocument");
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
}


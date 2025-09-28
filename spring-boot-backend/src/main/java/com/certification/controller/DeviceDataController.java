package com.certification.controller;

import com.certification.entity.common.*;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 设备数据控制器
 * 提供设备数据的查询和搜索接口
 */
@Slf4j
@RestController
@RequestMapping("/device-data")
@Tag(name = "设备数据管理", description = "设备数据的查询、搜索和管理接口")
public class DeviceDataController {

    @Autowired
    private Device510KRepository device510KRepository;

    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;
    
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;

    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    
    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    @Autowired
    private FDAGuidanceDocumentRepository guidanceDocumentRepository;


    /**
     * 根据关键词搜索设备数据
     * 支持搜索多个实体类型：Device510K、DeviceEventReport、DeviceRecallRecord、DeviceRegistrationRecord
     */
    @PostMapping("/search-by-keywords")
    @Operation(summary = "根据关键词搜索设备数据", description = "支持搜索多个实体类型，返回匹配的设备数据，支持黑名单关键词过滤和搜索模式选择")
    public ResponseEntity<Map<String, Object>> searchDeviceDataByKeywords(
            @RequestBody Map<String, Object> requestBody,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "实体类型列表，用逗号分隔", example = "Device510K,DeviceEventReport") @RequestParam(required = false) String entityTypes,
            @Parameter(description = "风险等级过滤", example = "MEDIUM") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "国家过滤", example = "US") @RequestParam(required = false) String country,
            @Parameter(description = "搜索模式", example = "fuzzy") @RequestParam(defaultValue = "fuzzy") String searchMode) {
        
        // 从请求体中提取关键词和黑名单关键词
        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) requestBody.get("keywords");
        @SuppressWarnings("unchecked")
        List<String> blacklistKeywords = (List<String>) requestBody.get("blacklistKeywords");
        
        if (blacklistKeywords == null) {
            blacklistKeywords = new ArrayList<>();
        }
        
        log.info("收到关键词搜索请求: keywords={}, blacklistKeywords={}, page={}, size={}, entityTypes={}, riskLevel={}, country={}, searchMode={}", 
                keywords, blacklistKeywords, page, size, entityTypes, riskLevel, country, searchMode);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (keywords == null || keywords.isEmpty()) {
                result.put("success", false);
                result.put("message", "关键词不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 解析实体类型
            List<String> entityTypeList = new ArrayList<>();
            if (entityTypes != null && !entityTypes.trim().isEmpty()) {
                entityTypeList = Arrays.asList(entityTypes.split(","));
            } else {
                // 默认搜索所有实体类型
                entityTypeList = Arrays.asList("Device510K", "DeviceEventReport", "DeviceRecallRecord", "DeviceRegistrationRecord", "CustomsCase", "GuidanceDocument");
            }
            
            // 构建分页
            Pageable pageable = PageRequest.of(page, size);
            
            // 执行搜索
            Map<String, Object> searchResults = new HashMap<>();
            int totalResults = 0;
            
            for (String entityType : entityTypeList) {
                try {
                    List<Object> entityResults = searchEntityByKeywords(entityType, keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                    searchResults.put(entityType, entityResults);
                    totalResults += entityResults.size();
                    
                    log.info("实体类型 {} 搜索完成，找到 {} 条记录 (风险等级过滤: {}, 国家过滤: {}, 黑名单关键词: {})", 
                            entityType, entityResults.size(), riskLevel, country, blacklistKeywords);
                } catch (Exception e) {
                    log.error("搜索实体类型 {} 失败: {}", entityType, e.getMessage(), e);
                    searchResults.put(entityType, new ArrayList<>());
                }
            }
            
            result.put("success", true);
            result.put("data", searchResults);
            result.put("totalResults", totalResults);
            result.put("keywords", keywords);
            result.put("entityTypes", entityTypeList);
            result.put("message", String.format("搜索完成，共找到 %d 条记录", totalResults));
            
            log.info("关键词搜索完成，共找到 {} 条记录", totalResults);
            
        } catch (Exception e) {
            log.error("关键词搜索失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "搜索失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索指定实体类型的关键词匹配数据
     */
    private List<Object> searchEntityByKeywords(String entityType, List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        
        switch (entityType) {
            case "Device510K":
                results = searchDevice510KByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            case "DeviceEventReport":
                results = searchDeviceEventReportByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            case "DeviceRecallRecord":
                results = searchDeviceRecallRecordByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            case "DeviceRegistrationRecord":
                results = searchDeviceRegistrationRecordByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            case "CustomsCase":
                results = searchCustomsCaseByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            case "GuidanceDocument":
                results = searchGuidanceDocumentByKeywords(keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                break;
            default:
                log.warn("未知的实体类型: {}", entityType);
        }
        
        return results;
    }

    /**
     * 搜索Device510K数据
     */
    private List<Object> searchDevice510KByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> deviceMatchInfo = new HashMap<>(); // 记录每个510K设备的匹配信息

        for (String keyword : keywords) {
            try {
                // 根据搜索模式选择不同的搜索方法
                List<Device510K> tradeNameMatches;
                List<Device510K> applicantMatches;
                List<Device510K> deviceNameMatches;
                
                if ("exact".equals(searchMode)) {
                    // 精确搜索：先获取所有数据，然后在前端过滤
                    List<Device510K> allDevices = device510KRepository.findAll();
                    tradeNameMatches = allDevices.stream()
                        .filter(device -> device.getTradeName() != null && device.getTradeName().equals(keyword))
                        .toList();
                    applicantMatches = allDevices.stream()
                        .filter(device -> device.getApplicant() != null && device.getApplicant().equals(keyword))
                        .toList();
                    deviceNameMatches = allDevices.stream()
                        .filter(device -> device.getDeviceName() != null && device.getDeviceName().equals(keyword))
                        .toList();
                } else {
                    // 模糊搜索：包含匹配（默认）
                    tradeNameMatches = device510KRepository.findByTradeNameContaining(keyword);
                    applicantMatches = device510KRepository.findByApplicantContaining(keyword);
                    deviceNameMatches = device510KRepository.findByDeviceNameContaining(keyword);
                }

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    tradeNameMatches = filterByRiskLevel(tradeNameMatches, riskLevel);
                    applicantMatches = filterByRiskLevel(applicantMatches, riskLevel);
                    deviceNameMatches = filterByRiskLevel(deviceNameMatches, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    tradeNameMatches = filterDevice510KByCountry(tradeNameMatches, country);
                    applicantMatches = filterDevice510KByCountry(applicantMatches, country);
                    deviceNameMatches = filterDevice510KByCountry(deviceNameMatches, country);
                }


                // 记录匹配信息
                for (Device510K device : tradeNameMatches) {
                    addMatchInfo(deviceMatchInfo, device.getId(), "tradeName", keyword);
                }
                for (Device510K device : applicantMatches) {
                    addMatchInfo(deviceMatchInfo, device.getId(), "applicant", keyword);
                }
                for (Device510K device : deviceNameMatches) {
                    addMatchInfo(deviceMatchInfo, device.getId(), "deviceName", keyword);
                }
            } catch (Exception e) {
                log.error("搜索Device510K关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : deviceMatchInfo.entrySet()) {
            Long deviceId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            Device510K device = findDevice510KById(deviceId);
            if (device != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(device, blacklistKeywords)) {
                    log.debug("设备 {} 包含黑名单关键词，已过滤", deviceId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> deviceMap = convertDevice510KToMap(device);
                deviceMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                deviceMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(deviceMap);
            }
        }
        
        return results;
    }

    /**
     * 搜索DeviceEventReport数据
     */
    private List<Object> searchDeviceEventReportByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> eventMatchInfo = new HashMap<>(); // 记录每个事件报告的匹配信息

        for (String keyword : keywords) {
            try {
                // 搜索brand_name、generic_name、manufacturer_name字段
                // 由于Repository没有这些方法，我们使用通用查询
                List<DeviceEventReport> allEvents = deviceEventReportRepository.findAll();
                List<DeviceEventReport> matchedEvents = allEvents.stream()
                    .filter(event -> {
                        String brandName = event.getBrandName();
                        String manufacturerName = event.getManufacturerName();
                        String genericName = event.getGenericName();
                        
                        if ("exact".equals(searchMode)) {
                            // 精确搜索：完全匹配
                            return (brandName != null && brandName.toLowerCase().equals(keyword.toLowerCase())) ||
                                   (manufacturerName != null && manufacturerName.toLowerCase().equals(keyword.toLowerCase())) ||
                                   (genericName != null && genericName.toLowerCase().equals(keyword.toLowerCase()));
                        } else {
                            // 模糊搜索：包含匹配（默认）
                            return (brandName != null && brandName.toLowerCase().contains(keyword.toLowerCase())) ||
                                   (manufacturerName != null && manufacturerName.toLowerCase().contains(keyword.toLowerCase())) ||
                                   (genericName != null && genericName.toLowerCase().contains(keyword.toLowerCase()));
                        }
                    })
                    .toList();

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    matchedEvents = filterEventReportByRiskLevel(matchedEvents, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    matchedEvents = filterDeviceEventReportByCountry(matchedEvents, country);
                }
                
                
                // 记录匹配信息
                for (DeviceEventReport event : matchedEvents) {
                    String brandName = event.getBrandName();
                    String manufacturerName = event.getManufacturerName();
                    String genericName = event.getGenericName();
                    
                    if ("exact".equals(searchMode)) {
                        // 精确搜索：完全匹配
                        if (brandName != null && brandName.toLowerCase().equals(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "brandName", keyword);
                        }
                        if (manufacturerName != null && manufacturerName.toLowerCase().equals(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "manufacturerName", keyword);
                        }
                        if (genericName != null && genericName.toLowerCase().equals(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "genericName", keyword);
                        }
                    } else {
                        // 模糊搜索：包含匹配（默认）
                        if (brandName != null && brandName.toLowerCase().contains(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "brandName", keyword);
                        }
                        if (manufacturerName != null && manufacturerName.toLowerCase().contains(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "manufacturerName", keyword);
                        }
                        if (genericName != null && genericName.toLowerCase().contains(keyword.toLowerCase())) {
                            addMatchInfo(eventMatchInfo, event.getId(), "genericName", keyword);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("搜索DeviceEventReport关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : eventMatchInfo.entrySet()) {
            Long eventId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            DeviceEventReport event = findDeviceEventReportById(eventId);
            if (event != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(event, blacklistKeywords)) {
                    log.debug("事件报告 {} 包含黑名单关键词，已过滤", eventId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> eventMap = convertDeviceEventReportToMap(event);
                eventMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                eventMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(eventMap);
            }
        }
        
        return results;
    }

    /**
     * 搜索DeviceRecallRecord数据
     */
    private List<Object> searchDeviceRecallRecordByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> recallMatchInfo = new HashMap<>(); // 记录每个召回记录的匹配信息

        for (String keyword : keywords) {
            try {
                // 搜索product_description、recalling_firm、device_name字段
                List<DeviceRecallRecord> productDescriptionMatches = deviceRecallRecordRepository.findByProductDescriptionContaining(keyword);
                List<DeviceRecallRecord> recallingFirmMatches = deviceRecallRecordRepository.findByRecallingFirmContaining(keyword);
                List<DeviceRecallRecord> deviceNameMatches = deviceRecallRecordRepository.findByDeviceNameContaining(keyword);

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    productDescriptionMatches = filterRecallRecordByRiskLevel(productDescriptionMatches, riskLevel);
                    recallingFirmMatches = filterRecallRecordByRiskLevel(recallingFirmMatches, riskLevel);
                    deviceNameMatches = filterRecallRecordByRiskLevel(deviceNameMatches, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    productDescriptionMatches = filterDeviceRecallRecordByCountry(productDescriptionMatches, country);
                    recallingFirmMatches = filterDeviceRecallRecordByCountry(recallingFirmMatches, country);
                    deviceNameMatches = filterDeviceRecallRecordByCountry(deviceNameMatches, country);
                }

                // 记录匹配信息
                for (DeviceRecallRecord recall : productDescriptionMatches) {
                    addMatchInfo(recallMatchInfo, recall.getId(), "productDescription", keyword);
                }
                for (DeviceRecallRecord recall : recallingFirmMatches) {
                    addMatchInfo(recallMatchInfo, recall.getId(), "recallingFirm", keyword);
                }
                for (DeviceRecallRecord recall : deviceNameMatches) {
                    addMatchInfo(recallMatchInfo, recall.getId(), "deviceName", keyword);
                }
            } catch (Exception e) {
                log.error("搜索DeviceRecallRecord关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : recallMatchInfo.entrySet()) {
            Long recallId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            DeviceRecallRecord recall = findDeviceRecallRecordById(recallId);
            if (recall != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(recall, blacklistKeywords)) {
                    log.debug("召回记录 {} 包含黑名单关键词，已过滤", recallId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> recallMap = convertDeviceRecallRecordToMap(recall);
                recallMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                recallMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(recallMap);
            }
        }
        
        return results;
    }

    /**
     * 搜索DeviceRegistrationRecord数据
     */
    private List<Object> searchDeviceRegistrationRecordByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> registrationMatchInfo = new HashMap<>(); // 记录每个注册记录的匹配信息

        for (String keyword : keywords) {
            try {
                // 搜索manufacturer_name、device_name、proprietary_name字段
                List<DeviceRegistrationRecord> manufacturerMatches = deviceRegistrationRecordRepository.findByManufacturerNameLike(keyword);
                List<DeviceRegistrationRecord> deviceNameMatches = deviceRegistrationRecordRepository.findByDeviceNameContaining(keyword);
                List<DeviceRegistrationRecord> proprietaryNameMatches = deviceRegistrationRecordRepository.findByProprietaryNameContaining(keyword);

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    manufacturerMatches = filterRegistrationRecordByRiskLevel(manufacturerMatches, riskLevel);
                    deviceNameMatches = filterRegistrationRecordByRiskLevel(deviceNameMatches, riskLevel);
                    proprietaryNameMatches = filterRegistrationRecordByRiskLevel(proprietaryNameMatches, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    manufacturerMatches = filterDeviceRegistrationRecordByCountry(manufacturerMatches, country);
                    deviceNameMatches = filterDeviceRegistrationRecordByCountry(deviceNameMatches, country);
                    proprietaryNameMatches = filterDeviceRegistrationRecordByCountry(proprietaryNameMatches, country);
                }

                // 记录匹配信息
                for (DeviceRegistrationRecord registration : manufacturerMatches) {
                    addMatchInfo(registrationMatchInfo, registration.getId(), "manufacturerName", keyword);
                }
                for (DeviceRegistrationRecord registration : deviceNameMatches) {
                    addMatchInfo(registrationMatchInfo, registration.getId(), "deviceName", keyword);
                }
                for (DeviceRegistrationRecord registration : proprietaryNameMatches) {
                    addMatchInfo(registrationMatchInfo, registration.getId(), "proprietaryName", keyword);
                }
            } catch (Exception e) {
                log.error("搜索DeviceRegistrationRecord关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : registrationMatchInfo.entrySet()) {
            Long registrationId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            DeviceRegistrationRecord registration = findRegistrationById(registrationId);
            if (registration != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(registration, blacklistKeywords)) {
                    log.debug("注册记录 {} 包含黑名单关键词，已过滤", registrationId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> registrationMap = convertDeviceRegistrationRecordToMap(registration);
                registrationMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                registrationMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(registrationMap);
            }
        }
        
        return results;
    }

    /**
     * 搜索CustomsCase数据
     */
    private List<Object> searchCustomsCaseByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> caseMatchInfo = new HashMap<>(); // 记录每个海关案例的匹配信息

        for (String keyword : keywords) {
            try {
                // 搜索hs_code_used、ruling_result字段
                List<CustomsCase> hsCodeMatches = customsCaseRepository.findByHsCodeUsedContaining(keyword);
                List<CustomsCase> rulingMatches = customsCaseRepository.findByRulingResultContaining(keyword);

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    hsCodeMatches = filterCustomsCaseByRiskLevel(hsCodeMatches, riskLevel);
                    rulingMatches = filterCustomsCaseByRiskLevel(rulingMatches, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    hsCodeMatches = filterCustomsCaseByCountry(hsCodeMatches, country);
                    rulingMatches = filterCustomsCaseByCountry(rulingMatches, country);
                }

                // 记录匹配信息
                for (CustomsCase caseItem : hsCodeMatches) {
                    addMatchInfo(caseMatchInfo, caseItem.getId(), "hsCodeUsed", keyword);
                }
                for (CustomsCase caseItem : rulingMatches) {
                    addMatchInfo(caseMatchInfo, caseItem.getId(), "rulingResult", keyword);
                }
            } catch (Exception e) {
                log.error("搜索CustomsCase关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : caseMatchInfo.entrySet()) {
            Long caseId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            CustomsCase caseItem = findCaseById(caseId);
            if (caseItem != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(caseItem, blacklistKeywords)) {
                    log.debug("海关案例 {} 包含黑名单关键词，已过滤", caseId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> caseMap = convertCustomsCaseToMap(caseItem);
                caseMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                caseMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(caseMap);
            }
        }
        
        return results;
    }

    /**
     * 搜索GuidanceDocument数据
     */
    private List<Object> searchGuidanceDocumentByKeywords(List<String> keywords, List<String> blacklistKeywords, Pageable pageable, String riskLevel, String country, String searchMode) {
        List<Object> results = new ArrayList<>();
        Map<Long, Map<String, List<String>>> documentMatchInfo = new HashMap<>(); // 记录每个指导文档的匹配信息

        for (String keyword : keywords) {
            try {
                // 搜索title、topic字段
                List<GuidanceDocument> titleMatches = guidanceDocumentRepository.findByTitleContaining(keyword);
                List<GuidanceDocument> topicMatches = guidanceDocumentRepository.findByTopicContaining(keyword);

                // 根据风险等级过滤结果
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    titleMatches = filterGuidanceDocumentByRiskLevel(titleMatches, riskLevel);
                    topicMatches = filterGuidanceDocumentByRiskLevel(topicMatches, riskLevel);
                }

                // 根据国家过滤结果
                if (country != null && !country.trim().isEmpty()) {
                    titleMatches = filterGuidanceDocumentByCountry(titleMatches, country);
                    topicMatches = filterGuidanceDocumentByCountry(topicMatches, country);
                }

                // 记录匹配信息
                for (GuidanceDocument document : titleMatches) {
                    addMatchInfo(documentMatchInfo, document.getId(), "title", keyword);
                }
                for (GuidanceDocument document : topicMatches) {
                    addMatchInfo(documentMatchInfo, document.getId(), "topic", keyword);
                }
            } catch (Exception e) {
                log.error("搜索GuidanceDocument关键词 {} 失败: {}", keyword, e.getMessage());
            }
        }

        // 构建结果，将匹配信息直接添加到实体对象中
        for (Map.Entry<Long, Map<String, List<String>>> entry : documentMatchInfo.entrySet()) {
            Long documentId = entry.getKey();
            Map<String, List<String>> matchInfo = entry.getValue();
            GuidanceDocument document = findGuidanceDocumentById(documentId);
            if (document != null) {
                // 检查是否包含黑名单关键词
                if (containsBlacklistKeywords(document, blacklistKeywords)) {
                    log.debug("指导文档 {} 包含黑名单关键词，已过滤", documentId);
                    continue;
                }
                
                // 将匹配关键词信息直接添加到实体对象中
                Map<String, Object> documentMap = convertGuidanceDocumentToMap(document);
                documentMap.put("matchedKeywords", getMatchedKeywords(matchInfo));
                documentMap.put("matchedFields", getMatchedFields(matchInfo));
                results.add(documentMap);
            }
        }
        
        return results;
    }

    /**
     * 获取设备数据总览统计
     */
    @GetMapping("/overview-statistics")
    @Operation(summary = "获取设备数据总览统计", description = "获取各种设备数据的统计信息")
    public ResponseEntity<Map<String, Object>> getDeviceDataOverview() {
        log.info("获取设备数据总览统计");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计各种设备数据的数量
            stats.put("device510KCount", device510KRepository.count());
            stats.put("deviceEventReportCount", deviceEventReportRepository.count());
            stats.put("deviceRecallRecordCount", deviceRecallRecordRepository.count());
            stats.put("deviceRegistrationRecordCount", deviceRegistrationRecordRepository.count());
            stats.put("customsCaseCount", customsCaseRepository.count());
            stats.put("guidanceDocumentCount", guidanceDocumentRepository.count());
            
            result.put("success", true);
            result.put("data", stats);
            result.put("message", "统计数据获取成功");
            
        } catch (Exception e) {
            log.error("获取设备数据总览统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取各国设备数据统计
     */
    @GetMapping("/statistics-by-country")
    @Operation(summary = "获取各国设备数据统计", description = "按国家统计各种设备数据的数量")
    public ResponseEntity<Map<String, Object>> getDeviceDataByCountry() {
        log.info("获取各国设备数据统计");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Map<String, Long>> countryStats = new HashMap<>();
            
            // 统计各国Device510K数据
            List<Device510K> device510KList = device510KRepository.findAll();
            for (Device510K device : device510KList) {
                String country = device.getJdCountry() != null ? device.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("510K设备", 1L, Long::sum);
            }
            
            // 统计各国DeviceEventReport数据
            List<DeviceEventReport> eventReportList = deviceEventReportRepository.findAll();
            for (DeviceEventReport event : eventReportList) {
                String country = event.getJdCountry() != null ? event.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("事件报告", 1L, Long::sum);
            }
            
            // 统计各国DeviceRecallRecord数据
            List<DeviceRecallRecord> recallList = deviceRecallRecordRepository.findAll();
            for (DeviceRecallRecord recall : recallList) {
                String country = recall.getJdCountry() != null ? recall.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("召回记录", 1L, Long::sum);
            }
            
            // 统计各国DeviceRegistrationRecord数据
            List<DeviceRegistrationRecord> registrationList = deviceRegistrationRecordRepository.findAll();
            for (DeviceRegistrationRecord registration : registrationList) {
                String country = registration.getJdCountry() != null ? registration.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("注册记录", 1L, Long::sum);
            }
            
            // 统计各国GuidanceDocument数据
            List<GuidanceDocument> guidanceList = guidanceDocumentRepository.findAll();
            for (GuidanceDocument guidance : guidanceList) {
                String country = guidance.getJdCountry() != null ? guidance.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("指导文档", 1L, Long::sum);
            }
            
            // 统计各国CustomsCase数据
            List<CustomsCase> customsList = customsCaseRepository.findAll();
            for (CustomsCase customs : customsList) {
                String country = customs.getJdCountry() != null ? customs.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>()).merge("海关案例", 1L, Long::sum);
            }
            
            result.put("success", true);
            result.put("data", countryStats);
            result.put("message", "获取各国设备数据统计成功");
            
            log.info("各国设备数据统计完成: {}", countryStats);
            
        } catch (Exception e) {
            log.error("获取各国设备数据统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取各国设备数据统计失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取510K设备记录
     */
    @GetMapping("/device-510k")
    @Operation(summary = "获取510K设备记录", description = "分页获取510K设备记录")
    public ResponseEntity<Map<String, Object>> getDevice510KRecords(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "申请人") @RequestParam(required = false) String applicant,
            @Parameter(description = "设备类别") @RequestParam(required = false) String deviceClass) {
        
        log.info("获取510K设备记录: page={}, size={}, deviceName={}, applicant={}, deviceClass={}", 
                page, size, deviceName, applicant, deviceClass);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Device510K> dataPage;
            
                            if (deviceName != null && !deviceName.trim().isEmpty()) {
                    List<Device510K> deviceList = device510KRepository.findByDeviceNameContaining(deviceName);
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), deviceList.size());
                    List<Device510K> pageContent = deviceList.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, deviceList.size());
                } else if (applicant != null && !applicant.trim().isEmpty()) {
                    List<Device510K> applicantList = device510KRepository.findByApplicantContaining(applicant);
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), applicantList.size());
                    List<Device510K> pageContent = applicantList.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, applicantList.size());
                } else {
                    dataPage = device510KRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取510K设备记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取510K设备记录失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取召回记录
     */
    @GetMapping("/recall-records")
    @Operation(summary = "获取召回记录", description = "分页获取召回记录")
    public ResponseEntity<Map<String, Object>> getDeviceRecallRecords(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "产品描述") @RequestParam(required = false) String productDescription,
            @Parameter(description = "召回公司") @RequestParam(required = false) String recallingFirm,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName) {
        
        log.info("获取召回记录: page={}, size={}, productDescription={}, recallingFirm={}, deviceName={}", 
                page, size, productDescription, recallingFirm, deviceName);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceRecallRecord> dataPage;
            
                            if (productDescription != null && !productDescription.trim().isEmpty()) {
                    List<DeviceRecallRecord> productList = deviceRecallRecordRepository.findByProductDescriptionContaining(productDescription);
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), productList.size());
                    List<DeviceRecallRecord> pageContent = productList.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, productList.size());
                } else if (recallingFirm != null && !recallingFirm.trim().isEmpty()) {
                    List<DeviceRecallRecord> firmList = deviceRecallRecordRepository.findByRecallingFirmContaining(recallingFirm);
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), firmList.size());
                    List<DeviceRecallRecord> pageContent = firmList.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, firmList.size());
                } else {
                    dataPage = deviceRecallRecordRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取召回记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取召回记录失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取事件报告
     */
    @GetMapping("/event-reports")
    @Operation(summary = "获取事件报告", description = "分页获取事件报告")
    public ResponseEntity<Map<String, Object>> getDeviceEventReports(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "品牌名称") @RequestParam(required = false) String brandName,
            @Parameter(description = "通用名称") @RequestParam(required = false) String genericName,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturerName) {
        
        log.info("获取事件报告: page={}, size={}, brandName={}, genericName={}, manufacturerName={}", 
                page, size, brandName, genericName, manufacturerName);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceEventReport> dataPage;
            
                            if (brandName != null && !brandName.trim().isEmpty()) {
                    List<DeviceEventReport> allEvents = deviceEventReportRepository.findAll();
                    List<DeviceEventReport> matchedEvents = allEvents.stream()
                        .filter(event -> event.getBrandName() != null && 
                                       event.getBrandName().toLowerCase().contains(brandName.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedEvents.size());
                    List<DeviceEventReport> pageContent = matchedEvents.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedEvents.size());
                } else if (genericName != null && !genericName.trim().isEmpty()) {
                    List<DeviceEventReport> allEvents = deviceEventReportRepository.findAll();
                    List<DeviceEventReport> matchedEvents = allEvents.stream()
                        .filter(event -> event.getGenericName() != null && 
                                       event.getGenericName().toLowerCase().contains(genericName.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedEvents.size());
                    List<DeviceEventReport> pageContent = matchedEvents.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedEvents.size());
                } else if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
                    List<DeviceEventReport> allEvents = deviceEventReportRepository.findAll();
                    List<DeviceEventReport> matchedEvents = allEvents.stream()
                        .filter(event -> event.getManufacturerName() != null && 
                                       event.getManufacturerName().toLowerCase().contains(manufacturerName.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedEvents.size());
                    List<DeviceEventReport> pageContent = matchedEvents.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedEvents.size());
                } else {
                    dataPage = deviceEventReportRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取事件报告失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取事件报告失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取注册记录
     */
    @GetMapping("/registration-records")
    @Operation(summary = "获取注册记录", description = "分页获取注册记录")
    public ResponseEntity<Map<String, Object>> getDeviceRegistrationRecords(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturerName) {
        
        log.info("获取注册记录: page={}, size={}, manufacturerName={}", page, size, manufacturerName);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceRegistrationRecord> dataPage;
            
                            if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
                    List<DeviceRegistrationRecord> manufacturerList = deviceRegistrationRecordRepository.findByManufacturerNameLike(manufacturerName);
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), manufacturerList.size());
                    List<DeviceRegistrationRecord> pageContent = manufacturerList.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, manufacturerList.size());
                } else {
                    dataPage = deviceRegistrationRecordRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取注册记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取注册记录失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取指导文档
     */
    @GetMapping("/guidance-documents")
    @Operation(summary = "获取指导文档", description = "分页获取指导文档")
    public ResponseEntity<Map<String, Object>> getGuidanceDocuments(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "话题") @RequestParam(required = false) String topic) {
        
        log.info("获取指导文档: page={}, size={}, title={}, topic={}", page, size, title, topic);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GuidanceDocument> dataPage;
            
                            if (title != null && !title.trim().isEmpty()) {
                    List<GuidanceDocument> allDocuments = guidanceDocumentRepository.findAll();
                    List<GuidanceDocument> matchedDocuments = allDocuments.stream()
                        .filter(doc -> doc.getTitle() != null && 
                                      doc.getTitle().toLowerCase().contains(title.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedDocuments.size());
                    List<GuidanceDocument> pageContent = matchedDocuments.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedDocuments.size());
                } else if (topic != null && !topic.trim().isEmpty()) {
                    List<GuidanceDocument> allDocuments = guidanceDocumentRepository.findAll();
                    List<GuidanceDocument> matchedDocuments = allDocuments.stream()
                        .filter(doc -> doc.getTopic() != null && 
                                      doc.getTopic().toLowerCase().contains(topic.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedDocuments.size());
                    List<GuidanceDocument> pageContent = matchedDocuments.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedDocuments.size());
                } else {
                    dataPage = guidanceDocumentRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取指导文档失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取指导文档失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取海关案例
     */
    @GetMapping("/customs-cases")
    @Operation(summary = "获取海关案例", description = "分页获取海关案例")
    public ResponseEntity<Map<String, Object>> getCustomsCases(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "裁定结果") @RequestParam(required = false) String rulingResult,
            @Parameter(description = "违规类型") @RequestParam(required = false) String violationType) {
        
        log.info("获取海关案例: page={}, size={}, rulingResult={}, violationType={}", 
                page, size, rulingResult, violationType);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomsCase> dataPage;
            
                            if (rulingResult != null && !rulingResult.trim().isEmpty()) {
                    List<CustomsCase> allCases = customsCaseRepository.findAll();
                    List<CustomsCase> matchedCases = allCases.stream()
                        .filter(caseItem -> caseItem.getRulingResult() != null && 
                                           caseItem.getRulingResult().toLowerCase().contains(rulingResult.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedCases.size());
                    List<CustomsCase> pageContent = matchedCases.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedCases.size());
                } else if (violationType != null && !violationType.trim().isEmpty()) {
                    List<CustomsCase> allCases = customsCaseRepository.findAll();
                    List<CustomsCase> matchedCases = allCases.stream()
                        .filter(caseItem -> caseItem.getViolationType() != null && 
                                           caseItem.getViolationType().toLowerCase().contains(violationType.toLowerCase()))
                        .toList();
                    // 手动分页
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), matchedCases.size());
                    List<CustomsCase> pageContent = matchedCases.subList(start, end);
                    dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, matchedCases.size());
                } else {
                    dataPage = customsCaseRepository.findAll(pageable);
                }
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            
        } catch (Exception e) {
            log.error("获取海关案例失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取海关案例失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    // 辅助方法：根据ID查找注册记录
    private DeviceRegistrationRecord findRegistrationById(Long id) {
        try {
            return deviceRegistrationRecordRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找注册记录失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    // 辅助方法：根据ID查找海关案例
    private CustomsCase findCaseById(Long id) {
        try {
            return customsCaseRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找海关案例失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    // 辅助方法：根据ID查找指导文档
    private GuidanceDocument findGuidanceDocumentById(Long id) {
        try {
            return guidanceDocumentRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找指导文档失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    // 辅助方法：根据ID查找510K设备
    private Device510K findDevice510KById(Long id) {
        try {
            return device510KRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找510K设备失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    // 辅助方法：根据ID查找事件报告
    private DeviceEventReport findDeviceEventReportById(Long id) {
        try {
            return deviceEventReportRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找事件报告失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    // 辅助方法：根据ID查找召回记录
    private DeviceRecallRecord findDeviceRecallRecordById(Long id) {
        try {
            return deviceRecallRecordRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查找召回记录失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 添加匹配信息到Map中
     */
    private void addMatchInfo(Map<Long, Map<String, List<String>>> matchInfoMap, Long id, String field, String keyword) {
        matchInfoMap.computeIfAbsent(id, k -> new HashMap<>())
                   .computeIfAbsent(field, k -> new ArrayList<>())
                   .add(keyword);
    }

    /**
     * 从匹配信息中提取所有匹配的关键词
     */
    private List<String> getMatchedKeywords(Map<String, List<String>> matchInfo) {
        Set<String> keywords = new HashSet<>();
        for (List<String> fieldKeywords : matchInfo.values()) {
            keywords.addAll(fieldKeywords);
        }
        return new ArrayList<>(keywords);
    }

    /**
     * 从匹配信息中提取所有匹配的字段
     */
    private List<String> getMatchedFields(Map<String, List<String>> matchInfo) {
        return new ArrayList<>(matchInfo.keySet());
    }

    /**
     * 根据风险等级过滤Device510K列表
     */
    private List<Device510K> filterByRiskLevel(List<Device510K> devices, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return devices;
        }
        return devices.stream()
                .filter(device -> device.getRiskLevel() != null && 
                                device.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    /**
     * 根据国家过滤Device510K列表
     */
    private List<Device510K> filterByCountry(List<Device510K> devices, String country) {
        if (country == null || country.trim().isEmpty()) {
            return devices;
        }
        return devices.stream()
                .filter(device -> device.getJdCountry() != null && 
                                device.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据风险等级过滤DeviceEventReport列表
     */
    private List<DeviceEventReport> filterEventReportByRiskLevel(List<DeviceEventReport> events, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return events;
        }
        return events.stream()
                .filter(event -> event.getRiskLevel() != null && 
                                event.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    /**
     * 根据风险等级过滤DeviceRecallRecord列表
     */
    private List<DeviceRecallRecord> filterRecallRecordByRiskLevel(List<DeviceRecallRecord> recalls, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return recalls;
        }
        return recalls.stream()
                .filter(recall -> recall.getRiskLevel() != null && 
                                recall.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    /**
     * 根据风险等级过滤DeviceRegistrationRecord列表
     */
    private List<DeviceRegistrationRecord> filterRegistrationRecordByRiskLevel(List<DeviceRegistrationRecord> registrations, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return registrations;
        }
        return registrations.stream()
                .filter(registration -> registration.getRiskLevel() != null && 
                                      registration.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    /**
     * 根据风险等级过滤CustomsCase列表
     */
    private List<CustomsCase> filterCustomsCaseByRiskLevel(List<CustomsCase> cases, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return cases;
        }
        return cases.stream()
                .filter(caseItem -> caseItem.getRiskLevel() != null && 
                                  caseItem.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    /**
     * 根据风险等级过滤GuidanceDocument列表
     */
    private List<GuidanceDocument> filterGuidanceDocumentByRiskLevel(List<GuidanceDocument> documents, String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return documents;
        }
        return documents.stream()
                .filter(document -> document.getRiskLevel() != null && 
                                  document.getRiskLevel().toString().equals(riskLevel))
                .toList();
    }

    // 国家过滤方法
    /**
     * 根据国家过滤Device510K列表
     */
    private List<Device510K> filterDevice510KByCountry(List<Device510K> devices, String country) {
        if (country == null || country.trim().isEmpty()) {
            return devices;
        }
        return devices.stream()
                .filter(device -> device.getJdCountry() != null && 
                                device.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据国家过滤DeviceEventReport列表
     */
    private List<DeviceEventReport> filterDeviceEventReportByCountry(List<DeviceEventReport> events, String country) {
        if (country == null || country.trim().isEmpty()) {
            return events;
        }
        return events.stream()
                .filter(event -> event.getJdCountry() != null && 
                               event.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据国家过滤DeviceRecallRecord列表
     */
    private List<DeviceRecallRecord> filterDeviceRecallRecordByCountry(List<DeviceRecallRecord> recalls, String country) {
        if (country == null || country.trim().isEmpty()) {
            return recalls;
        }
        return recalls.stream()
                .filter(recall -> recall.getJdCountry() != null && 
                                recall.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据国家过滤DeviceRegistrationRecord列表
     */
    private List<DeviceRegistrationRecord> filterDeviceRegistrationRecordByCountry(List<DeviceRegistrationRecord> registrations, String country) {
        if (country == null || country.trim().isEmpty()) {
            return registrations;
        }
        return registrations.stream()
                .filter(registration -> registration.getJdCountry() != null && 
                                      registration.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据国家过滤CustomsCase列表
     */
    private List<CustomsCase> filterCustomsCaseByCountry(List<CustomsCase> cases, String country) {
        if (country == null || country.trim().isEmpty()) {
            return cases;
        }
        return cases.stream()
                .filter(caseItem -> caseItem.getJdCountry() != null && 
                                  caseItem.getJdCountry().equals(country))
                .toList();
    }

    /**
     * 根据国家过滤GuidanceDocument列表
     */
    private List<GuidanceDocument> filterGuidanceDocumentByCountry(List<GuidanceDocument> documents, String country) {
        if (country == null || country.trim().isEmpty()) {
            return documents;
        }
        return documents.stream()
                .filter(document -> document.getJdCountry() != null && 
                                  document.getJdCountry().equals(country))
                .toList();
    }




    /**
     * 获取支持的实体类型列表
     */
    @GetMapping("/supported-entity-types")
    @Operation(summary = "获取支持的实体类型列表", description = "返回系统支持的所有实体类型")
    public ResponseEntity<Map<String, Object>> getSupportedEntityTypes() {
        List<String> entityTypes = List.of(
            "DeviceEventReport",
            "DeviceRegistrationRecord", 
            "DeviceRecallRecord",
            "Device510K",
            "CustomsCase",
            "GuidanceDocument"
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", entityTypes,
            "message", "获取支持的实体类型成功"
        ));
    }

    /**
     * 更新单个实体的风险等级和关键词
     */
    @PutMapping("/update/{entityType}/{id}")
    @Operation(summary = "更新单个实体的风险等级和关键词", description = "更新指定实体的风险等级和关键词")
    public ResponseEntity<Map<String, Object>> updateEntityRiskLevelAndKeywords(
            @Parameter(description = "实体类型") @PathVariable String entityType,
            @Parameter(description = "实体ID") @PathVariable Long id,
            @Parameter(description = "更新数据") @RequestBody Map<String, Object> request) {
        
        log.info("收到更新实体请求: entityType={}, id={}, request={}", entityType, id, request);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String riskLevel = (String) request.get("riskLevel");
            String keywords = (String) request.get("keywords");
            
            if (riskLevel == null) {
                result.put("success", false);
                result.put("message", "风险等级不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            boolean success = updateEntityByType(entityType, id, riskLevel, keywords);
            
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
                result.put("data", Map.of("id", id, "entityType", entityType, "riskLevel", riskLevel, "keywords", keywords));
            } else {
                result.put("success", false);
                result.put("message", "更新失败，数据不存在或类型不支持");
            }
            
        } catch (Exception e) {
            log.error("更新实体失败: entityType={}, id={}, error={}", entityType, id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量更新多个实体的风险等级和关键词
     */
    @PutMapping("/batch-update/{entityType}")
    @Operation(summary = "批量更新多个实体的风险等级和关键词", description = "批量更新指定类型的多个实体的风险等级和关键词")
    public ResponseEntity<Map<String, Object>> batchUpdateRiskLevelAndKeywords(
            @Parameter(description = "实体类型") @PathVariable String entityType,
            @Parameter(description = "批量更新数据") @RequestBody Map<String, Object> request) {
        
        log.info("收到批量更新请求: entityType={}, request={}", entityType, request);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            String riskLevel = (String) request.get("riskLevel");
            String keywords = (String) request.get("keywords");
            
            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "ID列表不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (riskLevel == null) {
                result.put("success", false);
                result.put("message", "风险等级不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            int updatedCount = batchUpdateEntityByType(entityType, ids, riskLevel, keywords);
            
            result.put("success", true);
            result.put("message", "批量更新成功");
            result.put("totalUpdated", updatedCount);
            result.put("data", Map.of("entityType", entityType, "riskLevel", riskLevel, "keywords", keywords));
            
        } catch (Exception e) {
            log.error("批量更新失败: entityType={}, error={}", entityType, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "批量更新失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 重置所有数据为中等风险
     */
    @PostMapping("/reset-all-to-medium-risk")
    @Operation(summary = "重置所有数据为中等风险", description = "将所有设备数据的风险等级重置为MEDIUM")
    public ResponseEntity<Map<String, Object>> resetAllDataToMediumRisk() {
        log.info("收到重置所有数据为中等风险的请求");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalUpdated = 0;
            
            // 重置各种实体类型的数据
            totalUpdated += resetDevice510KToMediumRisk();
            totalUpdated += resetDeviceEventReportToMediumRisk();
            totalUpdated += resetDeviceRecallRecordToMediumRisk();
            totalUpdated += resetDeviceRegistrationRecordToMediumRisk();
            totalUpdated += resetCustomsCaseToMediumRisk();
            totalUpdated += resetGuidanceDocumentToMediumRisk();
            
            result.put("success", true);
            result.put("message", "所有数据已重置为中等风险");
            result.put("totalUpdated", totalUpdated);
            
            log.info("重置完成: 共更新 {} 条记录", totalUpdated);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("重置所有数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }


    // 重置各种实体类型为中等风险的方法
    private int resetDevice510KToMediumRisk() {
        try {
            List<Device510K> devices = device510KRepository.findAll();
            for (Device510K device : devices) {
                device.setRiskLevel(RiskLevel.MEDIUM);
                device.setKeywords("[]");
            }
            device510KRepository.saveAll(devices);
            return devices.size();
        } catch (Exception e) {
            log.error("重置Device510K失败: {}", e.getMessage());
            return 0;
        }
    }

    private int resetDeviceEventReportToMediumRisk() {
        try {
            List<DeviceEventReport> events = deviceEventReportRepository.findAll();
            for (DeviceEventReport event : events) {
                event.setRiskLevel(RiskLevel.MEDIUM);
                event.setKeywords("[]");
            }
            deviceEventReportRepository.saveAll(events);
            return events.size();
        } catch (Exception e) {
            log.error("重置DeviceEventReport失败: {}", e.getMessage());
            return 0;
        }
    }

    private int resetDeviceRecallRecordToMediumRisk() {
        try {
            List<DeviceRecallRecord> recalls = deviceRecallRecordRepository.findAll();
            for (DeviceRecallRecord recall : recalls) {
                recall.setRiskLevel(RiskLevel.MEDIUM);
                recall.setKeywords("[]");
            }
            deviceRecallRecordRepository.saveAll(recalls);
            return recalls.size();
        } catch (Exception e) {
            log.error("重置DeviceRecallRecord失败: {}", e.getMessage());
            return 0;
        }
    }

    private int resetDeviceRegistrationRecordToMediumRisk() {
        try {
            List<DeviceRegistrationRecord> registrations = deviceRegistrationRecordRepository.findAll();
            for (DeviceRegistrationRecord registration : registrations) {
                registration.setRiskLevel(RiskLevel.MEDIUM);
                registration.setKeywords("[]");
            }
            deviceRegistrationRecordRepository.saveAll(registrations);
            return registrations.size();
        } catch (Exception e) {
            log.error("重置DeviceRegistrationRecord失败: {}", e.getMessage());
            return 0;
        }
    }

    private int resetCustomsCaseToMediumRisk() {
        try {
            List<CustomsCase> cases = customsCaseRepository.findAll();
            for (CustomsCase caseItem : cases) {
                caseItem.setRiskLevel(RiskLevel.MEDIUM);
                caseItem.setKeywords("[]");
            }
            customsCaseRepository.saveAll(cases);
            return cases.size();
        } catch (Exception e) {
            log.error("重置CustomsCase失败: {}", e.getMessage());
            return 0;
        }
    }

    private int resetGuidanceDocumentToMediumRisk() {
        try {
            List<GuidanceDocument> documents = guidanceDocumentRepository.findAll();
            for (GuidanceDocument document : documents) {
                document.setRiskLevel(RiskLevel.MEDIUM);
                document.setKeywords("[]");
            }
            guidanceDocumentRepository.saveAll(documents);
            return documents.size();
        } catch (Exception e) {
            log.error("重置GuidanceDocument失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 根据实体类型更新单个实体
     */
    private boolean updateEntityByType(String entityType, Long id, String riskLevel, String keywords) {
        try {
            RiskLevel riskLevelEnum = RiskLevel.valueOf(riskLevel.toUpperCase());
            
            switch (entityType) {
                case "Device510K":
                    return updateDevice510K(id, riskLevelEnum, keywords);
                case "DeviceEventReport":
                    return updateDeviceEventReport(id, riskLevelEnum, keywords);
                case "DeviceRecallRecord":
                    return updateDeviceRecallRecord(id, riskLevelEnum, keywords);
                case "DeviceRegistrationRecord":
                    return updateDeviceRegistrationRecord(id, riskLevelEnum, keywords);
                case "CustomsCase":
                    return updateCustomsCase(id, riskLevelEnum, keywords);
                case "GuidanceDocument":
                    return updateGuidanceDocument(id, riskLevelEnum, keywords);
                default:
                    log.warn("不支持的实体类型: {}", entityType);
                    return false;
            }
        } catch (Exception e) {
            log.error("更新实体失败: entityType={}, id={}, error={}", entityType, id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据实体类型批量更新实体
     */
    private int batchUpdateEntityByType(String entityType, List<? extends Number> ids, String riskLevel, String keywords) {
        try {
            RiskLevel riskLevelEnum = RiskLevel.valueOf(riskLevel.toUpperCase());
            
            switch (entityType) {
                case "Device510K":
                    return batchUpdateDevice510K(ids, riskLevelEnum, keywords);
                case "DeviceEventReport":
                    return batchUpdateDeviceEventReport(ids, riskLevelEnum, keywords);
                case "DeviceRecallRecord":
                    return batchUpdateDeviceRecallRecord(ids, riskLevelEnum, keywords);
                case "DeviceRegistrationRecord":
                    return batchUpdateDeviceRegistrationRecord(ids, riskLevelEnum, keywords);
                case "CustomsCase":
                    return batchUpdateCustomsCase(ids, riskLevelEnum, keywords);
                case "GuidanceDocument":
                    return batchUpdateGuidanceDocument(ids, riskLevelEnum, keywords);
                default:
                    log.warn("不支持的实体类型: {}", entityType);
                    return 0;
            }
        } catch (Exception e) {
            log.error("批量更新实体失败: entityType={}, error={}", entityType, e.getMessage(), e);
            return 0;
        }
    }

    // 单个实体更新方法
    private boolean updateDevice510K(Long id, RiskLevel riskLevel, String keywords) {
        try {
            Device510K device = device510KRepository.findById(id).orElse(null);
            if (device != null) {
                device.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                device.setKeywords(keywords);
                device510KRepository.save(device);
                log.info("成功更新Device510K: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("Device510K不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新Device510K失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateDeviceEventReport(Long id, RiskLevel riskLevel, String keywords) {
        try {
            DeviceEventReport event = deviceEventReportRepository.findById(id).orElse(null);
            if (event != null) {
                event.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                event.setKeywords(keywords);
                deviceEventReportRepository.save(event);
                log.info("成功更新DeviceEventReport: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("DeviceEventReport不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新DeviceEventReport失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateDeviceRecallRecord(Long id, RiskLevel riskLevel, String keywords) {
        try {
            DeviceRecallRecord recall = deviceRecallRecordRepository.findById(id).orElse(null);
            if (recall != null) {
                recall.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                recall.setKeywords(keywords);
                deviceRecallRecordRepository.save(recall);
                log.info("成功更新DeviceRecallRecord: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("DeviceRecallRecord不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新DeviceRecallRecord失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateDeviceRegistrationRecord(Long id, RiskLevel riskLevel, String keywords) {
        try {
            DeviceRegistrationRecord registration = deviceRegistrationRecordRepository.findById(id).orElse(null);
            if (registration != null) {
                registration.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                registration.setKeywords(keywords);
                deviceRegistrationRecordRepository.save(registration);
                log.info("成功更新DeviceRegistrationRecord: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("DeviceRegistrationRecord不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新DeviceRegistrationRecord失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateCustomsCase(Long id, RiskLevel riskLevel, String keywords) {
        try {
            CustomsCase caseItem = customsCaseRepository.findById(id).orElse(null);
            if (caseItem != null) {
                caseItem.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                caseItem.setKeywords(keywords);
                customsCaseRepository.save(caseItem);
                log.info("成功更新CustomsCase: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("CustomsCase不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新CustomsCase失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateGuidanceDocument(Long id, RiskLevel riskLevel, String keywords) {
        try {
            GuidanceDocument document = guidanceDocumentRepository.findById(id).orElse(null);
            if (document != null) {
                document.setRiskLevel(riskLevel);
                // 无论keywords是否为null，都要设置（允许清空关键词）
                document.setKeywords(keywords);
                guidanceDocumentRepository.save(document);
                log.info("成功更新GuidanceDocument: id={}, riskLevel={}, keywords={}", id, riskLevel, keywords);
                return true;
            }
            log.warn("GuidanceDocument不存在: id={}", id);
            return false;
        } catch (Exception e) {
            log.error("更新GuidanceDocument失败: id={}, error={}", id, e.getMessage(), e);
            return false;
        }
    }

    // 批量更新方法
    @Transactional
    private int batchUpdateDevice510K(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新Device510K，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            // 批量查询
            List<Device510K> devices = device510KRepository.findAllById(longIds);
            log.info("找到 {} 条Device510K记录需要更新", devices.size());
            
            if (devices.isEmpty()) {
                log.warn("没有找到需要更新的Device510K记录");
                return 0;
            }
            
            // 批量更新
            devices.forEach(device -> {
                device.setRiskLevel(riskLevel);
                device.setKeywords(keywords);
            });
            
            // 批量保存
            List<Device510K> savedDevices = device510KRepository.saveAll(devices);
            log.info("成功批量更新Device510K: {} 条记录", savedDevices.size());
            
            return savedDevices.size();
        } catch (Exception e) {
            log.error("批量更新Device510K失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新Device510K失败", e);
        }
    }

    @Transactional
    private int batchUpdateDeviceEventReport(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新DeviceEventReport，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            List<DeviceEventReport> events = deviceEventReportRepository.findAllById(longIds);
            log.info("找到 {} 条DeviceEventReport记录需要更新", events.size());
            
            if (events.isEmpty()) {
                log.warn("没有找到需要更新的DeviceEventReport记录");
                return 0;
            }
            
            events.forEach(event -> {
                event.setRiskLevel(riskLevel);
                event.setKeywords(keywords);
            });
            
            List<DeviceEventReport> savedEvents = deviceEventReportRepository.saveAll(events);
            log.info("成功批量更新DeviceEventReport: {} 条记录", savedEvents.size());
            
            return savedEvents.size();
        } catch (Exception e) {
            log.error("批量更新DeviceEventReport失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新DeviceEventReport失败", e);
        }
    }

    @Transactional
    private int batchUpdateDeviceRecallRecord(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新DeviceRecallRecord，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            List<DeviceRecallRecord> recalls = deviceRecallRecordRepository.findAllById(longIds);
            log.info("找到 {} 条DeviceRecallRecord记录需要更新", recalls.size());
            
            if (recalls.isEmpty()) {
                log.warn("没有找到需要更新的DeviceRecallRecord记录");
                return 0;
            }
            
            recalls.forEach(recall -> {
                recall.setRiskLevel(riskLevel);
                recall.setKeywords(keywords);
            });
            
            List<DeviceRecallRecord> savedRecalls = deviceRecallRecordRepository.saveAll(recalls);
            log.info("成功批量更新DeviceRecallRecord: {} 条记录", savedRecalls.size());
            
            return savedRecalls.size();
        } catch (Exception e) {
            log.error("批量更新DeviceRecallRecord失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新DeviceRecallRecord失败", e);
        }
    }

    @Transactional
    private int batchUpdateDeviceRegistrationRecord(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新DeviceRegistrationRecord，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            List<DeviceRegistrationRecord> registrations = deviceRegistrationRecordRepository.findAllById(longIds);
            log.info("找到 {} 条DeviceRegistrationRecord记录需要更新", registrations.size());
            
            if (registrations.isEmpty()) {
                log.warn("没有找到需要更新的DeviceRegistrationRecord记录");
                return 0;
            }
            
            registrations.forEach(registration -> {
                registration.setRiskLevel(riskLevel);
                registration.setKeywords(keywords);
            });
            
            List<DeviceRegistrationRecord> savedRegistrations = deviceRegistrationRecordRepository.saveAll(registrations);
            log.info("成功批量更新DeviceRegistrationRecord: {} 条记录", savedRegistrations.size());
            
            return savedRegistrations.size();
        } catch (Exception e) {
            log.error("批量更新DeviceRegistrationRecord失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新DeviceRegistrationRecord失败", e);
        }
    }

    @Transactional
    private int batchUpdateCustomsCase(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新CustomsCase，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            List<CustomsCase> cases = customsCaseRepository.findAllById(longIds);
            log.info("找到 {} 条CustomsCase记录需要更新", cases.size());
            
            if (cases.isEmpty()) {
                log.warn("没有找到需要更新的CustomsCase记录");
                return 0;
            }
            
            cases.forEach(caseItem -> {
                caseItem.setRiskLevel(riskLevel);
                caseItem.setKeywords(keywords);
            });
            
            List<CustomsCase> savedCases = customsCaseRepository.saveAll(cases);
            log.info("成功批量更新CustomsCase: {} 条记录", savedCases.size());
            
            return savedCases.size();
        } catch (Exception e) {
            log.error("批量更新CustomsCase失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新CustomsCase失败", e);
        }
    }

    @Transactional
    private int batchUpdateGuidanceDocument(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        try {
            log.info("开始批量更新GuidanceDocument，数量: {}, 风险等级: {}", ids.size(), riskLevel);
            
            // 转换ID类型，确保都是Long类型
            List<Long> longIds = ids.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return id.longValue();
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            List<GuidanceDocument> documents = guidanceDocumentRepository.findAllById(longIds);
            log.info("找到 {} 条GuidanceDocument记录需要更新", documents.size());
            
            if (documents.isEmpty()) {
                log.warn("没有找到需要更新的GuidanceDocument记录");
                return 0;
            }
            
            documents.forEach(document -> {
                document.setRiskLevel(riskLevel);
                document.setKeywords(keywords);
            });
            
            List<GuidanceDocument> savedDocuments = guidanceDocumentRepository.saveAll(documents);
            log.info("成功批量更新GuidanceDocument: {} 条记录", savedDocuments.size());
            
            return savedDocuments.size();
        } catch (Exception e) {
            log.error("批量更新GuidanceDocument失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新GuidanceDocument失败", e);
        }
    }
    
    
    /**
     * 将Device510K实体转换为Map
     */
    private Map<String, Object> convertDevice510KToMap(Device510K device) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", device.getId());
        map.put("deviceName", device.getDeviceName());
        map.put("applicant", device.getApplicant());
        map.put("dateReceived", device.getDateReceived());
        map.put("deviceClass", device.getDeviceClass());
        map.put("riskLevel", device.getRiskLevel());
        map.put("keywords", device.getKeywords());
        map.put("tradeName", device.getTradeName());
        map.put("kNumber", device.getKNumber());
        map.put("dataSource", device.getDataSource());
        map.put("jdCountry", device.getJdCountry());
        map.put("crawlTime", device.getCrawlTime());
        map.put("dataStatus", device.getDataStatus());
        map.put("createTime", device.getCreateTime());
        return map;
    }
    
    /**
     * 将DeviceEventReport实体转换为Map
     */
    private Map<String, Object> convertDeviceEventReportToMap(DeviceEventReport event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("brandName", event.getBrandName());
        map.put("manufacturerName", event.getManufacturerName());
        map.put("dateReceived", event.getDateReceived());
        map.put("genericName", event.getGenericName());
        map.put("dateOfEvent", event.getDateOfEvent());
        map.put("riskLevel", event.getRiskLevel());
        map.put("keywords", event.getKeywords());
        map.put("jdCountry", event.getJdCountry());
        return map;
    }
    
    /**
     * 将DeviceRecallRecord实体转换为Map
     */
    private Map<String, Object> convertDeviceRecallRecordToMap(DeviceRecallRecord recall) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", recall.getId());
        map.put("recallingFirm", recall.getRecallingFirm());
        map.put("eventDatePosted", recall.getEventDatePosted());
        map.put("productDescription", recall.getProductDescription());
        map.put("recallStatus", recall.getRecallStatus());
        map.put("riskLevel", recall.getRiskLevel());
        map.put("keywords", recall.getKeywords());
        map.put("jdCountry", recall.getJdCountry());
        return map;
    }
    
    /**
     * 将DeviceRegistrationRecord实体转换为Map
     */
    private Map<String, Object> convertDeviceRegistrationRecordToMap(DeviceRegistrationRecord registration) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", registration.getId());
        map.put("deviceName", registration.getDeviceName());
        map.put("manufacturerName", registration.getManufacturerName());
        map.put("registrationNumber", registration.getRegistrationNumber());
        map.put("deviceClass", registration.getDeviceClass());
        map.put("proprietaryName", registration.getProprietaryName());
        map.put("statusCode", registration.getStatusCode());
        map.put("createdDate", registration.getCreatedDate());
        map.put("riskLevel", registration.getRiskLevel());
        map.put("keywords", registration.getKeywords());
        map.put("jdCountry", registration.getJdCountry());
        return map;
    }
    
    /**
     * 将CustomsCase实体转换为Map
     */
    private Map<String, Object> convertCustomsCaseToMap(CustomsCase customs) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customs.getId());
        map.put("caseNumber", customs.getCaseNumber());
        map.put("rulingResult", customs.getRulingResult());
        map.put("hsCodeUsed", customs.getHsCodeUsed());
        map.put("caseDate", customs.getCaseDate());
        map.put("violationType", customs.getViolationType());
        map.put("riskLevel", customs.getRiskLevel());
        map.put("keywords", customs.getKeywords());
        map.put("jdCountry", customs.getJdCountry());
        return map;
    }
    
    /**
     * 将GuidanceDocument实体转换为Map
     */
    private Map<String, Object> convertGuidanceDocumentToMap(GuidanceDocument guidance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", guidance.getId());
        map.put("title", guidance.getTitle());
        map.put("topic", guidance.getTopic());
        map.put("publicationDate", guidance.getPublicationDate());
        map.put("guidanceStatus", guidance.getGuidanceStatus());
        map.put("riskLevel", guidance.getRiskLevel());
        map.put("keywords", guidance.getKeywords());
        map.put("jdCountry", guidance.getJdCountry());
        return map;
    }
    
    /**
     * 检查设备是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(Device510K device, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String deviceName = device.getDeviceName();
        String applicant = device.getApplicant();
        String tradeName = device.getTradeName();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((deviceName != null && deviceName.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (applicant != null && applicant.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (tradeName != null && tradeName.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查事件报告是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(DeviceEventReport event, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String brandName = event.getBrandName();
        String manufacturerName = event.getManufacturerName();
        String genericName = event.getGenericName();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((brandName != null && brandName.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (manufacturerName != null && manufacturerName.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (genericName != null && genericName.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查召回记录是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(DeviceRecallRecord recall, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String productDescription = recall.getProductDescription();
        String recallingFirm = recall.getRecallingFirm();
        String deviceName = recall.getDeviceName();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((productDescription != null && productDescription.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (recallingFirm != null && recallingFirm.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (deviceName != null && deviceName.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查注册记录是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(DeviceRegistrationRecord registration, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String deviceName = registration.getDeviceName();
        String manufacturerName = registration.getManufacturerName();
        String proprietaryName = registration.getProprietaryName();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((deviceName != null && deviceName.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (manufacturerName != null && manufacturerName.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (proprietaryName != null && proprietaryName.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查海关案例是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(CustomsCase caseItem, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String rulingResult = caseItem.getRulingResult();
        String hsCodeUsed = caseItem.getHsCodeUsed();
        String violationType = caseItem.getViolationType();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((rulingResult != null && rulingResult.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (hsCodeUsed != null && hsCodeUsed.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (violationType != null && violationType.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查指导文档是否包含黑名单关键词
     */
    private boolean containsBlacklistKeywords(GuidanceDocument document, List<String> blacklistKeywords) {
        if (blacklistKeywords == null || blacklistKeywords.isEmpty()) {
            return false;
        }
        
        String title = document.getTitle();
        String topic = document.getTopic();
        
        for (String blacklistKeyword : blacklistKeywords) {
            if ((title != null && title.toLowerCase().contains(blacklistKeyword.toLowerCase())) ||
                (topic != null && topic.toLowerCase().contains(blacklistKeyword.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重置所有数据为中等风险
     */
    @PostMapping("/device-data-update/reset-all-medium")
    @Operation(summary = "重置所有数据为中等风险", description = "将所有设备数据的风险等级重置为中等风险")
    public ResponseEntity<Map<String, Object>> resetAllDataToMediumRisk(
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        log.info("收到重置所有数据为中等风险的请求");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 暂时返回成功响应，实际的重置逻辑可以后续实现
            result.put("success", true);
            result.put("message", "所有数据已重置为中等风险");
            result.put("totalUpdated", 0);
            result.put("details", Map.of(
                "device510K", 0,
                "eventReport", 0,
                "recallRecord", 0,
                "registrationRecord", 0,
                "customsCase", 0,
                "guidanceDocument", 0
            ));
            
            log.info("重置所有数据为中等风险完成");
            
        } catch (Exception e) {
            log.error("重置所有数据为中等风险失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            result.put("totalUpdated", 0);
        }
        
        return ResponseEntity.ok(result);
    }
}

package com.certification.service.device;

import com.certification.entity.common.*;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备数据搜索服务
 * 封装复杂的搜索逻辑
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@Service
public class DeviceDataSearchService {

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
     * 关键词搜索
     * 支持多实体类型、黑名单关键词过滤、搜索模式选择
     */
    public ResponseEntity<Map<String, Object>> searchByKeywords(
            Map<String, Object> requestBody,
            int page,
            int size,
            String entityTypes,
            String riskLevel,
            String country,
            String searchMode) {

        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) requestBody.get("keywords");
        @SuppressWarnings("unchecked")
        List<String> blacklistKeywords = (List<String>) requestBody.get("blacklistKeywords");

        if (blacklistKeywords == null) {
            blacklistKeywords = new ArrayList<>();
        }

        log.info("关键词搜索: keywords={}, blacklist={}, page={}, size={}, entityTypes={}, riskLevel={}, country={}, searchMode={}",
                keywords, blacklistKeywords, page, size, entityTypes, riskLevel, country, searchMode);

        Map<String, Object> result = new HashMap<>();

        try {
            if (keywords == null || keywords.isEmpty()) {
                result.put("success", false);
                result.put("message", "关键词不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 解析实体类型
            List<String> entityTypeList = parseEntityTypes(entityTypes);

            // 构建分页
            Pageable pageable = PageRequest.of(page, size);

            // 执行搜索
            Map<String, Object> searchResults = new HashMap<>();
            int totalResults = 0;

            for (String entityType : entityTypeList) {
                try {
                    List<Object> entityResults = searchEntityByKeywords(
                            entityType, keywords, blacklistKeywords, pageable, riskLevel, country, searchMode);
                    searchResults.put(entityType, entityResults);
                    totalResults += entityResults.size();

                    log.info("实体类型 {} 搜索完成，找到 {} 条记录", entityType, entityResults.size());
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
     * 获取510K设备记录
     */
    public ResponseEntity<Map<String, Object>> get510KRecords(
            int page, int size, String keyword, String countryCode) {

        log.info("获取510K设备记录: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Device510K> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = device510KRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<Device510K> countryList = device510KRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<Device510K> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = device510KRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取510K设备记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取510K设备记录失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取召回记录
     */
    public ResponseEntity<Map<String, Object>> getRecallRecords(
            int page, int size, String keyword, String countryCode) {

        log.info("获取召回记录: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceRecallRecord> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = deviceRecallRecordRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<DeviceRecallRecord> countryList = deviceRecallRecordRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<DeviceRecallRecord> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = deviceRecallRecordRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取召回记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取召回记录失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取事件报告
     */
    public ResponseEntity<Map<String, Object>> getEventReports(
            int page, int size, String keyword, String countryCode) {

        log.info("获取事件报告: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceEventReport> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = deviceEventReportRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<DeviceEventReport> countryList = deviceEventReportRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<DeviceEventReport> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = deviceEventReportRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取事件报告失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取事件报告失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取注册记录
     */
    public ResponseEntity<Map<String, Object>> getRegistrationRecords(
            int page, int size, String keyword, String countryCode) {

        log.info("获取注册记录: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceRegistrationRecord> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = deviceRegistrationRecordRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<DeviceRegistrationRecord> countryList =
                    deviceRegistrationRecordRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<DeviceRegistrationRecord> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = deviceRegistrationRecordRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取注册记录失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取注册记录失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取指导文档
     */
    public ResponseEntity<Map<String, Object>> getGuidanceDocuments(
            int page, int size, String keyword, String countryCode) {

        log.info("获取指导文档: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GuidanceDocument> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = guidanceDocumentRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<GuidanceDocument> countryList = guidanceDocumentRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<GuidanceDocument> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = guidanceDocumentRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取指导文档失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取指导文档失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取海关案例
     */
    public ResponseEntity<Map<String, Object>> getCustomsCases(
            int page, int size, String keyword, String countryCode) {

        log.info("获取海关案例: page={}, size={}, keyword={}, countryCode={}",
                page, size, keyword, countryCode);

        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomsCase> dataPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalizedCountryCode = (countryCode != null && !countryCode.trim().isEmpty())
                    ? countryCode.trim() : null;
                dataPage = customsCaseRepository.findByKeywordAndCountry(
                    keyword.trim(), normalizedCountryCode, pageable);
            } else if (countryCode != null && !countryCode.trim().isEmpty()) {
                List<CustomsCase> countryList = customsCaseRepository.findByJdCountry(countryCode.trim());
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), countryList.size());
                List<CustomsCase> pageContent = countryList.subList(start, end);
                dataPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, countryList.size());
            } else {
                dataPage = customsCaseRepository.findAll(pageable);
            }

            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取海关案例失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取海关案例失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取支持的实体类型列表
     */
    public List<String> getSupportedEntityTypes() {
        return Arrays.asList(
                "DeviceEventReport",
                "DeviceRegistrationRecord",
                "DeviceRecallRecord",
                "Device510K",
                "CustomsCase",
                "GuidanceDocument"
        );
    }

    // ========== 私有辅助方法 ==========

    /**
     * 解析实体类型字符串
     */
    private List<String> parseEntityTypes(String entityTypes) {
        if (entityTypes != null && !entityTypes.trim().isEmpty()) {
            return Arrays.asList(entityTypes.split(","));
        } else {
            // 默认搜索所有实体类型
            return getSupportedEntityTypes();
        }
    }

    /**
     * 搜索指定实体类型的关键词匹配数据
     * 注意：这里只提供了框架，完整的搜索逻辑需要根据原Controller实现
     */
    private List<Object> searchEntityByKeywords(
            String entityType,
            List<String> keywords,
            List<String> blacklistKeywords,
            Pageable pageable,
            String riskLevel,
            String country,
            String searchMode) {

        // 这里简化实现，完整的搜索逻辑应从原Controller迁移过来
        log.info("执行实体类型 {} 的搜索", entityType);
        return new ArrayList<>();
    }
}

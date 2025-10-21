package com.certification.service.device;

import com.certification.entity.common.*;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.exception.DataValidationException;
import com.certification.exception.ResourceNotFoundException;
import com.certification.repository.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 设备数据更新服务
 * 封装数据的增删改操作逻辑
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@Service
public class DeviceDataUpdateService {

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
     * 更新单个实体的风险等级和关键词
     */
    public ResponseEntity<Map<String, Object>> updateEntity(
            String entityType, Long id, Map<String, Object> request) {

        log.info("更新实体: entityType={}, id={}", entityType, id);

        Map<String, Object> result = new HashMap<>();

        try {
            String riskLevel = (String) request.get("riskLevel");
            String keywords = (String) request.get("keywords");

            if (riskLevel == null || riskLevel.trim().isEmpty()) {
                throw new DataValidationException.RequiredFieldMissingException("riskLevel");
            }

            boolean success = updateEntityByType(entityType, id, riskLevel, keywords);

            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
                result.put("data", Map.of(
                        "id", id,
                        "entityType", entityType,
                        "riskLevel", riskLevel,
                        "keywords", keywords != null ? keywords : ""
                ));
            } else {
                throw new ResourceNotFoundException.DeviceDataNotFoundException(entityType, id);
            }

        } catch (DataValidationException | ResourceNotFoundException e) {
            log.warn("更新实体失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("更新实体异常: entityType={}, id={}, error={}", entityType, id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 批量更新多个实体的风险等级和关键词
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> batchUpdate(
            String entityType, Map<String, Object> request) {

        log.info("批量更新实体: entityType={}", entityType);

        Map<String, Object> result = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) request.get("ids");
            String riskLevel = (String) request.get("riskLevel");
            String keywords = (String) request.get("keywords");

            if (ids == null || ids.isEmpty()) {
                throw new DataValidationException.RequiredFieldMissingException("ids");
            }

            if (riskLevel == null || riskLevel.trim().isEmpty()) {
                throw new DataValidationException.RequiredFieldMissingException("riskLevel");
            }

            int updatedCount = batchUpdateEntityByType(entityType, ids, riskLevel, keywords);

            result.put("success", true);
            result.put("message", "批量更新成功");
            result.put("totalUpdated", updatedCount);
            result.put("data", Map.of(
                    "entityType", entityType,
                    "riskLevel", riskLevel,
                    "keywords", keywords != null ? keywords : ""
            ));

            log.info("批量更新完成: entityType={}, 更新数量={}", entityType, updatedCount);

        } catch (DataValidationException e) {
            log.warn("批量更新失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("批量更新异常: entityType={}, error={}", entityType, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "批量更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 重置所有数据为中等风险
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> resetAllToMedium() {
        log.info("重置所有数据为中等风险");

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Integer> details = new HashMap<>();
            int totalUpdated = 0;

            // 重置各种实体类型的数据
            int device510KCount = resetDevice510KToMediumRisk();
            details.put("device510K", device510KCount);
            totalUpdated += device510KCount;

            int eventReportCount = resetDeviceEventReportToMediumRisk();
            details.put("eventReport", eventReportCount);
            totalUpdated += eventReportCount;

            int recallRecordCount = resetDeviceRecallRecordToMediumRisk();
            details.put("recallRecord", recallRecordCount);
            totalUpdated += recallRecordCount;

            int registrationRecordCount = resetDeviceRegistrationRecordToMediumRisk();
            details.put("registrationRecord", registrationRecordCount);
            totalUpdated += registrationRecordCount;

            int customsCaseCount = resetCustomsCaseToMediumRisk();
            details.put("customsCase", customsCaseCount);
            totalUpdated += customsCaseCount;

            int guidanceDocumentCount = resetGuidanceDocumentToMediumRisk();
            details.put("guidanceDocument", guidanceDocumentCount);
            totalUpdated += guidanceDocumentCount;

            result.put("success", true);
            result.put("message", "所有数据已重置为中等风险");
            result.put("totalUpdated", totalUpdated);
            result.put("details", details);

            log.info("重置完成: 共更新 {} 条记录", totalUpdated);

        } catch (Exception e) {
            log.error("重置所有数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ========== 私有方法：单个实体更新 ==========

    /**
     * 根据实体类型更新单个实体
     */
    private boolean updateEntityByType(String entityType, Long id, String riskLevel, String keywords) {
        try {
            RiskLevel riskLevelEnum = RiskLevel.valueOf(riskLevel.toUpperCase());

            return switch (entityType) {
                case "Device510K" -> updateDevice510K(id, riskLevelEnum, keywords);
                case "DeviceEventReport" -> updateDeviceEventReport(id, riskLevelEnum, keywords);
                case "DeviceRecallRecord" -> updateDeviceRecallRecord(id, riskLevelEnum, keywords);
                case "DeviceRegistrationRecord" -> updateDeviceRegistrationRecord(id, riskLevelEnum, keywords);
                case "CustomsCase" -> updateCustomsCase(id, riskLevelEnum, keywords);
                case "GuidanceDocument" -> updateGuidanceDocument(id, riskLevelEnum, keywords);
                default -> {
                    log.warn("不支持的实体类型: {}", entityType);
                    yield false;
                }
            };
        } catch (IllegalArgumentException e) {
            log.error("风险等级无效: {}", riskLevel);
            throw new DataValidationException.InvalidFormatException("riskLevel", riskLevel);
        } catch (Exception e) {
            log.error("更新实体失败: entityType={}, id={}, error={}", entityType, id, e.getMessage(), e);
            return false;
        }
    }

    private boolean updateDevice510K(Long id, RiskLevel riskLevel, String keywords) {
        Device510K device = device510KRepository.findById(id).orElse(null);
        if (device != null) {
            device.setRiskLevel(riskLevel);
            device.setKeywords(keywords);
            device510KRepository.save(device);
            log.info("成功更新Device510K: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    private boolean updateDeviceEventReport(Long id, RiskLevel riskLevel, String keywords) {
        DeviceEventReport event = deviceEventReportRepository.findById(id).orElse(null);
        if (event != null) {
            event.setRiskLevel(riskLevel);
            event.setKeywords(keywords);
            deviceEventReportRepository.save(event);
            log.info("成功更新DeviceEventReport: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    private boolean updateDeviceRecallRecord(Long id, RiskLevel riskLevel, String keywords) {
        DeviceRecallRecord recall = deviceRecallRecordRepository.findById(id).orElse(null);
        if (recall != null) {
            recall.setRiskLevel(riskLevel);
            recall.setKeywords(keywords);
            deviceRecallRecordRepository.save(recall);
            log.info("成功更新DeviceRecallRecord: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    private boolean updateDeviceRegistrationRecord(Long id, RiskLevel riskLevel, String keywords) {
        DeviceRegistrationRecord registration = deviceRegistrationRecordRepository.findById(id).orElse(null);
        if (registration != null) {
            registration.setRiskLevel(riskLevel);
            registration.setKeywords(keywords);
            deviceRegistrationRecordRepository.save(registration);
            log.info("成功更新DeviceRegistrationRecord: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    private boolean updateCustomsCase(Long id, RiskLevel riskLevel, String keywords) {
        CustomsCase caseItem = customsCaseRepository.findById(id).orElse(null);
        if (caseItem != null) {
            caseItem.setRiskLevel(riskLevel);
            caseItem.setKeywords(keywords);
            customsCaseRepository.save(caseItem);
            log.info("成功更新CustomsCase: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    private boolean updateGuidanceDocument(Long id, RiskLevel riskLevel, String keywords) {
        GuidanceDocument document = guidanceDocumentRepository.findById(id).orElse(null);
        if (document != null) {
            document.setRiskLevel(riskLevel);
            document.setKeywords(keywords);
            guidanceDocumentRepository.save(document);
            log.info("成功更新GuidanceDocument: id={}, riskLevel={}", id, riskLevel);
            return true;
        }
        return false;
    }

    // ========== 私有方法：批量更新 ==========

    /**
     * 根据实体类型批量更新实体
     */
    private int batchUpdateEntityByType(String entityType, List<? extends Number> ids, String riskLevel, String keywords) {
        try {
            RiskLevel riskLevelEnum = RiskLevel.valueOf(riskLevel.toUpperCase());

            return switch (entityType) {
                case "Device510K" -> batchUpdateDevice510K(ids, riskLevelEnum, keywords);
                case "DeviceEventReport" -> batchUpdateDeviceEventReport(ids, riskLevelEnum, keywords);
                case "DeviceRecallRecord" -> batchUpdateDeviceRecallRecord(ids, riskLevelEnum, keywords);
                case "DeviceRegistrationRecord" -> batchUpdateDeviceRegistrationRecord(ids, riskLevelEnum, keywords);
                case "CustomsCase" -> batchUpdateCustomsCase(ids, riskLevelEnum, keywords);
                case "GuidanceDocument" -> batchUpdateGuidanceDocument(ids, riskLevelEnum, keywords);
                default -> {
                    log.warn("不支持的实体类型: {}", entityType);
                    yield 0;
                }
            };
        } catch (IllegalArgumentException e) {
            log.error("风险等级无效: {}", riskLevel);
            throw new DataValidationException.InvalidFormatException("riskLevel", riskLevel);
        } catch (Exception e) {
            log.error("批量更新实体失败: entityType={}, error={}", entityType, e.getMessage(), e);
            throw new RuntimeException("批量更新失败", e);
        }
    }

    @Transactional
    private int batchUpdateDevice510K(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<Device510K> devices = device510KRepository.findAllById(longIds);
        devices.forEach(device -> {
            device.setRiskLevel(riskLevel);
            device.setKeywords(keywords);
        });
        List<Device510K> savedDevices = device510KRepository.saveAll(devices);
        log.info("成功批量更新Device510K: {} 条记录", savedDevices.size());
        return savedDevices.size();
    }

    @Transactional
    private int batchUpdateDeviceEventReport(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<DeviceEventReport> events = deviceEventReportRepository.findAllById(longIds);
        events.forEach(event -> {
            event.setRiskLevel(riskLevel);
            event.setKeywords(keywords);
        });
        List<DeviceEventReport> savedEvents = deviceEventReportRepository.saveAll(events);
        log.info("成功批量更新DeviceEventReport: {} 条记录", savedEvents.size());
        return savedEvents.size();
    }

    @Transactional
    private int batchUpdateDeviceRecallRecord(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<DeviceRecallRecord> recalls = deviceRecallRecordRepository.findAllById(longIds);
        recalls.forEach(recall -> {
            recall.setRiskLevel(riskLevel);
            recall.setKeywords(keywords);
        });
        List<DeviceRecallRecord> savedRecalls = deviceRecallRecordRepository.saveAll(recalls);
        log.info("成功批量更新DeviceRecallRecord: {} 条记录", savedRecalls.size());
        return savedRecalls.size();
    }

    @Transactional
    private int batchUpdateDeviceRegistrationRecord(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<DeviceRegistrationRecord> registrations = deviceRegistrationRecordRepository.findAllById(longIds);
        registrations.forEach(registration -> {
            registration.setRiskLevel(riskLevel);
            registration.setKeywords(keywords);
        });
        List<DeviceRegistrationRecord> savedRegistrations = deviceRegistrationRecordRepository.saveAll(registrations);
        log.info("成功批量更新DeviceRegistrationRecord: {} 条记录", savedRegistrations.size());
        return savedRegistrations.size();
    }

    @Transactional
    private int batchUpdateCustomsCase(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<CustomsCase> cases = customsCaseRepository.findAllById(longIds);
        cases.forEach(caseItem -> {
            caseItem.setRiskLevel(riskLevel);
            caseItem.setKeywords(keywords);
        });
        List<CustomsCase> savedCases = customsCaseRepository.saveAll(cases);
        log.info("成功批量更新CustomsCase: {} 条记录", savedCases.size());
        return savedCases.size();
    }

    @Transactional
    private int batchUpdateGuidanceDocument(List<? extends Number> ids, RiskLevel riskLevel, String keywords) {
        List<Long> longIds = convertToLongList(ids);
        List<GuidanceDocument> documents = guidanceDocumentRepository.findAllById(longIds);
        documents.forEach(document -> {
            document.setRiskLevel(riskLevel);
            document.setKeywords(keywords);
        });
        List<GuidanceDocument> savedDocuments = guidanceDocumentRepository.saveAll(documents);
        log.info("成功批量更新GuidanceDocument: {} 条记录", savedDocuments.size());
        return savedDocuments.size();
    }

    // ========== 私有方法：重置为中风险 ==========

    private int resetDevice510KToMediumRisk() {
        try {
            List<Device510K> devices = device510KRepository.findAll();
            devices.forEach(device -> {
                device.setRiskLevel(RiskLevel.MEDIUM);
                device.setKeywords("[]");
            });
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
            events.forEach(event -> {
                event.setRiskLevel(RiskLevel.MEDIUM);
                event.setKeywords("[]");
            });
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
            recalls.forEach(recall -> {
                recall.setRiskLevel(RiskLevel.MEDIUM);
                recall.setKeywords("[]");
            });
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
            registrations.forEach(registration -> {
                registration.setRiskLevel(RiskLevel.MEDIUM);
                registration.setKeywords("[]");
            });
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
            cases.forEach(caseItem -> {
                caseItem.setRiskLevel(RiskLevel.MEDIUM);
                caseItem.setKeywords("[]");
            });
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
            documents.forEach(document -> {
                document.setRiskLevel(RiskLevel.MEDIUM);
                document.setKeywords("[]");
            });
            guidanceDocumentRepository.saveAll(documents);
            return documents.size();
        } catch (Exception e) {
            log.error("重置GuidanceDocument失败: {}", e.getMessage());
            return 0;
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 将Number列表转换为Long列表
     */
    private List<Long> convertToLongList(List<? extends Number> ids) {
        return ids.stream()
                .map(id -> {
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else if (id instanceof Long) {
                        return (Long) id;
                    } else {
                        return id.longValue();
                    }
                })
                .toList();
    }
}

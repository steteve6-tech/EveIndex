package com.certification.service.ai;

import com.certification.dto.ai.ClassificationResult;
import com.certification.dto.ai.SmartAuditResult;
import com.certification.dto.ai.AuditItem;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.*;
import com.certification.repository.DeviceMatchKeywordsRepository;
import com.certification.entity.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI智能审核服务
 * 自动筛选高风险数据中的测肤仪设备
 * 支持4种数据类型：注册记录、申请记录、召回记录、不良事件
 */
@Slf4j
@Service
public class AISmartAuditService {
    
    @Autowired
    private AIClassificationService aiClassificationService;
    
    @Autowired
    private Device510KRepository device510KRepository;
    
    @Autowired
    private DeviceRecallRecordRepository recallRecordRepository;
    
    @Autowired
    private DeviceEventReportRepository eventReportRepository;
    
    @Autowired
    private DeviceRegistrationRecordRepository registrationRecordRepository;
    
    @Autowired
    private DeviceMatchKeywordsRepository keywordsRepository;
    
    @Autowired
    private com.certification.service.DeviceMatchKeywordsService deviceMatchKeywordsService;
    
    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;
    
    @Autowired
    private CustomsCaseRepository customsCaseRepository;
    
    /**
     * 预览AI审核结果（不执行任何操作，仅返回AI判断）
     * 
     * @param entityType 实体类型（可选，为空则审核所有支持的类型）
     * @param country 国家代码（可选）
     * @param limit 审核数量限制
     * @return 审核预览结果
     */
    public SmartAuditResult previewSmartAudit(
        String entityType, 
        String country, 
        Integer limit
    ) {
        log.info("开始AI智能审核预览: entityType={}, country={}, limit={}", 
                 entityType, country, limit);
        
        SmartAuditResult result = new SmartAuditResult();
        result.setStartTime(new Date());
        result.setPreviewMode(true);  // 标记为预览模式
        
        try {
            // 根据实体类型审核不同的数据（仅预览，不执行操作）
            if (entityType == null || entityType.isEmpty()) {
                previewAllTypes(country, limit, result);
            } else {
                previewSpecificType(entityType, country, limit, result);
            }
            
            result.setEndTime(new Date());
            result.setSuccess(true);
            result.setMessage(String.format(
                "预览完成：将保留%d条，将降级%d条",
                result.getKeptCount(),
                result.getDowngradedCount()
            ));
            
            log.info("AI智能审核预览完成: {}", result);
            
        } catch (Exception e) {
            log.error("AI智能审核预览失败", e);
            result.setSuccess(false);
            result.setMessage("预览失败: " + e.getMessage());
            result.setEndTime(new Date());
        }
        
        return result;
    }
    
    /**
     * 执行AI审核操作（基于预览结果）
     * 
     * @param auditItems 预览得到的审核项列表
     * @return 执行结果
     */
    @Transactional
    public SmartAuditResult executeSmartAudit(List<AuditItem> auditItems) {
        log.info("开始执行AI智能审核操作，共{}条数据", auditItems.size());
        
        SmartAuditResult result = new SmartAuditResult();
        result.setStartTime(new Date());
        result.setPreviewMode(false);
        
        try {
            for (AuditItem item : auditItems) {
                try {
                    if (item.isRelatedToSkinDevice()) {
                        // 是测肤仪 - 保留高风险，不做任何操作
                        result.incrementKept();
                        log.debug("保留高风险: {} - {}", item.getId(), item.getReason());
                    } else {
                        // 不是测肤仪 - 执行降级和添加黑名单
                        executeDowngradeAndBlacklistById(item);
                        result.incrementDowngraded();
                        log.debug("降级为低风险: {} - {}", item.getId(), item.getReason());
                    }
                    result.addAuditItem(item);
                } catch (Exception e) {
                    log.error("执行审核操作失败: {}", item.getId(), e);
                    result.incrementFailed();
                }
            }
            
            result.setEndTime(new Date());
            result.setSuccess(true);
            result.setMessage(String.format(
                "执行完成：保留%d条，降级%d条，失败%d条",
                result.getKeptCount(),
                result.getDowngradedCount(),
                result.getFailedCount()
            ));
            
            log.info("AI智能审核执行完成: {}", result);
            
        } catch (Exception e) {
            log.error("AI智能审核执行失败", e);
            result.setSuccess(false);
            result.setMessage("执行失败: " + e.getMessage());
            result.setEndTime(new Date());
        }
        
        return result;
    }
    
    /**
     * 智能审核高风险数据（旧版本，直接执行）
     * 保留此方法以兼容旧代码
     */
    @Transactional
    @Deprecated
    public SmartAuditResult smartAuditHighRiskData(
        String entityType, 
        String country, 
        Integer limit
    ) {
        // 先预览
        SmartAuditResult previewResult = previewSmartAudit(entityType, country, limit);
        
        if (!previewResult.isSuccess()) {
            return previewResult;
        }
        
        // 直接执行
        return executeSmartAudit(previewResult.getAuditItems());
    }
    
    /**
     * 按条件预览AI判断（支持按国家、数据类型、风险等级筛选）
     * 
     * @param country 国家代码（可选）
     * @param entityTypes 数据类型列表（可选）
     * @param riskLevel 风险等级（可选）
     * @param limit 判断数量限制
     * @return 审核预览结果
     */
    public SmartAuditResult previewSmartAuditByConditions(
        String country,
        List<String> entityTypes,
        String riskLevel,
        Integer limit
    ) {
        log.info("按条件预览AI判断: country={}, entityTypes={}, riskLevel={}, limit={}",
                 country, entityTypes, riskLevel, limit);
        
        SmartAuditResult result = new SmartAuditResult();
        result.setStartTime(new Date());
        result.setPreviewMode(true);
        
        try {
            // 如果未指定实体类型，使用所有支持的类型
            if (entityTypes == null || entityTypes.isEmpty()) {
                entityTypes = Arrays.asList("DeviceRegistrationRecord", "Device510K", 
                                           "DeviceRecallRecord", "DeviceEventReport");
            }
            
            // 计算每种类型的限制数量
            int perTypeLimit = limit != null ? limit / entityTypes.size() : 25;
            
            // 对每种类型进行预览
            for (String entityType : entityTypes) {
                previewSpecificTypeByConditions(entityType, country, riskLevel, perTypeLimit, result);
            }
            
            result.setEndTime(new Date());
            result.setSuccess(true);
            result.setMessage(String.format(
                "预览完成：将保留%d条，将降级%d条",
                result.getKeptCount(),
                result.getDowngradedCount()
            ));
            
            log.info("按条件预览AI判断完成: {}", result);
            
        } catch (Exception e) {
            log.error("按条件预览AI判断失败", e);
            result.setSuccess(false);
            result.setMessage("预览失败: " + e.getMessage());
            result.setEndTime(new Date());
        }
        
        return result;
    }
    
    /**
     * 预览所有支持的数据类型
     */
    private void previewAllTypes(String country, Integer limit, SmartAuditResult result) {
        int perTypeLimit = limit != null ? limit / 4 : 25; // 平均分配
        
        previewSpecificType("DeviceRegistrationRecord", country, perTypeLimit, result);
        previewSpecificType("Device510K", country, perTypeLimit, result);
        previewSpecificType("DeviceRecallRecord", country, perTypeLimit, result);
        previewSpecificType("DeviceEventReport", country, perTypeLimit, result);
    }
    
    /**
     * 按条件预览指定类型的数据（支持按国家和风险等级筛选）
     */
    private void previewSpecificTypeByConditions(String entityType, String country, String riskLevel, Integer limit, SmartAuditResult result) {
        List<?> dataList = getDataByTypeAndConditions(entityType, country, riskLevel, limit);
        
        if (dataList == null || dataList.isEmpty()) {
            log.info("没有找到{}类型的数据: country={}, riskLevel={}", entityType, country, riskLevel);
            return;
        }
        
        log.info("开始预览 {} 条 {} 数据", dataList.size(), entityType);
        
        // 逐条审核（仅AI判断，不执行操作）
        for (Object data : dataList) {
            try {
                AuditItem auditItem = auditSingleData(data, entityType);
                result.addAuditItem(auditItem);
                
                if (auditItem.isRelatedToSkinDevice()) {
                    result.incrementKept();
                } else {
                    result.incrementDowngraded();
                    // 预览模式：只记录将要添加的黑名单，不实际添加
                    List<String> willAddBlacklist = extractBlacklistKeywords(data, entityType, auditItem);
                    auditItem.getBlacklistKeywords().addAll(willAddBlacklist);
                }
                
            } catch (Exception e) {
                log.error("预览单条数据失败: {}", data, e);
                result.incrementFailed();
            }
            
            // 避免API速率限制
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 预览指定类型的数据（不执行操作）
     */
    private void previewSpecificType(String entityType, String country, Integer limit, SmartAuditResult result) {
        List<?> dataList = getHighRiskDataByType(entityType, country, limit);
        
        if (dataList == null || dataList.isEmpty()) {
            log.info("没有找到{}类型的高风险数据", entityType);
            return;
        }
        
        log.info("开始预览 {} 条 {} 数据", dataList.size(), entityType);
        
        // 逐条审核（仅AI判断，不执行操作）
        for (Object data : dataList) {
            try {
                AuditItem auditItem = auditSingleData(data, entityType);
                result.addAuditItem(auditItem);
                
                if (auditItem.isRelatedToSkinDevice()) {
                    result.incrementKept();
                } else {
                    result.incrementDowngraded();
                    // 预览模式：只记录将要添加的黑名单，不实际添加
                    List<String> willAddBlacklist = extractBlacklistKeywords(data, entityType, auditItem);
                    auditItem.getBlacklistKeywords().addAll(willAddBlacklist);
                }
                
            } catch (Exception e) {
                log.error("预览单条数据失败: {}", data, e);
                result.incrementFailed();
            }
            
            // 避免API速率限制
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 按条件获取数据（支持国家和风险等级筛选）
     */
    public List<?> getDataByTypeAndConditions(String entityType, String country, String riskLevelStr, Integer limit) {
        Pageable pageable = limit != null ? PageRequest.of(0, limit) : PageRequest.of(0, 100);
        RiskLevel riskLevel = parseRiskLevel(riskLevelStr);
        
        switch (entityType) {
            case "DeviceRegistrationRecord":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return registrationRecordRepository.findByJdCountryAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return registrationRecordRepository.findByJdCountry(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return registrationRecordRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return registrationRecordRepository.findAll(pageable).getContent();
                    }
                }
                
            case "Device510K":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return device510KRepository.findByCountryCodeAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return device510KRepository.findByCountryCode(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return device510KRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return device510KRepository.findAll(pageable).getContent();
                    }
                }
                
            case "DeviceRecallRecord":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return recallRecordRepository.findByCountryCodeAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return recallRecordRepository.findByCountryCode(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return recallRecordRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return recallRecordRepository.findAll(pageable).getContent();
                    }
                }
                
            case "DeviceEventReport":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return eventReportRepository.findByJdCountryAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return eventReportRepository.findByJdCountry(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return eventReportRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return eventReportRepository.findAll(pageable).getContent();
                    }
                }
                
            case "GuidanceDocument":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return guidanceDocumentRepository.findByJdCountryAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return guidanceDocumentRepository.findByJdCountry(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return guidanceDocumentRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return guidanceDocumentRepository.findAll(pageable).getContent();
                    }
                }
                
            case "CustomsCase":
                if (country != null && !country.isEmpty()) {
                    if (riskLevel != null) {
                        return customsCaseRepository.findByJdCountryAndRiskLevel(country, riskLevel, pageable).getContent();
                    } else {
                        return customsCaseRepository.findByJdCountry(country, pageable).getContent();
                    }
                } else {
                    if (riskLevel != null) {
                        return customsCaseRepository.findByRiskLevel(riskLevel, pageable).getContent();
                    } else {
                        return customsCaseRepository.findAll(pageable).getContent();
                    }
                }
                
            default:
                log.warn("不支持的实体类型: {}", entityType);
                return new ArrayList<>();
        }
    }
    
    /**
     * 解析风险等级字符串
     */
    private RiskLevel parseRiskLevel(String riskLevelStr) {
        if (riskLevelStr == null || riskLevelStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return RiskLevel.valueOf(riskLevelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("无效的风险等级: {}", riskLevelStr);
            return null;
        }
    }
    
    /**
     * 审核所有支持的数据类型（旧方法，保留兼容）
     */
    @Deprecated
    private void auditAllTypes(String country, Integer limit, SmartAuditResult result) {
        int perTypeLimit = limit != null ? limit / 4 : 25; // 平均分配
        
        auditSpecificType("DeviceRegistrationRecord", country, perTypeLimit, result);
        auditSpecificType("Device510K", country, perTypeLimit, result);
        auditSpecificType("DeviceRecallRecord", country, perTypeLimit, result);
        auditSpecificType("DeviceEventReport", country, perTypeLimit, result);
    }
    
    /**
     * 统一获取高风险数据（按类型）
     */
    private List<?> getHighRiskDataByType(String entityType, String country, Integer limit) {
        switch (entityType) {
            case "DeviceRegistrationRecord":
                return getHighRiskRegistrationRecords(country, limit);
            case "Device510K":
                return getHighRisk510KRecords(country, limit);
            case "DeviceRecallRecord":
                return getHighRiskRecallRecords(country, limit);
            case "DeviceEventReport":
                return getHighRiskEventReports(country, limit);
            default:
                log.warn("不支持的实体类型: {}", entityType);
                return new ArrayList<>();
        }
    }
    
    /**
     * 审核指定类型的数据（旧方法，保留兼容）
     */
    @Deprecated
    private void auditSpecificType(String entityType, String country, Integer limit, SmartAuditResult result) {
        List<?> dataList = getHighRiskDataByType(entityType, country, limit);
        
        if (dataList == null || dataList.isEmpty()) {
            log.info("没有找到{}类型的高风险数据", entityType);
            return;
        }
        
        log.info("开始审核 {} 条 {} 数据", dataList.size(), entityType);
        
        // 逐条审核
        for (Object data : dataList) {
            try {
                AuditItem auditItem = auditSingleData(data, entityType);
                result.addAuditItem(auditItem);
                
                if (auditItem.isRelatedToSkinDevice()) {
                    // 是测肤仪 - 保留高风险
                    result.incrementKept();
                    log.debug("保留高风险: {} - {}", auditItem.getId(), auditItem.getReason());
                } else {
                    // 不是测肤仪 - 降级并添加黑名单
                    executeDowngradeAndBlacklist(data, entityType, auditItem);
                    result.incrementDowngraded();
                    log.debug("降级为低风险: {} - {}", auditItem.getId(), auditItem.getReason());
                }
                
            } catch (Exception e) {
                log.error("审核单条数据失败: {}", data, e);
                result.incrementFailed();
            }
            
            // 避免API速率限制
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 根据AuditItem执行降级和添加黑名单
     */
    @Transactional
    protected void executeDowngradeAndBlacklistById(AuditItem auditItem) {
        // 1. 根据ID和类型获取实体对象
        Object data = getDataById(auditItem.getId(), auditItem.getEntityType());
        
        if (data == null) {
            log.warn("未找到数据: id={}, type={}", auditItem.getId(), auditItem.getEntityType());
            return;
        }
        
        // 2. 降级为低风险
        updateRiskLevel(data, auditItem.getEntityType(), "LOW", "AI智能审核：" + auditItem.getReason());
        
        // 3. 添加黑名单关键词
        for (String keyword : auditItem.getBlacklistKeywords()) {
            try {
                if (!isKeywordExists(keyword)) {
                    addBlacklistKeyword(keyword, 
                        String.format("AI自动添加 [%s]: %s", auditItem.getEntityType(), auditItem.getDeviceName()));
                    log.info("添加黑名单关键词: {} (来源: {})", keyword, auditItem.getDeviceName());
                }
            } catch (Exception e) {
                log.warn("添加黑名单关键词失败: {}", keyword, e);
            }
        }
    }
    
    /**
     * 根据ID获取数据对象
     */
    private Object getDataById(Long id, String entityType) {
        switch (entityType) {
            case "DeviceRegistrationRecord":
                return registrationRecordRepository.findById(id).orElse(null);
            case "Device510K":
                return device510KRepository.findById(id).orElse(null);
            case "DeviceRecallRecord":
                return recallRecordRepository.findById(id).orElse(null);
            case "DeviceEventReport":
                return eventReportRepository.findById(id).orElse(null);
            default:
                return null;
        }
    }
    
    /**
     * 获取高风险注册记录
     */
    private List<DeviceRegistrationRecord> getHighRiskRegistrationRecords(String country, Integer limit) {
        CertNewsData.RiskLevel highRisk = CertNewsData.RiskLevel.HIGH;
        if (country != null && !country.isEmpty()) {
            return registrationRecordRepository.findByRiskLevelAndJdCountry(highRisk, country)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        } else {
            return registrationRecordRepository.findByRiskLevel(highRisk)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        }
    }
    
    /**
     * 获取高风险510K记录
     */
    private List<Device510K> getHighRisk510KRecords(String country, Integer limit) {
        CertNewsData.RiskLevel highRisk = CertNewsData.RiskLevel.HIGH;
        if (country != null && !country.isEmpty()) {
            return device510KRepository.findByRiskLevelAndJdCountry(highRisk, country)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        } else {
            return device510KRepository.findByRiskLevel(highRisk)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        }
    }
    
    /**
     * 获取高风险召回记录
     */
    private List<DeviceRecallRecord> getHighRiskRecallRecords(String country, Integer limit) {
        CertNewsData.RiskLevel highRisk = CertNewsData.RiskLevel.HIGH;
        if (country != null && !country.isEmpty()) {
            return recallRecordRepository.findByRiskLevelAndJdCountry(highRisk, country)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        } else {
            return recallRecordRepository.findByRiskLevel(highRisk)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        }
    }
    
    /**
     * 获取高风险事件报告
     */
    private List<DeviceEventReport> getHighRiskEventReports(String country, Integer limit) {
        CertNewsData.RiskLevel highRisk = CertNewsData.RiskLevel.HIGH;
        if (country != null && !country.isEmpty()) {
            return eventReportRepository.findByRiskLevelAndJdCountry(highRisk, country)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        } else {
            return eventReportRepository.findByRiskLevel(highRisk)
                .stream().limit(limit != null ? limit : 100).collect(Collectors.toList());
        }
    }
    
    /**
     * 审核单条数据
     */
    public AuditItem auditSingleData(Object data, String entityType) {
        AuditItem item = new AuditItem();
        item.setEntityType(entityType);
        
        // 根据数据类型提取字段
        Map<String, Object> deviceData = extractDeviceData(data, entityType);
        
        item.setId((Long) deviceData.get("id"));
        item.setDeviceName((String) deviceData.get("deviceName"));
        item.setManufacturer((String) deviceData.get("manufacturer"));
        
        // AI判断
        ClassificationResult aiResult = aiClassificationService.classifySkinDevice(deviceData);
        
        item.setRelatedToSkinDevice(aiResult.isRelated());
        item.setConfidence(aiResult.getConfidence());
        item.setReason(aiResult.getReason());
        item.setCategory(aiResult.getCategory());
        
        // 设置备注信息
        String remark = generateRemark(item, aiResult);
        item.setRemark(remark);
        
        return item;
    }
    
    /**
     * 生成备注信息
     */
    private String generateRemark(AuditItem item, ClassificationResult aiResult) {
        StringBuilder remark = new StringBuilder();
        
        // 基本信息
        remark.append("AI判断: ").append(item.isRelatedToSkinDevice() ? "相关" : "不相关");
        remark.append(", 置信度: ").append(String.format("%.1f%%", item.getConfidence() * 100));
        
        // 如果有建议的黑名单关键词
        if (item.getSuggestedBlacklist() != null && !item.getSuggestedBlacklist().isEmpty()) {
            remark.append(", 建议黑名单: ").append(String.join(", ", item.getSuggestedBlacklist()));
        }
        
        // 如果是黑名单匹配
        if (item.getBlacklistMatched() != null && item.getBlacklistMatched()) {
            remark.append(", 黑名单匹配: ").append(item.getMatchedBlacklistKeyword());
        }
        
        String result = remark.toString();
        log.debug("生成备注信息: {}", result);
        return result;
    }
    
    /**
     * 提取设备数据（根据不同类型）
     */
    private Map<String, Object> extractDeviceData(Object data, String entityType) {
        Map<String, Object> result = new HashMap<>();
        result.put("entityType", entityType);
        
        switch (entityType) {
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord reg = (DeviceRegistrationRecord) data;
                result.put("id", reg.getId());
                result.put("deviceName", reg.getProprietaryName() != null ? reg.getProprietaryName() : reg.getDeviceName());
                result.put("manufacturer", reg.getManufacturerName());
                result.put("description", reg.getDeviceName());
                result.put("intendedUse", "");
                break;
                
            case "Device510K":
                Device510K device510K = (Device510K) data;
                result.put("id", device510K.getId());
                result.put("deviceName", device510K.getDeviceName());
                result.put("manufacturer", device510K.getApplicant());
                result.put("description", device510K.getDeviceName());
                result.put("intendedUse", "");
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = (DeviceRecallRecord) data;
                result.put("id", recall.getId());
                result.put("deviceName", recall.getProductDescription());
                result.put("manufacturer", recall.getRecallingFirm());
                result.put("description", recall.getRecallStatus() + " - " + recall.getProductDescription());
                result.put("intendedUse", "");
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = (DeviceEventReport) data;
                result.put("id", event.getId());
                result.put("deviceName", event.getBrandName() != null ? event.getBrandName() : event.getGenericName());
                result.put("manufacturer", event.getManufacturerName());
                result.put("description", event.getGenericName());
                result.put("intendedUse", "");
                break;
                
            case "GuidanceDocument":
                GuidanceDocument document = (GuidanceDocument) data;
                result.put("id", document.getId());
                result.put("deviceName", document.getTitle());
                result.put("manufacturer", "");
                // 组合完整描述
                String docDescription = String.format("标题: %s, 话题: %s, 描述: %s",
                    document.getTitle() != null ? document.getTitle() : "",
                    document.getTopic() != null ? document.getTopic() : "",
                    document.getDescription() != null ? document.getDescription() : "");
                // 限制描述长度
                if (docDescription.length() > 500) {
                    docDescription = docDescription.substring(0, 500) + "...";
                }
                result.put("description", docDescription);
                result.put("intendedUse", "");
                break;
                
            case "CustomsCase":
                CustomsCase customsCase = (CustomsCase) data;
                result.put("id", customsCase.getId());
                result.put("deviceName", "海关案例: " + customsCase.getCaseNumber());
                result.put("manufacturer", "");
                // 组合描述信息
                String caseDescription = String.format("案例编号: %s, HS编码: %s, 裁定结果: %s",
                    customsCase.getCaseNumber() != null ? customsCase.getCaseNumber() : "",
                    customsCase.getHsCodeUsed() != null ? customsCase.getHsCodeUsed() : "",
                    customsCase.getRulingResult() != null ? customsCase.getRulingResult() : "");
                // 限制描述长度
                if (caseDescription.length() > 500) {
                    caseDescription = caseDescription.substring(0, 500) + "...";
                }
                result.put("description", caseDescription);
                result.put("intendedUse", "");
                break;
        }
        
        return result;
    }
    
    /**
     * 执行降级和添加黑名单
     */
    @Transactional
    protected void executeDowngradeAndBlacklist(Object data, String entityType, AuditItem auditItem) {
        // 1. 降级为低风险
        updateRiskLevel(data, entityType, "LOW", "AI智能审核：" + auditItem.getReason());
        
        // 2. 提取关键字段添加到黑名单
        List<String> blacklistKeywords = extractBlacklistKeywords(data, entityType, auditItem);
        
        for (String keyword : blacklistKeywords) {
            try {
                // 检查是否已存在
                if (!isKeywordExists(keyword)) {
                    addBlacklistKeyword(keyword, 
                        String.format("AI自动添加 [%s]: %s", entityType, auditItem.getDeviceName()));
                    auditItem.addBlacklistKeyword(keyword);
                    log.info("添加黑名单关键词: {} (来源: {})", keyword, auditItem.getDeviceName());
                }
            } catch (Exception e) {
                log.warn("添加黑名单关键词失败: {}", keyword, e);
            }
        }
    }
    
    /**
     * 更新风险等级
     */
    private void updateRiskLevel(Object data, String entityType, String riskLevel, String updateReason) {
        // 将字符串转换为RiskLevel枚举
        CertNewsData.RiskLevel riskLevelEnum;
        try {
            riskLevelEnum = CertNewsData.RiskLevel.valueOf(riskLevel);
        } catch (Exception e) {
            log.warn("无效的风险等级: {}, 使用默认值LOW", riskLevel);
            riskLevelEnum = CertNewsData.RiskLevel.LOW;
        }
        
        switch (entityType) {
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord reg = (DeviceRegistrationRecord) data;
                reg.setRiskLevel(riskLevelEnum);
                // 注意：DeviceRegistrationRecord可能没有updateReason字段，这里只更新riskLevel
                registrationRecordRepository.save(reg);
                log.info("更新注册记录 {} 风险等级为 {}: {}", reg.getId(), riskLevel, updateReason);
                break;
                
            case "Device510K":
                Device510K device510K = (Device510K) data;
                device510K.setRiskLevel(riskLevelEnum);
                device510KRepository.save(device510K);
                log.info("更新510K记录 {} 风险等级为 {}: {}", device510K.getId(), riskLevel, updateReason);
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = (DeviceRecallRecord) data;
                recall.setRiskLevel(riskLevelEnum);
                recallRecordRepository.save(recall);
                log.info("更新召回记录 {} 风险等级为 {}: {}", recall.getId(), riskLevel, updateReason);
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = (DeviceEventReport) data;
                event.setRiskLevel(riskLevelEnum);
                eventReportRepository.save(event);
                log.info("更新事件报告 {} 风险等级为 {}: {}", event.getId(), riskLevel, updateReason);
                break;
                
            case "GuidanceDocument":
                GuidanceDocument document = (GuidanceDocument) data;
                document.setRiskLevel(riskLevelEnum);
                guidanceDocumentRepository.save(document);
                log.info("更新指导文档 {} 风险等级为 {}: {}", document.getId(), riskLevel, updateReason);
                break;
                
            case "CustomsCase":
                CustomsCase customsCase = (CustomsCase) data;
                customsCase.setRiskLevel(riskLevelEnum);
                customsCaseRepository.save(customsCase);
                log.info("更新海关案例 {} 风险等级为 {}: {}", customsCase.getId(), riskLevel, updateReason);
                break;
        }
    }
    
    /**
     * 提取黑名单关键词
     */
    public List<String> extractBlacklistKeywords(Object data, String entityType, AuditItem auditItem) {
        Set<String> keywords = new HashSet<>();
        
        String manufacturer = auditItem.getManufacturer();
        if (manufacturer != null && !manufacturer.isEmpty()) {
            manufacturer = cleanManufacturerName(manufacturer);
            if (manufacturer.length() >= 3) {
                keywords.add(manufacturer);
            }
        }
        
        // 根据数据类型提取额外字段
        switch (entityType) {
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord reg = (DeviceRegistrationRecord) data;
                addIfValid(keywords, cleanManufacturerName(reg.getManufacturerName()));
                break;
                
            case "Device510K":
                Device510K device510K = (Device510K) data;
                addIfValid(keywords, cleanManufacturerName(device510K.getApplicant()));
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = (DeviceRecallRecord) data;
                addIfValid(keywords, cleanManufacturerName(recall.getRecallingFirm()));
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = (DeviceEventReport) data;
                addIfValid(keywords, cleanManufacturerName(event.getManufacturerName()));
                break;
        }
        
        return new ArrayList<>(keywords);
    }
    
    /**
     * 添加关键词（如果有效）
     */
    private void addIfValid(Set<String> keywords, String keyword) {
        if (keyword != null && keyword.length() >= 3 && !isSkinDeviceBrand(keyword)) {
            keywords.add(keyword);
        }
    }
    
    /**
     * 清理制造商名称
     */
    private String cleanManufacturerName(String name) {
        if (name == null || name.isEmpty()) return "";
        
        name = name.trim();
        
        // 去除常见的公司后缀
        String[] suffixes = {
            " Inc", " Inc.", " LLC", " Ltd", " Ltd.", " Co", " Co.", 
            " Corporation", " Corp", " Corp.", " Company", " GmbH", " AG",
            "有限公司", "股份有限公司", "科技", "医疗", "器械", "集团"
        };
        
        for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
                name = name.substring(0, name.length() - suffix.length()).trim();
            }
        }
        
        return name;
    }
    
    /**
     * 判断是否为测肤仪品牌（不应该加入黑名单）
     */
    private boolean isSkinDeviceBrand(String brandName) {
        String[] skinDeviceBrands = {
            "VISIA", "Canfield", "OBSERV", "DermaFlash", "Neutrogena",
            "SkinCeuticals", "Dermalogica", "JANUS", "Callegari", "SkinAnalyzer"
        };
        
        String lowerBrand = brandName.toLowerCase();
        for (String brand : skinDeviceBrands) {
            if (lowerBrand.contains(brand.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查关键词是否已存在
     */
    private boolean isKeywordExists(String keyword) {
        Optional<DeviceMatchKeywords> existing = keywordsRepository
            .findByKeywordAndKeywordType(keyword, DeviceMatchKeywords.KeywordType.BLACKLIST);
        return existing.isPresent();
    }
    
    /**
     * 添加黑名单关键词
     */
    private void addBlacklistKeyword(String keyword, String source) {
        DeviceMatchKeywords entity = new DeviceMatchKeywords();
        entity.setKeyword(keyword);
        entity.setKeywordType(DeviceMatchKeywords.KeywordType.BLACKLIST);
        entity.setEnabled(true);
        // 注意：source字段可能不存在，这里用keyword本身
        keywordsRepository.save(entity);
        log.info("添加黑名单关键词: {} (来源: {})", keyword, source);
    }
    
    /**
     * 新方法：带黑名单预检查的AI判断预览
     * 
     * @param country 国家代码
     * @param entityTypes 数据类型列表
     * @param riskLevel 风险等级
     * @param limit 判断数量限制（null表示全部）
     * @param judgeAll 是否判断全部数据
     * @return 审核预览结果
     */
    public SmartAuditResult previewWithBlacklistCheck(
        String country,
        List<String> entityTypes,
        String riskLevel,
        Integer limit,
        Boolean judgeAll
    ) {
        log.info("开始AI判断预览（带黑名单检查）: country={}, entityTypes={}, riskLevel={}, limit={}, judgeAll={}",
                country, entityTypes, riskLevel, limit, judgeAll);
        
        SmartAuditResult result = new SmartAuditResult();
        result.setStartTime(new Date());
        result.setPreviewMode(true);
        
        try {
            // 1. 获取所有启用的黑名单关键词
            List<String> blacklistKeywords = deviceMatchKeywordsService.getBlacklistKeywordStrings();
            log.info("当前黑名单关键词数量: {}", blacklistKeywords.size());
            
            // 2. 确定要查询的数据类型
            if (entityTypes == null || entityTypes.isEmpty()) {
                entityTypes = Arrays.asList("Device510K", "DeviceRegistrationRecord", 
                                          "DeviceRecallRecord", "DeviceEventReport",
                                          "GuidanceDocument", "CustomsCase");
            }
            
            // 3. 对每种类型进行判断
            for (String entityType : entityTypes) {
                previewTypeWithBlacklist(entityType, country, riskLevel, limit, judgeAll, blacklistKeywords, result);
            }
            
            result.setEndTime(new Date());
            result.setSuccess(true);
            result.setMessage(String.format(
                "预览完成：黑名单过滤%d条，AI判断%d条（保留%d条，降级%d条）",
                result.getBlacklistFiltered(),
                result.getAiJudged(),
                result.getAiKept(),
                result.getAiDowngraded()
            ));
            
            log.info("AI判断预览完成: {}", result.getMessage());
            
        } catch (Exception e) {
            log.error("AI判断预览失败", e);
            result.setSuccess(false);
            result.setMessage("预览失败: " + e.getMessage());
            result.setEndTime(new Date());
        }
        
        return result;
    }
    
    /**
     * 预览指定类型的数据（带黑名单检查）
     */
    private void previewTypeWithBlacklist(
        String entityType,
        String country,
        String riskLevel,
        Integer limit,
        Boolean judgeAll,
        List<String> blacklistKeywords,
        SmartAuditResult result
    ) {
        // 获取数据
        List<?> dataList = getDataByTypeAndConditions(entityType, country, riskLevel, 
            judgeAll ? Integer.MAX_VALUE : (limit != null ? limit : 50));
        
        if (dataList == null || dataList.isEmpty()) {
            log.info("没有找到{}类型的数据", entityType);
            return;
        }
        
        log.info("开始判断 {} 条 {} 数据", dataList.size(), entityType);
        
        // 逐条处理
        for (Object data : dataList) {
            try {
                // 提取设备信息
                Map<String, Object> deviceData = extractDeviceData(data, entityType);
                
                // 黑名单预检查
                String matchedBlacklist = deviceMatchKeywordsService.checkBlacklistMatchMultiple(
                    (String) deviceData.get("deviceName"),
                    (String) deviceData.get("manufacturer"),
                    (String) deviceData.get("description")
                );
                
                AuditItem item;
                if (matchedBlacklist != null) {
                    // 匹配黑名单 - 直接标记为低风险，跳过AI判断
                    item = new AuditItem();
                    item.setEntityType(entityType);
                    item.setId((Long) deviceData.get("id"));
                    item.setDeviceName((String) deviceData.get("deviceName"));
                    item.setManufacturer((String) deviceData.get("manufacturer"));
                    
                    item.setBlacklistMatched(true);
                    item.setMatchedBlacklistKeyword(matchedBlacklist);
                    item.setRelatedToSkinDevice(false);
                    item.setConfidence(1.0);
                    item.setReason("黑名单匹配: " + matchedBlacklist);
                    item.setCategory("黑名单过滤");
                    
                    // 设置备注信息
                    String remark = generateRemark(item, null);
                    item.setRemark(remark);
                    
                    result.incrementBlacklistFiltered();
                    
                    log.debug("黑名单过滤: {} - {}", item.getId(), matchedBlacklist);
                } else {
                    // 未匹配黑名单 - 调用AI判断
                    item = auditSingleData(data, entityType);
                    item.setBlacklistMatched(false);
                    
                    if (item.isRelatedToSkinDevice()) {
                        // AI判断为相关
                        result.incrementAiKept();
                        log.debug("AI判断保留: {} - {}", item.getId(), item.getReason());
                    } else {
                        // AI判断为不相关 - 提取建议的黑名单关键词
                        result.incrementAiDowngraded();
                        
                        // 提取制造商和品牌作为建议的黑名单
                        String manufacturer = (String) deviceData.get("manufacturer");
                        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                            item.addSuggestedBlacklist(manufacturer);
                        }
                        
                        log.debug("AI判断降级: {} - {}, 建议黑名单: {}", 
                            item.getId(), item.getReason(), item.getSuggestedBlacklist());
                    }
                    
                    // 避免API速率限制
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                result.addAuditItem(item);
                
            } catch (Exception e) {
                log.error("处理单条数据失败: {}", data, e);
                result.incrementFailed();
            }
        }
    }
    
    /**
     * 执行AI判断并添加黑名单（基于预览结果）
     * 
     * @param auditItems 预览得到的审核项列表
     * @param newBlacklistKeywords 要添加的黑名单关键词
     * @return 执行结果
     */
    @Transactional
    public SmartAuditResult executeWithBlacklistUpdate(List<AuditItem> auditItems, List<String> newBlacklistKeywords) {
        log.info("开始执行AI判断操作，共{}条数据，将添加{}个黑名单关键词", 
            auditItems.size(), newBlacklistKeywords != null ? newBlacklistKeywords.size() : 0);
        
        SmartAuditResult result = new SmartAuditResult();
        result.setStartTime(new Date());
        result.setPreviewMode(false);
        
        try {
            // 1. 执行数据更新
            for (AuditItem item : auditItems) {
                try {
                    if (item.getBlacklistMatched()) {
                        // 黑名单匹配的数据 - 直接降级
                        updateRiskLevelById(item, "LOW", "黑名单匹配: " + item.getMatchedBlacklistKeyword());
                        result.incrementBlacklistFiltered();
                    } else if (item.isRelatedToSkinDevice()) {
                        // AI判断为相关 - 保持高风险
                        updateRemarkById(item, String.format(
                            "AI判断: 与测肤仪相关\n置信度: %.0f%%\n原因: %s\n分类: %s",
                            item.getConfidence() * 100,
                            item.getReason(),
                            item.getCategory()
                        ));
                        result.incrementAiKept();
                    } else {
                        // AI判断为不相关 - 降级
                        updateRiskLevelById(item, "LOW", String.format(
                            "AI判断: 非测肤仪设备\n置信度: %.0f%%\n原因: %s",
                            item.getConfidence() * 100,
                            item.getReason()
                        ));
                        result.incrementAiDowngraded();
                    }
                    
                    result.addAuditItem(item);
                } catch (Exception e) {
                    log.error("执行单条数据更新失败: {}", item.getId(), e);
                    result.incrementFailed();
                }
            }
            
            // 2. 批量添加新的黑名单关键词
            int blacklistAdded = 0;
            if (newBlacklistKeywords != null && !newBlacklistKeywords.isEmpty()) {
                blacklistAdded = deviceMatchKeywordsService.smartAddBlacklistKeywords(newBlacklistKeywords);
                log.info("新增黑名单关键词: {} 个", blacklistAdded);
            }
            
            result.setEndTime(new Date());
            result.setSuccess(true);
            result.setMessage(String.format(
                "执行完成：黑名单过滤%d条，AI保留%d条，AI降级%d条，新增黑名单%d个",
                result.getBlacklistFiltered(),
                result.getAiKept(),
                result.getAiDowngraded(),
                blacklistAdded
            ));
            
            log.info("AI判断执行完成: {}", result.getMessage());
            
        } catch (Exception e) {
            log.error("AI判断执行失败", e);
            result.setSuccess(false);
            result.setMessage("执行失败: " + e.getMessage());
            result.setEndTime(new Date());
        }
        
        return result;
    }
    
    /**
     * 根据ID更新风险等级和备注
     */
    private void updateRiskLevelById(AuditItem item, String riskLevel, String remark) {
        String entityType = item.getEntityType();
        Long id = item.getId();
        
        switch (entityType) {
            case "Device510K":
                Device510K device510K = device510KRepository.findById(id).orElse(null);
                if (device510K != null) {
                    device510K.setRiskLevel(RiskLevel.valueOf(riskLevel));
                    device510K.setRemark(remark);
                    device510KRepository.save(device510K);
                }
                break;
                
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord registration = registrationRecordRepository.findById(id).orElse(null);
                if (registration != null) {
                    registration.setRiskLevel(RiskLevel.valueOf(riskLevel));
                    registration.setRemark(remark);
                    registrationRecordRepository.save(registration);
                }
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = recallRecordRepository.findById(id).orElse(null);
                if (recall != null) {
                    recall.setRiskLevel(RiskLevel.valueOf(riskLevel));
                    recall.setRemark(remark);
                    recallRecordRepository.save(recall);
                }
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = eventReportRepository.findById(id).orElse(null);
                if (event != null) {
                    event.setRiskLevel(RiskLevel.valueOf(riskLevel));
                    event.setRemark(remark);
                    eventReportRepository.save(event);
                }
                break;
        }
        
        log.debug("更新数据: entityType={}, id={}, riskLevel={}", entityType, id, riskLevel);
    }
    
    /**
     * 仅更新备注（保持原风险等级）
     */
    private void updateRemarkById(AuditItem item, String remark) {
        String entityType = item.getEntityType();
        Long id = item.getId();
        
        switch (entityType) {
            case "Device510K":
                Device510K device510K = device510KRepository.findById(id).orElse(null);
                if (device510K != null) {
                    device510K.setRemark(remark);
                    device510KRepository.save(device510K);
                }
                break;
                
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord registration = registrationRecordRepository.findById(id).orElse(null);
                if (registration != null) {
                    registration.setRemark(remark);
                    registrationRecordRepository.save(registration);
                }
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = recallRecordRepository.findById(id).orElse(null);
                if (recall != null) {
                    recall.setRemark(remark);
                    recallRecordRepository.save(recall);
                }
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = eventReportRepository.findById(id).orElse(null);
                if (event != null) {
                    event.setRemark(remark);
                    eventReportRepository.save(event);
                }
                break;
        }
        
        log.debug("更新备注: entityType={}, id={}", entityType, id);
    }
}


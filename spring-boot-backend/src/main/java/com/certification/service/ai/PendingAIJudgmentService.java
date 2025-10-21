package com.certification.service.ai;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.ai.AIPendingJudgment;
import com.certification.repository.ai.AIPendingJudgmentRepository;
import com.certification.service.ai.AIJudgeStrategy;
import com.certification.service.ai.strategy.ApplicationJudgeStrategy;
import com.certification.service.ai.strategy.CustomsJudgeStrategy;
import com.certification.service.ai.strategy.DocumentJudgeStrategy;
import com.certification.service.ai.strategy.EventJudgeStrategy;
import com.certification.service.ai.strategy.RecallJudgeStrategy;
import com.certification.service.ai.strategy.RegistrationJudgeStrategy;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI判断待审核管理服务
 * 管理待审核的AI判断，支持确认、拒绝和批量操作
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PendingAIJudgmentService {

    private final AIPendingJudgmentRepository pendingJudgmentRepository;
    private final ApplicationJudgeStrategy applicationJudgeStrategy;
    private final RegistrationJudgeStrategy registrationJudgeStrategy;
    private final RecallJudgeStrategy recallJudgeStrategy;
    private final EventJudgeStrategy eventJudgeStrategy;
    private final DocumentJudgeStrategy documentJudgeStrategy;
    private final CustomsJudgeStrategy customsJudgeStrategy;
    private final ObjectMapper objectMapper;

    /**
     * 获取待审核列表
     */
    public List<AIPendingJudgment> getPendingJudgments(String moduleType, String status) {
        log.info("获取待审核列表: moduleType={}, status={}", moduleType, status);

        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }

        return pendingJudgmentRepository.findByModuleTypeAndStatusOrderByCreatedTimeDesc(
                moduleType, status);
    }

    /**
     * 获取待审核数量
     */
    public long getPendingCount(String moduleType) {
        return pendingJudgmentRepository.countPendingByModuleType(moduleType);
    }

    /**
     * 获取按实体类型分组的待审核数量
     */
    public Map<String, Long> getPendingCountByEntityType(String moduleType) {
        log.info("获取待审核数量: moduleType={}", moduleType);

        Map<String, Long> result = new HashMap<>();
        String[] entityTypes = {"Application", "Registration", "Recall", "Event", "Document", "Customs"};

        for (String entityType : entityTypes) {
            long count = pendingJudgmentRepository.countPendingByModuleAndEntityType(moduleType, entityType);
            if (count > 0) {
                result.put(entityType, count);
            }
        }

        return result;
    }

    /**
     * 获取判断详情
     */
    public JudgmentDetailsDTO getJudgmentDetails(Long id) {
        log.info("获取判断详情: id={}", id);

        AIPendingJudgment judgment = pendingJudgmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("待审核记录不存在: " + id));

        return convertToDetailsDTO(judgment);
    }

    /**
     * 获取设备数据模块的统计信息
     */
    public DeviceDataStatisticsDTO getDeviceDataStatistics() {
        log.info("获取设备数据模块统计信息");

        DeviceDataStatisticsDTO stats = new DeviceDataStatisticsDTO();

        // 被黑名单过滤的数量
        long filteredCount = pendingJudgmentRepository.countFilteredByBlacklist("DEVICE_DATA");
        stats.setFilteredByBlacklistCount(filteredCount);

        // 将被设置为高风险的数量
        long highRiskCount = pendingJudgmentRepository.countHighRiskJudgments("DEVICE_DATA");
        stats.setHighRiskCount(highRiskCount);

        // 新增黑名单关键词
        List<AIPendingJudgment> pendingList = pendingJudgmentRepository
                .findByModuleTypeAndStatusOrderByCreatedTimeDesc("DEVICE_DATA", "PENDING");

        Set<String> allBlacklistKeywords = new HashSet<>();
        for (AIPendingJudgment judgment : pendingList) {
            if (judgment.getBlacklistKeywords() != null && !judgment.getBlacklistKeywords().isEmpty()) {
                try {
                    List<String> keywords = objectMapper.readValue(
                            judgment.getBlacklistKeywords(),
                            new TypeReference<List<String>>() {});
                    allBlacklistKeywords.addAll(keywords);
                } catch (Exception e) {
                    log.warn("解析黑名单关键词失败: {}", judgment.getId(), e);
                }
            }
        }
        stats.setNewBlacklistKeywords(new ArrayList<>(allBlacklistKeywords));

        return stats;
    }

    /**
     * 确认判断并执行操作
     */
    @Transactional
    public void confirmJudgment(Long id, String confirmedBy) {
        log.info("确认AI判断: id={}, confirmedBy={}", id, confirmedBy);

        AIPendingJudgment judgment = pendingJudgmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("待审核记录不存在: " + id));

        // 检查状态
        if (!"PENDING".equals(judgment.getStatus())) {
            throw new IllegalStateException("该记录已处理，状态: " + judgment.getStatus());
        }

        // 执行AI判断结果
        executeJudgmentResult(judgment);

        // 更新状态
        judgment.confirm(confirmedBy != null ? confirmedBy : "SYSTEM");
        pendingJudgmentRepository.save(judgment);

        log.info("AI判断确认完成: id={}", id);
    }

    /**
     * 批量确认
     */
    @Transactional
    public BatchConfirmResult batchConfirm(List<Long> ids, String confirmedBy) {
        log.info("批量确认AI判断: count={}", ids.size());

        BatchConfirmResult result = new BatchConfirmResult();
        result.setTotal(ids.size());

        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long id : ids) {
            try {
                confirmJudgment(id, confirmedBy);
                successCount++;
            } catch (Exception e) {
                failedCount++;
                errors.add("ID " + id + ": " + e.getMessage());
                log.error("确认判断失败: id={}", id, e);
            }
        }

        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setErrors(errors);

        log.info("批量确认完成: 总数={}, 成功={}, 失败={}", ids.size(), successCount, failedCount);

        return result;
    }

    /**
     * 拒绝判断
     */
    @Transactional
    public void rejectJudgment(Long id, String rejectedBy) {
        log.info("拒绝AI判断: id={}, rejectedBy={}", id, rejectedBy);

        AIPendingJudgment judgment = pendingJudgmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("待审核记录不存在: " + id));

        // 更新状态为已拒绝
        judgment.reject(rejectedBy != null ? rejectedBy : "SYSTEM");
        pendingJudgmentRepository.save(judgment);

        log.info("AI判断已拒绝: id={}", id);
    }

    /**
     * 清理过期的待审核记录
     */
    @Transactional
    public int cleanupExpiredJudgments() {
        log.info("清理过期的待审核记录");

        LocalDateTime now = LocalDateTime.now();
        List<AIPendingJudgment> expiredList = pendingJudgmentRepository.findExpiredPendingJudgments(now);

        int count = 0;
        for (AIPendingJudgment judgment : expiredList) {
            judgment.expire();
            pendingJudgmentRepository.save(judgment);
            count++;
        }

        log.info("清理完成: 过期记录数={}", count);
        return count;
    }

    /**
     * 执行AI判断结果
     * 调用对应的Strategy更新实体
     */
    private void executeJudgmentResult(AIPendingJudgment judgment) {
        log.debug("执行AI判断结果: entityType={}, entityId={}",
                 judgment.getEntityType(), judgment.getEntityId());

        try {
            // 获取对应的策略
            AIJudgeStrategy strategy = getStrategy(judgment.getEntityType());

            // 获取实体
            Object entity = strategy.findEntityById(judgment.getEntityId());
            if (entity == null) {
                throw new IllegalStateException("实体不存在: " + judgment.getEntityId());
            }

            // 解析AI判断结果
            AIJudgeResult judgeResult = parseJudgeResult(judgment);

            // 更新实体（风险等级和备注）
            strategy.updateEntityWithJudgeResult(entity, judgeResult);

            // 保存实体
            strategy.saveEntity(entity);

            log.info("AI判断结果执行成功: entityType={}, entityId={}",
                    judgment.getEntityType(), judgment.getEntityId());

        } catch (Exception e) {
            log.error("执行AI判断结果失败: {}", judgment.getId(), e);
            throw new RuntimeException("执行AI判断结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取对应的策略
     */
    private AIJudgeStrategy getStrategy(String entityType) {
        return switch (entityType) {
            case "Application" -> applicationJudgeStrategy;
            case "Registration" -> registrationJudgeStrategy;
            case "Recall" -> recallJudgeStrategy;
            case "Event" -> eventJudgeStrategy;
            case "Document" -> documentJudgeStrategy;
            case "Customs" -> customsJudgeStrategy;
            default -> throw new IllegalArgumentException("不支持的实体类型: " + entityType);
        };
    }

    /**
     * 解析AI判断结果JSON
     */
    private AIJudgeResult parseJudgeResult(AIPendingJudgment judgment) {
        try {
            Map<String, Object> resultMap = objectMapper.readValue(
                    judgment.getJudgeResult(),
                    new TypeReference<Map<String, Object>>() {});

            AIJudgeResult result = new AIJudgeResult();
            result.setRelated((Boolean) resultMap.get("isRelated"));
            result.setConfidence(((Number) resultMap.get("confidence")).doubleValue());
            result.setReason((String) resultMap.get("reason"));

            return result;

        } catch (Exception e) {
            log.error("解析AI判断结果失败: {}", judgment.getId(), e);
            throw new RuntimeException("解析AI判断结果失败", e);
        }
    }

    /**
     * 转换为详情DTO
     */
    private JudgmentDetailsDTO convertToDetailsDTO(AIPendingJudgment judgment) {
        JudgmentDetailsDTO dto = new JudgmentDetailsDTO();
        dto.setId(judgment.getId());
        dto.setModuleType(judgment.getModuleType());
        dto.setEntityType(judgment.getEntityType());
        dto.setEntityId(judgment.getEntityId());
        dto.setSuggestedRiskLevel(judgment.getSuggestedRiskLevel());
        dto.setSuggestedRemark(judgment.getSuggestedRemark());
        dto.setFilteredByBlacklist(judgment.getFilteredByBlacklist());
        dto.setStatus(judgment.getStatus());
        dto.setCreatedTime(judgment.getCreatedTime());
        dto.setExpireTime(judgment.getExpireTime());

        // 解析AI判断结果
        if (judgment.getJudgeResult() != null) {
            try {
                dto.setJudgeResult(objectMapper.readValue(
                        judgment.getJudgeResult(),
                        new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                log.warn("解析判断结果失败: {}", judgment.getId());
            }
        }

        // 解析黑名单关键词
        if (judgment.getBlacklistKeywords() != null) {
            try {
                dto.setBlacklistKeywords(objectMapper.readValue(
                        judgment.getBlacklistKeywords(),
                        new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                log.warn("解析黑名单关键词失败: {}", judgment.getId());
            }
        }

        return dto;
    }

    // ==================== DTO类 ====================

    /**
     * 判断详情DTO
     */
    @Data
    public static class JudgmentDetailsDTO {
        private Long id;
        private String moduleType;
        private String entityType;
        private Long entityId;
        private Map<String, Object> judgeResult;
        private String suggestedRiskLevel;
        private String suggestedRemark;
        private List<String> blacklistKeywords;
        private Boolean filteredByBlacklist;
        private String status;
        private LocalDateTime createdTime;
        private LocalDateTime expireTime;
    }

    /**
     * 设备数据统计DTO
     */
    @Data
    public static class DeviceDataStatisticsDTO {
        private Long filteredByBlacklistCount;  // 被黑名单过滤的数量
        private Long highRiskCount;              // 将被设置为高风险的数量
        private List<String> newBlacklistKeywords; // 新增黑名单关键词
    }

    /**
     * 批量确认结果
     */
    @Data
    public static class BatchConfirmResult {
        private Integer total;
        private Integer successCount;
        private Integer failedCount;
        private List<String> errors;
    }
}

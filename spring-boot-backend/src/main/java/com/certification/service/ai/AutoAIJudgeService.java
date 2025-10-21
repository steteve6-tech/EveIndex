package com.certification.service.ai;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.ai.AIPendingJudgment;
import com.certification.entity.common.*;
import com.certification.repository.DeviceMatchKeywordsRepository;
import com.certification.repository.ai.AIPendingJudgmentRepository;
import com.certification.repository.common.*;
import com.certification.service.ai.strategy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 自动AI判断服务
 * 在爬取后自动判断设备数据是否与测肤仪相关
 *
 * 【重要更新】现在AI判断结果会先保存到待审核表，等待用户确认后再执行
 */
@Slf4j
@Service
public class AutoAIJudgeService {

    @Autowired
    private Device510KRepository device510KRepository;

    @Autowired
    private DeviceRecallRecordRepository recallRecordRepository;

    @Autowired
    private DeviceEventReportRepository eventReportRepository;

    @Autowired
    private DeviceRegistrationRecordRepository registrationRecordRepository;

    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;

    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    @Autowired
    private DeviceMatchKeywordsRepository keywordsRepository;

    @Autowired
    private AIPendingJudgmentRepository pendingJudgmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationJudgeStrategy applicationJudgeStrategy;

    @Autowired
    private RegistrationJudgeStrategy registrationJudgeStrategy;

    @Autowired
    private RecallJudgeStrategy recallJudgeStrategy;

    @Autowired
    private EventJudgeStrategy eventJudgeStrategy;

    @Autowired
    private DocumentJudgeStrategy documentJudgeStrategy;

    @Autowired
    private CustomsJudgeStrategy customsJudgeStrategy;
    
    /**
     * 判断新数据（延迟执行模式 + 批量保存）
     * AI判断后不立即执行，而是保存到待审核表等待用户确认
     *
     * 【重要优化】
     * 1. AI判断失败时，保持原risk_level不变
     * 2. 每100条自动保存到数据库，避免长时间运行后数据丢失
     *
     * @param results 新爬取的数据列表
     * @param moduleName 模块名称（device510k, deviceeventreport等）
     */
    @Transactional
    public void judgeNewData(List<?> results, String moduleName) {
        if (results == null || results.isEmpty()) {
            log.debug("没有需要判断的数据");
            return;
        }

        log.info("========== 开始自动AI判断（延迟执行模式 + 批量保存） ==========");
        log.info("模块: {}, 总数据量: {}", moduleName, results.size());

        int totalCount = results.size();
        int successCount = 0;
        int failedCount = 0;
        int relatedCount = 0;
        int unrelatedCount = 0;
        int filteredCount = 0;
        int batchCount = 0;

        // 批量保存的缓冲区
        List<Object> entitiesToSave = new ArrayList<>();
        List<AIPendingJudgment> pendingJudgmentsToSave = new ArrayList<>();

        final int BATCH_SIZE = 100; // 每100条保存一次

        AIJudgeStrategy strategy = getStrategyByModuleName(moduleName);
        if (strategy == null) {
            log.error("未找到对应的判断策略: moduleName={}", moduleName);
            return;
        }

        for (int i = 0; i < totalCount; i++) {
            Object data = results.get(i);

            try {
                // 获取实体ID
                Long entityId = getEntityId(data);
                if (entityId == null) {
                    log.warn("无法获取实体ID，跳过: index={}, data={}", i, data.getClass().getSimpleName());
                    failedCount++;
                    continue;
                }

                // AI判断
                AIJudgeResult judgeResult = null;
                try {
                    judgeResult = strategy.judge(data);
                } catch (Exception aiError) {
                    log.error("AI判断失败（保持原风险等级不变）: entityId={}, error={}", entityId, aiError.getMessage());
                    failedCount++;

                    // AI判断失败，标记为新增数据但不创建待审核记录
                    markAsNewData(data);
                    entitiesToSave.add(data);
                    continue;
                }

                // 将实体标记为新增数据
                markAsNewData(data);
                entitiesToSave.add(data);

                // 创建待审核记录
                AIPendingJudgment pending = createPendingJudgment(
                        moduleName, entityId, judgeResult);
                pendingJudgmentsToSave.add(pending);

                // 统计
                if (judgeResult.isRelated()) {
                    relatedCount++;
                } else {
                    unrelatedCount++;
                    if (judgeResult.getBlacklistKeywords() != null && !judgeResult.getBlacklistKeywords().isEmpty()) {
                        filteredCount++;
                    }
                }

                successCount++;

                // 每处理100条，或到达最后一条时，批量保存
                if (entitiesToSave.size() >= BATCH_SIZE || i == totalCount - 1) {
                    batchCount++;

                    log.info(">>> 批量保存第{}批: 已处理 {}/{}, 成功={}, 失败={}",
                            batchCount, i + 1, totalCount, successCount, failedCount);

                    // 批量保存实体
                    batchSaveEntities(entitiesToSave, moduleName);

                    // 批量保存待审核记录
                    if (!pendingJudgmentsToSave.isEmpty()) {
                        pendingJudgmentRepository.saveAll(pendingJudgmentsToSave);
                        log.info(">>> 已保存 {} 条待审核记录到数据库", pendingJudgmentsToSave.size());
                    }

                    // 清空缓冲区
                    entitiesToSave.clear();
                    pendingJudgmentsToSave.clear();

                    log.info(">>> 第{}批保存完成 ✓", batchCount);
                }

                // 避免API速率限制
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("处理单条数据时发生异常: index={}, error={}", i, e.getMessage(), e);
                failedCount++;

                // 即使出错也继续处理，但先保存已处理的数据
                if (!entitiesToSave.isEmpty() || !pendingJudgmentsToSave.isEmpty()) {
                    try {
                        log.warn("发生异常，先保存已处理的 {} 条数据", entitiesToSave.size());
                        batchSaveEntities(entitiesToSave, moduleName);
                        if (!pendingJudgmentsToSave.isEmpty()) {
                            pendingJudgmentRepository.saveAll(pendingJudgmentsToSave);
                        }
                        entitiesToSave.clear();
                        pendingJudgmentsToSave.clear();
                    } catch (Exception saveError) {
                        log.error("紧急保存失败: {}", saveError.getMessage(), saveError);
                    }
                }
            }
        }

        log.info("========== 自动AI判断完成 ==========");
        log.info("总数据: {}, 成功: {}, 失败: {}", totalCount, successCount, failedCount);
        log.info("相关: {}, 不相关: {}, 黑名单过滤: {}", relatedCount, unrelatedCount, filteredCount);
        log.info("批量保存次数: {}", batchCount);
        log.info("=======================================");
    }

    /**
     * 批量保存实体到数据库
     */
    private void batchSaveEntities(List<Object> entities, String moduleName) {
        if (entities.isEmpty()) {
            return;
        }

        try {
            switch (moduleName.toLowerCase()) {
                case "device510k":
                    device510KRepository.saveAll((List<Device510K>) (List<?>) entities);
                    break;
                case "deviceeventreport":
                    eventReportRepository.saveAll((List<DeviceEventReport>) (List<?>) entities);
                    break;
                case "devicerecallrecord":
                    recallRecordRepository.saveAll((List<DeviceRecallRecord>) (List<?>) entities);
                    break;
                case "deviceregistrationrecord":
                    registrationRecordRepository.saveAll((List<DeviceRegistrationRecord>) (List<?>) entities);
                    break;
                case "guidancedocument":
                    guidanceDocumentRepository.saveAll((List<GuidanceDocument>) (List<?>) entities);
                    break;
                case "customscase":
                    customsCaseRepository.saveAll((List<CustomsCase>) (List<?>) entities);
                    break;
                default:
                    log.warn("未知的模块名称，无法批量保存: {}", moduleName);
            }
            log.debug("批量保存了 {} 条实体数据", entities.size());
        } catch (Exception e) {
            log.error("批量保存实体失败: moduleName={}, count={}, error={}",
                    moduleName, entities.size(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建待审核判断记录
     */
    private AIPendingJudgment createPendingJudgment(String moduleName, Long entityId, AIJudgeResult judgeResult) {
        try {
            AIPendingJudgment pending = new AIPendingJudgment();
            pending.setModuleType("DEVICE_DATA");
            pending.setEntityType(normalizeModuleName(moduleName));
            pending.setEntityId(entityId);

            // 序列化AI判断结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("isRelated", judgeResult.isRelated());
            resultMap.put("confidence", judgeResult.getConfidence());
            resultMap.put("reason", judgeResult.getReason());
            pending.setJudgeResult(objectMapper.writeValueAsString(resultMap));

            // 设置建议的风险等级和备注
            pending.setSuggestedRiskLevel(judgeResult.isRelated() ? "HIGH" : "LOW");
            pending.setSuggestedRemark(formatRemark(judgeResult));

            // 设置黑名单关键词
            if (judgeResult.getBlacklistKeywords() != null && !judgeResult.getBlacklistKeywords().isEmpty()) {
                pending.setBlacklistKeywords(objectMapper.writeValueAsString(judgeResult.getBlacklistKeywords()));
                pending.setFilteredByBlacklist(true);
            } else {
                pending.setFilteredByBlacklist(false);
            }

            return pending;

        } catch (Exception e) {
            log.error("创建待审核记录失败: entityId={}", entityId, e);
            throw new RuntimeException("创建待审核记录失败", e);
        }
    }

    /**
     * 标准化模块名称
     */
    private String normalizeModuleName(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "device510k":
                return "Application";
            case "deviceeventreport":
                return "Event";
            case "devicerecallrecord":
                return "Recall";
            case "deviceregistrationrecord":
                return "Registration";
            case "guidancedocument":
                return "Document";
            case "customscase":
                return "Customs";
            default:
                return moduleName;
        }
    }

    /**
     * 格式化备注
     */
    private String formatRemark(AIJudgeResult judgeResult) {
        StringBuilder remark = new StringBuilder();
        remark.append("AI判断结果：");
        remark.append(judgeResult.isRelated() ? "相关" : "不相关");
        remark.append("\n置信度：").append(String.format("%.2f", judgeResult.getConfidence() * 100)).append("%");
        if (judgeResult.getReason() != null && !judgeResult.getReason().isEmpty()) {
            remark.append("\n理由：").append(judgeResult.getReason());
        }
        return remark.toString();
    }

    /**
     * 获取实体ID
     */
    private Long getEntityId(Object entity) {
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(entity);
            return id != null ? ((Number) id).longValue() : null;
        } catch (Exception e) {
            log.error("获取实体ID失败: {}", entity.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 标记为新增数据
     */
    private void markAsNewData(Object entity) {
        try {
            Method setIsNewMethod = entity.getClass().getMethod("setIsNew", Boolean.class);
            setIsNewMethod.invoke(entity, true);
        } catch (Exception e) {
            log.debug("标记新增数据失败（可能实体类没有isNew字段）: {}", entity.getClass().getSimpleName());
        }
    }
    
    /**
     * 根据模块名称获取对应的判断策略
     */
    private AIJudgeStrategy getStrategyByModuleName(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "device510k":
                return applicationJudgeStrategy;
            case "deviceeventreport":
                return eventJudgeStrategy;
            case "devicerecallrecord":
                return recallJudgeStrategy;
            case "deviceregistrationrecord":
                return registrationJudgeStrategy;
            case "guidancedocument":
                return documentJudgeStrategy;
            case "customscase":
                return customsJudgeStrategy;
            default:
                return null;
        }
    }
    
    /**
     * 保存实体到数据库
     */
    private void saveEntity(Object entity, String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "device510k":
                device510KRepository.save((Device510K) entity);
                break;
            case "deviceeventreport":
                eventReportRepository.save((DeviceEventReport) entity);
                break;
            case "devicerecallrecord":
                recallRecordRepository.save((DeviceRecallRecord) entity);
                break;
            case "deviceregistrationrecord":
                registrationRecordRepository.save((DeviceRegistrationRecord) entity);
                break;
            case "guidancedocument":
                guidanceDocumentRepository.save((GuidanceDocument) entity);
                break;
            case "customscase":
                customsCaseRepository.save((CustomsCase) entity);
                break;
            default:
                log.warn("未知的模块名称，无法保存: moduleName={}", moduleName);
        }
    }
    
    @Autowired
    private com.certification.service.DeviceMatchKeywordsService deviceMatchKeywordsService;

    /**
     * 添加黑名单关键词（带白名单保护）
     */
    private void addBlacklistKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        for (String keyword : keywords) {
            try {
                // 白名单保护：如果关键词在白名单中，跳过添加到黑名单
                String whitelistMatch = deviceMatchKeywordsService.checkWhitelistMatch(keyword);
                if (whitelistMatch != null) {
                    log.info("制造商在白名单中，跳过添加黑名单: {} (白名单关键词: {})", keyword, whitelistMatch);
                    continue;
                }

                if (!isKeywordExists(keyword)) {
                    DeviceMatchKeywords blacklistKeyword = new DeviceMatchKeywords();
                    blacklistKeyword.setKeyword(keyword);
                    blacklistKeyword.setKeywordType(DeviceMatchKeywords.KeywordType.BLACKLIST);
                    blacklistKeyword.setEnabled(true);
                    keywordsRepository.save(blacklistKeyword);
                    log.info("添加黑名单关键词: {}", keyword);
                }
            } catch (Exception e) {
                log.warn("添加黑名单关键词失败: keyword={}, error={}", keyword, e.getMessage());
            }
        }
    }
    
    /**
     * 检查关键词是否已存在
     */
    private boolean isKeywordExists(String keyword) {
        try {
            return keywordsRepository.findByKeyword(keyword).isPresent();
        } catch (Exception e) {
            log.warn("检查关键词是否存在失败: keyword={}", keyword);
            return false;
        }
    }
}


package com.certification.service.ai;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.*;
import com.certification.repository.DeviceMatchKeywordsRepository;
import com.certification.repository.common.*;
import com.certification.service.ai.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 自动AI判断服务
 * 在爬取后自动判断设备数据是否与测肤仪相关
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
     * 判断新数据
     * @param results 新爬取的数据列表
     * @param moduleName 模块名称（device510k, deviceeventreport等）
     */
    @Transactional
    public void judgeNewData(List<?> results, String moduleName) {
        if (results == null || results.isEmpty()) {
            log.debug("没有需要判断的数据");
            return;
        }
        
        log.info("开始自动AI判断: moduleName={}, count={}", moduleName, results.size());
        
        int successCount = 0;
        int failedCount = 0;
        int keptCount = 0;    // 保留高风险
        int downgradedCount = 0;  // 降为低风险
        
        for (Object data : results) {
            try {
                AIJudgeStrategy strategy = getStrategyByModuleName(moduleName);
                if (strategy == null) {
                    log.warn("未找到对应的判断策略: moduleName={}", moduleName);
                    continue;
                }
                
                // AI判断
                AIJudgeResult judgeResult = strategy.judge(data);
                
                // 更新实体
                strategy.updateEntityWithJudgeResult(data, judgeResult);
                
                // 保存到数据库
                saveEntity(data, moduleName);
                
                // 统计
                if (judgeResult.isRelated()) {
                    keptCount++;
                } else {
                    downgradedCount++;
                    // 添加黑名单关键词
                    addBlacklistKeywords(judgeResult.getBlacklistKeywords());
                }
                
                successCount++;
                
                // 避免API速率限制
                Thread.sleep(500);
                
            } catch (Exception e) {
                log.error("AI判断单条数据失败: data={}, error={}", data, e.getMessage(), e);
                failedCount++;
            }
        }
        
        log.info("自动AI判断完成: moduleName={}, 成功={}, 失败={}, 保留高风险={}, 降为低风险={}", 
                 moduleName, successCount, failedCount, keptCount, downgradedCount);
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
    
    /**
     * 添加黑名单关键词
     */
    private void addBlacklistKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        
        for (String keyword : keywords) {
            try {
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


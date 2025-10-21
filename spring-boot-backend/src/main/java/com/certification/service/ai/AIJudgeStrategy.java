package com.certification.service.ai;

import com.certification.dto.ai.AIJudgeResult;

/**
 * AI判断策略接口
 * 定义判断设备是否为测肤仪相关的策略
 */
public interface AIJudgeStrategy {
    
    /**
     * 判断设备是否为测肤仪相关
     * @param entity 设备实体对象
     * @return AI判断结果
     */
    AIJudgeResult judge(Object entity);
    
    /**
     * 获取策略支持的实体类型
     * @return 实体类型名称（如："Device510K", "DeviceRegistrationRecord"）
     */
    String getSupportedEntityType();
    
    /**
     * 更新实体的风险等级和备注
     * @param entity 设备实体对象
     * @param judgeResult AI判断结果
     */
    void updateEntityWithJudgeResult(Object entity, AIJudgeResult judgeResult);

    /**
     * 根据ID查找实体
     * @param entityId 实体ID
     * @return 实体对象，如果不存在则返回null
     */
    Object findEntityById(Long entityId);

    /**
     * 保存实体
     * @param entity 实体对象
     */
    void saveEntity(Object entity);
}


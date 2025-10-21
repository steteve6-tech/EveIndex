package com.certification.service.crawler;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.repository.UnifiedTaskConfigRepository;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 参数预设管理服务
 */
@Slf4j
@Service
public class ParamPresetService {
    
    @Autowired
    private UnifiedTaskConfigRepository taskConfigRepository;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Autowired
    private DynamicTaskSchedulerService schedulerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 创建参数预设
     * 
     * @param request 预设请求
     * @return 创建的预设
     */
    @Transactional
    public UnifiedTaskConfig createPreset(PresetRequest request) {
        log.info("创建参数预设: {}", request.getTaskName());
        
        // 验证参数
        if (!validatePresetParams(request.getCrawlerName(), request.getParameters())) {
            throw new IllegalArgumentException("参数验证失败");
        }
        
        // 创建任务配置
        UnifiedTaskConfig config = new UnifiedTaskConfig();
        config.setTaskName(request.getTaskName());
        config.setCrawlerName(request.getCrawlerName());
        config.setCountryCode(request.getCountryCode());
        config.setTaskType("PRESET");
        config.setParameters(request.getParameters());
        config.setCronExpression(request.getCronExpression());
        config.setDescription(request.getDescription());
        config.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        config.setPriority(request.getPriority() != null ? request.getPriority() : 5);
        config.setTimeoutMinutes(request.getTimeoutMinutes() != null ? request.getTimeoutMinutes() : 30);
        config.setRetryCount(request.getRetryCount() != null ? request.getRetryCount() : 0);
        config.setCreatedBy(request.getCreatedBy());
        
        config = taskConfigRepository.save(config);
        
        // 如果启用且有Cron表达式，创建定时任务
        if (config.getEnabled() && config.getCronExpression() != null && !config.getCronExpression().trim().isEmpty()) {
            schedulerService.scheduleTask(config);
        }
        
        log.info("参数预设创建成功: ID={}, 名称={}", config.getId(), config.getTaskName());
        
        return config;
    }
    
    /**
     * 更新参数预设
     * 
     * @param id 预设ID
     * @param request 更新请求
     * @return 更新后的预设
     */
    @Transactional
    public UnifiedTaskConfig updatePreset(Long id, PresetRequest request) {
        log.info("更新参数预设: ID={}", id);
        
        UnifiedTaskConfig config = taskConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("预设不存在: " + id));
        
        // 验证参数
        if (request.getParameters() != null && !validatePresetParams(config.getCrawlerName(), request.getParameters())) {
            throw new IllegalArgumentException("参数验证失败");
        }
        
        // 更新字段
        if (request.getTaskName() != null) {
            config.setTaskName(request.getTaskName());
        }
        if (request.getParameters() != null) {
            config.setParameters(request.getParameters());
        }
        if (request.getCronExpression() != null) {
            config.setCronExpression(request.getCronExpression());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        if (request.getPriority() != null) {
            config.setPriority(request.getPriority());
        }
        if (request.getTimeoutMinutes() != null) {
            config.setTimeoutMinutes(request.getTimeoutMinutes());
        }
        if (request.getRetryCount() != null) {
            config.setRetryCount(request.getRetryCount());
        }
        if (request.getUpdatedBy() != null) {
            config.setUpdatedBy(request.getUpdatedBy());
        }
        
        config = taskConfigRepository.save(config);
        
        // 更新定时任务
        schedulerService.updateScheduledTask(config.getId());
        
        log.info("参数预设更新成功: ID={}", id);
        
        return config;
    }
    
    /**
     * 复制参数预设
     * 
     * @param id 原预设ID
     * @param newName 新预设名称
     * @return 新预设
     */
    @Transactional
    public UnifiedTaskConfig copyPreset(Long id, String newName) {
        log.info("复制参数预设: ID={}, 新名称={}", id, newName);
        
        UnifiedTaskConfig source = taskConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("预设不存在: " + id));
        
        UnifiedTaskConfig copy = new UnifiedTaskConfig();
        copy.setTaskName(newName);
        copy.setCrawlerName(source.getCrawlerName());
        copy.setCountryCode(source.getCountryCode());
        copy.setTaskType(source.getTaskType());
        copy.setParameters(source.getParameters());
        copy.setKeywords(source.getKeywords());
        copy.setCronExpression(source.getCronExpression());
        copy.setDescription("复制自: " + source.getTaskName());
        copy.setEnabled(false); // 复制的预设默认禁用
        copy.setPriority(source.getPriority());
        copy.setTimeoutMinutes(source.getTimeoutMinutes());
        copy.setRetryCount(source.getRetryCount());
        
        copy = taskConfigRepository.save(copy);
        
        log.info("参数预设复制成功: 新ID={}", copy.getId());
        
        return copy;
    }
    
    /**
     * 删除参数预设
     * 
     * @param id 预设ID
     */
    @Transactional
    public void deletePreset(Long id) {
        log.info("删除参数预设: ID={}", id);
        
        UnifiedTaskConfig config = taskConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("预设不存在: " + id));
        
        // 取消定时任务
        schedulerService.cancelTask(id);
        
        // 删除配置
        taskConfigRepository.delete(config);
        
        log.info("参数预设删除成功: ID={}", id);
    }
    
    /**
     * 删除所有参数预设
     * ⚠️ 警告：此操作会清空所有任务配置！
     */
    @Transactional
    public void deleteAllPresets() {
        log.warn("⚠️ 开始清空所有参数预设...");
        
        // 获取所有预设
        List<UnifiedTaskConfig> allPresets = taskConfigRepository.findAll();
        
        log.info("找到 {} 个预设，开始删除...", allPresets.size());
        
        // 取消所有定时任务
        for (UnifiedTaskConfig config : allPresets) {
            try {
                schedulerService.cancelTask(config.getId());
            } catch (Exception e) {
                log.warn("取消定时任务失败: ID={}", config.getId(), e);
            }
        }
        
        // 删除所有配置
        taskConfigRepository.deleteAll();
        
        log.info("✅ 所有参数预设已清空");
    }
    
    /**
     * 条件查询预设
     * 
     * @param crawlerName 爬虫名称（可选）
     * @param countryCode 国家代码（可选）
     * @param crawlerType 爬虫类型（可选）
     * @param enabled 启用状态（可选）
     * @param pageable 分页参数
     * @return 预设列表
     */
    public Page<UnifiedTaskConfig> getPresetsByCondition(
            String crawlerName, 
            String countryCode, 
            String crawlerType, 
            Boolean enabled, 
            Pageable pageable) {
        
        log.debug("查询预设: crawlerName={}, countryCode={}, crawlerType={}, enabled={}", 
                  crawlerName, countryCode, crawlerType, enabled);
        
        // 先按countryCode查询基础数据
        Page<UnifiedTaskConfig> presets = taskConfigRepository.findByConditions(
            countryCode, 
            crawlerName, 
            "PRESET", 
            enabled, 
            pageable
        );
        
        // 如果指定了crawlerType，进行二次筛选
        if (crawlerType != null && !crawlerType.isEmpty()) {
            List<UnifiedTaskConfig> filteredList = presets.getContent().stream()
                .filter(preset -> {
                    String type = extractCrawlerType(preset.getCrawlerName());
                    return crawlerType.equalsIgnoreCase(type);
                })
                .collect(Collectors.toList());
            
            log.debug("根据crawlerType={} 筛选后剩余 {} 条记录", crawlerType, filteredList.size());
            
            // 返回筛选后的分页结果
            return new PageImpl<>(filteredList, pageable, filteredList.size());
        }
        
        return presets;
    }
    
    /**
     * 从爬虫名称中提取类型
     * 例如: US_510K -> 510K, TW_Registration -> REGISTRATION
     */
    private String extractCrawlerType(String crawlerName) {
        if (crawlerName == null || crawlerName.isEmpty()) {
            return "";
        }
        
        // 爬虫名称格式：US_510K, KR_Event, TW_Registration等
        String[] parts = crawlerName.split("_");
        if (parts.length >= 2) {
            String type = parts[1].toUpperCase();
            
            // 特殊处理：CustomsCase → Customs（与前端保持一致）
            if (type.equals("CUSTOMSCASE")) {
                return "CUSTOMS";
            }
            
            return type;
        }
        
        return "";
    }
    
    /**
     * 根据爬虫获取所有预设
     * 
     * @param crawlerName 爬虫名称
     * @return 预设列表
     */
    public List<UnifiedTaskConfig> getPresetsByCrawler(String crawlerName) {
        return taskConfigRepository.findByCrawlerNameAndTaskTypeAndEnabled(crawlerName, "PRESET", true);
    }
    
    /**
     * 验证预设参数
     * 
     * @param crawlerName 爬虫名称
     * @param parametersJson 参数JSON
     * @return 是否有效
     */
    public boolean validatePresetParams(String crawlerName, String parametersJson) {
        if (parametersJson == null || parametersJson.trim().isEmpty()) {
            return false;
        }
        
        try {
            Map<String, Object> params = objectMapper.readValue(
                parametersJson, 
                new TypeReference<Map<String, Object>>() {}
            );
            
            return schemaRegistry.validateParams(crawlerName, params);
            
        } catch (Exception e) {
            log.error("参数解析失败: {}", parametersJson, e);
            return false;
        }
    }
    
    /**
     * 预设请求类
     */
    public static class PresetRequest {
        private String taskName;
        private String crawlerName;
        private String countryCode;
        private String parameters;
        private String cronExpression;
        private String description;
        private Boolean enabled;
        private Integer priority;
        private Integer timeoutMinutes;
        private Integer retryCount;
        private String createdBy;
        private String updatedBy;
        
        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getCrawlerName() { return crawlerName; }
        public void setCrawlerName(String crawlerName) { this.crawlerName = crawlerName; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
        
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public Integer getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        
        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
        
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }
}


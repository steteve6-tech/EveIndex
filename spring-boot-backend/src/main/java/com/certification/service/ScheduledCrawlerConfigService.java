package com.certification.service;

import com.certification.entity.ScheduledCrawlerConfig;
import com.certification.repository.ScheduledCrawlerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 定时爬取配置管理服务
 */
@Slf4j
@Service
@Transactional
public class ScheduledCrawlerConfigService {

    @Autowired
    private ScheduledCrawlerConfigRepository configRepository;


    /**
     * 获取所有配置
     */
    public List<ScheduledCrawlerConfig> getAllConfigs() {
        return configRepository.findAll().stream()
                .filter(config -> config.getDeleted() == 0)
                .toList();
    }

    /**
     * 分页获取配置
     */
    public Page<ScheduledCrawlerConfig> getConfigsPage(Pageable pageable) {
        return configRepository.findAll(pageable);
    }

    /**
     * 根据ID获取配置
     */
    public Optional<ScheduledCrawlerConfig> getConfigById(Long id) {
        return configRepository.findById(id)
                .filter(config -> config.getDeleted() == 0);
    }

    /**
     * 根据模块名称获取配置
     */
    public List<ScheduledCrawlerConfig> getConfigsByModule(String moduleName) {
        return configRepository.findByModuleNameAndDeleted(moduleName, 0);
    }

    /**
     * 根据爬虫名称获取配置
     */
    public List<ScheduledCrawlerConfig> getConfigsByCrawler(String crawlerName) {
        return configRepository.findByCrawlerNameAndDeleted(crawlerName, 0);
    }

    /**
     * 获取所有启用的配置
     */
    public List<ScheduledCrawlerConfig> getEnabledConfigs() {
        return configRepository.findByEnabledAndDeleted(true, 0);
    }

    /**
     * 获取需要执行的定时任务
     */
    public List<ScheduledCrawlerConfig> getTasksToExecute() {
        return configRepository.findTasksToExecute(LocalDateTime.now());
    }

    /**
     * 创建或更新配置
     */
    public ScheduledCrawlerConfig saveConfig(ScheduledCrawlerConfig config) {
        // 检查是否已存在相同的配置
        Optional<ScheduledCrawlerConfig> existingConfig = configRepository
                .findByModuleNameAndCrawlerNameAndCountryCodeAndDeleted(
                        config.getModuleName(),
                        config.getCrawlerName(),
                        config.getCountryCode(),
                        0);

        if (existingConfig.isPresent() && !existingConfig.get().getId().equals(config.getId())) {
            throw new IllegalArgumentException("相同模块、爬虫和国家的配置已存在");
        }

        // 设置默认值
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (config.getExecutionCount() == null) {
            config.setExecutionCount(0L);
        }
        if (config.getSuccessCount() == null) {
            config.setSuccessCount(0L);
        }
        if (config.getFailureCount() == null) {
            config.setFailureCount(0L);
        }

        return configRepository.save(config);
    }

    /**
     * 删除配置（逻辑删除）
     */
    public void deleteConfig(Long id) {
        Optional<ScheduledCrawlerConfig> config = configRepository.findById(id);
        if (config.isPresent()) {
            config.get().setDeleted(1);
            configRepository.save(config.get());
        }
    }

    /**
     * 启用/禁用配置
     */
    public void toggleConfigStatus(Long id, Boolean enabled) {
        Optional<ScheduledCrawlerConfig> config = configRepository.findById(id);
        if (config.isPresent()) {
            config.get().setEnabled(enabled);
            configRepository.save(config.get());
        }
    }

    /**
     * 更新执行状态
     */
    public void updateExecutionStatus(Long id, ScheduledCrawlerConfig.ExecutionStatus status, 
                                    String result, LocalDateTime executionTime) {
        Optional<ScheduledCrawlerConfig> config = configRepository.findById(id);
        if (config.isPresent()) {
            ScheduledCrawlerConfig configEntity = config.get();
            configEntity.setLastExecutionTime(executionTime);
            configEntity.setLastExecutionStatus(status);
            configEntity.setLastExecutionResult(result);
            configEntity.setExecutionCount(configEntity.getExecutionCount() + 1);
            
            if (status == ScheduledCrawlerConfig.ExecutionStatus.SUCCESS) {
                configEntity.setSuccessCount(configEntity.getSuccessCount() + 1);
            } else if (status == ScheduledCrawlerConfig.ExecutionStatus.FAILED) {
                configEntity.setFailureCount(configEntity.getFailureCount() + 1);
            }
            
            configRepository.save(configEntity);
        }
    }

    /**
     * 更新下次执行时间
     */
    public void updateNextExecutionTime(Long id, LocalDateTime nextTime) {
        Optional<ScheduledCrawlerConfig> config = configRepository.findById(id);
        if (config.isPresent()) {
            config.get().setNextExecutionTime(nextTime);
            configRepository.save(config.get());
        }
    }


    /**
     * 批量创建默认配置
     */
    public void createDefaultConfigs() {
        log.info("开始创建默认定时爬取配置");
        
        // CertNewsData模块的3个爬虫配置
        createConfigIfNotExists("certnewsdata", "SGS", null, "0 0 2 * * ?", 
                "每天凌晨2点执行SGS爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("certnewsdata", "UL", null, "0 30 2 * * ?", 
                "每天凌晨2点30分执行UL爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("certnewsdata", "Beice", null, "0 0 3 * * ?", 
                "每天凌晨3点执行Beice爬虫", "{\"batchSize\": 30, \"maxRecords\": 150}");
        
        // 美国6个实体类的爬虫配置
        createConfigIfNotExists("device510k", "US_510K", "US", "0 0 4 * * ?", 
                "每天凌晨4点执行美国510K爬虫", "{\"batchSize\": 100, \"maxRecords\": 500}");
        
        createConfigIfNotExists("deviceeventreport", "US_Event", "US", "0 30 4 * * ?", 
                "每天凌晨4点30分执行美国事件报告爬虫", "{\"batchSize\": 100, \"maxRecords\": 500}");
        
        createConfigIfNotExists("devicerecallrecord", "US_Recall", "US", "0 0 5 * * ?", 
                "每天凌晨5点执行美国召回记录爬虫", "{\"batchSize\": 100, \"maxRecords\": 500}");
        
        createConfigIfNotExists("deviceregistrationrecord", "US_Registration", "US", "0 30 5 * * ?", 
                "每天凌晨5点30分执行美国注册记录爬虫", "{\"batchSize\": 100, \"maxRecords\": 500}");
        
        createConfigIfNotExists("guidancedocument", "US_Guidance", "US", "0 0 6 * * ?", 
                "每天凌晨6点执行美国指导文档爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("customscase", "US_CustomsCase", "US", "0 30 6 * * ?", 
                "每天凌晨6点30分执行美国海关案例爬虫", "{\"batchSize\": 20, \"maxRecords\": 100}");
        
        // 欧盟6个实体类的爬虫配置
        createConfigIfNotExists("device510k", "EU_510K", "EU", "0 0 7 * * ?", 
                "每天凌晨7点执行欧盟510K爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("deviceeventreport", "EU_Event", "EU", "0 30 7 * * ?", 
                "每天凌晨7点30分执行欧盟事件报告爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("devicerecallrecord", "EU_Recall", "EU", "0 0 8 * * ?", 
                "每天凌晨8点执行欧盟召回记录爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("deviceregistrationrecord", "EU_Registration", "EU", "0 30 8 * * ?", 
                "每天凌晨8点30分执行欧盟注册记录爬虫", "{\"batchSize\": 50, \"maxRecords\": 200}");
        
        createConfigIfNotExists("guidancedocument", "EU_Guidance", "EU", "0 0 9 * * ?", 
                "每天凌晨9点执行欧盟指导文档爬虫", "{\"batchSize\": 30, \"maxRecords\": 150}");
        
        createConfigIfNotExists("customscase", "EU_CustomsCase", "EU", "0 30 9 * * ?", 
                "每天凌晨9点30分执行欧盟海关案例爬虫", "{\"batchSize\": 20, \"maxRecords\": 100}");
        
        log.info("默认定时爬取配置创建完成");
    }

    /**
     * 创建配置（如果不存在）
     */
    private void createConfigIfNotExists(String moduleName, String crawlerName, String countryCode, 
                                       String cronExpression, String description, String crawlParams) {
        Optional<ScheduledCrawlerConfig> existing = configRepository
                .findByModuleNameAndCrawlerNameAndCountryCodeAndDeleted(
                        moduleName, crawlerName, countryCode, 0);
        
        if (existing.isEmpty()) {
            ScheduledCrawlerConfig config = new ScheduledCrawlerConfig();
            config.setModuleName(moduleName);
            config.setCrawlerName(crawlerName);
            config.setCountryCode(countryCode);
            config.setCronExpression(cronExpression);
            config.setDescription(description);
            config.setCrawlParams(crawlParams);
            config.setEnabled(true);
            config.setExecutionCount(0L);
            config.setSuccessCount(0L);
            config.setFailureCount(0L);
            
            configRepository.save(config);
            log.info("创建定时爬取配置: {} - {} - {}", moduleName, crawlerName, countryCode);
        }
    }
}

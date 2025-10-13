package com.certification.service.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 爬虫注册中心
 * 管理所有爬虫适配器，提供统一的查找和调用接口
 */
@Slf4j
@Service
public class CrawlerRegistryService {
    
    /**
     * Spring会自动注入所有ICrawlerExecutor的实现类
     * 如果没有实现类，使用空列表
     */
    @Autowired(required = false)
    private List<ICrawlerExecutor> crawlerExecutors = new ArrayList<>();
    
    /**
     * 爬虫映射表：唯一标识 -> 爬虫执行器
     * 唯一标识格式：{国家代码}_{爬虫类型}，例如：US_510K
     */
    private Map<String, ICrawlerExecutor> crawlerMap = new HashMap<>();
    
    /**
     * 按国家分组的爬虫映射
     */
    private Map<String, List<ICrawlerExecutor>> crawlersByCountry = new HashMap<>();
    
    /**
     * 按类型分组的爬虫映射
     */
    private Map<String, List<ICrawlerExecutor>> crawlersByType = new HashMap<>();
    
    /**
     * 爬虫启用状态映射：爬虫名称 -> 是否启用
     */
    private Map<String, Boolean> crawlerEnabledStatus = new HashMap<>();
    
    /**
     * 初始化：注册所有爬虫
     */
    @PostConstruct
    public void init() {
        log.info("========== 开始注册爬虫适配器 ==========");
        
        if (crawlerExecutors == null || crawlerExecutors.isEmpty()) {
            log.warn("未找到任何爬虫适配器实现！");
            return;
        }
        
        // 注册所有爬虫
        for (ICrawlerExecutor executor : crawlerExecutors) {
            try {
                String key = executor.getUniqueKey();
                crawlerMap.put(key, executor);
                
                // 按国家分组
                crawlersByCountry.computeIfAbsent(executor.getCountryCode(), k -> new ArrayList<>())
                    .add(executor);
                
                // 按类型分组
                crawlersByType.computeIfAbsent(executor.getCrawlerType(), k -> new ArrayList<>())
                    .add(executor);
                
                // 默认启用所有爬虫
                crawlerEnabledStatus.put(executor.getCrawlerName(), true);
                
                log.info("注册爬虫: {} ({}) - {}", 
                    executor.getCrawlerName(), 
                    key, 
                    executor.getDescription());
                
            } catch (Exception e) {
                log.error("注册爬虫失败: {}", executor.getClass().getSimpleName(), e);
            }
        }
        
        log.info("========== 爬虫注册完成 ==========");
        log.info("共注册 {} 个爬虫适配器", crawlerMap.size());
        log.info("支持国家: {}", crawlersByCountry.keySet());
        log.info("支持类型: {}", crawlersByType.keySet());
    }
    
    /**
     * 根据唯一标识获取爬虫
     * 
     * @param uniqueKey 唯一标识（国家_类型）
     * @return 爬虫执行器
     */
    public ICrawlerExecutor getCrawler(String uniqueKey) {
        ICrawlerExecutor executor = crawlerMap.get(uniqueKey);
        if (executor == null) {
            log.warn("未找到爬虫: {}", uniqueKey);
        }
        return executor;
    }
    
    /**
     * 根据国家和类型获取爬虫
     * 
     * @param countryCode 国家代码
     * @param crawlerType 爬虫类型
     * @return 爬虫执行器
     */
    public ICrawlerExecutor getCrawler(String countryCode, String crawlerType) {
        String key = countryCode + "_" + crawlerType;
        return getCrawler(key);
    }
    
    /**
     * 获取指定国家的所有爬虫
     * 
     * @param countryCode 国家代码
     * @return 爬虫列表
     */
    public List<ICrawlerExecutor> getCrawlersByCountry(String countryCode) {
        return crawlersByCountry.getOrDefault(countryCode, Collections.emptyList());
    }
    
    /**
     * 获取指定类型的所有爬虫
     * 
     * @param crawlerType 爬虫类型
     * @return 爬虫列表
     */
    public List<ICrawlerExecutor> getCrawlersByType(String crawlerType) {
        return crawlersByType.getOrDefault(crawlerType, Collections.emptyList());
    }
    
    /**
     * 获取所有已注册的爬虫
     * 
     * @return 爬虫列表
     */
    public List<ICrawlerExecutor> getAllCrawlers() {
        return new ArrayList<>(crawlerMap.values());
    }
    
    /**
     * 获取支持的国家列表
     * 
     * @return 国家代码列表
     */
    public Set<String> getSupportedCountries() {
        return crawlersByCountry.keySet();
    }
    
    /**
     * 获取支持的爬虫类型列表
     * 
     * @return 爬虫类型列表
     */
    public Set<String> getSupportedTypes() {
        return crawlersByType.keySet();
    }
    
    /**
     * 检查爬虫是否存在
     * 
     * @param countryCode 国家代码
     * @param crawlerType 爬虫类型
     * @return 是否存在
     */
    public boolean hasCrawler(String countryCode, String crawlerType) {
        String key = countryCode + "_" + crawlerType;
        return crawlerMap.containsKey(key);
    }
    
    /**
     * 获取爬虫统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCrawlers", crawlerMap.size());
        stats.put("countriesCount", crawlersByCountry.size());
        stats.put("typesCount", crawlersByType.size());
        
        // 按国家统计
        Map<String, Integer> countByCountry = new HashMap<>();
        for (Map.Entry<String, List<ICrawlerExecutor>> entry : crawlersByCountry.entrySet()) {
            countByCountry.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("countByCountry", countByCountry);
        
        // 按类型统计
        Map<String, Integer> countByType = new HashMap<>();
        for (Map.Entry<String, List<ICrawlerExecutor>> entry : crawlersByType.entrySet()) {
            countByType.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("countByType", countByType);
        
        return stats;
    }
    
    /**
     * 获取所有爬虫信息列表
     * 
     * @return 爬虫信息列表
     */
    public List<Map<String, Object>> getAllCrawlerInfo() {
        return crawlerMap.values().stream()
            .map(executor -> {
                Map<String, Object> info = new HashMap<>();
                info.put("crawlerName", executor.getCrawlerName());
                info.put("countryCode", executor.getCountryCode());
                info.put("crawlerType", executor.getCrawlerType());
                info.put("uniqueKey", executor.getUniqueKey());
                info.put("description", executor.getDescription());
                info.put("version", executor.getVersion());
                info.put("available", executor.isAvailable());
                info.put("enabled", crawlerEnabledStatus.getOrDefault(executor.getCrawlerName(), true));
                return info;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 启用爬虫
     * 
     * @param crawlerName 爬虫名称
     * @return 是否成功
     */
    public boolean enableCrawler(String crawlerName) {
        if (!crawlerMap.containsKey(crawlerName)) {
            log.warn("爬虫不存在: {}", crawlerName);
            return false;
        }
        crawlerEnabledStatus.put(crawlerName, true);
        log.info("启用爬虫: {}", crawlerName);
        return true;
    }
    
    /**
     * 停用爬虫
     * 
     * @param crawlerName 爬虫名称
     * @return 是否成功
     */
    public boolean disableCrawler(String crawlerName) {
        if (!crawlerMap.containsKey(crawlerName)) {
            log.warn("爬虫不存在: {}", crawlerName);
            return false;
        }
        crawlerEnabledStatus.put(crawlerName, false);
        log.info("停用爬虫: {}", crawlerName);
        return true;
    }
    
    /**
     * 检查爬虫是否启用
     * 
     * @param crawlerName 爬虫名称
     * @return 是否启用
     */
    public boolean isCrawlerEnabled(String crawlerName) {
        return crawlerEnabledStatus.getOrDefault(crawlerName, true);
    }
    
    /**
     * 获取所有启用的爬虫
     * 
     * @return 启用的爬虫列表
     */
    public List<ICrawlerExecutor> getEnabledCrawlers() {
        return crawlerMap.values().stream()
            .filter(executor -> isCrawlerEnabled(executor.getCrawlerName()))
            .collect(Collectors.toList());
    }
}


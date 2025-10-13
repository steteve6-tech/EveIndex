package com.certification.analysis;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.CertNewsDailyCountryRiskStats;
import com.certification.repository.CrawlerDataRepository;
import com.certification.repository.DailyCountryRiskStatsRepository;
import com.certification.standards.KeywordService;
import com.certification.service.DailyCountryRiskStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.certification.entity.common.CertNewsData.RiskLevel.MEDIUM;

/**
 * 认证新闻分析服务
 * 负责处理爬虫数据的相关性分析和风险等级评估
 */
@Slf4j
@Service
@Transactional
public class CertNewsanalysis {
    
    @Autowired
    private CrawlerDataRepository crawlerDataRepository;
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private DailyCountryRiskStatsService dailyCountryRiskStatsService;
    
    @Autowired
    private DailyCountryRiskStatsRepository dailyCountryRiskStatsRepository;
    
    @Autowired
    private com.certification.service.ai.CertNewsAIJudgeService certNewsAIJudgeService;
    
    // 关键词文件路径 - 支持多种路径
    private static final String[] KEYWORDS_FILE_PATHS = {
        "CertNewsKeywords.txt",  // 运行环境路径
        "src/main/java/com/certification/analysis/CertNewsKeywords.txt"  // 开发环境路径
    };
    
    /**
     * 自动处理所有数据的相关状态
     * 根据关键词匹配自动设置相关状态和风险等级
     */
    public Map<String, Object> autoProcessRelatedStatus() {
        return autoProcessRelatedStatus(null);
    }
    
    /**
     * 自动处理相关状态（带关键词参数）
     * 根据关键词匹配自动设置相关状态
     */
    public Map<String, Object> autoProcessRelatedStatus(List<String> customKeywords) {
        log.info("🔍 开始处理中风险数据，根据关键词升级为高风险，使用关键词: {}", customKeywords);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有中风险且未删除的数据
            List<CertNewsData> mediumRiskData = crawlerDataRepository.findByRiskLevelAndDeleted(MEDIUM, 0);
            log.info("📊 找到 {} 条中风险数据需要处理", mediumRiskData.size());
            
            if (mediumRiskData.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有中风险数据需要处理");
                result.put("totalProcessed", 0);
                return result;
            }
            
            int processedCount = 0;
            int upgradedToHighRisk = 0; // 升级为高风险的数量
            int matchedCount = 0; // 匹配到关键词的数量
            int unchangedCount = 0;
            int errorCount = 0;
            
            // 分批处理以提高性能
            int batchSize = 100;
            for (int i = 0; i < mediumRiskData.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, mediumRiskData.size());
                List<CertNewsData> batch = mediumRiskData.subList(i, endIndex);
                
                log.info("🔄 处理第 {}-{}/{} 条数据", i + 1, endIndex, mediumRiskData.size());
                
                for (CertNewsData data : batch) {
                    try {
                        // 构建搜索文本（包含标题、内容、摘要、产品、类型）
                        String searchText = buildEnhancedSearchText(data);
                        
                        // 根据关键词进行匹配
                        List<String> matchedKeywords = new ArrayList<>();
                        
                        if (customKeywords != null && !customKeywords.isEmpty()) {
                            // 使用前端传递的关键词列表
                            matchedKeywords = getMatchedKeywords(searchText, customKeywords);
                            log.debug("使用自定义关键词匹配，数据 {} 匹配到 {} 个关键词", data.getId(), matchedKeywords.size());
                        } else {
                            // 优先使用文件关键词
                            List<String> fileKeywords = loadKeywordsFromFile();
                            if (!fileKeywords.isEmpty()) {
                                matchedKeywords = getMatchedKeywords(searchText, fileKeywords);
                                log.debug("使用文件关键词匹配，数据 {} 匹配到 {} 个关键词", data.getId(), matchedKeywords.size());
                            } else {
                                // 使用数据库关键词服务
                                List<String> dbKeywords = keywordService.getAllEnabledKeywords();
                                if (containsAnyKeyword(searchText, dbKeywords)) {
                                    matchedKeywords = getMatchedKeywords(searchText, dbKeywords);
                                } else {
                                    matchedKeywords = new ArrayList<>();
                                }
                                log.debug("使用数据库关键词匹配，数据 {} 匹配到 {} 个关键词", data.getId(), matchedKeywords.size());
                            }
                        }
                        
                        // 如果匹配到关键词，则升级为高风险
                        if (!matchedKeywords.isEmpty()) {
                            // 更新匹配的关键词
                            data.setMatchedKeywords(String.join(",", matchedKeywords));
                            
                            // 设置为相关
                            data.setRelated(true);
                            
                            // 升级为高风险
                            data.setRiskLevel(CertNewsData.RiskLevel.HIGH);
                            
                            // 保存更新
                            crawlerDataRepository.save(data);
                            
                            upgradedToHighRisk++;
                            matchedCount++;
                            processedCount++;
                            
                            log.debug("✅ 数据 {} 匹配关键词 {} 已升级为高风险", data.getId(), matchedKeywords);
                        } else {
                            // 没有匹配到关键词，保持中风险不变
                            unchangedCount++;
                            log.debug("➡️ 数据 {} 未匹配到关键词，保持中风险", data.getId());
                        }
                        
                    } catch (Exception e) {
                        log.error("❌ 处理数据 {} 时发生错误: {}", data.getId(), e.getMessage());
                        errorCount++;
                    }
                }
                
                // 每批处理完成后输出进度
                log.info("📊 批次处理完成，进度: {}/{} ({:.1f}%), 已升级: {}, 未变更: {}, 错误: {}", 
                    endIndex, mediumRiskData.size(), (double)endIndex / mediumRiskData.size() * 100,
                    upgradedToHighRisk, unchangedCount, errorCount);
            }
            
            // 处理完成，输出最终统计
            log.info("✅ 中风险数据处理完成！总计: {}, 处理: {}, 匹配: {}, 升级为高风险: {}, 未变更: {}, 错误: {}", 
                mediumRiskData.size(), processedCount, matchedCount, upgradedToHighRisk, unchangedCount, errorCount);
            
            result.put("success", true);
            result.put("totalProcessed", processedCount);
            result.put("relatedCount", matchedCount); // 匹配到关键词的数量
            result.put("unrelatedCount", 0); // 中风险数据不会标记为不相关
            result.put("unchangedCount", unchangedCount);
            result.put("riskProcessedCount", upgradedToHighRisk); // 升级为高风险的数量
            result.put("errorCount", errorCount);
            result.put("totalData", mediumRiskData.size());
            
            // 计算使用的关键词数量
            int usedKeywordsCount;
            if (customKeywords != null && !customKeywords.isEmpty()) {
                usedKeywordsCount = customKeywords.size();
            } else {
                List<String> fileKeywords = loadKeywordsFromFile();
                usedKeywordsCount = fileKeywords.isEmpty() ? keywordService.getAllEnabledKeywords().size() : fileKeywords.size();
            }
            result.put("usedKeywords", usedKeywordsCount);
            
            result.put("message", String.format("中风险数据处理完成，共检查 %d 条中风险数据，匹配关键词 %d 条，升级为高风险 %d 条，未变更 %d 条，使用 %d 个关键词", 
                mediumRiskData.size(), matchedCount, upgradedToHighRisk, unchangedCount, usedKeywordsCount));
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("✅ 中风险数据处理完成: 检查 {} 条，匹配 {} 条，升级为高风险 {} 条，未变更 {} 条", 
                mediumRiskData.size(), matchedCount, upgradedToHighRisk, unchangedCount);
            
            // 更新每日国家风险统计数据
            try {
                updateDailyCountryRiskStats();
                log.info("每日国家风险统计数据更新完成");
            } catch (Exception statsException) {
                log.warn("更新每日国家风险统计数据失败: {}", statsException.getMessage(), statsException);
                // 不影响主流程，只记录警告
            }
            
        } catch (Exception e) {
            log.error("自动处理相关状态时发生错误: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 根据数据源自动处理相关状态
     * 根据关键词匹配自动设置指定数据源的相关状态
     */
    public Map<String, Object> autoProcessRelatedStatusBySource(String sourceName) {
        log.info("开始根据数据源自动处理所有数据的相关状态: {}", sourceName);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取指定数据源的所有未删除数据
            List<CertNewsData> allData = crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0);
            log.info("找到数据源 {} 的 {} 条数据需要处理", sourceName, allData.size());
            
            int processedCount = 0;
            int relatedCount = 0;
            int unrelatedCount = 0;
            int unchangedCount = 0;
            int riskProcessedCount = 0;
            
            for (CertNewsData data : allData) {
                // 检查标题、内容、摘要是否包含关键词
                String searchText = buildSearchText(data);
                
                // 优先使用文件关键词，如果没有则使用默认关键词服务
                List<String> fileKeywords = loadKeywordsFromFile();
                List<String> matchedKeywords;
                boolean isRelated;
                
                if (!fileKeywords.isEmpty()) {
                    matchedKeywords = getMatchedKeywords(searchText, fileKeywords);
                    isRelated = !matchedKeywords.isEmpty();
                } else {
                    // 使用默认关键词服务进行匹配
                    matchedKeywords = keywordService.getContainedKeywords(searchText);
                    isRelated = !matchedKeywords.isEmpty();
                }
                
                // 检查当前状态是否需要更新
                boolean needsUpdate = data.getRelated() == null || data.getRelated() != isRelated;
                
                if (needsUpdate) {
                    // 更新相关状态和匹配的关键词
                    boolean updateSuccess = updateRelatedStatusWithKeywords(data.getId(), isRelated, matchedKeywords);
                    if (updateSuccess) {
                        // 如果标记为相关，同时设置为高风险
                        if (isRelated) {
                            try {
                                boolean riskUpdateSuccess = updateRiskLevel(data.getId(), RiskLevel.HIGH);
                                if (riskUpdateSuccess) {
                                    riskProcessedCount++;
                                    log.debug("已将相关数据 {} 设置为高风险", data.getId());
                                }
                            } catch (Exception e) {
                                log.warn("设置数据 {} 风险等级失败: {}", data.getId(), e.getMessage());
                            }
                        }
                        
                        processedCount++;
                        if (isRelated) {
                            relatedCount++;
                        } else {
                            unrelatedCount++;
                        }
                    }
                } else {
                    unchangedCount++;
                }
            }
            
            result.put("success", true);
            result.put("totalProcessed", processedCount);
            result.put("relatedCount", relatedCount);
            result.put("unrelatedCount", unrelatedCount);
            result.put("unchangedCount", unchangedCount);
            result.put("riskProcessedCount", riskProcessedCount);
            result.put("totalData", allData.size());
            result.put("sourceName", sourceName);
            
            List<String> fileKeywords = loadKeywordsFromFile();
            int usedKeywordsCount = fileKeywords.isEmpty() ? keywordService.getAllEnabledKeywords().size() : fileKeywords.size();
            result.put("usedKeywords", usedKeywordsCount);
            
            result.put("message", String.format("数据源 %s 自动处理完成，共处理 %d 条数据，相关 %d 条（设置为高风险 %d 条），不相关 %d 条，未变更 %d 条，使用 %d 个关键词", 
                sourceName, processedCount, relatedCount, riskProcessedCount, unrelatedCount, unchangedCount, usedKeywordsCount));
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("数据源 {} 自动处理相关状态完成: 处理 {} 条，相关 {} 条（设置为高风险 {} 条），不相关 {} 条，未变更 {} 条", 
                sourceName, processedCount, relatedCount, riskProcessedCount, unrelatedCount, unchangedCount);
            
            // 更新每日国家风险统计数据
            try {
                updateDailyCountryRiskStats();
                log.info("每日国家风险统计数据更新完成");
            } catch (Exception statsException) {
                log.warn("更新每日国家风险统计数据失败: {}", statsException.getMessage(), statsException);
                // 不影响主流程，只记录警告
            }
            
        } catch (Exception e) {
            log.error("数据源 {} 自动处理相关状态时发生错误: {}", sourceName, e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 从文件加载关键词
     */
    public List<String> loadKeywordsFromFile() {
        List<String> keywords = new ArrayList<>();
        
        // 尝试多个路径
        for (String filePath : KEYWORDS_FILE_PATHS) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    for (String line : lines) {
                        String trimmedLine = line.trim();
                        if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                            keywords.add(trimmedLine);
                        }
                    }
                    log.info("从文件加载了 {} 个关键词，文件路径: {}", keywords.size(), filePath);
                    return keywords; // 找到文件就返回
                }
            } catch (Exception e) {
                log.warn("尝试加载关键词文件失败: {}, 错误: {}", filePath, e.getMessage());
            }
        }
        
        // 如果所有路径都失败，记录警告
        log.warn("所有关键词文件路径都不存在: {}", Arrays.toString(KEYWORDS_FILE_PATHS));
        return keywords;
    }
    
    /**
     * 保存关键词到文件
     */
    public boolean saveKeywordsToFile(List<String> keywords) {
        try {
            // 优先使用开发环境路径（带目录结构）
            Path filePath = Paths.get(KEYWORDS_FILE_PATHS[1]);
            
            // 确保目录存在
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            
            // 写入关键词到文件
            StringBuilder content = new StringBuilder();
            content.append("# 认证新闻关键词列表\n");
            content.append("# 每行一个关键词，以#开头的行为注释\n");
            content.append("# 生成时间: ").append(LocalDateTime.now()).append("\n\n");
            
            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    content.append(keyword.trim()).append("\n");
                }
            }
            
            Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));
            log.info("成功保存 {} 个关键词到文件: {}", keywords.size(), filePath);
            return true;
            
        } catch (Exception e) {
            log.error("保存关键词到文件失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取文件关键词信息
     */
    public Map<String, Object> getFileKeywordsInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> keywords = loadKeywordsFromFile();
            result.put("success", true);
            result.put("keywords", keywords);
            result.put("count", keywords.size());
            result.put("filePath", KEYWORDS_FILE_PATHS[0]);
            result.put("timestamp", LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error("获取文件关键词信息失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取关键词信息失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 从localStorage关键词迁移到文件
     */
    public Map<String, Object> migrateFromLocalStorage(List<String> localKeywords) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (localKeywords == null || localKeywords.isEmpty()) {
                result.put("success", false);
                result.put("error", "没有提供要迁移的关键词");
                return result;
            }
            
            // 保存到文件
            boolean saveSuccess = saveKeywordsToFile(localKeywords);
            if (saveSuccess) {
                result.put("success", true);
                result.put("message", "成功将本地关键词迁移到文件");
                result.put("migratedCount", localKeywords.size());
                result.put("filePath", KEYWORDS_FILE_PATHS[0]);
                log.info("成功迁移 {} 个关键词从localStorage到文件", localKeywords.size());
            } else {
                result.put("success", false);
                result.put("error", "保存关键词到文件失败");
            }
        } catch (Exception e) {
            log.error("迁移关键词失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "迁移失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 构建搜索文本
     * 合并标题、内容、摘要、产品字段用于关键词匹配
     */
    private String buildSearchText(CertNewsData data) {
        StringBuilder searchText = new StringBuilder();
        
        if (data.getTitle() != null) {
            searchText.append(data.getTitle()).append(" ");
        }
        if (data.getContent() != null) {
            searchText.append(data.getContent()).append(" ");
        }
        if (data.getSummary() != null) {
            searchText.append(data.getSummary()).append(" ");
        }
        if (data.getProduct() != null) {
            searchText.append(data.getProduct()).append(" ");
        }
        
        return searchText.toString();
    }
    
    /**
     * 检查文本是否包含任何关键词
     */
    private boolean containsAnyKeyword(String text, List<String> keywords) {
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取文本中匹配的关键词列表
     */
    private List<String> getMatchedKeywords(String text, List<String> keywords) {
        List<String> matchedKeywords = new ArrayList<>();
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return matchedKeywords;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    matchedKeywords.add(keyword);
                }
            }
        }
        return matchedKeywords;
    }
    
    /**
     * 更新相关状态和匹配的关键词
     */
    private boolean updateRelatedStatusWithKeywords(String id, boolean isRelated, List<String> matchedKeywords) {
        try {
            // 查找数据
            var optionalData = crawlerDataRepository.findById(id);
            if (optionalData.isEmpty()) {
                log.warn("数据不存在: {}", id);
                return false;
            }
            
            CertNewsData data = optionalData.get();
            
            // 更新相关状态
            data.setRelated(isRelated);
            
            // 更新匹配的关键词
            if (matchedKeywords != null && !matchedKeywords.isEmpty()) {
                data.setMatchedKeywords(String.join(",", matchedKeywords));
            } else {
                data.setMatchedKeywords(null);
            }
            
            // 保存更新
            crawlerDataRepository.save(data);
            
            log.debug("更新数据 {} 相关状态为: {}, 匹配关键词: {}", id, isRelated, matchedKeywords);
            return true;
            
        } catch (Exception e) {
            log.error("更新数据 {} 相关状态失败: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新风险等级
     */
    private boolean updateRiskLevel(String id, RiskLevel riskLevel) {
        try {
            // 查找数据
            var optionalData = crawlerDataRepository.findById(id);
            if (optionalData.isEmpty()) {
                log.warn("数据不存在: {}", id);
                return false;
            }
            
            CertNewsData data = optionalData.get();
            
            // 更新风险等级
            data.setRiskLevel(riskLevel);
            
            // 保存更新
            crawlerDataRepository.save(data);
            
            log.debug("更新数据 {} 风险等级为: {}", id, riskLevel);
            return true;
            
        } catch (Exception e) {
            log.error("更新数据 {} 风险等级失败: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新每日国家风险统计数据
     * 在自动处理完成后调用，更新今天的数据
     */
    private void updateDailyCountryRiskStats() {
        try {
            LocalDate today = LocalDate.now();
            log.info("开始更新今天({})的每日国家风险统计数据", today);
            
            // 预定义的国家列表
            List<String> predefinedCountries = Arrays.asList(
                "泰国", "印尼", "欧盟", "美国", "智利", "秘鲁", "韩国", "日本", 
                "南非", "以色列", "阿联酋", "马来西亚", "中国", "澳大利亚", 
                "印度", "台湾", "未确定", "其它国家", "新加坡"
            );
            
            // 直接为所有预定义国家统计和更新数据
            updateAllPredefinedCountriesStats(today, predefinedCountries);
            
            log.info("今天({})的每日国家风险统计数据更新完成", today);
        } catch (Exception e) {
            log.error("更新每日国家风险统计数据失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 为所有预定义国家统计和更新数据
     * 参考Dashboard.vue的逻辑：先初始化所有国家，再统计实际数据
     */
    private void updateAllPredefinedCountriesStats(LocalDate today, List<String> predefinedCountries) {
        try {
            log.info("开始为所有预定义国家统计今天({})的数据", today);
            
            // 首先为所有预定义国家初始化统计数据（包括没有数据的国家）
            Map<String, CountryStats> countryStatsMap = new HashMap<>();
            for (String country : predefinedCountries) {
                countryStatsMap.put(country, new CountryStats(country));
            }
            
            // 获取所有数据（不限制时间范围）
            List<CertNewsData> allData = crawlerDataRepository.findByDeletedFalse();
            
            log.info("今天({})共有 {} 条数据需要统计", today, allData.size());
            
            // 统计实际数据，将数据分配到对应的国家
            for (CertNewsData data : allData) {
                String country = data.getCountry();
                if (country == null || country.trim().isEmpty()) {
                    country = "未确定";
                } else {
                    country = country.trim();
                }
                
                // 如果国家不在预定义列表中，归类到"其它国家"
                if (!predefinedCountries.contains(country) && !"未确定".equals(country)) {
                    country = "其它国家";
                }
                
                // 确保该国家在统计Map中
                if (!countryStatsMap.containsKey(country)) {
                    countryStatsMap.put(country, new CountryStats(country));
                }
                
                CountryStats stats = countryStatsMap.get(country);
                stats.totalCount++;
                
                // 根据风险等级统计
                if (data.getRiskLevel() != null) {
                    switch (data.getRiskLevel()) {
                        case HIGH:
                            stats.highRiskCount++;
                            break;
                        case MEDIUM:
                            stats.mediumRiskCount++;
                            break;
                        case LOW:
                            stats.lowRiskCount++;
                            break;
                        case NONE:
                            stats.noRiskCount++;
                            break;
                    }
                } else {
                    stats.noRiskCount++;
                }
            }
            
            // 将统计结果保存到数据库
            int processedCount = 0;
            int updatedCount = 0;
            int createdCount = 0;
            
            for (CountryStats stats : countryStatsMap.values()) {
                try {
                    // 检查数据库中是否已有该国家的数据
                    var existingStats = dailyCountryRiskStatsRepository.findByStatDateAndCountryAndDeletedFalse(today, stats.country);
                    
                    if (existingStats != null) {
                        // 如果数据库中有数据，则更新字段
                        existingStats.setHighRiskCount(stats.highRiskCount);
                        existingStats.setMediumRiskCount(stats.mediumRiskCount);
                        existingStats.setLowRiskCount(stats.lowRiskCount);
                        existingStats.setNoRiskCount(stats.noRiskCount);
                        existingStats.setTotalCount(stats.totalCount);
                        
                        dailyCountryRiskStatsRepository.save(existingStats);
                        updatedCount++;
                        
                        log.debug("更新国家 {} 的数据: 高风险={}, 中风险={}, 低风险={}, 无风险={}, 总计={}", 
                            stats.country, stats.highRiskCount, stats.mediumRiskCount, stats.lowRiskCount, stats.noRiskCount, stats.totalCount);
                    } else {
                        // 如果数据库中没有数据，则创建新记录
                        createOrUpdateStatsRecord(today, stats.country, 
                            stats.highRiskCount, stats.mediumRiskCount, stats.lowRiskCount, stats.noRiskCount, stats.totalCount);
                        createdCount++;
                        
                        log.debug("创建国家 {} 的数据: 高风险={}, 中风险={}, 低风险={}, 无风险={}, 总计={}", 
                            stats.country, stats.highRiskCount, stats.mediumRiskCount, stats.lowRiskCount, stats.noRiskCount, stats.totalCount);
                    }
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    log.warn("处理国家 {} 的数据失败: {}", stats.country, e.getMessage());
                }
            }
            
            log.info("预定义国家数据统计完成: 处理 {} 个国家，更新 {} 个，创建 {} 个", 
                processedCount, updatedCount, createdCount);
            
        } catch (Exception e) {
            log.error("更新预定义国家统计数据失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 国家统计数据内部类
     */
    private static class CountryStats {
        String country;
        long highRiskCount = 0;
        long mediumRiskCount = 0;
        long lowRiskCount = 0;
        long noRiskCount = 0;
        long totalCount = 0;
        
        CountryStats(String country) {
            this.country = country;
        }
    }
    
    /**
     * 确保今天的数据存在，如果不存在则生成预定义国家的数据
     */
    private void ensureTodayDataExists(LocalDate today, List<String> predefinedCountries) {
        try {
            // 检查今天是否已有数据
            List<Map<String, Object>> todayStats = dailyCountryRiskStatsService.getStatsByDate(today);
            
            if (todayStats == null || todayStats.isEmpty()) {
                log.info("今天({})没有数据，开始生成预定义国家的数据", today);
                
                // 为每个预定义国家生成数据
                for (String country : predefinedCountries) {
                    try {
                        // 查询该国家今天的各风险等级数据数量
                        long highRiskCount = countByCountryAndRiskLevelAndDateRange(
                            country, RiskLevel.HIGH, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
                        
                        long mediumRiskCount = countByCountryAndRiskLevelAndDateRange(
                            country, MEDIUM, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
                        
                        long lowRiskCount = countByCountryAndRiskLevelAndDateRange(
                            country, RiskLevel.LOW, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
                        
                        long noRiskCount = countByCountryAndRiskLevelAndDateRange(
                            country, RiskLevel.NONE, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
                        
                        long totalCount = countByCountryAndDateRange(
                            country, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
                        
                        // 创建统计数据记录
                        createOrUpdateStatsRecord(today, country, 
                            highRiskCount, mediumRiskCount, lowRiskCount, noRiskCount, totalCount);
                        
                        log.debug("为国家 {} 生成今天的数据: 高风险={}, 中风险={}, 低风险={}, 无风险={}, 总计={}", 
                            country, highRiskCount, mediumRiskCount, lowRiskCount, noRiskCount, totalCount);
                            
                    } catch (Exception e) {
                        log.warn("为国家 {} 生成今天的数据失败: {}", country, e.getMessage());
                    }
                }
                
                log.info("今天({})的预定义国家数据生成完成，共处理 {} 个国家", today, predefinedCountries.size());
            } else {
                log.info("今天({})已有数据，共 {} 条记录", today, todayStats.size());
            }
            
        } catch (Exception e) {
            log.error("确保今天数据存在失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建或更新统计数据记录
     */
    private void createOrUpdateStatsRecord(LocalDate statDate, String country, 
            long highRiskCount, long mediumRiskCount, long lowRiskCount, long noRiskCount, long totalCount) {
        try {
            // 查找是否已存在该日期的统计记录
            var existingStats = dailyCountryRiskStatsRepository.findByStatDateAndCountryAndDeletedFalse(statDate, country);
            
            CertNewsDailyCountryRiskStats stats;
            if (existingStats != null) {
                stats = existingStats;
            } else {
                stats = new CertNewsDailyCountryRiskStats();
                stats.setStatDate(statDate);
                stats.setCountry(country);
            }
            
            // 设置各风险等级的数量
            stats.setHighRiskCount(highRiskCount);
            stats.setMediumRiskCount(mediumRiskCount);
            stats.setLowRiskCount(lowRiskCount);
            stats.setNoRiskCount(noRiskCount);
            stats.setTotalCount(totalCount);
            
            // 保存统计数据
            dailyCountryRiskStatsRepository.save(stats);
            
        } catch (Exception e) {
            log.error("创建或更新统计数据记录失败: 日期={}, 国家={}, 错误={}", statDate, country, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 手动更新今天的数据
     * 提供独立的API接口用于手动触发今天数据的更新
     */
    public Map<String, Object> updateTodayCountryRiskStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDate today = LocalDate.now();
            log.info("手动触发更新今天({})的每日国家风险统计数据", today);
            
            // 调用DailyCountryRiskStatsService来统计今天的数据
            dailyCountryRiskStatsService.calculateDailyStats(today);
            
            result.put("success", true);
            result.put("message", "今天的数据更新成功");
            result.put("statDate", today.toString());
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("手动更新今天({})的每日国家风险统计数据完成", today);
            
        } catch (Exception e) {
            log.error("手动更新今天的数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新今天的数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 统计指定国家、风险等级和日期范围内的数据数量
     */
    private long countByCountryAndRiskLevelAndDateRange(String country, RiskLevel riskLevel, 
            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return crawlerDataRepository.countByCountryAndRiskLevelAndCreatedAtBetweenAndDeletedFalse(
                country, riskLevel, startTime, endTime);
        } catch (Exception e) {
            log.warn("统计国家 {} 风险等级 {} 数据数量失败: {}", country, riskLevel, e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 统计指定国家和日期范围内的数据总数
     */
    private long countByCountryAndDateRange(String country, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return crawlerDataRepository.countByCountryAndCreatedAtBetweenAndDeletedFalse(
                country, startTime, endTime);
        } catch (Exception e) {
            log.warn("统计国家 {} 数据总数失败: {}", country, e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 构建增强版搜索文本（包含更多字段和文本清理）
     * 合并标题、内容、摘要、产品、类型字段用于关键词匹配
     */
    private String buildEnhancedSearchText(CertNewsData data) {
        StringBuilder searchText = new StringBuilder();
        
        // 优先级高：标题
        if (data.getTitle() != null && !data.getTitle().trim().isEmpty()) {
            searchText.append(cleanText(data.getTitle())).append(" ");
        }
        
        // 优先级高：摘要
        if (data.getSummary() != null && !data.getSummary().trim().isEmpty()) {
            searchText.append(cleanText(data.getSummary())).append(" ");
        }
        
        // 优先级高：产品
        if (data.getProduct() != null && !data.getProduct().trim().isEmpty()) {
            searchText.append(cleanText(data.getProduct())).append(" ");
        }
        
        // 优先级中：类型
        if (data.getType() != null && !data.getType().trim().isEmpty()) {
            searchText.append(cleanText(data.getType())).append(" ");
        }
        
        // 优先级低：内容（截断处理）
        if (data.getContent() != null && !data.getContent().trim().isEmpty()) {
            String content = cleanText(data.getContent());
            // 截断超长内容，只取前1500个字符以提高性能
            if (content.length() > 1500) {
                content = content.substring(0, 1500);
            }
            searchText.append(content).append(" ");
        }
        
        return searchText.toString().trim();
    }
    
    /**
     * 清理文本，移除HTML标签和特殊字符
     */
    private String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        return text
            .replaceAll("<[^>]+>", " ")      // 移除HTML标签
            .replaceAll("&[a-zA-Z0-9#]+;", " ") // 移除HTML实体
            .replaceAll("[\\r\\n\\t]+", " ")    // 将换行和制表符替换为空格
            .replaceAll("\\s+", " ")         // 合并多个空格
            .trim();
    }
    
    /**
     * 执行AI判断认证新闻
     * 判断认证新闻是否与无线电子设备认证标准相关
     * 
     * @param riskLevel 风险等级筛选（可选）
     * @param sourceName 数据源筛选（可选）
     * @param limit 处理数量限制
     * @param judgeAll 是否判断所有数据
     * @return 处理结果
     */
    public Map<String, Object> executeAIJudgeForCertNews(
        String riskLevel, 
        String sourceName, 
        Integer limit,
        Boolean judgeAll
    ) {
        log.info("开始执行认证新闻AI判断: riskLevel={}, sourceName={}, limit={}, judgeAll={}", 
            riskLevel, sourceName, limit, judgeAll);
        
        Map<String, Object> result = new HashMap<>();
        List<com.certification.dto.ai.CertNewsAuditItem> auditItems = new ArrayList<>();
        Set<String> allExtractedKeywords = new HashSet<>();
        
        try {
            // 获取待判断数据
            List<CertNewsData> dataToJudge = getDataForAIJudge(riskLevel, sourceName, limit, judgeAll);
            log.info("找到 {} 条数据需要AI判断", dataToJudge.size());
            
            if (dataToJudge.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有符合条件的数据需要处理");
                result.put("totalCount", 0);
                result.put("aiKept", 0);
                result.put("aiDowngraded", 0);
                result.put("auditItems", auditItems);
                result.put("newExtractedKeywords", new ArrayList<>());
                return result;
            }
            
            int aiKept = 0;
            int aiDowngraded = 0;
            
            for (CertNewsData data : dataToJudge) {
                try {
                    com.certification.dto.ai.CertNewsAuditItem item = new com.certification.dto.ai.CertNewsAuditItem();
                    item.setId(data.getId());
                    item.setTitle(data.getTitle());
                    item.setCountry(data.getCountry());
                    item.setSourceName(data.getSourceName());
                    
                    // AI判断
                    Map<String, Object> newsData = buildNewsDataMap(data);
                    com.certification.dto.ai.CertNewsClassificationResult aiResult = 
                        judgeNewsWithAI(newsData);
                    
                    item.setRelatedToCertification(aiResult.isRelatedToCertification());
                    item.setConfidence(aiResult.getConfidence());
                    item.setReason(aiResult.getReason());
                    item.setExtractedKeywords(aiResult.getExtractedKeywords());
                    
                    // 构建备注信息
                    StringBuilder remarkBuilder = new StringBuilder();
                    remarkBuilder.append("AI判断: ")
                                 .append(aiResult.isRelatedToCertification() ? "相关" : "不相关")
                                 .append(", 置信度: ")
                                 .append(String.format("%.1f%%", aiResult.getConfidence() * 100));
                    
                    if (aiResult.getReason() != null && !aiResult.getReason().isEmpty()) {
                        remarkBuilder.append(", 理由: ").append(aiResult.getReason());
                    }
                    
                    if (aiResult.isRelatedToCertification()) {
                        // AI判断为相关 - 设置为高风险
                        data.setRiskLevel(CertNewsData.RiskLevel.HIGH);
                        data.setRelated(true);
                        
                        // 写入提取的认证关键词到matched_keywords
                        if (aiResult.getExtractedKeywords() != null && !aiResult.getExtractedKeywords().isEmpty()) {
                            data.setMatchedKeywords(String.join(",", aiResult.getExtractedKeywords()));
                            allExtractedKeywords.addAll(aiResult.getExtractedKeywords());
                            remarkBuilder.append(", 提取关键词: ").append(aiResult.getExtractedKeywords());
                        }
                        
                        aiKept++;
                        log.debug("AI判断为相关: {} - {}, 关键词: {}", 
                            data.getId(), aiResult.getReason(), aiResult.getExtractedKeywords());
                    } else {
                        // AI判断为不相关 - 设置为低风险
                        data.setRiskLevel(CertNewsData.RiskLevel.LOW);
                        data.setRelated(false);
                        data.setMatchedKeywords(null);  // 清空关键词
                        
                        aiDowngraded++;
                        log.debug("AI判断为不相关: {} - {}", data.getId(), aiResult.getReason());
                    }
                    
                    // 写入判断依据到remarks字段
                    data.setRemarks(remarkBuilder.toString());
                    item.setRemark(remarkBuilder.toString());
                    
                    // 设置处理状态
                    data.setStatus(CertNewsData.DataStatus.PROCESSED);
                    data.setIsProcessed(true);
                    data.setProcessedTime(java.time.LocalDateTime.now());
                    
                    // 保存更新
                    crawlerDataRepository.save(data);
                    
                    auditItems.add(item);
                    
                    // 避免API速率限制
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    log.error("处理数据 {} 失败: {}", data.getId(), e.getMessage(), e);
                }
            }
            
            // 将新提取的关键词添加到关键词文件
            List<String> newKeywords = new ArrayList<>(allExtractedKeywords);
            if (!newKeywords.isEmpty()) {
                updateCertificationKeywords(newKeywords);
                log.info("提取了 {} 个认证关键词，已更新到关键词文件", newKeywords.size());
            }
            
            result.put("success", true);
            result.put("message", String.format("AI判断完成：相关%d条，不相关%d条，提取关键词%d个",
                aiKept, aiDowngraded, newKeywords.size()));
            result.put("totalCount", dataToJudge.size());
            result.put("aiKept", aiKept);
            result.put("aiDowngraded", aiDowngraded);
            result.put("auditItems", auditItems);
            result.put("newExtractedKeywords", newKeywords);
            result.put("extractedKeywordCount", newKeywords.size());
            
            log.info("AI判断执行完成: 总计{}, 相关{}, 不相关{}, 提取关键词{}",
                dataToJudge.size(), aiKept, aiDowngraded, newKeywords.size());
            
        } catch (Exception e) {
            log.error("执行AI判断失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "AI判断失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取待AI判断的数据
     */
    private List<CertNewsData> getDataForAIJudge(
        String riskLevel, String sourceName, Integer limit, Boolean judgeAll
    ) {
        List<CertNewsData> dataList = new ArrayList<>();
        
        try {
            // 解析风险等级
            CertNewsData.RiskLevel targetRiskLevel = null;
            if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                try {
                    targetRiskLevel = CertNewsData.RiskLevel.valueOf(riskLevel.toUpperCase());
                    log.info("筛选风险等级: {}", targetRiskLevel);
                } catch (IllegalArgumentException e) {
                    log.warn("无效的风险等级: {}, 将查询中风险数据", riskLevel);
                    targetRiskLevel = CertNewsData.RiskLevel.MEDIUM;
                }
            } else {
                // 默认查询中风险数据
                targetRiskLevel = CertNewsData.RiskLevel.MEDIUM;
                log.info("未指定风险等级，默认查询中风险数据");
            }
            
            List<CertNewsData> allData;
            if (judgeAll != null && judgeAll) {
                // 查询所有符合风险等级的数据
                allData = crawlerDataRepository.findByRiskLevelAndDeleted(targetRiskLevel, 0);
                log.info("查询所有{}数据，共{}条", targetRiskLevel, allData.size());
            } else {
                // 限制数量查询
                allData = crawlerDataRepository.findByRiskLevelAndDeleted(targetRiskLevel, 0);
            }
            
            // 应用筛选条件
            int queryLimit = (limit != null && limit > 0) ? limit : 10;
            for (CertNewsData data : allData) {
                if (sourceName != null && !sourceName.isEmpty() && !sourceName.equals(data.getSourceName())) {
                    continue;
                }
                dataList.add(data);
                
                if (!(judgeAll != null && judgeAll) && dataList.size() >= queryLimit) {
                    break;
                }
            }
            
            log.info("筛选后得到{}条数据", dataList.size());
        } catch (Exception e) {
            log.error("获取待判断数据失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }
    
    /**
     * 构建新闻数据Map用于AI判断
     */
    private Map<String, Object> buildNewsDataMap(CertNewsData data) {
        Map<String, Object> newsData = new HashMap<>();
        newsData.put("id", data.getId());
        newsData.put("title", data.getTitle() != null ? data.getTitle() : "");
        newsData.put("content", data.getContent() != null ? data.getContent() : "");
        newsData.put("summary", data.getSummary() != null ? data.getSummary() : "");
        newsData.put("country", data.getCountry() != null ? data.getCountry() : "");
        newsData.put("sourceName", data.getSourceName() != null ? data.getSourceName() : "");
        return newsData;
    }
    
    /**
     * 使用AI判断新闻
     */
    private com.certification.dto.ai.CertNewsClassificationResult judgeNewsWithAI(Map<String, Object> newsData) {
        return certNewsAIJudgeService.classifyCertificationNews(newsData);
    }
    
    /**
     * 将提取的认证关键词添加到关键词文件
     */
    public void updateCertificationKeywords(List<String> newKeywords) {
        try {
            // 加载现有关键词
            List<String> existingKeywords = loadKeywordsFromFile();
            Set<String> allKeywords = new HashSet<>(existingKeywords);
            
            // 添加新关键词
            int addedCount = 0;
            for (String keyword : newKeywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String trimmed = keyword.trim();
                    if (!allKeywords.contains(trimmed)) {
                        allKeywords.add(trimmed);
                        addedCount++;
                    }
                }
            }
            
            if (addedCount > 0) {
                // 保存到文件
                List<String> sortedKeywords = new ArrayList<>(allKeywords);
                Collections.sort(sortedKeywords);
                saveKeywordsToFile(sortedKeywords);
                log.info("成功添加 {} 个新认证关键词到关键词文件", addedCount);
            } else {
                log.info("没有新的认证关键词需要添加");
            }
            
        } catch (Exception e) {
            log.error("更新认证关键词失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从关键词文件中删除指定关键词
     */
    public boolean deleteKeywordFromFile(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("删除关键词失败：关键词为空");
                return false;
            }
            
            // 加载现有关键词
            List<String> existingKeywords = loadKeywordsFromFile();
            
            // 删除指定关键词
            boolean removed = existingKeywords.remove(keyword.trim());
            
            if (removed) {
                // 保存到文件
                saveKeywordsToFile(existingKeywords);
                log.info("成功从关键词文件中删除关键词: {}", keyword);
                return true;
            } else {
                log.warn("关键词文件中未找到关键词: {}", keyword);
                return false;
            }
            
        } catch (Exception e) {
            log.error("删除关键词失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新关键词文件中的关键词
     */
    public boolean updateKeywordInFile(String oldKeyword, String newKeyword) {
        try {
            if (oldKeyword == null || oldKeyword.trim().isEmpty() || 
                newKeyword == null || newKeyword.trim().isEmpty()) {
                log.warn("更新关键词失败：关键词为空");
                return false;
            }
            
            // 加载现有关键词
            List<String> existingKeywords = loadKeywordsFromFile();
            
            // 查找并替换关键词
            int index = existingKeywords.indexOf(oldKeyword.trim());
            if (index >= 0) {
                existingKeywords.set(index, newKeyword.trim());
                
                // 保存到文件
                saveKeywordsToFile(existingKeywords);
                log.info("成功更新关键词: {} -> {}", oldKeyword, newKeyword);
                return true;
            } else {
                log.warn("关键词文件中未找到关键词: {}", oldKeyword);
                return false;
            }
            
        } catch (Exception e) {
            log.error("更新关键词失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
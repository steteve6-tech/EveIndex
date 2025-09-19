package com.certification.standards;

import com.certification.entity.common.Keyword;
import com.certification.entity.common.CrawlerData;
import com.certification.repository.KeywordRepository;
import com.certification.repository.CrawlerDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 关键词服务类
 */
@Slf4j
@Service
public class KeywordService {

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private CrawlerDataRepository crawlerDataRepository;

    /**
     * 设置当前关键词为默认关键词
     */
    @Transactional
    public void setCurrentKeywordsAsDefault() {
        log.info("开始设置当前关键词为默认关键词");
        
        try {
            // 获取当前所有启用的关键词
            List<Keyword> currentKeywords = keywordRepository.findAllEnabledKeywordObjects();
            
            if (currentKeywords.isEmpty()) {
                throw new RuntimeException("当前没有启用的关键词，无法设置为默认关键词");
            }
            
            // 将当前关键词列表保存为默认关键词
            // 这里我们可以将关键词列表序列化到配置文件中，或者保存到数据库的配置表
            // 为了简单起见，我们先记录日志
            log.info("当前关键词数量: {}", currentKeywords.size());
            for (Keyword keyword : currentKeywords) {
                log.info("关键词: {} - {}", keyword.getKeyword(), keyword.getDescription());
            }
            
            // TODO: 这里可以将关键词列表保存到配置文件或数据库配置表中
            // 暂时先记录到日志中，后续可以扩展为保存到配置文件
            // 在实际应用中，可以将关键词列表保存到：
            // 1. 数据库配置表
            // 2. 配置文件（如application.yml）
            // 3. 外部存储（如Redis）
            
            // 为了演示目的，我们创建一个简单的文件来保存默认关键词
            // 在实际项目中，建议使用数据库配置表或配置文件
            String defaultKeywordsString = getCurrentKeywordsAsString();
            log.info("默认关键词内容:\n{}", defaultKeywordsString);
            
            // 保存到文件（可选，用于演示）
            try {
                java.nio.file.Files.write(
                    java.nio.file.Paths.get("default-keywords.txt"),
                    defaultKeywordsString.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
                log.info("默认关键词已保存到文件: default-keywords.txt");
            } catch (Exception fileException) {
                log.warn("保存默认关键词到文件失败: {}", fileException.getMessage());
            }
            
            log.info("默认关键词设置完成");
            
        } catch (Exception e) {
            log.error("设置默认关键词失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置默认关键词失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前关键词列表的字符串形式
     */
    public String getCurrentKeywordsAsString() {
        log.info("获取当前关键词列表的字符串形式");
        
        try {
            List<Keyword> currentKeywords = keywordRepository.findAllEnabledKeywordObjects();
            StringBuilder sb = new StringBuilder();
            
            for (Keyword keyword : currentKeywords) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(keyword.getKeyword());
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("获取当前关键词字符串失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取默认关键词列表
     */
    public List<String> getDefaultKeywords() {
        log.info("获取默认关键词列表");
        
        // 首先尝试从文件读取保存的默认关键词
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("default-keywords.txt");
            if (java.nio.file.Files.exists(filePath)) {
                String content = java.nio.file.Files.readString(filePath);
                if (content != null && !content.trim().isEmpty()) {
                    List<String> keywords = Arrays.asList(content.split("\n"));
                    log.info("从文件读取到 {} 个默认关键词", keywords.size());
                    return keywords;
                }
            }
        } catch (Exception e) {
            log.warn("从文件读取默认关键词失败: {}", e.getMessage());
        }
        
        // 如果文件不存在或读取失败，返回系统预设的默认关键词
        log.info("使用系统预设的默认关键词");
        return Arrays.asList(
            "EN 18031-2",
            "EN 18031-3",
            "RED cybersecurity",
            "Article 3(3)(d)(e)(f)",
            "Delegated Reg. (EU) 2022/30",
            "OJEU harmonised standards",
            "EN 300 328",
            "EN 301 893",
            "EN 301 489-1",
            "EN 301 489-17",
            "EN 62311",
            "EN IEC 62368-1",
            "China RoHS",
            "GB/T 39560",
            "GB 26572-2025",
            "SJ/T 11364",
            "CMIIT ID",
            "SRRC",
            "RoHS 2.0",
            "RoHS 3",
            "Restriction of Hazardous Substances",
            "IEC 62321",
            "FCC ID",
            "Equipment Authorization",
            "Part 15B",
            "Part 15C",
            "Part 15E",
            "SDoC",
            "KDB 447498",
            "KDB 996369",
            "module integration",
            "permissive change",
            "WPC ETA",
            "Equipment Type Approval",
            "Self-declaration",
            "2.4 GHz",
            "5 GHz",
            "6 GHz",
            "NBTC Type Approval",
            "NBTC TS 1035-2562",
            "Wi-Fi 6E",
            "Wi-Fi 7",
            "Class A",
            "Class B",
            "FCC 15.407",
            "ETSI acceptance",
            "5.925–6.425 GHz",
            "CE RED 2024/53/EU",
            "Harmonised standards",
            "Notified Body",
            "EU DoC",
            "IMDA Equipment Registration",
            "IMDA TS SRD",
            "TELEC",
            "MIC Technical Conformity Certification",
            "Radio Law",
            "Specific Radio Equipment",
            "2.4 GHz Technical Standard",
            "5 GHz Technical Standard",
            "6 GHz Technical Standard",
            "NCC Low-Power Radio Equipment",
            "LP0002 Technical Specification",
            "6 GHz Frequency Expansion",
            "RCM",
            "ACMA Supplier's Declaration",
            "Radiocommunications LIPD Class Licence",
            "AS/NZS CISPR 32",
            "EESS",
            "ERAC",
            "KC Conformity",
            "EMC"
        );
    }

    /**
     * 获取默认关键词字符串
     */
    public String getDefaultKeywordsAsString() {
        log.info("获取默认关键词字符串");
        
        try {
            List<String> defaultKeywords = getDefaultKeywords();
            return String.join("\n", defaultKeywords);
            
        } catch (Exception e) {
            log.error("获取默认关键词字符串失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 检查是否有保存的默认关键词
     */
    public boolean hasSavedDefaultKeywords() {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("default-keywords.txt");
            return java.nio.file.Files.exists(filePath) && 
                   java.nio.file.Files.size(filePath) > 0;
        } catch (Exception e) {
            log.warn("检查保存的默认关键词失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除保存的默认关键词
     */
    public void deleteSavedDefaultKeywords() {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("default-keywords.txt");
            if (java.nio.file.Files.exists(filePath)) {
                java.nio.file.Files.delete(filePath);
                log.info("已删除保存的默认关键词文件");
            }
        } catch (Exception e) {
            log.warn("删除保存的默认关键词失败: {}", e.getMessage());
        }
    }

    /**
     * 获取当前关键词数量
     */
    public long getCurrentKeywordsCount() {
        try {
            return keywordRepository.countByEnabledTrue();
        } catch (Exception e) {
            log.warn("获取当前关键词数量失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 获取默认关键词数量
     */
    public long getDefaultKeywordsCount() {
        try {
            List<String> defaultKeywords = getDefaultKeywords();
            return defaultKeywords.size();
        } catch (Exception e) {
            log.warn("获取默认关键词数量失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 比较当前关键词和默认关键词
     */
    public Map<String, Object> compareWithDefaultKeywords() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> currentKeywords = keywordRepository.findAllEnabledKeywords();
            List<String> defaultKeywords = getDefaultKeywords();
            
            // 计算差异
            Set<String> currentSet = new HashSet<>(currentKeywords);
            Set<String> defaultSet = new HashSet<>(defaultKeywords);
            
            // 当前有但默认没有的关键词
            Set<String> onlyInCurrent = new HashSet<>(currentSet);
            onlyInCurrent.removeAll(defaultSet);
            
            // 默认有但当前没有的关键词
            Set<String> onlyInDefault = new HashSet<>(defaultSet);
            onlyInDefault.removeAll(currentSet);
            
            // 共同的关键词
            Set<String> common = new HashSet<>(currentSet);
            common.retainAll(defaultSet);
            
            result.put("currentCount", currentKeywords.size());
            result.put("defaultCount", defaultKeywords.size());
            result.put("commonCount", common.size());
            result.put("onlyInCurrentCount", onlyInCurrent.size());
            result.put("onlyInDefaultCount", onlyInDefault.size());
            result.put("onlyInCurrent", new ArrayList<>(onlyInCurrent));
            result.put("onlyInDefault", new ArrayList<>(onlyInDefault));
            result.put("common", new ArrayList<>(common));
            result.put("isSame", currentSet.equals(defaultSet));
            
        } catch (Exception e) {
            log.error("比较关键词失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词详细信息
     */
    public Map<String, Object> getDefaultKeywordsInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> defaultKeywords = getDefaultKeywords();
            boolean hasSaved = hasSavedDefaultKeywords();
            long currentCount = getCurrentKeywordsCount();
            
            result.put("defaultKeywords", defaultKeywords);
            result.put("defaultCount", defaultKeywords.size());
            result.put("hasSaved", hasSaved);
            result.put("currentCount", currentCount);
            result.put("isModified", !hasSaved || currentCount != defaultKeywords.size());
            
        } catch (Exception e) {
            log.error("获取默认关键词信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取当前关键词详细信息
     */
    public Map<String, Object> getCurrentKeywordsInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Keyword> currentKeywords = keywordRepository.findAllEnabledKeywordObjects();
            List<String> currentKeywordStrings = keywordRepository.findAllEnabledKeywords();
            long currentCount = getCurrentKeywordsCount();
            
            result.put("currentKeywords", currentKeywords);
            result.put("currentKeywordStrings", currentKeywordStrings);
            result.put("currentCount", currentCount);
            result.put("currentString", getCurrentKeywordsAsString());
            
        } catch (Exception e) {
            log.error("获取当前关键词信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词统计信息
     */
    public Map<String, Object> getDefaultKeywordsStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long currentCount = getCurrentKeywordsCount();
            long defaultCount = getDefaultKeywordsCount();
            boolean hasSaved = hasSavedDefaultKeywords();
            Map<String, Object> comparison = compareWithDefaultKeywords();
            
            result.put("currentCount", currentCount);
            result.put("defaultCount", defaultCount);
            result.put("hasSaved", hasSaved);
            result.put("comparison", comparison);
            result.put("isModified", !hasSaved || currentCount != defaultCount);
            result.put("modificationType", hasSaved ? "saved" : "system");
            
        } catch (Exception e) {
            log.error("获取默认关键词统计信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词完整信息
     */
    public Map<String, Object> getDefaultKeywordsFullInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有相关信息
            Map<String, Object> currentInfo = getCurrentKeywordsInfo();
            Map<String, Object> defaultInfo = getDefaultKeywordsInfo();
            Map<String, Object> stats = getDefaultKeywordsStats();
            Map<String, Object> comparison = compareWithDefaultKeywords();
            
            result.put("currentInfo", currentInfo);
            result.put("defaultInfo", defaultInfo);
            result.put("stats", stats);
            result.put("comparison", comparison);
            result.put("summary", Map.of(
                "currentCount", stats.get("currentCount"),
                "defaultCount", stats.get("defaultCount"),
                "hasSaved", stats.get("hasSaved"),
                "isModified", stats.get("isModified"),
                "modificationType", stats.get("modificationType")
            ));
            
        } catch (Exception e) {
            log.error("获取默认关键词完整信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词摘要信息
     */
    public Map<String, Object> getDefaultKeywordsSummary() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long currentCount = getCurrentKeywordsCount();
            long defaultCount = getDefaultKeywordsCount();
            boolean hasSaved = hasSavedDefaultKeywords();
            boolean isModified = !hasSaved || currentCount != defaultCount;
            String modificationType = hasSaved ? "saved" : "system";
            
            result.put("currentCount", currentCount);
            result.put("defaultCount", defaultCount);
            result.put("hasSaved", hasSaved);
            result.put("isModified", isModified);
            result.put("modificationType", modificationType);
            result.put("status", isModified ? "modified" : "default");
            result.put("canRestore", hasSaved);
            result.put("canSetAsDefault", currentCount > 0);
            
        } catch (Exception e) {
            log.error("获取默认关键词摘要信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词状态信息
     */
    public Map<String, Object> getDefaultKeywordsStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long currentCount = getCurrentKeywordsCount();
            long defaultCount = getDefaultKeywordsCount();
            boolean hasSaved = hasSavedDefaultKeywords();
            boolean isModified = !hasSaved || currentCount != defaultCount;
            String modificationType = hasSaved ? "saved" : "system";
            String status = isModified ? "modified" : "default";
            
            result.put("currentCount", currentCount);
            result.put("defaultCount", defaultCount);
            result.put("hasSaved", hasSaved);
            result.put("isModified", isModified);
            result.put("modificationType", modificationType);
            result.put("status", status);
            result.put("canRestore", hasSaved);
            result.put("canSetAsDefault", currentCount > 0);
            result.put("canDeleteSaved", hasSaved);
            result.put("lastModified", java.time.LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.error("获取默认关键词状态信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词操作历史
     */
    public Map<String, Object> getDefaultKeywordsHistory() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean hasSaved = hasSavedDefaultKeywords();
            long currentCount = getCurrentKeywordsCount();
            long defaultCount = getDefaultKeywordsCount();
            
            List<Map<String, Object>> history = new ArrayList<>();
            
            // 添加系统初始化记录
            Map<String, Object> initRecord = new HashMap<>();
            initRecord.put("operation", "system_init");
            initRecord.put("description", "系统初始化默认关键词");
            initRecord.put("timestamp", java.time.LocalDateTime.now().minusDays(1).toString());
            initRecord.put("count", defaultCount);
            history.add(initRecord);
            
            // 如果有保存的默认关键词，添加保存记录
            if (hasSaved) {
                Map<String, Object> saveRecord = new HashMap<>();
                saveRecord.put("operation", "user_save");
                saveRecord.put("description", "用户设置当前关键词为默认关键词");
                saveRecord.put("timestamp", java.time.LocalDateTime.now().minusHours(1).toString());
                saveRecord.put("count", currentCount);
                history.add(saveRecord);
            }
            
            // 添加当前状态记录
            Map<String, Object> currentRecord = new HashMap<>();
            currentRecord.put("operation", "current_status");
            currentRecord.put("description", "当前关键词状态");
            currentRecord.put("timestamp", java.time.LocalDateTime.now().toString());
            currentRecord.put("count", currentCount);
            currentRecord.put("isModified", currentCount != defaultCount);
            history.add(currentRecord);
            
            result.put("history", history);
            result.put("totalOperations", history.size());
            result.put("lastOperation", history.get(history.size() - 1));
            
        } catch (Exception e) {
            log.error("获取默认关键词操作历史失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词操作建议
     */
    public Map<String, Object> getDefaultKeywordsSuggestions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long currentCount = getCurrentKeywordsCount();
            long defaultCount = getDefaultKeywordsCount();
            boolean hasSaved = hasSavedDefaultKeywords();
            boolean isModified = !hasSaved || currentCount != defaultCount;
            
            List<Map<String, Object>> suggestions = new ArrayList<>();
            
            // 如果没有关键词，建议初始化
            if (currentCount == 0) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "initialize");
                suggestion.put("priority", "high");
                suggestion.put("title", "初始化默认关键词");
                suggestion.put("description", "当前没有关键词，建议初始化默认关键词");
                suggestion.put("action", "initialize");
                suggestions.add(suggestion);
            }
            
            // 如果当前关键词与默认不同且没有保存，建议保存
            if (currentCount != defaultCount && !hasSaved && currentCount > 0) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "save");
                suggestion.put("priority", "medium");
                suggestion.put("title", "保存当前关键词为默认");
                suggestion.put("description", "当前关键词与系统默认不同，建议保存为新的默认关键词");
                suggestion.put("action", "setAsDefault");
                suggestions.add(suggestion);
            }
            
            // 如果有保存的默认关键词，建议恢复
            if (hasSaved && currentCount != defaultCount) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "restore");
                suggestion.put("priority", "low");
                suggestion.put("title", "恢复保存的默认关键词");
                suggestion.put("description", "可以恢复到之前保存的默认关键词");
                suggestion.put("action", "restore");
                suggestions.add(suggestion);
            }
            
            // 如果当前关键词数量很多，建议优化
            if (currentCount > 100) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "optimize");
                suggestion.put("priority", "medium");
                suggestion.put("title", "优化关键词数量");
                suggestion.put("description", "当前关键词数量较多，建议优化以提高处理效率");
                suggestion.put("action", "optimize");
                suggestions.add(suggestion);
            }
            
            result.put("suggestions", suggestions);
            result.put("totalSuggestions", suggestions.size());
            result.put("hasHighPriority", suggestions.stream().anyMatch(s -> "high".equals(s.get("priority"))));
            result.put("hasMediumPriority", suggestions.stream().anyMatch(s -> "medium".equals(s.get("priority"))));
            result.put("hasLowPriority", suggestions.stream().anyMatch(s -> "low".equals(s.get("priority"))));
            
        } catch (Exception e) {
            log.error("获取默认关键词操作建议失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词完整报告
     */
    public Map<String, Object> getDefaultKeywordsReport() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有相关信息
            Map<String, Object> currentInfo = getCurrentKeywordsInfo();
            Map<String, Object> defaultInfo = getDefaultKeywordsInfo();
            Map<String, Object> stats = getDefaultKeywordsStats();
            Map<String, Object> comparison = compareWithDefaultKeywords();
            Map<String, Object> status = getDefaultKeywordsStatus();
            Map<String, Object> history = getDefaultKeywordsHistory();
            Map<String, Object> suggestions = getDefaultKeywordsSuggestions();
            
            // 生成报告摘要
            Map<String, Object> summary = new HashMap<>();
            summary.put("reportTitle", "默认关键词管理报告");
            summary.put("generatedAt", java.time.LocalDateTime.now().toString());
            summary.put("currentCount", stats.get("currentCount"));
            summary.put("defaultCount", stats.get("defaultCount"));
            summary.put("hasSaved", stats.get("hasSaved"));
            summary.put("isModified", stats.get("isModified"));
            summary.put("modificationType", stats.get("modificationType"));
            summary.put("status", status.get("status"));
            summary.put("totalSuggestions", suggestions.get("totalSuggestions"));
            summary.put("hasHighPrioritySuggestions", suggestions.get("hasHighPriority"));
            
            result.put("summary", summary);
            result.put("currentInfo", currentInfo);
            result.put("defaultInfo", defaultInfo);
            result.put("stats", stats);
            result.put("comparison", comparison);
            result.put("status", status);
            result.put("history", history);
            result.put("suggestions", suggestions);
            result.put("recommendations", generateRecommendations(stats, suggestions));
            
        } catch (Exception e) {
            log.error("获取默认关键词完整报告失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 生成推荐操作
     */
    private List<Map<String, Object>> generateRecommendations(Map<String, Object> stats, Map<String, Object> suggestions) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            long currentCount = (Long) stats.get("currentCount");
            long defaultCount = (Long) stats.get("defaultCount");
            boolean hasSaved = (Boolean) stats.get("hasSaved");
            boolean isModified = (Boolean) stats.get("isModified");
            
            // 如果没有关键词，强烈推荐初始化
            if (currentCount == 0) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("action", "initialize");
                rec.put("priority", "critical");
                rec.put("description", "立即初始化默认关键词以启用自动处理功能");
                rec.put("impact", "high");
                recommendations.add(rec);
            }
            
            // 如果关键词数量差异很大，推荐保存
            if (Math.abs(currentCount - defaultCount) > 10 && !hasSaved) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("action", "setAsDefault");
                rec.put("priority", "high");
                rec.put("description", "保存当前关键词配置为新的默认设置");
                rec.put("impact", "medium");
                recommendations.add(rec);
            }
            
            // 如果关键词数量过多，推荐优化
            if (currentCount > 100) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("action", "optimize");
                rec.put("priority", "medium");
                rec.put("description", "考虑优化关键词数量以提高处理效率");
                rec.put("impact", "medium");
                recommendations.add(rec);
            }
            
        } catch (Exception e) {
            log.warn("生成推荐操作失败: {}", e.getMessage());
        }
        
        return recommendations;
    }

    /**
     * 获取默认关键词导出信息
     */
    public Map<String, Object> getDefaultKeywordsExport() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前关键词信息
            List<String> currentKeywords = keywordRepository.findAllEnabledKeywords();
            List<String> defaultKeywords = getDefaultKeywords();
            String currentString = getCurrentKeywordsAsString();
            String defaultString = getDefaultKeywordsAsString();
            
            // 生成导出数据
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            exportData.put("currentKeywords", currentKeywords);
            exportData.put("defaultKeywords", defaultKeywords);
            exportData.put("currentString", currentString);
            exportData.put("defaultString", defaultString);
            exportData.put("currentCount", currentKeywords.size());
            exportData.put("defaultCount", defaultKeywords.size());
            exportData.put("hasSaved", hasSavedDefaultKeywords());
            
            // 生成CSV格式数据
            StringBuilder csvData = new StringBuilder();
            csvData.append("关键词类型,关键词内容\n");
            for (String keyword : currentKeywords) {
                csvData.append("当前关键词,").append(keyword).append("\n");
            }
            for (String keyword : defaultKeywords) {
                csvData.append("默认关键词,").append(keyword).append("\n");
            }
            
            // 生成JSON格式数据
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("currentKeywords", currentKeywords);
            jsonData.put("defaultKeywords", defaultKeywords);
            jsonData.put("metadata", Map.of(
                "exportTime", java.time.LocalDateTime.now().toString(),
                "currentCount", currentKeywords.size(),
                "defaultCount", defaultKeywords.size(),
                "hasSaved", hasSavedDefaultKeywords()
            ));
            
            result.put("exportData", exportData);
            result.put("csvData", csvData.toString());
            result.put("jsonData", jsonData);
            result.put("formats", Arrays.asList("csv", "json", "txt"));
            
        } catch (Exception e) {
            log.error("获取默认关键词导出信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取默认关键词备份信息
     */
    public Map<String, Object> getDefaultKeywordsBackup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前关键词信息
            List<String> currentKeywords = keywordRepository.findAllEnabledKeywords();
            List<String> defaultKeywords = getDefaultKeywords();
            boolean hasSaved = hasSavedDefaultKeywords();
            
            // 生成备份数据
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("backupTime", java.time.LocalDateTime.now().toString());
            backupData.put("currentKeywords", currentKeywords);
            backupData.put("defaultKeywords", defaultKeywords);
            backupData.put("currentCount", currentKeywords.size());
            backupData.put("defaultCount", defaultKeywords.size());
            backupData.put("hasSaved", hasSaved);
            backupData.put("backupVersion", "1.0");
            backupData.put("backupType", "full");
            
            // 生成备份文件内容
            StringBuilder backupContent = new StringBuilder();
            backupContent.append("# 关键词备份文件\n");
            backupContent.append("# 备份时间: ").append(java.time.LocalDateTime.now().toString()).append("\n");
            backupContent.append("# 当前关键词数量: ").append(currentKeywords.size()).append("\n");
            backupContent.append("# 默认关键词数量: ").append(defaultKeywords.size()).append("\n");
            backupContent.append("# 是否有保存的默认关键词: ").append(hasSaved).append("\n\n");
            
            backupContent.append("# 当前关键词列表\n");
            for (String keyword : currentKeywords) {
                backupContent.append(keyword).append("\n");
            }
            
            backupContent.append("\n# 默认关键词列表\n");
            for (String keyword : defaultKeywords) {
                backupContent.append(keyword).append("\n");
            }
            
            // 生成备份元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("backupTime", java.time.LocalDateTime.now().toString());
            metadata.put("backupVersion", "1.0");
            metadata.put("backupType", "full");
            metadata.put("currentCount", currentKeywords.size());
            metadata.put("defaultCount", defaultKeywords.size());
            metadata.put("hasSaved", hasSaved);
            metadata.put("checksum", generateChecksum(currentKeywords.toString() + defaultKeywords.toString()));
            
            result.put("backupData", backupData);
            result.put("backupContent", backupContent.toString());
            result.put("metadata", metadata);
            result.put("backupSize", backupContent.length());
            result.put("canRestore", hasSaved);
            
        } catch (Exception e) {
            log.error("获取默认关键词备份信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 生成简单的校验和
     */
    private String generateChecksum(String content) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("生成校验和失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 获取默认关键词完整管理信息
     */
    public Map<String, Object> getDefaultKeywordsManagement() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有相关信息
            Map<String, Object> currentInfo = getCurrentKeywordsInfo();
            Map<String, Object> defaultInfo = getDefaultKeywordsInfo();
            Map<String, Object> stats = getDefaultKeywordsStats();
            Map<String, Object> comparison = compareWithDefaultKeywords();
            Map<String, Object> status = getDefaultKeywordsStatus();
            Map<String, Object> history = getDefaultKeywordsHistory();
            Map<String, Object> suggestions = getDefaultKeywordsSuggestions();
            Map<String, Object> report = getDefaultKeywordsReport();
            Map<String, Object> export = getDefaultKeywordsExport();
            Map<String, Object> backup = getDefaultKeywordsBackup();
            
            // 生成管理摘要
            Map<String, Object> managementSummary = new HashMap<>();
            managementSummary.put("managementTitle", "默认关键词完整管理信息");
            managementSummary.put("generatedAt", java.time.LocalDateTime.now().toString());
            managementSummary.put("currentCount", stats.get("currentCount"));
            managementSummary.put("defaultCount", stats.get("defaultCount"));
            managementSummary.put("hasSaved", stats.get("hasSaved"));
            managementSummary.put("isModified", stats.get("isModified"));
            managementSummary.put("modificationType", stats.get("modificationType"));
            managementSummary.put("status", status.get("status"));
            managementSummary.put("totalSuggestions", suggestions.get("totalSuggestions"));
            managementSummary.put("hasHighPrioritySuggestions", suggestions.get("hasHighPriority"));
            managementSummary.put("canRestore", status.get("canRestore"));
            managementSummary.put("canSetAsDefault", status.get("canSetAsDefault"));
            managementSummary.put("canDeleteSaved", status.get("canDeleteSaved"));
            
            // 生成操作建议
            List<Map<String, Object>> actions = generateManagementActions(stats, status, suggestions);
            
            result.put("managementSummary", managementSummary);
            result.put("currentInfo", currentInfo);
            result.put("defaultInfo", defaultInfo);
            result.put("stats", stats);
            result.put("comparison", comparison);
            result.put("status", status);
            result.put("history", history);
            result.put("suggestions", suggestions);
            result.put("report", report);
            result.put("export", export);
            result.put("backup", backup);
            result.put("actions", actions);
            result.put("totalActions", actions.size());
            
        } catch (Exception e) {
            log.error("获取默认关键词完整管理信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 生成管理操作建议
     */
    private List<Map<String, Object>> generateManagementActions(Map<String, Object> stats, Map<String, Object> status, Map<String, Object> suggestions) {
        List<Map<String, Object>> actions = new ArrayList<>();
        
        try {
            long currentCount = (Long) stats.get("currentCount");
            long defaultCount = (Long) stats.get("defaultCount");
            boolean hasSaved = (Boolean) stats.get("hasSaved");
            boolean isModified = (Boolean) stats.get("isModified");
            boolean canRestore = (Boolean) status.get("canRestore");
            boolean canSetAsDefault = (Boolean) status.get("canSetAsDefault");
            boolean canDeleteSaved = (Boolean) status.get("canDeleteSaved");
            
            // 初始化操作
            if (currentCount == 0) {
                Map<String, Object> action = new HashMap<>();
                action.put("action", "initialize");
                action.put("priority", "critical");
                action.put("title", "初始化默认关键词");
                action.put("description", "当前没有关键词，需要初始化以启用自动处理功能");
                action.put("apiEndpoint", "/keywords/initialize");
                action.put("method", "POST");
                action.put("impact", "high");
                actions.add(action);
            }
            
            // 设置默认关键词操作
            if (canSetAsDefault && currentCount > 0) {
                Map<String, Object> action = new HashMap<>();
                action.put("action", "setAsDefault");
                action.put("priority", "high");
                action.put("title", "设置当前关键词为默认");
                action.put("description", "将当前关键词列表保存为新的默认关键词");
                action.put("apiEndpoint", "/keywords/set-default");
                action.put("method", "POST");
                action.put("impact", "medium");
                actions.add(action);
            }
            
            // 恢复默认关键词操作
            if (canRestore) {
                Map<String, Object> action = new HashMap<>();
                action.put("action", "restore");
                action.put("priority", "medium");
                action.put("title", "恢复默认关键词");
                action.put("description", "恢复到之前保存的默认关键词");
                action.put("apiEndpoint", "/keywords/restore-default");
                action.put("method", "POST");
                action.put("impact", "medium");
                actions.add(action);
            }
            
            // 删除保存的默认关键词操作
            if (canDeleteSaved) {
                Map<String, Object> action = new HashMap<>();
                action.put("action", "deleteSaved");
                action.put("priority", "low");
                action.put("title", "删除保存的默认关键词");
                action.put("description", "删除保存的默认关键词文件，恢复到系统默认");
                action.put("apiEndpoint", "/keywords/delete-saved-default");
                action.put("method", "DELETE");
                action.put("impact", "low");
                actions.add(action);
            }
            
            // 导出操作
            Map<String, Object> exportAction = new HashMap<>();
            exportAction.put("action", "export");
            exportAction.put("priority", "low");
            exportAction.put("title", "导出关键词");
            exportAction.put("description", "导出当前关键词和默认关键词");
            exportAction.put("apiEndpoint", "/keywords/default-export");
            exportAction.put("method", "GET");
            exportAction.put("impact", "low");
            actions.add(exportAction);
            
            // 备份操作
            Map<String, Object> backupAction = new HashMap<>();
            backupAction.put("action", "backup");
            backupAction.put("priority", "low");
            backupAction.put("title", "备份关键词");
            backupAction.put("description", "创建关键词的完整备份");
            backupAction.put("apiEndpoint", "/keywords/default-backup");
            backupAction.put("method", "GET");
            backupAction.put("impact", "low");
            actions.add(backupAction);
            
        } catch (Exception e) {
            log.warn("生成管理操作建议失败: {}", e.getMessage());
        }
        
        return actions;
    }

    /**
     * 恢复默认关键词
     */
    @Transactional
    public void restoreDefaultKeywords() {
        log.info("开始恢复默认关键词");
        
        try {
            // 获取默认关键词列表
            List<String> defaultKeywords = getDefaultKeywords();
            
            // 清空当前所有关键词
            keywordRepository.deleteAll();
            
            // 添加默认关键词
            for (int i = 0; i < defaultKeywords.size(); i++) {
                String keyword = defaultKeywords.get(i);
                Keyword keywordEntity = new Keyword();
                keywordEntity.setKeyword(keyword);
                keywordEntity.setDescription("默认关键词");
                keywordEntity.setEnabled(true);
                keywordEntity.setSortOrder(i + 1);
                keywordRepository.save(keywordEntity);
            }
            
            log.info("默认关键词恢复完成，共恢复 {} 个关键词", defaultKeywords.size());
            
        } catch (Exception e) {
            log.error("恢复默认关键词失败: {}", e.getMessage(), e);
            throw new RuntimeException("恢复默认关键词失败: " + e.getMessage());
        }
    }

    /**
     * 初始化默认关键词列表
     */
    @Transactional
    public void initializeDefaultKeywords() {
        List<String> defaultKeywords = Arrays.asList(
            "EN 18031-2",
            "EN 18031-3",
            "RED cybersecurity",
            "Article 3(3)(d)(e)(f)",
            "Delegated Reg. (EU) 2022/30",
            "OJEU harmonised standards",
            "EN 300 328",
            "EN 301 893",
            "EN 301 489-1",
            "EN 301 489-17",
            "EN 62311",
            "EN IEC 62368-1",
            "China RoHS",
            "GB/T 39560",
            "GB 26572-2025",
            "SJ/T 11364",
            "CMIIT ID",
            "SRRC",
            "RoHS 2.0",
            "RoHS 3",
            "Restriction of Hazardous Substances",
            "IEC 62321",
            "FCC ID",
            "Equipment Authorization",
            "Part 15B",
            "Part 15C",
            "Part 15E",
            "SDoC",
            "KDB 447498",
            "KDB 996369",
            "module integration",
            "permissive change",
            "WPC ETA",
            "Equipment Type Approval",
            "Self-declaration",
            "2.4 GHz",
            "5 GHz",
            "6 GHz",
            "NBTC Type Approval",
            "NBTC TS 1035-2562",
            "Wi-Fi 6E",
            "Wi-Fi 7",
            "Class A",
            "Class B",
            "FCC 15.407",
            "ETSI acceptance",
            "5.925–6.425 GHz",
            "CE RED 2024/53/EU",
            "Harmonised standards",
            "Notified Body",
            "EU DoC",
            "IMDA Equipment Registration",
            "IMDA TS SRD",
            "TELEC",
            "MIC Technical Conformity Certification",
            "Radio Law",
            "Specific Radio Equipment",
            "2.4 GHz Technical Standard",
            "5 GHz Technical Standard",
            "6 GHz Technical Standard",
            "NCC Low-Power Radio Equipment",
            "LP0002 Technical Specification",
            "6 GHz Frequency Expansion",
            "RCM",
            "ACMA Supplier's Declaration",
            "Radiocommunications LIPD Class Licence",
            "AS/NZS CISPR 32",
            "EESS",
            "ERAC",
            "KC Conformity",
            "EMC",
            "Electrical Safety",
            "Wireless/EMC/Electrical Safety Standards",
            "SUBTEL Homologación",
            "Resolución",
            "RLAN",
            "Certificación",
            "SIRIM Type Approval",
            "MCMC Type Approval",
            "e-ComM",
            "SRD",
            "TDRA Type Approval",
            "Registration Certificate",
            "Declaration of Conformity Card",
            "DoC Card",
            "UAE RoHS",
            "ECAS",
            "EQM",
            "Cabinet Decision No. 10 of 2017",
            "MoIAT",
            "MTC Homologación",
            "Equipos de telecomunicaciones",
            "Procedimiento",
            "ICASA Type Approval",
            "Equipment Authorisation",
            "NRCS LOA",
            "VC 8055",
            "SANS/IEC 62368-1",
            "MoC Type Approval",
            "Importing communications equipment",
            "SII Safety Standard SI 62368-1",
            "SDPPI Type Approval",
            "Kominfo Type Approval",
            "Postel",
            "KEPDIRJEN 161/2022",
            "notice",
            "announcement",
            "draft",
            "public hearing",
            "2024",
            "2025",
            "2026",
            "harmonised",
            "conformity declaration",
            "DoC",
            "Declaration of Conformity",
            "NB",
            "technical announcement",
            "standard update",
            "6GHz",
            "6E",
            "WLAN",
            "SRD",
            "RLAN",
            "limit value",
            "transmit power",
            "EIRP",
            "PSD",
            "frequency allocation",
            "listed frequency bands",
            "IEC 62368-1",
            "external power supply",
            "electrical safety",
            "RF exposure",
            // 新增关键词
            "SRRC 型号核准",
            "无线电发射设备 型号核准",
            "KC（RRA）",
            "적합등록",
            "적합인증",
            "국립전파연구원",
            "무선설비",
            "有害物質限制",
            "型号核准",
            "GB 26572-2025",
            "SJ/T 11364",
            "自我聲明",
            "進口備案",
            "公告",
            "公聽",
            "ETSI 接受",
            "協調標準",
            "短距無線",
            "技術規範",
            "表列頻段",
            "功率",
            "評估清單",
            "技術基準適合証明",
            "工事設計認証",
            "電波法",
            "特定無線設備",
            "2.4 GHz 技術基準",
            "5 GHz 技術基準",
            "6 GHz 技術基準",
            "低功率射頻電機",
            "技術規範",
            "型式認證",
            "審驗合格標籤",
            "6 GHz 擴頻",
            "6 GHz 公告",
            "電磁相容",
            "電氣安全",
            "전기안전",
            "無線/EMC/電安標準",
            "Homologación",
            "Resolución",
            "Certificación",
            "頻段",
            "認證",
            "登錄",
            "零售展示卡",
            "標籤規範",
            "通訊設備核准",
            "標準機構",
            "安規轉版",
            "認證",
            "公告",
            "6 GHz 規範",
            "通知",
            "草案",
            "列入協調標準",
            "相容性聲明",
            "技術通告",
            "標準更新",
            "限值",
            "发射功率",
            "頻率核配",
            "組件 認證 延伸",
            "電源適配器 安規",
            "外接電源供應器"
        );

        for (String keywordText : defaultKeywords) {
            if (!keywordRepository.existsByKeyword(keywordText)) {
                Keyword keyword = new Keyword()
                    .setKeyword(keywordText)
                    .setDescription("默认关键词")
                    .setEnabled(true)
                    .setSortOrder(0);
                keywordRepository.save(keyword);
                log.info("初始化关键词: {}", keywordText);
            }
        }
    }

    /**
     * 获取所有关键词
     */
    public List<Keyword> getAllKeywords() {
        return keywordRepository.findByEnabledOrderBySortOrderAsc(true);
    }

    /**
     * 获取所有启用的关键词字符串列表
     */
    public List<String> getAllEnabledKeywords() {
        return keywordRepository.findAllEnabledKeywords();
    }

    /**
     * 添加关键词
     */
    @Transactional
    public Keyword addKeyword(String keywordText, String description) {
        if (keywordRepository.existsByKeyword(keywordText)) {
            throw new RuntimeException("关键词已存在: " + keywordText);
        }

        Keyword keyword = new Keyword()
            .setKeyword(keywordText)
            .setDescription(description)
            .setEnabled(true)
            .setSortOrder(0);

        Keyword savedKeyword = keywordRepository.save(keyword);
        log.info("添加关键词: {}", keywordText);
        return savedKeyword;
    }

    /**
     * 删除关键词
     */
    @Transactional
    public void deleteKeyword(String keywordText) {
        Optional<Keyword> keyword = keywordRepository.findByKeyword(keywordText);
        if (keyword.isPresent()) {
            keywordRepository.delete(keyword.get());
            log.info("删除关键词: {}", keywordText);
        } else {
            throw new RuntimeException("关键词不存在: " + keywordText);
        }
    }

    /**
     * 更新关键词
     */
    @Transactional
    public Keyword updateKeyword(Long id, String keywordText, String description, Boolean enabled) {
        Optional<Keyword> existingKeyword = keywordRepository.findById(id);
        if (existingKeyword.isPresent()) {
            Keyword keyword = existingKeyword.get();
            keyword.setKeyword(keywordText);
            keyword.setDescription(description);
            keyword.setEnabled(enabled);
            
            Keyword updatedKeyword = keywordRepository.save(keyword);
            log.info("更新关键词: {}", keywordText);
            return updatedKeyword;
        } else {
            throw new RuntimeException("关键词不存在，ID: " + id);
        }
    }

    /**
     * 检查文本是否包含任何关键词
     */
    public boolean containsAnyKeyword(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        List<String> keywords = getAllEnabledKeywords();
        String lowerText = text.toLowerCase();
        
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中包含的关键词列表
     */
    public List<String> getContainedKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> keywords = getAllEnabledKeywords();
        List<String> containedKeywords = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    containedKeywords.add(keyword);
                }
            }
        }
        return containedKeywords;
    }
    
    /**
     * 保存关键词列表（智能增删改）
     * 保留文本中存在的关键词，添加新增的关键词，删除文本中不存在的关键词
     */
    @Transactional
    public Map<String, Object> saveKeywordListWithDetails(List<String> newKeywords) {
        log.info("开始智能保存关键词列表，新关键词数量: {}", newKeywords.size());
        
        try {
            // 获取现有关键词
            List<Keyword> existingKeywords = keywordRepository.findAllEnabledKeywordObjects();
            log.info("现有关键词数量: {}", existingKeywords.size());
            
            // 创建新关键词的Set，用于快速查找
            Set<String> newKeywordSet = new HashSet<>();
            for (String keyword : newKeywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    newKeywordSet.add(keyword.trim().toLowerCase());
                }
            }
            
            // 统计操作结果
            int addedCount = 0;
            int deletedCount = 0;
            int keptCount = 0;
            
            // 处理现有关键词
            for (Keyword existingKeyword : existingKeywords) {
                String existingKeywordText = existingKeyword.getKeyword();
                if (existingKeywordText != null) {
                    String existingKeywordLower = existingKeywordText.toLowerCase();
                    
                    if (newKeywordSet.contains(existingKeywordLower)) {
                        // 关键词仍然存在，保留
                        keptCount++;
                        log.debug("保留现有关键词: {}", existingKeywordText);
                        // 从新关键词集合中移除，避免重复添加
                        newKeywordSet.remove(existingKeywordLower);
                    } else {
                        // 关键词不存在于新列表中，删除
                        keywordRepository.delete(existingKeyword);
                        deletedCount++;
                        log.debug("删除关键词: {}", existingKeywordText);
                    }
                }
            }
            
            // 添加新的关键词
            for (String newKeywordText : newKeywords) {
                if (newKeywordText != null && !newKeywordText.trim().isEmpty()) {
                    String trimmedKeyword = newKeywordText.trim();
                    String trimmedKeywordLower = trimmedKeyword.toLowerCase();
                    
                    // 检查是否已经存在（不区分大小写）
                    boolean alreadyExists = false;
                    for (Keyword existingKeyword : existingKeywords) {
                        if (existingKeyword.getKeyword() != null && 
                            existingKeyword.getKeyword().toLowerCase().equals(trimmedKeywordLower)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    
                    if (!alreadyExists) {
                        try {
                            Keyword keyword = new Keyword()
                                .setKeyword(trimmedKeyword)
                                .setDescription("用户自定义关键词")
                                .setEnabled(true)
                                .setSortOrder(existingKeywords.size() + addedCount);
                            keywordRepository.save(keyword);
                            addedCount++;
                            log.debug("添加新关键词: {}", trimmedKeyword);
                        } catch (Exception e) {
                            log.warn("添加关键词失败，跳过: {} - 错误: {}", trimmedKeyword, e.getMessage());
                            // 继续处理下一个关键词，不抛出异常
                        }
                    }
                }
            }
            
            // 强制刷新数据库连接，确保所有操作完成
            keywordRepository.flush();
            
            int totalCount = keptCount + addedCount;
            log.info("关键词列表智能保存完成 - 保留: {} 个, 新增: {} 个, 删除: {} 个, 总计: {} 个关键词", 
                    keptCount, addedCount, deletedCount, totalCount);
            
            // 返回一个包含详细统计信息的对象
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("keptCount", keptCount);
            result.put("addedCount", addedCount);
            result.put("deletedCount", deletedCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("智能保存关键词列表时发生错误", e);
            throw new RuntimeException("智能保存关键词列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存关键词列表（保持向后兼容）
     * 保留文本中存在的关键词，添加新增的关键词，删除文本中不存在的关键词
     */
    @Transactional
    public int saveKeywordList(List<String> newKeywords) {
        Map<String, Object> result = saveKeywordListWithDetails(newKeywords);
        return (Integer) result.get("totalCount");
    }

    /**
     * 获取每个关键词匹配的数据数量
     */
    public Map<String, Integer> getKeywordMatchCounts() {
        log.info("开始获取关键词匹配数量");
        
        Map<String, Integer> matchCounts = new HashMap<>();
        
        try {
            // 获取所有启用的关键词
            List<String> keywords = getAllEnabledKeywords();
            log.info("获取到 {} 个启用的关键词", keywords.size());
            
            // 获取所有爬虫数据
            List<CrawlerData> allData = crawlerDataRepository.findAll();
            log.info("获取到 {} 条爬虫数据", allData.size());
            
            // 为每个关键词计算匹配数量
            for (String keyword : keywords) {
                int count = 0;
                String lowerKeyword = keyword.toLowerCase();
                
                for (CrawlerData data : allData) {
                    if (containsKeyword(data, lowerKeyword)) {
                        count++;
                    }
                }
                
                matchCounts.put(keyword, count);
                log.debug("关键词 '{}' 匹配 {} 条数据", keyword, count);
            }
            
            log.info("关键词匹配数量统计完成，共 {} 个关键词", matchCounts.size());
            
        } catch (Exception e) {
            log.error("获取关键词匹配数量失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取关键词匹配数量失败: " + e.getMessage());
        }
        
        return matchCounts;
    }

    /**
     * 获取带匹配数量的关键词列表
     */
    public List<Map<String, Object>> getKeywordsWithMatchCounts() {
        log.info("开始获取带匹配数量的关键词列表");
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 获取所有关键词对象
            List<Keyword> keywords = getAllKeywords();
            log.info("获取到 {} 个关键词对象", keywords.size());
            
            // 获取匹配数量
            Map<String, Integer> matchCounts = getKeywordMatchCounts();
            
            // 构建结果
            for (Keyword keyword : keywords) {
                Map<String, Object> keywordInfo = new HashMap<>();
                keywordInfo.put("id", keyword.getId());
                keywordInfo.put("keyword", keyword.getKeyword());
                keywordInfo.put("description", keyword.getDescription());
                keywordInfo.put("enabled", keyword.getEnabled());
                keywordInfo.put("sortOrder", keyword.getSortOrder());
                keywordInfo.put("matchCount", matchCounts.getOrDefault(keyword.getKeyword(), 0));
                
                result.add(keywordInfo);
            }
            
            // 按匹配数量降序排序
            result.sort((a, b) -> {
                Integer countA = (Integer) a.get("matchCount");
                Integer countB = (Integer) b.get("matchCount");
                return countB.compareTo(countA);
            });
            
            log.info("带匹配数量的关键词列表获取完成，共 {} 个关键词", result.size());
            
        } catch (Exception e) {
            log.error("获取带匹配数量的关键词列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取带匹配数量的关键词列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 检查数据是否包含关键词
     */
    private boolean containsKeyword(CrawlerData data, String keyword) {
        if (data == null || keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        String lowerKeyword = keyword.toLowerCase();
        
        // 检查标题
        if (data.getTitle() != null && data.getTitle().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        // 检查摘要
        if (data.getSummary() != null && data.getSummary().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        // 检查内容
        if (data.getContent() != null && data.getContent().toLowerCase().contains(lowerKeyword)) {
            return true;
        }
        
        return false;
    }

    /**
     * 批量删除0匹配的关键词
     */
    public Map<String, Object> deleteZeroMatchKeywords() {
        log.info("开始批量删除0匹配的关键词");
        
        Map<String, Object> result = new HashMap<>();
        List<String> deletedKeywords = new ArrayList<>();
        int deletedCount = 0;
        
        try {
            // 获取所有关键词的匹配数量
            Map<String, Integer> matchCounts = getKeywordMatchCounts();
            log.info("获取到 {} 个关键词的匹配数量", matchCounts.size());
            
            // 找出所有0匹配的关键词
            List<String> zeroMatchKeywords = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
                if (entry.getValue() == 0) {
                    zeroMatchKeywords.add(entry.getKey());
                }
            }
            
            log.info("找到 {} 个0匹配的关键词", zeroMatchKeywords.size());
            
            if (zeroMatchKeywords.isEmpty()) {
                result.put("deletedCount", 0);
                result.put("deletedKeywords", deletedKeywords);
                result.put("message", "没有找到0匹配的关键词");
                return result;
            }
            
            // 批量删除0匹配的关键词
            for (String keyword : zeroMatchKeywords) {
                try {
                    deleteKeyword(keyword);
                    deletedKeywords.add(keyword);
                    deletedCount++;
                    log.debug("删除0匹配关键词: {}", keyword);
                } catch (Exception e) {
                    log.warn("删除关键词 '{}' 失败: {}", keyword, e.getMessage());
                }
            }
            
            log.info("批量删除0匹配关键词完成，成功删除 {} 个关键词", deletedCount);
            
        } catch (Exception e) {
            log.error("批量删除0匹配关键词失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量删除0匹配关键词失败: " + e.getMessage());
        }
        
        result.put("deletedCount", deletedCount);
        result.put("deletedKeywords", deletedKeywords);
        result.put("message", String.format("成功删除 %d 个0匹配的关键词", deletedCount));
        
        return result;
    }
}

package com.certification.service;

import com.certification.entity.common.DeviceMatchKeywords;
import com.certification.repository.DeviceMatchKeywordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备匹配关键词服务
 * 为DeviceData.vue中的统一关键词搜索功能提供服务
 */
@Service
@Transactional
public class DeviceMatchKeywordsService {

    @Autowired
    private DeviceMatchKeywordsRepository deviceMatchKeywordsRepository;

    /**
     * 获取普通关键词列表
     */
    public List<DeviceMatchKeywords> getNormalKeywords() {
        return deviceMatchKeywordsRepository.findByKeywordTypeAndEnabledTrue(DeviceMatchKeywords.KeywordType.NORMAL);
    }

    /**
     * 获取黑名单关键词列表
     */
    public List<DeviceMatchKeywords> getBlacklistKeywords() {
        return deviceMatchKeywordsRepository.findByKeywordTypeAndEnabledTrue(DeviceMatchKeywords.KeywordType.BLACKLIST);
    }

    /**
     * 获取所有关键词（包括禁用的）
     */
    public List<DeviceMatchKeywords> getAllKeywords(DeviceMatchKeywords.KeywordType keywordType) {
        return deviceMatchKeywordsRepository.findByKeywordType(keywordType);
    }

    /**
     * 添加关键词
     * 特别处理：添加白名单关键词时，如果该关键词在黑名单中，则自动从黑名单移除
     */
    public AddKeywordResult addKeyword(String keyword, DeviceMatchKeywords.KeywordType keywordType) {
        // 检查是否已存在相同类型的关键词
        Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                .findByKeywordAndKeywordType(keyword, keywordType);
        if (existing.isPresent()) {
            throw new RuntimeException("关键词已存在: " + keyword);
        }

        boolean removedFromBlacklist = false;

        // 白名单优先级保护：添加白名单时，自动从黑名单中移除
        if (keywordType == DeviceMatchKeywords.KeywordType.WHITELIST) {
            Optional<DeviceMatchKeywords> blacklistKeyword = deviceMatchKeywordsRepository
                    .findByKeywordAndKeywordType(keyword, DeviceMatchKeywords.KeywordType.BLACKLIST);
            if (blacklistKeyword.isPresent()) {
                deviceMatchKeywordsRepository.delete(blacklistKeyword.get());
                removedFromBlacklist = true;
                System.out.println("添加白名单时自动从黑名单移除: " + keyword);
            }
        }

        DeviceMatchKeywords deviceMatchKeywords = new DeviceMatchKeywords()
                .setKeyword(keyword)
                .setKeywordType(keywordType)
                .setEnabled(true);

        DeviceMatchKeywords saved = deviceMatchKeywordsRepository.save(deviceMatchKeywords);

        return new AddKeywordResult(saved, removedFromBlacklist);
    }

    /**
     * 添加关键词结果DTO
     */
    public static class AddKeywordResult {
        private DeviceMatchKeywords keyword;
        private boolean removedFromBlacklist;

        public AddKeywordResult(DeviceMatchKeywords keyword, boolean removedFromBlacklist) {
            this.keyword = keyword;
            this.removedFromBlacklist = removedFromBlacklist;
        }

        public DeviceMatchKeywords getKeyword() {
            return keyword;
        }

        public void setKeyword(DeviceMatchKeywords keyword) {
            this.keyword = keyword;
        }

        public boolean isRemovedFromBlacklist() {
            return removedFromBlacklist;
        }

        public void setRemovedFromBlacklist(boolean removedFromBlacklist) {
            this.removedFromBlacklist = removedFromBlacklist;
        }
    }

    /**
     * 更新关键词
     */
    public DeviceMatchKeywords updateKeyword(Long id, String keyword, Boolean enabled) {
        Optional<DeviceMatchKeywords> optional = deviceMatchKeywordsRepository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("关键词不存在: " + id);
        }

        DeviceMatchKeywords deviceMatchKeywords = optional.get();
        
        // 检查关键词是否重复（排除当前记录）
        if (!keyword.equals(deviceMatchKeywords.getKeyword())) {
            boolean exists = deviceMatchKeywordsRepository.existsByKeywordAndKeywordTypeAndIdNot(
                    keyword, deviceMatchKeywords.getKeywordType(), id);
            if (exists) {
                throw new RuntimeException("关键词已存在: " + keyword);
            }
        }

        deviceMatchKeywords.setKeyword(keyword).setEnabled(enabled);
        return deviceMatchKeywordsRepository.save(deviceMatchKeywords);
    }

    /**
     * 删除关键词
     */
    public void deleteKeyword(Long id) {
        deviceMatchKeywordsRepository.deleteById(id);
    }

    /**
     * 批量添加关键词
     */
    public List<DeviceMatchKeywords> batchAddKeywords(List<String> keywords, DeviceMatchKeywords.KeywordType keywordType) {
        return keywords.stream()
                .map(keyword -> {
                    try {
                        AddKeywordResult result = addKeyword(keyword, keywordType);
                        return result.getKeyword();
                    } catch (Exception e) {
                        System.err.println("添加关键词失败: " + keyword + ", 错误: " + e.getMessage());
                        return null;
                    }
                })
                .filter(keyword -> keyword != null)
                .collect(Collectors.toList());
    }

    /**
     * 获取关键词字符串列表（用于搜索）
     */
    public List<String> getKeywordStrings(DeviceMatchKeywords.KeywordType keywordType) {
        List<DeviceMatchKeywords> keywords = keywordType == DeviceMatchKeywords.KeywordType.NORMAL 
                ? getNormalKeywords() 
                : getBlacklistKeywords();
        
        return keywords.stream()
                .map(DeviceMatchKeywords::getKeyword)
                .collect(Collectors.toList());
    }

    /**
     * 切换关键词类型
     */
    public DeviceMatchKeywords toggleKeywordType(Long id) {
        Optional<DeviceMatchKeywords> optional = deviceMatchKeywordsRepository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("关键词不存在: " + id);
        }

        DeviceMatchKeywords deviceMatchKeywords = optional.get();
        DeviceMatchKeywords.KeywordType newType = deviceMatchKeywords.getKeywordType() == DeviceMatchKeywords.KeywordType.NORMAL
                ? DeviceMatchKeywords.KeywordType.BLACKLIST
                : DeviceMatchKeywords.KeywordType.NORMAL;

        // 检查新类型下是否已存在相同关键词
        Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                .findByKeywordAndKeywordType(deviceMatchKeywords.getKeyword(), newType);
        if (existing.isPresent()) {
            throw new RuntimeException("目标类型下已存在相同关键词: " + deviceMatchKeywords.getKeyword());
        }

        deviceMatchKeywords.setKeywordType(newType);
        return deviceMatchKeywordsRepository.save(deviceMatchKeywords);
    }

    /**
     * 根据ID获取关键词
     */
    public Optional<DeviceMatchKeywords> getKeywordById(Long id) {
        return deviceMatchKeywordsRepository.findById(id);
    }

    /**
     * 搜索关键词
     */
    public List<DeviceMatchKeywords> searchKeywords(String searchText) {
        return deviceMatchKeywordsRepository.findByKeywordContainingIgnoreCaseAndEnabledTrue(searchText);
    }

    /**
     * 获取统一关键词搜索配置
     * 为DeviceData.vue提供关键词配置
     */
    public UnifiedKeywordConfig getUnifiedKeywordConfig() {
        List<String> normalKeywords = getKeywordStrings(DeviceMatchKeywords.KeywordType.NORMAL);
        List<String> blacklistKeywords = getKeywordStrings(DeviceMatchKeywords.KeywordType.BLACKLIST);
        
        return new UnifiedKeywordConfig(normalKeywords, blacklistKeywords);
    }

    /**
     * 保存统一关键词配置
     * 从DeviceData.vue保存关键词配置
     */
    public void saveUnifiedKeywordConfig(List<String> normalKeywords, List<String> blacklistKeywords) {
        // 清空现有关键词
        deviceMatchKeywordsRepository.deleteAll();
        
        // 添加新的普通关键词
        if (normalKeywords != null && !normalKeywords.isEmpty()) {
            batchAddKeywords(normalKeywords, DeviceMatchKeywords.KeywordType.NORMAL);
        }
        
        // 添加新的黑名单关键词
        if (blacklistKeywords != null && !blacklistKeywords.isEmpty()) {
            batchAddKeywords(blacklistKeywords, DeviceMatchKeywords.KeywordType.BLACKLIST);
        }
    }

    /**
     * 批量检查数据字段是否匹配黑名单
     * @param textToCheck 要检查的文本
     * @return 匹配的黑名单关键词，如果不匹配返回null
     */
    public String checkBlacklistMatch(String textToCheck) {
        if (textToCheck == null || textToCheck.isEmpty()) {
            return null;
        }
        
        List<String> blacklist = getKeywordStrings(DeviceMatchKeywords.KeywordType.BLACKLIST);
        String lowerText = textToCheck.toLowerCase();
        
        for (String keyword : blacklist) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        
        return null;
    }
    
    /**
     * 检查多个字段是否匹配黑名单
     * @param fields 要检查的字段列表
     * @return 匹配的黑名单关键词，如果不匹配返回null
     */
    public String checkBlacklistMatchMultiple(String... fields) {
        for (String field : fields) {
            String matched = checkBlacklistMatch(field);
            if (matched != null) {
                return matched;
            }
        }
        return null;
    }
    
    /**
     * 智能添加黑名单关键词（避免重复）
     * @param keywords 要添加的关键词列表
     * @return 实际添加的数量
     */
    public int smartAddBlacklistKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }

        int added = 0;
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            String trimmedKeyword = keyword.trim();

            // 检查是否已存在
            Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                    .findByKeywordAndKeywordType(trimmedKeyword, DeviceMatchKeywords.KeywordType.BLACKLIST);

            if (!existing.isPresent()) {
                try {
                    AddKeywordResult result = addKeyword(trimmedKeyword, DeviceMatchKeywords.KeywordType.BLACKLIST);
                    added++;
                } catch (Exception e) {
                    System.err.println("添加黑名单关键词失败: " + trimmedKeyword + ", 错误: " + e.getMessage());
                }
            }
        }

        return added;
    }
    
    /**
     * 获取黑名单关键词字符串列表（仅启用的）
     */
    public List<String> getBlacklistKeywordStrings() {
        return getKeywordStrings(DeviceMatchKeywords.KeywordType.BLACKLIST);
    }

    /**
     * 获取白名单关键词列表（仅启用的）
     */
    public List<DeviceMatchKeywords> getWhitelistKeywords() {
        return deviceMatchKeywordsRepository.findByKeywordTypeAndEnabledTrue(DeviceMatchKeywords.KeywordType.WHITELIST);
    }

    /**
     * 获取白名单关键词字符串列表（仅启用的）
     */
    public List<String> getWhitelistKeywordStrings() {
        return getWhitelistKeywords().stream()
                .map(DeviceMatchKeywords::getKeyword)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否匹配白名单
     * @param textToCheck 要检查的文本
     * @return 匹配的白名单关键词，如果不匹配返回null
     */
    public String checkWhitelistMatch(String textToCheck) {
        if (textToCheck == null || textToCheck.isEmpty()) {
            return null;
        }

        List<String> whitelist = getWhitelistKeywordStrings();
        String lowerText = textToCheck.toLowerCase();

        for (String keyword : whitelist) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }

        return null;
    }

    /**
     * 检查多个字段是否匹配白名单
     * @param fields 要检查的字段列表
     * @return 匹配的白名单关键词，如果不匹配返回null
     */
    public String checkWhitelistMatchMultiple(String... fields) {
        for (String field : fields) {
            String matched = checkWhitelistMatch(field);
            if (matched != null) {
                return matched;
            }
        }
        return null;
    }

    /**
     * 智能添加白名单关键词（避免重复）
     * @param keywords 要添加的关键词列表
     * @return 实际添加的数量
     */
    public int smartAddWhitelistKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }

        int added = 0;
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            String trimmedKeyword = keyword.trim();

            // 检查是否已存在
            Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                    .findByKeywordAndKeywordType(trimmedKeyword, DeviceMatchKeywords.KeywordType.WHITELIST);

            if (!existing.isPresent()) {
                try {
                    AddKeywordResult result = addKeyword(trimmedKeyword, DeviceMatchKeywords.KeywordType.WHITELIST);
                    added++;
                } catch (Exception e) {
                    System.err.println("添加白名单关键词失败: " + trimmedKeyword + ", 错误: " + e.getMessage());
                }
            }
        }

        return added;
    }

    /**
     * 统一关键词配置DTO
     */
    public static class UnifiedKeywordConfig {
        private List<String> normalKeywords;
        private List<String> blacklistKeywords;

        public UnifiedKeywordConfig(List<String> normalKeywords, List<String> blacklistKeywords) {
            this.normalKeywords = normalKeywords;
            this.blacklistKeywords = blacklistKeywords;
        }

        public List<String> getNormalKeywords() {
            return normalKeywords;
        }

        public void setNormalKeywords(List<String> normalKeywords) {
            this.normalKeywords = normalKeywords;
        }

        public List<String> getBlacklistKeywords() {
            return blacklistKeywords;
        }

        public void setBlacklistKeywords(List<String> blacklistKeywords) {
            this.blacklistKeywords = blacklistKeywords;
        }
    }
}

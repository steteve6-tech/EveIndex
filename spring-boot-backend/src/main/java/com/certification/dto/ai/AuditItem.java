package com.certification.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.*;

/**
 * 单条审核项
 */
@Data
public class AuditItem {
    private Long id;
    private String entityType;
    private String deviceName;
    private String manufacturer;
    
    private boolean relatedToSkinDevice;  // 是否为测肤仪
    private double confidence;            // AI判断置信度
    private String reason;                // 判断理由
    private String category;              // 设备类别
    
    private List<String> blacklistKeywords = new ArrayList<>();
    
    // 新增字段：黑名单相关
    private Boolean blacklistMatched = false;  // 是否匹配黑名单
    private String matchedBlacklistKeyword;    // 匹配的黑名单关键词
    private List<String> suggestedBlacklist = new ArrayList<>();  // 建议添加的黑名单关键词
    
    @JsonProperty("remark")
    private String remark;                     // 备注信息
    
    public void addBlacklistKeyword(String keyword) {
        blacklistKeywords.add(keyword);
    }
    
    public void addSuggestedBlacklist(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            suggestedBlacklist.add(keyword.trim());
        }
    }
}


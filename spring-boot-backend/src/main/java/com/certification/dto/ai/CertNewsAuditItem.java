package com.certification.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.*;

/**
 * 认证新闻审核项
 */
@Data
public class CertNewsAuditItem {
    private String id;                          // 新闻ID
    private String title;                       // 标题
    private String country;                     // 国家
    private String sourceName;                  // 数据源
    
    private boolean relatedToCertification;     // 是否与认证标准相关
    private double confidence;                  // AI判断置信度
    private String reason;                      // 判断理由
    private List<String> extractedKeywords = new ArrayList<>();  // 提取的认证关键词
    
    // 黑名单相关
    private Boolean blacklistMatched = false;   // 是否匹配黑名单
    private String matchedBlacklistKeyword;     // 匹配的黑名单关键词
    
    @JsonProperty("remark")
    private String remark;                      // 备注信息
    
    public void addExtractedKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !extractedKeywords.contains(keyword.trim())) {
            extractedKeywords.add(keyword.trim());
        }
    }
}


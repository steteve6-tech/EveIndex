package com.certification.dto.ai;

import lombok.Data;
import java.util.*;

/**
 * 智能审核结果
 */
@Data
public class SmartAuditResult {
    private boolean success;
    private String message;
    private Date startTime;
    private Date endTime;
    private boolean previewMode;    // 是否为预览模式
    
    private int total;              // 总共审核数量
    private int keptCount;          // 保留高风险数量
    private int downgradedCount;    // 降级数量
    private int failedCount;        // 失败数量
    
    // 新增：黑名单相关统计
    private int blacklistFiltered = 0;  // 黑名单直接过滤的数量
    private int aiJudged = 0;           // AI判断的数量
    private int aiKept = 0;             // AI判断保留的数量
    private int aiDowngraded = 0;       // AI判断降级的数量
    
    private List<AuditItem> auditItems = new ArrayList<>();
    
    public void addAuditItem(AuditItem item) {
        auditItems.add(item);
        total++;
    }
    
    public void incrementKept() {
        keptCount++;
    }
    
    public void incrementDowngraded() {
        downgradedCount++;
    }
    
    public void incrementFailed() {
        failedCount++;
    }
    
    // 新增方法
    public void incrementBlacklistFiltered() {
        blacklistFiltered++;
        downgradedCount++;  // 黑名单过滤的也算降级
    }
    
    public void incrementAiKept() {
        aiKept++;
        keptCount++;
        aiJudged++;
    }
    
    public void incrementAiDowngraded() {
        aiDowngraded++;
        downgradedCount++;
        aiJudged++;
    }
    
    public long getDuration() {
        if (startTime == null || endTime == null) return 0;
        return endTime.getTime() - startTime.getTime();
    }
}


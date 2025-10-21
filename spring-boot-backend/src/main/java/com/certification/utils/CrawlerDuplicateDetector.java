package com.certification.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 爬虫连续重复批次检测工具类
 * 用于检测连续多个批次数据是否全部重复，以决定是否停止爬取
 */
@Slf4j
@Data
public class CrawlerDuplicateDetector {

    /**
     * 连续重复批次计数器
     */
    private int consecutiveDuplicateBatches = 0;

    /**
     * 连续重复批次停止阈值（默认3次）
     */
    private int duplicateThreshold = 3;

    /**
     * 总获取数量
     */
    private int totalFetched = 0;

    /**
     * 总保存数量
     */
    private int totalSaved = 0;

    /**
     * 总跳过数量
     */
    private int totalSkipped = 0;

    /**
     * 构造函数
     */
    public CrawlerDuplicateDetector() {
        this(3);
    }

    /**
     * 构造函数（指定阈值）
     * @param duplicateThreshold 连续重复批次停止阈值
     */
    public CrawlerDuplicateDetector(int duplicateThreshold) {
        this.duplicateThreshold = duplicateThreshold;
    }

    /**
     * 记录批次结果
     * @param batchSize 批次大小（获取的数据总数）
     * @param savedCount 本批次保存的新数据数量
     * @return 是否应该停止爬取
     */
    public boolean recordBatch(int batchSize, int savedCount) {
        totalFetched += batchSize;
        totalSaved += savedCount;
        totalSkipped += (batchSize - savedCount);

        // 如果本批次没有保存任何新数据
        if (savedCount == 0 && batchSize > 0) {
            consecutiveDuplicateBatches++;
            log.info("📊 批次统计 - 本批次: 获取{}条, 新增0条, 重复{}条 | 连续重复批次: {}/{}",
                batchSize, batchSize, consecutiveDuplicateBatches, duplicateThreshold);

            // 达到阈值，应该停止
            if (consecutiveDuplicateBatches >= duplicateThreshold) {
                log.warn("⚠️ 连续 {} 个批次都是重复数据，建议停止爬取", consecutiveDuplicateBatches);
                return true;
            }
        } else if (savedCount > 0) {
            // 有新数据，重置计数器
            log.info("✅ 批次统计 - 本批次: 获取{}条, 新增{}条, 重复{}条 | 连续重复批次已重置",
                batchSize, savedCount, (batchSize - savedCount));
            consecutiveDuplicateBatches = 0;
        }

        return false;
    }

    /**
     * 简化版记录方法（直接传入是否全部重复）
     * @param hasNewData 是否有新数据
     * @param dataCount 数据数量
     * @return 是否应该停止爬取
     */
    public boolean recordBatchSimple(boolean hasNewData, int dataCount) {
        if (!hasNewData && dataCount > 0) {
            consecutiveDuplicateBatches++;
            totalFetched += dataCount;
            totalSkipped += dataCount;

            log.info("📊 批次统计 - 本批次全部重复 ({} 条) | 连续重复批次: {}/{}",
                dataCount, consecutiveDuplicateBatches, duplicateThreshold);

            if (consecutiveDuplicateBatches >= duplicateThreshold) {
                log.warn("⚠️ 连续 {} 个批次都是重复数据，建议停止爬取", consecutiveDuplicateBatches);
                return true;
            }
        } else if (hasNewData) {
            log.info("✅ 批次统计 - 本批次有新数据 | 连续重复批次已重置");
            consecutiveDuplicateBatches = 0;
        }

        return false;
    }

    /**
     * 重置计数器
     */
    public void reset() {
        consecutiveDuplicateBatches = 0;
    }

    /**
     * 检查是否应该停止（不更新计数）
     * @return 是否应该停止
     */
    public boolean shouldStop() {
        return consecutiveDuplicateBatches >= duplicateThreshold;
    }

    /**
     * 打印最终统计
     */
    public void printFinalStats(String crawlerName) {
        log.info("=" + "=".repeat(60));
        log.info("🎯 {} 爬取完成统计:", crawlerName);
        log.info("   总获取: {} 条", totalFetched);
        log.info("   总保存: {} 条", totalSaved);
        log.info("   总跳过: {} 条", totalSkipped);
        log.info("   连续重复批次: {} 次", consecutiveDuplicateBatches);
        log.info("=" + "=".repeat(60));
    }

    /**
     * 获取统计摘要
     */
    public String getSummary() {
        return String.format("获取%d条，保存%d条，跳过%d条，连续重复%d次",
            totalFetched, totalSaved, totalSkipped, consecutiveDuplicateBatches);
    }
}

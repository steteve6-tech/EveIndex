package com.certification.scheduled;

import com.certification.service.NewDataStatisticsService;
import com.certification.service.ai.PendingAIJudgmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI判断和新增数据清理定时任务
 * 定期清理过期的待审核记录和已查看的新增数据标记
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AIJudgmentCleanupJob {

    private final PendingAIJudgmentService pendingJudgmentService;
    private final NewDataStatisticsService newDataStatisticsService;

    /**
     * 清理过期的待审核AI判断
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredJudgments() {
        log.info("========== 开始清理过期的待审核AI判断 ==========");

        try {
            int count = pendingJudgmentService.cleanupExpiredJudgments();
            log.info("✅ 清理完成: 过期记录数 = {}", count);

        } catch (Exception e) {
            log.error("❌ 清理过期AI判断失败", e);
        }

        log.info("========== 清理过期AI判断任务结束 ==========");
    }

    /**
     * 清理已查看的新增数据标记
     * 每天凌晨3点执行，清理已查看超过7天的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupViewedNewData() {
        log.info("========== 开始清理已查看的新增数据标记 ==========");

        try {
            int daysToKeep = 7; // 保留7天
            NewDataStatisticsService.CleanupResult result =
                    newDataStatisticsService.cleanupViewedNewData(daysToKeep);

            log.info("✅ 清理完成: 总计 {} 条记录", result.getTotalCount());
            log.info("详细统计: {}", result.getCountByType());

        } catch (Exception e) {
            log.error("❌ 清理已查看新增数据失败", e);
        }

        log.info("========== 清理新增数据标记任务结束 ==========");
    }

    /**
     * 每周日凌晨4点执行完整清理
     * 清理已确认/已拒绝超过30天的待审核记录
     */
    @Scheduled(cron = "0 0 4 * * 0")
    public void weeklyFullCleanup() {
        log.info("========== 开始每周完整清理任务 ==========");

        try {
            // 这里可以添加额外的清理逻辑
            // 例如：删除已确认/已拒绝超过30天的记录
            log.info("执行每周完整清理...");

            // 清理过期判断
            int expiredCount = pendingJudgmentService.cleanupExpiredJudgments();
            log.info("清理过期AI判断: {} 条", expiredCount);

            // 清理已查看数据（保留3天）
            NewDataStatisticsService.CleanupResult result =
                    newDataStatisticsService.cleanupViewedNewData(3);
            log.info("清理已查看新增数据: {} 条", result.getTotalCount());

            log.info("✅ 每周完整清理任务完成");

        } catch (Exception e) {
            log.error("❌ 每周完整清理任务失败", e);
        }

        log.info("========== 每周完整清理任务结束 ==========");
    }

    /**
     * 统计任务 - 每天早上8点执行
     * 记录当前待审核数量和新增数据数量
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyStatistics() {
        log.info("========== 每日统计报告 ==========");

        try {
            // 统计设备数据模块待审核数量
            long devicePendingCount = pendingJudgmentService.getPendingCount("DEVICE_DATA");
            log.info("设备数据模块 - 待审核AI判断: {} 条", devicePendingCount);

            // 统计医疗认证模块待审核数量
            long certPendingCount = pendingJudgmentService.getPendingCount("CERT_NEWS");
            log.info("医疗认证模块 - 待审核AI判断: {} 条", certPendingCount);

            // 统计新增数据
            var newDataCount = newDataStatisticsService.getNewDataCount("DEVICE_DATA");
            log.info("新增数据统计: {}", newDataCount);

            log.info("✅ 每日统计完成");

        } catch (Exception e) {
            log.error("❌ 每日统计任务失败", e);
        }

        log.info("========== 每日统计报告结束 ==========");
    }
}

package com.certification.scheduler;

import com.certification.service.DailyCountryRiskStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 每日国家高风险数据统计定时任务
 */
@Slf4j
@Component
public class DailyCountryRiskStatsScheduler {

    @Autowired
    private DailyCountryRiskStatsService dailyCountryRiskStatsService;

    /**
     * 每天凌晨2点执行，统计前一天的高风险数据
     * cron表达式: 秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateDailyStats() {
        log.info("开始执行每日国家高风险数据统计定时任务");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            dailyCountryRiskStatsService.calculateDailyStats(yesterday);
            
            log.info("每日国家高风险数据统计定时任务执行完成，统计日期: {}", yesterday);
            
        } catch (Exception e) {
            log.error("每日国家高风险数据统计定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每天凌晨3点执行，统计前7天的高风险数据（用于补全数据）
     * 0 0 3 * * ? 表示每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void calculateWeeklyStats() {
        log.info("开始执行每周国家高风险数据统计定时任务");
        
        try {
            LocalDate today = LocalDate.now();
            
            // 统计前7天的数据
            for (int i = 1; i <= 7; i++) {
                LocalDate statDate = today.minusDays(i);
                try {
                    dailyCountryRiskStatsService.calculateDailyStats(statDate);
                    log.debug("统计日期 {} 的高风险数据完成", statDate);
                } catch (Exception e) {
                    log.warn("统计日期 {} 的高风险数据失败: {}", statDate, e.getMessage());
                }
            }
            
            log.info("每周国家高风险数据统计定时任务执行完成");
            
        } catch (Exception e) {
            log.error("每周国家高风险数据统计定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每小时执行一次，检查是否有遗漏的统计数据
     * 0 0 * * * ? 表示每小时的第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkMissingStats() {
        log.debug("检查是否有遗漏的统计数据");
        
        try {
            // 这里可以添加检查逻辑，比如检查最近几天的数据是否完整
            // 如果发现遗漏，可以自动补全
            
            log.debug("统计数据检查完成");
            
        } catch (Exception e) {
            log.error("统计数据检查失败: {}", e.getMessage(), e);
        }
    }
}

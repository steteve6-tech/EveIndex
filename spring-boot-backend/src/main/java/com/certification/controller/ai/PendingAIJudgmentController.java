package com.certification.controller.ai;

import com.certification.entity.ai.AIPendingJudgment;
import com.certification.service.ai.PendingAIJudgmentService;
import com.certification.service.ai.PendingAIJudgmentService.BatchConfirmResult;
import com.certification.service.ai.PendingAIJudgmentService.DeviceDataStatisticsDTO;
import com.certification.service.ai.PendingAIJudgmentService.JudgmentDetailsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI判断待审核Controller
 * 提供待审核AI判断的管理API
 */
@RestController
@RequestMapping("/ai-judgment")
@Tag(name = "AI判断待审核管理", description = "管理待审核的AI判断结果")
@Slf4j
@RequiredArgsConstructor
public class PendingAIJudgmentController {

    private final PendingAIJudgmentService pendingJudgmentService;

    /**
     * 获取待审核列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审核列表", description = "获取指定模块的待审核AI判断列表")
    public ResponseEntity<Map<String, Object>> getPendingList(
            @Parameter(description = "模块类型: DEVICE_DATA 或 CERT_NEWS")
            @RequestParam(required = true) String moduleType,
            @Parameter(description = "状态: PENDING/CONFIRMED/REJECTED/EXPIRED", required = false)
            @RequestParam(required = false) String status) {

        log.info("获取待审核列表: moduleType={}, status={}", moduleType, status);

        List<AIPendingJudgment> list = pendingJudgmentService.getPendingJudgments(moduleType, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        response.put("total", list.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取待审核数量
     */
    @GetMapping("/pending/count")
    @Operation(summary = "获取待审核数量", description = "获取指定模块的待审核数量")
    public ResponseEntity<Map<String, Object>> getPendingCount(
            @Parameter(description = "模块类型: DEVICE_DATA 或 CERT_NEWS")
            @RequestParam(required = true) String moduleType) {

        log.info("获取待审核数量: moduleType={}", moduleType);

        long count = pendingJudgmentService.getPendingCount(moduleType);
        Map<String, Long> countByType = pendingJudgmentService.getPendingCountByEntityType(moduleType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", count);
        response.put("byEntityType", countByType);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取设备数据模块统计信息
     */
    @GetMapping("/pending/statistics/device-data")
    @Operation(summary = "获取设备数据统计", description = "获取设备数据模块的待审核统计信息（黑名单过滤、高风险等）")
    public ResponseEntity<Map<String, Object>> getDeviceDataStatistics() {

        log.info("获取设备数据统计信息");

        DeviceDataStatisticsDTO stats = pendingJudgmentService.getDeviceDataStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取判断详情
     */
    @GetMapping("/pending/{id}")
    @Operation(summary = "获取判断详情", description = "获取单个待审核判断的详细信息")
    public ResponseEntity<Map<String, Object>> getJudgmentDetails(
            @Parameter(description = "待审核记录ID")
            @PathVariable Long id) {

        log.info("获取判断详情: id={}", id);

        JudgmentDetailsDTO details = pendingJudgmentService.getJudgmentDetails(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", details);

        return ResponseEntity.ok(response);
    }

    /**
     * 确认单个判断
     */
    @PostMapping("/confirm/{id}")
    @Operation(summary = "确认判断", description = "确认单个AI判断并执行操作")
    public ResponseEntity<Map<String, Object>> confirmJudgment(
            @Parameter(description = "待审核记录ID")
            @PathVariable Long id,
            @Parameter(description = "确认人")
            @RequestParam(required = false) String confirmedBy) {

        log.info("确认AI判断: id={}, confirmedBy={}", id, confirmedBy);

        try {
            pendingJudgmentService.confirmJudgment(id, confirmedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "确认成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("确认判断失败: id={}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "确认失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量确认判断
     */
    @PostMapping("/batch-confirm")
    @Operation(summary = "批量确认", description = "批量确认多个AI判断")
    public ResponseEntity<Map<String, Object>> batchConfirm(
            @RequestBody BatchConfirmRequest request) {

        log.info("批量确认AI判断: count={}", request.getIds().size());

        try {
            BatchConfirmResult result = pendingJudgmentService.batchConfirm(
                    request.getIds(),
                    request.getConfirmedBy());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", String.format("批量确认完成：成功 %d 条，失败 %d 条",
                    result.getSuccessCount(), result.getFailedCount()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量确认失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量确认失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 拒绝判断
     */
    @PostMapping("/reject/{id}")
    @Operation(summary = "拒绝判断", description = "拒绝单个AI判断")
    public ResponseEntity<Map<String, Object>> rejectJudgment(
            @Parameter(description = "待审核记录ID")
            @PathVariable Long id,
            @Parameter(description = "拒绝人")
            @RequestParam(required = false) String rejectedBy) {

        log.info("拒绝AI判断: id={}, rejectedBy={}", id, rejectedBy);

        try {
            pendingJudgmentService.rejectJudgment(id, rejectedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "拒绝成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("拒绝判断失败: id={}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "拒绝失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清理过期记录
     */
    @PostMapping("/cleanup-expired")
    @Operation(summary = "清理过期记录", description = "清理30天未确认的过期待审核记录")
    public ResponseEntity<Map<String, Object>> cleanupExpired() {

        log.info("手动触发清理过期记录");

        try {
            int count = pendingJudgmentService.cleanupExpiredJudgments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            response.put("message", String.format("已清理 %d 条过期记录", count));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("清理过期记录失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== DTO ====================

    /**
     * 批量确认请求
     */
    @Data
    public static class BatchConfirmRequest {
        private List<Long> ids;
        private String confirmedBy;
    }
}

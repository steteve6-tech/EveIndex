package com.certification.controller;

import com.certification.service.NewDataStatisticsService;
import com.certification.service.NewDataStatisticsService.CleanupResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新增数据Controller
 * 提供新增数据的查询和管理API
 */
@RestController
@RequestMapping("/new-data")
@Tag(name = "新增数据管理", description = "管理爬虫新增数据的查询和标记")
@Slf4j
@RequiredArgsConstructor
public class NewDataController {

    private final NewDataStatisticsService newDataStatisticsService;

    /**
     * 获取新增数据数量
     */
    @GetMapping("/count")
    @Operation(summary = "获取新增数据数量", description = "获取指定模块的新增数据数量统计")
    public ResponseEntity<Map<String, Object>> getNewDataCount(
            @Parameter(description = "模块类型: DEVICE_DATA 或 CERT_NEWS")
            @RequestParam(required = true) String moduleType) {

        log.info("获取新增数据数量: moduleType={}", moduleType);

        Map<String, Long> countMap = newDataStatisticsService.getNewDataCount(moduleType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", countMap);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定实体类型的新增数据数量
     */
    @GetMapping("/count/{entityType}")
    @Operation(summary = "获取指定类型新增数据数量", description = "获取单个实体类型的新增数据数量")
    public ResponseEntity<Map<String, Object>> getNewDataCountByType(
            @Parameter(description = "实体类型: Application/Recall/Event/Registration/Document/Customs")
            @PathVariable String entityType) {

        log.info("获取新增数据数量: entityType={}", entityType);

        long count = newDataStatisticsService.getNewDataCountByEntityType(entityType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entityType", entityType);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取新增数据列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取新增数据列表", description = "获取指定实体类型的新增数据列表（分页）")
    public ResponseEntity<Map<String, Object>> getNewDataList(
            @Parameter(description = "实体类型: Application/Recall/Event/Registration/Document/Customs")
            @RequestParam(required = true) String entityType,
            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取新增数据列表: entityType={}, page={}, size={}", entityType, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<?> dataPage = newDataStatisticsService.getNewDataList(entityType, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dataPage.getContent());
        response.put("total", dataPage.getTotalElements());
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", dataPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 标记数据为已查看
     */
    @PostMapping("/mark-viewed")
    @Operation(summary = "标记为已查看", description = "将指定数据标记为已查看")
    public ResponseEntity<Map<String, Object>> markAsViewed(
            @RequestBody MarkViewedRequest request) {

        log.info("标记数据为已查看: entityType={}, count={}",
                request.getEntityType(), request.getIds().size());

        try {
            int count = newDataStatisticsService.markDataAsViewed(
                    request.getEntityType(),
                    request.getIds());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            response.put("message", String.format("已标记 %d 条数据为已查看", count));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("标记数据为已查看失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量取消新增标记
     */
    @PostMapping("/clear-new-flag")
    @Operation(summary = "取消新增标记", description = "批量取消数据的新增标记")
    public ResponseEntity<Map<String, Object>> clearNewFlag(
            @RequestBody ClearNewFlagRequest request) {

        log.info("取消新增标记: entityType={}, count={}",
                request.getEntityType(), request.getIds().size());

        try {
            int count = newDataStatisticsService.batchClearNewFlag(
                    request.getEntityType(),
                    request.getIds());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            response.put("message", String.format("已取消 %d 条数据的新增标记", count));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("取消新增标记失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清理已查看的新增数据标记
     */
    @PostMapping("/cleanup-viewed")
    @Operation(summary = "清理已查看数据", description = "清理已查看超过指定天数的新增数据标记")
    public ResponseEntity<Map<String, Object>> cleanupViewed(
            @Parameter(description = "保留天数（默认7天）")
            @RequestParam(defaultValue = "7") int daysToKeep) {

        log.info("清理已查看的新增数据标记: daysToKeep={}", daysToKeep);

        try {
            CleanupResult result = newDataStatisticsService.cleanupViewedNewData(daysToKeep);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", String.format("已清理 %d 条记录", result.getTotalCount()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("清理已查看数据失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量将所有数据设置为普通数据（非新增）
     */
    @PostMapping("/batch-set-all-normal")
    @Operation(summary = "批量设置所有数据为普通数据", description = "将所有现有数据的isNew标记设置为false")
    public ResponseEntity<Map<String, Object>> batchSetAllAsNormal() {

        log.info("开始批量将所有数据设置为普通数据...");

        try {
            Map<String, Integer> result = newDataStatisticsService.batchSetAllDataAsNormal();

            int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCount", totalCount);
            response.put("details", result);
            response.put("message", String.format("成功将 %d 条数据设置为普通数据", totalCount));

            log.info("批量设置完成: 总计 {} 条数据", totalCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量设置所有数据为普通数据失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 自动标记已查看（用于页面加载时）
     */
    @PostMapping("/auto-mark-viewed")
    @Operation(summary = "自动标记已查看", description = "用户打开页面时自动将所有新增数据标记为已查看")
    public ResponseEntity<Map<String, Object>> autoMarkViewed(
            @Parameter(description = "模块类型: DEVICE_DATA 或 CERT_NEWS")
            @RequestParam(required = true) String moduleType) {

        log.info("自动标记已查看: moduleType={}", moduleType);

        try {
            Map<String, Integer> result = newDataStatisticsService.autoMarkAllNewDataAsViewed(moduleType);

            int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCount", totalCount);
            response.put("details", result);
            response.put("message", String.format("已自动标记 %d 条数据为已查看", totalCount));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("自动标记已查看失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== DTO ====================

    /**
     * 标记已查看请求
     */
    @Data
    public static class MarkViewedRequest {
        private String entityType;
        private List<Long> ids;
    }

    /**
     * 取消新增标记请求
     */
    @Data
    public static class ClearNewFlagRequest {
        private String entityType;
        private List<Long> ids;
    }
}

package com.certification.controller;

import com.certification.entity.CompetitorInfo;
import com.certification.service.CompetitorInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/competitor-info")
@Tag(name = "竞品信息管理", description = "竞品信息的管理和查询接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
@RequiredArgsConstructor
public class CompetitorInfoController {

    private final CompetitorInfoService competitorInfoService;

    @GetMapping("/statistics")
    @Operation(summary = "获取竞品信息统计数据", description = "获取竞品信息的总数、活跃数量、本月新增、风险提醒等统计数据")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = competitorInfoService.getStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计数据成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取竞品信息统计数据失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "获取统计数据失败: " + e.getMessage(),
                "data", Map.of(
                    "totalRecords", 0,
                    "activeCompetitors", 0,
                    "monthlyNew", 0,
                    "riskAlerts", 0
                )
            ));
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取竞品信息列表", description = "分页查询竞品信息列表，支持关键词搜索和状态筛选")
    public ResponseEntity<Map<String, Object>> getCompetitorList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "数据来源筛选") @RequestParam(required = false) String dataSource,
            @Parameter(description = "风险等级筛选") @RequestParam(required = false) String riskLevel) {
        
        try {
            Page<CompetitorInfo> result = competitorInfoService.getCompetitorList(
                keyword, status, dataSource, riskLevel, page, size);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取竞品列表成功",
                "data", Map.of(
                    "list", result.getContent(),
                    "total", result.getTotalElements(),
                    "page", page,
                    "size", size,
                    "totalPages", result.getTotalPages()
                )
            ));
        } catch (Exception e) {
            log.error("获取竞品信息列表失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "获取竞品列表失败: " + e.getMessage(),
                "data", Map.of(
                    "list", List.of(),
                    "total", 0,
                    "page", page,
                    "size", size,
                    "totalPages", 0
                )
            ));
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取竞品信息详情", description = "根据ID获取竞品信息的详细信息")
    public ResponseEntity<Map<String, Object>> getCompetitorInfoDetail(
            @Parameter(description = "竞品信息ID") @PathVariable Long id) {
        
        try {
            Optional<CompetitorInfo> competitorInfo = competitorInfoService.getCompetitorInfoById(id);
            if (competitorInfo.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取竞品详情成功",
                    "data", competitorInfo.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取竞品信息详情失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "获取竞品详情失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建竞品信息", description = "创建新的竞品信息记录")
    public ResponseEntity<Map<String, Object>> createCompetitorInfo(
            @RequestBody CompetitorInfo competitorInfo) {
        
        try {
            CompetitorInfo created = competitorInfoService.createCompetitorInfo(competitorInfo);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "创建竞品信息成功",
                "data", created
            ));
        } catch (Exception e) {
            log.error("创建竞品信息失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "创建竞品信息失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "更新竞品信息", description = "更新指定ID的竞品信息")
    public ResponseEntity<Map<String, Object>> updateCompetitorInfo(
            @Parameter(description = "竞品信息ID") @PathVariable Long id,
            @RequestBody CompetitorInfo competitorInfo) {
        
        try {
            CompetitorInfo updated = competitorInfoService.updateCompetitorInfo(id, competitorInfo);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "更新竞品信息成功",
                "data", updated
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "data", null
            ));
        } catch (Exception e) {
            log.error("更新竞品信息失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "更新竞品信息失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除竞品信息", description = "删除指定ID的竞品信息")
    public ResponseEntity<Map<String, Object>> deleteCompetitorInfo(
            @Parameter(description = "竞品信息ID") @PathVariable Long id) {
        
        try {
            competitorInfoService.deleteCompetitorInfo(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "删除竞品信息成功",
                "data", null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "data", null
            ));
        } catch (Exception e) {
            log.error("删除竞品信息失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "删除竞品信息失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @PostMapping("/push-data")
    @Operation(summary = "批量推送竞品数据", description = "批量推送竞品数据到数据库")
    public ResponseEntity<Map<String, Object>> pushDataToCompetitorInfo(
            @RequestBody List<CompetitorInfo> competitorInfoList) {
        
        try {
            Map<String, Object> result = competitorInfoService.pushDataToCompetitorInfo(competitorInfoList);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量推送数据成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("批量推送竞品数据失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "批量推送数据失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @DeleteMapping("/clear-all")
    @Operation(summary = "清空所有竞品数据", description = "清空数据库中的所有竞品信息数据")
    public ResponseEntity<Map<String, Object>> clearAllCompetitorData() {
        try {
            competitorInfoService.clearAllCompetitorData();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "清空所有竞品数据成功",
                "data", null
            ));
        } catch (Exception e) {
            log.error("清空竞品数据失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "清空数据失败: " + e.getMessage(),
                "data", null
            ));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查竞品信息服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "竞品信息服务正常运行",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
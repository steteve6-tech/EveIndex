package com.certification.controller;

import com.certification.entity.common.CrawlerData;
import com.certification.standards.CrawlerDataService;
import com.certification.service.VisualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 可视化控制器
 * 提供图表生成和数据导出的API接口
 */
@Slf4j
@RestController
@RequestMapping("/visualization")
@Tag(name = "可视化接口", description = "图表生成和数据导出相关接口")
public class VisualizationController {
    
    @Autowired
    private VisualizationService visualizationService;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    /**
     * 获取仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表板数据", description = "获取包含统计信息和图表路径的仪表板数据")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> dashboardData = visualizationService.generateDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成数据源分布饼图
     */
    @PostMapping("/charts/source-pie")
    @Operation(summary = "生成数据源分布饼图", description = "生成数据源分布的饼图并返回文件路径")
    public ResponseEntity<Map<String, String>> generateSourcePieChart() {
        try {
            String chartPath = visualizationService.generateSourcePieChart();
            if (chartPath != null) {
                return ResponseEntity.ok(Map.of("chartPath", chartPath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "生成图表失败"));
            }
        } catch (Exception e) {
            log.error("生成数据源分布饼图失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 生成数据状态分布柱状图
     */
    @PostMapping("/charts/status-bar")
    @Operation(summary = "生成数据状态分布柱状图", description = "生成数据状态分布的柱状图并返回文件路径")
    public ResponseEntity<Map<String, String>> generateStatusBarChart() {
        try {
            String chartPath = visualizationService.generateStatusBarChart();
            if (chartPath != null) {
                return ResponseEntity.ok(Map.of("chartPath", chartPath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "生成图表失败"));
            }
        } catch (Exception e) {
            log.error("生成数据状态分布柱状图失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 生成时间趋势图
     */
    @PostMapping("/charts/trend")
    @Operation(summary = "生成时间趋势图", description = "生成指定天数的数据趋势图")
    public ResponseEntity<Map<String, String>> generateTrendChart(
            @Parameter(description = "天数", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        try {
            String chartPath = visualizationService.generateTrendChart(days);
            if (chartPath != null) {
                return ResponseEntity.ok(Map.of("chartPath", chartPath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "生成图表失败"));
            }
        } catch (Exception e) {
            log.error("生成时间趋势图失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 导出所有数据到Excel
     */
    @PostMapping("/export/all")
    @Operation(summary = "导出所有数据到Excel", description = "将所有爬虫数据导出到Excel文件")
    public ResponseEntity<Map<String, String>> exportAllDataToExcel() {
        try {
            String filePath = visualizationService.exportAllDataToExcel();
            if (filePath != null) {
                return ResponseEntity.ok(Map.of("filePath", filePath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "导出失败"));
            }
        } catch (Exception e) {
            log.error("导出所有数据到Excel失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 根据数据源导出数据到Excel
     */
    @PostMapping("/export/source/{sourceName}")
    @Operation(summary = "根据数据源导出数据到Excel", description = "根据数据源名称导出数据到Excel文件")
    public ResponseEntity<Map<String, String>> exportDataBySourceToExcel(
            @Parameter(description = "数据源名称", example = "SGS")
            @PathVariable String sourceName) {
        try {
            String filePath = visualizationService.exportDataBySourceToExcel(sourceName);
            if (filePath != null) {
                return ResponseEntity.ok(Map.of("filePath", filePath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "导出失败"));
            }
        } catch (Exception e) {
            log.error("根据数据源导出数据到Excel失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 根据状态导出数据到Excel
     */
    @PostMapping("/export/status/{status}")
    @Operation(summary = "根据状态导出数据到Excel", description = "根据数据状态导出数据到Excel文件")
    public ResponseEntity<Map<String, String>> exportDataByStatusToExcel(
            @Parameter(description = "数据状态", example = "PROCESSED")
            @PathVariable CrawlerData.DataStatus status) {
        try {
            String filePath = visualizationService.exportDataByStatusToExcel(status);
            if (filePath != null) {
                return ResponseEntity.ok(Map.of("filePath", filePath));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "导出失败"));
            }
        } catch (Exception e) {
            log.error("根据状态导出数据到Excel失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取数据统计信息，包括总数、各数据源统计、各状态统计等")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = crawlerDataService.getStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据", description = "获取指定天数的数据趋势信息")
    public ResponseEntity<Map<String, Object>> getTrendData(
            @Parameter(description = "天数", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> trendData = Map.of(
                "days", days,
                "trendData", crawlerDataService.getTrendData(days)
            );
            return ResponseEntity.ok(trendData);
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.certification.controller;

import com.certification.service.CountryRiskStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 国家风险统计控制器
 * 提供国家风险趋势和统计数据的API接口
 */
@Slf4j
@Tag(name = "国家风险统计", description = "国家风险趋势和统计数据接口")
@RestController
@RequestMapping("/country-risk")
public class CountryRiskStatisticsController {

    @Autowired
    private CountryRiskStatisticsService countryRiskStatisticsService;

    /**
     * 获取国家风险趋势数据
     */
    @GetMapping("/trends")
    @Operation(summary = "获取国家风险趋势", description = "获取每个国家每天的高风险数据数量变化趋势")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getCountryRiskTrends() {
        log.info("获取国家风险趋势数据");
        
        try {
            Map<String, Object> result = countryRiskStatisticsService.getCountryRiskTrends();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取国家风险趋势数据失败: {}", e.getMessage(), e);
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "获取国家风险趋势数据失败: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取指定日期的国家风险统计
     */
    @GetMapping("/stats/{date}")
    @Operation(summary = "获取指定日期的国家风险统计", description = "获取指定日期的国家风险统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "日期格式错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getCountryRiskStatsByDate(
            @Parameter(description = "日期，格式：yyyy-MM-dd") 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("获取{}的国家风险统计数据", date);
        
        try {
            Map<String, Object> result = countryRiskStatisticsService.getCountryRiskStatsByDate(date);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取{}国家风险统计数据失败: {}", date, e.getMessage(), e);
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "获取国家风险统计数据失败: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取国家风险排行榜
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取国家风险排行榜", description = "获取最近7天的国家风险排行榜")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getCountryRiskRanking() {
        log.info("获取国家风险排行榜");
        
        try {
            Map<String, Object> result = countryRiskStatisticsService.getCountryRiskRanking();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取国家风险排行榜失败: {}", e.getMessage(), e);
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "获取国家风险排行榜失败: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 初始化基准数据
     */
    @PostMapping("/init-baseline")
    @Operation(summary = "初始化基准数据", description = "初始化昨天的基准数据，为趋势图表提供起始点")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "初始化成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> initializeBaselineData() {
        log.info("初始化基准数据");
        
        try {
            countryRiskStatisticsService.initializeBaselineData();
            Map<String, Object> result = Map.of(
                "success", true,
                "message", "基准数据初始化完成",
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("初始化基准数据失败: {}", e.getMessage(), e);
            Map<String, Object> error = Map.of(
                "success", false,
                "error", "初始化基准数据失败: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(error);
        }
    }
}

package com.certification.controller.device;

import com.certification.service.device.DeviceDataStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备数据统计Controller
 * 职责: 设备数据的统计和分析
 *
 * 拆分自原 DeviceDataController
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@RestController
@RequestMapping("/device-data/statistics")
@Tag(name = "设备数据统计", description = "设备数据的统计和分析接口")
public class DeviceDataStatisticsController {

    @Autowired
    private DeviceDataStatisticsService statisticsService;

    /**
     * 获取设备数据总览统计
     * GET /device-data/statistics/overview
     */
    @GetMapping("/overview")
    @Operation(summary = "获取设备数据总览统计", description = "获取各种设备数据的总体统计信息")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return statisticsService.getOverview();
    }

    /**
     * 按国家统计设备数据
     * GET /device-data/statistics/by-country
     */
    @GetMapping("/by-country")
    @Operation(summary = "按国家统计设备数据", description = "按国家统计各种设备数据的数量")
    public ResponseEntity<Map<String, Object>> getCountryStatistics() {
        return statisticsService.getCountryStatistics();
    }

    /**
     * 按风险等级统计设备数据
     * GET /device-data/statistics/by-risk-level
     */
    @GetMapping("/by-risk-level")
    @Operation(summary = "按风险等级统计设备数据", description = "统计各类型设备数据的高中低风险数量")
    public ResponseEntity<Map<String, Object>> getRiskLevelStatistics() {
        return statisticsService.getRiskLevelStatistics();
    }

    /**
     * 获取高风险数据统计
     * GET /device-data/statistics/high-risk
     */
    @GetMapping("/high-risk")
    @Operation(summary = "获取高风险数据统计", description = "获取所有高风险设备数据的统计信息")
    public ResponseEntity<Map<String, Object>> getHighRiskStatistics() {
        return statisticsService.getHighRiskStatistics();
    }
}

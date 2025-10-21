package com.certification.controller.device;

import com.certification.service.device.DeviceDataUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备数据管理Controller
 * 职责: 设备数据的增删改操作
 *
 * 拆分自原 DeviceDataController
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@RestController
@RequestMapping("/device-data/management")
@Tag(name = "设备数据管理", description = "设备数据的增删改接口")
public class DeviceDataManagementController {

    @Autowired
    private DeviceDataUpdateService updateService;

    /**
     * 更新单个实体的风险等级和关键词
     * PUT /device-data/management/{entityType}/{id}
     */
    @PutMapping("/{entityType}/{id}")
    @Operation(summary = "更新单个实体的风险等级和关键词",
               description = "更新指定实体的风险等级和关键词")
    public ResponseEntity<Map<String, Object>> updateEntity(
            @Parameter(description = "实体类型", example = "Device510K")
            @PathVariable String entityType,
            @Parameter(description = "实体ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "更新数据，包含 riskLevel 和 keywords")
            @RequestBody Map<String, Object> request) {

        log.info("收到更新实体请求: entityType={}, id={}", entityType, id);
        return updateService.updateEntity(entityType, id, request);
    }

    /**
     * 批量更新多个实体的风险等级和关键词
     * PUT /device-data/management/{entityType}/batch
     */
    @PutMapping("/{entityType}/batch")
    @Operation(summary = "批量更新多个实体的风险等级和关键词",
               description = "批量更新指定类型的多个实体的风险等级和关键词")
    public ResponseEntity<Map<String, Object>> batchUpdate(
            @Parameter(description = "实体类型", example = "Device510K")
            @PathVariable String entityType,
            @Parameter(description = "批量更新数据，包含 ids, riskLevel 和 keywords")
            @RequestBody Map<String, Object> request) {

        log.info("收到批量更新请求: entityType={}", entityType);
        return updateService.batchUpdate(entityType, request);
    }

    /**
     * 重置所有数据为中等风险
     * POST /device-data/management/reset-medium
     */
    @PostMapping("/reset-medium")
    @Operation(summary = "重置所有数据为中等风险",
               description = "将所有设备数据的风险等级重置为MEDIUM，关键词重置为空数组")
    public ResponseEntity<Map<String, Object>> resetAllToMedium() {
        log.info("收到重置所有数据为中等风险的请求");
        return updateService.resetAllToMedium();
    }
}

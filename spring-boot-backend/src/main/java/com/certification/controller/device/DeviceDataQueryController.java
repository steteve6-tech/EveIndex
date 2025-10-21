package com.certification.controller.device;

import com.certification.service.device.DeviceDataSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备数据查询Controller
 * 职责: 设备数据的查询、搜索、分页
 *
 * 拆分自原 DeviceDataController
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@RestController
@RequestMapping("/device-data/query")
@Tag(name = "设备数据查询", description = "设备数据的查询和搜索接口")
public class DeviceDataQueryController {

    @Autowired
    private DeviceDataSearchService searchService;

    /**
     * 根据关键词搜索设备数据
     * POST /device-data/query/search-by-keywords
     */
    @PostMapping("/search-by-keywords")
    @Operation(summary = "根据关键词搜索设备数据",
               description = "支持搜索多个实体类型，返回匹配的设备数据，支持黑名单关键词过滤和搜索模式选择")
    public ResponseEntity<Map<String, Object>> searchByKeywords(
            @RequestBody Map<String, Object> requestBody,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "实体类型列表，用逗号分隔", example = "Device510K,DeviceEventReport")
            @RequestParam(required = false) String entityTypes,
            @Parameter(description = "风险等级过滤", example = "MEDIUM")
            @RequestParam(required = false) String riskLevel,
            @Parameter(description = "国家过滤", example = "US")
            @RequestParam(required = false) String country,
            @Parameter(description = "搜索模式", example = "fuzzy")
            @RequestParam(defaultValue = "fuzzy") String searchMode) {

        log.info("关键词搜索请求: page={}, size={}, entityTypes={}, riskLevel={}, country={}, searchMode={}",
                page, size, entityTypes, riskLevel, country, searchMode);

        return searchService.searchByKeywords(
            requestBody, page, size, entityTypes, riskLevel, country, searchMode);
    }

    /**
     * 获取510K设备记录
     * GET /device-data/query/510k
     */
    @GetMapping("/510k")
    @Operation(summary = "获取510K设备记录", description = "分页获取510K设备记录，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> get510KRecords(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.get510KRecords(page, size, keyword, countryCode);
    }

    /**
     * 获取召回记录
     * GET /device-data/query/recall-records
     */
    @GetMapping("/recall-records")
    @Operation(summary = "获取召回记录", description = "分页获取召回记录，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> getRecallRecords(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.getRecallRecords(page, size, keyword, countryCode);
    }

    /**
     * 获取事件报告
     * GET /device-data/query/event-reports
     */
    @GetMapping("/event-reports")
    @Operation(summary = "获取事件报告", description = "分页获取事件报告，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> getEventReports(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.getEventReports(page, size, keyword, countryCode);
    }

    /**
     * 获取注册记录
     * GET /device-data/query/registration-records
     */
    @GetMapping("/registration-records")
    @Operation(summary = "获取注册记录", description = "分页获取注册记录，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> getRegistrationRecords(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.getRegistrationRecords(page, size, keyword, countryCode);
    }

    /**
     * 获取指导文档
     * GET /device-data/query/guidance-documents
     */
    @GetMapping("/guidance-documents")
    @Operation(summary = "获取指导文档", description = "分页获取指导文档，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> getGuidanceDocuments(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.getGuidanceDocuments(page, size, keyword, countryCode);
    }

    /**
     * 获取海关案例
     * GET /device-data/query/customs-cases
     */
    @GetMapping("/customs-cases")
    @Operation(summary = "获取海关案例", description = "分页获取海关案例，支持关键词和国家搜索")
    public ResponseEntity<Map<String, Object>> getCustomsCases(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "国家代码")
            @RequestParam(required = false) String countryCode) {

        return searchService.getCustomsCases(page, size, keyword, countryCode);
    }

    /**
     * 获取支持的实体类型列表
     * GET /device-data/query/supported-entity-types
     */
    @GetMapping("/supported-entity-types")
    @Operation(summary = "获取支持的实体类型列表", description = "返回系统支持的所有设备实体类型")
    public ResponseEntity<Map<String, Object>> getSupportedEntityTypes() {
        List<String> entityTypes = searchService.getSupportedEntityTypes();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", entityTypes,
                "message", "获取支持的实体类型成功"
        ));
    }
}

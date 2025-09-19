package com.certification.controller;

import com.certification.dto.*;
import com.certification.entity.common.Standard;
import com.certification.service.StandardService;
import com.certification.standards.StandardManagementService;
import com.certification.standards.StandardQueryService;
import com.certification.standards.KeywordConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "标准管理", description = "认证标准的增删改查接口")
@RestController
@RequestMapping("/standards")
public class StandardController {
    
    @Autowired
    private StandardService standardService;
    
    @Autowired
    private StandardManagementService standardManagementService;

    @Autowired
    private StandardQueryService standardQueryService;

    @Autowired
    private KeywordConfig keywordConfig;
    
    /**
     * 获取标准列表
     */
    @GetMapping
    @Operation(summary = "获取标准列表", description = "分页获取标准列表，支持关键词搜索和筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStandards(
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "风险等级") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "国家") @RequestParam(required = false) String country,
            @Parameter(description = "标准状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, Math.min(size, 100));
            Page<Standard> standardsPage = standardService.searchStandards(keyword, riskLevel, country, status, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("standards", standardsPage.getContent());
            response.put("total", standardsPage.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", standardsPage.getTotalPages());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取单个标准详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取标准详情", description = "根据ID获取标准详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "标准不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStandard(
            @Parameter(description = "标准ID") @PathVariable Long id) {
        try {
            Standard standard = standardService.getStandardById(id);
            if (standard != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", standard);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "标准不存在");
                return ResponseEntity.status(404).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取标准详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    

    

    

    
    /**
     * 获取高风险标准
     */
    @GetMapping("/high-risk")
    @Operation(summary = "获取高风险标准", description = "获取所有高风险等级的标准")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getHighRiskStandards() {
        try {
            List<Standard> standards = standardService.getHighRiskStandards();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standards);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取高风险标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取监控中的标准
     */
    @GetMapping("/monitored")
    @Operation(summary = "获取监控中的标准", description = "获取所有监控中的标准")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getMonitoredStandards() {
        try {
            List<Standard> standards = standardService.getMonitoredStandards();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standards);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取监控标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取即将到期的标准
     */
    @GetMapping("/expiring")
    @Operation(summary = "获取即将到期的标准", description = "获取即将到期的标准")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getExpiringStandards() {
        try {
            List<Standard> standards = standardService.getExpiringStandards();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standards);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取即将到期标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取标准统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取标准统计信息", description = "获取标准的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStandardStatistics() {
        try {
            Map<String, Object> stats = standardService.getStandardStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 更新标准监控状态
     */
    @PutMapping("/{id}/monitoring")
    @Operation(summary = "更新监控状态", description = "更新标准的监控状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> updateMonitoringStatus(
            @Parameter(description = "标准ID") @PathVariable Long id,
            @Parameter(description = "监控状态") @RequestBody Map<String, Boolean> request) {
        
        try {
            Boolean isMonitored = request.get("isMonitored");
            if (isMonitored == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "缺少必要参数: isMonitored");
                return ResponseEntity.badRequest().body(error);
            }
            
            standardService.updateMonitoringStatus(id, isMonitored);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "监控状态更新成功");
            response.put("standardId", id);
            response.put("isMonitored", isMonitored);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "更新监控状态失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取即将生效的标准
     */
    @GetMapping("/upcoming")
    @Operation(summary = "获取即将生效的标准", description = "获取即将生效的标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getUpcomingStandards(
            @Parameter(description = "天数") @RequestParam(defaultValue = "365") int days,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Standard> standardPage = standardService.getUpcomingStandardsPage(days, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standardPage.getContent());
            response.put("total", standardPage.getTotalElements());
            response.put("current", page);
            response.put("pageSize", size);
            response.put("totalPages", standardPage.getTotalPages());
            response.put("generatedAt", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取即将生效标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 触发数据更新
     */
    @PostMapping("/update")
    @Operation(summary = "触发数据更新", description = "手动触发标准数据更新")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> triggerUpdate() {
        try {
            standardService.performDataUpdate();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "数据更新已完成");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "数据更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取风险统计
     */
    @GetMapping("/risk-statistics")
    @Operation(summary = "获取风险统计", description = "获取风险等级统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getRiskStatistics() {
        try {
            Map<String, Object> stats = standardService.getRiskStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取风险统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取最近更新的标准
     */
    @GetMapping("/recent-updates")
    @Operation(summary = "获取最近更新的标准", description = "获取最近更新的标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getRecentlyUpdatedStandards(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standardService.getRecentlyUpdatedStandards(limit));
            response.put("count", limit);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取最近更新标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取按发布时间排序的最新标准
     */
    @GetMapping("/latest-by-published")
    @Operation(summary = "获取按发布时间排序的最新标准", description = "获取按发布时间排序的最新标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getLatestStandardsByPublishedDate(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "3") int limit) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standardService.getLatestStandardsByPublishedDate(limit));
            response.put("count", limit);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取最新标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取按生效时间排序的即将生效标准
     */
    @GetMapping("/upcoming-by-effective")
    @Operation(summary = "获取按生效时间排序的即将生效标准", description = "获取未来生效的即将生效标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getUpcomingStandardsByEffectiveDate(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "3") int limit) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standardService.getUpcomingStandardsByEffectiveDate(limit));
            response.put("count", limit);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取即将生效标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取风险等级统计
     */
    @GetMapping("/risk-level-stats")
    @Operation(summary = "获取风险等级统计", description = "获取各风险等级的标准数量统计")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getRiskLevelStats() {
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standardService.getRiskLevelStats());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取风险等级统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 根据国家获取标准列表
     */
    @GetMapping("/country/{country}")
    @Operation(summary = "根据国家获取标准", description = "根据国家获取标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStandardsByCountry(
            @Parameter(description = "国家") @PathVariable String country,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int limit) {
        
        try {
            List<Standard> standards = standardService.getStandardsByCountry(country);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standards);
            response.put("total", standards.size());
            response.put("country", country);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 根据多个国家获取标准列表
     */
    @PostMapping("/countries")
    @Operation(summary = "根据多个国家获取标准", description = "根据多个国家获取标准列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getStandardsByCountries(
            @Parameter(description = "国家列表") @RequestBody List<String> countries) {
        
        try {
            List<Standard> standards = standardService.getStandardsByCountries(countries);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standards);
            response.put("total", standards.size());
            response.put("countries", countries);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取国家统计信息
     */
    @GetMapping("/statistics/countries")
    @Operation(summary = "获取国家统计信息", description = "获取各国家的标准统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ResponseEntity<Map<String, Object>> getCountryStatistics() {
        try {
            Map<String, Object> stats = standardService.getCountryStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== 从 StandardManagementController 移植的方法 ====================

    /**
     * 创建新标准
     */
    @PostMapping("/create")
    @Operation(summary = "创建新标准", description = "创建新的认证标准")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = Standard.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> createStandard(@RequestBody StandardCreateRequest request) {
        try {
            log.info("创建新标准: {}", request.getStandardNumber());
            Standard standard = standardManagementService.createStandard(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standard);
            response.put("message", "标准创建成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("创建标准失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("创建标准异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "创建标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 更新标准
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标准", description = "更新指定ID的标准信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = Standard.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "404", description = "标准不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> updateStandard(
            @Parameter(description = "标准ID") @PathVariable Long id,
            @RequestBody StandardUpdateRequest request) {
        try {
            log.info("更新标准: ID={}", id);
            Standard standard = standardManagementService.updateStandard(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", standard);
            response.put("message", "标准更新成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("更新标准失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("更新标准异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "更新标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 删除标准
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准", description = "逻辑删除指定ID的标准")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "标准不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> deleteStandard(@Parameter(description = "标准ID") @PathVariable Long id) {
        try {
            log.info("删除标准: ID={}", id);
            standardManagementService.deleteStandard(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "标准删除成功");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("删除标准失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("删除标准异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "删除标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 根据标准编号获取标准
     */
    @GetMapping("/number/{standardNumber}")
    @Operation(summary = "根据编号获取标准", description = "根据标准编号获取标准信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = Standard.class))),
            @ApiResponse(responseCode = "404", description = "标准不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getStandardByNumber(
            @Parameter(description = "标准编号") @PathVariable String standardNumber) {
        try {
            log.info("根据编号获取标准: {}", standardNumber);
            Standard standard = standardManagementService.getStandardByNumber(standardNumber);
            
            Map<String, Object> response = new HashMap<>();
            if (standard != null) {
                response.put("success", true);
                response.put("data", standard);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "标准不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            log.error("根据编号获取标准异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取标准失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 分页查询标准（管理版本）
     */
    @GetMapping("/management")
    @Operation(summary = "分页查询标准（管理版本）", description = "根据条件分页查询标准列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = StandardSearchResult.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getStandardsManagement(
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "风险等级") @RequestParam(required = false) String risk,
            @Parameter(description = "国家/地区") @RequestParam(required = false) String country,
            @Parameter(description = "标准状态") @RequestParam(required = false) Standard.StandardStatus status,
            @Parameter(description = "是否监控") @RequestParam(required = false) Boolean isMonitored,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        try {
            log.info("分页查询标准: keyword={}, risk={}, country={}, page={}, size={}", 
                    keyword, risk, country, page, size);

            StandardQueryRequest request = new StandardQueryRequest();
            request.setKeyword(keyword);
            request.setRisk(risk);
            request.setCountry(country);
            request.setStatus(status);
            request.setIsMonitored(isMonitored);
            request.setPage(page);
            request.setSize(size);

            Pageable pageable = PageRequest.of(page - 1, Math.min(size, 100));
            Page<Standard> standardsPage = standardManagementService.getStandards(request, pageable);

            StandardSearchResult result = new StandardSearchResult();
            result.setStandards(standardsPage.getContent());
            result.setTotal(standardsPage.getTotalElements());
            result.setPage(page);
            result.setSize(size);
            result.setTotalPages(standardsPage.getTotalPages());
            result.setRiskStats(standardManagementService.getRiskStatistics());
            result.setStatusStats(standardManagementService.getStatusStatistics());
            result.setCached(false);
            result.setTimestamp(java.time.LocalDateTime.now().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分页查询标准异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 根据关键词查询爬虫数据
     */
    @GetMapping("/crawler-data")
    @Operation(summary = "查询爬虫数据", description = "根据关键词配置查询相关的爬虫数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = CrawlerDataSearchResult.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> queryCrawlerData(CrawlerDataQueryRequest request) {
        try {
            log.info("查询爬虫数据: {}", request);
            CrawlerDataSearchResult result = standardQueryService.queryCrawlerDataByKeywords(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询爬虫数据异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询爬虫数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 根据市场查询爬虫数据
     */
    @GetMapping("/crawler-data/market/{marketCode}")
    @Operation(summary = "根据市场查询爬虫数据", description = "根据市场关键词查询相关的爬虫数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = CrawlerDataSearchResult.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> queryCrawlerDataByMarket(
            @Parameter(description = "市场代码") @PathVariable String marketCode,
            CrawlerDataQueryRequest request) {
        try {
            log.info("根据市场查询爬虫数据: marketCode={}", marketCode);
            CrawlerDataSearchResult result = standardQueryService.queryCrawlerDataByMarket(marketCode, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据市场查询爬虫数据异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询爬虫数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 查询高优先级爬虫数据
     */
    @GetMapping("/crawler-data/high-priority")
    @Operation(summary = "查询高优先级爬虫数据", description = "根据高优先级监测项查询爬虫数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = CrawlerDataSearchResult.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> queryHighPriorityCrawlerData(CrawlerDataQueryRequest request) {
        try {
            log.info("查询高优先级爬虫数据");
            CrawlerDataSearchResult result = standardQueryService.queryCrawlerDataByHighPriority(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询高优先级爬虫数据异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询高优先级爬虫数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取关键词配置
     */
    @GetMapping("/keywords")
    @Operation(summary = "获取关键词配置", description = "获取当前的关键词配置信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getKeywordConfig() {
        try {
            log.info("获取关键词配置");
            Map<String, Object> config = new HashMap<>();
            config.put("marketKeywords", keywordConfig.getMarketKeywords());
            config.put("highPriorityWatchItems", keywordConfig.getHighPriorityWatchItems());
            config.put("crawlerTemplates", keywordConfig.getCrawlerTemplates());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", config);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取关键词配置异常: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取关键词配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

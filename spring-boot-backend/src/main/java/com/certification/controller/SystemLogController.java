package com.certification.controller;

import org.springframework.data.domain.Page;
import com.certification.entity.notification.SystemLog;
import com.certification.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system-logs")
public class SystemLogController {
    
    @Autowired
    private SystemLogService systemLogService;
    
    /**
     * 获取系统日志列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            Page<SystemLog> logs = systemLogService.getLogs(level, type, status, page, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs.getContent());
            response.put("total", logs.getTotalElements());
            response.put("page", page);
            response.put("limit", limit);
            response.put("total_pages", logs.getTotalPages());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取日志失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取最近的日志
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<SystemLog> logs = systemLogService.getRecentLogs(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("total", logs.size());
            response.put("limit", limit);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取最近日志失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取错误日志
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorLogs(
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            List<SystemLog> logs = systemLogService.getErrorLogs(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("total", logs.size());
            response.put("limit", limit);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取错误日志失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLogStatistics() {
        
        try {
            Map<String, Object> stats = systemLogService.getLogStatistics();
            stats.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取日志统计失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 清理旧日志
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldLogs(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        
        try {
            systemLogService.cleanupOldLogs(daysToKeep);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "日志清理完成");
            response.put("days_to_keep", daysToKeep);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "日志清理失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 手动记录日志
     */
    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logMessage(
            @RequestBody Map<String, String> request) {
        
        try {
            String level = request.get("level");
            String type = request.get("type");
            String title = request.get("title");
            String message = request.get("message");
            String source = request.get("source");
            
            if (title == null || message == null || source == null) {
                throw new IllegalArgumentException("title, message, source 是必需的参数");
            }
            
            switch (level != null ? level.toUpperCase() : "INFO") {
                case "INFO":
                    systemLogService.logInfo(title, message, source);
                    break;
                case "WARN":
                    systemLogService.logWarn(title, message, source);
                    break;
                case "ERROR":
                    systemLogService.logError(title, message, source, null);
                    break;
                default:
                    systemLogService.logInfo(title, message, source);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "日志记录成功");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "记录日志失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}


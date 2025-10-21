package com.certification.controller;

import com.certification.entity.common.CertNewsTaskConfig;
import com.certification.entity.common.CertNewsTaskLog;
import com.certification.service.certnews.CertNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证新闻定时任务管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/cert-news-tasks")
@CrossOrigin(origins = "*")
public class CertNewsTaskController {

    @Autowired
    private CertNewsTaskService taskService;

    /**
     * 获取所有任务配置
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTasks() {
        try {
            List<CertNewsTaskConfig> tasks = taskService.getAllTasks();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tasks);
            response.put("total", tasks.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取任务列表失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取启用的任务
     */
    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledTasks() {
        try {
            List<CertNewsTaskConfig> tasks = taskService.getEnabledTasks();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tasks);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取启用任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取启用任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 根据ID获取任务
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaskById(@PathVariable Long id) {
        try {
            CertNewsTaskConfig task = taskService.getTaskById(id);

            Map<String, Object> response = new HashMap<>();
            if (task != null) {
                response.put("success", true);
                response.put("data", task);
            } else {
                response.put("success", false);
                response.put("message", "任务不存在");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 创建或更新任务
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody CertNewsTaskConfig task) {
        try {
            CertNewsTaskConfig savedTask = taskService.saveTask(task);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedTask);
            response.put("message", "任务保存成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTask(
            @PathVariable Long id,
            @RequestBody CertNewsTaskConfig task) {
        try {
            task.setId(id);
            CertNewsTaskConfig savedTask = taskService.saveTask(task);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedTask);
            response.put("message", "任务更新成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务删除成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 启用任务
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableTask(@PathVariable Long id) {
        try {
            taskService.enableTask(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已启用");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启用任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "启用任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 禁用任务
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableTask(@PathVariable Long id) {
        try {
            taskService.disableTask(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已禁用");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("禁用任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "禁用任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 手动执行任务
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long id) {
        try {
            CertNewsTaskLog log = taskService.executeTask(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", log);
            response.put("message", "任务执行完成");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("执行任务失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "执行任务失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取任务执行日志
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<Map<String, Object>> getTaskLogs(@PathVariable Long id) {
        try {
            List<CertNewsTaskLog> logs = taskService.getTaskLogs(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logs);
            response.put("total", logs.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取任务日志失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取任务日志失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取所有任务日志
     */
    @GetMapping("/logs/all")
    public ResponseEntity<Map<String, Object>> getAllLogs() {
        try {
            List<CertNewsTaskLog> logs = taskService.getAllLogs();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logs);
            response.put("total", logs.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取所有日志失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取所有日志失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取最近的日志
     */
    @GetMapping("/logs/recent")
    public ResponseEntity<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CertNewsTaskLog> logs = taskService.getRecentLogs(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近日志失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近日志失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取爬虫类型列表
     */
    @GetMapping("/crawler-types")
    public ResponseEntity<Map<String, Object>> getCrawlerTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", List.of(
            Map.of("value", "SGS", "label", "SGS 认证机构"),
            Map.of("value", "UL", "label", "UL Solutions"),
            Map.of("value", "BEICE", "label", "北测检测")
        ));
        return ResponseEntity.ok(response);
    }
}

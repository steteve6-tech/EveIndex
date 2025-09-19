package com.certification.controller;

import org.springframework.data.domain.Page;
import com.certification.entity.notification.Notification;
import com.certification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取通知列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isSent,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            Page<Notification> notificationsPage = notificationService.getNotifications(type, null, isSent, page, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationsPage.getContent());
            response.put("total", notificationsPage.getTotalElements());
            response.put("page", page);
            response.put("limit", limit);
            response.put("total_pages", notificationsPage.getTotalPages());
            
            // 获取通知统计
            Map<String, Object> notificationStats = notificationService.getNotificationStatistics();
            response.put("notification_statistics", notificationStats);
            
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "查询通知列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取待发送的通知
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingNotifications() {
        try {
            List<Notification> pendingNotifications = notificationService.getPendingNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("pending_notifications", pendingNotifications);
            response.put("count", pendingNotifications.size());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取待发送通知失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 获取通知统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {
        try {
            Map<String, Object> stats = notificationService.getNotificationStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取通知统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 创建邮件通知
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> createEmailNotification(
            @RequestBody Map<String, String> request) {
        
        try {
            String title = request.get("title");
            String content = request.get("content");
            String recipientEmail = request.get("recipientEmail");
            String priorityStr = request.get("priority");
            
            if (title == null || content == null || recipientEmail == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必要参数: title, content, recipientEmail");
                return ResponseEntity.badRequest().body(error);
            }
            
            Notification.Priority priority = Notification.Priority.NORMAL;
            if (priorityStr != null) {
                try {
                    priority = Notification.Priority.valueOf(priorityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 使用默认优先级
                }
            }
            
            Notification notification = notificationService.createEmailNotification(
                title, content, recipientEmail, priority);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "邮件通知已创建");
            response.put("notification_id", notification.getId());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建邮件通知失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 创建Webhook通知
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> createWebhookNotification(
            @RequestBody Map<String, String> request) {
        
        try {
            String title = request.get("title");
            String content = request.get("content");
            String webhookUrl = request.get("webhookUrl");
            String priorityStr = request.get("priority");
            
            if (title == null || content == null || webhookUrl == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必要参数: title, content, webhookUrl");
                return ResponseEntity.badRequest().body(error);
            }
            
            Notification.Priority priority = Notification.Priority.NORMAL;
            if (priorityStr != null) {
                try {
                    priority = Notification.Priority.valueOf(priorityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 使用默认优先级
                }
            }
            
            Notification notification = notificationService.createWebhookNotification(
                title, content, webhookUrl, priority);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Webhook通知已创建");
            response.put("notification_id", notification.getId());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建Webhook通知失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "通知已删除");
            response.put("notification_id", id);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除通知失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 重新发送失败的通知
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<Map<String, Object>> retryFailedNotifications() {
        try {
            notificationService.retryFailedNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "失败通知重新发送任务已启动");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "重新发送失败通知失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

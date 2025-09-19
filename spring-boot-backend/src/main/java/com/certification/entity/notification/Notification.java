package com.certification.entity.notification;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_notification")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "通知实体")
public class Notification {
    
    @Schema(description = "通知ID", example = "1")
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "通知标题", example = "系统通知")
    @Column(name = "title")
    private String title;
    
    @Schema(description = "通知内容")
    @Column(name = "content")
    private String content;
    
    @Schema(description = "通知类型", example = "SYSTEM")
    @Column(name = "notification_type")
    private NotificationType notificationType;
    
    @Schema(description = "优先级", example = "NORMAL")
    @Column(name = "priority")
    private Priority priority;
    
    @Schema(description = "是否已读", example = "false")
    @Column(name = "is_read")
    private Boolean isRead;
    
    @Schema(description = "阅读时间")
    @Column(name = "read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
    
    @Schema(description = "接收人", example = "admin")
    @Column(name = "recipient")
    private String recipient;
    
    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Schema(description = "逻辑删除标记")
    @Column(name = "deleted")
    private Integer deleted = 0;
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        SYSTEM("系统通知"),
        CRAWLER("爬虫通知"),
        MAINTENANCE("维护通知"),
        ALERT("告警通知");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");
        
        private final String description;
        
        Priority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}


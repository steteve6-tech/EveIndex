package com.certification.entity.notification;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_system_log")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "系统日志实体")
public class SystemLog {
    
    @Schema(description = "日志ID", example = "1")
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "日志类型", example = "SYSTEM_STARTUP")
    @Column(name = "log_type")
    private LogType logType;
    
    @Schema(description = "日志级别", example = "INFO")
    @Column(name = "log_level")
    private LogLevel logLevel;
    
    @Schema(description = "日志状态", example = "SUCCESS")
    @Column(name = "log_status")
    private LogStatus logStatus;
    
    @Schema(description = "日志消息")
    @Column(name = "message")
    private String message;
    
    @Schema(description = "详细信息")
    @Column(name = "details")
    private String details;
    
    @Schema(description = "IP地址", example = "127.0.0.1")
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Schema(description = "用户代理")
    @Column(name = "user_agent")
    private String userAgent;
    
    @Schema(description = "执行时间(毫秒)", example = "100")
    @Column(name = "execution_time")
    private Long executionTime;
    
    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "逻辑删除标记")
    @Column(name = "deleted")
    private Integer deleted = 0;
    
    /**
     * 日志类型枚举
     */
    public enum LogType {
        SYSTEM_STARTUP("系统启动"),
        DATA_CRAWL("数据爬取"),
        USER_LOGIN("用户登录"),
        DATA_CLEANUP("数据清理"),
        API_CALL("API调用"),
        ERROR("错误日志");
        
        private final String description;
        
        LogType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG("调试"),
        INFO("信息"),
        WARN("警告"),
        ERROR("错误"),
        FATAL("致命");
        
        private final String description;
        
        LogLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 日志状态枚举
     */
    public enum LogStatus {
        SUCCESS("成功"),
        FAILED("失败"),
        IN_PROGRESS("进行中"),
        CANCELLED("已取消");
        
        private final String description;
        
        LogStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}


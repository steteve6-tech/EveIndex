package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 设备匹配关键词实体类
 * 用于DeviceData.vue中的统一关键词搜索功能
 * 对应数据库表：t_devicematch_keywords
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_devicematch_keywords", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"keyword", "keyword_type"}))
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备匹配关键词实体")
public class DeviceMatchKeywords {

    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "关键词内容")
    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Schema(description = "关键词类型")
    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", nullable = false)
    private KeywordType keywordType = KeywordType.NORMAL;

    @Schema(description = "是否启用")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 关键词类型枚举
     */
    public enum KeywordType {
        NORMAL("normal", "普通关键词"),
        BLACKLIST("blacklist", "黑名单关键词");

        private final String code;
        private final String description;

        KeywordType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}

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
 * 关键词实体类
 * 用于存储和管理关键词列表
 * 对应数据库表：t_keyword
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_keyword")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "关键词实体")
public class Keyword {

    @Schema(description = "关键词ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关键词内容
     */
    @Schema(description = "关键词内容", example = "RoHS2.0")
    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;

    /**
     * 关键词描述
     */
    @Schema(description = "关键词描述", example = "欧盟有害物质限制指令")
    @Column(name = "description")
    private String description;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 排序权重
     */
    @Schema(description = "排序权重", example = "1")
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

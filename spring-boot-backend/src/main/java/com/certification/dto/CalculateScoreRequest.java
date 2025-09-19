package com.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;



/**
 * 计算分数请求DTO
 */
@Data
@Schema(description = "计算风险分数请求")
public class CalculateScoreRequest {

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "国家ID", example = "2")
    private Long countryId;

    @Schema(description = "是否保存到数据库", example = "true")
    private Boolean saveToDatabase = true;
}

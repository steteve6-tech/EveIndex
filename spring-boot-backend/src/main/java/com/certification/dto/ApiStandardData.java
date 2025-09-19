package com.certification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiStandardData {
    private String standardNumber;
    private String version;
    private String title;
    private String description;
    private String publishedDate;
    private String effectiveDate;
    private String downloadUrl;
    private String scope;
    private String status;
    private String transitionEnd;
    private String category;
    private List<String> tech;
    
    // 保留带参数的构造函数
    public ApiStandardData(String standardNumber) {
        this.standardNumber = standardNumber;
    }
}

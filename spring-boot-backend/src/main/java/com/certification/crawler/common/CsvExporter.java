package com.certification.crawler.common;

import com.certification.crawler.countrydata.customs.base.CustomsCrawlerResult;
import com.certification.crawler.countrydata.medical.base.MedicalCrawlerResult;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * CSV导出工具类
 */
@Component
public class CsvExporter {
    
    /**
     * 导出数据到CSV文件
     * @param data 数据列表
     * @param headers 表头
     * @param filePath 文件路径
     * @throws IOException 文件写入异常
     */
    public void exportToCsv(List<Map<String, String>> data, List<String> headers, String filePath) throws IOException {
        exportToCsv(data, headers, filePath, ",");
    }
    
    /**
     * 导出数据到CSV文件（自定义分隔符）
     * @param data 数据列表
     * @param headers 表头
     * @param filePath 文件路径
     * @param delimiter 分隔符
     * @throws IOException 文件写入异常
     */
    public void exportToCsv(List<Map<String, String>> data, List<String> headers, String filePath, String delimiter) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // 写入表头
            writer.write(String.join(delimiter, headers));
            writer.write("\n");
            
            // 写入数据
            for (Map<String, String> row : data) {
                List<String> values = headers.stream()
                        .map(header -> escapeCsvValue(row.getOrDefault(header, "")))
                        .toList();
                writer.write(String.join(delimiter, values));
                writer.write("\n");
            }
        }
    }
    
    /**
     * 导出简单数据到CSV文件
     * @param data 数据列表（每行是一个字符串数组）
     * @param headers 表头
     * @param filePath 文件路径
     * @throws IOException 文件写入异常
     */
    public void exportSimpleToCsv(List<String[]> data, String[] headers, String filePath) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        
        // 添加表头
        lines.add(String.join(",", headers));
        
        // 添加数据行
        for (String[] row : data) {
            String[] escapedRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                escapedRow[i] = escapeCsvValue(row[i] != null ? row[i] : "");
            }
            lines.add(String.join(",", escapedRow));
        }
        
        Files.write(Path.of(filePath), lines);
    }
    
    /**
     * 转义CSV值
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果值包含逗号、引号或换行符，需要用引号包围
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // 将值中的引号替换为两个引号
            String escapedValue = value.replace("\"", "\"\"");
            return "\"" + escapedValue + "\"";
        }
        
        return value;
    }
    
    /**
     * 导出认证信息爬虫结果到CSV
     * @param results 爬虫结果列表
     * @param filePath 文件路径
     * @throws IOException 文件写入异常
     */
    public void exportCertificationResults(List<com.certification.crawler.certification.base.CrawlerResult> results, String filePath) throws IOException {
        List<String> headers = List.of("title", "url", "content", "date", "source", "category", "country", "type");
        
        List<Map<String, String>> data = results.stream()
                .map(result -> Map.of(
                        "title", result.getTitle() != null ? result.getTitle() : "",
                        "url", result.getUrl() != null ? result.getUrl() : "",
                        "content", result.getContent() != null ? result.getContent() : "",
                        "date", result.getDate() != null ? result.getDate() : "",
                        "source", result.getSource() != null ? result.getSource() : "",
                        "category", result.getCategory() != null ? result.getCategory() : "",
                        "country", result.getCountry() != null ? result.getCountry() : "",
                        "type", result.getType() != null ? result.getType() : ""
                ))
                .toList();
        
        exportToCsv(data, headers, filePath);
    }
    
    /**
     * 导出海关信息爬虫结果到CSV
     * @param results 爬虫结果列表
     * @param filePath 文件路径
     * @throws IOException 文件写入异常
     */
    public void exportCustomsResults(List<CustomsCrawlerResult> results, String filePath) throws IOException {
        List<String> headers = List.of("title", "url", "content", "publishDate", "country", "customsOffice", 
                "documentType", "category", "source", "documentNumber", "effectiveDate", "status");
        
        List<Map<String, String>> data = results.stream()
                .map(result -> {
                    Map<String, String> map = new java.util.HashMap<>();
                    map.put("title", result.getTitle() != null ? result.getTitle() : "");
                    map.put("url", result.getUrl() != null ? result.getUrl() : "");
                    map.put("content", result.getContent() != null ? result.getContent() : "");
                    map.put("publishDate", result.getPublishDate() != null ? result.getPublishDate() : "");
                    map.put("country", result.getCountry() != null ? result.getCountry() : "");
                    map.put("customsOffice", result.getCustomsOffice() != null ? result.getCustomsOffice() : "");
                    map.put("documentType", result.getDocumentType() != null ? result.getDocumentType() : "");
                    map.put("category", result.getCategory() != null ? result.getCategory() : "");
                    map.put("source", result.getSource() != null ? result.getSource() : "");
                    map.put("documentNumber", result.getDocumentNumber() != null ? result.getDocumentNumber() : "");
                    map.put("effectiveDate", result.getEffectiveDate() != null ? result.getEffectiveDate() : "");
                    map.put("status", result.getStatus() != null ? result.getStatus() : "");
                    return map;
                })
                .toList();
        
        exportToCsv(data, headers, filePath);
    }
    
    /**
     * 导出医疗器械信息爬虫结果到CSV
     * @param results 爬虫结果列表
     * @param filePath 文件路径
     * @throws IOException 文件写入异常
     */
    public void exportMedicalResults(List<MedicalCrawlerResult> results, String filePath) throws IOException {
        List<String> headers = List.of("title", "url", "content", "publishDate", "country", "regulatoryAuthority",
                "documentType", "deviceCategory", "riskLevel", "source", "documentNumber", "effectiveDate", 
                "status", "manufacturer", "productName", "approvalNumber");
        
        List<Map<String, String>> data = results.stream()
                .map(result -> {
                    Map<String, String> map = new java.util.HashMap<>();
                    map.put("title", result.getTitle() != null ? result.getTitle() : "");
                    map.put("url", result.getUrl() != null ? result.getUrl() : "");
                    map.put("content", result.getContent() != null ? result.getContent() : "");
                    map.put("publishDate", result.getPublishDate() != null ? result.getPublishDate() : "");
                    map.put("country", result.getCountry() != null ? result.getCountry() : "");
                    map.put("regulatoryAuthority", result.getRegulatoryAuthority() != null ? result.getRegulatoryAuthority() : "");
                    map.put("documentType", result.getDocumentType() != null ? result.getDocumentType() : "");
                    map.put("deviceCategory", result.getDeviceCategory() != null ? result.getDeviceCategory() : "");
                    map.put("riskLevel", result.getRiskLevel() != null ? result.getRiskLevel() : "");
                    map.put("source", result.getSource() != null ? result.getSource() : "");
                    map.put("documentNumber", result.getDocumentNumber() != null ? result.getDocumentNumber() : "");
                    map.put("effectiveDate", result.getEffectiveDate() != null ? result.getEffectiveDate() : "");
                    map.put("status", result.getStatus() != null ? result.getStatus() : "");
                    map.put("manufacturer", result.getManufacturer() != null ? result.getManufacturer() : "");
                    map.put("productName", result.getProductName() != null ? result.getProductName() : "");
                    map.put("approvalNumber", result.getApprovalNumber() != null ? result.getApprovalNumber() : "");
                    return map;
                })
                .toList();
        
        exportToCsv(data, headers, filePath);
    }
}

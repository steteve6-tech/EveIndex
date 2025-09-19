package com.certification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的JSON数据分析器
 * 用于分析FDA API返回的JSON数据
 */
public class SimpleJsonAnalyzer {
    
    public static void main(String[] args) {
        System.out.println("=== FDA JSON数据分析器 ===");
        
        // 分析设备注册信息
        analyzeDeviceRegistration("device_reglist_results.json");
        
        // 分析设备不良事件
        analyzeDeviceEvents("device_events_xray.json");
        
        // 分析设备上市前审批
        analyzeDevicePMA("device_pma_approved.json");
        
        // 分析设备510k审批
        analyzeDevice510K("device_510k_medtronic.json");
        
        System.out.println("\n=== 分析完成 ===");
    }
    
    /**
     * 分析设备注册信息数据
     */
    public static void analyzeDeviceRegistration(String filename) {
        System.out.println("\n=== 设备注册信息数据分析 ===");
        
        if (!new File(filename).exists()) {
            System.out.println("文件 " + filename + " 不存在，跳过分析");
            return;
        }
        
        try {
            String content = readFileContent(filename);
            
            // 统计国家代码
            Map<String, Integer> countryStats = new HashMap<>();
            Pattern countryPattern = Pattern.compile("\"iso_country_code\"\\s*:\\s*\"([^\"]+)\"");
            Matcher countryMatcher = countryPattern.matcher(content);
            
            while (countryMatcher.find()) {
                String country = countryMatcher.group(1);
                countryStats.put(country, countryStats.getOrDefault(country, 0) + 1);
            }
            
            System.out.println("国家分布 (前10):");
            countryStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计产品代码
            Map<String, Integer> productCodeStats = new HashMap<>();
            Pattern productPattern = Pattern.compile("\"product_code\"\\s*:\\s*\"([^\"]+)\"");
            Matcher productMatcher = productPattern.matcher(content);
            
            while (productMatcher.find()) {
                String productCode = productMatcher.group(1);
                productCodeStats.put(productCode, productCodeStats.getOrDefault(productCode, 0) + 1);
            }
            
            System.out.println("\n产品代码分布 (前10):");
            productCodeStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计记录总数
            Pattern recordPattern = Pattern.compile("\\{[^}]*\"registration_number\"[^}]*\\}");
            Matcher recordMatcher = recordPattern.matcher(content);
            int recordCount = 0;
            while (recordMatcher.find()) {
                recordCount++;
            }
            System.out.println("\n总记录数: " + recordCount);
            
        } catch (IOException e) {
            System.err.println("分析设备注册信息时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析设备不良事件数据
     */
    public static void analyzeDeviceEvents(String filename) {
        System.out.println("\n=== 设备不良事件数据分析 ===");
        
        if (!new File(filename).exists()) {
            System.out.println("文件 " + filename + " 不存在，跳过分析");
            return;
        }
        
        try {
            String content = readFileContent(filename);
            
            // 统计事件类型
            Map<String, Integer> eventTypeStats = new HashMap<>();
            Pattern eventTypePattern = Pattern.compile("\"event_type\"\\s*:\\s*\"([^\"]+)\"");
            Matcher eventTypeMatcher = eventTypePattern.matcher(content);
            
            while (eventTypeMatcher.find()) {
                String eventType = eventTypeMatcher.group(1);
                eventTypeStats.put(eventType, eventTypeStats.getOrDefault(eventType, 0) + 1);
            }
            
            System.out.println("事件类型分布:");
            eventTypeStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计制造商
            Map<String, Integer> manufacturerStats = new HashMap<>();
            Pattern manufacturerPattern = Pattern.compile("\"manufacturer_d_name\"\\s*:\\s*\"([^\"]+)\"");
            Matcher manufacturerMatcher = manufacturerPattern.matcher(content);
            
            while (manufacturerMatcher.find()) {
                String manufacturer = manufacturerMatcher.group(1);
                manufacturerStats.put(manufacturer, manufacturerStats.getOrDefault(manufacturer, 0) + 1);
            }
            
            System.out.println("\n制造商分布 (前10):");
            manufacturerStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计记录总数
            Pattern recordPattern = Pattern.compile("\\{[^}]*\"report_number\"[^}]*\\}");
            Matcher recordMatcher = recordPattern.matcher(content);
            int recordCount = 0;
            while (recordMatcher.find()) {
                recordCount++;
            }
            System.out.println("\n总记录数: " + recordCount);
            
        } catch (IOException e) {
            System.err.println("分析设备不良事件时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析设备上市前审批数据
     */
    public static void analyzeDevicePMA(String filename) {
        System.out.println("\n=== 设备上市前审批数据分析 ===");
        
        if (!new File(filename).exists()) {
            System.out.println("文件 " + filename + " 不存在，跳过分析");
            return;
        }
        
        try {
            String content = readFileContent(filename);
            
            // 统计决策代码
            Map<String, Integer> decisionCodeStats = new HashMap<>();
            Pattern decisionPattern = Pattern.compile("\"decision_code\"\\s*:\\s*\"([^\"]+)\"");
            Matcher decisionMatcher = decisionPattern.matcher(content);
            
            while (decisionMatcher.find()) {
                String decisionCode = decisionMatcher.group(1);
                decisionCodeStats.put(decisionCode, decisionCodeStats.getOrDefault(decisionCode, 0) + 1);
            }
            
            System.out.println("决策代码分布:");
            decisionCodeStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计申请人
            Map<String, Integer> applicantStats = new HashMap<>();
            Pattern applicantPattern = Pattern.compile("\"applicant\"\\s*:\\s*\"([^\"]+)\"");
            Matcher applicantMatcher = applicantPattern.matcher(content);
            
            while (applicantMatcher.find()) {
                String applicant = applicantMatcher.group(1);
                applicantStats.put(applicant, applicantStats.getOrDefault(applicant, 0) + 1);
            }
            
            System.out.println("\n申请人分布 (前10):");
            applicantStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计记录总数
            Pattern recordPattern = Pattern.compile("\\{[^}]*\"pma_number\"[^}]*\\}");
            Matcher recordMatcher = recordPattern.matcher(content);
            int recordCount = 0;
            while (recordMatcher.find()) {
                recordCount++;
            }
            System.out.println("\n总记录数: " + recordCount);
            
        } catch (IOException e) {
            System.err.println("分析设备上市前审批时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析设备510k审批数据
     */
    public static void analyzeDevice510K(String filename) {
        System.out.println("\n=== 设备510k审批数据分析 ===");
        
        if (!new File(filename).exists()) {
            System.out.println("文件 " + filename + " 不存在，跳过分析");
            return;
        }
        
        try {
            String content = readFileContent(filename);
            
            // 统计决策代码
            Map<String, Integer> decisionCodeStats = new HashMap<>();
            Pattern decisionPattern = Pattern.compile("\"decision_code\"\\s*:\\s*\"([^\"]+)\"");
            Matcher decisionMatcher = decisionPattern.matcher(content);
            
            while (decisionMatcher.find()) {
                String decisionCode = decisionMatcher.group(1);
                decisionCodeStats.put(decisionCode, decisionCodeStats.getOrDefault(decisionCode, 0) + 1);
            }
            
            System.out.println("决策代码分布:");
            decisionCodeStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计申请人
            Map<String, Integer> applicantStats = new HashMap<>();
            Pattern applicantPattern = Pattern.compile("\"applicant\"\\s*:\\s*\"([^\"]+)\"");
            Matcher applicantMatcher = applicantPattern.matcher(content);
            
            while (applicantMatcher.find()) {
                String applicant = applicantMatcher.group(1);
                applicantStats.put(applicant, applicantStats.getOrDefault(applicant, 0) + 1);
            }
            
            System.out.println("\n申请人分布 (前10):");
            applicantStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            // 统计记录总数
            Pattern recordPattern = Pattern.compile("\\{[^}]*\"k_number\"[^}]*\\}");
            Matcher recordMatcher = recordPattern.matcher(content);
            int recordCount = 0;
            while (recordMatcher.find()) {
                recordCount++;
            }
            System.out.println("\n总记录数: " + recordCount);
            
        } catch (IOException e) {
            System.err.println("分析设备510k审批时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 读取文件内容
     */
    private static String readFileContent(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}

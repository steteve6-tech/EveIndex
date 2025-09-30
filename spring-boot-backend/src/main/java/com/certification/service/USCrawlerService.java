package com.certification.service;

import com.certification.crawler.countrydata.us.*;
import com.certification.crawler.countrydata.us.others.D_510K;
import com.certification.crawler.countrydata.us.others.D_recall;
import com.certification.crawler.countrydata.us.others.US_event_api;
import com.certification.crawler.countrydata.us.US_CustomsCase;
import com.certification.entity.common.CustomsCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 美国爬虫服务
 * 提供美国相关爬虫的统一调用接口
 */
@Slf4j
@Service
public class USCrawlerService {

    @Autowired
    private D_510K d510kCrawler;
    
    @Autowired
    private US_510K us510kCrawler;
    
    @Autowired
    private US_event_api usEventCrawler;
    
    @Autowired
    private US_recall_api usRecallCrawler;
    
    /**
     * 智能解析关键词字符串
     * 优先按逗号分割，保留包含空格的多词关键词
     */
    private List<String> parseKeywordsFromString(String keywordsStr) {
        if (keywordsStr == null || keywordsStr.trim().isEmpty()) {
            return Arrays.asList();
        }
        
        // 按逗号分割，然后去除每个关键词的前后空格
        return Arrays.stream(keywordsStr.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toList());
    }

    @Autowired
    private US_event dEventCrawler;

    @Autowired
    private D_recall dRecallCrawler;

    @Autowired
    private US_registration dRegistrationCrawler;

    @Autowired
    private unicrawl uniCrawler;

    @Autowired
    private US_CustomsCase USCustomsCase;

    @Autowired
    private US_Guidance USGuidance;

    /**
     * 执行D_510K爬虫测试
     */
    public Map<String, Object> testD510K(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行D_510K爬虫测试，参数: {}", params);
            
            // 提取参数
            String deviceName = (String) params.getOrDefault("deviceName", "");
            String applicantName = (String) params.getOrDefault("applicantName", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            Map<String, Object> crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}", inputKeywords.size());
                crawlResult = d510kCrawler.crawlFDADataWithKeywords(inputKeywords, dateFrom, dateTo, maxPages);
            } else {
                log.info("使用传统参数爬取 - 设备名称: {}, 申请人: {}, 日期范围: {} - {}, 最大页数: {}", 
                        deviceName, applicantName, dateFrom, dateTo, maxPages);
                crawlResult = d510kCrawler.crawlFDADataWithParams(deviceName, applicantName, dateFrom, dateTo, maxPages);
            }
            
            result.put("success", true);
            result.put("message", "D_510K爬虫测试成功");
            result.put("totalSaved", crawlResult.get("totalSaved"));
            result.put("totalSkipped", crawlResult.get("totalSkipped"));
            result.put("totalPages", crawlResult.get("totalPages"));
            result.put("data", crawlResult);
            
            log.info("D_510K爬虫测试完成，保存记录数: {}, 跳过记录数: {}, 总页数: {}", 
                    crawlResult.get("totalSaved"), crawlResult.get("totalSkipped"), crawlResult.get("totalPages"));
            
        } catch (Exception e) {
            log.error("D_510K爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "D_510K爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行US_510K爬虫测试
     */
    public Map<String, Object> testUS510K(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行US_510K爬虫测试，参数: {}", params);
            
            // 提取参数
            String deviceName = (String) params.getOrDefault("deviceName", "");
            String applicantName = (String) params.getOrDefault("applicantName", "");
            String tradeName = (String) params.getOrDefault("tradeName", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 计算maxRecords：如果maxPages为0，则传递-1表示爬取所有数据
            int maxRecords = (maxPages == 0) ? -1 : maxPages * 100;
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            String crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 爬取模式: {}", 
                        inputKeywords.size(), maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                crawlResult = us510kCrawler.crawlAndSaveWithKeywords(inputKeywords, maxRecords, 50, dateFrom, dateTo);
            } else {
                log.info("使用传统参数爬取 - 设备名称: {}, 申请人: {}, trade_name: {}, 日期范围: {} - {}, 爬取模式: {}", 
                        deviceName, applicantName, tradeName, dateFrom, dateTo, 
                        maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                
                // 根据参数选择搜索方式
                if (deviceName != null && !deviceName.trim().isEmpty()) {
                    crawlResult = us510kCrawler.crawlAndSaveByDeviceName(deviceName, maxRecords, 50, dateFrom, dateTo);
                } else if (applicantName != null && !applicantName.trim().isEmpty()) {
                    crawlResult = us510kCrawler.crawlAndSaveByApplicant(applicantName, maxRecords, 50, dateFrom, dateTo);
                } else if (tradeName != null && !tradeName.trim().isEmpty()) {
                    crawlResult = us510kCrawler.crawlAndSaveByTradeName(tradeName, maxRecords, 50, dateFrom, dateTo);
                } else {
                    crawlResult = us510kCrawler.crawlAndSaveDevice510K("device_name:medical", maxRecords, 50, dateFrom, dateTo);
                }
            }
            
            // 解析爬取结果中的统计信息
            int savedCount = 0;
            int skippedCount = 0;
            if (crawlResult != null && crawlResult.contains("保存成功:")) {
                try {
                    // 解析格式: "保存成功: X 条新记录, 跳过重复: Y 条"
                    String[] parts = crawlResult.split("保存成功: | 条新记录, 跳过重复: | 条");
                    if (parts.length >= 3) {
                        savedCount = Integer.parseInt(parts[1].trim());
                        skippedCount = Integer.parseInt(parts[2].trim());
                    }
                } catch (Exception e) {
                    log.warn("解析爬取结果统计信息失败: {}", e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "US_510K爬虫测试成功");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("savedCount", savedCount);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", savedCount + skippedCount);
            
            log.info("US_510K爬虫测试完成，数据库保存结果: {}", crawlResult);
            
        } catch (com.certification.exception.AllDataDuplicateException e) {
            log.warn("US_510K爬虫测试完成 - 所有数据均为重复数据: {}", e.getMessage());
            
            // 从异常消息中解析跳过的记录数
            int skippedCount = 0;
            if (e.getMessage() != null && e.getMessage().contains("连续")) {
                try {
                    // 解析格式: "连续 X 个批次都是重复数据，停止爬取"
                    String[] parts = e.getMessage().split("连续 | 个批次都是重复数据");
                    if (parts.length >= 2) {
                        int consecutiveBatches = Integer.parseInt(parts[1].trim());
                        // 估算跳过数量：每个批次大约50条记录
                        skippedCount = consecutiveBatches * 50;
                    }
                } catch (Exception parseException) {
                    log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "爬取完成 - 所有数据均为重复数据");
            result.put("crawlResult", e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", skippedCount);
            
        } catch (Exception e) {
            log.error("US_510K爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "US_510K爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行D_event爬虫测试
     */
    public Map<String, Object> testDEvent(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行D_event爬虫测试，参数: {}", params);
            
            // 提取参数
            String brandName = (String) params.getOrDefault("brandName", "");
            String manufacturer = (String) params.getOrDefault("manufacturer", "");
            String modelNumber = (String) params.getOrDefault("modelNumber", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            Map<String, Object> crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}", inputKeywords.size());
                crawlResult = dEventCrawler.crawlFDADataWithKeywords(inputKeywords, dateFrom, dateTo, maxPages);
            } else {
                log.info("使用传统参数爬取 - 品牌名称: {}, 制造商: {}, 型号: {}, 日期范围: {} - {}, 最大页数: {}", 
                        brandName, manufacturer, modelNumber, dateFrom, dateTo, maxPages);
                crawlResult = dEventCrawler.crawlMAUDEDataWithParams(brandName, manufacturer, modelNumber, dateFrom, dateTo, maxPages);
            }
            
            result.put("success", true);
            result.put("message", "D_event爬虫测试成功");
            result.put("totalSaved", crawlResult.get("totalSaved"));
            result.put("totalSkipped", crawlResult.get("totalSkipped"));
            result.put("totalPages", crawlResult.get("totalPages"));
            result.put("data", crawlResult);
            
            log.info("D_event爬虫测试完成，保存记录数: {}, 跳过记录数: {}, 总页数: {}", 
                    crawlResult.get("totalSaved"), crawlResult.get("totalSkipped"), crawlResult.get("totalPages"));
            
        } catch (Exception e) {
            log.error("D_event爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "D_event爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行US_event爬虫测试
     */
    public Map<String, Object> testUSEvent(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行US_event爬虫测试，参数: {}", params);
            
            // 提取参数
            String deviceName = (String) params.getOrDefault("deviceName", "");
            String manufacturer = (String) params.getOrDefault("manufacturer", "");
            String productProblem = (String) params.getOrDefault("productProblem", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 计算maxRecords：如果maxPages为0，则传递-1表示爬取所有数据
            int maxRecords = (maxPages == 0) ? -1 : maxPages * 100;
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            String crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 爬取模式: {}", 
                        inputKeywords.size(), maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                crawlResult = usEventCrawler.crawlAndSaveWithKeywords(inputKeywords, maxRecords, 50, dateFrom, dateTo);
            } else {
                log.info("使用传统参数爬取 - 设备名称: {}, 制造商: {}, 产品问题: {}, 日期范围: {} - {}, 爬取模式: {}", 
                        deviceName, manufacturer, productProblem, dateFrom, dateTo, 
                        maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                
                // 根据参数选择搜索方式
                if (deviceName != null && !deviceName.trim().isEmpty()) {
                    crawlResult = usEventCrawler.crawlAndSaveByDeviceName(deviceName, maxRecords, 50, dateFrom, dateTo);
                } else if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                    crawlResult = usEventCrawler.crawlAndSaveByManufacturer(manufacturer, maxRecords, 50, dateFrom, dateTo);
                } else if (productProblem != null && !productProblem.trim().isEmpty()) {
                    crawlResult = usEventCrawler.crawlAndSaveByProductProblem(productProblem, maxRecords, 50, dateFrom, dateTo);
                } else {
                    crawlResult = usEventCrawler.crawlAndSaveDeviceEvent("device.device_name:medical", maxRecords, 50, dateFrom, dateTo);
                }
            }
            
            // 解析爬取结果中的统计信息
            int savedCount = 0;
            int skippedCount = 0;
            if (crawlResult != null && crawlResult.contains("保存成功:")) {
                try {
                    // 解析格式: "保存成功: X 条新记录, 跳过重复: Y 条"
                    String[] parts = crawlResult.split("保存成功: | 条新记录, 跳过重复: | 条");
                    if (parts.length >= 3) {
                        savedCount = Integer.parseInt(parts[1].trim());
                        skippedCount = Integer.parseInt(parts[2].trim());
                    }
                } catch (Exception e) {
                    log.warn("解析爬取结果统计信息失败: {}", e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "US_event爬虫测试成功");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("savedCount", savedCount);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", savedCount + skippedCount);
            
            log.info("US_event爬虫测试完成，数据库保存结果: {}", crawlResult);
            
        } catch (com.certification.exception.AllDataDuplicateException e) {
            log.warn("US_event爬虫测试完成 - 所有数据均为重复数据: {}", e.getMessage());
            
            // 从异常消息中解析跳过的记录数
            int skippedCount = 0;
            if (e.getMessage() != null && e.getMessage().contains("跳过记录数:")) {
                try {
                    // 解析格式: "批次数据全部重复，停止爬取。跳过记录数: 10"
                    String[] parts = e.getMessage().split("跳过记录数: |");
                    if (parts.length >= 2) {
                        skippedCount = Integer.parseInt(parts[1].trim());
                    }
                } catch (Exception parseException) {
                    log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "爬取完成 - 所有数据均为重复数据");
            result.put("crawlResult", e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", skippedCount);
            
        } catch (Exception e) {
            log.error("US_event爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "US_event爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行D_recall爬虫测试
     */
    public Map<String, Object> testDRecall(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行D_recall爬虫测试，参数: {}", params);
            
            // 提取参数
            String productName = (String) params.getOrDefault("productName", "");
            String reasonForRecall = (String) params.getOrDefault("reasonForRecall", "");
            String recallingFirm = (String) params.getOrDefault("recallingFirm", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            Map<String, Object> crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 日期范围: {} - {}, 最大页数: {}", 
                        inputKeywords.size(), dateFrom, dateTo, maxPages);
                crawlResult = dRecallCrawler.crawlFDARecallDataWithKeywords(inputKeywords, dateFrom, dateTo, maxPages);
            } else {
                // 使用传统参数化搜索
                log.info("使用传统参数搜索 - 产品名称: {}, 召回原因: {}, 召回公司: {}, 日期范围: {} - {}, 最大页数: {}", 
                        productName, reasonForRecall, recallingFirm, dateFrom, dateTo, maxPages);
                crawlResult = dRecallCrawler.crawlFDARecallDataWithParams(productName, reasonForRecall, recallingFirm, dateFrom, dateTo, maxPages);
            }
            
            result.put("success", true);
            result.put("message", "D_recall爬虫测试成功");
            result.put("totalSaved", crawlResult.get("totalSaved"));
            result.put("totalSkipped", crawlResult.get("totalSkipped"));
            result.put("totalPages", crawlResult.get("totalPages"));
            result.put("data", crawlResult);
            
            // 如果使用了关键词，添加关键词相关信息
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                result.put("keywordsProcessed", crawlResult.get("keywordsProcessed"));
            }
            
            log.info("D_recall爬虫测试完成，保存记录数: {}, 跳过记录数: {}, 总页数: {}", 
                    crawlResult.get("totalSaved"), crawlResult.get("totalSkipped"), crawlResult.get("totalPages"));
            
        } catch (Exception e) {
            log.error("D_recall爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "D_recall爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行US_recall_api爬虫测试
     */
    public Map<String, Object> testUSRecall(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行US_recall_api爬虫测试，参数: {}", params);
            
            // 提取参数
            String recallingFirm = (String) params.getOrDefault("recallingFirm", "");
            String brandName = (String) params.getOrDefault("brandName", "");
            String productDescription = (String) params.getOrDefault("productDescription", "");
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
            
            // 计算maxRecords：如果maxPages为0，则传递-1表示爬取所有数据
            int maxRecords = (maxPages == 0) ? -1 : maxPages * 100;
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }
            
            String crawlResult;
            
            // 如果有关键词列表，使用关键词爬取方法
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 爬取模式: {}", 
                        inputKeywords.size(), maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                crawlResult = usRecallCrawler.crawlAndSaveWithKeywords(inputKeywords, maxRecords, 50, dateFrom, dateTo);
            } else {
                log.info("使用传统参数爬取 - 召回公司: {}, brand name: {}, 产品描述: {}, 日期范围: {} - {}, 爬取模式: {}", 
                        recallingFirm, brandName, productDescription, dateFrom, dateTo, 
                        maxRecords == -1 ? "所有数据" : "限制数量(" + maxRecords + ")");
                
                // 根据参数选择搜索方式
                if (recallingFirm != null && !recallingFirm.trim().isEmpty()) {
                    crawlResult = usRecallCrawler.crawlAndSaveByRecallingFirm(recallingFirm, maxRecords, 50, dateFrom, dateTo);
                } else if (brandName != null && !brandName.trim().isEmpty()) {
                    crawlResult = usRecallCrawler.crawlAndSaveByBrandName(brandName, maxRecords, 50, dateFrom, dateTo);
                } else if (productDescription != null && !productDescription.trim().isEmpty()) {
                    crawlResult = usRecallCrawler.crawlAndSaveByProductDescription(productDescription, maxRecords, 50, dateFrom, dateTo);
                } else {
                    crawlResult = usRecallCrawler.crawlAndSaveDeviceRecall("recalling_firm:medical", maxRecords, 50, dateFrom, dateTo);
                }
            }
            
            // 解析爬取结果中的统计信息
            int savedCount = 0;
            int skippedCount = 0;
            if (crawlResult != null && crawlResult.contains("保存成功:")) {
                try {
                    // 解析格式: "保存成功: X 条新记录, 跳过重复: Y 条"
                    String[] parts = crawlResult.split("保存成功: | 条新记录, 跳过重复: | 条");
                    log.info("解析US_recall_api结果 - 原始字符串: '{}', 分割后部分数量: {}", crawlResult, parts.length);
                    for (int i = 0; i < parts.length; i++) {
                        log.info("parts[{}] = '{}'", i, parts[i]);
                    }
                    if (parts.length >= 3) {
                        savedCount = Integer.parseInt(parts[1].trim());
                        skippedCount = Integer.parseInt(parts[2].trim());
                        log.info("解析成功 - savedCount: {}, skippedCount: {}", savedCount, skippedCount);
                    } else {
                        log.warn("分割后部分数量不足，无法解析: {}", parts.length);
                    }
                } catch (Exception e) {
                    log.warn("解析爬取结果统计信息失败: {}", e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "US_recall_api爬虫测试成功");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("savedCount", savedCount);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", savedCount + skippedCount);
            
            log.info("US_recall_api爬虫测试完成，数据库保存结果: {}", crawlResult);
            
        } catch (com.certification.exception.AllDataDuplicateException e) {
            log.warn("US_recall_api爬虫测试完成 - 所有数据均为重复数据: {}", e.getMessage());
            
            // 从异常消息中解析跳过的记录数
            int skippedCount = 0;
            if (e.getMessage() != null && e.getMessage().contains("跳过记录数:")) {
                try {
                    // 解析格式: "批次数据全部重复，停止爬取。跳过记录数: 10"
                    String[] parts = e.getMessage().split("跳过记录数: |");
                    if (parts.length >= 2) {
                        skippedCount = Integer.parseInt(parts[1].trim());
                    }
                } catch (Exception parseException) {
                    log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "爬取完成 - 所有数据均为重复数据");
            result.put("crawlResult", e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", skippedCount);
            
        } catch (Exception e) {
            log.error("US_recall_api爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "US_recall_api爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行US_registration爬虫测试
     */
    public Map<String, Object> testUSRegistration(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行US_registration爬虫测试，参数: {}", params);

            // 提取参数
            String establishmentName = (String) params.getOrDefault("establishmentName", "");
            String proprietaryName = (String) params.getOrDefault("proprietaryName", "");
            String ownerOperatorName = (String) params.getOrDefault("ownerOperatorName", "");
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 0);

            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'",
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null",
                    inputKeywordsObj);

            List<String> inputKeywords = null;

            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
                log.info("inputKeywords作为List处理，数量: {}", inputKeywords.size());
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                log.info("inputKeywords作为String处理，内容: '{}', 长度: {}", keywordsStr, keywordsStr.length());
                if (!keywordsStr.trim().isEmpty()) {
                    // 智能分割关键词
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                    log.info("分割后的关键词列表，数量: {}", inputKeywords.size());
                }
            }

            String crawlResult;
            
            // 计算maxRecords：如果maxPages为0，则传递-1表示爬取所有数据
            int maxRecords = (maxPages == 0) ? -1 : maxPages * 100;

            // 如果有关键词列表，使用关键词爬取方法（复杂策略）
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}", inputKeywords.size());
                log.info("搜索策略: 每个关键词将依次作为专有名称、制造商名称、设备名称进行搜索");
                crawlResult = dRegistrationCrawler.crawlAndSaveWithKeywords(inputKeywords, maxRecords, 50);
            } else {
                log.info("使用专门搜索方法 - 机构/贸易名称: {}, 专有名称: {}, 所有者/经营者名称: {}, 最大页数: {}",
                        establishmentName, proprietaryName, ownerOperatorName, maxPages);

                // 使用专门的搜索方法，按优先级选择搜索类型
                if (proprietaryName != null && !proprietaryName.trim().isEmpty()) {
                    log.info("使用专有名称搜索: {}", proprietaryName);
                    crawlResult = dRegistrationCrawler.crawlAndSaveByProprietaryName(proprietaryName, maxRecords, 50);
                } else if (ownerOperatorName != null && !ownerOperatorName.trim().isEmpty()) {
                    log.info("使用制造商名称搜索: {}", ownerOperatorName);
                    crawlResult = dRegistrationCrawler.crawlAndSaveByManufacturerName(ownerOperatorName, maxRecords, 50);
                } else if (establishmentName != null && !establishmentName.trim().isEmpty()) {
                    log.info("使用设备名称搜索: {}", establishmentName);
                    crawlResult = dRegistrationCrawler.crawlAndSaveByDeviceName(establishmentName, maxRecords, 50);
                } else {
                    log.warn("没有提供有效的搜索参数，使用默认搜索");
                    crawlResult = dRegistrationCrawler.crawlAndSaveDeviceRegistration("device_name:medical", maxRecords, 50);
                }
            }

            // 解析爬取结果中的统计信息
            int savedCount = 0;
            int skippedCount = 0;
            if (crawlResult != null && crawlResult.contains("保存成功:")) {
                try {
                    // 解析格式: "保存成功: X 条新记录, 跳过重复: Y 条"
                    String[] parts = crawlResult.split("保存成功: | 条新记录, 跳过重复: | 条");
                    log.info("解析US_registration结果 - 原始字符串: '{}', 分割后部分数量: {}", crawlResult, parts.length);
                    for (int i = 0; i < parts.length; i++) {
                        log.info("parts[{}] = '{}'", i, parts[i]);
                    }
                    if (parts.length >= 3) {
                        savedCount = Integer.parseInt(parts[1].trim());
                        skippedCount = Integer.parseInt(parts[2].trim());
                        log.info("解析成功 - savedCount: {}, skippedCount: {}", savedCount, skippedCount);
                    } else {
                        log.warn("分割后部分数量不足，无法解析: {}", parts.length);
                    }
                } catch (Exception e) {
                    log.warn("解析爬取结果统计信息失败: {}", e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "US_registration爬虫测试成功，数据已保存到数据库");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("savedCount", savedCount);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", savedCount + skippedCount);

            log.info("US_registration爬虫测试完成，数据库保存结果: {}", crawlResult);

        } catch (com.certification.exception.AllDataDuplicateException e) {
            log.warn("US_registration爬虫测试完成 - 所有数据均为重复数据: {}", e.getMessage());
            
            // 从异常消息中解析跳过的记录数
            int skippedCount = 0;
            if (e.getMessage() != null && e.getMessage().contains("跳过记录数:")) {
                try {
                    // 解析格式: "批次数据全部重复，停止爬取。跳过记录数: 10"
                    String[] parts = e.getMessage().split("跳过记录数: |");
                    if (parts.length >= 2) {
                        skippedCount = Integer.parseInt(parts[1].trim());
                    }
                } catch (Exception parseException) {
                    log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "爬取完成 - 所有数据均为重复数据");
            result.put("crawlResult", e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", skippedCount);
            result.put("totalProcessed", skippedCount);

        } catch (Exception e) {
            log.error("US_registration爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "US_registration爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 执行unicrawl爬虫测试
     */
    public Map<String, Object> testUnicrawl(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行unicrawl爬虫测试，参数: {}", params);
            
            // 提取参数
            Integer totalCount = (Integer) params.getOrDefault("totalCount", 50);
            String dateFrom = (String) params.getOrDefault("dateFrom", "");
            String dateTo = (String) params.getOrDefault("dateTo", "");
            @SuppressWarnings("unchecked")
            List<String> inputKeywords = (List<String>) params.getOrDefault("inputKeywords", null);
            Integer maxPages = (Integer) params.getOrDefault("maxPages", 0);
            
            log.info("爬取参数 - 总数量: {}, 日期范围: {} - {}, 输入关键词: {}, 最大页数: {}", 
                    totalCount, dateFrom, dateTo, inputKeywords, maxPages);
            
            // 调用新的爬虫方法
            var crawlResult = uniCrawler.crawlAllCrawlers(inputKeywords, dateFrom, dateTo, maxPages, totalCount);
            
            result.put("success", true);
            result.put("message", "unicrawl爬虫测试成功");
            result.put("totalSaved", crawlResult.size());
            result.put("totalSkipped", 0);
            result.put("totalPages", 1);
            result.put("data", crawlResult);
            
            log.info("unicrawl爬虫测试完成，结果: {}", crawlResult);
            
        } catch (Exception e) {
            log.error("unicrawl爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "unicrawl爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行CustomsCaseCrawler爬虫测试
     */
    public Map<String, Object> testCustomsCase(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        String hsCode = (String) params.getOrDefault("hsCode", "9018");
        int maxRecords = (Integer) params.getOrDefault("maxRecords", 10);
        int batchSize = (Integer) params.getOrDefault("batchSize", 10);
        String startDate = (String) params.getOrDefault("startDate", null);
        
        try {
            log.info("开始执行CustomsCaseCrawler爬虫测试，参数: {}", params);
            
            // 检查是否有关键词参数
            Object inputKeywordsObj = params.get("inputKeywords");
            log.info("inputKeywordsObj详情 - 类型: {}, 值: '{}'", 
                    inputKeywordsObj != null ? inputKeywordsObj.getClass().getSimpleName() : "null", 
                    inputKeywordsObj);
            
            List<String> inputKeywords = null;
            
            if (inputKeywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tempList = (List<String>) inputKeywordsObj;
                inputKeywords = tempList;
            } else if (inputKeywordsObj instanceof String) {
                String keywordsStr = (String) inputKeywordsObj;
                if (!keywordsStr.trim().isEmpty()) {
                    inputKeywords = parseKeywordsFromString(keywordsStr);
                }
            }
            
            log.info("解析后的关键词列表: {}", inputKeywords);
            
            log.info("爬取参数 - HS编码: {}, 最大记录数: {}, 批次大小: {}, 开始日期: {}, 关键词数量: {}", 
                    hsCode, maxRecords, batchSize, startDate, inputKeywords != null ? inputKeywords.size() : 0);
            
            if (inputKeywords != null && !inputKeywords.isEmpty()) {
                log.info("使用关键词列表爬取模式");
                log.info("搜索策略: 每个关键词将依次作为HS编码、关键词进行搜索");
                String crawlResult = USCustomsCase.crawlWithKeywords(inputKeywords, maxRecords, batchSize, startDate, null);
                
                // 解析爬取结果中的统计信息
                int savedCount = 0;
                if (crawlResult != null && crawlResult.contains("总获取记录数:")) {
                    try {
                        // 解析格式: "关键词列表爬取完成，处理关键词数: X, 总获取记录数: Y"
                        String[] parts = crawlResult.split("总获取记录数: |");
                        if (parts.length >= 2) {
                            savedCount = Integer.parseInt(parts[1].trim());
                        }
                    } catch (Exception e) {
                        log.warn("解析爬取结果统计信息失败: {}", e.getMessage());
                    }
                }
                
                result.put("success", true);
                result.put("message", "CustomsCaseCrawler关键词爬取成功");
                result.put("crawlResult", crawlResult);
                result.put("savedCount", savedCount);
                result.put("skippedCount", 0); // 关键词爬取方法不返回跳过数量
                result.put("totalProcessed", savedCount);
                result.put("keywordsProcessed", inputKeywords.size());
                result.put("keywords", inputKeywords);
                
                log.info("CustomsCaseCrawler关键词爬取完成，处理关键词数: {}, 获取记录数: {}", inputKeywords.size(), savedCount);
            } else {
                log.info("使用HS编码爬取模式");
                try {
                    List<CustomsCase> crawlResult = USCustomsCase.crawlAndSaveCustomsCases(hsCode, maxRecords, batchSize);
                    
                    result.put("success", true);
                    result.put("message", "CustomsCaseCrawler HS编码爬取成功");
                    result.put("crawlResult", "成功爬取 " + crawlResult.size() + " 条记录");
                    result.put("savedCount", crawlResult.size());
                    result.put("skippedCount", 0); // HS编码爬取不返回跳过数量
                    result.put("totalProcessed", crawlResult.size());
                    result.put("hsCode", hsCode);
                    result.put("maxRecords", maxRecords);
                    result.put("batchSize", batchSize);
                    result.put("startDate", startDate);
                    
                    log.info("CustomsCaseCrawler HS编码爬取完成，HS编码: {}, 最大记录数: {}, 保存记录数: {}", hsCode, maxRecords, crawlResult.size());
                } catch (com.certification.exception.AllDataDuplicateException e) {
                    log.warn("CustomsCaseCrawler HS编码爬取完成 - 所有数据均为重复数据: {}", e.getMessage());
                    
                    // 从异常消息中解析跳过的记录数
                    int skippedCount = 0;
                    if (e.getMessage() != null && e.getMessage().contains("跳过记录数:")) {
                        try {
                            // 解析格式: "批次数据全部重复，停止爬取。跳过记录数: 10"
                            String[] parts = e.getMessage().split("跳过记录数: |");
                            if (parts.length >= 2) {
                                skippedCount = Integer.parseInt(parts[1].trim());
                            }
                        } catch (Exception parseException) {
                            log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                        }
                    }
                    
                    result.put("success", true);
                    result.put("message", "爬取完成 - 所有数据均为重复数据");
                    result.put("crawlResult", e.getMessage());
                    result.put("savedCount", 0);
                    result.put("skippedCount", skippedCount);
                    result.put("totalProcessed", skippedCount);
                    result.put("hsCode", hsCode);
                    result.put("maxRecords", maxRecords);
                    result.put("batchSize", batchSize);
                    result.put("startDate", startDate);
                }
            }
            
        } catch (Exception e) {
            log.error("CustomsCaseCrawler爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "CustomsCaseCrawler爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行GuidanceCrawler爬虫测试
     */
    public Map<String, Object> testGuidance(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始执行GuidanceCrawler爬虫测试，参数: {}", params);
            
            Integer maxRecords = (Integer) params.getOrDefault("maxRecords", 10);
            
            log.info("爬取参数 - 最大记录数: {}", maxRecords);
            
            // 调用GuidanceCrawler爬虫
            USGuidance.crawlWithLimit(maxRecords);
            
            result.put("success", true);
            result.put("message", "GuidanceCrawler爬虫测试成功");
            result.put("crawlResult", "爬取完成，最大记录数: " + maxRecords);
            result.put("maxRecords", maxRecords);
            
            log.info("GuidanceCrawler爬虫测试完成，最大记录数: {}", maxRecords);
            
        } catch (RuntimeException e) {
            // 检查是否是"批次数据全部重复"的RuntimeException
            if (e.getMessage() != null && e.getMessage().contains("批次数据全部重复")) {
                log.warn("GuidanceCrawler爬虫测试完成 - 所有数据均为重复数据: {}", e.getMessage());
                
                // 从异常消息中解析跳过的记录数
                int skippedCount = 0;
                if (e.getMessage().contains("跳过记录数:")) {
                    try {
                        // 解析格式: "批次数据全部重复，停止爬取。跳过记录数: 10"
                        String[] parts = e.getMessage().split("跳过记录数: |");
                        if (parts.length >= 2) {
                            skippedCount = Integer.parseInt(parts[1].trim());
                        }
                    } catch (Exception parseException) {
                        log.warn("解析跳过记录数失败: {}", parseException.getMessage());
                    }
                }
                
                result.put("success", true);
                result.put("message", "爬取完成 - 所有数据均为重复数据");
                result.put("crawlResult", e.getMessage());
                result.put("savedCount", 0);
                result.put("skippedCount", skippedCount);
                result.put("totalProcessed", skippedCount);
            } else {
                log.error("GuidanceCrawler爬虫测试失败", e);
                result.put("success", false);
                result.put("message", "GuidanceCrawler爬虫测试失败: " + e.getMessage());
                result.put("error", e.getMessage());
            }
        } catch (Exception e) {
            log.error("GuidanceCrawler爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "GuidanceCrawler爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}

package com.certification.controller;

import com.certification.crawler.countrydata.eu.Eu_BTI;
import com.certification.crawler.countrydata.eu.Eu_registration;
import com.certification.crawler.countrydata.eu.EuSafetyGateAlertCrawler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 欧盟爬虫控制器
 * 提供欧盟相关爬虫的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/eu-crawler")
@Tag(name = "欧盟爬虫", description = "欧盟相关爬虫API接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
public class EUCrawlerController {

    @Autowired
    private Eu_BTI euBTICrawler;

    @Autowired
    private Eu_registration euRegistrationCrawler;

    @Autowired
    private EuSafetyGateAlertCrawler euSafetyGateAlertCrawler;

    /**
     * BTI爬虫参数化搜索
     */
    @PostMapping("/bti/search")
    @Operation(summary = "BTI参数化搜索", description = "按多种参数搜索欧盟绑定关税信息数据")
    public ResponseEntity<Map<String, Object>> searchBTI(
            @Parameter(description = "发布国家") @RequestParam(required = false) String refCountry,
            @Parameter(description = "BTI参考号") @RequestParam(required = false) String reference,
            @Parameter(description = "有效期开始日期(DD/MM/YYYY)") @RequestParam(required = false) String valStartDate,
            @Parameter(description = "有效期开始日期结束(DD/MM/YYYY)") @RequestParam(required = false) String valStartDateTo,
            @Parameter(description = "有效期结束日期(DD/MM/YYYY)") @RequestParam(required = false) String valEndDate,
            @Parameter(description = "有效期结束日期结束(DD/MM/YYYY)") @RequestParam(required = false) String valEndDateTo,
            @Parameter(description = "补充日期(DD/MM/YYYY)") @RequestParam(required = false) String supplDate,
            @Parameter(description = "商品编码") @RequestParam(required = false) String nomenc,
            @Parameter(description = "商品编码结束") @RequestParam(required = false) String nomencTo,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keywordSearch,
            @Parameter(description = "关键词匹配规则(OR/AND)") @RequestParam(required = false, defaultValue = "OR") String keywordMatchRule,
            @Parameter(description = "排除关键词") @RequestParam(required = false) String excludeKeyword,
            @Parameter(description = "最大页数") @RequestParam(required = false, defaultValue = "5") Integer maxPages) {

        log.info("收到BTI参数化搜索请求 - 发布国家: {}, BTI参考号: {}, 有效期开始: {}, 有效期结束: {}, 商品编码: {}, 关键词: {}, 最大页数: {}",
                refCountry, reference, valStartDate, valEndDate, nomenc, keywordSearch, maxPages);

        try {
            // 处理空值
            refCountry = refCountry != null ? refCountry : "";
            reference = reference != null ? reference : "";
            valStartDate = valStartDate != null ? valStartDate : "";
            valStartDateTo = valStartDateTo != null ? valStartDateTo : "";
            valEndDate = valEndDate != null ? valEndDate : "";
            valEndDateTo = valEndDateTo != null ? valEndDateTo : "";
            supplDate = supplDate != null ? supplDate : "";
            nomenc = nomenc != null ? nomenc : "";
            nomencTo = nomencTo != null ? nomencTo : "";
            keywordSearch = keywordSearch != null ? keywordSearch : "";
            keywordMatchRule = keywordMatchRule != null ? keywordMatchRule : "OR";
            excludeKeyword = excludeKeyword != null ? excludeKeyword : "";

            // 调用BTI爬虫
            List<Map<String, String>> btiData = euBTICrawler.crawlBTIDataWithParams(
                    refCountry, reference, valStartDate, valStartDateTo,
                    valEndDate, valEndDateTo, supplDate, nomenc, nomencTo,
                    keywordSearch, keywordMatchRule, excludeKeyword, maxPages);

            // 保存到CSV
            String filename = "BTI_" + System.currentTimeMillis() + ".csv";
            euBTICrawler.saveBTIDataToCSV(btiData, filename);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "BTI数据爬取成功");
            result.put("totalRecords", btiData.size());
            result.put("filename", filename);
            result.put("data", btiData);
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("refCountry", refCountry);
            searchParams.put("reference", reference);
            searchParams.put("valStartDate", valStartDate);
            searchParams.put("valStartDateTo", valStartDateTo);
            searchParams.put("valEndDate", valEndDate);
            searchParams.put("valEndDateTo", valEndDateTo);
            searchParams.put("supplDate", supplDate);
            searchParams.put("nomenc", nomenc);
            searchParams.put("nomencTo", nomencTo);
            searchParams.put("keywordSearch", keywordSearch);
            searchParams.put("keywordMatchRule", keywordMatchRule);
            searchParams.put("excludeKeyword", excludeKeyword);
            searchParams.put("maxPages", maxPages);
            result.put("searchParams", searchParams);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("BTI参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "BTI参数化搜索失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * BTI爬虫基础搜索
     */
    @PostMapping("/bti/basic")
    @Operation(summary = "BTI基础搜索", description = "使用默认参数搜索BTI数据")
    public ResponseEntity<Map<String, Object>> basicBTISearch(
            @Parameter(description = "最大页数") @RequestParam(required = false, defaultValue = "3") Integer maxPages) {

        log.info("收到BTI基础搜索请求 - 最大页数: {}", maxPages);

        try {
            // 调用BTI爬虫基础方法
            List<Map<String, String>> btiData = euBTICrawler.crawlBTIData(maxPages);

            // 保存到CSV
            String filename = "BTI_Basic_" + System.currentTimeMillis() + ".csv";
            euBTICrawler.saveBTIDataToCSV(btiData, filename);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "BTI基础搜索完成");
            result.put("totalRecords", btiData.size());
            result.put("filename", filename);
            result.put("data", btiData);
            result.put("maxPages", maxPages);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("BTI基础搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "BTI基础搜索失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 获取BTI爬虫配置模板
     */
    @GetMapping("/bti/config")
    @Operation(summary = "获取BTI爬虫配置", description = "获取BTI爬虫的参数配置模板")
    public ResponseEntity<Map<String, Object>> getBTIConfig() {

        Map<String, Object> config = new HashMap<>();
        config.put("refCountry", "发布国家代码");
        config.put("reference", "BTI参考号");
        config.put("valStartDate", "有效期开始日期(DD/MM/YYYY)");
        config.put("valStartDateTo", "有效期开始日期结束(DD/MM/YYYY)");
        config.put("valEndDate", "有效期结束日期(DD/MM/YYYY)");
        config.put("valEndDateTo", "有效期结束日期结束(DD/MM/YYYY)");
        config.put("supplDate", "补充日期(DD/MM/YYYY)");
        config.put("nomenc", "商品编码");
        config.put("nomencTo", "商品编码结束");
        config.put("keywordSearch", "关键词搜索");
        config.put("keywordMatchRule", "关键词匹配规则(OR/AND)");
        config.put("excludeKeyword", "排除关键词");
        config.put("maxPages", "最大页数");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "BTI爬虫配置模板",
                "config", config
        ));
    }

    /**
     * 测试BTI爬虫连接
     */
    @GetMapping("/bti/test")
    @Operation(summary = "测试BTI爬虫连接", description = "测试BTI爬虫是否能正常连接")
    public ResponseEntity<Map<String, Object>> testBTIConnection() {

        try {
            // 执行一个简单的测试爬取
            List<Map<String, String>> testData = euBTICrawler.crawlBTIData(1);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "BTI爬虫连接测试成功",
                    "testRecords", testData.size(),
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("BTI爬虫连接测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "BTI爬虫连接测试失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 获取EU爬虫状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取EU爬虫状态", description = "获取所有EU爬虫的运行状态")
    public ResponseEntity<Map<String, Object>> getEUStatus() {

        Map<String, Object> status = new HashMap<>();

        // BTI爬虫状态
        try {
            List<Map<String, String>> testData = euBTICrawler.crawlBTIData(1);
            status.put("bti", Map.of(
                    "status", "正常",
                    "testRecords", testData.size(),
                    "lastTest", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            status.put("bti", Map.of(
                    "status", "异常",
                    "error", e.getMessage(),
                    "lastTest", System.currentTimeMillis()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "EU爬虫状态检查完成",
                "crawlers", status,
                "timestamp", System.currentTimeMillis()
        ));
    }

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

    /**
     * EU注册信息关键词爬取
     */
    @PostMapping("/registration/keywords")
    @Operation(summary = "EU注册信息关键词爬取", description = "基于关键词列表爬取EUDAMED设备注册信息")
    public ResponseEntity<Map<String, Object>> crawlRegistrationWithKeywords(
            @Parameter(description = "关键词列表(逗号分隔)") @RequestParam(required = false) String inputKeywords,
            @Parameter(description = "最大记录数，0或-1表示爬取所有数据") @RequestParam(required = false, defaultValue = "100") Integer maxRecords,
            @Parameter(description = "批次大小") @RequestParam(required = false, defaultValue = "50") Integer batchSize,
            @Parameter(description = "开始日期(可选,格式:yyyy-MM-dd)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(可选,格式:yyyy-MM-dd)") @RequestParam(required = false) String dateTo) {

        log.info("收到EU注册信息关键词爬取请求 - 关键词: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}",
                inputKeywords, maxRecords, batchSize, dateFrom, dateTo);

        try {
            // 处理maxRecords参数：0或-1表示爬取所有数据
            int actualMaxRecords = maxRecords;
            if (maxRecords == 0 || maxRecords == -1) {
                actualMaxRecords = -1;
                log.info("maxRecords为{}，将爬取所有数据", maxRecords);
            }

            // 解析关键词
            List<String> keywords = null;
            if (inputKeywords != null && !inputKeywords.trim().isEmpty()) {
                keywords = parseKeywordsFromString(inputKeywords);
                log.info("解析后的关键词列表，数量: {}", keywords.size());
            }

            String crawlResult;

            // 如果有关键词列表，使用关键词爬取方法
            if (keywords != null && !keywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 日期范围: {} - {}, 最大记录数: {}",
                        keywords.size(), dateFrom, dateTo, actualMaxRecords);
                crawlResult = euRegistrationCrawler.crawlAndSaveWithKeywords(keywords, actualMaxRecords, batchSize, dateFrom, dateTo);
            } else {
                log.info("使用默认搜索 - 搜索词: medical device, 最大记录数: {}", actualMaxRecords);
                crawlResult = euRegistrationCrawler.crawlAndSaveDeviceRegistration("medical device", actualMaxRecords, batchSize, dateFrom, dateTo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "EU注册信息爬取成功");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("maxRecords", actualMaxRecords);
            result.put("originalMaxRecords", maxRecords);

            // 如果使用了关键词，添加关键词相关信息
            if (keywords != null && !keywords.isEmpty()) {
                result.put("keywordsProcessed", keywords.size());
                result.put("keywords", keywords);
            }

            log.info("EU注册信息爬取完成，数据库保存结果: {}", crawlResult);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("EU注册信息关键词爬取失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "EU注册信息关键词爬取失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * EU注册信息参数化搜索
     */
    @PostMapping("/registration/search")
    @Operation(summary = "EU注册信息参数化搜索", description = "按Trade name、制造商名称、风险等级等参数搜索EUDAMED设备注册信息")
    public ResponseEntity<Map<String, Object>> searchRegistration(
            @Parameter(description = "Trade name") @RequestParam(required = false) String tradeName,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturerName,
            @Parameter(description = "风险等级") @RequestParam(required = false) String riskClass,
            @Parameter(description = "最大记录数，-1表示爬取所有数据") @RequestParam(required = false, defaultValue = "100") Integer maxRecords,
            @Parameter(description = "批次大小") @RequestParam(required = false, defaultValue = "50") Integer batchSize,
            @Parameter(description = "开始日期(可选,格式:yyyy-MM-dd)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(可选,格式:yyyy-MM-dd)") @RequestParam(required = false) String dateTo) {

        log.info("收到EU注册信息参数化搜索请求 - Trade name: {}, 制造商: {}, 风险等级: {}, 最大记录数: {}, 日期范围: {} - {}",
                tradeName, manufacturerName, riskClass, maxRecords, dateFrom, dateTo);

        try {
            String crawlResult;

            // 根据参数选择搜索方式
            if (tradeName != null && !tradeName.trim().isEmpty()) {
                log.info("使用Trade name搜索: {}", tradeName);
                crawlResult = euRegistrationCrawler.crawlAndSaveByTradeName(tradeName, maxRecords, batchSize);
            } else if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
                log.info("使用制造商名称搜索: {}", manufacturerName);
                crawlResult = euRegistrationCrawler.crawlAndSaveByManufacturerName(manufacturerName, maxRecords, batchSize);
            } else if (riskClass != null && !riskClass.trim().isEmpty()) {
                log.info("使用风险等级搜索: {}", riskClass);
                crawlResult = euRegistrationCrawler.crawlAndSaveByRiskClass(riskClass, maxRecords, batchSize);
            } else {
                log.info("使用默认搜索 - 搜索词: medical device");
                crawlResult = euRegistrationCrawler.crawlAndSaveDeviceRegistration("medical device", maxRecords, batchSize, dateFrom, dateTo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "EU注册信息搜索成功");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);

            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("tradeName", tradeName);
            searchParams.put("manufacturerName", manufacturerName);
            searchParams.put("riskClass", riskClass);
            searchParams.put("maxRecords", maxRecords);
            searchParams.put("batchSize", batchSize);
            searchParams.put("dateFrom", dateFrom);
            searchParams.put("dateTo", dateTo);
            result.put("searchParams", searchParams);

            log.info("EU注册信息搜索完成，数据库保存结果: {}", crawlResult);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("EU注册信息参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "EU注册信息参数化搜索失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 获取EU注册信息爬虫配置模板
     */
    @GetMapping("/registration/config")
    @Operation(summary = "获取EU注册信息爬虫配置", description = "获取EU注册信息爬虫的参数配置模板")
    public ResponseEntity<Map<String, Object>> getRegistrationConfig() {

        Map<String, Object> config = new HashMap<>();
        config.put("inputKeywords", "关键词列表(逗号分隔)");
        config.put("tradeName", "Trade name");
        config.put("manufacturerName", "制造商名称");
        config.put("riskClass", "风险等级");
        config.put("maxRecords", "最大记录数，0或-1表示爬取所有数据");
        config.put("batchSize", "批次大小");
        config.put("dateFrom", "开始日期(格式:yyyy-MM-dd)");
        config.put("dateTo", "结束日期(格式:yyyy-MM-dd)");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "EU注册信息爬虫配置模板",
                "config", config
        ));
    }

    // ==================== EU Safety Gate Alert 转换为设备召回记录 ====================

    /**
     * 爬取EU Safety Gate预警数据并转换为设备召回记录（支持关键词列表）
     */
    @PostMapping("/safety-gate/convert-to-recalls")
    @Operation(summary = "爬取EU预警数据并转换为召回记录", description = "基于关键词列表爬取EU Safety Gate预警数据并转换保存为设备召回记录")
    public ResponseEntity<Map<String, Object>> crawlSafetyGateAndConvertToRecalls(
            @Parameter(description = "关键词列表(逗号分隔)") @RequestParam(required = false) String inputKeywords,
            @Parameter(description = "最大爬取页数，0或-1表示爬取所有数据", example = "5") @RequestParam(defaultValue = "5") int maxPages,
            @Parameter(description = "排序字段", example = "PUBLICATION_DATE") @RequestParam(required = false) String sortField,
            @Parameter(description = "排序方向", example = "DESC") @RequestParam(required = false) String sortDirection,
            @Parameter(description = "语言", example = "en") @RequestParam(required = false) String language,
            @Parameter(description = "产品类别", example = "ELECTRICAL_APPLIANCES") @RequestParam(required = false) String productCategories) {

        log.info("收到EU Safety Gate转换召回记录请求 - 关键词: {}, 最大页数: {}, 排序: {} {}, 语言: {}, 产品类别: {}",
                inputKeywords, maxPages, sortField, sortDirection, language, productCategories);

        try {
            // 处理maxPages参数：0或-1表示爬取所有数据
            int actualMaxPages = maxPages;
            if (maxPages == 0 || maxPages == -1) {
                actualMaxPages = -1;
                log.info("maxPages为{}，将爬取所有数据", maxPages);
            }

            // 解析关键词
            List<String> keywords = null;
            if (inputKeywords != null && !inputKeywords.trim().isEmpty()) {
                keywords = parseKeywordsFromString(inputKeywords);
                log.info("解析后的关键词列表，数量: {}", keywords.size());
            }

            // 解析产品类别参数
            List<String> categoryList = new ArrayList<>();
            if (productCategories != null && !productCategories.trim().isEmpty()) {
                categoryList = Arrays.asList(productCategories.split(","));
                categoryList = categoryList.stream().map(String::trim).collect(Collectors.toList());
            } else {
                // 默认产品类别：电器设备
                categoryList = Arrays.asList("ELECTRICAL_APPLIANCES");
            }

            Map<String, Object> result;

            // 如果有关键词列表，使用关键词爬取方法
            if (keywords != null && !keywords.isEmpty()) {
                log.info("使用关键词列表爬取 - 关键词数量: {}, 最大页数: {}", keywords.size(), actualMaxPages);
                
                // 为每个关键词执行爬取
                int totalSaved = 0;
                int totalSkipped = 0;
                int totalAlerts = 0;
                List<Map<String, Object>> keywordResults = new ArrayList<>();
                
                for (String keyword : keywords) {
                    log.info("正在爬取关键词: {}", keyword);
                    Map<String, Object> keywordResult = euSafetyGateAlertCrawler.crawlAndSaveAsDeviceRecallsWithCategories(
                            actualMaxPages, keyword, sortField != null ? sortField : "", 
                            sortDirection != null ? sortDirection : "", 
                            language != null ? language : "", categoryList);
                    
                    totalSaved += (Integer) keywordResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) keywordResult.getOrDefault("totalSkipped", 0);
                    totalAlerts += (Integer) keywordResult.getOrDefault("totalAlerts", 0);
                    
                    keywordResults.add(Map.of(
                            "keyword", keyword,
                            "saved", keywordResult.getOrDefault("totalSaved", 0),
                            "skipped", keywordResult.getOrDefault("totalSkipped", 0),
                            "alerts", keywordResult.getOrDefault("totalAlerts", 0)
                    ));
                }
                
                result = new HashMap<>();
                result.put("success", true);
                result.put("message", "EU Safety Gate预警数据转换召回记录成功");
                result.put("totalSaved", totalSaved);
                result.put("totalSkipped", totalSkipped);
                result.put("totalAlerts", totalAlerts);
                result.put("totalPages", actualMaxPages);
                result.put("originalMaxPages", maxPages);
                result.put("keywordResults", keywordResults);
                result.put("savedToDatabase", true);
                result.put("keywordsProcessed", keywords.size());
                result.put("keywords", keywords);
                result.put("categories", categoryList);
                result.put("timestamp", System.currentTimeMillis());
            } else {
                log.info("使用默认搜索 - 搜索所有数据，最大页数: {}", actualMaxPages);
                result = euSafetyGateAlertCrawler.crawlAndSaveAsDeviceRecallsWithCategories(
                        actualMaxPages, "", sortField != null ? sortField : "", 
                        sortDirection != null ? sortDirection : "", 
                        language != null ? language : "", categoryList);
                
                // 添加额外的响应信息
                result.put("savedToDatabase", true);
                result.put("categories", categoryList);
                result.put("originalMaxPages", maxPages);
                result.put("timestamp", System.currentTimeMillis());
            }

            log.info("EU Safety Gate预警数据转换召回记录完成，结果: {}", result);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("EU Safety Gate转换召回记录失败: {}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "EU Safety Gate转换召回记录失败");
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 获取支持的产品类别列表
     */
    @GetMapping("/safety-gate/product-categories")
    @Operation(summary = "获取支持的产品类别", description = "获取EU Safety Gate支持的产品类别列表")
    public ResponseEntity<Map<String, Object>> getSupportedProductCategories() {

        try {
            // EU Safety Gate支持的产品类别
            List<Map<String, String>> categories = Arrays.asList(
                    Map.of("value", "ELECTRICAL_APPLIANCES", "label", "电器设备", "description", "电器和电子设备"),
                    Map.of("value", "TOYS", "label", "玩具", "description", "儿童玩具和游戏设备"),
                    Map.of("value", "COSMETICS", "label", "化妆品", "description", "化妆品和个人护理产品"),
                    Map.of("value", "CLOTHING_TEXTILES", "label", "服装纺织品", "description", "服装、鞋子和纺织品"),
                    Map.of("value", "CONSTRUCTION_PRODUCTS", "label", "建筑产品", "description", "建筑材料和工具"),
                    Map.of("value", "MACHINERY", "label", "机械设备", "description", "工业机械和设备"),
                    Map.of("value", "MOTOR_VEHICLES", "label", "机动车辆", "description", "汽车和摩托车"),
                    Map.of("value", "RECREATIONAL_CRAFTS", "label", "休闲船艇", "description", "休闲船艇和水上运动设备"),
                    Map.of("value", "PERSONAL_PROTECTIVE_EQUIPMENT", "label", "个人防护设备", "description", "安全帽、手套等防护用品"),
                    Map.of("value", "MEDICAL_DEVICES", "label", "医疗器械", "description", "医疗器械和设备"),
                    Map.of("value", "CHILDCARE_ARTICLES", "label", "育儿用品", "description", "婴儿车、安全座椅等"),
                    Map.of("value", "FURNITURE", "label", "家具", "description", "家具和家居用品"),
                    Map.of("value", "CHEMICAL_PRODUCTS", "label", "化学产品", "description", "化学品和清洁剂"),
                    Map.of("value", "LIGHTING_EQUIPMENT", "label", "照明设备", "description", "灯具和照明设备"),
                    Map.of("value", "SPORTS_EQUIPMENT", "label", "体育设备", "description", "运动和健身设备"),
                    Map.of("value", "KITCHEN_UTENSILS", "label", "厨房用具", "description", "厨房用具和餐具")
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取产品类别列表成功",
                    "categories", categories,
                    "defaultCategory", "ELECTRICAL_APPLIANCES",
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("获取产品类别列表失败: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取产品类别列表失败",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 获取EU Safety Gate爬虫状态
     */
    @GetMapping("/safety-gate/status")
    @Operation(summary = "获取EU Safety Gate爬虫状态", description = "获取EU Safety Gate爬虫的运行状态和数据统计")
    public ResponseEntity<Map<String, Object>> getSafetyGateStatus() {

        try {
            // 测试爬虫可用性
            Map<String, Object> testResult = euSafetyGateAlertCrawler.crawlSafetyGateAlerts(1);

            Map<String, Object> status = new HashMap<>();
            status.put("available", true);
            status.put("testResult", testResult);
            status.put("lastTest", System.currentTimeMillis());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "EU Safety Gate爬虫状态正常",
                    "status", status,
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("获取EU Safety Gate爬虫状态失败: {}", e.getMessage(), e);

            Map<String, Object> status = new HashMap<>();
            status.put("available", false);
            status.put("error", e.getMessage());
            status.put("lastTest", System.currentTimeMillis());

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "EU Safety Gate爬虫状态异常",
                    "status", status,
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
}

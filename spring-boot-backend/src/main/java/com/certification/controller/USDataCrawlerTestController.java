package com.certification.controller;

import com.certification.crawler.countrydata.us.US_recall_api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FDA设备数据爬虫统一测试Controller
 * 提供统一的测试接口，支持所有查询场景
 */
@RestController
@RequestMapping("/api/test/us")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class USDataCrawlerTestController {

    @Autowired
    private US_recall_api recallApi;

    // ==================== 工具方法 ====================
    
    /**
     * 智能解析关键词字符串
     * 优先按逗号分割，保留包含空格的多词关键词
     * （参考USCrawlerService.parseKeywordsFromString方法）
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

    // ==================== 核心统一测试接口 ====================

    /**
     * ⭐ 统一测试接口 - 支持所有参数和查询模式
     * 
     * GET方式（支持逗号分隔关键词）：
     * /api/test/us/unified-crawl?recallingFirm=xxx&inputKeywords=Abbott,Medtronic&dateFrom=20240101&dateTo=20240630&maxRecords=10
     * 或兼容旧格式：
     * /api/test/us/unified-crawl?recallingFirm=xxx&inputKeywords=Abbott,Medtronic&dateFrom=2024-01-01&dateTo=2024-06-30&maxPages=5
     * 
     * POST方式（支持JSON数组关键词）：
     * /api/test/us/unified-crawl
     * Body: {
     *   "brandName": "Heartware",
     *   "recallingFirm": "Abbott", 
     *   "productDescription": "cardiac",
     *   "keywords": ["Abbott", "Medtronic"],           // JSON数组格式（推荐）
     *   "inputKeywords": "Abbott,Medtronic",           // 或逗号分隔字符串格式（兼容旧接口）
     *   "dateFrom": "20240101",                        // YYYYMMDD格式（推荐）
     *   "dateTo": "20240630",                          // 或 "2024-06-30"（兼容旧接口）
     *   "recentDays": 60,                              // 最近N天（自动计算日期）
     *   "maxRecords": 100,                             // 记录数（推荐）
     *   "maxPages": 5,                                 // 或页数（兼容旧接口，×100转换）
     *   "batchSize": 20
     * }
     */
    @RequestMapping(value = "/unified-crawl", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> unifiedCrawl(
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String recallingFirm,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false) List<String> keywords,
            @RequestParam(required = false) String inputKeywords,  // 兼容逗号分隔格式
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer recentDays,
            @RequestParam(required = false) Integer maxPages,      // 兼容页数参数
            @RequestParam(defaultValue = "10") int maxRecords,
            @RequestParam(defaultValue = "5") int batchSize,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("\n========== 统一测试接口 - 智能参数识别 ==========");
            
            // 如果是POST请求且有body，从body中读取参数
            if (requestBody != null && !requestBody.isEmpty()) {
                brandName = (String) requestBody.getOrDefault("brandName", brandName);
                recallingFirm = (String) requestBody.getOrDefault("recallingFirm", recallingFirm);
                productDescription = (String) requestBody.getOrDefault("productDescription", productDescription);
                dateFrom = (String) requestBody.getOrDefault("dateFrom", dateFrom);
                dateTo = (String) requestBody.getOrDefault("dateTo", dateTo);
                
                if (requestBody.containsKey("keywords")) {
                    @SuppressWarnings("unchecked")
                    List<String> bodyKeywords = (List<String>) requestBody.get("keywords");
                    keywords = bodyKeywords;
                }
                
                if (requestBody.containsKey("inputKeywords")) {
                    Object inputKeywordsObj = requestBody.get("inputKeywords");
                    if (inputKeywordsObj instanceof String) {
                        inputKeywords = (String) inputKeywordsObj;
                    }
                }
                
                if (requestBody.containsKey("recentDays")) {
                    recentDays = (Integer) requestBody.get("recentDays");
                }
                
                if (requestBody.containsKey("maxPages")) {
                    maxPages = (Integer) requestBody.get("maxPages");
                }
                
                if (requestBody.containsKey("maxRecords")) {
                    maxRecords = (Integer) requestBody.get("maxRecords");
                }
                
                if (requestBody.containsKey("batchSize")) {
                    batchSize = (Integer) requestBody.get("batchSize");
                }
            }
            
            // ========== 参数格式兼容转换 ==========
            
            // 1. 处理关键词列表（支持两种格式）
            if (inputKeywords != null && !inputKeywords.trim().isEmpty()) {
                // 兼容旧格式：逗号分隔字符串 → List
                keywords = parseKeywordsFromString(inputKeywords);
                System.out.println("关键词格式转换: 逗号分隔字符串 → List，数量: " + keywords.size());
            }
            
            // 2. 处理日期格式（支持两种格式）
            if (dateFrom != null && dateFrom.contains("-")) {
                // 兼容旧格式：YYYY-MM-DD → YYYYMMDD
                String oldFormat = dateFrom;
                dateFrom = dateFrom.replace("-", "");
                System.out.println("日期格式转换: " + oldFormat + " → " + dateFrom);
            }
            
            if (dateTo != null && dateTo.contains("-")) {
                String oldFormat = dateTo;
                dateTo = dateTo.replace("-", "");
                System.out.println("日期格式转换: " + oldFormat + " → " + dateTo);
            }
            
            // 3. 处理数量参数（支持两种格式）
            if (maxPages != null && maxPages > 0) {
                // 兼容旧格式：maxPages → maxRecords
                int convertedRecords = maxPages * 100;
                System.out.println("数量参数转换: maxPages=" + maxPages + " → maxRecords=" + convertedRecords);
                maxRecords = convertedRecords;
            } else if (maxPages != null && maxPages == 0) {
                // maxPages=0 表示全部数据
                maxRecords = -1;
                System.out.println("数量参数转换: maxPages=0 → maxRecords=-1（全部数据）");
            }
            
            // 构建参数
            US_recall_api.RecallCrawlerParams params = 
                new US_recall_api.RecallCrawlerParams()
                    .maxRecords(maxRecords)
                    .batchSize(batchSize);
            
            // 设置搜索字段
            if (brandName != null && !brandName.trim().isEmpty()) {
                params.brandName(brandName);
                System.out.println("品牌名称: " + brandName);
            }
            
            if (recallingFirm != null && !recallingFirm.trim().isEmpty()) {
                params.recallingFirm(recallingFirm);
                System.out.println("召回公司: " + recallingFirm);
            }
            
            if (productDescription != null && !productDescription.trim().isEmpty()) {
                params.productDescription(productDescription);
                System.out.println("产品描述: " + productDescription);
            }
            
            // 设置关键词列表
            if (keywords != null && !keywords.isEmpty()) {
                params.keywords(keywords);
                System.out.println("关键词列表: " + keywords + " (数量: " + keywords.size() + ")");
            }
            
            // 设置时间范围（recentDays优先）
            if (recentDays != null && recentDays > 0) {
                params.recentDays(recentDays);
                System.out.println("最近天数: " + recentDays + " (自动计算日期，考虑30天延迟)");
            } else if (dateFrom != null && dateTo != null) {
                params.dateRange(dateFrom, dateTo);
                System.out.println("日期范围: " + dateFrom + " - " + dateTo);
            }
            
            // 识别查询模式
            String queryMode;
            if (keywords != null && !keywords.isEmpty()) {
                queryMode = "关键词列表批量查询（每个关键词在品牌、公司、描述3个字段中搜索）";
            } else if (brandName != null || recallingFirm != null || productDescription != null) {
                int fieldCount = 0;
                if (brandName != null) fieldCount++;
                if (recallingFirm != null) fieldCount++;
                if (productDescription != null) fieldCount++;
                
                if (fieldCount > 1) {
                    queryMode = "多字段组合查询（使用AND连接）";
                } else {
                    queryMode = "单字段查询";
                }
            } else {
                queryMode = "时间范围查询（无字段限制）";
            }
            
            System.out.println("查询模式: " + queryMode);
            System.out.println("最大记录数: " + maxRecords);
            System.out.println("批次大小: " + batchSize);
            
            // 执行爬取
            String result = recallApi.crawl(params);
            
            long endTime = System.currentTimeMillis();
            
            // 构建响应
            Map<String, Object> usedParams = new HashMap<>();
            if (brandName != null) usedParams.put("brandName", brandName);
            if (recallingFirm != null) usedParams.put("recallingFirm", recallingFirm);
            if (productDescription != null) usedParams.put("productDescription", productDescription);
            if (keywords != null) usedParams.put("keywords", keywords);
            if (recentDays != null) usedParams.put("recentDays", recentDays);
            if (dateFrom != null) usedParams.put("dateFrom", dateFrom);
            if (dateTo != null) usedParams.put("dateTo", dateTo);
            usedParams.put("maxRecords", maxRecords);
            usedParams.put("batchSize", batchSize);
            
            response.put("success", true);
            response.put("testName", "统一测试接口");
            response.put("queryMode", queryMode);
            response.put("result", result);
            response.put("parameters", usedParams);
            response.put("executionTimeMs", endTime - startTime);
            response.put("timestamp", LocalDate.now().toString());
            
            System.out.println("测试完成，耗时: " + (endTime - startTime) + "ms");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== 辅助工具接口 ====================

    /**
     * 智能日期范围建议
     * GET /api/test/us/suggest-date-range
     * 
     * 提供推荐的日期范围，考虑FDA数据延迟
     */
    @GetMapping("/suggest-date-range")
    public ResponseEntity<Map<String, Object>> suggestDateRange() {
        Map<String, Object> response = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        // 建议的日期范围（考虑FDA数据延迟）
        List<Map<String, String>> suggestions = Arrays.asList(
            Map.of(
                "name", "2024年全年",
                "dateFrom", "20240101",
                "dateTo", "20241231",
                "description", "推荐：数据最完整",
                "recommended", "true"
            ),
            Map.of(
                "name", "2024年上半年",
                "dateFrom", "20240101",
                "dateTo", "20240630",
                "description", "推荐：测试用"
            ),
            Map.of(
                "name", "2024年下半年",
                "dateFrom", "20240701",
                "dateTo", "20241231",
                "description", "2024年下半年数据"
            ),
            Map.of(
                "name", "2023年全年",
                "dateFrom", "20230101",
                "dateTo", "20231231",
                "description", "历史数据参考"
            ),
            Map.of(
                "name", "最近90天（考虑延迟）",
                "dateFrom", today.minusDays(120).format(formatter),
                "dateTo", today.minusDays(30).format(formatter),
                "description", "避开最近30天的数据延迟"
            ),
            Map.of(
                "name", "最近一年（考虑延迟）",
                "dateFrom", today.minusYears(1).minusDays(30).format(formatter),
                "dateTo", today.minusDays(30).format(formatter),
                "description", "一年数据，避开最近30天"
            )
        );
        
        response.put("currentDate", today.format(formatter));
        response.put("currentDateReadable", today.toString());
        response.put("suggestions", suggestions);
        response.put("warning", "注意：FDA数据通常有1-4周的延迟，建议查询至少30天前的数据");
        response.put("dataAvailability", Map.of(
            "recallDataStartYear", "2002",
            "eventDataStartYear", "2009",
            "updateFrequency", "每周更新",
            "estimatedDelay", "1-4周"
        ));
        response.put("formatGuide", Map.of(
            "dateFormat", "YYYYMMDD（8位数字）或 YYYY-MM-DD（自动转换）",
            "keywordsFormat", "JSON数组 [\"A\", \"B\"] 或 逗号分隔 \"A,B\"（自动转换）",
            "maxRecordsFormat", "记录数 maxRecords=100 或 页数 maxPages=1（自动×100转换）"
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取接口使用说明
     * GET /api/test/us/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("interfaceName", "FDA设备召回数据统一测试接口");
        response.put("version", "v2.0");
        response.put("mainEndpoint", "/api/test/us/unified-crawl");
        
        // 参数说明
        Map<String, Map<String, String>> parameters = new HashMap<>();
        parameters.put("brandName", Map.of(
            "type", "String",
            "required", "否",
            "description", "品牌名称",
            "example", "Medtronic"
        ));
        parameters.put("recallingFirm", Map.of(
            "type", "String",
            "required", "否",
            "description", "召回公司名称",
            "example", "Abbott"
        ));
        parameters.put("productDescription", Map.of(
            "type", "String",
            "required", "否",
            "description", "产品描述",
            "example", "pacemaker"
        ));
        parameters.put("keywords / inputKeywords", Map.of(
            "type", "List<String> / String",
            "required", "否",
            "description", "关键词列表（JSON数组或逗号分隔字符串）",
            "example", "[\"Abbott\", \"Medtronic\"] 或 \"Abbott,Medtronic\""
        ));
        parameters.put("dateFrom / dateTo", Map.of(
            "type", "String",
            "required", "否",
            "description", "日期范围（YYYYMMDD或YYYY-MM-DD格式）",
            "example", "20240101 或 2024-01-01"
        ));
        parameters.put("recentDays", Map.of(
            "type", "Integer",
            "required", "否",
            "description", "最近N天（自动计算日期，考虑30天FDA延迟）",
            "example", "60"
        ));
        parameters.put("maxRecords / maxPages", Map.of(
            "type", "Integer",
            "required", "否",
            "description", "最大记录数 或 最大页数（页数×100自动转换）",
            "example", "100 或 1（页数）"
        ));
        parameters.put("batchSize", Map.of(
            "type", "Integer",
            "required", "否",
            "description", "批次大小",
            "default", "5"
        ));
        
        response.put("parameters", parameters);
        
        // 使用示例
        List<Map<String, String>> examples = Arrays.asList(
            Map.of(
                "name", "1. 单字段查询",
                "method", "GET",
                "url", "/api/test/us/unified-crawl?recallingFirm=Abbott&dateFrom=20240101&dateTo=20240630&maxRecords=10"
            ),
            Map.of(
                "name", "2. 多字段组合（AND）",
                "method", "GET",
                "url", "/api/test/us/unified-crawl?brandName=Heartware&recallingFirm=Abbott&dateFrom=20240101&dateTo=20240630"
            ),
            Map.of(
                "name", "3. 关键词列表（JSON数组）",
                "method", "POST",
                "body", "{\"keywords\": [\"Abbott\", \"Medtronic\"], \"dateFrom\": \"20240101\", \"dateTo\": \"20240630\"}"
            ),
            Map.of(
                "name", "4. 关键词列表（逗号分隔，兼容旧格式）",
                "method", "GET",
                "url", "/api/test/us/unified-crawl?inputKeywords=Abbott,Medtronic&dateFrom=2024-01-01&dateTo=2024-06-30&maxPages=1"
            ),
            Map.of(
                "name", "5. 最近N天",
                "method", "GET",
                "url", "/api/test/us/unified-crawl?recentDays=60&recallingFirm=Medtronic&maxRecords=100"
            )
        );
        
        response.put("examples", examples);
        
        // 查询模式说明
        response.put("queryModes", Map.of(
            "关键词列表模式", "当设置keywords或inputKeywords时触发，每个关键词在品牌、公司、描述3个字段中搜索",
            "多字段组合模式", "当设置2-3个字段时触发，自动用AND连接",
            "单字段查询模式", "当只设置1个字段时触发",
            "时间范围查询模式", "当只设置日期参数时触发"
        ));
        
        response.put("tips", Arrays.asList(
            "使用2024年或更早的数据进行测试（FDA数据有30天延迟）",
            "日期格式支持 YYYYMMDD 和 YYYY-MM-DD 两种",
            "关键词支持 JSON数组 和 逗号分隔字符串 两种",
            "数量参数支持 maxRecords（记录数）和 maxPages（页数×100）",
            "推荐使用 POST + JSON 格式进行复杂查询",
            "推荐使用 GET 格式进行简单查询"
        ));
        
        return ResponseEntity.ok(response);
    }
}

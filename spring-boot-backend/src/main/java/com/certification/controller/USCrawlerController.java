package com.certification.controller;

import com.certification.service.USCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 美国爬虫控制器
 * 提供美国相关爬虫的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/us-crawler")
@Tag(name = "美国爬虫管理", description = "美国相关爬虫的测试和管理接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
public class USCrawlerController {

    @Autowired
    private USCrawlerService usCrawlerService;

    /**
     * 测试D_510K爬虫 - 已禁用
     */
    // @PostMapping("/test/d510k")
    // @Operation(summary = "测试D_510K爬虫", description = "测试FDA 510K设备审批数据爬虫")
    // public ResponseEntity<Map<String, Object>> testD510K(
    //         @RequestBody(required = false) Map<String, Object> params) {
    //     
    //     log.info("收到D_510K爬虫测试请求，参数: {}", params);
    //     
    //     try {
    //         Map<String, Object> result = usCrawlerService.testD510K(params != null ? params : Map.of());
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         log.error("D_510K爬虫测试失败", e);
    //         return ResponseEntity.internalServerError().body(Map.of(
    //             "success", false,
    //             "message", "D_510K爬虫测试失败: " + e.getMessage(),
    //             "error", e.getMessage()
    //         ));
    //     }
    // }

    /**
     * 测试US_510K爬虫
     */
    @PostMapping("/test/us510k")
    @Operation(summary = "测试US_510K爬虫", description = "测试新的FDA 510K设备数据爬虫")
    public ResponseEntity<Map<String, Object>> testUS510K(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到US_510K爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testUS510K(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_510K爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_510K爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * D_510K参数化搜索爬虫 - 已禁用
     */
    // @PostMapping("/search/d510k")
    // @Operation(summary = "D_510K参数化搜索", description = "按设备名称、申请人、决策日期等参数搜索FDA 510K数据")
    // public ResponseEntity<Map<String, Object>> searchD510K(
    //         @Parameter(description = "设备名称关键词") @RequestParam(required = false) String deviceName,
    //         @Parameter(description = "申请人名称") @RequestParam(required = false) String applicantName,
    //         @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String dateFrom,
    //         @Parameter(description = "结束日期(MM/DD/YYYY)") @RequestParam(required = false) String dateTo,
    //         @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
    //         @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
    //     
    //     log.info("收到D_510K参数化搜索请求 - 设备名称: {}, 申请人: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
    //             deviceName, applicantName, dateFrom, dateTo, maxPages, inputKeywords);
    //     
    //     try {
    //         Map<String, Object> params = Map.of(
    //             "deviceName", deviceName != null ? deviceName : "",
    //             "applicantName", applicantName != null ? applicantName : "",
    //             "dateFrom", dateFrom != null ? dateFrom : "",
    //             "dateTo", dateTo != null ? dateTo : "",
    //             "maxPages", maxPages,
    //             "inputKeywords", inputKeywords != null ? inputKeywords : ""
    //         );
    //         
    //         Map<String, Object> result = usCrawlerService.testD510K(params);
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         log.error("D_510K参数化搜索失败", e);
    //         return ResponseEntity.internalServerError().body(Map.of(
    //             "success", false,
    //             "message", "D_510K参数化搜索失败: " + e.getMessage(),
    //             "error", e.getMessage()
    //         ));
    //     }
    // }

    /**
     * US_510K参数化搜索爬虫
     */
    @PostMapping("/search/us510k")
    @Operation(summary = "US_510K参数化搜索", description = "按设备名称、申请人、trade_name、日期范围等参数搜索FDA 510K数据")
    public ResponseEntity<Map<String, Object>> searchUS510K(
            @Parameter(description = "设备名称关键词") @RequestParam(required = false) String deviceName,
            @Parameter(description = "申请人名称") @RequestParam(required = false) String applicantName,
            @Parameter(description = "trade_name关键词(使用openfda.device_name搜索)") @RequestParam(required = false) String tradeName,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_510K参数化搜索请求 - 设备名称: {}, 申请人: {}, trade_name: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
                deviceName, applicantName, tradeName, dateFrom, dateTo, maxPages, inputKeywords);
        
        try {
            Map<String, Object> params = Map.of(
                "deviceName", deviceName != null ? deviceName : "",
                "applicantName", applicantName != null ? applicantName : "",
                "tradeName", tradeName != null ? tradeName : "",
                "dateFrom", dateFrom != null ? dateFrom : "",
                "dateTo", dateTo != null ? dateTo : "",
                "maxPages", maxPages,
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );
            
            Map<String, Object> result = usCrawlerService.testUS510K(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_510K参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_510K参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试D_event爬虫
     */
    @PostMapping("/test/devent")
    @Operation(summary = "测试D_event爬虫", description = "测试FDA设备不良事件数据爬虫")
    public ResponseEntity<Map<String, Object>> testDEvent(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到D_event爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testDEvent(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("D_event爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "D_event爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试US_event爬虫
     */
    @PostMapping("/test/usevent")
    @Operation(summary = "测试US_event爬虫", description = "测试新的FDA设备不良事件数据爬虫")
    public ResponseEntity<Map<String, Object>> testUSEvent(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到US_event爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testUSEvent(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_event爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_event爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * D_event参数化搜索爬虫
     */
    @PostMapping("/search/devent")
    @Operation(summary = "D_event参数化搜索", description = "按品牌名称、制造商、型号、报告日期等参数搜索FDA MAUDE数据")
    public ResponseEntity<Map<String, Object>> searchDEvent(
            @Parameter(description = "品牌名称") @RequestParam(required = false) String brandName,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturer,
            @Parameter(description = "型号") @RequestParam(required = false) String modelNumber,
            @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(MM/DD/YYYY)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到D_event参数化搜索请求 - 品牌名称: {}, 制造商: {}, 型号: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
                brandName, manufacturer, modelNumber, dateFrom, dateTo, maxPages, inputKeywords);
        
        try {
            Map<String, Object> params = Map.of(
                "brandName", brandName != null ? brandName : "",
                "manufacturer", manufacturer != null ? manufacturer : "",
                "modelNumber", modelNumber != null ? modelNumber : "",
                "dateFrom", dateFrom != null ? dateFrom : "",
                "dateTo", dateTo != null ? dateTo : "",
                "maxPages", maxPages,
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );
            
            Map<String, Object> result = usCrawlerService.testDEvent(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("D_event参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "D_event参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * US_event参数化搜索爬虫
     */
    @PostMapping("/search/usevent")
    @Operation(summary = "US_event参数化搜索", description = "按设备名称、制造商、产品问题、报告日期等参数搜索FDA设备不良事件数据")
    public ResponseEntity<Map<String, Object>> searchUSEvent(
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturer,
            @Parameter(description = "产品问题") @RequestParam(required = false) String productProblem,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_event参数化搜索请求 - 设备名称: {}, 制造商: {}, 产品问题: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
                deviceName, manufacturer, productProblem, dateFrom, dateTo, maxPages, inputKeywords);
        
        try {
            Map<String, Object> params = Map.of(
                "deviceName", deviceName != null ? deviceName : "",
                "manufacturer", manufacturer != null ? manufacturer : "",
                "productProblem", productProblem != null ? productProblem : "",
                "dateFrom", dateFrom != null ? dateFrom : "",
                "dateTo", dateTo != null ? dateTo : "",
                "maxPages", maxPages,
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );
            
            Map<String, Object> result = usCrawlerService.testUSEvent(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_event参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_event参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试D_recall爬虫 - 已禁用
     */
    // @PostMapping("/test/drecall")
    // @Operation(summary = "测试D_recall爬虫", description = "测试FDA设备召回数据爬虫")
    // public ResponseEntity<Map<String, Object>> testDRecall(
    //         @RequestBody(required = false) Map<String, Object> params) {
    //     
    //     log.info("收到D_recall爬虫测试请求，参数: {}", params);
    //     
    //     try {
    //         Map<String, Object> result = usCrawlerService.testDRecall(params != null ? params : Map.of());
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         log.error("D_recall爬虫测试失败", e);
    //         return ResponseEntity.internalServerError().body(Map.of(
    //             "success", false,
    //             "message", "D_recall爬虫测试失败: " + e.getMessage(),
    //             "error", e.getMessage()
    //         ));
    //     }
    // }

    /**
     * 测试US_recall_api爬虫
     */
    @PostMapping("/test/usrecall")
    @Operation(summary = "测试US_recall_api爬虫", description = "测试新的FDA设备召回数据爬虫")
    public ResponseEntity<Map<String, Object>> testUSRecall(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到US_recall_api爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testUSRecall(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_recall_api爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_recall_api爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * D_recall参数化搜索爬虫 - 已禁用
     */
    // @PostMapping("/search/drecall")
    // @Operation(summary = "D_recall参数化搜索", description = "按产品名称、召回原因、召回公司、召回日期等参数搜索FDA召回数据，支持关键词列表")
    // public ResponseEntity<Map<String, Object>> searchDRecall(
    //         @Parameter(description = "产品名称") @RequestParam(required = false) String productName,
    //         @Parameter(description = "召回原因") @RequestParam(required = false) String reasonForRecall,
    //         @Parameter(description = "召回公司") @RequestParam(required = false) String recallingFirm,
    //         @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String dateFrom,
    //         @Parameter(description = "结束日期(MM/DD/YYYY)") @RequestParam(required = false) String dateTo,
    //         @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
    //         @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
    //     
    //     log.info("收到D_recall参数化搜索请求 - 产品名称: {}, 召回原因: {}, 召回公司: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
    //             productName, reasonForRecall, recallingFirm, dateFrom, dateTo, maxPages, inputKeywords);
    //     log.info("inputKeywords参数详情 - 类型: {}, 值: '{}', 长度: {}", 
    //             inputKeywords != null ? inputKeywords.getClass().getSimpleName() : "null", 
    //             inputKeywords, 
    //             inputKeywords != null ? inputKeywords.length() : 0);
    //     
    //     try {
    //         Map<String, Object> params = Map.of(
    //             "productName", productName != null ? productName : "",
    //             "reasonForRecall", reasonForRecall != null ? reasonForRecall : "",
    //             "recallingFirm", recallingFirm != null ? recallingFirm : "",
    //             "dateFrom", dateFrom != null ? dateFrom : "",
    //             "dateTo", dateTo != null ? dateTo : "",
    //             "maxPages", maxPages,
    //             "inputKeywords", inputKeywords != null ? inputKeywords : ""
    //         );
    //         
    //         Map<String, Object> result = usCrawlerService.testDRecall(params);
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         log.error("D_recall参数化搜索失败", e);
    //         return ResponseEntity.internalServerError().body(Map.of(
    //             "success", false,
    //             "message", "D_recall参数化搜索失败: " + e.getMessage(),
    //             "error", e.getMessage()
    //         ));
    //     }
    // }

    /**
     * US_recall_api参数化搜索爬虫
     */
    @PostMapping("/search/usrecall")
    @Operation(summary = "US_recall_api参数化搜索", description = "按召回公司、brand name、产品描述、召回日期等参数搜索FDA召回数据，支持关键词列表")
    public ResponseEntity<Map<String, Object>> searchUSRecall(
            @Parameter(description = "召回公司") @RequestParam(required = false) String recallingFirm,
            @Parameter(description = "brand name") @RequestParam(required = false) String brandName,
            @Parameter(description = "产品描述") @RequestParam(required = false) String productDescription,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_recall_api参数化搜索请求 - 召回公司: {}, brand name: {}, 产品描述: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
                recallingFirm, brandName, productDescription, dateFrom, dateTo, maxPages, inputKeywords);
        
        try {
            Map<String, Object> params = Map.of(
                "recallingFirm", recallingFirm != null ? recallingFirm : "",
                "brandName", brandName != null ? brandName : "",
                "productDescription", productDescription != null ? productDescription : "",
                "dateFrom", dateFrom != null ? dateFrom : "",
                "dateTo", dateTo != null ? dateTo : "",
                "maxPages", maxPages,
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );
            
            Map<String, Object> result = usCrawlerService.testUSRecall(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_recall_api参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_recall_api参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

//    /**
//     * 测试D_registration爬虫
//     */
//    @PostMapping("/test/dregistration")
//    @Operation(summary = "测试D_registration爬虫", description = "测试FDA设备注册数据爬虫")
//    public ResponseEntity<Map<String, Object>> testDRegistration(
//            @RequestBody(required = false) Map<String, Object> params) {
//
//        log.info("收到D_registration爬虫测试请求，参数: {}", params);
//
//        try {
//            Map<String, Object> result = usCrawlerService.testDRegistration(params != null ? params : Map.of());
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("D_registration爬虫测试失败", e);
//            return ResponseEntity.internalServerError().body(Map.of(
//                "success", false,
//                "message", "D_registration爬虫测试失败: " + e.getMessage(),
//                "error", e.getMessage()
//            ));
//        }
//    }
//
    /**
     * US_registration参数化搜索爬虫
     */
    @PostMapping("/search/usregistration")
    @Operation(summary = "US_registration专门搜索", description = "按设备名称、专有名称、制造商名称等参数搜索FDA注册数据")
    public ResponseEntity<Map<String, Object>> searchUSRegistration(
            @Parameter(description = "设备名称（使用device_name搜索）") @RequestParam(required = false) String establishmentName,
            @Parameter(description = "专有名称（使用proprietary_name搜索）") @RequestParam(required = false) String proprietaryName,
            @Parameter(description = "制造商名称（使用manufacturer_name搜索）") @RequestParam(required = false) String ownerOperatorName,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {

        log.info("收到US_registration专门搜索请求 - 机构/贸易名称: {}, 专有名称: {}, 所有者/经营者名称: {}, 最大页数: {}, 关键词: {}",
                establishmentName, proprietaryName, ownerOperatorName, maxPages, inputKeywords);

        try {
            Map<String, Object> params = Map.of(
                "establishmentName", establishmentName != null ? establishmentName : "",
                "proprietaryName", proprietaryName != null ? proprietaryName : "",
                "ownerOperatorName", ownerOperatorName != null ? ownerOperatorName : "",
                "maxPages", maxPages,
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );

            Map<String, Object> result = usCrawlerService.testUSRegistration(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_registration参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_registration参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试unicrawl爬虫
     */
    @PostMapping("/test/unicrawl")
    @Operation(summary = "测试unicrawl爬虫", description = "测试统一爬虫")
    public ResponseEntity<Map<String, Object>> testUnicrawl(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到unicrawl爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testUnicrawl(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("unicrawl爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "unicrawl爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * unicrawl参数化搜索爬虫
     */
    @PostMapping("/search/unicrawl")
    @Operation(summary = "unicrawl参数化搜索", description = "按关键词列表、日期范围等参数搜索所有爬虫数据")
    public ResponseEntity<Map<String, Object>> searchUnicrawl(
            @Parameter(description = "总爬取数量") @RequestParam(required = false, defaultValue = "50") Integer totalCount,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords,
            @Parameter(description = "最大页数(0表示爬取所有)") @RequestParam(required = false, defaultValue = "0") Integer maxPages) {
        
        log.info("收到unicrawl参数化搜索请求 - 总爬取数量: {}, 日期范围: {} - {}, 关键词: {}, 最大页数: {}", 
                totalCount, dateFrom, dateTo, inputKeywords, maxPages);
        
        try {
            Map<String, Object> params = Map.of(
                "totalCount", totalCount,
                "dateFrom", dateFrom != null ? dateFrom : "",
                "dateTo", dateTo != null ? dateTo : "",
                "inputKeywords", inputKeywords != null ? inputKeywords : "",
                "maxPages", maxPages
            );
            
            Map<String, Object> result = usCrawlerService.testUnicrawl(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("unicrawl参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "unicrawl参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试CustomsCaseCrawler爬虫
     */
    @PostMapping("/test/customs-case")
    @Operation(summary = "测试CustomsCaseCrawler爬虫", description = "测试海关案例数据爬虫")
    public ResponseEntity<Map<String, Object>> testCustomsCase(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到CustomsCaseCrawler爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testCustomsCase(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("CustomsCaseCrawler爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "CustomsCaseCrawler爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * CustomsCaseCrawler参数化搜索爬虫
     */
    @PostMapping("/search/customs-case")
    @Operation(summary = "CustomsCaseCrawler参数化搜索", description = "按HS编码、最大记录数、批次大小、开始日期等参数搜索海关案例数据，支持关键词列表")
    public ResponseEntity<Map<String, Object>> searchCustomsCase(
            @Parameter(description = "HS编码") @RequestParam(required = false) String hsCode,
            @Parameter(description = "最大记录数") @RequestParam(required = false, defaultValue = "10") Integer maxRecords,
            @Parameter(description = "批次大小") @RequestParam(required = false, defaultValue = "10") Integer batchSize,
            @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String startDate,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到CustomsCaseCrawler参数化搜索请求 - HS编码: {}, 最大记录数: {}, 批次大小: {}, 开始日期: {}, 关键词: {}", 
                hsCode, maxRecords, batchSize, startDate, inputKeywords);
        
        try {
            Map<String, Object> params = Map.of(
                "hsCode", hsCode != null ? hsCode : "9018",
                "maxRecords", maxRecords,
                "batchSize", batchSize,
                "startDate", startDate != null ? startDate : "",
                "inputKeywords", inputKeywords != null ? inputKeywords : ""
            );
            
            Map<String, Object> result = usCrawlerService.testCustomsCase(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("CustomsCaseCrawler参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "CustomsCaseCrawler参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试GuidanceCrawler爬虫
     */
    @PostMapping("/test/guidance")
    @Operation(summary = "测试GuidanceCrawler爬虫", description = "测试FDA指导文档爬虫")
    public ResponseEntity<Map<String, Object>> testGuidance(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到GuidanceCrawler爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testGuidance(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GuidanceCrawler爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "GuidanceCrawler爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * GuidanceCrawler参数化搜索爬虫
     */
    @PostMapping("/search/guidance")
    @Operation(summary = "GuidanceCrawler参数化搜索", description = "按最大记录数参数搜索FDA指导文档数据")
    public ResponseEntity<Map<String, Object>> searchGuidance(
            @Parameter(description = "最大记录数") @RequestParam(required = false, defaultValue = "10") Integer maxRecords) {
        
        log.info("收到GuidanceCrawler参数化搜索请求 - 最大记录数: {}", maxRecords);
        
        try {
            Map<String, Object> params = Map.of(
                "maxRecords", maxRecords
            );
            
            Map<String, Object> result = usCrawlerService.testGuidance(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GuidanceCrawler参数化搜索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "GuidanceCrawler参数化搜索失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 获取爬虫参数配置模板
     */
    @GetMapping("/config/{crawlerType}")
    @Operation(summary = "获取爬虫参数配置模板", description = "获取指定爬虫的参数配置模板")
    public ResponseEntity<Map<String, Object>> getCrawlerConfig(
            @Parameter(description = "爬虫类型") @PathVariable String crawlerType) {
        
        log.info("获取爬虫配置模板: {}", crawlerType);
        
        Map<String, Object> config = switch (crawlerType.toLowerCase()) {
            case "d510k", "d_510k" -> Map.of(
                "error", "D_510K爬虫已禁用"
            );
            case "us510k", "us_510k" -> Map.of(
                "deviceName", "设备名称关键词",
                "applicantName", "申请人名称",
                "tradeName", "trade_name关键词(使用openfda.device_name搜索)",
                "dateFrom", "开始日期(YYYY-MM-DD)",
                "dateTo", "结束日期(YYYY-MM-DD)",
                "maxPages", "最大爬取页数",
                "inputKeywords", "输入关键词列表"
            );
            case "devent", "d_event" -> Map.of(
                "brandName", "品牌名称",
                "manufacturer", "制造商名称",
                "modelNumber", "型号",
                "dateFrom", "开始日期(MM/DD/YYYY)",
                "dateTo", "结束日期(MM/DD/YYYY)",
                "maxPages", "最大爬取页数"
            );
            case "usevent", "us_event" -> Map.of(
                "deviceName", "设备名称",
                "manufacturer", "制造商名称",
                "productProblem", "产品问题",
                "dateFrom", "开始日期(YYYY-MM-DD)",
                "dateTo", "结束日期(YYYY-MM-DD)",
                "maxPages", "最大爬取页数",
                "inputKeywords", "输入关键词列表"
            );
            case "drecall", "d_recall" -> Map.of(
                "error", "D_recall爬虫已禁用"
            );
            case "usrecall", "us_recall" -> Map.of(
                "recallingFirm", "召回公司",
                "brandName", "brand name",
                "productDescription", "产品描述",
                "dateFrom", "开始日期(YYYY-MM-DD)",
                "dateTo", "结束日期(YYYY-MM-DD)",
                "maxPages", "最大爬取页数",
                "inputKeywords", "输入关键词列表"
            );
            case "usregistration", "us_registration" -> Map.of(
                "establishmentName", "设备名称（使用device_name搜索）",
                "proprietaryName", "专有名称（使用proprietary_name搜索）",
                "ownerOperatorName", "制造商名称（使用manufacturer_name搜索）",
                "maxPages", "最大爬取页数"
            );
            case "unicrawl" -> Map.of(
                "totalCount", "总爬取数量",
                "dateFrom", "开始日期(YYYY-MM-DD)",
                "dateTo", "结束日期(YYYY-MM-DD)",
                "inputKeywords", "输入关键词列表",
                "maxPages", "最大页数(0表示爬取所有)"
            );
            case "customs-case", "customscase" -> Map.of(
                "hsCode", "HS编码",
                "maxRecords", "最大记录数",
                "batchSize", "批次大小",
                "startDate", "开始日期(MM/DD/YYYY)",
                "inputKeywords", "输入关键词列表"
            );
            case "guidance" -> Map.of(
                "maxRecords", "最大记录数"
            );
            default -> Map.of("error", "未知的爬虫类型");
        };
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "crawlerType", crawlerType,
            "config", config
        ));
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查美国爬虫服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "美国爬虫服务正常运行",
            "timestamp", System.currentTimeMillis(),
            "availableCrawlers", new String[]{
                "us510k", "devent", "usevent", "usrecall", "usregistration", 
                "unicrawl", "customs-case", "guidance"
            },
            "disabledCrawlers", new String[]{
                "d510k", "drecall"
            }
        ));
    }
}




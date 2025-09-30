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
@RequestMapping("/us-crawler")
@Tag(name = "美国爬虫管理", description = "美国相关爬虫的测试和管理接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://localhost:3101", "http://127.0.0.1:3000", "http://127.0.0.1:3100", "http://127.0.0.1:3101"})
public class USCrawlerController {

    @Autowired
    private USCrawlerService usCrawlerService;

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
     * 执行GuidanceCrawler爬虫
     */
    @PostMapping("/execute/guidance")
    @Operation(summary = "执行GuidanceCrawler爬虫", description = "执行FDA指导文档爬虫进行数据爬取")
    public ResponseEntity<Map<String, Object>> executeGuidance(
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "10") int maxRecords) {
        
        log.info("收到GuidanceCrawler爬虫执行请求 - 最大记录数: {}", maxRecords);
        
        try {
            Map<String, Object> params = Map.of("maxRecords", maxRecords);
            Map<String, Object> result = usCrawlerService.testGuidance(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GuidanceCrawler爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "GuidanceCrawler爬虫执行失败: " + e.getMessage(),
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
     * 执行CustomsCaseCrawler爬虫
     */
    @PostMapping("/execute/customs-case")
    @Operation(summary = "执行CustomsCaseCrawler爬虫", description = "执行海关案例数据爬虫进行数据爬取，支持关键词列表")
    public ResponseEntity<Map<String, Object>> executeCustomsCase(
            @Parameter(description = "HS编码") @RequestParam(required = false) String hsCode,
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "10") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "10") int batchSize,
            @Parameter(description = "开始日期(MM/DD/YYYY)") @RequestParam(required = false) String startDate,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到CustomsCaseCrawler爬虫执行请求 - HS编码: {}, 最大记录数: {}, 批次大小: {}, 开始日期: {}, 关键词: {}", 
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
            log.error("CustomsCaseCrawler爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "CustomsCaseCrawler爬虫执行失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

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
     * 执行US_510K爬虫
     */
    @PostMapping("/execute/us510k")
    @Operation(summary = "执行US_510K爬虫", description = "执行FDA 510K设备数据爬虫进行数据爬取，支持关键词列表")
    public ResponseEntity<Map<String, Object>> executeUS510K(
            @Parameter(description = "设备名称关键词") @RequestParam(required = false) String deviceName,
            @Parameter(description = "申请人名称") @RequestParam(required = false) String applicantName,
            @Parameter(description = "trade_name关键词(使用openfda.device_name搜索)") @RequestParam(required = false) String tradeName,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(defaultValue = "0") int maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_510K爬虫执行请求 - 设备名称: {}, 申请人: {}, trade_name: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
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
            log.error("US_510K爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_510K爬虫执行失败: " + e.getMessage(),
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
     * 执行US_event爬虫
     */
    @PostMapping("/execute/usevent")
    @Operation(summary = "执行US_event爬虫", description = "执行FDA设备不良事件数据爬虫进行数据爬取，支持关键词列表")
    public ResponseEntity<Map<String, Object>> executeUSEvent(
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "制造商名称") @RequestParam(required = false) String manufacturer,
            @Parameter(description = "产品问题") @RequestParam(required = false) String productProblem,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(defaultValue = "0") int maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_event爬虫执行请求 - 设备名称: {}, 制造商: {}, 产品问题: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
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
            log.error("US_event爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_event爬虫执行失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

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
     * 执行US_recall_api爬虫
     */
    @PostMapping("/execute/usrecall")
    @Operation(summary = "执行US_recall_api爬虫", description = "执行FDA设备召回数据爬虫进行数据爬取，支持关键词列表")
    public ResponseEntity<Map<String, Object>> executeUSRecall(
            @Parameter(description = "召回公司") @RequestParam(required = false) String recallingFirm,
            @Parameter(description = "brand name") @RequestParam(required = false) String brandName,
            @Parameter(description = "产品描述") @RequestParam(required = false) String productDescription,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(defaultValue = "0") int maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {
        
        log.info("收到US_recall_api爬虫执行请求 - 召回公司: {}, brand name: {}, 产品描述: {}, 日期范围: {} - {}, 最大页数: {}, 关键词: {}", 
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
            log.error("US_recall_api爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_recall_api爬虫执行失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试US_registration爬虫
     */
    @PostMapping("/test/usregistration")
    @Operation(summary = "测试US_registration爬虫", description = "测试FDA设备注册数据爬虫")
    public ResponseEntity<Map<String, Object>> testUSRegistration(
            @RequestBody(required = false) Map<String, Object> params) {
        
        log.info("收到US_registration爬虫测试请求，参数: {}", params);
        
        try {
            Map<String, Object> result = usCrawlerService.testUSRegistration(params != null ? params : Map.of());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("US_registration爬虫测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_registration爬虫测试失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 执行US_registration爬虫
     */
    @PostMapping("/execute/usregistration")
    @Operation(summary = "执行US_registration爬虫", description = "执行FDA设备注册数据爬虫进行数据爬取，支持关键词列表")
    public ResponseEntity<Map<String, Object>> executeUSRegistration(
            @Parameter(description = "设备名称（使用device_name搜索）") @RequestParam(required = false) String establishmentName,
            @Parameter(description = "专有名称（使用proprietary_name搜索）") @RequestParam(required = false) String proprietaryName,
            @Parameter(description = "制造商名称（使用manufacturer_name搜索）") @RequestParam(required = false) String ownerOperatorName,
            @Parameter(description = "最大爬取页数，0表示爬取所有数据") @RequestParam(defaultValue = "0") int maxPages,
            @Parameter(description = "输入关键词列表") @RequestParam(required = false) String inputKeywords) {

        log.info("收到US_registration爬虫执行请求 - 机构/贸易名称: {}, 专有名称: {}, 所有者/经营者名称: {}, 最大页数: {}, 关键词: {}",
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
            log.error("US_registration爬虫执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "US_registration爬虫执行失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }
}
package com.certification.service.crawler.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 爬虫参数Schema注册中心
 * 定义每个爬虫支持的参数字段
 */
@Slf4j
@Service
public class CrawlerSchemaRegistry {
    
    private final Map<String, CrawlerSchema> schemaMap = new HashMap<>();
    
    /**
     * 初始化：注册所有爬虫的Schema
     */
    @PostConstruct
    public void init() {
        log.info("========== 开始注册爬虫参数Schema ==========");
        
        // 美国爬虫
        registerUS510KSchema();
        registerUSRecallSchema();
        registerUSRegistrationSchema();
        registerUSEventSchema();
        registerUSGuidanceSchema();
        registerUSCustomsCaseSchema();
        
        // 欧盟爬虫
        registerEURecallSchema();
        registerEURegistrationSchema();
        registerEUGuidanceSchema();
        registerEUCustomsCaseSchema();
        
        // 韩国爬虫
        registerKRRecallSchema();
        
        log.info("========== Schema注册完成 ==========");
        log.info("共注册 {} 个爬虫Schema", schemaMap.size());
    }
    
    /**
     * 美国510K爬虫Schema
     */
    private void registerUS510KSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_510K")
            .setCountryCode("US")
            .setCrawlerType("510K")
            .setDescription("美国510K设备数据爬虫")
            .setSupportsKeywordBatch(true)
            .setSupportsDateRange(true);
        
        // 特定参数字段
        schema.addField(CrawlerParameterField.keywordList("deviceNames", "设备名称列表")
            .setDescription("按设备名称搜索")
            .setPlaceholder("Skin Analyzer, 3D Scanner")
            .setRequired(false));
        
        schema.addField(CrawlerParameterField.keywordList("applicants", "申请人列表")
            .setDescription("按申请人/公司名称搜索")
            .setPlaceholder("Abbott, Medtronic")
            .setRequired(false));
        
        schema.addField(CrawlerParameterField.keywordList("tradeNames", "品牌名称列表")
            .setDescription("按品牌/商标名称搜索")
            .setPlaceholder("VISIA, Observ")
            .setRequired(false));
        
        // 通用参数
        addCommonFields(schema);
        
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 美国召回爬虫Schema
     */
    private void registerUSRecallSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_Recall")
            .setCountryCode("US")
            .setCrawlerType("RECALL")
            .setDescription("美国设备召回数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("brandNames", "品牌名称列表")
            .setDescription("按品牌名称搜索召回记录")
            .setPlaceholder("Medtronic, Abbott"));
        
        schema.addField(CrawlerParameterField.keywordList("recallingFirms", "召回公司列表")
            .setDescription("按召回公司搜索")
            .setPlaceholder("Boston Scientific"));
        
        schema.addField(CrawlerParameterField.keywordList("productDescriptions", "产品描述列表")
            .setDescription("按产品描述关键词搜索")
            .setPlaceholder("Skin, Analyzer"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 美国设备注册爬虫Schema
     */
    private void registerUSRegistrationSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_Registration")
            .setCountryCode("US")
            .setCrawlerType("REGISTRATION")
            .setDescription("美国设备注册数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("manufacturerNames", "制造商名称列表")
            .setDescription("按制造商名称搜索")
            .setPlaceholder("Abbott Laboratories, Medtronic"));
        
        schema.addField(CrawlerParameterField.keywordList("deviceNames", "设备名称列表")
            .setDescription("按设备名称搜索")
            .setPlaceholder("Skin Analyzer, Medical Device"));
        
        schema.addField(CrawlerParameterField.keywordList("proprietaryNames", "专有名称列表")
            .setDescription("按专有/商标名称搜索")
            .setPlaceholder("VISIA, Observ"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 美国事件报告爬虫Schema
     */
    private void registerUSEventSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_Event")
            .setCountryCode("US")
            .setCrawlerType("EVENT")
            .setDescription("美国设备事件报告爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("brandNames", "品牌名称列表")
            .setDescription("按品牌名称搜索事件")
            .setPlaceholder("Skin Analyzer"));
        
        schema.addField(CrawlerParameterField.keywordList("manufacturerNames", "制造商名称列表")
            .setDescription("按制造商搜索")
            .setPlaceholder("Abbott"));
        
        schema.addField(CrawlerParameterField.keywordList("genericNames", "通用名称列表")
            .setDescription("按通用名称搜索")
            .setPlaceholder("Imaging System"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 美国指导文档爬虫Schema
     */
    private void registerUSGuidanceSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_Guidance")
            .setCountryCode("US")
            .setCrawlerType("GUIDANCE")
            .setDescription("美国FDA指导文档爬虫")
            .setSupportsKeywordBatch(false);  // 不支持关键词搜索
        
        // 只有通用参数（maxRecords等）
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 美国海关判例爬虫Schema
     */
    private void registerUSCustomsCaseSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("US_CustomsCase")
            .setCountryCode("US")
            .setCrawlerType("CUSTOMS")
            .setDescription("美国海关判例数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("hsCodeKeywords", "HS编码列表")
            .setDescription("按HS编码搜索判例")
            .setPlaceholder("9018, 8543"));
        
        schema.addField(CrawlerParameterField.keywordList("rulingKeywords", "裁定关键词列表")
            .setDescription("在裁定结果中搜索")
            .setPlaceholder("medical, device"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 欧盟召回爬虫Schema
     */
    private void registerEURecallSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("EU_Recall")
            .setCountryCode("EU")
            .setCrawlerType("RECALL")
            .setDescription("欧盟设备召回数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("searchKeywords", "搜索关键词列表")
            .setDescription("在召回数据中搜索")
            .setPlaceholder("Medical Device, Safety"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 欧盟设备注册爬虫Schema
     */
    private void registerEURegistrationSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("EU_Registration")
            .setCountryCode("EU")
            .setCrawlerType("REGISTRATION")
            .setDescription("欧盟设备注册数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("tradeNames", "商标名称列表")
            .setDescription("按商标名称搜索")
            .setPlaceholder("VISIA"));
        
        schema.addField(CrawlerParameterField.keywordList("manufacturerNames", "制造商名称列表")
            .setDescription("按制造商搜索")
            .setPlaceholder("Canfield"));
        
        schema.addField(CrawlerParameterField.keywordList("riskClasses", "风险等级列表")
            .setDescription("按风险等级搜索")
            .setPlaceholder("I, IIa, IIb, III"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 欧盟指导文档爬虫Schema
     */
    private void registerEUGuidanceSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("EU_Guidance")
            .setCountryCode("EU")
            .setCrawlerType("GUIDANCE")
            .setDescription("欧盟医疗设备新闻爬虫")
            .setSupportsKeywordBatch(false);  // 不支持关键词搜索
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 欧盟海关判例爬虫Schema
     */
    private void registerEUCustomsCaseSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("EU_CustomsCase")
            .setCountryCode("EU")
            .setCrawlerType("CUSTOMS")
            .setDescription("欧盟TARIC海关数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("taricCodes", "TARIC编码列表")
            .setDescription("TARIC编码搜索（每20个为一批）")
            .setPlaceholder("9018, 9019"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 韩国召回爬虫Schema
     */
    private void registerKRRecallSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("KR_Recall")
            .setCountryCode("KR")
            .setCrawlerType("RECALL")
            .setDescription("韩国MFDS召回数据爬虫");
        
        schema.addField(CrawlerParameterField.keywordList("searchKeywords", "搜索关键词列表")
            .setDescription("在召回数据中搜索（支持韩文）")
            .setPlaceholder("의료기기, 리콜"));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 添加通用字段（所有爬虫都支持）
     */
    private void addCommonFields(CrawlerSchema schema) {
        schema.addCommonField(CrawlerParameterField.number("maxRecords", "最大记录数", -1)
            .setDescription("-1表示爬取所有数据")
            .setPlaceholder("-1"));
        
        schema.addCommonField(CrawlerParameterField.number("batchSize", "批次大小", 100)
            .setDescription("每次请求的数据量")
            .setPlaceholder("100"));
        
        schema.addCommonField(CrawlerParameterField.number("recentDays", "最近天数", null)
            .setDescription("爬取最近N天的数据（可选）")
            .setPlaceholder("30"));
        
        schema.addCommonField(CrawlerParameterField.text("dateFrom", "开始日期")
            .setDescription("日期格式：yyyyMMdd")
            .setPlaceholder("20240101"));
        
        schema.addCommonField(CrawlerParameterField.text("dateTo", "结束日期")
            .setDescription("日期格式：yyyyMMdd")
            .setPlaceholder("20241231"));
    }
    
    /**
     * 获取爬虫Schema
     */
    public CrawlerSchema getSchema(String crawlerName) {
        return schemaMap.get(crawlerName);
    }
    
    /**
     * 获取所有Schema
     */
    public List<CrawlerSchema> getAllSchemas() {
        return new ArrayList<>(schemaMap.values());
    }
    
    /**
     * 获取Schema映射（用于前端）
     */
    public Map<String, Map<String, Object>> getSchemasForFrontend() {
        Map<String, Map<String, Object>> schemas = new HashMap<>();
        
        for (Map.Entry<String, CrawlerSchema> entry : schemaMap.entrySet()) {
            CrawlerSchema schema = entry.getValue();
            Map<String, Object> schemaData = new HashMap<>();
            
            schemaData.put("crawlerName", schema.getCrawlerName());
            schemaData.put("countryCode", schema.getCountryCode());
            schemaData.put("crawlerType", schema.getCrawlerType());
            schemaData.put("description", schema.getDescription());
            schemaData.put("supportsKeywordBatch", schema.getSupportsKeywordBatch());
            schemaData.put("supportsDateRange", schema.getSupportsDateRange());
            schemaData.put("fields", schema.getFields());
            schemaData.put("commonFields", schema.getCommonFields());
            
            schemas.put(entry.getKey(), schemaData);
        }
        
        return schemas;
    }
    
    /**
     * 验证参数是否符合Schema
     */
    public boolean validateParams(String crawlerName, Map<String, Object> params) {
        CrawlerSchema schema = getSchema(crawlerName);
        if (schema == null) {
            log.warn("未找到爬虫Schema: {}", crawlerName);
            return false;
        }
        
        // 验证必填字段
        for (CrawlerParameterField field : schema.getAllFields()) {
            if (field.getRequired() && !params.containsKey(field.getFieldName())) {
                log.warn("缺少必填字段: {}", field.getFieldName());
                return false;
            }
        }
        
        return true;
    }
}


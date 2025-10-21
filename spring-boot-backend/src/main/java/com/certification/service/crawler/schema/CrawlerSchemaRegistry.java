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
        registerKRRegistrationSchema();
        registerKREventSchema();
        registerKRGuidanceSchema();
        registerKRCustomsCaseSchema();

        // 日本爬虫
        registerJPRecallSchema();
        registerJPGuidanceSchema();
        registerJPRegistrationSchema();

        // 台湾爬虫
        registerTWRegistrationSchema();
        registerTWCustomsCaseSchema();
        registerTWGuidanceSchema();
        registerTWRecallSchema();

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
        
        // 特定参数字段（根据FDA API文档）
        schema.addField(CrawlerParameterField.keywordList("deviceNames", "设备名称列表")
            .setDescription("按设备名称搜索 (device_name)")
            .setPlaceholder("Skin Analyzer, 3D Scanner")
            .setRequired(false));
        
        schema.addField(CrawlerParameterField.keywordList("applicantNames", "申请人列表")
            .setDescription("按申请人/公司名称搜索 (applicant)")
            .setPlaceholder("Abbott, Medtronic")
            .setRequired(false));
        
        schema.addField(CrawlerParameterField.keywordList("tradeNames", "商品名称列表")
            .setDescription("按商品/商标名称搜索 (openfda.trade_name)")
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
            .setDescription("韩国MFDS召回数据爬虫")
            .setSupportsKeywordBatch(true);
        
        // 公司名称列表
        schema.addField(CrawlerParameterField.keywordList("companyNames", "公司名称列表")
            .setDescription("按公司名称搜索（支持韩文）")
            .setPlaceholder("(주)필립스코리아, 삼성전자"));
        
        // 产品名称列表
        schema.addField(CrawlerParameterField.keywordList("itemNames", "产品名称列表")
            .setDescription("按产品名称搜索（支持韩文）")
            .setPlaceholder("이동형 엑스선 투시 촬영장치, 초음파 영상진단장치"));
        
        // 兼容旧的搜索关键词（可选）
        schema.addField(CrawlerParameterField.keywordList("searchKeywords", "搜索关键词列表（旧版）")
            .setDescription("通用搜索关键词（兼容旧版本）")
            .setPlaceholder("의료기기, 리콜")
            .setRequired(false));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 韩国注册记录爬虫Schema
     */
    private void registerKRRegistrationSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("KR_Registration")
            .setCountryCode("KR")
            .setCrawlerType("REGISTRATION")
            .setDescription("韩国MFDS医疗器械注册数据爬虫")
            .setSupportsKeywordBatch(true);
        
        // 搜索关键词（query参数）
        schema.addField(CrawlerParameterField.keywordList("searchQueries", "搜索关键词列表")
            .setDescription("在注册数据中搜索（支持韩文，对应API的query参数）")
            .setPlaceholder("심전계, 초음파")
            .setRequired(false));
        
        // 企业名称列表（entpName参数）
        schema.addField(CrawlerParameterField.keywordList("companyNames", "企业名称列表")
            .setDescription("按企业名称搜索（支持韩文，对应API的entpName参数）")
            .setPlaceholder("(주)필립스코리아, 삼성전자")
            .setRequired(false));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 韩国不良事件爬虫Schema
     */
    private void registerKREventSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("KR_Event")
            .setCountryCode("KR")
            .setCrawlerType("EVENT")
            .setDescription("韩国MFDS医疗器械不良事件数据爬虫")
            .setSupportsKeywordBatch(true);
        
        // 企业名称列表（searchPentpNm参数）
        schema.addField(CrawlerParameterField.keywordList("companyNames", "企业名称列表")
            .setDescription("按企业名称搜索（支持韩文，对应API的searchPentpNm参数）")
            .setPlaceholder("웨펜메디칼아이엘(주), 삼성전자")
            .setRequired(false));
        
        // 产品名称列表（searchPrdtNm参数）
        schema.addField(CrawlerParameterField.keywordList("productNames", "产品名称列表")
            .setDescription("按产品名称搜索（支持韩文，对应API的searchPrdtNm参数）")
            .setPlaceholder("혈액응고검사시약, 초음파영상진단장치")
            .setRequired(false));
        
        // 型号名称列表（searchModelnm参数）
        schema.addField(CrawlerParameterField.keywordList("modelNames", "型号名称列表")
            .setDescription("按型号名称搜索（支持韩文，对应API的searchModelnm参数）")
            .setPlaceholder("HemosIL RecombiPlasTin 2G")
            .setRequired(false));
        
        addCommonFields(schema);
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 韩国指导文档爬虫Schema
     */
    private void registerKRGuidanceSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("KR_Guidance")
            .setCountryCode("KR")
            .setCrawlerType("GUIDANCE")
            .setDescription("韩国MFDS医疗器械指导文档爬虫")
            .setSupportsKeywordBatch(true);
        
        // 搜索关键词列表（searchKwd参数）
        schema.addField(CrawlerParameterField.keywordList("searchKeywords", "搜索关键词列表")
            .setDescription("在指导文档中搜索（支持韩文，对应API的searchKwd参数）")
            .setPlaceholder("의료기기, 가이드라인, 규정")
            .setRequired(false));
        
        // 注意：Guidance爬虫不使用dateFrom/dateTo，所以只添加maxRecords和batchSize
        schema.addCommonField(CrawlerParameterField.number("maxRecords", "最大记录数", -1)
            .setDescription("-1表示爬取所有数据")
            .setPlaceholder("-1"));
        
        schema.addCommonField(CrawlerParameterField.number("batchSize", "批次大小", 100)
            .setDescription("每次请求的数据量")
            .setPlaceholder("100"));
        
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 韩国海关案例爬虫Schema
     */
    private void registerKRCustomsCaseSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("KR_CustomsCase")
            .setCountryCode("KR")
            .setCrawlerType("CUSTOMS_CASE")
            .setDescription("韩国海关案例数据爬虫")
            .setSupportsKeywordBatch(true);
        
        // 搜索关键词列表（searchWord参数）
        schema.addField(CrawlerParameterField.keywordList("searchQueries", "搜索关键词列表")
            .setDescription("在海关案例中搜索（支持韩文，对应API的searchWord参数）")
            .setRequired(false)
            .setPlaceholder("医疗器械, 의료기기"));
        
        // 产品名称列表（productName参数）
        schema.addField(CrawlerParameterField.keywordList("productNames", "产品名称列表")
            .setDescription("按产品名称搜索（支持韩文，对应API的productName参数）")
            .setRequired(false)
            .setPlaceholder("심장박동기, 심박조율기"));
        
        // 添加通用字段
        addCommonFields(schema);
        
        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }
    
    /**
     * 日本召回记录爬虫Schema
     */
    private void registerJPRecallSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("JP_Recall")
            .setCountryCode("JP")
            .setCrawlerType("RECALL")
            .setDescription("日本PMDA医疗器械召回记录爬虫")
            .setSupportsKeywordBatch(true);

        // 贩卖商列表（txtSaleName参数）
        schema.addField(CrawlerParameterField.keywordList("sellers", "贩卖商列表")
            .setDescription("按贩卖商名称搜索（支持日文，对应API的txtSaleName参数）")
            .setRequired(false)
            .setPlaceholder("フィリップス・ジャパン, テルモ株式会社"));

        // 制造商列表（txtCompName参数）
        schema.addField(CrawlerParameterField.keywordList("manufacturers", "制造商列表")
            .setDescription("按制造商名称搜索（支持日文，对应API的txtCompName参数）")
            .setRequired(false)
            .setPlaceholder("メドトロニック, ボストン・サイエンティフィック"));

        // 召回年份列表（cboYear参数）
        schema.addField(CrawlerParameterField.keywordList("years", "召回年份列表")
            .setDescription("按召回年份搜索（对应API的cboYear参数）")
            .setRequired(false)
            .setPlaceholder("2024, 2023, 2022"));

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 日本法规指导文档爬虫Schema
     */
    private void registerJPGuidanceSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("JP_Guidance")
            .setCountryCode("JP")
            .setCrawlerType("GUIDANCE")
            .setDescription("日本PMDA法规指导文档爬虫 - 搜索认证基准、审批基准和指导方针")
            .setSupportsKeywordBatch(true);

        // 搜索关键词（Q_kjn_kname参数）
        schema.addField(CrawlerParameterField.keywordList("searchKeyword", "法规标题搜索关键词")
            .setDescription("在法规标题(Title of criteria)和用途(Intended use)中搜索，支持日文和英文，对应API的Q_kjn_kname参数")
            .setRequired(false)
            .setPlaceholder("skin, 皮膚, 医療機器, analyzer, ガイドライン"));

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 日本注册记录爬虫Schema
     */
    private void registerJPRegistrationSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("JP_Registration")
            .setCountryCode("JP")
            .setCrawlerType("REGISTRATION")
            .setDescription("日本PMDA医疗器械注册记录爬虫")
            .setSupportsKeywordBatch(true);

        // 关键词列表（搜索设备名称）
        schema.addField(CrawlerParameterField.keywordList("keywords", "搜索关键词列表")
            .setDescription("在设备名称中搜索（支持日文和英文）")
            .setRequired(false)
            .setPlaceholder("skin, 皮膚, heart, 心臓, スキンアナライザー, 超音波診断装置"));

        // 公司名称列表（companyName参数）
        schema.addField(CrawlerParameterField.keywordList("companyNames", "公司名称列表")
            .setDescription("按公司名称搜索（支持日文，对应API的txtCompName参数）")
            .setRequired(false)
            .setPlaceholder("株式会社Xenoma, フィリップス・ジャパン"));

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 台湾注册记录爬虫Schema
     * 支持4个搜索参数的任意组合：applicantNames, factoryNames, prodNameC, prodNameE
     */
    private void registerTWRegistrationSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("TW_Registration")
            .setCountryCode("TW")
            .setCrawlerType("REGISTRATION")
            .setDescription("台湾FDA医疗器械注册记录爬虫 - 爬取台湾食品药物管理署的医疗器械许可数据（支持4个参数任意组合搜索）")
            .setSupportsKeywordBatch(true);

        // 申请人名称列表（applicantName参数）
        schema.addField(CrawlerParameterField.keywordList("applicantNames", "申请人名称列表")
            .setDescription("按申请人名称搜索（支持中文和英文）")
            .setRequired(false)
            .setPlaceholder("醫樺儀器有限公司, 壯生醫療器材股份有限公司"));

        // 制造商/工厂名称列表（factoryName参数）
        schema.addField(CrawlerParameterField.keywordList("factoryNames", "制造商/工厂名称列表")
            .setDescription("按制造商或工厂名称搜索（支持中文和英文）")
            .setRequired(false)
            .setPlaceholder("ETHICON, LLC, 昭惠實業股份有限公司"));

        // 中文产品名称列表（prodNameC参数）
        schema.addField(CrawlerParameterField.keywordList("prodNameC", "中文产品名称列表")
            .setDescription("按中文产品名称搜索")
            .setRequired(false)
            .setPlaceholder("皮膚分析儀, 心臟監測器, 醫療設備"));

        // 英文产品名称列表（prodNameE参数）
        schema.addField(CrawlerParameterField.keywordList("prodNameE", "英文产品名称列表")
            .setDescription("按英文产品名称搜索")
            .setRequired(false)
            .setPlaceholder("skin analyzer, heart monitor, medical device"));

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 台湾海关判例爬虫Schema
     */
    private void registerTWCustomsCaseSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("TW_CustomsCase")
            .setCountryCode("TW")
            .setCrawlerType("CUSTOMS_CASE")
            .setDescription("台湾海关判例爬虫 - 爬取台湾经济部国际贸易局的货品输出入规定公告异动资料")
            .setSupportsKeywordBatch(true)
            .setSupportsDateRange(true);

        // CCC号列列表
        schema.addField(CrawlerParameterField.keywordList("cccCodes", "CCC号列列表")
            .setDescription("按CCC货品分类代码搜索（台湾海关使用的商品分类编码）")
            .setRequired(false)
            .setPlaceholder("90189010, 90189020, 85437090"));

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 台湾法规文档爬虫Schema
     */
    private void registerTWGuidanceSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("TW_Guidance")
            .setCountryCode("TW")
            .setCrawlerType("GUIDANCE")
            .setDescription("台湾法规文档爬虫 - 爬取台湾食品药物管理署医疗器材相关法规");

        // 说明：台湾法规爬虫默认爬取所有医疗器材法规，不需要额外参数

        // 添加通用字段
        addCommonFields(schema);

        schemaMap.put(schema.getCrawlerName(), schema);
        log.info("注册Schema: {}", schema.getCrawlerName());
    }

    /**
     * 台湾召回记录爬虫Schema
     */
    private void registerTWRecallSchema() {
        CrawlerSchema schema = new CrawlerSchema()
            .setCrawlerName("TW_Recall")
            .setCountryCode("TW")
            .setCrawlerType("RECALL")
            .setDescription("台湾召回记录爬虫 - 爬取台湾食品药物管理署医疗器材回收警讯资料")
            .setSupportsKeywordBatch(true)
            .setSupportsDateRange(true);

        // 设备名称列表
        schema.addField(CrawlerParameterField.keywordList("deviceNames", "设备名称列表")
            .setDescription("按医疗器材名称搜索召回记录（支持中文和英文）")
            .setRequired(false)
            .setPlaceholder("導引鞘, 心臟導管, 呼吸器"));

        // 添加通用字段
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


package com.certification.crawler.countrydata.us;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.entity.common.DeviceRecallRecord;
import com.certification.entity.common.CrawlerCheckpoint;
import com.certification.repository.common.DeviceRecallRecordRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.certification.util.RiskLevelUtil;
import com.certification.util.KeywordUtil;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * FDA设备召回数据爬取器
 * 专门用于爬取FDA设备召回数据并保存到数据库
 * 参考DeviceRecallCrawler.java的爬取方法，使用US_510K.java的格式结构
 */
@Component
public class US_recall_api {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;

    @Autowired
    private CrawlerCheckpointRepository crawlerCheckpointRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;

    public US_recall_api() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 多格式 LocalDate 反序列化器，支持 yyyyMMdd / yyyy-MM-dd / MM/dd/yyyy
     */
    static class MultiFormatLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        private static final String[] PATTERNS = {"yyyyMMdd", "yyyy-MM-dd", "MM/dd/yyyy"};
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.trim().isEmpty()) return null;
            for (String pattern : PATTERNS) {
                try {
                    return LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
            // 无法解析则抛出原始异常便于排查数据问题
            throw new UncheckedIOException(new IOException("无法解析日期: " + text));
        }
    }

    /**
     * FDA API响应模型
     */
    public static class FDAResponse {
        @JsonProperty("results")
        private List<MedicalDeviceRecall> results;

        @JsonProperty("meta")
        private Meta meta;

        public List<MedicalDeviceRecall> getResults() { return results; }
        public void setResults(List<MedicalDeviceRecall> results) { this.results = results; }
        public Meta getMeta() { return meta; }
        public void setMeta(Meta meta) { this.meta = meta; }
    }

    /**
     * 元数据模型
     */
    public static class Meta {
        @JsonProperty("results")
        private ResultsMeta results;

        public ResultsMeta getResults() { return results; }
        public void setResults(ResultsMeta results) { this.results = results; }
    }

    /**
     * 结果元数据模型
     */
    public static class ResultsMeta {
        @JsonProperty("total")
        private Integer total;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
    }

    /**
     * 设备召回数据模型（对齐新需求：MedicalDeviceRecall）
     */
    public static class MedicalDeviceRecall {
        // 召回唯一标识
        @JsonProperty("cfres_id")
        private String cfresId; // 召回事件 ID（核心唯一标识）
        @JsonProperty("product_res_number")
        private String productResNumber; // 产品召回编号
        @JsonProperty("res_event_number")
        private String resEventNumber; // 召回事件编号

        // 召回时间线（关键节点）
        @JsonProperty("event_date_initiated")
        @JsonDeserialize(using = MultiFormatLocalDateDeserializer.class)
        private LocalDate eventDateInitiated; // 召回启动日期
        @JsonProperty("event_date_posted")
        @JsonDeserialize(using = MultiFormatLocalDateDeserializer.class)
        private LocalDate eventDatePosted; // 召回发布日期
        @JsonProperty("event_date_terminated")
        @JsonDeserialize(using = MultiFormatLocalDateDeserializer.class)
        private LocalDate eventDateTerminated; // 召回终止日期

        // 召回状态
        @JsonProperty("recall_status")
        private String recallStatus; // 召回状态

        // 涉事产品核心信息
        @JsonProperty("product_code")
        private String productCode; // 产品代码（FDA 分类标识）
        @JsonProperty("product_description")
        private String productDescription; // 产品描述（含型号、用途）
        @JsonProperty("code_info")
        private String codeInfo; // 产品编码信息（如部件号）
        @JsonProperty("k_numbers")
        private List<String> kNumbers; // 产品审批 K 编号列表

        // 召回责任方信息
        @JsonProperty("recalling_firm")
        private String recallingFirm; // 召回公司名称
        // 注意：FDA 原始数据通常是分散的地址字段（address_1/city/state/zip），此处为汇总字段
        private String recallingFirmAddress; // 召回公司完整地址（合并街道、城市、州、邮编）

        // 召回核心原因与处理
        @JsonProperty("reason_for_recall")
        private String reasonForRecall; // 召回原因
        @JsonProperty("root_cause_description")
        private String rootCauseDescription; // 根本原因
        @JsonProperty("action")
        private String action; // 采取的措施

        // 召回范围信息
        @JsonProperty("product_quantity")
        private String productQuantity; // 涉事产品数量（如 311 units）
        @JsonProperty("distribution_pattern")
        private String distributionPattern; // 分销范围

        // 设备分类与合规信息
        @JsonProperty("device_name")
        private String deviceName; // FDA 统一设备名称
        @JsonProperty("medical_specialty")
        private String medicalSpecialty; // 所属医疗领域
        @JsonProperty("regulation_number")
        private String regulationNumber; // 对应 FDA 法规编号
        @JsonProperty("device_class")
        private String deviceClass; // 设备风险等级（1/2/3 类）
        // 数据适用国家
        private String jdCountry;

        // Getters and Setters
        public String getCfresId() { return cfresId; }
        public void setCfresId(String cfresId) { this.cfresId = cfresId; }
        public String getProductResNumber() { return productResNumber; }
        public void setProductResNumber(String productResNumber) { this.productResNumber = productResNumber; }
        public String getResEventNumber() { return resEventNumber; }
        public void setResEventNumber(String resEventNumber) { this.resEventNumber = resEventNumber; }
        public LocalDate getEventDateInitiated() { return eventDateInitiated; }
        public void setEventDateInitiated(LocalDate eventDateInitiated) { this.eventDateInitiated = eventDateInitiated; }
        public LocalDate getEventDatePosted() { return eventDatePosted; }
        public void setEventDatePosted(LocalDate eventDatePosted) { this.eventDatePosted = eventDatePosted; }
        public LocalDate getEventDateTerminated() { return eventDateTerminated; }
        public void setEventDateTerminated(LocalDate eventDateTerminated) { this.eventDateTerminated = eventDateTerminated; }
        public String getRecallStatus() { return recallStatus; }
        public void setRecallStatus(String recallStatus) { this.recallStatus = recallStatus; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public String getProductDescription() { return productDescription; }
        public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
        public String getCodeInfo() { return codeInfo; }
        public void setCodeInfo(String codeInfo) { this.codeInfo = codeInfo; }
        public List<String> getKNumbers() { return kNumbers; }
        public void setKNumbers(List<String> kNumbers) { this.kNumbers = kNumbers; }
        public String getRecallingFirm() { return recallingFirm; }
        public void setRecallingFirm(String recallingFirm) { this.recallingFirm = recallingFirm; }
        public String getRecallingFirmAddress() { return recallingFirmAddress; }
        public void setRecallingFirmAddress(String recallingFirmAddress) { this.recallingFirmAddress = recallingFirmAddress; }
        public String getReasonForRecall() { return reasonForRecall; }
        public void setReasonForRecall(String reasonForRecall) { this.reasonForRecall = reasonForRecall; }
        public String getRootCauseDescription() { return rootCauseDescription; }
        public void setRootCauseDescription(String rootCauseDescription) { this.rootCauseDescription = rootCauseDescription; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getProductQuantity() { return productQuantity; }
        public void setProductQuantity(String productQuantity) { this.productQuantity = productQuantity; }
        public String getDistributionPattern() { return distributionPattern; }
        public void setDistributionPattern(String distributionPattern) { this.distributionPattern = distributionPattern; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getMedicalSpecialty() { return medicalSpecialty; }
        public void setMedicalSpecialty(String medicalSpecialty) { this.medicalSpecialty = medicalSpecialty; }
        public String getRegulationNumber() { return regulationNumber; }
        public void setRegulationNumber(String regulationNumber) { this.regulationNumber = regulationNumber; }
        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
        public String getJdCountry() { return jdCountry; }
        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }
    }

    /**
     * 记录爬取信息到CrawlerCheckpoint表
     */
    private void recordCrawlerInfo(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo,
                                   int totalFetched, int totalSaved, String status, String errorMessage) {
        try {
            CrawlerCheckpoint checkpoint = new CrawlerCheckpoint();
            checkpoint.setCrawlerType("US_Recall");
            checkpoint.setSearchTerm(searchTerm);
            checkpoint.setDateFrom(dateFrom);
            checkpoint.setDateTo(dateTo);
            checkpoint.setCurrentSkip(0);
            checkpoint.setTotalFetched(totalFetched);
            checkpoint.setTargetTotal(maxRecords == -1 ? null : maxRecords);
            checkpoint.setBatchSize(batchSize);
            checkpoint.setStatus(CrawlerCheckpoint.CrawlerStatus.valueOf(status));
            checkpoint.setErrorMessage(errorMessage);
            checkpoint.setLastUpdated(java.time.LocalDateTime.now());
            checkpoint.setCreatedTime(java.time.LocalDateTime.now());

            crawlerCheckpointRepository.save(checkpoint);
            System.out.println("爬取信息已记录到CrawlerCheckpoint表，ID: " + checkpoint.getId());
        } catch (Exception e) {
            System.err.println("记录爬取信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索字段类型枚举
     */
    public enum SearchFieldType {
        BRAND_NAME("openfda.brand_name", "品牌名称"),
        RECALLING_FIRM("recalling_firm", "召回公司"),
        PRODUCT_DESCRIPTION("product_description", "产品描述"),
        PRODUCT_CODE("product_code", "产品代码"),
        REASON_FOR_RECALL("reason_for_recall", "召回原因"),
        CUSTOM("", "自定义查询");
        
        private final String fieldName;
        private final String description;
        
        SearchFieldType(String fieldName, String description) {
            this.fieldName = fieldName;
            this.description = description;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 召回数据爬取参数构建器
     * 使用Builder模式，支持链式调用，支持多种灵活的查询方式
     */
    public static class RecallCrawlerParams {
        // 单个字段查询
        private String brandName;
        private String recallingFirm;
        private String productDescription;
        
        // 关键词列表（用于批量查询）
        private List<String> keywords;
        
        // 自定义查询
        private String customSearchTerm;
        
        // 查询参数
        private int maxRecords = 100;
        private int batchSize = 20;
        
        // 时间范围
        private String dateFrom;
        private String dateTo;
        private Integer recentDays; // 最近N天
        
        public RecallCrawlerParams() {}
        
        // ========== 单字段查询方法 ==========
        
        /**
         * 设置品牌名称查询
         */
        public RecallCrawlerParams brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }
        
        /**
         * 设置召回公司查询
         */
        public RecallCrawlerParams recallingFirm(String recallingFirm) {
            this.recallingFirm = recallingFirm;
            return this;
        }
        
        /**
         * 设置产品描述查询
         */
        public RecallCrawlerParams productDescription(String productDescription) {
            this.productDescription = productDescription;
            return this;
        }
        
        // ========== 兼容旧方法名 ==========
        
        public RecallCrawlerParams byBrandName(String brandName) {
            return brandName(brandName);
        }
        
        public RecallCrawlerParams byRecallingFirm(String firmName) {
            return recallingFirm(firmName);
        }
        
        public RecallCrawlerParams byProductDescription(String description) {
            return productDescription(description);
        }
        
        // ========== 关键词列表查询 ==========
        
        /**
         * 设置关键词列表（会在品牌、公司、描述三个字段中查询）
         */
        public RecallCrawlerParams keywords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }
        
        /**
         * 设置关键词列表（可变参数形式）
         */
        public RecallCrawlerParams keywords(String... keywords) {
            this.keywords = Arrays.asList(keywords);
            return this;
        }
        
        // ========== 自定义查询 ==========
        
        /**
         * 设置自定义搜索词（直接传入完整的搜索表达式）
         */
        public RecallCrawlerParams customSearch(String searchTerm) {
            this.customSearchTerm = searchTerm;
            return this;
        }
        
        // ========== 数量参数 ==========
        
        /**
         * 设置最大记录数
         */
        public RecallCrawlerParams maxRecords(int maxRecords) {
            this.maxRecords = maxRecords;
            return this;
        }
        
        /**
         * 设置批次大小
         */
        public RecallCrawlerParams batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }
        
        // ========== 时间范围参数 ==========
        
        /**
         * 设置日期范围
         */
        public RecallCrawlerParams dateRange(String dateFrom, String dateTo) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.recentDays = null; // 清除recentDays
            return this;
        }
        
        /**
         * 设置最近N天（自动计算日期范围，考虑FDA数据延迟）
         * @param days 天数（建议30-365天，避免最近30天内的数据）
         */
        public RecallCrawlerParams recentDays(int days) {
            this.recentDays = days;
            // 考虑FDA数据延迟（30天），计算实际查询日期
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.minusDays(30); // 结束日期：30天前
            LocalDate startDate = endDate.minusDays(days); // 起始日期：再往前N天
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            this.dateFrom = startDate.format(formatter);
            this.dateTo = endDate.format(formatter);
            
            System.out.println("自动计算日期范围（考虑FDA 30天延迟）: " + dateFrom + " - " + dateTo);
            return this;
        }
        
        /**
         * 设置最近N天（不考虑延迟，直接计算）
         * @param days 天数
         * @param ignoreDelay 是否忽略延迟（true=直接计算到今天）
         */
        public RecallCrawlerParams recentDays(int days, boolean ignoreDelay) {
            if (ignoreDelay) {
                LocalDate today = LocalDate.now();
                LocalDate startDate = today.minusDays(days);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                this.dateFrom = startDate.format(formatter);
                this.dateTo = today.format(formatter);
            } else {
                return recentDays(days);
            }
            return this;
        }
        
        // ========== 内部方法 ==========
        
        /**
         * 检查是否有搜索条件
         */
        boolean hasSearchConditions() {
            return brandName != null || recallingFirm != null || 
                   productDescription != null || customSearchTerm != null;
        }
        
        /**
         * 检查是否使用关键词列表模式
         */
        boolean isKeywordsMode() {
            return keywords != null && !keywords.isEmpty();
        }
        
        // Getters
        public String getBrandName() { return brandName; }
        public String getRecallingFirm() { return recallingFirm; }
        public String getProductDescription() { return productDescription; }
        public List<String> getKeywords() { return keywords; }
        public String getCustomSearchTerm() { return customSearchTerm; }
        public int getMaxRecords() { return maxRecords; }
        public int getBatchSize() { return batchSize; }
        public String getDateFrom() { return dateFrom; }
        public String getDateTo() { return dateTo; }
        public Integer getRecentDays() { return recentDays; }
    }
    
    /**
     * 统一的智能爬取方法（推荐使用）⭐
     * 支持多种查询模式的自动识别和处理
     * 
     * @param params 爬取参数
     * @return 爬取结果信息
     * 
     * 支持的查询模式：
     * <pre>
     * 1. 单字段查询：
     *    crawl(new RecallCrawlerParams()
     *        .brandName("Medtronic")
     *        .dateRange("20240101", "20240630"));
     * 
     * 2. 多字段组合查询：
     *    crawl(new RecallCrawlerParams()
     *        .brandName("Heartware")
     *        .recallingFirm("Abbott")
     *        .dateRange("20240101", "20240630"));
     * 
     * 3. 关键词列表批量查询：
     *    crawl(new RecallCrawlerParams()
     *        .keywords("Abbott", "Medtronic", "Boston Scientific")
     *        .dateRange("20240101", "20240630"));
     * 
     * 4. 最近N天查询：
     *    crawl(new RecallCrawlerParams()
     *        .recentDays(60)  // 自动计算日期范围，考虑30天延迟
     *        .recallingFirm("Medtronic"));
     * 
     * 5. 自定义查询：
     *    crawl(new RecallCrawlerParams()
     *        .customSearch("product_code:FOZ+AND+recalling_firm:Medtronic"));
     * </pre>
     */
    public String crawl(RecallCrawlerParams params) {
        // 模式1：关键词列表批量查询
        if (params.isKeywordsMode()) {
            return handleKeywordsBatchCrawl(params);
        }
        
        // 模式2：自定义查询
        if (params.getCustomSearchTerm() != null && !params.getCustomSearchTerm().trim().isEmpty()) {
            return crawlAndSaveDeviceRecall(
                params.getCustomSearchTerm(), 
                params.getMaxRecords(), 
                params.getBatchSize(), 
                params.getDateFrom(), 
                params.getDateTo()
            );
        }
        
        // 模式3：单字段或多字段组合查询
        return handleCombinedFieldsCrawl(params);
    }
    
    /**
     * 处理关键词列表批量爬取
     */
    private String handleKeywordsBatchCrawl(RecallCrawlerParams params) {
        List<String> keywords = params.getKeywords();
        System.out.println("\n========== 关键词列表批量爬取模式 ==========");
        System.out.println("关键词数量: " + keywords.size());
        System.out.println("关键词列表: " + keywords);
        System.out.println("日期范围: " + params.getDateFrom() + " - " + params.getDateTo());
        System.out.println("每个关键词最大记录数: " + params.getMaxRecords());
        
        int totalSaved = 0;
        
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            System.out.println("\n处理关键词: " + keyword);
            
            try {
                // 1. 在品牌名称字段中搜索
                System.out.println("  [1/3] 在品牌名称中搜索 '" + keyword + "'");
                String brandResult = crawlAndSaveDeviceRecall(
                    "openfda.brand_name:" + keyword,
                    params.getMaxRecords(),
                    params.getBatchSize(),
                    params.getDateFrom(),
                    params.getDateTo()
                );
                int brandCount = extractSavedCount(brandResult);
                System.out.println("  品牌名称搜索结果: " + brandCount + " 条");
                
                // 2. 在召回公司字段中搜索
                System.out.println("  [2/3] 在召回公司中搜索 '" + keyword + "'");
                String firmResult = crawlAndSaveDeviceRecall(
                    "recalling_firm:" + keyword,
                    params.getMaxRecords(),
                    params.getBatchSize(),
                    params.getDateFrom(),
                    params.getDateTo()
                );
                int firmCount = extractSavedCount(firmResult);
                System.out.println("  召回公司搜索结果: " + firmCount + " 条");
                
                // 3. 在产品描述字段中搜索
                System.out.println("  [3/3] 在产品描述中搜索 '" + keyword + "'");
                String descResult = crawlAndSaveDeviceRecall(
                    "product_description:" + keyword,
                    params.getMaxRecords(),
                    params.getBatchSize(),
                    params.getDateFrom(),
                    params.getDateTo()
                );
                int descCount = extractSavedCount(descResult);
                System.out.println("  产品描述搜索结果: " + descCount + " 条");
                
                int keywordTotal = brandCount + firmCount + descCount;
                totalSaved += keywordTotal;
                System.out.println("关键词 '" + keyword + "' 总计: " + keywordTotal + " 条");
                
            } catch (Exception e) {
                System.err.println("处理关键词 '" + keyword + "' 时发生错误: " + e.getMessage());
            }
        }
        
        return String.format("关键词列表批量爬取完成，共处理 %d 个关键词，总计保存: %d 条记录", 
            keywords.size(), totalSaved);
    }
    
    /**
     * 处理单字段或多字段组合爬取
     */
    private String handleCombinedFieldsCrawl(RecallCrawlerParams params) {
        StringBuilder searchQuery = new StringBuilder();
        List<String> conditions = new ArrayList<>();
        
        // 收集所有搜索条件
        if (params.getBrandName() != null && !params.getBrandName().trim().isEmpty()) {
            conditions.add("openfda.brand_name:" + params.getBrandName().trim());
        }
        
        if (params.getRecallingFirm() != null && !params.getRecallingFirm().trim().isEmpty()) {
            conditions.add("recalling_firm:" + params.getRecallingFirm().trim());
        }
        
        if (params.getProductDescription() != null && !params.getProductDescription().trim().isEmpty()) {
            conditions.add("product_description:" + params.getProductDescription().trim());
        }
        
        // 使用AND连接多个条件
        if (!conditions.isEmpty()) {
            searchQuery.append(String.join("+AND+", conditions));
        }
        
        String searchTerm = searchQuery.length() > 0 ? searchQuery.toString() : null;
        
        System.out.println("\n========== 单字段/多字段组合爬取模式 ==========");
        if (params.getBrandName() != null) {
            System.out.println("品牌名称: " + params.getBrandName());
        }
        if (params.getRecallingFirm() != null) {
            System.out.println("召回公司: " + params.getRecallingFirm());
        }
        if (params.getProductDescription() != null) {
            System.out.println("产品描述: " + params.getProductDescription());
        }
        System.out.println("组合查询: " + (searchTerm != null ? searchTerm : "无搜索条件"));
        System.out.println("日期范围: " + params.getDateFrom() + " - " + params.getDateTo());
        
        return crawlAndSaveDeviceRecall(
            searchTerm,
            params.getMaxRecords(),
            params.getBatchSize(),
            params.getDateFrom(),
            params.getDateTo()
        );
    }
    
    /**
     * 通用爬取方法（支持时间范围）
     * @param searchTerm 搜索词（可选）
     * @param maxRecords 最大记录数（-1表示全部）
     * @param batchSize 批次大小
     * @param dateFrom 起始日期（格式：YYYYMMDD，如：20240101）
     * @param dateTo 结束日期（格式：YYYYMMDD，如：20241231，为空则查询到未来）
     * @return 爬取结果信息
     */
    public String crawlAndSaveDeviceRecall(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (maxRecords == -1) {
            System.out.println("开始爬取FDA设备召回数据，搜索词: " + searchTerm + ", 爬取模式: 所有数据, 批次大小: " + batchSize);
        } else {
            System.out.println("开始爬取FDA设备召回数据，搜索词: " + searchTerm + ", 最大记录数: " + maxRecords + ", 批次大小: " + batchSize);
        }

        try {
            String result = crawlAndSaveDeviceRecallInBatches(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
            return result;

        } catch (Exception e) {
            System.err.println("爬取FDA设备召回数据失败: " + e.getMessage());
            e.printStackTrace();

            recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, 0, 0, "FAILED", e.getMessage());

            return "FDA设备召回数据爬取失败: " + e.getMessage();
        }
    }

    /**
     * 基于关键词列表爬取FDA设备召回数据
     * 
     * @param inputKeywords 关键词列表
     * @param maxRecords 每个关键词的最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 起始日期（格式：YYYYMMDD）
     * @param dateTo 结束日期（格式：YYYYMMDD）
     * @return 爬取结果信息
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认搜索");
            return crawlAndSaveDeviceRecall("product_description:medical", maxRecords, batchSize, dateFrom, dateTo);
        }

        System.out.println("开始基于关键词列表爬取FDA设备召回数据...");
        System.out.println("关键词数量: " + inputKeywords.size());
        System.out.println("日期范围: " + dateFrom + " - " + dateTo);
        System.out.println("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));

        int totalSaved = 0;

        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            keyword = keyword.trim();
            System.out.println("\n处理关键词: " + keyword);

            try {
                // 1. 使用关键词作为召回公司进行搜索
                System.out.println("关键词 '" + keyword + "' 作为召回公司搜索");
                String firmSearchTerm = "recalling_firm:" + keyword;
                String firmResult = crawlAndSaveDeviceRecall(firmSearchTerm, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("召回公司搜索结果: " + firmResult);

                // 2. 使用关键词作为brand name进行搜索
                System.out.println("关键词 '" + keyword + "' 作为brand name搜索");
                String brandSearchTerm = "openfda.brand_name:" + keyword;
                String brandResult = crawlAndSaveDeviceRecall(brandSearchTerm, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("brand name搜索结果: " + brandResult);

                // 3. 使用关键词作为产品描述进行搜索
                System.out.println("关键词 '" + keyword + "' 作为产品描述搜索");
                String productSearchTerm = "product_description:" + keyword;
                String productResult = crawlAndSaveDeviceRecall(productSearchTerm, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("产品描述搜索结果: " + productResult);

                // 解析结果中的保存数量
                totalSaved += extractSavedCount(firmResult);
                totalSaved += extractSavedCount(brandResult);
                totalSaved += extractSavedCount(productResult);

            } catch (Exception e) {
                System.err.println("处理关键词 '" + keyword + "' 时发生错误: " + e.getMessage());
            }
        }

        return String.format("基于关键词列表的FDA设备召回数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 从结果字符串中提取保存的记录数
     */
    private int extractSavedCount(String result) {
        if (result == null || result.isEmpty()) {
            return 0;
        }

        try {
            // 查找 "保存: X 条记录" 或 "新增: X 条" 等模式
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:保存|新增|入库)[:：]?\\s*(\\d+)\\s*条");
            java.util.regex.Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略解析错误
        }

        return 0;
    }
    
    /**
     * 基于多字段参数爬取Recall数据（新的统一方法）⭐
     * 支持：brandNames, recallingFirms, productDescriptions, dateFrom/dateTo
     * 
     * @param brandNames 品牌名称列表
     * @param recallingFirms 召回公司列表
     * @param productDescriptions 产品描述列表
     * @param dateFrom 起始日期（yyyyMMdd格式）
     * @param dateTo 结束日期（yyyyMMdd格式）
     * @param maxRecords 最大记录数（-1表示全部）
     * @param batchSize 批次大小
     * @return 爬取结果信息
     */
    public String crawlAndSaveWithMultipleFields(
            List<String> brandNames,
            List<String> recallingFirms,
            List<String> productDescriptions,
            String dateFrom,
            String dateTo,
            int maxRecords,
            int batchSize) {
        
        System.out.println("开始使用多字段参数爬取FDA Recall数据...");
        System.out.println("品牌名称数量: " + (brandNames != null ? brandNames.size() : 0));
        System.out.println("召回公司数量: " + (recallingFirms != null ? recallingFirms.size() : 0));
        System.out.println("产品描述数量: " + (productDescriptions != null ? productDescriptions.size() : 0));
        System.out.println("日期范围: " + dateFrom + " - " + dateTo);
        System.out.println("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));
        
        int totalSaved = 0;
        
        // 1. 按品牌名称搜索
        if (brandNames != null && !brandNames.isEmpty()) {
            for (String brandName : brandNames) {
                if (brandName == null || brandName.trim().isEmpty()) continue;
                
                System.out.println("按品牌名称搜索: " + brandName);
                String searchQuery = "openfda.brand_name:" + brandName.trim();
                
                try {
                    String result = crawlAndSaveDeviceRecall(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("品牌名称搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("品牌名称 '" + brandName + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        // 2. 按召回公司搜索
        if (recallingFirms != null && !recallingFirms.isEmpty()) {
            for (String firm : recallingFirms) {
                if (firm == null || firm.trim().isEmpty()) continue;
                
                System.out.println("按召回公司搜索: " + firm);
                String searchQuery = "recalling_firm:" + firm.trim();
                
                try {
                    String result = crawlAndSaveDeviceRecall(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("召回公司搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("召回公司 '" + firm + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        // 3. 按产品描述搜索
        if (productDescriptions != null && !productDescriptions.isEmpty()) {
            for (String description : productDescriptions) {
                if (description == null || description.trim().isEmpty()) continue;
                
                System.out.println("按产品描述搜索: " + description);
                String searchQuery = "product_description:" + description.trim();
                
                try {
                    String result = crawlAndSaveDeviceRecall(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("产品描述搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("产品描述 '" + description + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        String finalResult = String.format(
            "多字段Recall数据爬取完成，总保存: %d 条记录", totalSaved);
        System.out.println(finalResult);
        
        return finalResult;
    }

    /**
     * 分批爬取并保存设备召回数据
     */
    private String crawlAndSaveDeviceRecallInBatches(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {

        List<MedicalDeviceRecall> allResults = crawlDeviceRecall(searchTerm, maxRecords, batchSize, dateFrom, dateTo);

        if (allResults.isEmpty()) {
            return "未找到匹配的设备召回数据";
        }

        return saveBatchToDatabase(allResults);
    }

    /**
     * 爬取设备召回数据
     */
    private List<MedicalDeviceRecall> crawlDeviceRecall(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {

        System.out.println("开始爬取设备召回数据...");

        List<MedicalDeviceRecall> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords == -1);
        Integer totalAvailable = null;

        while (crawlAll || totalFetched < maxRecords) {
            int currentLimit;
            if (crawlAll) {
                currentLimit = Math.min(batchSize, 1000);
            } else {
                currentLimit = Math.min(batchSize, maxRecords - totalFetched);
                currentLimit = Math.min(currentLimit, 1000);
            }

            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备召回数据（偏移量: %d，数量: %d）%n",
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));
            
            // 构建搜索查询（支持时间范围）
            StringBuilder searchQuery = new StringBuilder();
            
            // 添加搜索词
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                searchQuery.append(searchTerm.trim());
            }
            
            // 添加时间范围参数（使用event_date_initiated或event_date_posted）
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                String effectiveDateTo = (dateTo != null && !dateTo.trim().isEmpty()) ? dateTo.trim() : "20991231";
                String dateRange = String.format("event_date_initiated:[%s+TO+%s]", dateFrom.trim(), effectiveDateTo);
                
                if (searchQuery.length() > 0) {
                    searchQuery.append("+AND+").append(dateRange);
                } else {
                    searchQuery.append(dateRange);
                }
            }
            
            // 设置search参数
            if (searchQuery.length() > 0) {
                params.put("search", searchQuery.toString());
            }

            try {
                FDAResponse response = fetchData("/device/recall.json", params);
                List<MedicalDeviceRecall> results = response != null ? response.getResults() : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备召回数据，爬取结束");
                    break;
                }

                // 记录总数量信息（只在第一次获取时记录）
                if (totalAvailable == null && response != null && response.getMeta() != null && response.getMeta().getResults() != null) {
                    totalAvailable = response.getMeta().getResults().getTotal();
                    System.out.printf("API返回总匹配记录数: %d 条%n", totalAvailable);

                    if (crawlAll && totalAvailable != null) {
                        maxRecords = totalAvailable;
                        crawlAll = false;
                        System.out.printf("开始爬取所有数据，目标数量: %d 条%n", maxRecords);
                    }
                }

                // 转换为MedicalDeviceRecall对象并设置国家
                for (MedicalDeviceRecall recall : results) {
                    recall.setJdCountry("US");
                }

                allResults.addAll(results);
                totalFetched += results.size();
                skip += currentLimit;

                // 显示当前进度
                if (totalAvailable != null) {
                    System.out.printf("当前进度: %d/%d (%.1f%%)%n",
                            totalFetched, totalAvailable, (double) totalFetched / totalAvailable * 100);
                }

                // 检查是否已获取所有匹配记录
                if (totalAvailable != null && totalFetched >= totalAvailable) {
                    System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            } catch (Exception e) {
                System.err.println("爬取过程中发生错误: " + e.getMessage());
                throw e;
            }
        }

        List<MedicalDeviceRecall> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备召回数据爬取完成，共 %d 条数据%n", finalResults.size());
        return finalResults;
    }

    /**
     * 获取数据
     */
    private FDAResponse fetchData(String endpoint, Map<String, String> params) throws IOException, URISyntaxException {
        // 手动构建URL，对search参数特殊处理
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + endpoint);
        urlBuilder.append("?api_key=").append(API_KEY);
        
        // 处理其他参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if ("search".equals(key)) {
                // search参数不进行URL编码，FDA API需要特定格式
                // 直接附加，保持+号和括号
                urlBuilder.append("&search=").append(value);
            } else {
                // 其他参数正常编码
                urlBuilder.append("&").append(key).append("=").append(value);
            }
        }
        
        String requestUrl = urlBuilder.toString();
        System.out.println("请求URL: " + requestUrl);

        HttpGet httpGet = new HttpGet(requestUrl);

        for (int attempt = 1; attempt <= crawlerConfig.getRetry().getMaxAttempts(); attempt++) {
            try (var response = httpClient.executeOpen(null, httpGet, null)) {
                int statusCode = response.getCode();
                String reasonPhrase = response.getReasonPhrase();

                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                        // 检查是否包含错误信息
                        if (json.contains("\"error\"")) {
                            try {
                                com.fasterxml.jackson.databind.JsonNode errorNode = objectMapper.readTree(json);
                                if (errorNode.has("error")) {
                                    com.fasterxml.jackson.databind.JsonNode error = errorNode.get("error");
                                    String errorCode = error.has("code") ? error.get("code").asText() : "UNKNOWN";
                                    String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
                                    String errorDetails = error.has("details") ? error.get("details").asText() : "";

                                    if ("NOT_FOUND".equals(errorCode) && "No matches found!".equals(errorMessage)) {
                                        System.out.println("API返回：未找到匹配记录 - " + errorMessage);
                                        FDAResponse emptyResponse = new FDAResponse();
                                        emptyResponse.setResults(new ArrayList<>());
                                        return emptyResponse;
                                    } else {
                                        System.err.printf("API返回错误: code=%s, message=%s%n", errorCode, errorMessage);
                                        if (!errorDetails.isEmpty()) {
                                            System.err.println("错误详情: " + errorDetails);
                                        }
                                        System.err.println("完整响应: " + json);
                                        throw new IOException("API错误: " + errorCode + " - " + errorMessage + 
                                            (errorDetails.isEmpty() ? "" : " | " + errorDetails));
                                    }
                                }
                            } catch (IOException ioe) {
                                throw ioe; // 重新抛出IOException
                            } catch (Exception parseError) {
                                System.err.println("解析错误响应失败: " + parseError.getMessage());
                                System.err.println("原始响应: " + json);
                                throw new IOException("API返回错误响应，解析失败: " + parseError.getMessage());
                            }
                        }

                        return objectMapper.readValue(json, FDAResponse.class);
                    }
                } else if (statusCode == 500) {
                    // 500错误特殊处理，尝试读取响应体
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String errorBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                        System.err.printf("服务器错误(500)响应内容: %s%n", errorBody);
                        throw new IOException("FDA API服务器错误(500): " + errorBody);
                    }
                    System.err.printf("请求失败，状态码: %d，原因: %s（第%d次重试）%n", statusCode, reasonPhrase, attempt);
                } else {
                    System.err.printf("请求失败，状态码: %d，原因: %s（第%d次重试）%n", statusCode, reasonPhrase, attempt);
                    if (attempt < crawlerConfig.getRetry().getMaxAttempts()) {
                        try {
                            TimeUnit.SECONDS.sleep(crawlerConfig.getRetry().getDelaySeconds());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.printf("请求异常: %s（第%d次重试）%n", e.getMessage(), attempt);
                if (attempt < crawlerConfig.getRetry().getMaxAttempts()) {
                    try {
                        TimeUnit.SECONDS.sleep(crawlerConfig.getRetry().getDelaySeconds());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new IOException("请求失败，已重试 " + crawlerConfig.getRetry().getMaxAttempts() + " 次");
    }

    /**
     * 批量保存到数据库
     */
    @Transactional
    private String saveBatchToDatabase(List<MedicalDeviceRecall> records) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);

        for (int i = 0; i < records.size(); i += crawlerConfig.getBatch().getSaveSize()) {
            int endIndex = Math.min(i + crawlerConfig.getBatch().getSaveSize(), records.size());
            List<MedicalDeviceRecall> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceRecallRecord> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (MedicalDeviceRecall record : batch) {
                try {
                    // 检查是否已存在（使用cfresId）
                    boolean isDuplicate = false;
                    
                    String cfresId = record.getCfresId();
                    
                    // 重试机制：最多重试3次
                    int retryCount = 0;
                    int maxRetries = 3;
                    boolean querySuccess = false;
                    
                    while (retryCount < maxRetries && !querySuccess) {
                        try {
                            if (cfresId != null && !cfresId.trim().isEmpty()) {
                                if (deviceRecallRecordRepository.findByCfresId(cfresId).isPresent()) {
                                    isDuplicate = true;
                                }
                            }
                            querySuccess = true;
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount >= maxRetries) {
                                System.err.println("查询重复记录失败，跳过该记录: " + e.getMessage());
                                isDuplicate = true; // 查询失败时跳过该记录
                            } else {
                                try {
                                    Thread.sleep(1000 * retryCount);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    }

                    if (!isDuplicate) {
                        DeviceRecallRecord entity = convertToEntity(record);
                        newRecords.add(entity);
                    } else {
                        batchDuplicateCount++;
                    }
                } catch (Exception e) {
                    System.err.println("处理记录时发生错误: " + e.getMessage());
                    batchDuplicateCount++;
                }
            }

            // 保存新记录
            if (!newRecords.isEmpty()) {
                int saveRetryCount = 0;
                int maxSaveRetries = 3;
                boolean saveSuccess = false;

                while (saveRetryCount < maxSaveRetries && !saveSuccess) {
                    try {
                        List<DeviceRecallRecord> savedRecords = deviceRecallRecordRepository.saveAll(newRecords);
                        savedCount += savedRecords.size();
                        totalSkipped += batchDuplicateCount;
                        System.out.println("第 " + batchCount + " 批次保存成功，新增: " + newRecords.size() + " 条，重复: " + batchDuplicateCount + " 条");
                        saveSuccess = true;
                    } catch (Exception e) {
                        saveRetryCount++;
                        if (saveRetryCount >= maxSaveRetries) {
                            System.err.println("第 " + batchCount + " 批次保存失败，已重试" + maxSaveRetries + "次: " + e.getMessage());
                        } else {
                            System.err.println("第 " + batchCount + " 批次保存失败，第" + saveRetryCount + "次重试: " + e.getMessage());
                            try {
                                Thread.sleep(2000 * saveRetryCount);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            }

            // 批次检测：检查是否应该停止爬取
            boolean shouldStop = detector.recordBatch(batch.size(), newRecords.size());
            if (shouldStop) {
                System.out.println("⚠️ 检测到连续重复批次，停止保存剩余数据");
                break;
            }
        }

        // 打印最终统计
        detector.printFinalStats("US_recall_api");

        return String.format("保存成功: %d 条新记录, 跳过重复: %d 条", savedCount, totalSkipped);
    }

    /**
     * 将API的召回模型转换为实体
     */
    private DeviceRecallRecord convertToEntity(MedicalDeviceRecall src) {
        if (src == null) return null;

        DeviceRecallRecord entity = new DeviceRecallRecord();

        // 设置基本信息（适配重写后的DeviceRecallRecord实体类）
        entity.setCfresId(truncateString(src.getCfresId(), 100));
        entity.setProductDescription(src.getProductDescription());
        entity.setRecallingFirm(truncateString(src.getRecallingFirm(), 255));
        entity.setRecallStatus(truncateString(src.getRecallStatus(), 100));
        entity.setEventDatePosted(src.getEventDatePosted());
        entity.setDeviceName(truncateString(src.getDeviceName(), 255));
        entity.setProductCode(truncateString(src.getProductCode(), 50));
        entity.setDataSource("FDA");
        entity.setCountryCode("US");
        entity.setJdCountry(src.getJdCountry());
        
        // 设置爬取时间为当前时间
        entity.setCrawlTime(java.time.LocalDateTime.now());

        // 计算风险等级
        RiskLevel calculatedRiskLevel = RiskLevelUtil.calculateRiskLevelByRecallStatus(src.getRecallStatus());
        entity.setRiskLevel(calculatedRiskLevel);

        // 提取关键词
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();

        // 从产品描述提取关键词
        if (src.getProductDescription() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromProductDescription(src.getProductDescription(), predefinedKeywords));
        }

        // 从召回公司提取关键词
        if (src.getRecallingFirm() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(src.getRecallingFirm(), predefinedKeywords));
        }

        // 从设备名称提取关键词
        if (src.getDeviceName() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(src.getDeviceName(), predefinedKeywords));
        }

        // 从召回原因提取关键词
        if (src.getReasonForRecall() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(src.getReasonForRecall(), predefinedKeywords));
        }

        // 去重并转换为JSON存储
        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
        entity.setKeywords(KeywordUtil.keywordsToJson(uniqueKeywords));

        return entity;
    }

    /**
     * 获取预定义关键词列表
     */
    private List<String> getPredefinedKeywords() {
        return Arrays.asList(
            "Skin", "Analyzer", "3D", "AI", "AIMYSKIN", "Facial", "Detector", "Scanner",
            "Care", "Portable", "Spectral", "Spectra", "Skin Analysis", "Skin Scanner",
            "3D skin imaging system", "Facial Imaging", "Skin pigmentation analysis system",
            "skin elasticity analysis", "monitor", "imaging", "medical device", "FDA",
            "recall", "withdrawal", "defect", "safety", "hazard", "medical specialty", "device class"
        );
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        System.out.printf("警告: 字段内容过长，已截断至%d字符: %s%n", maxLength, str.substring(0, Math.min(50, str.length())));
        return str.substring(0, maxLength);
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

}

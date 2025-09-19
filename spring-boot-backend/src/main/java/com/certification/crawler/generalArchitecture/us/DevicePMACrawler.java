package com.certification.crawler.generalArchitecture.us;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.certification.entity.common.CrawlerCheckpoint;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * FDA设备上市前审批爬取器
 * 专门用于爬取设备上市前审批数据
 */
@Component
public class DevicePMACrawler {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CrawlerCheckpointRepository checkpointRepository;
    
    private static final String CRAWLER_TYPE = "DevicePMA";
    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次

    @Autowired
    public DevicePMACrawler(CrawlerCheckpointRepository checkpointRepository) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.checkpointRepository = checkpointRepository;
    }

    /**
     * 设备上市前审批数据模型（原始API直映射，保留）
     */
    public static class DevicePMA {
        @JsonProperty("pma_number")
        private String pmaNumber;
        
        @JsonProperty("applicant")
        private String applicant;
        
        @JsonProperty("generic_name")
        private String genericName;
        
        @JsonProperty("trade_name")
        private String tradeName;
        
        @JsonProperty("product_code")
        private String productCode;
        
        @JsonProperty("decision_date")
        private String decisionDate;
        
        @JsonProperty("decision_code")
        private String decisionCode;
        
        @JsonProperty("openfda")
        private OpenFDA openfda;

        // Getters and Setters
        public String getPmaNumber() { return pmaNumber; }
        public void setPmaNumber(String pmaNumber) { this.pmaNumber = pmaNumber; }
        
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }
        
        public String getGenericName() { return genericName; }
        public void setGenericName(String genericName) { this.genericName = genericName; }
        
        public String getTradeName() { return tradeName; }
        public void setTradeName(String tradeName) { this.tradeName = tradeName; }
        
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        
        public String getDecisionDate() { return decisionDate; }
        public void setDecisionDate(String decisionDate) { this.decisionDate = decisionDate; }
        
        public String getDecisionCode() { return decisionCode; }
        public void setDecisionCode(String decisionCode) { this.decisionCode = decisionCode; }
        
        public OpenFDA getOpenfda() { return openfda; }
        public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
    }

    /**
     * 新数据模型：人工耳蜗 PMA 扁平核心信息
     */
    public static class CochlearImplantPma {
        // PMA 主编号
        private String pmaNumber;
        // 补充申请编号
        private String supplementNumber;
        // 申请人
        private String applicant;
        // 申请人完整地址（组合）
        private String fullAddress;
        // 设备通用名称
        private String genericName;
        // 设备商品名称
        private String tradeName;
        // 产品代码
        private String productCode;
        // 咨询委员会描述
        private String advisoryCommitteeDescription;
        // 补充申请类型
        private String supplementType;
        // 补充申请原因
        private String supplementReason;
        // 是否加急
        private String expeditedReviewFlag;
        // FDA 接收日期
        private LocalDate dateReceived;
        // 决策日期
        private LocalDate decisionDate;
        // 决策代码
        private String decisionCode;
        // 批准声明
        private String aoStatement;
        // 设备分类
        private String deviceClass;
        // 数据来源
        private String dataSource;
        // 数据适用国家
        private String jdCountry;

        // getters/setters
        public String getPmaNumber() { return pmaNumber; }
        public void setPmaNumber(String pmaNumber) { this.pmaNumber = pmaNumber; }
        public String getSupplementNumber() { return supplementNumber; }
        public void setSupplementNumber(String supplementNumber) { this.supplementNumber = supplementNumber; }
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }
        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
        public String getGenericName() { return genericName; }
        public void setGenericName(String genericName) { this.genericName = genericName; }
        public String getTradeName() { return tradeName; }
        public void setTradeName(String tradeName) { this.tradeName = tradeName; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public String getAdvisoryCommitteeDescription() { return advisoryCommitteeDescription; }
        public void setAdvisoryCommitteeDescription(String advisoryCommitteeDescription) { this.advisoryCommitteeDescription = advisoryCommitteeDescription; }
        public String getSupplementType() { return supplementType; }
        public void setSupplementType(String supplementType) { this.supplementType = supplementType; }
        public String getSupplementReason() { return supplementReason; }
        public void setSupplementReason(String supplementReason) { this.supplementReason = supplementReason; }
        public String getExpeditedReviewFlag() { return expeditedReviewFlag; }
        public void setExpeditedReviewFlag(String expeditedReviewFlag) { this.expeditedReviewFlag = expeditedReviewFlag; }
        public LocalDate getDateReceived() { return dateReceived; }
        public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }
        public LocalDate getDecisionDate() { return decisionDate; }
        public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }
        public String getDecisionCode() { return decisionCode; }
        public void setDecisionCode(String decisionCode) { this.decisionCode = decisionCode; }
        public String getAoStatement() { return aoStatement; }
        public void setAoStatement(String aoStatement) { this.aoStatement = aoStatement; }
        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public String getJdCountry() { return jdCountry; }
        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }
    }

    /**
     * OpenFDA数据模型
     */
    public static class OpenFDA {
        @JsonProperty("device_name")
        private String deviceName;
        
        @JsonProperty("medical_specialty_description")
        private String medicalSpecialtyDescription;
        
        @JsonProperty("regulation_number")
        private String regulationNumber;
        
        @JsonProperty("device_class")
        private String deviceClass;

        // Getters and Setters
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        
        public String getMedicalSpecialtyDescription() { return medicalSpecialtyDescription; }
        public void setMedicalSpecialtyDescription(String medicalSpecialtyDescription) { this.medicalSpecialtyDescription = medicalSpecialtyDescription; }
        
        public String getRegulationNumber() { return regulationNumber; }
        public void setRegulationNumber(String regulationNumber) { this.regulationNumber = regulationNumber; }
        
        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
    }

    /**
     * 发送HTTP请求并获取数据，包含重试机制
     */
    private Map<String, Object> fetchData(String endpoint, Map<String, String> params) 
            throws IOException, URISyntaxException, ParseException {
        URIBuilder uriBuilder = new URIBuilder(BASE_URL + endpoint);

        // 首先添加API密钥参数
        if (API_KEY != null && !API_KEY.isEmpty()) {
            uriBuilder.addParameter("api_key", API_KEY);
        }
        
        // 然后添加其他请求参数
        params.forEach(uriBuilder::addParameter);

        String requestUrl = uriBuilder.build().toString();
        System.out.println("请求URL: " + requestUrl);
        
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        
        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                String reasonPhrase = response.getReasonPhrase();
                
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String json = EntityUtils.toString(entity);
                        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                    }
                } else {
                    System.err.printf("请求失败，状态码: %d，原因: %s（第%d次重试）%n", statusCode, reasonPhrase, attempt);
                    if (attempt < RETRY_COUNT) {
                        try {
                            TimeUnit.SECONDS.sleep(RETRY_DELAY);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.printf("请求异常: %s（第%d次重试）%n", e.getMessage(), attempt);
                if (attempt < RETRY_COUNT) {
                    try {
                        TimeUnit.SECONDS.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new IOException("请求失败，已重试 " + RETRY_COUNT + " 次");
    }

    /**
     * 爬取设备上市前审批数据（原始结构）
     */
    public List<DevicePMA> crawlDevicePMA(String searchTerm, int maxRecords, int batchSize) 
            throws IOException, URISyntaxException, ParseException {
        return crawlDevicePMA(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取设备上市前审批数据（支持时间范围和断点续传）
     */
    public List<DevicePMA> crawlDevicePMA(String searchTerm, int maxRecords, int batchSize,
                                         String dateFrom, String dateTo) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备上市前审批数据...");
        
        // 检查是否有断点可以继续
        CrawlerCheckpoint checkpoint = getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        
        if (checkpoint.isCompleted()) {
            System.out.println("爬取任务已完成，无需重复执行");
            return new ArrayList<>();
        }
        
        List<DevicePMA> allResults = new ArrayList<>();
        int skip = checkpoint.getCurrentSkip();
        int totalFetched = checkpoint.getTotalFetched();

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
            
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备上市前审批数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));
            
            // 添加时间范围参数
            if (dateFrom != null && dateTo != null) {
                String dateRange = String.format("date_received:[%s TO %s]", dateFrom, dateTo);
                String currentSearch = searchTerm != null && !searchTerm.isEmpty() ? 
                    searchTerm + " AND " + dateRange : dateRange;
                params.put("search", currentSearch);
            }

            try {
                Map<String, Object> data = fetchData("/device/pma.json", params);
                List<Map<String, Object>> results = data != null ? 
                        (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备上市前审批数据，爬取结束");
                    checkpoint.markCompleted();
                    checkpointRepository.save(checkpoint);
                    break;
                }

                // 转换为DevicePMA对象
                for (Map<String, Object> result : results) {
                    DevicePMA pma = objectMapper.convertValue(result, DevicePMA.class);
                    allResults.add(pma);
                }

                totalFetched += results.size();
                skip += currentLimit;
                
                // 更新断点进度
                checkpoint.updateProgress(skip, totalFetched);
                checkpointRepository.save(checkpoint);

                // 检查是否已获取所有匹配记录
                if (data != null && data.containsKey("meta")) {
                    Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                    if (meta.containsKey("results")) {
                        Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                        Integer totalAvailable = (Integer) resultsMeta.get("total");
                        if (totalAvailable != null && totalFetched >= totalAvailable) {
                            System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
                            checkpoint.markCompleted();
                            checkpointRepository.save(checkpoint);
                            break;
                        }
                    }
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
            } catch (Exception e) {
                System.err.println("爬取过程中发生错误: " + e.getMessage());
                checkpoint.markFailed(e.getMessage());
                checkpointRepository.save(checkpoint);
                throw e;
            }
        }

        List<DevicePMA> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备上市前审批数据爬取完成，共 %d 条数据%n", finalResults.size());
        return finalResults;
    }
    
    /**
     * 获取或创建断点
     */
    private CrawlerCheckpoint getOrCreateCheckpoint(String searchTerm, int maxRecords, int batchSize, 
                                                   String dateFrom, String dateTo) {
        return checkpointRepository.findByCrawlerTypeAndSearchConditions(
                CRAWLER_TYPE, searchTerm, dateFrom, dateTo)
                .orElseGet(() -> {
                    CrawlerCheckpoint newCheckpoint = new CrawlerCheckpoint();
                    newCheckpoint.setCrawlerType(CRAWLER_TYPE);
                    newCheckpoint.setSearchTerm(searchTerm);
                    newCheckpoint.setDateFrom(dateFrom);
                    newCheckpoint.setDateTo(dateTo);
                    newCheckpoint.setTargetTotal(maxRecords);
                    newCheckpoint.setBatchSize(batchSize);
                    newCheckpoint.setStatus(CrawlerCheckpoint.CrawlerStatus.RUNNING);
                    return checkpointRepository.save(newCheckpoint);
                });
    }

    /**
     * 以新模型返回人工耳蜗 PMA 数据
     */
    public List<CochlearImplantPma> crawlCochlearImplantPma(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        List<CochlearImplantPma> out = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;
        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
            if (currentLimit <= 0) break;

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData("/device/pma.json", params);
            List<Map<String, Object>> results = data != null ? (List<Map<String, Object>>) data.get("results") : new ArrayList<>();
            if (results.isEmpty()) break;

            for (Map<String, Object> r : results) {
                out.add(mapToCochlearImplantPma(r));
            }
            totalFetched += results.size();
            skip += currentLimit;
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        break;
                    }
                }
            }
        }
        return out.subList(0, Math.min(out.size(), maxRecords));
    }

    private CochlearImplantPma mapToCochlearImplantPma(Map<String, Object> r) {
        CochlearImplantPma c = new CochlearImplantPma();
        c.setPmaNumber(str(r.get("pma_number")));
        c.setSupplementNumber(str(r.get("supplement_number")));
        c.setApplicant(str(r.get("applicant")));
        c.setGenericName(str(r.get("generic_name")));
        c.setTradeName(str(r.get("trade_name")));
        c.setProductCode(str(r.get("product_code")));
        c.setAdvisoryCommitteeDescription(str(r.get("advisory_committee_description")));
        c.setSupplementType(str(r.get("supplement_type")));
        c.setSupplementReason(str(r.get("supplement_reason")));
        c.setExpeditedReviewFlag(str(r.get("expedited_review_flag")));
        c.setDateReceived(parseDate(str(r.get("date_received"))));
        c.setDecisionDate(parseDate(str(r.get("decision_date"))));
        c.setDecisionCode(str(r.get("decision_code")));
        c.setAoStatement(str(r.get("ao_statement")));
        // fullAddress 组合：尝试多种常见字段名
        String addr1 = str(r.getOrDefault("address_1", r.getOrDefault("street_1", null)));
        String addr2 = str(r.getOrDefault("address_2", r.getOrDefault("street_2", null)));
        String city = str(r.get("city"));
        String state = str(r.getOrDefault("state", r.get("state_code")));
        String zip = str(r.getOrDefault("zip_code", r.get("postal_code")));
        c.setFullAddress(joinNonEmpty(", ", addr1, addr2, city, state, zip));
        // device_class 位于 openfda
        @SuppressWarnings("unchecked") Map<String, Object> openfda = (Map<String, Object>) r.get("openfda");
        if (openfda != null) {
            c.setDeviceClass(str(openfda.get("device_class")));
        }
        // 数据来源
        c.setDataSource("FDA");
        // 数据适用国家
        c.setJdCountry("US");
        return c;
    }

    private String str(Object o) { return o == null ? null : String.valueOf(o); }
    private String joinNonEmpty(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isEmpty()) {
                if (sb.length() > 0) sb.append(sep);
                sb.append(p);
            }
        }
        return sb.toString();
    }
    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] patterns = {"yyyyMMdd", "yyyy-MM-dd", "MM/dd/yyyy"};
        for (String p : patterns) {
            try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(p)); }
            catch (DateTimeParseException ignore) {}
        }
        return null;
    }

    /**
     * 根据设备名称爬取设备上市前审批数据
     */
    public List<DevicePMA> crawlDevicePMAByDeviceName(String deviceName, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDevicePMAByDeviceName(deviceName, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据设备名称爬取设备上市前审批数据（支持时间范围）
     */
    public List<DevicePMA> crawlDevicePMAByDeviceName(String deviceName, int maxRecords, int batchSize,
                                                     String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (deviceName == null || deviceName.trim().isEmpty())
                ? ""
                : "generic_name:\"" + deviceName.trim() + "\"";
        System.out.println("按设备名称搜索: " + term);
        return crawlDevicePMA(term, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 根据专业描述关键词爬取设备上市前审批数据
     */
    public List<DevicePMA> crawlDevicePMAByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDevicePMAByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据专业描述关键词爬取设备上市前审批数据（支持时间范围）
     */
    public List<DevicePMA> crawlDevicePMAByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
                                                           String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (specialtyKeywords == null || specialtyKeywords.trim().isEmpty())
                ? ""
                : "openfda.medical_specialty_description:\"" + specialtyKeywords.trim() + "\"";
        System.out.println("按专业描述关键词搜索: " + term);
        return crawlDevicePMA(term, maxRecords, batchSize, dateFrom, dateTo);
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

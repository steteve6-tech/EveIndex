//package com.certification.crawler.generalArchitecture.us;
//
//import com.certification.repository.common.Device510KRepository;
//import com.certification.repository.common.CrawlerCheckpointRepository;
//import com.certification.entity.common.CrawlerCheckpoint;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.HttpEntity;
//import org.apache.hc.core5.http.ParseException;
//import org.apache.hc.core5.http.io.entity.EntityUtils;
//import org.apache.hc.core5.net.URIBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import com.certification.dto.MedicalDeviceApproval;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import com.certification.util.RiskLevelUtil;
//import com.certification.util.KeywordUtil;
//import com.certification.entity.common.CrawlerData.RiskLevel;
//
///**
// * FDA设备510k审批爬取器
// * 专门用于爬取设备510k审批数据
// */
//@Component
//public class Device510KCrawler {
//
//    private static final String BASE_URL = "https://api.fda.gov";
//    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
//    private static final int RETRY_COUNT = 3;
//    private static final int RETRY_DELAY = 5;
//
//    private final CloseableHttpClient httpClient;
//    private final ObjectMapper objectMapper;
//    private final Device510KRepository device510KRepository;
//    private final CrawlerCheckpointRepository checkpointRepository;
//
//    private static final String CRAWLER_TYPE = "Device510K";
//    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次
//
//    @Autowired
//    public Device510KCrawler(Device510KRepository device510KRepository,
//                           CrawlerCheckpointRepository checkpointRepository) {
//        this.httpClient = HttpClients.createDefault();
//        this.objectMapper = new ObjectMapper();
//        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        this.device510KRepository = device510KRepository;
//        this.checkpointRepository = checkpointRepository;
//    }
//
//    /**
//     * 设备510k审批数据模型
//     */
//    public static class Device510K {
//        @JsonProperty("address_1")
//        private String address1;
//
//        @JsonProperty("address_2")
//        private String address2;
//
//        @JsonProperty("advisory_committee")
//        private String advisoryCommittee;
//
//        @JsonProperty("advisory_committee_description")
//        private String advisoryCommitteeDescription;
//
//        @JsonProperty("applicant")
//        private String applicant;
//
//        @JsonProperty("city")
//        private String city;
//
//        @JsonProperty("clearance_type")
//        private String clearanceType;
//
//        @JsonProperty("contact")
//        private String contact;
//
//        @JsonProperty("country_code")
//        private String countryCode;
//
//        @JsonProperty("date_received")
//        private String dateReceived;
//
//        @JsonProperty("decision_code")
//        private String decisionCode;
//
//        @JsonProperty("decision_date")
//        private String decisionDate;
//
//        @JsonProperty("decision_description")
//        private String decisionDescription;
//
//        @JsonProperty("device_name")
//        private String deviceName;
//
//        @JsonProperty("expedited_review_flag")
//        private String expeditedReviewFlag;
//
//        @JsonProperty("k_number")
//        private String kNumber;
//
//        @JsonProperty("openfda")
//        private OpenFDA openfda;
//
//        @JsonProperty("postal_code")
//        private String postalCode;
//
//        @JsonProperty("product_code")
//        private String productCode;
//
//        @JsonProperty("review_advisory_committee")
//        private String reviewAdvisoryCommittee;
//
//        @JsonProperty("state")
//        private String state;
//
//        @JsonProperty("statement_or_summary")
//        private String statementOrSummary;
//
//        @JsonProperty("third_party_flag")
//        private String thirdPartyFlag;
//
//        @JsonProperty("zip_code")
//        private String zipCode;
//
//        @JsonProperty("jd_country")
//        private String jdCountry;
//
//        @JsonProperty("meta")
//        private Map<String, Object> meta;
//
//        // Getters and Setters
//        public String getAddress1() { return address1; }
//        public void setAddress1(String address1) { this.address1 = address1; }
//
//        public String getAddress2() { return address2; }
//        public void setAddress2(String address2) { this.address2 = address2; }
//
//        public String getAdvisoryCommittee() { return advisoryCommittee; }
//        public void setAdvisoryCommittee(String advisoryCommittee) { this.advisoryCommittee = advisoryCommittee; }
//
//        public String getAdvisoryCommitteeDescription() { return advisoryCommitteeDescription; }
//        public void setAdvisoryCommitteeDescription(String advisoryCommitteeDescription) { this.advisoryCommitteeDescription = advisoryCommitteeDescription; }
//
//        public String getApplicant() { return applicant; }
//        public void setApplicant(String applicant) { this.applicant = applicant; }
//
//        public String getCity() { return city; }
//        public void setCity(String city) { this.city = city; }
//
//        public String getClearanceType() { return clearanceType; }
//        public void setClearanceType(String clearanceType) { this.clearanceType = clearanceType; }
//
//        public String getContact() { return contact; }
//        public void setContact(String contact) { this.contact = contact; }
//
//        public String getCountryCode() { return countryCode; }
//        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
//
//        public String getDateReceived() { return dateReceived; }
//        public void setDateReceived(String dateReceived) { this.dateReceived = dateReceived; }
//
//        public String getDecisionCode() { return decisionCode; }
//        public void setDecisionCode(String decisionCode) { this.decisionCode = decisionCode; }
//
//        public String getDecisionDate() { return decisionDate; }
//        public void setDecisionDate(String decisionDate) { this.decisionDate = decisionDate; }
//
//        public String getDecisionDescription() { return decisionDescription; }
//        public void setDecisionDescription(String decisionDescription) { this.decisionDescription = decisionDescription; }
//
//        public String getDeviceName() { return deviceName; }
//        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
//
//        public String getExpeditedReviewFlag() { return expeditedReviewFlag; }
//        public void setExpeditedReviewFlag(String expeditedReviewFlag) { this.expeditedReviewFlag = expeditedReviewFlag; }
//
//        public String getKNumber() { return kNumber; }
//        public void setKNumber(String kNumber) { this.kNumber = kNumber; }
//
//        public OpenFDA getOpenfda() { return openfda; }
//        public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
//
//        public String getPostalCode() { return postalCode; }
//        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
//
//        public String getProductCode() { return productCode; }
//        public void setProductCode(String productCode) { this.productCode = productCode; }
//
//        public String getReviewAdvisoryCommittee() { return reviewAdvisoryCommittee; }
//        public void setReviewAdvisoryCommittee(String reviewAdvisoryCommittee) { this.reviewAdvisoryCommittee = reviewAdvisoryCommittee; }
//
//        public String getState() { return state; }
//        public void setState(String state) { this.state = state; }
//
//        public String getStatementOrSummary() { return statementOrSummary; }
//        public void setStatementOrSummary(String statementOrSummary) { this.statementOrSummary = statementOrSummary; }
//
//        public String getThirdPartyFlag() { return thirdPartyFlag; }
//        public void setThirdPartyFlag(String thirdPartyFlag) { this.thirdPartyFlag = thirdPartyFlag; }
//
//        public String getZipCode() { return zipCode; }
//        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
//
//        public String getJdCountry() { return jdCountry; }
//        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }
//
//        public Map<String, Object> getMeta() { return meta; }
//        public void setMeta(Map<String, Object> meta) { this.meta = meta; }
//    }
//
//    /**
//     * OpenFDA数据模型
//     */
//    public static class OpenFDA {
//        @JsonProperty("device_name")
//        private String deviceName;
//
//        @JsonProperty("medical_specialty_description")
//        private String medicalSpecialtyDescription;
//
//        @JsonProperty("regulation_number")
//        private String regulationNumber;
//
//        @JsonProperty("device_class")
//        private String deviceClass;
//
//        // Getters and Setters
//        public String getDeviceName() { return deviceName; }
//        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
//
//        public String getMedicalSpecialtyDescription() { return medicalSpecialtyDescription; }
//        public void setMedicalSpecialtyDescription(String medicalSpecialtyDescription) { this.medicalSpecialtyDescription = medicalSpecialtyDescription; }
//
//        public String getRegulationNumber() { return regulationNumber; }
//        public void setRegulationNumber(String regulationNumber) { this.regulationNumber = regulationNumber; }
//
//        public String getDeviceClass() { return deviceClass; }
//        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
//    }
//
//    /**
//     * 发送HTTP请求并获取数据，包含重试机制
//     */
//    private Map<String, Object> fetchData(String endpoint, Map<String, String> params)
//            throws IOException, URISyntaxException, ParseException {
//        URIBuilder uriBuilder = new URIBuilder(BASE_URL + endpoint);
//
//        // 首先添加API密钥参数
//        if (API_KEY != null && !API_KEY.isEmpty()) {
//            uriBuilder.addParameter("api_key", API_KEY);
//        }
//
//        // 然后添加其他请求参数
//        params.forEach(uriBuilder::addParameter);
//
//        String requestUrl = uriBuilder.build().toString();
//        System.out.println("请求URL: " + requestUrl);
//
//        HttpGet httpGet = new HttpGet(uriBuilder.build());
//
//        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
//            try (var response = httpClient.executeOpen(null, httpGet, null)) {
//                int statusCode = response.getCode();
//                String reasonPhrase = response.getReasonPhrase();
//
//                if (statusCode == 200) {
//                    HttpEntity entity = response.getEntity();
//                    if (entity != null) {
//                        String json = EntityUtils.toString(entity);
//                        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
//                    }
//                } else {
//                    System.err.printf("请求失败，状态码: %d，原因: %s（第%d次重试）%n", statusCode, reasonPhrase, attempt);
//                    if (attempt < RETRY_COUNT) {
//                        try {
//                            TimeUnit.SECONDS.sleep(RETRY_DELAY);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                            break;
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                System.err.printf("请求异常: %s（第%d次重试）%n", e.getMessage(), attempt);
//                if (attempt < RETRY_COUNT) {
//                    try {
//                        TimeUnit.SECONDS.sleep(RETRY_DELAY);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }
//        }
//
//        throw new IOException("请求失败，已重试 " + RETRY_COUNT + " 次");
//    }
//
//    /**
//     * 将API的510k模型转换为实体对象
//     */
//    private com.certification.entity.common.Device510K toEntity(Device510K src) {
//        if (src == null) return null;
//        com.certification.entity.common.Device510K e = new com.certification.entity.common.Device510K();
//        // 新增字段映射
//        e.setDeviceGeneralName(src.getOpenfda() == null ? null : src.getOpenfda().getDeviceName());
//        e.setDeviceClass(src.getOpenfda() == null ? null : src.getOpenfda().getDeviceClass());
//        e.setDecisionResult(src.getDecisionDescription());
//        e.setRegulationNumber(src.getOpenfda() == null ? null : src.getOpenfda().getRegulationNumber());
//        e.setContactPerson(src.getContact());
//        e.setAddress(buildAddress(src));
//
//        // 限制各字段长度，避免数据库字段溢出
//        e.setAddress1(truncateString(src.getAddress1(), 255));
//        e.setAddress2(truncateString(src.getAddress2(), 255));
//        e.setAdvisoryCommittee(truncateString(src.getAdvisoryCommittee(), 255));
//        e.setAdvisoryCommitteeDescription(truncateString(src.getAdvisoryCommitteeDescription(), 65535)); // TEXT类型
//        e.setApplicant(truncateString(src.getApplicant(), 255));
//        e.setCity(truncateString(src.getCity(), 100));
//        e.setClearanceType(truncateString(src.getClearanceType(), 50));
//        e.setContact(truncateString(src.getContact(), 255));
//        e.setCountryCode(src.getCountryCode());
//        // 类型变更为 LocalDate
//        e.setDateReceived(parseDate(src.getDateReceived()));
//        e.setDecisionCode(src.getDecisionCode());
//        e.setDecisionDate(parseDate(src.getDecisionDate()));
//        e.setDecisionDescription(src.getDecisionDescription());
//        // 限制 device_name 字段长度，避免数据库字段溢出
//        String deviceName = src.getDeviceName();
//        if (deviceName != null && deviceName.length() > 255) {
//            deviceName = deviceName.substring(0, 255);
//            System.out.println("警告: device_name 字段内容过长，已截断至255字符");
//        }
//        e.setDeviceName(deviceName);
//        e.setExpeditedReviewFlag(truncateString(src.getExpeditedReviewFlag(), 10));
//        e.setKNumber(truncateString(src.getKNumber(), 32));
//        try {
//            e.setOpenfda(src.getOpenfda() == null ? null : objectMapper.writeValueAsString(src.getOpenfda()));
//        } catch (Exception ignore) {
//            e.setOpenfda(null);
//        }
//        e.setPostalCode(truncateString(src.getPostalCode(), 20));
//        e.setProductCode(truncateString(src.getProductCode(), 20));
//        e.setReviewAdvisoryCommittee(truncateString(src.getReviewAdvisoryCommittee(), 255));
//        e.setState(truncateString(src.getState(), 50));
//        e.setStatementOrSummary(truncateString(src.getStatementOrSummary(), 65535)); // TEXT类型
//        e.setThirdPartyFlag(truncateString(src.getThirdPartyFlag(), 10));
//        e.setZipCode(truncateString(src.getZipCode(), 20));
//        try {
//            e.setMeta(src.getMeta() == null ? null : objectMapper.writeValueAsString(src.getMeta()));
//        } catch (Exception ignore) {
//            e.setMeta(null);
//        }
//        e.setDataSource("FDA");
//        // 设置用于国家判定字段，固定为US
//        e.setJdCountry("US");
//
//        // 新增：计算风险等级
//        RiskLevel calculatedRiskLevel = RiskLevelUtil.calculateRiskLevelByDeviceClass(src.getOpenfda() == null ? null : src.getOpenfda().getDeviceClass());
//        e.setRiskLevel(calculatedRiskLevel);
//
//        // 新增：提取关键词
//        List<String> predefinedKeywords = getPredefinedKeywords();
//        List<String> extractedKeywords = new ArrayList<>();
//
//        // 从设备名称提取关键词
//        if (src.getDeviceName() != null) {
//            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(src.getDeviceName(), predefinedKeywords));
//        }
//
//        // 从申请人提取关键词
//        if (src.getApplicant() != null) {
//            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(src.getApplicant(), predefinedKeywords));
//        }
//
//        // 从专业描述提取关键词
//        if (src.getOpenfda() != null && src.getOpenfda().getMedicalSpecialtyDescription() != null) {
//            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromProductDescription(
//                src.getOpenfda().getMedicalSpecialtyDescription(), predefinedKeywords));
//        }
//
//        // 去重并转换为JSON存储
//        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
//        e.setKeywords(KeywordUtil.keywordsToJson(uniqueKeywords));
//
//        return e;
//    }
//
//    /**
//     * 获取预定义关键词列表
//     */
//    private List<String> getPredefinedKeywords() {
//        return Arrays.asList(
//            "Skin", "Analyzer", "3D", "AI", "AIMYSKIN", "Facial", "Detector", "Scanner",
//            "Care", "Portable", "Spectral", "Spectra", "Skin Analysis", "Skin Scanner",
//            "3D skin imaging system", "Facial Imaging", "Skin pigmentation analysis system",
//            "skin elasticity analysis", "monitor", "imaging", "medical device", "FDA",
//            "510k", "clearance", "approval", "medical specialty", "device class"
//        );
//    }
//
//    /**
//     * 将 510k API 模型转换为新的审批DTO
//     */
//    private MedicalDeviceApproval toApproval(Device510K src) {
//        if (src == null) return null;
//        MedicalDeviceApproval dto = new MedicalDeviceApproval();
//        dto.setKNumber(src.getKNumber());
//        dto.setApplicant(src.getApplicant());
//        dto.setDeviceName(src.getDeviceName());
//        // 统一设备名称（通用名）从 openfda.device_name 获取
//        dto.setDeviceGeneralName(src.getOpenfda() == null ? null : src.getOpenfda().getDeviceName());
//        dto.setDeviceClass(src.getOpenfda() == null ? null : src.getOpenfda().getDeviceClass());
//        dto.setDecisionResult(src.getDecisionDescription());
//        dto.setDecisionDate(parseDate(src.getDecisionDate()));
//        dto.setDateReceived(parseDate(src.getDateReceived()));
//        // 优先使用 review_advisory_committee，否则使用 advisory_committee
//        String committee = (src.getReviewAdvisoryCommittee() != null && !src.getReviewAdvisoryCommittee().isEmpty())
//                ? src.getReviewAdvisoryCommittee() : src.getAdvisoryCommittee();
//        dto.setAdvisoryCommittee(committee);
//        dto.setProductCode(src.getProductCode());
//        dto.setRegulationNumber(src.getOpenfda() == null ? null : src.getOpenfda().getRegulationNumber());
//        dto.setContactPerson(src.getContact());
//        dto.setAddress(buildAddress(src));
//        dto.setClearanceType(src.getClearanceType());
//        dto.setDataSource("FDA");
//        return dto;
//    }
//
//    private String buildAddress(Device510K src) {
//        List<String> parts = new ArrayList<>();
//        if (src.getAddress1() != null && !src.getAddress1().isEmpty()) parts.add(src.getAddress1());
//        if (src.getAddress2() != null && !src.getAddress2().isEmpty()) parts.add(src.getAddress2());
//        if (src.getCity() != null && !src.getCity().isEmpty()) parts.add(src.getCity());
//        if (src.getState() != null && !src.getState().isEmpty()) parts.add(src.getState());
//        if (src.getPostalCode() != null && !src.getPostalCode().isEmpty()) parts.add(src.getPostalCode());
//        if (src.getCountryCode() != null && !src.getCountryCode().isEmpty()) parts.add(src.getCountryCode());
//        return String.join(", ", parts);
//    }
//
//    private LocalDate parseDate(String s) {
//        if (s == null || s.isEmpty()) return null;
//        // ��试常见格式
//        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy"};
//        for (String p : patterns) {
//            try {
//                return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
//            } catch (DateTimeParseException ignore) {}
//        }
//        return null;
//    }
//
//    /**
//     * 爬取设备510k审批数据（支持时间范围和断点续传）
//     */
//    public List<Device510K> crawlDevice510K(String searchTerm, int maxRecords, int batchSize,
//                                           String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        System.out.println("开始爬取设备510k审批数据...");
//
//        // 检查是否有断点可以继续
//        CrawlerCheckpoint checkpoint = getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
//
//        if (checkpoint.isCompleted()) {
//            System.out.println("爬取任务已完成，无需重复执行");
//            return new ArrayList<>();
//        }
//
//        List<Device510K> allResults = new ArrayList<>();
//        int skip = checkpoint.getCurrentSkip();
//        int totalFetched = checkpoint.getTotalFetched();
//
//        while (totalFetched < maxRecords) {
//            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
//
//            if (currentLimit <= 0) {
//                break;
//            }
//
//            System.out.printf("获取第 %d 页设备510k审批数据（偏移量: %d，数量: %d）%n",
//                    skip / batchSize + 1, skip, currentLimit);
//
//            Map<String, String> params = new HashMap<>();
//            params.put("search", searchTerm);
//            params.put("limit", String.valueOf(currentLimit));
//            params.put("skip", String.valueOf(skip));
//
//            // 添加时间范围参数
//            if (dateFrom != null && dateTo != null) {
//                String dateRange = String.format("date_received:[%s TO %s]", dateFrom, dateTo);
//                String currentSearch = searchTerm != null && !searchTerm.isEmpty() ?
//                    searchTerm + " AND " + dateRange : dateRange;
//                params.put("search", currentSearch);
//            }
//
//            try {
//                Map<String, Object> data = fetchData("/device/510k.json", params);
//                List<Map<String, Object>> results = data != null ?
//                        (List<Map<String, Object>>) data.get("results") : new ArrayList<>();
//
//                if (results.isEmpty()) {
//                    System.out.println("没有更多设备510k审批数据，爬取结束");
//                    checkpoint.markCompleted();
//                    checkpointRepository.save(checkpoint);
//                    break;
//                }
//
//                // 转换为Device510K对象
//                for (Map<String, Object> result : results) {
//                    Device510K k510 = objectMapper.convertValue(result, Device510K.class);
//                    allResults.add(k510);
//                }
//
//                totalFetched += results.size();
//                skip += currentLimit;
//
//                // 更新断点进度
//                checkpoint.updateProgress(skip, totalFetched);
//                checkpointRepository.save(checkpoint);
//
//                // 检查是否已获取所有匹配记录
//                if (data != null && data.containsKey("meta")) {
//                    Map<String, Object> meta = (Map<String, Object>) data.get("meta");
//                    if (meta.containsKey("results")) {
//                        Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
//                        Integer totalAvailable = (Integer) resultsMeta.get("total");
//                        if (totalAvailable != null && totalFetched >= totalAvailable) {
//                            System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
//                            checkpoint.markCompleted();
//                            checkpointRepository.save(checkpoint);
//                            break;
//                        }
//                    }
//                }
//
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//
//            } catch (Exception e) {
//                System.err.println("爬取过程中发生错误: " + e.getMessage());
//                checkpoint.markFailed(e.getMessage());
//                checkpointRepository.save(checkpoint);
//                throw e;
//            }
//        }
//
//        List<Device510K> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
//        System.out.printf("设备510k审批数据爬取完成，共 %d 条数据%n", finalResults.size());
//        return finalResults;
//    }
//
//    /**
//     * 获取或创建断点
//     */
//    private CrawlerCheckpoint getOrCreateCheckpoint(String searchTerm, int maxRecords, int batchSize,
//                                                   String dateFrom, String dateTo) {
//        return checkpointRepository.findByCrawlerTypeAndSearchConditions(
//                CRAWLER_TYPE, searchTerm, dateFrom, dateTo)
//                .orElseGet(() -> {
//                    CrawlerCheckpoint newCheckpoint = new CrawlerCheckpoint();
//                    newCheckpoint.setCrawlerType(CRAWLER_TYPE);
//                    newCheckpoint.setSearchTerm(searchTerm);
//                    newCheckpoint.setDateFrom(dateFrom);
//                    newCheckpoint.setDateTo(dateTo);
//                    newCheckpoint.setTargetTotal(maxRecords);
//                    newCheckpoint.setBatchSize(batchSize);
//                    newCheckpoint.setStatus(CrawlerCheckpoint.CrawlerStatus.RUNNING);
//                    return checkpointRepository.save(newCheckpoint);
//                });
//    }
//
//    /**
//     * 以新的审批DTO形式返回 510k 数据
//     */
//    public List<MedicalDeviceApproval> crawl510KApprovals(String searchTerm, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawl510KApprovals(searchTerm, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 以新的审批DTO形式返回 510k 数据（支持时间范围）
//     */
//    public List<MedicalDeviceApproval> crawl510KApprovals(String searchTerm, int maxRecords, int batchSize,
//                                                         String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<Device510K> raw = crawlDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
//        List<MedicalDeviceApproval> out = new ArrayList<>();
//        for (Device510K k : raw) {
//            out.add(toApproval(k));
//        }
//        return out;
//    }
//
//    /**
//     * 爬取并保存设备510k审批数据
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510K(String searchTerm, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 爬取并保存设备510k审批数据（支持时间范围和分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510K(String searchTerm, int maxRecords, int batchSize,
//                                                                                   String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<Device510K> list = crawlDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
//        List<com.certification.entity.common.Device510K> saved = new ArrayList<>();
//        List<com.certification.entity.common.Device510K> batchToSave = new ArrayList<>();
//
//        for (Device510K item : list) {
//            // 去重：按K编号唯一
//            if (item.getKNumber() != null && device510KRepository.findByKNumber(item.getKNumber()).isPresent()) {
//                continue;
//            }
//            com.certification.entity.common.Device510K entity = toEntity(item);
//            batchToSave.add(entity);
//
//            // 每50条数据保存一次
//            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
//                List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//                saved.addAll(batchSaved);
//                batchToSave.clear();
//                System.out.printf("已保存 %d 条设备510k审批数据%n", saved.size());
//            }
//        }
//
//        // 保存剩余的数据
//        if (!batchToSave.isEmpty()) {
//            List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//            saved.addAll(batchSaved);
//            System.out.printf("已保存剩余 %d 条设备510k审批数据%n", batchSaved.size());
//        }
//
//        return saved;
//    }
//
//    /**
//     * 根据设备名称爬取510k审批数据
//     */
//    public List<Device510K> crawlDevice510KByDeviceName(String deviceName, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlDevice510KByDeviceName(deviceName, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据设备名称爬取510k审批数据（支持时间范围）
//     */
//    public List<Device510K> crawlDevice510KByDeviceName(String deviceName, int maxRecords, int batchSize,
//                                                       String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (deviceName == null || deviceName.trim().isEmpty())
//                ? ""
//                : "device_name:\"" + deviceName.trim() + "\"";
//        System.out.println("按设备名称搜索: " + term);
//        return crawlDevice510K(term, maxRecords, batchSize, dateFrom, dateTo);
//    }
//
//    /**
//     * 根据设备名称爬取并保存510k审批数据
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByDeviceName(String deviceName, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlAndSaveDevice510KByDeviceName(deviceName, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据设备名称爬取并保存510k审批数据（支持时间范围和分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByDeviceName(String deviceName, int maxRecords, int batchSize,
//                                                                                               String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<Device510K> list = crawlDevice510KByDeviceName(deviceName, maxRecords, batchSize, dateFrom, dateTo);
//        List<com.certification.entity.common.Device510K> saved = new ArrayList<>();
//        List<com.certification.entity.common.Device510K> batchToSave = new ArrayList<>();
//
//        for (Device510K item : list) {
//            if (item.getKNumber() != null && device510KRepository.findByKNumber(item.getKNumber()).isPresent()) {
//                continue;
//            }
//            com.certification.entity.common.Device510K entity = toEntity(item);
//            batchToSave.add(entity);
//
//            // 每50条数据保存一次
//            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
//                List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//                saved.addAll(batchSaved);
//                batchToSave.clear();
//                System.out.printf("已保存 %d 条设备510k审批数据%n", saved.size());
//            }
//        }
//
//        // 保存剩余的数据
//        if (!batchToSave.isEmpty()) {
//            List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//            saved.addAll(batchSaved);
//            System.out.printf("已保存剩余 %d 条设备510k审批数据%n", batchSaved.size());
//        }
//
//        return saved;
//    }
//
//    /**
//     * 根据专业描述关键词爬取510k审批数据
//     */
//    public List<Device510K> crawlDevice510KByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlDevice510KByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据专业描述关键词爬取510k审批数据（支持时间范围）
//     */
//    public List<Device510K> crawlDevice510KByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
//                                                             String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (specialtyKeywords == null || specialtyKeywords.trim().isEmpty())
//                ? ""
//                : "medical_specialty_description:\"" + specialtyKeywords.trim() + "\"";
//        System.out.println("按专业描述关键词搜索: " + term);
//        return crawlDevice510K(term, maxRecords, batchSize, dateFrom, dateTo);
//    }
//
//    /**
//     * 根据专业描述关键词爬取并保存510k审批数据
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlAndSaveDevice510KByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据专业描述关键词爬取并保存510k审批数据（支持时间范围和分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
//                                                                                                     String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<Device510K> list = crawlDevice510KByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, dateFrom, dateTo);
//        List<com.certification.entity.common.Device510K> saved = new ArrayList<>();
//        List<com.certification.entity.common.Device510K> batchToSave = new ArrayList<>();
//
//        for (Device510K item : list) {
//            if (item.getKNumber() != null && device510KRepository.findByKNumber(item.getKNumber()).isPresent()) {
//                continue;
//            }
//            com.certification.entity.common.Device510K entity = toEntity(item);
//            batchToSave.add(entity);
//
//            // 每50条数据保存一次
//            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
//                List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//                saved.addAll(batchSaved);
//                batchToSave.clear();
//                System.out.printf("已保存 %d 条设备510k审批数据%n", saved.size());
//            }
//        }
//
//        // 保存剩余的数据
//        if (!batchToSave.isEmpty()) {
//            List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//            saved.addAll(batchSaved);
//            System.out.printf("已保存剩余 %d 条设备510k审批数据%n", batchSaved.size());
//        }
//
//        return saved;
//    }
//
//    /**
//     * 根据申请人爬取510k审批数据
//     */
//    public List<Device510K> crawlDevice510KByApplicant(String applicant, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlDevice510KByApplicant(applicant, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据申请人爬取510k审批数据（支持时间范围）
//     */
//    public List<Device510K> crawlDevice510KByApplicant(String applicant, int maxRecords, int batchSize,
//                                                       String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (applicant == null || applicant.trim().isEmpty())
//                ? ""
//                : "applicant:" + applicant.trim();
//        System.out.println("按申请人搜索: " + term);
//        return crawlDevice510K(term, maxRecords, batchSize, dateFrom, dateTo);
//    }
//
//    /**
//     * 根据申请人爬取并保存510k审批数据
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByApplicant(String applicant, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlAndSaveDevice510KByApplicant(applicant, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 根据申请人爬取并保存510k审批数据（支持时间范围和分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.Device510K> crawlAndSaveDevice510KByApplicant(String applicant, int maxRecords, int batchSize,
//                                                                                              String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<Device510K> list = crawlDevice510KByApplicant(applicant, maxRecords, batchSize, dateFrom, dateTo);
//        List<com.certification.entity.common.Device510K> saved = new ArrayList<>();
//        List<com.certification.entity.common.Device510K> batchToSave = new ArrayList<>();
//
//        for (Device510K item : list) {
//            if (item.getKNumber() != null && device510KRepository.findByKNumber(item.getKNumber()).isPresent()) {
//                continue;
//            }
//            com.certification.entity.common.Device510K entity = toEntity(item);
//            batchToSave.add(entity);
//
//            // 每50条数据保存一次
//            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
//                List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//                saved.addAll(batchSaved);
//                batchToSave.clear();
//                System.out.printf("已保存 %d 条设备510k审批数据%n", saved.size());
//            }
//        }
//
//        // 保存剩余的数据
//        if (!batchToSave.isEmpty()) {
//            List<com.certification.entity.common.Device510K> batchSaved = device510KRepository.saveAll(batchToSave);
//            saved.addAll(batchSaved);
//            System.out.printf("已保存剩余 %d 条设备510k审批数据%n", batchSaved.size());
//        }
//
//        return saved;
//    }
//
//    /**
//     * 截断字符串到指定长度
//     */
//    private String truncateString(String str, int maxLength) {
//        if (str == null) return null;
//        if (str.length() <= maxLength) return str;
//        System.out.printf("警告: 字段内容过长，已截断至%d字符: %s%n", maxLength, str.substring(0, Math.min(50, str.length())));
//        return str.substring(0, maxLength);
//    }
//
//    /**
//     * 关闭HTTP客户端
//     */
//    public void close() throws IOException {
//        if (httpClient != null) {
//            httpClient.close();
//        }
//    }
//}

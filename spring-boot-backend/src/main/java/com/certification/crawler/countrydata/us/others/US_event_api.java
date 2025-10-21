package com.certification.crawler.countrydata.us.others;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.entity.common.DeviceEventReport;
import com.certification.entity.common.CrawlerCheckpoint;
import com.certification.repository.common.DeviceEventReportRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.certification.util.RiskLevelUtil;
import com.certification.util.KeywordUtil;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * FDA设备不良事件数据爬取器
 * 专门用于爬取FDA设备不良事件数据并保存到数据库
 * 参考DeviceEventCrawler.java的爬取方法，使用US_510K.java的格式结构
 */
@Component
public class US_event_api {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;
    
    @Autowired
    private CrawlerCheckpointRepository crawlerCheckpointRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;

    public US_event_api() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * FDA API响应模型
     */
    public static class FDAResponse {
        @JsonProperty("results")
        private List<DeviceEvent> results;
        
        @JsonProperty("meta")
        private Meta meta;

        public List<DeviceEvent> getResults() { return results; }
        public void setResults(List<DeviceEvent> results) { this.results = results; }
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
     * 设备不良事件数据模型
     */
    public static class DeviceEvent {
        @JsonProperty("event_type")
        private String eventType;
        
        @JsonProperty("report_number")
        private String reportNumber;
        
        @JsonProperty("date_received")
        private String dateReceived;
        
        @JsonProperty("date_of_event")
        private String dateOfEvent;
        
        @JsonProperty("adverse_event_flag")
        private String adverseEventFlag;

        @JsonProperty("date_report_to_fda")
        private String dateReportToFda;

        @JsonProperty("report_to_fda")
        private String reportToFda;

        @JsonProperty("report_to_manufacturer")
        private String reportToManufacturer;

        @JsonProperty("mdr_report_key")
        private String mdrReportKey;

        @JsonProperty("event_location")
        private String eventLocation;

        @JsonProperty("event_key")
        private String eventKey;

        @JsonProperty("number_devices_in_event")
        private String numberDevicesInEvent;

        @JsonProperty("number_patients_in_event")
        private String numberPatientsInEvent;

        @JsonProperty("product_problem_flag")
        private String productProblemFlag;

        @JsonProperty("product_problems")
        private List<String> productProblems;

        @JsonProperty("remedial_action")
        private List<String> remedialAction;

        @JsonProperty("source_type")
        private List<String> sourceType;

        @JsonProperty("type_of_report")
        private List<String> typeOfReport;

        @JsonProperty("mdr_text")
        private List<Map<String, Object>> mdrText;

        @JsonProperty("device")
        private List<Device> devices;
        
        @JsonProperty("patient")
        private List<Patient> patients;

        // 数据适用国家
        private String jdCountry;

        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getReportNumber() { return reportNumber; }
        public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }
        
        public String getDateReceived() { return dateReceived; }
        public void setDateReceived(String dateReceived) { this.dateReceived = dateReceived; }
        
        public String getDateOfEvent() { return dateOfEvent; }
        public void setDateOfEvent(String dateOfEvent) { this.dateOfEvent = dateOfEvent; }
        
        public String getAdverseEventFlag() { return adverseEventFlag; }
        public void setAdverseEventFlag(String adverseEventFlag) { this.adverseEventFlag = adverseEventFlag; }

        public String getDateReportToFda() { return dateReportToFda; }
        public void setDateReportToFda(String dateReportToFda) { this.dateReportToFda = dateReportToFda; }

        public String getReportToFda() { return reportToFda; }
        public void setReportToFda(String reportToFda) { this.reportToFda = reportToFda; }

        public String getReportToManufacturer() { return reportToManufacturer; }
        public void setReportToManufacturer(String reportToManufacturer) { this.reportToManufacturer = reportToManufacturer; }

        public String getMdrReportKey() { return mdrReportKey; }
        public void setMdrReportKey(String mdrReportKey) { this.mdrReportKey = mdrReportKey; }

        public String getEventLocation() { return eventLocation; }
        public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

        public String getEventKey() { return eventKey; }
        public void setEventKey(String eventKey) { this.eventKey = eventKey; }

        public String getNumberDevicesInEvent() { return numberDevicesInEvent; }
        public void setNumberDevicesInEvent(String numberDevicesInEvent) { this.numberDevicesInEvent = numberDevicesInEvent; }

        public String getNumberPatientsInEvent() { return numberPatientsInEvent; }
        public void setNumberPatientsInEvent(String numberPatientsInEvent) { this.numberPatientsInEvent = numberPatientsInEvent; }

        public String getProductProblemFlag() { return productProblemFlag; }
        public void setProductProblemFlag(String productProblemFlag) { this.productProblemFlag = productProblemFlag; }

        public List<String> getProductProblems() { return productProblems; }
        public void setProductProblems(List<String> productProblems) { this.productProblems = productProblems; }

        public List<String> getRemedialAction() { return remedialAction; }
        public void setRemedialAction(List<String> remedialAction) { this.remedialAction = remedialAction; }

        public List<String> getSourceType() { return sourceType; }
        public void setSourceType(List<String> sourceType) { this.sourceType = sourceType; }

        public List<String> getTypeOfReport() { return typeOfReport; }
        public void setTypeOfReport(List<String> typeOfReport) { this.typeOfReport = typeOfReport; }

        public List<Map<String, Object>> getMdrText() { return mdrText; }
        public void setMdrText(List<Map<String, Object>> mdrText) { this.mdrText = mdrText; }

        public List<Device> getDevices() { return devices; }
        public void setDevices(List<Device> devices) { this.devices = devices; }
        
        public List<Patient> getPatients() { return patients; }
        public void setPatients(List<Patient> patients) { this.patients = patients; }

        public String getJdCountry() { return jdCountry; }
        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }
    }

    /**
     * 设备信息模型
     */
    public static class Device {
        @JsonProperty("device_name")
        private String deviceName;
        @JsonProperty("manufacturer_name")
        private String manufacturerName;
        @JsonProperty("model_number")
        private String modelNumber;
        @JsonProperty("generic_name")
        private String genericName;
        @JsonProperty("device_class")
        private String deviceClass;
        @JsonProperty("product_code")
        private String productCode;
        @JsonProperty("k_numbers")
        private List<String> kNumbers;

        // Getters and Setters
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getManufacturerName() { return manufacturerName; }
        public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
        public String getModelNumber() { return modelNumber; }
        public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }
        public String getGenericName() { return genericName; }
        public void setGenericName(String genericName) { this.genericName = genericName; }
        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public List<String> getKNumbers() { return kNumbers; }
        public void setKNumbers(List<String> kNumbers) { this.kNumbers = kNumbers; }
    }

    /**
     * 患者信息模型
     */
    public static class Patient {
        @JsonProperty("patient_sequence_number")
        private String patientSequenceNumber;
        @JsonProperty("patient_age")
        private String patientAge;
        @JsonProperty("patient_sex")
        private String patientSex;
        @JsonProperty("patient_weight")
        private String patientWeight;
        @JsonProperty("patient_outcome")
        private List<String> patientOutcome;

        // Getters and Setters
        public String getPatientSequenceNumber() { return patientSequenceNumber; }
        public void setPatientSequenceNumber(String patientSequenceNumber) { this.patientSequenceNumber = patientSequenceNumber; }
        public String getPatientAge() { return patientAge; }
        public void setPatientAge(String patientAge) { this.patientAge = patientAge; }
        public String getPatientSex() { return patientSex; }
        public void setPatientSex(String patientSex) { this.patientSex = patientSex; }
        public String getPatientWeight() { return patientWeight; }
        public void setPatientWeight(String patientWeight) { this.patientWeight = patientWeight; }
        public List<String> getPatientOutcome() { return patientOutcome; }
        public void setPatientOutcome(List<String> patientOutcome) { this.patientOutcome = patientOutcome; }
    }

    /**
     * 记录爬取信息到CrawlerCheckpoint表
     */
    private void recordCrawlerInfo(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo, 
                                   int totalFetched, int totalSaved, String status, String errorMessage) {
        try {
            CrawlerCheckpoint checkpoint = new CrawlerCheckpoint();
            checkpoint.setCrawlerType("US_Event");
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
     * 通用爬取方法
     */
    public String crawlAndSaveDeviceEvent(String searchTerm, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize, null, null);
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
    public String crawlAndSaveDeviceEvent(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (maxRecords == -1) {
            System.out.println("开始爬取FDA设备不良事件数据，搜索词: " + searchTerm + ", 爬取模式: 所有数据, 批次大小: " + batchSize);
        } else {
            System.out.println("开始爬取FDA设备不良事件数据，搜索词: " + searchTerm + ", 最大记录数: " + maxRecords + ", 批次大小: " + batchSize);
        }
        
        try {
            String result = crawlAndSaveDeviceEventInBatches(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
            return result;
            
        } catch (Exception e) {
            System.err.println("爬取FDA设备不良事件数据失败: " + e.getMessage());
            e.printStackTrace();
            
            recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, 0, 0, "FAILED", e.getMessage());
            
            return "FDA设备不良事件数据爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按设备名称搜索
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize) {
        String searchTerm = "device.device_name:" + deviceName;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按设备名称搜索（支持时间范围）
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "device.device_name:" + deviceName;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按制造商搜索
     */
    public String crawlAndSaveByManufacturer(String manufacturer, int maxRecords, int batchSize) {
        String searchTerm = "device.manufacturer_name:" + manufacturer;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按制造商搜索（支持时间范围）
     */
    public String crawlAndSaveByManufacturer(String manufacturer, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "device.manufacturer_name:" + manufacturer;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按产品问题搜索
     */
    public String crawlAndSaveByProductProblem(String productProblem, int maxRecords, int batchSize) {
        String searchTerm = "product_problems:" + productProblem;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按产品问题搜索（支持时间范围）
     */
    public String crawlAndSaveByProductProblem(String productProblem, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "product_problems:" + productProblem;
        return crawlAndSaveDeviceEvent(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 基于关键词列表爬取FDA设备不良事件数据
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认搜索");
            return crawlAndSaveDeviceEvent("device.device_name:medical", maxRecords, batchSize, dateFrom, dateTo);
        }

        System.out.println("开始基于关键词列表爬取FDA设备不良事件数据...");
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
                // 1. 使用关键词作为设备名称进行搜索
                System.out.println("关键词 '" + keyword + "' 作为设备名称搜索");
                String deviceNameResult = crawlAndSaveByDeviceName(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("设备名称搜索结果: " + deviceNameResult);

                // 2. 使用关键词作为制造商进行搜索
                System.out.println("关键词 '" + keyword + "' 作为制造商搜索");
                String manufacturerResult = crawlAndSaveByManufacturer(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("制造商搜索结果: " + manufacturerResult);

                // 3. 使用关键词作为产品问题进行搜索
                System.out.println("关键词 '" + keyword + "' 作为产品问题搜索");
                String productProblemResult = crawlAndSaveByProductProblem(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("产品问题搜索结果: " + productProblemResult);

                // 解析结果中的保存数量
                totalSaved += extractSavedCount(deviceNameResult);
                totalSaved += extractSavedCount(manufacturerResult);
                totalSaved += extractSavedCount(productProblemResult);

            } catch (Exception e) {
                System.err.println("处理关键词 '" + keyword + "' 时发生错误: " + e.getMessage());
            }
        }

        return String.format("基于关键词列表的FDA设备不良事件数据爬取完成，总共保存: %d 条记录", totalSaved);
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
     * 分批爬取并保存设备不良事件数据
     */
    private String crawlAndSaveDeviceEventInBatches(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) 
            throws IOException, URISyntaxException, ParseException {
        
        List<DeviceEvent> allResults = crawlDeviceEvent(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        
        if (allResults.isEmpty()) {
            return "未找到匹配的设备不良事件数据";
        }

        return saveBatchToDatabase(allResults);
    }

    /**
     * 爬取设备不良事件数据
     */
    private List<DeviceEvent> crawlDeviceEvent(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        
        System.out.println("开始爬取设备不良事件数据...");
        
        List<DeviceEvent> allResults = new ArrayList<>();
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

            System.out.printf("获取第 %d 页设备不良事件数据（偏移量: %d，数量: %d）%n", 
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
            
            // 添加时间范围参数（使用date_received字段，这是FDA接收报告的日期）
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                String effectiveDateTo = (dateTo != null && !dateTo.trim().isEmpty()) ? dateTo.trim() : "20991231";
                String dateRange = String.format("date_received:[%s+TO+%s]", dateFrom.trim(), effectiveDateTo);
                
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
                FDAResponse response = fetchData("/device/event.json", params);
                List<DeviceEvent> results = response != null ? response.getResults() : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备不良事件数据，爬取结束");
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

                // 转换为DeviceEvent对象并设置国家
                for (DeviceEvent event : results) {
                    event.setJdCountry("US");
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

        List<DeviceEvent> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备不良事件数据爬取完成，共 %d 条数据%n", finalResults.size());
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
                                    
                                    if ("NOT_FOUND".equals(errorCode) && "No matches found!".equals(errorMessage)) {
                                        System.out.println("API返回：未找到匹配记录 - " + errorMessage);
                                        FDAResponse emptyResponse = new FDAResponse();
                                        emptyResponse.setResults(new ArrayList<>());
                                        return emptyResponse;
                                    } else {
                                        System.err.printf("API返回错误: code=%s, message=%s%n", errorCode, errorMessage);
                                        throw new IOException("API错误: " + errorCode + " - " + errorMessage);
                                    }
                                }
                            } catch (Exception parseError) {
                                System.err.println("解析错误响应失败: " + parseError.getMessage());
                                throw new IOException("API返回错误响应，解析失败: " + parseError.getMessage());
                            }
                        }
                        
                        return objectMapper.readValue(json, FDAResponse.class);
                    }
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
    private String saveBatchToDatabase(List<DeviceEvent> records) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int batchCount = 0;

        for (int i = 0; i < records.size(); i += crawlerConfig.getBatch().getSaveSize()) {
            int endIndex = Math.min(i + crawlerConfig.getBatch().getSaveSize(), records.size());
            List<DeviceEvent> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceEventReport> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (DeviceEvent record : batch) {
                try {
                    // 检查是否已存在（使用reportNumber）
                    boolean isDuplicate = false;
                    
                    String reportNumber = record.getReportNumber();
                    
                    // 重试机制：最多重试3次
                    int retryCount = 0;
                    int maxRetries = 3;
                    boolean querySuccess = false;
                    
                    while (retryCount < maxRetries && !querySuccess) {
                        try {
                            if (reportNumber != null && !reportNumber.trim().isEmpty()) {
                                if (deviceEventReportRepository.findByReportNumber(reportNumber).isPresent()) {
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
                        DeviceEventReport entity = convertToEntity(record);
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
                        List<DeviceEventReport> savedRecords = deviceEventReportRepository.saveAll(newRecords);
                        savedCount += savedRecords.size();
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
        }

        return savedCount + " 条记录";
    }

    /**
     * 将API的事件模型转换为实体
     */
    private DeviceEventReport convertToEntity(DeviceEvent src) {
        if (src == null) return null;

        DeviceEventReport entity = new DeviceEventReport();

        // 设置基本信息
        entity.setReportNumber(truncateString(src.getReportNumber(), 64));
        entity.setDateOfEvent(parseDate(src.getDateOfEvent()));
        entity.setDateReceived(parseDate(src.getDateReceived()));
        entity.setDataSource("FDA");
        entity.setJdCountry(src.getJdCountry());
        
        // 设置爬取时间
        entity.setCrawlTime(java.time.LocalDateTime.now());
        
        // 从设备信息中提取字段
        if (src.getDevices() != null && !src.getDevices().isEmpty()) {
            Device device = src.getDevices().get(0); // 取第一个设备
            entity.setBrandName(truncateString(device.getDeviceName(), 255));
            entity.setGenericName(truncateString(device.getGenericName(), 255));
            entity.setManufacturerName(truncateString(device.getManufacturerName(), 255));
            entity.setDeviceClass(truncateString(device.getDeviceClass(), 10));
        }
        
        
        // 计算风险等级
        RiskLevel calculatedRiskLevel = RiskLevelUtil.calculateRiskLevelByEventType(src.getEventType());
        entity.setRiskLevel(calculatedRiskLevel);
        
        // 提取关键词
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();
        
        // 从设备信息提取关键词
        if (src.getDevices() != null && !src.getDevices().isEmpty()) {
            for (Device device : src.getDevices()) {
                if (device.getDeviceName() != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(device.getDeviceName(), predefinedKeywords));
                }
                if (device.getManufacturerName() != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(device.getManufacturerName(), predefinedKeywords));
                }
            }
        }
        
        // 从产品问题提取关键词
        if (src.getProductProblems() != null) {
            for (String problem : src.getProductProblems()) {
                if (problem != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(problem, predefinedKeywords));
                }
            }
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
            "event", "adverse", "malfunction", "injury", "death", "medical specialty", "device class"
        );
    }

    /**
     * 将字符串列表连接为CSV格式
     */
    private String joinCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list.stream().filter(Objects::nonNull).toArray(String[]::new));
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignore) {}
        }
        return null;
    }

    /**
     * 解析整数
     */
    private Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
     * 基于多字段参数爬取Event数据（新的统一方法）⭐
     * 支持：brandNames, manufacturerNames, genericNames, dateFrom/dateTo
     * 
     * @param brandNames 品牌名称列表
     * @param manufacturerNames 制造商名称列表
     * @param genericNames 通用名称列表
     * @param dateFrom 起始日期（yyyyMMdd格式）
     * @param dateTo 结束日期（yyyyMMdd格式）
     * @param maxRecords 最大记录数（-1表示全部）
     * @param batchSize 批次大小
     * @return 爬取结果信息
     */
    public String crawlAndSaveWithMultipleFields(
            List<String> brandNames,
            List<String> manufacturerNames,
            List<String> genericNames,
            String dateFrom,
            String dateTo,
            int maxRecords,
            int batchSize) {
        
        System.out.println("开始使用多字段参数爬取FDA Event数据...");
        System.out.println("品牌名称数量: " + (brandNames != null ? brandNames.size() : 0));
        System.out.println("制造商名称数量: " + (manufacturerNames != null ? manufacturerNames.size() : 0));
        System.out.println("通用名称数量: " + (genericNames != null ? genericNames.size() : 0));
        System.out.println("日期范围: " + dateFrom + " - " + dateTo);
        System.out.println("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));
        
        int totalSaved = 0;
        
        // 1. 按品牌名称搜索
        if (brandNames != null && !brandNames.isEmpty()) {
            for (String brandName : brandNames) {
                if (brandName == null || brandName.trim().isEmpty()) continue;
                
                System.out.println("按品牌名称搜索: " + brandName);
                String searchQuery = "device.brand_name:" + brandName.trim();
                
                try {
                    String result = crawlAndSaveDeviceEvent(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("品牌名称搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("品牌名称 '" + brandName + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        // 2. 按制造商名称搜索
        if (manufacturerNames != null && !manufacturerNames.isEmpty()) {
            for (String manufacturer : manufacturerNames) {
                if (manufacturer == null || manufacturer.trim().isEmpty()) continue;
                
                System.out.println("按制造商名称搜索: " + manufacturer);
                String searchQuery = "device.manufacturer_name:" + manufacturer.trim();
                
                try {
                    String result = crawlAndSaveDeviceEvent(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("制造商名称搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("制造商名称 '" + manufacturer + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        // 3. 按通用名称搜索
        if (genericNames != null && !genericNames.isEmpty()) {
            for (String genericName : genericNames) {
                if (genericName == null || genericName.trim().isEmpty()) continue;
                
                System.out.println("按通用名称搜索: " + genericName);
                String searchQuery = "device.generic_name:" + genericName.trim();
                
                try {
                    String result = crawlAndSaveDeviceEvent(searchQuery, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    System.out.println("通用名称搜索结果: " + result);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("通用名称 '" + genericName + "' 搜索失败: " + e.getMessage());
                }
            }
        }
        
        String finalResult = String.format(
            "多字段Event数据爬取完成，总保存: %d 条记录", totalSaved);
        System.out.println(finalResult);
        
        return finalResult;
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        System.out.println("=== US_Event 爬虫测试 ===");
        
        try {
            US_event_api crawler = new US_event_api();
            
            // 测试1: 按设备名称搜索
            System.out.println("\n1. 测试按设备名称搜索...");
            String result1 = crawler.crawlAndSaveByDeviceName("skin", 10, 5);
            System.out.println("   结果: " + result1);
            
            // 测试2: 按制造商搜索
            System.out.println("\n2. 测试按制造商搜索...");
            String result2 = crawler.crawlAndSaveByManufacturer("medtronic", 10, 5);
            System.out.println("   结果: " + result2);
            
            // 测试3: 按产品问题搜索
            System.out.println("\n3. 测试按产品问题搜索...");
            String result3 = crawler.crawlAndSaveByProductProblem("malfunction", 10, 5);
            System.out.println("   结果: " + result3);
            
            // 测试4: 基于关键词列表搜索
            System.out.println("\n4. 测试基于关键词列表搜索...");
            List<String> keywords = Arrays.asList("skin", "analyzer", "monitor");
            String result4 = crawler.crawlAndSaveWithKeywords(keywords, 20, 5, null, null);
            System.out.println("   结果: " + result4);
            
            crawler.close();
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.certification.crawler.generalArchitecture.us;

import com.certification.repository.common.DeviceEventReportRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.certification.entity.common.CrawlerCheckpoint;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.certification.dto.MedicalDeviceEventSingle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * FDA设备不良事件爬取器
 * 专门用于爬取设备不良事件数据
 */
@Component
public class DeviceEventCrawler {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final DeviceEventReportRepository deviceEventReportRepository;
    private final CrawlerCheckpointRepository checkpointRepository;
    
    private static final String CRAWLER_TYPE = "DeviceEvent";
    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次

    @Autowired
    public DeviceEventCrawler(DeviceEventReportRepository deviceEventReportRepository,
                             CrawlerCheckpointRepository checkpointRepository) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.deviceEventReportRepository = deviceEventReportRepository;
        this.checkpointRepository = checkpointRepository;
    }

    /**
     * 设备不良事件数据模型（精简保留重要字段）
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

        // 新增：数据适用国家（非FDA原字段，用于业务标记）
        @JsonProperty("jd_country")
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
        public void setMdrText(List<Map<String, Object>> mdrText) {
            this.mdrText = mdrText; }

        public List<Device> getDevices() { return devices; }
        public void setDevices(List<Device> devices) { this.devices = devices; }
        
        public List<Patient> getPatients() { return patients; }
        public void setPatients(List<Patient> patients) { this.patients = patients; }

        public String getJdCountry() { return jdCountry; }
        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }

        public static class Device {
            @JsonProperty("brand_name")
            private String brandName;
            
            @JsonProperty("generic_name")
            private String genericName;
            
            @JsonProperty("manufacturer_d_name")
            private String manufacturerName;
            
            @JsonProperty("model_number")
            private String modelNumber;
            
            @JsonProperty("device_report_product_code")
            private String deviceReportProductCode;
            
            @JsonProperty("openfda")
            private OpenFDA openfda;

            // Getters and Setters
            public String getBrandName() { return brandName; }
            public void setBrandName(String brandName) { this.brandName = brandName; }
            
            public String getGenericName() { return genericName; }
            public void setGenericName(String genericName) { this.genericName = genericName; }
            
            public String getManufacturerName() { return manufacturerName; }
            public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
            
            public String getModelNumber() { return modelNumber; }
            public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }
            
            public String getDeviceReportProductCode() { return deviceReportProductCode; }
            public void setDeviceReportProductCode(String deviceReportProductCode) { this.deviceReportProductCode = deviceReportProductCode; }
            
            public OpenFDA getOpenfda() { return openfda; }
            public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
        }

        public static class Patient {
            @JsonProperty("patient_sequence_number")
            private String patientSequenceNumber;
            
            @JsonProperty("patient_age")
            private String patientAge;
            
            @JsonProperty("patient_sex")
            private String patientSex;

            // Getters and Setters
            public String getPatientSequenceNumber() { return patientSequenceNumber; }
            public void setPatientSequenceNumber(String patientSequenceNumber) { this.patientSequenceNumber = patientSequenceNumber; }
            
            public String getPatientAge() { return patientAge; }
            public void setPatientAge(String patientAge) { this.patientAge = patientAge; }
            
            public String getPatientSex() { return patientSex; }
            public void setPatientSex(String patientSex) { this.patientSex = patientSex; }
        }
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
     * 爬取设备不良事件数据
     */
    public List<DeviceEvent> crawlDeviceEvents(String searchTerm, int maxRecords, int batchSize) 
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceEvents(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取设备不良事件数据（支持时间范围和断点续传）
     */
    public List<DeviceEvent> crawlDeviceEvents(String searchTerm, int maxRecords, int batchSize,
                                              String dateFrom, String dateTo) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备不良事件数据...");
        
        // 检查是否有断点可以继续
        CrawlerCheckpoint checkpoint = getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        
        if (checkpoint.isCompleted()) {
            System.out.println("爬取任务已完成，无需重复执行");
            return new ArrayList<>();
        }

        List<DeviceEvent> allResults = new ArrayList<>();
        int skip = checkpoint.getCurrentSkip();
        int totalFetched = checkpoint.getTotalFetched();

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
            
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备不良事件数据（偏移量: %d，数量: %d）%n", 
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
                Map<String, Object> data = fetchData("/device/event.json", params);
                List<Map<String, Object>> results = data != null ? 
                        (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备不良事件数据，爬取结束");
                    checkpoint.markCompleted();
                    checkpointRepository.save(checkpoint);
                    break;
                }

                // 转换为DeviceEvent对象
                for (Map<String, Object> result : results) {
                    DeviceEvent event = objectMapper.convertValue(result, DeviceEvent.class);
                    // 新增：设置国家归属
                    event.setJdCountry("US");
                    allResults.add(event);
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

        List<DeviceEvent> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备不良事件数据爬取完成，共 %d 条数据%n", finalResults.size());
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
     * 将API事件模型转换为实体（扁平化单表结构）
     */
    private com.certification.entity.common.DeviceEventReport toEntity(DeviceEvent src) {
        if (src == null) return null;
        com.certification.entity.common.DeviceEventReport e = new com.certification.entity.common.DeviceEventReport();
        // 基本信息
        e.setReportNumber(src.getReportNumber());
        e.setEventType(src.getEventType());
        e.setTypeOfReport(joinCsv(src.getTypeOfReport()));
        e.setDateOfEvent(parseDate(src.getDateOfEvent()));
        e.setDateReport(parseDate(src.getDateReportToFda()));
        e.setDateReceived(parseDate(src.getDateReceived()));
        e.setSourceType(joinCsv(src.getSourceType()));
        e.setReportSourceCode(null);

        // 设备与制造商信息（取第一台）
        DeviceEvent.Device dev = firstOrNull(src.getDevices());
        if (dev != null) {
            e.setBrandName(dev.getBrandName());
            e.setModelNumber(dev.getModelNumber());
            e.setGenericName(dev.getGenericName());
            e.setManufacturerName(dev.getManufacturerName());
            // 城市/州/国家通常不在该接口，置空
            e.setManufacturerCity(null);
            e.setManufacturerState(null);
            e.setManufacturerCountry(null);
            if (dev.getOpenfda() != null) {
                e.setDeviceClass(dev.getOpenfda().getDeviceClass());
                e.setMedicalSpecialty(dev.getOpenfda().getMedicalSpecialtyDescription());
                e.setRegulationNumber(dev.getOpenfda().getRegulationNumber());
            }
        }

        // 其他信息
        e.setDeviceEvaluatedByManufacturer(null);
        e.setMdrTextDescription(extractMdrText(src.getMdrText(), true));
        e.setMdrTextAction(extractMdrText(src.getMdrText(), false));
        e.setContactPerson(null);
        e.setContactPhone(null);
        LocalDate added = parseDate(getString(src, "date_added"));
        e.setDateAdded(added != null ? added : parseDate(src.getDateReceived()));
        e.setPatientCount(parseIntSafe(src.getNumberPatientsInEvent()));

        // 数据来源
        e.setDataSource("FDA");
        // 新增：国家归属（优先使用来源对象中的值，兜底US）
        e.setJdCountry(src != null && src.getJdCountry() != null ? src.getJdCountry() : "US");
        return e;
    }

    /**
     * 爬取并保存设备不良事件（按 reportNumber 去重）
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEvents(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlAndSaveDeviceEvents(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取并保存设备不良事件（支持时间范围和分批保存）
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEvents(String searchTerm, int maxRecords, int batchSize,
                                                                                           String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        List<DeviceEvent> list = crawlDeviceEvents(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        List<com.certification.entity.common.DeviceEventReport> saved = new ArrayList<>();
        List<com.certification.entity.common.DeviceEventReport> batchToSave = new ArrayList<>();
        
        for (DeviceEvent item : list) {
            boolean exists = item.getReportNumber() != null && !item.getReportNumber().isEmpty()
                    && deviceEventReportRepository.findByReportNumber(item.getReportNumber()).isPresent();
            if (exists) { continue; }
            com.certification.entity.common.DeviceEventReport entity = toEntity(item);
            batchToSave.add(entity);
            
            // 每50条数据保存一次
            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
                List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
                saved.addAll(batchSaved);
                batchToSave.clear();
                System.out.printf("已保存 %d 条设备不良事件数据%n", saved.size());
            }
        }
        
        // 保存剩余的数据
        if (!batchToSave.isEmpty()) {
            List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
            saved.addAll(batchSaved);
            System.out.printf("已保存剩余 %d 条设备不良事件数据%n", batchSaved.size());
        }
        
        return saved;
    }

    /**
     * 按设备名称搜索并保存（在device数组内的brand_name字段上搜索）
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveEventsByDeviceName(String deviceName, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        String term = (deviceName == null || deviceName.trim().isEmpty()) ? "" : "device.brand_name:\"" + deviceName.trim() + "\"";
        return crawlAndSaveDeviceEvents(term, maxRecords, batchSize);
    }

    /**
     * 以扁平单表模型返回设备不良事件数据
     */
    public List<MedicalDeviceEventSingle> crawlDeviceEventsSingle(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        List<DeviceEvent> raw = crawlDeviceEvents(searchTerm, maxRecords, batchSize);
        List<MedicalDeviceEventSingle> out = new ArrayList<>();
        for (DeviceEvent e : raw) {
            out.add(toSingle(e));
        }
        return out;
    }

    private MedicalDeviceEventSingle toSingle(DeviceEvent src) {
        MedicalDeviceEventSingle d = new MedicalDeviceEventSingle();
        // 主��由数据库生成，此处不赋值
        d.setReportNumber(src.getReportNumber());
        d.setEventType(src.getEventType());
        d.setTypeOfReport(joinCsv(src.getTypeOfReport()));
        d.setDateOfEvent(parseDate(src.getDateOfEvent()));
        // 将 FDA 提交日期映射为报告提交日期
        d.setDateReport(parseDate(src.getDateReportToFda()));
        d.setDateReceived(parseDate(src.getDateReceived()));
        d.setSourceType(joinCsv(src.getSourceType()));
        // 报告来源代码：API未必提供，优先保留为空或后续补充映射
        d.setReportSourceCode(null);

        // 设备与制造商信息（取第一台设备为代表）
        DeviceEvent.Device dev = firstOrNull(src.getDevices());
        if (dev != null) {
            d.setBrandName(dev.getBrandName());
            d.setModelNumber(dev.getModelNumber());
            d.setGenericName(dev.getGenericName());
            d.setManufacturerName(dev.getManufacturerName());
            // 城市/州/国家字段在 event 接口通常不存在，保留为空
            d.setManufacturerCity(null);
            d.setManufacturerState(null);
            d.setManufacturerCountry(null);
            if (dev.getOpenfda() != null) {
                d.setDeviceClass(dev.getOpenfda().getDeviceClass());
                d.setMedicalSpecialty(dev.getOpenfda().getMedicalSpecialtyDescription());
                d.setRegulationNumber(dev.getOpenfda().getRegulationNumber());
            }
        }

        // 制造商是否评估设备（API字段可能缺失，留空）
        d.setDeviceEvaluatedByManufacturer(null);

        // 提取文本描述与措施
        d.setMdrTextDescription(extractMdrText(src.getMdrText(), true));
        d.setMdrTextAction(extractMdrText(src.getMdrText(), false));

        // 联系人与电话：接口常无，预留为空
        d.setContactPerson(null);
        d.setContactPhone(null);

        // dateAdded：接口可能包含 date_added；若无则用 dateReceived 兜底
        LocalDate added = parseDate(getString(src, "date_added"));
        d.setDateAdded(added != null ? added : parseDate(src.getDateReceived()));

        // 患者数量
        d.setPatientCount(parseIntSafe(src.getNumberPatientsInEvent()));
        // 数据来源
        d.setDataSource("FDA");
        // 新增：国家归属（优先使用来源对象中的值，兜底US）
        d.setJdCountry(src != null && src.getJdCountry() != null ? src.getJdCountry() : "US");
        return d;
    }

    private String getString(DeviceEvent src, String key) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(src, Map.class);
            Object v = map.get(key);
            return v == null ? null : String.valueOf(v);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    private DeviceEvent.Device firstOrNull(List<DeviceEvent.Device> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private String joinCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    private Integer parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException ignore) { return null; }
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

    private String extractMdrText(List<Map<String, Object>> mdrText, boolean description) {
        if (mdrText == null || mdrText.isEmpty()) return null;

        // 1) 优先：按 text_type_code 的明确值匹配
        for (Map<String, Object> t : mdrText) {
            String code = val(t.get("text_type_code"));
            String text = trimToNull(val(t.get("text")));
            if (text == null) continue;

            if (description) {
                if (equalsIgnoreCase(code, "Description of Event or Problem") || equalsIgnoreCase(code, "Description") || "D".equalsIgnoreCase(code)) {
                    return text;
                }
            } else {
                if (equalsIgnoreCase(code, "Additional Manufacturer Narrative") || equalsIgnoreCase(code, "Manufacturer Narrative") || equalsIgnoreCase(code, "Manufacturer") || "M".equalsIgnoreCase(code)) {
                    return text;
                }
            }
        }

        // 2) 其次：使用关键词模糊匹配（兼容不同写法）
        for (Map<String, Object> t : mdrText) {
            String code = val(t.get("text_type_code"));
            String type2 = val(t.get("type")); // 某些数据源可能提供该键
            String text = trimToNull(val(t.get("text")));
            if (text == null) continue;
            String combined = ((code == null ? "" : code) + " " + (type2 == null ? "" : type2)).toLowerCase();
            if (description) {
                if (combined.contains("description") || combined.contains("event or problem")) {
                    return text;
                }
            } else {
                if (combined.contains("manufacturer") || combined.contains("action") || combined.contains("narrative")) {
                    return text;
                }
            }
        }

        // 3) 兜底：返回第一个非空文本
        for (Map<String, Object> t : mdrText) {
            String text = trimToNull(val(t.get("text")));
            if (text != null) return text;
        }
        return null;
    }

    private String val(Object o) { return o == null ? null : String.valueOf(o); }
    private String trimToNull(String s) { if (s == null) return null; String r = s.trim(); return r.isEmpty() ? null : r; }
    private boolean containsIgnoreCase(String s, String kw) { return s != null && s.toLowerCase().contains(kw.toLowerCase()); }
    private boolean equalsIgnoreCase(String a, String b) { return a != null && b != null && a.equalsIgnoreCase(b); }

    /**
     * 根据设备名称爬取设备不良事件数据
     */
    public List<DeviceEvent> crawlDeviceEventsByDeviceName(String deviceName, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceEventsByDeviceName(deviceName, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据设备名称爬取设备不良事件数据（支持时间范围）
     */
    public List<DeviceEvent> crawlDeviceEventsByDeviceName(String deviceName, int maxRecords, int batchSize,
                                                          String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (deviceName == null || deviceName.trim().isEmpty())
                ? ""
                : "device.generic_name:\"" + deviceName.trim() + "\"";
        System.out.println("按设备名称搜索: " + term);
        return crawlDeviceEvents(term, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 根据设备名称爬取并保存设备不良事件数据
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEventsByDeviceName(String deviceName, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlAndSaveDeviceEventsByDeviceName(deviceName, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据设备名称爬取并保存设备不良事件数据（支持时间范围和分批保存）
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEventsByDeviceName(String deviceName, int maxRecords, int batchSize,
                                                                                                        String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        List<DeviceEvent> list = crawlDeviceEventsByDeviceName(deviceName, maxRecords, batchSize, dateFrom, dateTo);
        List<com.certification.entity.common.DeviceEventReport> saved = new ArrayList<>();
        List<com.certification.entity.common.DeviceEventReport> batchToSave = new ArrayList<>();
        
        for (DeviceEvent item : list) {
            if (item.getReportNumber() != null && deviceEventReportRepository.findByReportNumber(item.getReportNumber()).isPresent()) {
                continue;
            }
            com.certification.entity.common.DeviceEventReport entity = toEntity(item);
            batchToSave.add(entity);
            
            // 每50条数据保存一次
            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
                List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
                saved.addAll(batchSaved);
                batchToSave.clear();
                System.out.printf("已保存 %d 条设备不良事件数据%n", saved.size());
            }
        }
        
        // 保存剩余的数据
        if (!batchToSave.isEmpty()) {
            List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
            saved.addAll(batchSaved);
            System.out.printf("已保存剩余 %d 条设备不良事件数据%n", batchSaved.size());
        }
        
        return saved;
    }

    /**
     * 根据专业描述关键词爬取设备不良事件数据
     */
    public List<DeviceEvent> crawlDeviceEventsByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceEventsByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据专业描述关键词爬取设备不良事件数据（支持时间范围）
     */
    public List<DeviceEvent> crawlDeviceEventsByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
                                                                String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (specialtyKeywords == null || specialtyKeywords.trim().isEmpty())
                ? ""
                : "device.openfda.medical_specialty_description:\"" + specialtyKeywords.trim() + "\"";
        System.out.println("按专业描述关键词搜索: " + term);
        return crawlDeviceEvents(term, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 根据专业描述关键词爬取并保存设备不良事件数据
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEventsByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlAndSaveDeviceEventsByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据专业描述关键词爬取并保存设备不良事件数据（支持时间范围和分批保存）
     */
    @Transactional
    public List<com.certification.entity.common.DeviceEventReport> crawlAndSaveDeviceEventsByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
                                                                                                              String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        List<DeviceEvent> list = crawlDeviceEventsByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, dateFrom, dateTo);
        List<com.certification.entity.common.DeviceEventReport> saved = new ArrayList<>();
        List<com.certification.entity.common.DeviceEventReport> batchToSave = new ArrayList<>();
        
        for (DeviceEvent item : list) {
            if (item.getReportNumber() != null && deviceEventReportRepository.findByReportNumber(item.getReportNumber()).isPresent()) {
                continue;
            }
            com.certification.entity.common.DeviceEventReport entity = toEntity(item);
            batchToSave.add(entity);
            
            // 每50条数据保存一次
            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
                List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
                saved.addAll(batchSaved);
                batchToSave.clear();
                System.out.printf("已保存 %d 条设备不良事件数据%n", saved.size());
            }
        }
        
        // 保存剩余的数据
        if (!batchToSave.isEmpty()) {
            List<com.certification.entity.common.DeviceEventReport> batchSaved = deviceEventReportRepository.saveAll(batchToSave);
            saved.addAll(batchSaved);
            System.out.printf("已保存剩余 %d 条设备不良事件数据%n", batchSaved.size());
        }
        
        return saved;
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

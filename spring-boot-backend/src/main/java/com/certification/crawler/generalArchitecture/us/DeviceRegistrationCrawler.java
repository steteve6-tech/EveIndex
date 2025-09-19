//package com.certification.crawler.generalArchitecture.us;
//
//import com.certification.dto.MedicalDeviceManufacturerKey;
//import com.certification.repository.common.DeviceRegistrationRecordRepository;
//import com.certification.repository.common.CrawlerCheckpointRepository;
//import com.certification.entity.common.CrawlerCheckpoint;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.core.JsonProcessingException;
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
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * FDA设备注册信息爬取器
// * 专门用于爬取设备注册信息数据
// */
//@Component
//public class DeviceRegistrationCrawler {
//
//    private static final String BASE_URL = "https://api.fda.gov";
//    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
//    private static final int RETRY_COUNT = 3;
//    private static final int RETRY_DELAY = 5;
//
//    private final CloseableHttpClient httpClient;
//    private final ObjectMapper objectMapper;
//    private final DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
//    private final CrawlerCheckpointRepository checkpointRepository;
//
//    private static final String CRAWLER_TYPE = "DeviceRegistration";
//    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次
//
//    @Autowired
//    public DeviceRegistrationCrawler(DeviceRegistrationRecordRepository deviceRegistrationRecordRepository,
//                                   CrawlerCheckpointRepository checkpointRepository) {
//        this.httpClient = HttpClients.createDefault();
//        this.objectMapper = new ObjectMapper();
//        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        this.deviceRegistrationRecordRepository = deviceRegistrationRecordRepository;
//        this.checkpointRepository = checkpointRepository;
//    }
//
//    /**
//     * 设备注册信息数据模型
//     */
//    public static class DeviceRegistration {
//        @JsonProperty("proprietary_name")
//        private List<String> proprietaryName;
//
//        @JsonProperty("establishment_type")
//        private List<String> establishmentType;
//
//        @JsonProperty("registration")
//        private Registration registration;
//
//        @JsonProperty("pma_number")
//        private String pmaNumber;
//
//        @JsonProperty("k_number")
//        private String kNumber;
//
//        @JsonProperty("products")
//        private List<Product> products;
//        // 数据适用国家
//        private String jdCountry;
//
//        // Getters and Setters
//        public List<String> getProprietaryName() { return proprietaryName; }
//        public void setProprietaryName(List<String> proprietaryName) { this.proprietaryName = proprietaryName; }
//
//        public List<String> getEstablishmentType() { return establishmentType; }
//        public void setEstablishmentType(List<String> establishmentType) { this.establishmentType = establishmentType; }
//
//        public Registration getRegistration() { return registration; }
//        public void setRegistration(Registration registration) { this.registration = registration; }
//
//        public String getPmaNumber() { return pmaNumber; }
//        public void setPmaNumber(String pmaNumber) { this.pmaNumber = pmaNumber; }
//
//        public String getKNumber() { return kNumber; }
//        public void setKNumber(String kNumber) { this.kNumber = kNumber; }
//
//        public List<Product> getProducts() { return products; }
//        public void setProducts(List<Product> products) { this.products = products; }
//        public String getJdCountry() { return jdCountry; }
//        public void setJdCountry(String jdCountry) { this.jdCountry = jdCountry; }
//
//        public static class Registration {
//            @JsonProperty("registration_number")
//            private String registrationNumber;
//
//            @JsonProperty("fei_number")
//            private String feiNumber;
//
//            @JsonProperty("status_code")
//            private String statusCode;
//
//            @JsonProperty("name")
//            private String name;
//
//            @JsonProperty("address_line_1")
//            private String addressLine1;
//
//            @JsonProperty("city")
//            private String city;
//
//            @JsonProperty("state_code")
//            private String stateCode;
//
//            @JsonProperty("iso_country_code")
//            private String isoCountryCode;
//
//            @JsonProperty("postal_code")
//            private String postalCode;
//
//            @JsonProperty("address_line_2")
//            private String addressLine2;
//            @JsonProperty("zip_code")
//            private String zipCode;
//            @JsonProperty("initial_importer_flag")
//            private String initialImporterFlag;
//            @JsonProperty("reg_expiry_date_year")
//            private String regExpiryDateYear;
//            @JsonProperty("us_agent")
//            private UsAgent usAgent;
//            @JsonProperty("owner_operator")
//            private OwnerOperator ownerOperator;
//
//            public String getRegistrationNumber() { return registrationNumber; }
//            public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
//            public String getFeiNumber() { return feiNumber; }
//            public void setFeiNumber(String feiNumber) { this.feiNumber = feiNumber; }
//
//            public String getStatusCode() { return statusCode; }
//            public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
//
//            public String getName() { return name; }
//            public void setName(String name) { this.name = name; }
//
//            public String getAddressLine1() { return addressLine1; }
//            public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
//
//            public String getCity() { return city; }
//            public void setCity(String city) { this.city = city; }
//
//            public String getStateCode() { return stateCode; }
//            public void setStateCode(String stateCode) { this.stateCode = stateCode; }
//
//            public String getIsoCountryCode() { return isoCountryCode; }
//            public void setIsoCountryCode(String isoCountryCode) { this.isoCountryCode = isoCountryCode; }
//
//            public String getPostalCode() { return postalCode; }
//            public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
//            public String getAddressLine2() { return addressLine2; }
//            public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
//            public String getZipCode() { return zipCode; }
//            public void setZipCode(String zipCode) { this.zipCode = zipCode; }
//            public String getInitialImporterFlag() { return initialImporterFlag; }
//            public void setInitialImporterFlag(String initialImporterFlag) { this.initialImporterFlag = initialImporterFlag; }
//            public String getRegExpiryDateYear() { return regExpiryDateYear; }
//            public void setRegExpiryDateYear(String regExpiryDateYear) { this.regExpiryDateYear = regExpiryDateYear; }
//            public UsAgent getUsAgent() { return usAgent; }
//            public void setUsAgent(UsAgent usAgent) { this.usAgent = usAgent; }
//            public OwnerOperator getOwnerOperator() { return ownerOperator; }
//            public void setOwnerOperator(OwnerOperator ownerOperator) { this.ownerOperator = ownerOperator; }
//        }
//
//        /** us_agent 数据模型 */
//        public static class UsAgent {
//            @JsonProperty("name")
//            private String name;
//            @JsonProperty("business_name")
//            private String businessName;
//            @JsonProperty("bus_phone_area_code")
//            private String busPhoneAreaCode;
//            @JsonProperty("bus_phone_num")
//            private String busPhoneNum;
//            @JsonProperty("bus_phone_extn")
//            private String busPhoneExtn;
//            @JsonProperty("fax_area_code")
//            private String faxAreaCode;
//            @JsonProperty("fax_num")
//            private String faxNum;
//            @JsonProperty("email_address")
//            private String emailAddress;
//            @JsonProperty("address_line_1")
//            private String addressLine1;
//            @JsonProperty("address_line_2")
//            private String addressLine2;
//            @JsonProperty("city")
//            private String city;
//            @JsonProperty("state_code")
//            private String stateCode;
//            @JsonProperty("iso_country_code")
//            private String isoCountryCode;
//            @JsonProperty("zip_code")
//            private String zipCode;
//            @JsonProperty("postal_code")
//            private String postalCode;
//
//            // getters/setters
//            public String getName() { return name; }
//            public void setName(String name) { this.name = name; }
//            public String getBusinessName() { return businessName; }
//            public void setBusinessName(String businessName) { this.businessName = businessName; }
//            public String getBusPhoneAreaCode() { return busPhoneAreaCode; }
//            public void setBusPhoneAreaCode(String busPhoneAreaCode) { this.busPhoneAreaCode = busPhoneAreaCode; }
//            public String getBusPhoneNum() { return busPhoneNum; }
//            public void setBusPhoneNum(String busPhoneNum) { this.busPhoneNum = busPhoneNum; }
//            public String getBusPhoneExtn() { return busPhoneExtn; }
//            public void setBusPhoneExtn(String busPhoneExtn) { this.busPhoneExtn = busPhoneExtn; }
//            public String getFaxAreaCode() { return faxAreaCode; }
//            public void setFaxAreaCode(String faxAreaCode) { this.faxAreaCode = faxAreaCode; }
//            public String getFaxNum() { return faxNum; }
//            public void setFaxNum(String faxNum) { this.faxNum = faxNum; }
//            public String getEmailAddress() { return emailAddress; }
//            public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
//            public String getAddressLine1() { return addressLine1; }
//            public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
//            public String getAddressLine2() { return addressLine2; }
//            public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
//            public String getCity() { return city; }
//            public void setCity(String city) { this.city = city; }
//            public String getStateCode() { return stateCode; }
//            public void setStateCode(String stateCode) { this.stateCode = stateCode; }
//            public String getIsoCountryCode() { return isoCountryCode; }
//            public void setIsoCountryCode(String isoCountryCode) { this.isoCountryCode = isoCountryCode; }
//            public String getZipCode() { return zipCode; }
//            public void setZipCode(String zipCode) { this.zipCode = zipCode; }
//            public String getPostalCode() { return postalCode; }
//            public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
//        }
//
//        /** owner_operator 数据模型 */
//        public static class OwnerOperator {
//            @JsonProperty("firm_name")
//            private String firmName;
//            @JsonProperty("owner_operator_number")
//            private String ownerOperatorNumber;
//            @JsonProperty("official_correspondent")
//            private Map<String, Object> officialCorrespondent;
//            @JsonProperty("contact_address")
//            private ContactAddress contactAddress;
//
//            // getters/setters
//            public String getFirmName() { return firmName; }
//            public void setFirmName(String firmName) { this.firmName = firmName; }
//            public String getOwnerOperatorNumber() { return ownerOperatorNumber; }
//            public void setOwnerOperatorNumber(String ownerOperatorNumber) { this.ownerOperatorNumber = ownerOperatorNumber; }
//            public Map<String, Object> getOfficialCorrespondent() { return officialCorrespondent; }
//            public void setOfficialCorrespondent(Map<String, Object> officialCorrespondent) { this.officialCorrespondent = officialCorrespondent; }
//            public ContactAddress getContactAddress() { return contactAddress; }
//            public void setContactAddress(ContactAddress contactAddress) { this.contactAddress = contactAddress; }
//        }
//
//        /** owner_operator.contact_address 数据模型 */
//        public static class ContactAddress {
//            @JsonProperty("address_1")
//            private String address1;
//            @JsonProperty("address_2")
//            private String address2;
//            @JsonProperty("city")
//            private String city;
//            @JsonProperty("state_code")
//            private String stateCode;
//            @JsonProperty("state_province")
//            private String stateProvince;
//            @JsonProperty("iso_country_code")
//            private String isoCountryCode;
//            @JsonProperty("postal_code")
//            private String postalCode;
//
//            // getters/setters
//            public String getAddress1() { return address1; }
//            public void setAddress1(String address1) { this.address1 = address1; }
//            public String getAddress2() { return address2; }
//            public void setAddress2(String address2) { this.address2 = address2; }
//            public String getCity() { return city; }
//            public void setCity(String city) { this.city = city; }
//            public String getStateCode() { return stateCode; }
//            public void setStateCode(String stateCode) { this.stateCode = stateCode; }
//            public String getStateProvince() { return stateProvince; }
//            public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
//            public String getIsoCountryCode() { return isoCountryCode; }
//            public void setIsoCountryCode(String isoCountryCode) { this.isoCountryCode = isoCountryCode; }
//            public String getPostalCode() { return postalCode; }
//            public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
//        }
//
//        public static class Product {
//            @JsonProperty("product_code")
//            private String productCode;
//
//            @JsonProperty("created_date")
//            private String createdDate;
//
//            @JsonProperty("owner_operator_number")
//            private String ownerOperatorNumber;
//
//            @JsonProperty("exempt")
//            private String exempt;
//
//            @JsonProperty("openfda")
//            private OpenFDA openfda;
//
//            // Getters and Setters
//            public String getProductCode() { return productCode; }
//            public void setProductCode(String productCode) { this.productCode = productCode; }
//
//            public String getCreatedDate() { return createdDate; }
//            public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
//
//            public String getOwnerOperatorNumber() { return ownerOperatorNumber; }
//            public void setOwnerOperatorNumber(String ownerOperatorNumber) { this.ownerOperatorNumber = ownerOperatorNumber; }
//
//            public String getExempt() { return exempt; }
//            public void setExempt(String exempt) { this.exempt = exempt; }
//
//            public OpenFDA getOpenfda() { return openfda; }
//            public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
//        }
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
//            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
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
//     * 爬取设备注册信息数据（支持断点续传）
//     */
//    public List<DeviceRegistration> crawlDeviceRegistration(String searchTerm, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlDeviceRegistration(searchTerm, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 爬取设备注册信息数据（支持时间范围）
//     */
//    public List<DeviceRegistration> crawlDeviceRegistration(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        System.out.println("开始爬取设备注册信息数据...");
//
//        // 检查是否有断点可以继续
//        CrawlerCheckpoint checkpoint = getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
//
//        if (checkpoint.isCompleted()) {
//            System.out.println("爬取任务已完成，无需重复执行");
//            return new ArrayList<>();
//        }
//
//        List<DeviceRegistration> allResults = new ArrayList<>();
//        int skip = checkpoint.getCurrentSkip();
//        int totalFetched = checkpoint.getTotalFetched();
//
//        while (totalFetched < maxRecords) {
//            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
//            // openFDA 单次请求最大 1000 条
//            currentLimit = Math.min(currentLimit, 1000);
//
//            if (currentLimit <= 0) {
//                break;
//            }
//
//            System.out.printf("获取第 %d 页设备注册数据（偏移量: %d，数量: %d）%n",
//                    skip / batchSize + 1, skip, currentLimit);
//
//            Map<String, String> params = new HashMap<>();
//            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
//                params.put("search", searchTerm);
//            }
//            params.put("limit", String.valueOf(currentLimit));
//            params.put("skip", String.valueOf(skip));
//
//            // 时间范围过滤已移除
//
//            try {
//                Map<String, Object> data = fetchData("/device/registrationlisting.json", params);
//                List<Map<String, Object>> results = data != null ? (List<Map<String, Object>>) data.get("results") : new ArrayList<>();
//
//                if (results.isEmpty()) {
//                    System.out.println("没有更多设备注册数据，爬取结束");
//                    checkpoint.markCompleted();
//                    checkpointRepository.save(checkpoint);
//                    break;
//                }
//
//                // 转换为DeviceRegistration对象
//                for (Map<String, Object> result : results) {
//                    DeviceRegistration registration = objectMapper.convertValue(result, DeviceRegistration.class);
//                    // 新增：设置数据适用国家
//                    registration.setJdCountry("US");
//                    allResults.add(registration);
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
//        List<DeviceRegistration> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
//        System.out.printf("设备注册信息数据爬取完成，共 %d 条数据%n", finalResults.size());
//        return finalResults;
//    }
//
//    /**
//     * 获取或创建断点
//     */
//    private CrawlerCheckpoint getOrCreateCheckpoint(String searchTerm, int maxRecords, int batchSize) {
//        return getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, null, null);
//    }
//
//    private CrawlerCheckpoint getOrCreateCheckpoint(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
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
//     * 将API的注册模型转换为实体（扁平化模型）
//     */
//    private com.certification.entity.common.DeviceRegistrationRecord toEntity(DeviceRegistration src) {
//        if (src == null) return null;
//        MedicalDeviceManufacturerKey m = toManufacturerKey(src);
//        com.certification.entity.common.DeviceRegistrationRecord e = new com.certification.entity.common.DeviceRegistrationRecord();
//        e.setRegistrationNumber(m.getRegistrationNumber());
//        e.setFeiNumber(m.getFeiNumber());
//        e.setManufacturerName(m.getManufacturerName());
//        e.setStatusCode(m.getStatusCode());
//        e.setRegExpiryYear(m.getRegExpiryYear());
//        e.setEstablishmentType(m.getEstablishmentType());
//        e.setManufacturerFullAddress(m.getManufacturerFullAddress());
//        e.setManufacturerCountryCode(m.getManufacturerCountryCode());
//        e.setUsAgentBusinessName(m.getUsAgentBusinessName());
//        e.setUsAgentContactInfo(m.getUsAgentContactInfo());
//        e.setOwnerFirmName(m.getOwnerFirmName());
//        e.setOwnerFullAddress(m.getOwnerFullAddress());
//        e.setKNumber(m.getKNumber());
//        try { e.setProductCodes(m.getProductCodes()==null?null:objectMapper.writeValueAsString(m.getProductCodes())); } catch (JsonProcessingException ignore) { e.setProductCodes(null);}
//        try { e.setDeviceNames(m.getDeviceNames()==null?null:objectMapper.writeValueAsString(m.getDeviceNames())); } catch (JsonProcessingException ignore) { e.setDeviceNames(null);}
//        try { e.setDeviceClasses(m.getDeviceClasses()==null?null:objectMapper.writeValueAsString(m.getDeviceClasses())); } catch (JsonProcessingException ignore) { e.setDeviceClasses(null);}
//        try { e.setRegulationNumbers(m.getRegulationNumbers()==null?null:objectMapper.writeValueAsString(m.getRegulationNumbers())); } catch (JsonProcessingException ignore) { e.setRegulationNumbers(null);}
//        // 设置数据来源
//        e.setDataSource("FDA");
//        // 设置数据适用国家
//        e.setJdCountry("US");
//        return e;
//    }
//
//    /**
//     * 爬取并保存设备注册信息（支持分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, null, null);
//    }
//
//    /**
//     * 爬取并保存设备注册信息（支持时间范围和分批保存）
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize,
//                                                                                                        String dateFrom, String dateTo)
//            throws IOException, URISyntaxException, ParseException {
//        List<DeviceRegistration> list = crawlDeviceRegistration(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
//        List<com.certification.entity.common.DeviceRegistrationRecord> saved = new ArrayList<>();
//        List<com.certification.entity.common.DeviceRegistrationRecord> batchToSave = new ArrayList<>();
//
//        for (DeviceRegistration item : list) {
//            MedicalDeviceManufacturerKey m = toManufacturerKey(item);
//            // 去重：优先按注册编号，其次 FEI，再次 K 编号
//            boolean shouldSkip = false;
//
//            try {
//                if (m.getRegistrationNumber() != null && deviceRegistrationRecordRepository.findByRegistrationNumber(m.getRegistrationNumber()).isPresent()) {
//                    shouldSkip = true;
//                } else if (m.getRegistrationNumber() == null && m.getFeiNumber() != null && deviceRegistrationRecordRepository.findByFeiNumber(m.getFeiNumber()).isPresent()) {
//                    shouldSkip = true;
//                } else if (m.getRegistrationNumber() == null && m.getFeiNumber() == null && m.getKNumber() != null && deviceRegistrationRecordRepository.findByKNumber(m.getKNumber()).isPresent()) {
//                    shouldSkip = true;
//                }
//            } catch (Exception e) {
//                System.err.printf("去重检查失败，跳过该记录: %s%n", e.getMessage());
//                shouldSkip = true;
//            }
//
//            if (shouldSkip) {
//                continue;
//            }
//
//            com.certification.entity.common.DeviceRegistrationRecord entity = toEntity(item);
//            batchToSave.add(entity);
//
//            // 每50条数据保存一次
//            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
//                List<com.certification.entity.common.DeviceRegistrationRecord> batchSaved = deviceRegistrationRecordRepository.saveAll(batchToSave);
//                saved.addAll(batchSaved);
//                batchToSave.clear();
//                System.out.printf("已保存 %d 条设备注册信息数据%n", saved.size());
//            }
//        }
//
//        // 保存剩余的数据
//        if (!batchToSave.isEmpty()) {
//            List<com.certification.entity.common.DeviceRegistrationRecord> batchSaved = deviceRegistrationRecordRepository.saveAll(batchToSave);
//            saved.addAll(batchSaved);
//            System.out.printf("已保存剩余 %d 条设备注册信息数据%n", batchSaved.size());
//        }
//
//        return saved;
//    }
//
//    /**
//     * 按K编号爬取并保存
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveByKNumber(String kNumber, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (kNumber == null || kNumber.trim().isEmpty()) ? "" : "k_number:\"" + kNumber.trim() + "\"";
//        return crawlAndSaveDeviceRegistration(term, maxRecords, batchSize);
//    }
//
//    /**
//     * 按PMA编号爬取并保存
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveByPmaNumber(String pmaNumber, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (pmaNumber == null || pmaNumber.trim().isEmpty()) ? "" : "pma_number:\"" + pmaNumber.trim() + "\"";
//        return crawlAndSaveDeviceRegistration(term, maxRecords, batchSize);
//    }
//
//    /**
//     * 按产品代码爬取并保存（示例：products.product_code:HQY）
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveByProductCode(String productCode, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (productCode == null || productCode.trim().isEmpty()) ? "" : "products.product_code:" + productCode.trim();
//        return crawlAndSaveDeviceRegistration(term, maxRecords, batchSize);
//    }
//    /**
//     * 按设备名称爬取并保存
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveByDevicename(String deviceName, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (deviceName == null || deviceName.trim().isEmpty()) ? "" : "products.openfda.device_name:" + deviceName.trim();
//        return crawlAndSaveDeviceRegistration(term, maxRecords, batchSize);
//    }
//
//    /**
//     * 按专有名称爬取并保存
//     */
//    @Transactional
//    public List<com.certification.entity.common.DeviceRegistrationRecord> crawlAndSaveByProprietaryName(String proprietaryName, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (proprietaryName == null || proprietaryName.trim().isEmpty()) ? "" : "proprietary_name:" + proprietaryName.trim();
//        return crawlAndSaveDeviceRegistration(term, maxRecords, batchSize);
//    }
//
//
//    /**
//     * 以新的扁平模型返回制造商关键信息
//     */
//    public List<MedicalDeviceManufacturerKey> crawlManufacturerKeys(String searchTerm, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        List<DeviceRegistration> raw = crawlDeviceRegistration(searchTerm, maxRecords, batchSize);
//        List<MedicalDeviceManufacturerKey> out = new ArrayList<>();
//        for (DeviceRegistration r : raw) {
//            out.add(toManufacturerKey(r));
//        }
//        return out;
//    }
//
//    private MedicalDeviceManufacturerKey toManufacturerKey(DeviceRegistration src) {
//        MedicalDeviceManufacturerKey d = new MedicalDeviceManufacturerKey();
//        if (src == null) return d;
//        DeviceRegistration.Registration reg = src.getRegistration();
//        // 企业核心标识
//        if (reg != null) {
//            d.setRegistrationNumber(reg.getRegistrationNumber());
//            d.setFeiNumber(reg.getFeiNumber());
//            d.setManufacturerName(reg.getName());
//            d.setStatusCode(reg.getStatusCode());
//            d.setRegExpiryYear(reg.getRegExpiryDateYear());
//            d.setManufacturerCountryCode(reg.getIsoCountryCode());
//            d.setManufacturerFullAddress(joinNonEmpty(
//                    ", ",
//                    reg.getIsoCountryCode(),
//                    reg.getCity(),
//                    joinNonEmpty(" ", reg.getAddressLine1(), reg.getAddressLine2()),
//                    firstNonEmpty(reg.getPostalCode(), reg.getZipCode())
//            ));
//            // 美国代理
//            DeviceRegistration.UsAgent agent = reg.getUsAgent();
//            if (agent != null) {
//                d.setUsAgentBusinessName(agent.getBusinessName());
//                String phone = joinNonEmpty("-", agent.getBusPhoneAreaCode(), agent.getBusPhoneNum());
//                if (agent.getBusPhoneExtn() != null && !agent.getBusPhoneExtn().isEmpty()) {
//                    phone = phone + " x" + agent.getBusPhoneExtn();
//                }
//                String contact = joinNonEmpty(", ", phone, agent.getEmailAddress());
//                d.setUsAgentContactInfo(contact);
//            }
//            // 母公司
//            DeviceRegistration.OwnerOperator owner = reg.getOwnerOperator();
//            if (owner != null) {
//                d.setOwnerFirmName(owner.getFirmName());
//                DeviceRegistration.ContactAddress addr = owner.getContactAddress();
//                if (addr != null) {
//                    d.setOwnerFullAddress(joinNonEmpty(
//                            ", ",
//                            addr.getIsoCountryCode(),
//                            addr.getCity(),
//                            joinNonEmpty(" ", addr.getAddress1(), addr.getAddress2()),
//                            addr.getPostalCode()
//                    ));
//                }
//            }
//        }
//        // 企业类型（列表转字符串）
//        if (src.getEstablishmentType() != null && !src.getEstablishmentType().isEmpty()) {
//            d.setEstablishmentType(src.getEstablishmentType().stream().filter(Objects::nonNull).collect(Collectors.joining(", ")));
//        }
//        // 产品核心信息
//        d.setKNumber(src.getKNumber());
//        List<String> productCodes = new ArrayList<>();
//        List<String> deviceNames = new ArrayList<>();
//        List<String> deviceClasses = new ArrayList<>();
//        List<String> regulationNumbers = new ArrayList<>();
//        if (src.getProducts() != null) {
//            for (DeviceRegistration.Product p : src.getProducts()) {
//                if (p == null) continue;
//                if (p.getProductCode() != null) productCodes.add(p.getProductCode());
//                OpenFDA f = p.getOpenfda();
//                if (f != null) {
//                    if (f.getDeviceName() != null) deviceNames.add(f.getDeviceName());
//                    if (f.getDeviceClass() != null) deviceClasses.add(f.getDeviceClass());
//                    if (f.getRegulationNumber() != null) regulationNumbers.add(f.getRegulationNumber());
//                }
//            }
//        }
//        d.setProductCodes(distinctList(productCodes));
//        d.setDeviceNames(distinctList(deviceNames));
//        d.setDeviceClasses(distinctList(deviceClasses));
//        d.setRegulationNumbers(distinctList(regulationNumbers));
//        // 数据来源
//        d.setDataSource("FDA");
//        return d;
//    }
//
//    private List<String> distinctList(List<String> list) {
//        if (list == null) return null;
//        return list.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
//    }
//
//    private String firstNonEmpty(String a, String b) {
//        return (a != null && !a.isEmpty()) ? a : ((b != null && !b.isEmpty()) ? b : null);
//    }
//
//    private String joinNonEmpty(String sep, String... parts) {
//        StringBuilder sb = new StringBuilder();
//        for (String p : parts) {
//            if (p != null && !p.isEmpty()) {
//                if (sb.length() > 0) sb.append(sep);
//                sb.append(p);
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 根据设备名称爬取设备注册信息数据
//     */
//    public List<DeviceRegistration> crawlDeviceRegistrationByDeviceName(String deviceName, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (deviceName == null || deviceName.trim().isEmpty())
//                ? ""
//                : "products.openfda.device_name:\"" + deviceName.trim() + "\"";
//        System.out.println("按设备名称搜索: " + term);
//        return crawlDeviceRegistration(term, maxRecords, batchSize);
//    }
//
//    /**
//     * 根据专业描述关键词爬取设备注册信息数据
//     */
//    public List<DeviceRegistration> crawlDeviceRegistrationByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        String term = (specialtyKeywords == null || specialtyKeywords.trim().isEmpty())
//                ? ""
//                : "products.openfda.medical_specialty_description:\"" + specialtyKeywords.trim() + "\"";
//        System.out.println("按专业描述关键词搜索: " + term);
//        return crawlDeviceRegistration(term, maxRecords, batchSize);
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

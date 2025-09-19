package com.certification.crawler.countrydata.eu;

import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.entity.common.CrawlerCheckpoint;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * EU EUDAMED设备数据库爬虫（集成数据库版本）
 * 爬取EUDAMED设备数据库中的设备信息并保存到数据库
 * 参考D_registration.java的结构进行优化
 */
@Component
public class Eu_registration {
    
    private static final String BASE_URL = "https://ec.europa.eu/tools/eudamed/api/devices/udiDiData";
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5;
    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次
    
    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    
    @Autowired
    private CrawlerCheckpointRepository crawlerCheckpointRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    
    /**
     * 记录爬取信息到CrawlerCheckpoint表
     */
    private void recordCrawlerInfo(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo, 
                                   int totalFetched, int totalSaved, String status, String errorMessage) {
        try {
            CrawlerCheckpoint checkpoint = new CrawlerCheckpoint();
            checkpoint.setCrawlerType("Eu_registration");
            checkpoint.setSearchTerm(searchTerm);
            checkpoint.setDateFrom(dateFrom);
            checkpoint.setDateTo(dateTo);
            checkpoint.setCurrentSkip(0); // 不实现断点续传，设为0
            checkpoint.setTotalFetched(totalFetched);
            checkpoint.setTargetTotal(maxRecords == -1 ? null : maxRecords);
            checkpoint.setBatchSize(batchSize);
            checkpoint.setStatus(CrawlerCheckpoint.CrawlerStatus.valueOf(status));
            checkpoint.setErrorMessage(errorMessage);
            checkpoint.setLastUpdated(LocalDateTime.now());
            checkpoint.setCreatedTime(LocalDateTime.now());
            
            crawlerCheckpointRepository.save(checkpoint);
            System.out.println("爬取信息已记录到CrawlerCheckpoint表，ID: " + checkpoint.getId());
        } catch (Exception e) {
            System.err.println("记录爬取信息失败: " + e.getMessage());
        }
    }
    
    /**
     * EU API 响应数据结构
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EUResponse {
        @JsonProperty("content")
        private List<EUDAMEDDevice> content;
        
        @JsonProperty("totalElements")
        private long totalElements;
        
        @JsonProperty("totalPages")
        private int totalPages;
        
        @JsonProperty("size")
        private int size;
        
        @JsonProperty("number")
        private int number;
        
        @JsonProperty("pageable")
        private Object pageable; // 添加pageable字段以兼容API响应
        
        // Getters and setters
        public List<EUDAMEDDevice> getContent() { return content; }
        public void setContent(List<EUDAMEDDevice> content) { this.content = content; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public Object getPageable() { return pageable; }
        public void setPageable(Object pageable) { this.pageable = pageable; }
    }
    
    /**
     * EUDAMED 设备数据结构
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EUDAMEDDevice {
        @JsonProperty("uuid")
        private String uuid;
        
        @JsonProperty("versionNumber")
        private String versionNumber;
        
        @JsonProperty("basicUdi")
        private String basicUdi;
        
        @JsonProperty("tradeName")
        private String tradeName;
        
        @JsonProperty("manufacturerName")
        private String manufacturerName;
        
        @JsonProperty("manufacturerSrn")
        private String manufacturerSrn;
        
        @JsonProperty("riskClass")
        private RiskClassInfo riskClass;
        
        @JsonProperty("deviceStatusType")
        private StatusInfo deviceStatusType;
        
        @JsonProperty("manufacturerStatus")
        private StatusInfo manufacturerStatus;
        
        @JsonProperty("reference")
        private String reference;
        
        @JsonProperty("primaryDi")
        private String primaryDi;
        
        @JsonProperty("ulid")
        private String ulid;
        
        @JsonProperty("basicUdiDiDataUlid")
        private String basicUdiDiDataUlid;
        
        // Getters and setters
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getVersionNumber() { return versionNumber; }
        public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }
        public String getBasicUdi() { return basicUdi; }
        public void setBasicUdi(String basicUdi) { this.basicUdi = basicUdi; }
        public String getTradeName() { return tradeName; }
        public void setTradeName(String tradeName) { this.tradeName = tradeName; }
        public String getManufacturerName() { return manufacturerName; }
        public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
        public String getManufacturerSrn() { return manufacturerSrn; }
        public void setManufacturerSrn(String manufacturerSrn) { this.manufacturerSrn = manufacturerSrn; }
        public RiskClassInfo getRiskClass() { return riskClass; }
        public void setRiskClass(RiskClassInfo riskClass) { this.riskClass = riskClass; }
        public StatusInfo getDeviceStatusType() { return deviceStatusType; }
        public void setDeviceStatusType(StatusInfo deviceStatusType) { this.deviceStatusType = deviceStatusType; }
        public StatusInfo getManufacturerStatus() { return manufacturerStatus; }
        public void setManufacturerStatus(StatusInfo manufacturerStatus) { this.manufacturerStatus = manufacturerStatus; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getPrimaryDi() { return primaryDi; }
        public void setPrimaryDi(String primaryDi) { this.primaryDi = primaryDi; }
        public String getUlid() { return ulid; }
        public void setUlid(String ulid) { this.ulid = ulid; }
        public String getBasicUdiDiDataUlid() { return basicUdiDiDataUlid; }
        public void setBasicUdiDiDataUlid(String basicUdiDiDataUlid) { this.basicUdiDiDataUlid = basicUdiDiDataUlid; }
    }
    
    /**
     * 风险等级信息
     */
    public static class RiskClassInfo {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 状态信息
     */
    public static class StatusInfo {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 通用爬取方法（参考D_registration.java结构）
     */
    public String crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 通用爬取方法（支持时间范围）
     */
    public String crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (maxRecords == -1) {
            System.out.println("开始爬取EUDAMED设备注册信息，搜索词: " + searchTerm + ", 爬取模式: 所有数据, 批次大小: " + batchSize);
        } else {
            System.out.println("开始爬取EUDAMED设备注册信息，搜索词: " + searchTerm + ", 最大记录数: " + maxRecords + ", 批次大小: " + batchSize);
        }
        
        try {
            // 使用新的批次爬取和保存方法
            String result = crawlAndSaveDeviceRegistrationInBatches(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
            return result;
            
        } catch (Exception e) {
            System.err.println("爬取EUDAMED设备注册信息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 记录失败信息
            recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, 0, 0, "FAILED", e.getMessage());
            
            return "EUDAMED设备注册信息爬取失败: " + e.getMessage();
        }
    }
    
    /**
     * 批次爬取和保存设备注册信息（支持连续重复检测）
     */
    private String crawlAndSaveDeviceRegistrationInBatches(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        
        int totalFetched = 0;
        int totalSaved = 0;
        int currentPage = 0;
        long totalAvailable = 0;
        boolean crawlAll = (maxRecords == -1);
        int consecutiveEmptyBatches = 0;
        int batchCount = 0;
        
        while (crawlAll || totalFetched < maxRecords) {
            int currentLimit;
            if (crawlAll) {
                currentLimit = Math.min(batchSize, 25); // EU API每页最多25条
            } else {
                currentLimit = Math.min(batchSize, maxRecords - totalFetched);
                currentLimit = Math.min(currentLimit, 25);
            }

            if (currentLimit <= 0) {
                break;
            }

            batchCount++;
            System.out.printf("获取第 %d 批次EUDAMED设备注册数据（页码: %d，数量: %d）%n",
                    batchCount, currentPage, currentLimit);

            try {
                // 构建API URL
                String apiUrl = buildApiUrl(currentPage, currentLimit, searchTerm, null, null, null);
                System.out.println("请求URL: " + apiUrl);
                
                // 发送HTTP请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Accept", "application/json")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Cache-Control", "No-Cache")
                        .header("Content-Type", "application/json")
                        .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                        .header("Sec-Ch-Ua-Mobile", "?0")
                        .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                        .header("Sec-Fetch-Dest", "empty")
                        .header("Sec-Fetch-Mode", "cors")
                        .header("Sec-Fetch-Site", "same-origin")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", "https://ec.europa.eu/tools/eudamed/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                        .timeout(java.time.Duration.ofSeconds(30))
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    System.err.printf("请求失败，状态码: %d%n", response.statusCode());
                    break;
                }
                
                // 解析JSON响应
                EUResponse euResponse = objectMapper.readValue(response.body(), EUResponse.class);
                
                if (euResponse == null || euResponse.getContent() == null || euResponse.getContent().isEmpty()) {
                    System.out.println("没有更多EUDAMED设备注册数据，爬取结束");
                    break;
                }

                // 记录总数量信息（只在第一次获取时记录）
                if (totalAvailable == 0 && euResponse.getTotalElements() > 0) {
                    totalAvailable = euResponse.getTotalElements();
                    System.out.printf("API返回总匹配记录数: %d 条%n", totalAvailable);
                    
                    if (crawlAll && totalAvailable > 0) {
                        maxRecords = (int) totalAvailable;
                        crawlAll = false;
                        System.out.printf("开始爬取所有数据，目标数量: %d 条%n", maxRecords);
                    }
                }

                // 转换当前批次数据为实体
                List<DeviceRegistrationRecord> entities = new ArrayList<>();
                for (EUDAMEDDevice item : euResponse.getContent()) {
                    DeviceRegistrationRecord entity = convertToEntity(item);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }

                // 立即保存当前批次到数据库
                String saveResult = saveBatchToDatabase(entities);
                
                // 解析保存的记录数
                int savedCount = 0;
                try {
                    savedCount = Integer.parseInt(saveResult.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    savedCount = 0;
                }
                
                totalFetched += entities.size();
                totalSaved += savedCount;
                currentPage++;

                // 检查是否全部重复
                if (savedCount == 0 && !entities.isEmpty()) {
                    consecutiveEmptyBatches++;
                    System.out.println("第 " + batchCount + " 批次全部重复，连续空批次: " + consecutiveEmptyBatches);
                    
                    // 如果连续三次全部重复，停止爬取
                    if (consecutiveEmptyBatches >= 3) {
                        System.out.println("连续 " + consecutiveEmptyBatches + " 个批次都是重复数据，停止爬取");
                        break;
                    }
                } else {
                    consecutiveEmptyBatches = 0; // 重置连续空批次计数
                }

                // 显示当前进度
                if (totalAvailable > 0) {
                    System.out.printf("当前进度: %d/%d (%.1f%%)，已保存: %d 条%n", 
                            totalFetched, totalAvailable, (double) totalFetched / totalAvailable * 100, totalSaved);
                }

                // 检查是否已获取所有匹配记录
                if (totalAvailable > 0 && totalFetched >= totalAvailable) {
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
                // 如果是"未找到匹配记录"的错误，不打印堆栈跟踪
                if (!e.getMessage().contains("未找到匹配记录") && !e.getMessage().contains("No matches found")) {
                    e.printStackTrace();
                }
                break;
            }
        }

        // 记录爬取信息
        recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, totalFetched, totalSaved, "COMPLETED", null);
        
        return String.format("EUDAMED设备注册信息爬取完成，总共获取: %d 条记录，保存: %d 条记录", totalFetched, totalSaved);
    }
    
    /**
     * 将EUDAMED设备数据转换为DeviceRegistrationRecord实体
     */
    private DeviceRegistrationRecord convertToEntity(EUDAMEDDevice src) {
        if (src == null) return null;

        DeviceRegistrationRecord entity = new DeviceRegistrationRecord();

        // 设置数据源标识
        entity.setDataSource("EU_EUDAMED");
        entity.setJdCountry("EU");

        // 设置核心标识字段
        // UDI-DI 作为主要标识符
        entity.setRegistrationNumber(src.getUuid());
        
        // Basic UDI-DI 作为次要标识符
        if (src.getBasicUdi() != null && !src.getBasicUdi().trim().isEmpty()) {
            entity.setFeiNumber(src.getBasicUdi());
        }

        // 设置制造商信息
        if (src.getManufacturerName() != null && !src.getManufacturerName().trim().isEmpty()) {
            entity.setManufacturerName(src.getManufacturerName());
        }

        // 设置设备信息
        // Trade name 作为设备名称和专有名称
        if (src.getTradeName() != null && !src.getTradeName().trim().isEmpty()) {
            entity.setDeviceName(src.getTradeName());
            entity.setProprietaryName(src.getTradeName());
        }

        // 设置风险等级
        if (src.getRiskClass() != null && src.getRiskClass().getCode() != null) {
            entity.setRiskClass(src.getRiskClass().getCode());
        }

        // 设置状态信息
        if (src.getDeviceStatusType() != null && src.getDeviceStatusType().getCode() != null) {
            entity.setStatusCode(src.getDeviceStatusType().getCode());
        }

        // 设置创建日期（使用版本号作为创建时间标识）
        if (src.getVersionNumber() != null && !src.getVersionNumber().trim().isEmpty()) {
            entity.setCreatedDate(src.getVersionNumber());
        }

        // 设置元数据
        entity.setCrawlTime(LocalDateTime.now());

        return entity;
    }

    /**
     * 批量保存到数据库（参考D_registration.java的重复检查逻辑）
     */
    @Transactional
    private String saveBatchToDatabase(List<DeviceRegistrationRecord> records) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int duplicateCount = 0;
        int batchCount = 0;
        int consecutiveEmptyBatches = 0;

        for (int i = 0; i < records.size(); i += BATCH_SAVE_SIZE) {
            int endIndex = Math.min(i + BATCH_SAVE_SIZE, records.size());
            List<DeviceRegistrationRecord> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceRegistrationRecord> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (DeviceRegistrationRecord record : batch) {
                try {
                    // 检查是否已存在（使用registration_number、fei_number、device_name三个字段）
                    boolean isDuplicate = false;
                    
                    // 获取三个关键字段的值
                    String registrationNumber = record.getRegistrationNumber();
                    String feiNumber = record.getFeiNumber();
                    String deviceName = record.getDeviceName();
                    
                    // 重试机制：最多重试3次
                    int retryCount = 0;
                    int maxRetries = 3;
                    boolean querySuccess = false;
                    
                    while (retryCount < maxRetries && !querySuccess) {
                        try {
                            // 如果三个字段都有值，使用复合查询
                            if (registrationNumber != null && !registrationNumber.trim().isEmpty() &&
                                feiNumber != null && !feiNumber.trim().isEmpty() &&
                                deviceName != null && !deviceName.trim().isEmpty()) {
                                
                                List<DeviceRegistrationRecord> existingRecords = deviceRegistrationRecordRepository
                                        .findByRegistrationNumberAndFeiNumberAndDeviceName(registrationNumber, feiNumber, deviceName);
                                if (!existingRecords.isEmpty()) {
                                    isDuplicate = true;
                                }
                            } else {
                                // 如果某些字段为空，则分别检查非空字段
                                if (registrationNumber != null && !registrationNumber.trim().isEmpty()) {
                                    List<DeviceRegistrationRecord> existingByReg = deviceRegistrationRecordRepository
                                            .findByRegistrationNumber(registrationNumber);
                                    if (!existingByReg.isEmpty()) {
                                        isDuplicate = true;
                                    }
                                }
                                
                                if (!isDuplicate && feiNumber != null && !feiNumber.trim().isEmpty()) {
                                    List<DeviceRegistrationRecord> existingByFei = deviceRegistrationRecordRepository
                                            .findByFeiNumber(feiNumber);
                                    if (!existingByFei.isEmpty()) {
                                        isDuplicate = true;
                                    }
                                }
                            }
                            querySuccess = true;
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount >= maxRetries) {
                                System.err.println("重复检查失败，已重试" + maxRetries + "次，跳过该记录: " + e.getMessage());
                                // 连接问题，将记录标记为重复以避免数据冲突
                                isDuplicate = true;
                                break;
                            } else {
                                System.err.println("重复检查失败，第" + retryCount + "次重试: " + e.getMessage());
                                try {
                                    Thread.sleep(1000 * retryCount); // 递增延迟
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    }

                    if (isDuplicate) {
                        batchDuplicateCount++;
                        duplicateCount++;
                    } else {
                        newRecords.add(record);
                    }
                } catch (Exception e) {
                    System.err.println("重复检查过程中发生未知错误，跳过该记录: " + e.getMessage());
                    batchDuplicateCount++;
                    duplicateCount++;
                }
            }

            if (newRecords.isEmpty()) {
                System.out.println("本批次全部为重复记录，跳过了 " + batch.size() + " 条记录");
                consecutiveEmptyBatches++;
                System.out.println("第 " + batchCount + " 批次数据全部重复，连续空批次: " + consecutiveEmptyBatches);
                
                // 如果连续多个批次都是重复数据，停止爬取
                if (consecutiveEmptyBatches >= 3) {
                    System.out.println("连续 " + consecutiveEmptyBatches + " 个批次都是重复数据，停止爬取");
                    break;
                }
            } else {
                consecutiveEmptyBatches = 0;
                
                // 批次保存重试机制
                int saveRetryCount = 0;
                int maxSaveRetries = 3;
                boolean saveSuccess = false;
                
                while (saveRetryCount < maxSaveRetries && !saveSuccess) {
                    try {
                        deviceRegistrationRecordRepository.saveAll(newRecords);
                        savedCount += newRecords.size();
                        System.out.println("第 " + batchCount + " 批次保存成功，新增: " + newRecords.size() + " 条，重复: " + batchDuplicateCount + " 条");
                        saveSuccess = true;
                    } catch (Exception e) {
                        saveRetryCount++;
                        if (saveRetryCount >= maxSaveRetries) {
                            System.err.println("第 " + batchCount + " 批次保存失败，已重试" + maxSaveRetries + "次: " + e.getMessage());
                        } else {
                            System.err.println("第 " + batchCount + " 批次保存失败，第" + saveRetryCount + "次重试: " + e.getMessage());
                            try {
                                Thread.sleep(2000 * saveRetryCount); // 递增延迟
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
     * 构建API URL
     */
    public String buildApiUrl(int pageIndex, int pageSize, String searchKeyword, String deviceStatus, String sortField, String sortDirection) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?page=").append(pageIndex);
        url.append("&pageSize=").append(pageSize);
        url.append("&size=").append(pageSize);
        url.append("&iso2Code=en");
        
        // 排序参数
        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "primaryDi";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc";
        }
        url.append("&sort=").append(sortField).append(",").append(sortDirection);
        url.append("&sort=versionNumber,DESC");
        
        // 搜索关键词
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            try {
                String encodedKeyword = java.net.URLEncoder.encode(searchKeyword.trim(), "UTF-8");
                url.append("&name=").append(encodedKeyword);
            } catch (Exception e) {
                url.append("&name=").append(searchKeyword.trim());
            }
        }
        
        // 设备状态参数
        if (deviceStatus == null || deviceStatus.trim().isEmpty()) {
            deviceStatus = "refdata.device-model-status.on-the-market";
        }
        url.append("&deviceStatusCode=").append(deviceStatus);
        
        url.append("&languageIso2Code=en");
        
        return url.toString();
    }
    
    /**
     * 按Trade name搜索设备（统一方法命名）
     */
    public String crawlAndSaveByTradeName(String tradeName, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceRegistration(tradeName, maxRecords, batchSize);
    }
    
    /**
     * 按制造商名称搜索设备（统一方法命名）
     */
    public String crawlAndSaveByManufacturerName(String manufacturerName, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceRegistration(manufacturerName, maxRecords, batchSize);
    }
    
    /**
     * 按风险等级搜索设备（统一方法命名）
     */
    public String crawlAndSaveByRiskClass(String riskClass, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceRegistration(riskClass, maxRecords, batchSize);
    }
    
    /**
     * 基于关键词列表爬取设备注册信息（复杂策略）
     * 每个关键词将依次作为Trade name、制造商名称、风险等级进行搜索
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认搜索");
            return crawlAndSaveDeviceRegistration("medical device", maxRecords, batchSize, dateFrom, dateTo);
        }

        System.out.println("开始基于关键词列表爬取EUDAMED设备注册信息...");
        System.out.println("关键词数量: " + inputKeywords.size());
        System.out.println("搜索策略: 每个关键词将依次作为Trade name、制造商名称、风险等级进行搜索");
        System.out.println("日期范围: " + dateFrom + " - " + dateTo);
        System.out.println("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));

        int totalSaved = 0;
        int totalFetched = 0;
        int keywordsProcessed = 0;

        // 遍历每个关键词进行搜索
        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            keyword = keyword.trim();
            System.out.println("正在搜索关键词: " + keyword);
            keywordsProcessed++;

            try {
                // 1. 使用关键词作为Trade name进行搜索
                System.out.println("关键词 '" + keyword + "' 作为Trade name搜索");
                String tradeNameResult = crawlAndSaveByTradeName(keyword, maxRecords, batchSize);
                System.out.println("Trade name搜索结果: " + tradeNameResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 2. 使用关键词作为制造商名称进行搜索
                System.out.println("关键词 '" + keyword + "' 作为制造商名称搜索");
                String manufacturerResult = crawlAndSaveByManufacturerName(keyword, maxRecords, batchSize);
                System.out.println("制造商名称搜索结果: " + manufacturerResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 3. 使用关键词作为风险等级进行搜索
                System.out.println("关键词 '" + keyword + "' 作为风险等级搜索");
                String riskClassResult = crawlAndSaveByRiskClass(keyword, maxRecords, batchSize);
                System.out.println("风险等级搜索结果: " + riskClassResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            } catch (Exception e) {
                System.err.println("关键词 '" + keyword + "' 搜索失败: " + e.getMessage());
                // 如果是"未找到匹配记录"的错误，不打印堆栈跟踪
                if (!e.getMessage().contains("未找到匹配记录") && !e.getMessage().contains("No matches found")) {
                    e.printStackTrace();
                }
            }
        }

        String result = String.format("关键词列表爬取完成，处理关键词数: %d, 总保存记录数: %d", keywordsProcessed, totalSaved);
        System.out.println(result);
        
        // 记录关键词爬取信息
        String keywordsStr = inputKeywords != null ? String.join(",", inputKeywords) : "";
        recordCrawlerInfo("关键词搜索: " + keywordsStr, maxRecords, batchSize, dateFrom, dateTo, totalFetched, totalSaved, "COMPLETED", null);
        
        return result;
    }
    
    /**
     * 基于关键词列表爬取设备注册信息（简化版本，无时间范围）
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize) {
        return crawlAndSaveWithKeywords(inputKeywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        System.out.println("=== EU EUDAMED 爬虫测试 ===");
        System.out.println("注意：此测试需要Spring Boot环境支持");
        System.out.println("请在Spring Boot应用中调用相关方法进行测试");
        
        // 测试用例说明
        System.out.println("\n可用的测试方法：");
        System.out.println("1. crawlAndSaveByTradeName(\"visia\", 10, 5) - 按Trade name搜索");
        System.out.println("2. crawlAndSaveByManufacturerName(\"Medtronic\", 10, 5) - 按制造商名称搜索");
        System.out.println("3. crawlAndSaveByRiskClass(\"Class III\", 10, 5) - 按风险等级搜索");
        System.out.println("4. crawlAndSaveDeviceRegistration(\"medical device\", 10, 5) - 通用搜索");
        System.out.println("5. crawlAndSaveWithKeywords(Arrays.asList(\"visia\", \"medtronic\"), 10, 5) - 关键词列表搜索");
        
        System.out.println("\n=== 测试完成 ===");
    }
}
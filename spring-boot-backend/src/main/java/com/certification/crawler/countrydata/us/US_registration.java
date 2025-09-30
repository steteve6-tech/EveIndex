package com.certification.crawler.countrydata.us;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.entity.common.CrawlerCheckpoint;
import com.certification.exception.AllDataDuplicateException;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * FDA设备注册信息爬取器
 * 专门用于爬取设备注册信息数据并保存到数据库
 */
@Component
public class US_registration {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    
    @Autowired
    private CrawlerCheckpointRepository crawlerCheckpointRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;

    public US_registration() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * FDA API响应模型
     */
    public static class FDAResponse {
        @JsonProperty("results")
        private List<DeviceRegistration> results;
        
        @JsonProperty("meta")
        private Meta meta;

        public List<DeviceRegistration> getResults() { return results; }
        public void setResults(List<DeviceRegistration> results) { this.results = results; }
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
     * 设备注册信息模型
     */
    public static class DeviceRegistration {
        @JsonProperty("proprietary_name")
        private List<String> proprietaryName;

        @JsonProperty("establishment_type")
        private List<String> establishmentType;

        @JsonProperty("registration")
        private Registration registration;

        @JsonProperty("pma_number")
        private String pmaNumber;

        @JsonProperty("k_number")
        private String kNumber;

        @JsonProperty("products")
        private List<Product> products;

        // Getters and Setters
        public List<String> getProprietaryName() { return proprietaryName; }
        public void setProprietaryName(List<String> proprietaryName) { this.proprietaryName = proprietaryName; }

        public List<String> getEstablishmentType() { return establishmentType; }
        public void setEstablishmentType(List<String> establishmentType) { this.establishmentType = establishmentType; }

        public Registration getRegistration() { return registration; }
        public void setRegistration(Registration registration) { this.registration = registration; }

        public String getPmaNumber() { return pmaNumber; }
        public void setPmaNumber(String pmaNumber) { this.pmaNumber = pmaNumber; }

        public String getKNumber() { return kNumber; }
        public void setKNumber(String kNumber) { this.kNumber = kNumber; }

        public List<Product> getProducts() { return products; }
        public void setProducts(List<Product> products) { this.products = products; }
    }

    /**
     * 注册信息模型
     */
    public static class Registration {
        @JsonProperty("registration_number")
        private String registrationNumber;

        @JsonProperty("fei_number")
        private String feiNumber;

        @JsonProperty("name")
        private String name;

        @JsonProperty("status_code")
        private String statusCode;

        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

        public String getFeiNumber() { return feiNumber; }
        public void setFeiNumber(String feiNumber) { this.feiNumber = feiNumber; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    }

    /**
     * 产品信息模型
     */
    public static class Product {
        @JsonProperty("product_code")
        private String productCode;

        @JsonProperty("created_date")
        private String createdDate;

        @JsonProperty("openfda")
        private OpenFDA openfda;

        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }

        public String getCreatedDate() { return createdDate; }
        public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

        public OpenFDA getOpenfda() { return openfda; }
        public void setOpenfda(OpenFDA openfda) { this.openfda = openfda; }
    }

    /**
     * OpenFDA信息模型
     */
    public static class OpenFDA {
        @JsonProperty("device_name")
        private Object deviceName;

        @JsonProperty("device_class")
        private Object deviceClass;

        public Object getDeviceName() { return deviceName; }
        public void setDeviceName(Object deviceName) { this.deviceName = deviceName; }

        public Object getDeviceClass() { return deviceClass; }
        public void setDeviceClass(Object deviceClass) { this.deviceClass = deviceClass; }
        
        // 辅助方法：获取设备名称字符串
        public String getDeviceNameAsString() {
            if (deviceName == null) return null;
            if (deviceName instanceof String) {
                return (String) deviceName;
            } else if (deviceName instanceof List) {
                List<?> list = (List<?>) deviceName;
                return list.isEmpty() ? null : list.get(0).toString();
            }
            return deviceName.toString();
        }
        
        // 辅助方法：获取设备类别字符串
        public String getDeviceClassAsString() {
            if (deviceClass == null) return null;
            if (deviceClass instanceof String) {
                return (String) deviceClass;
            } else if (deviceClass instanceof List) {
                List<?> list = (List<?>) deviceClass;
                return list.isEmpty() ? null : list.get(0).toString();
            }
            return deviceClass.toString();
        }
    }

    /**
     * 记录爬取信息到CrawlerCheckpoint表
     */
    private void recordCrawlerInfo(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo, 
                                   int totalFetched, int totalSaved, String status, String errorMessage) {
        try {
            CrawlerCheckpoint checkpoint = new CrawlerCheckpoint();
            checkpoint.setCrawlerType("US_registration");
            checkpoint.setSearchTerm(searchTerm);
            checkpoint.setDateFrom(dateFrom);
            checkpoint.setDateTo(dateTo);
            checkpoint.setCurrentSkip(0); // 不实现断点续传，设为0
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
    public String crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize) {
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, null, null);
    }


    /**
     * 通用爬取方法（支持时间范围）
     */
    public String crawlAndSaveDeviceRegistration(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (maxRecords == -1) {
            System.out.println("开始爬取设备注册信息，搜索词: " + searchTerm + ", 爬取模式: 所有数据, 批次大小: " + batchSize);
        } else {
            System.out.println("开始爬取设备注册信息，搜索词: " + searchTerm + ", 最大记录数: " + maxRecords + ", 批次大小: " + batchSize);
        }
        
        try {
            // 使用新的批次爬取和保存方法
            String result = crawlAndSaveDeviceRegistrationInBatches(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
            return result;
            
        } catch (Exception e) {
            System.err.println("爬取设备注册信息失败: " + e.getMessage());
            e.printStackTrace();
            
            // 记录失败信息
            recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, 0, 0, "FAILED", e.getMessage());
            
            return "设备注册信息爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按专有名称搜索
     */
    public String crawlAndSaveByProprietaryName(String proprietaryName, int maxRecords, int batchSize) {
        String searchTerm = "proprietary_name:" + proprietaryName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按专有名称搜索（支持时间范围）
     */
    public String crawlAndSaveByProprietaryName(String proprietaryName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "proprietary_name:" + proprietaryName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按制造商名称搜索
     */
    public String crawlAndSaveByManufacturerName(String manufacturerName, int maxRecords, int batchSize) {
        String searchTerm = "registration.name:" + manufacturerName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按制造商名称搜索（支持时间范围）
     */
    public String crawlAndSaveByManufacturerName(String manufacturerName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "registration.name:" + manufacturerName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按设备名称搜索
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize) {
        String searchTerm = "products.openfda.device_name:" + deviceName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按设备名称搜索（支持时间范围）
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "products.openfda.device_name:" + deviceName;
        return crawlAndSaveDeviceRegistration(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 基于关键词列表爬取设备注册信息（复杂策略）
     * 每个关键词将依次作为专有名称、制造商名称、设备名称进行搜索
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认搜索");
            return crawlAndSaveDeviceRegistration("device_name:medical", maxRecords, batchSize, dateFrom, dateTo);
        }

        System.out.println("开始基于关键词列表爬取设备注册信息...");
        System.out.println("关键词数量: " + inputKeywords.size());
        System.out.println("搜索策略: 每个关键词将依次作为专有名称、制造商名称、设备名称进行搜索");
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
                // 1. 使用关键词作为专有名称进行搜索
                System.out.println("关键词 '" + keyword + "' 作为专有名称搜索");
                String proprietaryResult = crawlAndSaveByProprietaryName(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("专有名称搜索结果: " + proprietaryResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 2. 使用关键词作为制造商名称进行搜索
                System.out.println("关键词 '" + keyword + "' 作为制造商名称搜索");
                String manufacturerResult = crawlAndSaveByManufacturerName(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("制造商名称搜索结果: " + manufacturerResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 3. 使用关键词作为设备名称进行搜索
                System.out.println("关键词 '" + keyword + "' 作为设备名称搜索");
                String deviceNameResult = crawlAndSaveByDeviceName(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("设备名称搜索结果: " + deviceNameResult);

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
     * 爬取设备注册信息数据
     */
    public List<DeviceRegistration> crawlDeviceRegistration(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceRegistration(searchTerm, maxRecords, batchSize, null, null);
    }

    /**
     * 爬取设备注册信息数据（支持时间范围）
     */
    public List<DeviceRegistration> crawlDeviceRegistration(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备注册信息数据...");

        List<DeviceRegistration> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;
        Integer totalAvailable = null; // 记录API返回的总数量
        boolean crawlAll = (maxRecords == -1); // 是否爬取所有数据

        while (crawlAll || totalFetched < maxRecords) {
            int currentLimit;
            if (crawlAll) {
                // 爬取所有数据时，使用批次大小作为限制
                currentLimit = Math.min(batchSize, 1000);
            } else {
                // 限制数量时，计算当前批次大小
                currentLimit = Math.min(batchSize, maxRecords - totalFetched);
                currentLimit = Math.min(currentLimit, 1000);
            }

            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备注册数据（偏移量: %d，数量: %d）%n",
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                params.put("search", searchTerm);
            }
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            try {
                FDAResponse response = fetchData("/device/registrationlisting.json", params);
                List<DeviceRegistration> results = response != null ? response.getResults() : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备注册数据，爬取结束");
                    break;
                }

                // 记录总数量信息（只在第一次获取时记录）
                if (totalAvailable == null && response != null && response.getMeta() != null && response.getMeta().getResults() != null) {
                    totalAvailable = response.getMeta().getResults().getTotal();
                    System.out.printf("API返回总匹配记录数: %d 条%n", totalAvailable);
                    
                    // 如果是要爬取所有数据，更新maxRecords为目标数量
                    if (crawlAll && totalAvailable != null) {
                        maxRecords = totalAvailable;
                        crawlAll = false; // 现在有了具体的目标数量
                        System.out.printf("开始爬取所有数据，目标数量: %d 条%n", maxRecords);
                    }
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

        List<DeviceRegistration> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备注册信息数据爬取完成，共 %d 条数据%n", finalResults.size());
        return finalResults;
    }

    /**
     * 将API的注册模型转换为实体
     */
    private DeviceRegistrationRecord convertToEntity(DeviceRegistration src) {
        if (src == null) return null;

        DeviceRegistrationRecord entity = new DeviceRegistrationRecord();

        // 设置注册编号（使用registration.registration_number）
        String registrationNumber = null;
        if (src.getRegistration() != null && src.getRegistration().getRegistrationNumber() != null) {
            registrationNumber = src.getRegistration().getRegistrationNumber();
        }
        entity.setRegistrationNumber(registrationNumber);

        // 设置FEI编号
        if (src.getRegistration() != null && src.getRegistration().getFeiNumber() != null) {
            entity.setFeiNumber(src.getRegistration().getFeiNumber());
        }

        // 设置制造商名称
        if (src.getRegistration() != null && src.getRegistration().getName() != null) {
            entity.setManufacturerName(src.getRegistration().getName());
        }

        // 设置专有名称（如果有多个，用分号连接）
        if (src.getProprietaryName() != null && !src.getProprietaryName().isEmpty()) {
            if (src.getProprietaryName().size() == 1) {
                entity.setProprietaryName(src.getProprietaryName().get(0));
            } else {
                // 多个专有名称用分号连接
                String allProprietaryNames = String.join("; ", src.getProprietaryName());
                entity.setProprietaryName(allProprietaryNames);
//                System.out.println("发现多个专有名称，已合并: " + allProprietaryNames);
            }
        }

        // 设置设备名称
        if (src.getProducts() != null && !src.getProducts().isEmpty()) {
            Product firstProduct = src.getProducts().get(0);
            if (firstProduct.getOpenfda() != null) {
                String deviceName = firstProduct.getOpenfda().getDeviceNameAsString();
                if (deviceName != null && !deviceName.trim().isEmpty()) {
                    // 检查设备名称长度，如果过长则记录警告但不截断
                    if (deviceName.length() > 500) {
                        System.out.println("警告：设备名称过长（" + deviceName.length() + "字符），可能影响数据库存储: " + deviceName.substring(0, 100) + "...");
                    }
                    entity.setDeviceName(deviceName);
                }
            }
        }

        // 设置设备类别
        if (src.getProducts() != null && !src.getProducts().isEmpty()) {
            Product firstProduct = src.getProducts().get(0);
            if (firstProduct.getOpenfda() != null) {
                String deviceClass = firstProduct.getOpenfda().getDeviceClassAsString();
                if (deviceClass != null && !deviceClass.trim().isEmpty()) {
                    // 检查设备类别长度，如果过长则记录警告但不截断
                    if (deviceClass.length() > 100) {
                        System.out.println("警告：设备类别过长（" + deviceClass.length() + "字符），可能影响数据库存储: " + deviceClass.substring(0, 50) + "...");
                    }
                    entity.setDeviceClass(deviceClass);
                }
            }
        }

        // 设置状态码
        if (src.getRegistration() != null && src.getRegistration().getStatusCode() != null) {
            entity.setStatusCode(src.getRegistration().getStatusCode());
        }

        // 设置创建日期（从products中获取第一个产品的created_date）
        if (src.getProducts() != null && !src.getProducts().isEmpty()) {
            Product firstProduct = src.getProducts().get(0);
            if (firstProduct.getCreatedDate() != null && !firstProduct.getCreatedDate().trim().isEmpty()) {
                entity.setCreatedDate(firstProduct.getCreatedDate());
            }
        }

        // 设置数据源和国家
        entity.setDataSource("US_FDA");
        entity.setJdCountry("US");

        return entity;
    }

    /**
     * 批次爬取和保存设备注册信息（支持连续重复检测）
     */
    private String crawlAndSaveDeviceRegistrationInBatches(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        
        int totalFetched = 0;
        int totalSaved = 0;
        int currentSkip = 0;
        long totalAvailable = 0;
        boolean crawlAll = (maxRecords == -1);
        int consecutiveEmptyBatches = 0;
        int batchCount = 0;
        
        while (crawlAll || totalFetched < maxRecords) {
            int currentLimit;
            if (crawlAll) {
                currentLimit = Math.min(batchSize, 100); // FDA API每页最多100条
            } else {
                currentLimit = Math.min(batchSize, maxRecords - totalFetched);
                currentLimit = Math.min(currentLimit, 100);
            }

            if (currentLimit <= 0) {
                break;
            }

            batchCount++;
            System.out.printf("获取第 %d 批次FDA设备注册数据（跳过: %d，数量: %d）%n",
                    batchCount, currentSkip, currentLimit);

            try {
                // 构建请求参数
                Map<String, String> params = new HashMap<>();
                params.put("limit", String.valueOf(currentLimit));
                params.put("skip", String.valueOf(currentSkip));
                
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    params.put("search", searchTerm.trim());
                }
                
                if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                    params.put("date_received", "[" + dateFrom + "+TO+" + (dateTo != null ? dateTo : "now") + "]");
                }
                
                // 使用正确的API端点获取设备注册数据
                FDAResponse fdaResponse = fetchData("/device/registrationlisting.json", params);
                
                if (fdaResponse == null || fdaResponse.getResults() == null || fdaResponse.getResults().isEmpty()) {
                    System.out.println("没有更多FDA设备注册数据，爬取结束");
                    break;
                }

                // 记录总数量信息（只在第一次获取时记录）
                if (totalAvailable == 0 && fdaResponse.getMeta() != null && 
                    fdaResponse.getMeta().getResults() != null && fdaResponse.getMeta().getResults().getTotal() != null) {
                    totalAvailable = fdaResponse.getMeta().getResults().getTotal();
                    System.out.printf("API返回总匹配记录数: %d 条%n", totalAvailable);
                    
                    if (crawlAll && totalAvailable > 0) {
                        maxRecords = (int) totalAvailable;
                        crawlAll = false;
                        System.out.printf("开始爬取所有数据，目标数量: %d 条%n", maxRecords);
                    }
                }

                // 转换当前批次数据为实体
                List<DeviceRegistrationRecord> entities = new ArrayList<>();
                for (DeviceRegistration item : fdaResponse.getResults()) {
                    DeviceRegistrationRecord record = convertToEntity(item);
                    if (record != null) {
                        entities.add(record);
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
                currentSkip += currentLimit;

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
        
        return String.format("FDA设备注册信息爬取完成，总共获取: %d 条记录，保存: %d 条记录", totalFetched, totalSaved);
    }

    /**
     * 批量保存到数据库
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

        for (int i = 0; i < records.size(); i += crawlerConfig.getBatch().getSaveSize()) {
            int endIndex = Math.min(i + crawlerConfig.getBatch().getSaveSize(), records.size());
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

        return String.format("保存成功: %d 条新记录, 跳过重复: %d 条", savedCount, duplicateCount);
    }

    /**
     * 获取数据
     */
    private FDAResponse fetchData(String endpoint, Map<String, String> params) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(BASE_URL + endpoint);
        uriBuilder.addParameter("api_key", API_KEY);

        // 然后添加其他请求参数
        params.forEach(uriBuilder::addParameter);

        String requestUrl = uriBuilder.build().toString();
        System.out.println("请求URL: " + requestUrl);

        HttpGet httpGet = new HttpGet(uriBuilder.build());

        for (int attempt = 1; attempt <= crawlerConfig.getRetry().getMaxAttempts(); attempt++) {
            try (ClassicHttpResponse response = httpClient.executeOpen(null, httpGet, null)) {
                int statusCode = response.getCode();
                String reasonPhrase = response.getReasonPhrase();

                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                        
                        // 检查是否包含错误信息
                        if (json.contains("\"error\"")) {
                            try {
                                // 尝试解析错误响应
                                com.fasterxml.jackson.databind.JsonNode errorNode = objectMapper.readTree(json);
                                if (errorNode.has("error")) {
                                    com.fasterxml.jackson.databind.JsonNode error = errorNode.get("error");
                                    String errorCode = error.has("code") ? error.get("code").asText() : "UNKNOWN";
                                    String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
                                    
                                    if ("NOT_FOUND".equals(errorCode) && "No matches found!".equals(errorMessage)) {
                                        System.out.println("API返回：未找到匹配记录 - " + errorMessage);
                                        // 返回空的响应而不是抛出异常
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

}
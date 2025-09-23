package com.certification.crawler.countrydata.us;

import com.certification.entity.common.Device510K;
import com.certification.entity.common.CrawlerCheckpoint;
import com.certification.repository.common.Device510KRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * FDA 510K设备爬取器
 * 专门用于爬取FDA 510K设备数据并保存到数据库
 */
@Component
public class US_510K {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5;
    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private Device510KRepository device510KRepository;
    
    @Autowired
    private CrawlerCheckpointRepository crawlerCheckpointRepository;

    public US_510K() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * FDA API响应模型
     */
    public static class FDAResponse {
        @JsonProperty("results")
        private List<Device510KData> results;
        
        @JsonProperty("meta")
        private Meta meta;

        public List<Device510KData> getResults() { return results; }
        public void setResults(List<Device510KData> results) { this.results = results; }
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
     * FDA 510K数据模型
     */
    public static class Device510KData {
        @JsonProperty("applicant")
        private String applicant;

        @JsonProperty("country_code")
        private String countryCode;

        @JsonProperty("date_received")
        private String dateReceived;

        @JsonProperty("device_name")
        private String deviceName;

        @JsonProperty("k_number")
        private String kNumber;

        @JsonProperty("openfda")
        private OpenFDA openfda;

        // Getters and Setters
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getDateReceived() { return dateReceived; }
        public void setDateReceived(String dateReceived) { this.dateReceived = dateReceived; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getKNumber() { return kNumber; }
        public void setKNumber(String kNumber) { this.kNumber = kNumber; }

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
            checkpoint.setCrawlerType("US_510K");
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
    public String crawlAndSaveDevice510K(String searchTerm, int maxRecords, int batchSize) {
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, null, null);
    }

    /**
     * 通用爬取方法（支持时间范围）
     */
    public String crawlAndSaveDevice510K(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (maxRecords == -1) {
            System.out.println("开始爬取FDA 510K设备数据，搜索词: " + searchTerm + ", 爬取模式: 所有数据, 批次大小: " + batchSize);
        } else {
            System.out.println("开始爬取FDA 510K设备数据，搜索词: " + searchTerm + ", 最大记录数: " + maxRecords + ", 批次大小: " + batchSize);
        }
        
        try {
            String result = crawlAndSaveDevice510KInBatches(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
            return result;
            
        } catch (Exception e) {
            System.err.println("爬取FDA 510K设备数据失败: " + e.getMessage());
            e.printStackTrace();
            
            recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, 0, 0, "FAILED", e.getMessage());
            
            return "FDA 510K设备数据爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按设备名称搜索
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize) {
        String searchTerm = "device_name:" + deviceName;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按设备名称搜索（支持时间范围）
     */
    public String crawlAndSaveByDeviceName(String deviceName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "device_name:" + deviceName;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按申请人搜索
     */
    public String crawlAndSaveByApplicant(String applicant, int maxRecords, int batchSize) {
        String searchTerm = "applicant:" + applicant;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按申请人搜索（支持时间范围）
     */
    public String crawlAndSaveByApplicant(String applicant, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "applicant:" + applicant;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 按trade_name搜索（使用openfda.device_name字段）
     */
    public String crawlAndSaveByTradeName(String tradeName, int maxRecords, int batchSize) {
        String searchTerm = "openfda.device_name:" + tradeName;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize);
    }

    /**
     * 按trade_name搜索（支持时间范围）
     */
    public String crawlAndSaveByTradeName(String tradeName, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        String searchTerm = "openfda.device_name:" + tradeName;
        return crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 基于关键词列表爬取FDA 510K设备数据
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认搜索");
            return crawlAndSaveDevice510K("device_name:medical", maxRecords, batchSize, dateFrom, dateTo);
        }

        System.out.println("开始基于关键词列表爬取FDA 510K设备数据...");
        System.out.println("关键词数量: " + inputKeywords.size());
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
                // 1. 使用关键词作为设备名称进行搜索
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

                // 2. 使用关键词作为申请人进行搜索
                System.out.println("关键词 '" + keyword + "' 作为申请人搜索");
                String applicantResult = crawlAndSaveByApplicant(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("申请人搜索结果: " + applicantResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 3. 使用关键词作为trade_name进行搜索
                System.out.println("关键词 '" + keyword + "' 作为trade_name搜索");
                String tradeNameResult = crawlAndSaveByTradeName(keyword, maxRecords, batchSize, dateFrom, dateTo);
                System.out.println("trade_name搜索结果: " + tradeNameResult);

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
            } catch (Exception e) {
                System.err.println("关键词 '" + keyword + "' 搜索失败: " + e.getMessage());
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
     * 基于关键词列表爬取FDA 510K设备数据（简化版本，无时间范围）
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize) {
        return crawlAndSaveWithKeywords(inputKeywords, maxRecords, batchSize, null, null);
    }

    /**
     * 爬取FDA 510K设备数据
     */
    public List<Device510KData> crawlDevice510K(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDevice510K(searchTerm, maxRecords, batchSize, null, null);
    }

    /**
     * 爬取FDA 510K设备数据（支持时间范围）
     */
    public List<Device510KData> crawlDevice510K(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取FDA 510K设备数据...");

        List<Device510KData> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;
        Integer totalAvailable = null;
        boolean crawlAll = (maxRecords == -1);

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

            System.out.printf("获取第 %d 页FDA 510K设备数据（偏移量: %d，数量: %d）%n",
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                params.put("search", searchTerm);
            }
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
                FDAResponse response = fetchData("/device/510k.json", params);
                List<Device510KData> results = response != null ? response.getResults() : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多FDA 510K设备数据，爬取结束");
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

        List<Device510KData> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("FDA 510K设备数据爬取完成，共 %d 条数据%n", finalResults.size());
        return finalResults;
    }

    /**
     * 将API的510K模型转换为实体
     */
    private Device510K convertToEntity(Device510KData src) {
        if (src == null) return null;

        Device510K entity = new Device510K();

        // 设置基本信息
        entity.setDeviceName(truncateString(src.getDeviceName(), 255));
        entity.setApplicant(truncateString(src.getApplicant(), 255));
        entity.setKNumber(truncateString(src.getKNumber(), 32));
        entity.setCountryCode(src.getCountryCode());
        entity.setDateReceived(parseDate(src.getDateReceived()));
        
        // 设置设备类别和trade_name
        if (src.getOpenfda() != null) {
            entity.setDeviceClass(truncateString(src.getOpenfda().getDeviceClassAsString(), 10));
            // 将openfda.device_name作为trade_name
            entity.setTradeName(truncateString(src.getOpenfda().getDeviceNameAsString(), 255));
        }

        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setDataSource("FDA_510K");
        entity.setJdCountry("US");
        entity.setCrawlTime(LocalDateTime.now());
        entity.setDataStatus("ACTIVE");

        return entity;
    }

    /**
     * 批次爬取和保存FDA 510K设备数据（支持连续重复检测）
     */
    private String crawlAndSaveDevice510KInBatches(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        
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
            System.out.printf("获取第 %d 批次FDA 510K设备数据（跳过: %d，数量: %d）%n",
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
                    String dateRange = String.format("date_received:[%s TO %s]", dateFrom, dateTo != null ? dateTo : "now");
                    String currentSearch = searchTerm != null && !searchTerm.isEmpty() ? 
                        searchTerm + " AND " + dateRange : dateRange;
                    params.put("search", currentSearch);
                }
                
                // 使用现有的fetchData方法
                FDAResponse fdaResponse = fetchData("/device/510k.json", params);
                
                if (fdaResponse == null || fdaResponse.getResults() == null || fdaResponse.getResults().isEmpty()) {
                    System.out.println("没有更多FDA 510K设备数据，爬取结束");
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
                List<Device510K> entities = new ArrayList<>();
                for (Device510KData item : fdaResponse.getResults()) {
                    Device510K record = convertToEntity(item);
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
                if (!e.getMessage().contains("未找到匹配记录") && !e.getMessage().contains("No matches found")) {
                    e.printStackTrace();
                }
                break;
            }
        }

        // 记录爬取信息
        recordCrawlerInfo(searchTerm, maxRecords, batchSize, dateFrom, dateTo, totalFetched, totalSaved, "COMPLETED", null);
        
        return String.format("FDA 510K设备数据爬取完成，总共获取: %d 条记录，保存: %d 条记录", totalFetched, totalSaved);
    }

    /**
     * 批量保存到数据库
     */
    @Transactional
    private String saveBatchToDatabase(List<Device510K> records) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int batchCount = 0;
        int consecutiveEmptyBatches = 0;

        for (int i = 0; i < records.size(); i += BATCH_SAVE_SIZE) {
            int endIndex = Math.min(i + BATCH_SAVE_SIZE, records.size());
            List<Device510K> batch = records.subList(i, endIndex);
            batchCount++;

            List<Device510K> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (Device510K record : batch) {
                try {
                    // 检查是否已存在（使用K号）
                    boolean isDuplicate = false;
                    
                    String kNumber = record.getKNumber();
                    
                    // 重试机制：最多重试3次
                    int retryCount = 0;
                    int maxRetries = 3;
                    boolean querySuccess = false;
                    
                    while (retryCount < maxRetries && !querySuccess) {
                        try {
                            if (kNumber != null && !kNumber.trim().isEmpty()) {
                                if (device510KRepository.existsBykNumber(kNumber)) {
                                    isDuplicate = true;
                                }
                            }
                            querySuccess = true;
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount >= maxRetries) {
                                System.err.println("重复检查失败，已重试" + maxRetries + "次，跳过该记录: " + e.getMessage());
                                isDuplicate = true;
                                break;
                            } else {
                                System.err.println("重复检查失败，第" + retryCount + "次重试: " + e.getMessage());
                                try {
                                    Thread.sleep(1000 * retryCount);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    }

                    if (isDuplicate) {
                        batchDuplicateCount++;
                    } else {
                        newRecords.add(record);
                    }
                } catch (Exception e) {
                    System.err.println("重复检查过程中发生未知错误，跳过该记录: " + e.getMessage());
                    batchDuplicateCount++;
                }
            }

            if (newRecords.isEmpty()) {
                System.out.println("本批次全部为重复记录，跳过了 " + batch.size() + " 条记录");
                consecutiveEmptyBatches++;
                System.out.println("第 " + batchCount + " 批次数据全部重复，连续空批次: " + consecutiveEmptyBatches);
                
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
                        device510KRepository.saveAll(newRecords);
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

        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
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
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        System.out.printf("警告: 字段内容过长，已截断至%d字符: %s%n", maxLength, str.substring(0, Math.min(50, str.length())));
        return str.substring(0, maxLength);
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
     * 主函数用于测试
     */
    public static void main(String[] args) {
        System.out.println("=== US_510K 爬虫测试 ===");
        
        try {
            US_510K crawler = new US_510K();
            
            // 测试1: 按设备名称搜索
            System.out.println("\n1. 测试按设备名称搜索...");
            String result1 = crawler.crawlAndSaveByDeviceName("skin", 10, 5);
            System.out.println("   结果: " + result1);
            
            // 测试2: 按申请人搜索
            System.out.println("\n2. 测试按申请人搜索...");
            String result2 = crawler.crawlAndSaveByApplicant("medtronic", 10, 5);
            System.out.println("   结果: " + result2);
            
            // 测试3: 按trade_name搜索
            System.out.println("\n3. 测试按trade_name搜索...");
            String result3 = crawler.crawlAndSaveByTradeName("Monitor", 10, 5);
            System.out.println("   结果: " + result3);
            
            // 测试4: 通用搜索方法
            System.out.println("\n4. 测试通用搜索方法...");
            String result4 = crawler.crawlAndSaveDevice510K("device_name:catheter", 10, 5);
            System.out.println("   结果: " + result4);
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n=== 测试完成 ===");
        }
    }
}

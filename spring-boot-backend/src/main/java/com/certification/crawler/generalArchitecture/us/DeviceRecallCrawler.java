package com.certification.crawler.generalArchitecture.us;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
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
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import com.certification.util.RiskLevelUtil;
import com.certification.util.KeywordUtil;
import com.certification.entity.common.CrawlerData.RiskLevel;

import com.certification.entity.common.ProductRecall;
import com.certification.repository.common.ProductRecallRepository;
import com.certification.repository.common.CrawlerCheckpointRepository;
import com.certification.entity.common.CrawlerCheckpoint;
import lombok.Data;
// 新增导入
import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;

/**
 * FDA设备召回数据爬取器
 * 专门用于爬取FDA设备召回数据并保存到数据库
 */
@Component
public class DeviceRecallCrawler {

    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
    private static final int RETRY_COUNT = 5;
    private static final int RETRY_DELAY = 5;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ProductRecallRepository productRecallRepository;
    // 新增仓库
    private final DeviceRecallRecordRepository deviceRecallRecordRepository;
    private final CrawlerCheckpointRepository checkpointRepository;
    
    private static final String CRAWLER_TYPE = "DeviceRecall";
    private static final int BATCH_SAVE_SIZE = 50; // 每50条数据保存一次

    @Autowired
    public DeviceRecallCrawler(ProductRecallRepository productRecallRepository,
                               DeviceRecallRecordRepository deviceRecallRecordRepository,
                               CrawlerCheckpointRepository checkpointRepository) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.productRecallRepository = productRecallRepository;
        this.deviceRecallRecordRepository = deviceRecallRecordRepository;
        this.checkpointRepository = checkpointRepository;
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
     * 设备召回数据模型（对齐新需求：MedicalDeviceRecall）
     */
    @Data
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
        private LocalDate eventDateInitiated; // 召回回启动日期
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
            try (var response = httpClient.executeOpen(null, httpGet, null)) {
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
     * 爬取设备召回数据
     */
    public List<MedicalDeviceRecall> crawlDeviceRecall(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceRecall(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取设备召回数据（支持时间范围和断点续传）
     */
    public List<MedicalDeviceRecall> crawlDeviceRecall(String searchTerm, int maxRecords, int batchSize,
                                                      String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备召回数据...");
        
        // 检查是否有断点可以继续
        CrawlerCheckpoint checkpoint = getOrCreateCheckpoint(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        
        if (checkpoint.isCompleted()) {
            System.out.println("爬取任务已完成，无需重复执行");
            return new ArrayList<>();
        }
        
        List<MedicalDeviceRecall> allResults = new ArrayList<>();
        int skip = checkpoint.getCurrentSkip();
        int totalFetched = checkpoint.getTotalFetched();

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, maxRecords - totalFetched);
            
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备召回数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));
            
            // 添加时间范围参数
            if (dateFrom != null && dateTo != null) {
                String dateRange = String.format("event_date_initiated:[%s TO %s]", dateFrom, dateTo);
                String currentSearch = searchTerm != null && !searchTerm.isEmpty() ?
                    searchTerm + " AND " + dateRange : dateRange;
                params.put("search", currentSearch);
            }

            try {
                Map<String, Object> data = fetchData("/device/recall.json", params);
                List<Map<String, Object>> results = data != null ? 
                        (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

                if (results.isEmpty()) {
                    System.out.println("没有更多设备召回数据，爬取结束");
                    checkpoint.markCompleted();
                    checkpointRepository.save(checkpoint);
                    break;
                }

                // 转换为MedicalDeviceRecall对象
                for (Map<String, Object> result : results) {
                    MedicalDeviceRecall recall = objectMapper.convertValue(result, MedicalDeviceRecall.class);
                    // 新增：设置数据适用国家
                    recall.setJdCountry("US");
                    allResults.add(recall);
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
                        Integer totalAvailable = null;
                        Object totalObj = resultsMeta.get("total");
                        if (totalObj instanceof Number) {
                            totalAvailable = ((Number) totalObj).intValue();
                        }
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

        List<MedicalDeviceRecall> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        System.out.printf("设备召回数据爬取完成，共 %d 条数据%n", finalResults.size());
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
//
//    /**
//     * 根据产品描述和国家爬取设备召回数据
//     */
//    public List<MedicalDeviceRecall> crawlDeviceRecallByProductAndCountry(String productDescription, String country, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        System.out.printf("开始根据产品描述 '%s' 和国家 '%s' 爬取设备召回数据...%n", productDescription, country);
//
//        // 构建搜索条件：产品描述和国家
//        StringBuilder searchBuilder = new StringBuilder();
//
//        if (productDescription != null && !productDescription.trim().isEmpty()) {
//            searchBuilder.append("product_description.exact:\"").append(productDescription.trim()).append("\"");
//        }
//
////        if (country != null && !country.trim().isEmpty()) {
////            if (searchBuilder.length() > 0) {
////                searchBuilder.append(" AND ");
////            }
////            searchBuilder.append("country:\"").append(country.trim().toUpperCase()).append("\"");
////        }
//
//        String searchTerm = searchBuilder.toString();
//        System.out.println("搜索条件: " + searchTerm);
//
//        return crawlDeviceRecall(searchTerm, maxRecords, batchSize);
//    }

    /**
     * 根据召回原因爬取设备召回数据
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByReason(String reasonForRecall, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据召回原因 '%s' 爬取设备召回数据...%n", reasonForRecall);

        String searchTerm = "reason_for_recall:\"" + reasonForRecall.trim() + "\"";
        System.out.println("搜索条件: " + searchTerm);

        return crawlDeviceRecall(searchTerm, maxRecords, batchSize);
    }

    /**
     * 根据产品描述爬取设备召回数据
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByProductDescription(String productDescription, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据产品描��� '%s' 爬取设备召回数据...%n", productDescription);

        String searchTerm = "product_description:\"" + productDescription.trim() + "\"";
        System.out.println("搜索条件: " + searchTerm);

        return crawlDeviceRecall(searchTerm, maxRecords, batchSize);
    }

//    /**
//     * 根据召回原因和国家爬取设备召回数据
//     */
//    public List<MedicalDeviceRecall> crawlDeviceRecallByReasonAndCountry(String reasonForRecall, String country, int maxRecords, int batchSize)
//            throws IOException, URISyntaxException, ParseException {
//        System.out.printf("开始根据召回原因 '%s' 和国家 '%s' 爬取设备召回数据...%n", reasonForRecall, country);
//
//        StringBuilder searchBuilder = new StringBuilder();
//
//        if (reasonForRecall != null && !reasonForRecall.trim().isEmpty()) {
//            searchBuilder.append("reason_for_recall:\"").append(reasonForRecall.trim()).append("\"");
//        }
//
//        if (country != null && !country.trim().isEmpty()) {
//            if (searchBuilder.length() > 0) {
//                searchBuilder.append(" AND ");
//            }
//            searchBuilder.append("country:\"").append(country.trim().toUpperCase()).append("\"");
//        }
//
//        String searchTerm = searchBuilder.toString();
//        System.out.println("搜索条件: " + searchTerm);
//
//        return crawlDeviceRecall(searchTerm, maxRecords, batchSize);
//    }

    /**
     * 爬取并保存设备召回数据到数据库
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecalls(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlAndSaveDeviceRecalls(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取并保存设备召回数据到数据库（支持时间范围）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecalls(String searchTerm, int maxRecords, int batchSize,
                                                           String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取并保存FDA设备召回数据到 t_device_recall...");
        return crawlAndSaveMedicalDeviceRecalls(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 根据召回原因爬取并保存设备召回数据到数据库（保存到 t_device_recall）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecallsByReason(String reasonForRecall, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据召回原因 '%s' 爬取并保存FDA设备召回数据到 t_device_recall...%n", reasonForRecall);
        String term = "reason_for_recall:\"" + reasonForRecall.trim() + "\"";
        return crawlAndSaveMedicalDeviceRecalls(term, maxRecords, batchSize);
    }

    /**
     * 根据产品描述爬取并保存设备召回数据到数据库（保存到 t_device_recall）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecallsByProductDescription(String productDescription, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据产品描述 '%s' 爬取并保存FDA设备召回数据到 t_device_recall...%n", productDescription);
        String term = "product_description:\"" + productDescription.trim() + "\"";
        return crawlAndSaveMedicalDeviceRecalls(term, maxRecords, batchSize);
    }


    /**
     * 根据设备名称爬取并保存设备召回数据到数据库（保存到 t_device_recall）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecallsByDevicename(String devicename, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据设备名称 '%s' 爬取并保存FDA设备召回数据到 t_device_recall...%n", devicename);
        String term = "openfda.device_name:\"" + devicename.trim() + "\"";
        return crawlAndSaveMedicalDeviceRecalls(term, maxRecords, batchSize);
    }

    /**
     * 根据召回公司爬取并保存设备召回数据到数据库（保存到 t_device_recall）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveDeviceRecallsByRecallingFirm(String recallingfirm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        System.out.printf("开始根据召回公司 '%s' 爬取并保存FDA设备召回数据到 t_device_recall...%n", recallingfirm);
        String term = "search=recalling_firm:\"" + recallingfirm.trim() + "\"";
        return crawlAndSaveMedicalDeviceRecalls(term, maxRecords, batchSize);
    }


    /**
     * 爬取并保存设备召回数据到新表：t_device_recall
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveMedicalDeviceRecalls(String searchTerm, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlAndSaveMedicalDeviceRecalls(searchTerm, maxRecords, batchSize, null, null);
    }
    
    /**
     * 爬取并保存设备召回数据到新表：t_device_recall（支持时间范围和分批保存）
     */
    @Transactional
    public List<DeviceRecallRecord> crawlAndSaveMedicalDeviceRecalls(String searchTerm, int maxRecords, int batchSize,
                                                                   String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取并保存FDA设备召回数据到 t_device_recall...");
        List<MedicalDeviceRecall> deviceRecalls = crawlDeviceRecall(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        List<DeviceRecallRecord> saved = new ArrayList<>();
        List<DeviceRecallRecord> batchToSave = new ArrayList<>();

        // 批内去重：优先使用 cfresId，其次 productResNumber，最后使用组合键
        LinkedHashSet<String> seenKeys = new LinkedHashSet<>();
        // 新增：批内k_numbers去重集合
        LinkedHashSet<String> seenKNumbers = new LinkedHashSet<>();
        int batchDuplicates = 0;
        int kNumbersDuplicates = 0;

        for (MedicalDeviceRecall mr : deviceRecalls) {
            String key = buildUniqueKey(mr);
            if (key != null) {
                if (!seenKeys.add(key)) {
                    batchDuplicates++;
                    continue; // 批内重复，跳过
                }
            }

            // 新增：批内k_numbers去重检查
            String kNumbersStr = null;
            if (mr.getKNumbers() != null && !mr.getKNumbers().isEmpty()) {
                kNumbersStr = joinDistinctKNumbers(mr.getKNumbers());
                if (kNumbersStr != null && !kNumbersStr.trim().isEmpty()) {
                    if (!seenKNumbers.add(kNumbersStr)) {
                        kNumbersDuplicates++;
                        System.out.printf("批内K编号重复，跳过记录: %s%n", kNumbersStr);
                        continue; // 批内K编号重复，跳过
                    }
                }
            }

            // 去重（库内）：优先按 cfresId
            boolean exists = false;
            if (mr.getCfresId() != null && !mr.getCfresId().trim().isEmpty()) {
                exists = deviceRecallRecordRepository.findByCfresId(mr.getCfresId()).isPresent();
            }
            if (exists) {
                continue; // 库内已存在，跳过
            }

            DeviceRecallRecord entity = convertToDeviceRecallRecord(mr);
            batchToSave.add(entity);
            
            // 每50条数据保存一次
            if (batchToSave.size() >= BATCH_SAVE_SIZE) {
                List<DeviceRecallRecord> batchSaved = deviceRecallRecordRepository.saveAll(batchToSave);
                saved.addAll(batchSaved);
                batchToSave.clear();
                System.out.printf("已保存 %d 条设备召回数据%n", saved.size());
            }
        }
        
        // 保存剩余的数据
        if (!batchToSave.isEmpty()) {
            List<DeviceRecallRecord> batchSaved = deviceRecallRecordRepository.saveAll(batchToSave);
            saved.addAll(batchSaved);
            System.out.printf("已保存剩余 %d 条设备召回数据%n", batchSaved.size());
        }
        
        if (batchDuplicates > 0 || kNumbersDuplicates > 0) {
            System.out.printf("去重统计：批内重复 %d 条，K编号重复 %d 条，实际入库 %d 条%n", 
                    batchDuplicates, kNumbersDuplicates, saved.size());
        }
        System.out.println("成功保存 " + saved.size() + " 条设备召回记录到 t_device_recall");
        return saved;
    }

    // 生成唯一键：cfresId > productResNumber > 组合键（productCode|productDescription|eventDateInitiated|recallingFirm）
    private String buildUniqueKey(MedicalDeviceRecall src) {
        String a = safeUpper(src.getCfresId());
        if (a != null) return "CFRES:" + a;
        String b = safeUpper(src.getProductResNumber());
        if (b != null) return "PRES:" + b;
        String c1 = safeUpper(src.getProductCode());
        String c2 = safeUpper(src.getProductDescription());
        String c3 = src.getEventDateInitiated() == null ? null : src.getEventDateInitiated().toString();
        String c4 = safeUpper(src.getRecallingFirm());
        if (c1 == null && c2 == null && c3 == null && c4 == null) return null;
        return String.join("|", c1 == null ? "" : c1, c2 == null ? "" : c2, c3 == null ? "" : c3, c4 == null ? "" : c4);
    }

    private String safeUpper(String s) { return (s == null || s.trim().isEmpty()) ? null : s.trim().toUpperCase(); }

    /**
     * 将 MedicalDeviceRecall 映射为 DeviceRecallRecord（适配重写后的实体类）
     */
    private DeviceRecallRecord convertToDeviceRecallRecord(MedicalDeviceRecall src) {
        DeviceRecallRecord d = new DeviceRecallRecord();
        // 设置基本信息（适配重写后的DeviceRecallRecord实体类）
        d.setCfresId(src.getCfresId());
        d.setProductDescription(src.getProductDescription());
        d.setRecallingFirm(safeTrim(src.getRecallingFirm()));
        d.setRecallStatus(src.getRecallStatus());
        d.setEventDatePosted(src.getEventDatePosted());
        d.setDeviceName(src.getDeviceName());
        d.setProductCode(safeTrim(src.getProductCode()));
        d.setDataSource("FDA");
        d.setCountryCode("US");
        d.setJdCountry("US");
        
        // 计算风险等级
        RiskLevel calculatedRiskLevel = RiskLevelUtil.calculateRiskLevelByRecallStatus(src.getRecallStatus());
        d.setRiskLevel(calculatedRiskLevel);
        
        // 新增：提取关键词
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
        d.setKeywords(KeywordUtil.keywordsToJson(uniqueKeywords));
        
        return d;
    }

    private String safeTrim(String s) { return s == null ? null : s.trim(); }

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
     * 将MedicalDeviceRecall转换为ProductRecall
     */
    private ProductRecall convertToProductRecall(MedicalDeviceRecall deviceRecall) {
        ProductRecall productRecall = new ProductRecall();
        
        // 设置CFRES ID
        if (deviceRecall.getCfresId() != null && !deviceRecall.getCfresId().trim().isEmpty()) {
            productRecall.setCfresId(deviceRecall.getCfresId());
        }
        
        // 设置事件发起日期
        if (deviceRecall.getEventDateInitiated() != null) {
            productRecall.setEventDateInitiated(deviceRecall.getEventDateInitiated());
        }
        
        // 设置召回编号（使用 product_res_number）
        if (deviceRecall.getProductResNumber() != null && !deviceRecall.getProductResNumber().trim().isEmpty()) {
            productRecall.setRecallNumber(deviceRecall.getProductResNumber());
        }
        
        // 设置召回原因
        if (deviceRecall.getReasonForRecall() != null && !deviceRecall.getReasonForRecall().trim().isEmpty()) {
            productRecall.setRecallReason(deviceRecall.getReasonForRecall());
        }

        // 设置产品代��
        if (deviceRecall.getProductCode() != null && !deviceRecall.getProductCode().trim().isEmpty()) {
            productRecall.setProductCode(deviceRecall.getProductCode());
        }

        // 设置��回状态
        if (deviceRecall.getRecallStatus() != null && !deviceRecall.getRecallStatus().trim().isEmpty()) {
            productRecall.setRecallStatus(deviceRecall.getRecallStatus());
        }

        // 设置召回公司
        if (deviceRecall.getRecallingFirm() != null && !deviceRecall.getRecallingFirm().trim().isEmpty()) {
            productRecall.setRecallingFirm(deviceRecall.getRecallingFirm());
        }
        
        // 设置K编号列表
        if (deviceRecall.getKNumbers() != null && !deviceRecall.getKNumbers().isEmpty()) {
            productRecall.setKNumbers(joinDistinctKNumbers(deviceRecall.getKNumbers()));
        }

        // 设置产品描述
        if (deviceRecall.getProductDescription() != null && !deviceRecall.getProductDescription().trim().isEmpty()) {
            productRecall.setProductDescription(deviceRecall.getProductDescription());
        }
        
        // 设置召回日期（使用启动日期）
        productRecall.setRecallDate(deviceRecall.getEventDateInitiated());

        // 设置受影响数量（由 product_quantity 解析）
        productRecall.setAffectedQuantity(parseAffectedQuantity(deviceRecall.getProductQuantity()));

        // 构建官方公告URL（使用 product_res_number 检索）
        String authorityNoticeUrl = deviceRecall.getProductResNumber() != null && !deviceRecall.getProductResNumber().trim().isEmpty() ?
                String.format("https://api.fda.gov/device/recall.json?search=product_res_number:%s", deviceRecall.getProductResNumber()) :
                "https://api.fda.gov/device/recall.json";
        productRecall.setAuthorityNoticeUrl(authorityNoticeUrl);
        
        // 国家：若数据缺失，默认设置为美国
        productRecall.setCountryCode("US");

        return productRecall;
    }

    /**
     * 解析召回日期，支持yyyyMMdd和yyyy-MM-dd等常见格式
     */
    private LocalDate parseRecallDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String[] patterns = {"yyyyMMdd", "yyyy-MM-dd", "MM/dd/yyyy"};
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (Exception ignored) {
            }
        }
        System.err.println("无法解析日期: " + dateStr);
        return null;
    }

    /**
     * 解析受影响数量
     */
    private Integer parseAffectedQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 移除可能的非数字字符
            String cleanQuantity = quantityStr.replaceAll("[^0-9]", "");
            if (cleanQuantity.isEmpty()) {
                return null;
            }
            return Integer.parseInt(cleanQuantity);
        } catch (NumberFormatException e) {
            System.err.println("无法解析数量: " + quantityStr);
            return null;
        }
    }

    /**
     * 将国家名称映射为国家代码（保留工具方法，当前转换流程默认 US）
     */
    private String mapCountryNameToCode(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return "US"; // 默认返回美国
        }
        
        String normalizedName = countryName.trim().toLowerCase();
        
        // 国家名称到代码的映射（只包含t_country表中存在的代码）
        switch (normalizedName) {
            case "united states":
            case "united states of america":
            case "usa":
            case "us":
                return "US";
            case "china":
            case "peoples republic of china":
            case "cn":
                return "CN";
            case "european union":
            case "eu":
                return "EU";
            case "japan":
            case "jp":
                return "JP";
            case "south korea":
            case "korea":
            case "kr":
                return "KR";
            case "australia":
            case "au":
                return "AU";
            case "united arab emirates":
            case "uae":
            case "ae":
                return "AE";
            case "india":
            case "in":
                return "IN";
            case "thailand":
            case "th":
                return "TH";
            case "singapore":
            case "sg":
                return "SG";
            case "taiwan":
            case "tw":
                return "TW";
            case "chile":
            case "cl":
                return "CL";
            case "malaysia":
            case "my":
                return "MY";
            case "peru":
            case "pe":
                return "PE";
            case "south africa":
            case "za":
                return "ZA";
            case "israel":
            case "il":
                return "IL";
            case "indonesia":
            case "id":
                return "ID";
            default:
                if (normalizedName.length() == 2) {
                    String upperCode = normalizedName.toUpperCase();
                    if (isValidCountryCode(upperCode)) {
                        return upperCode;
                    }
                }
                System.out.println("未知的国家名称: " + countryName + "，使用默认值: US");
                return "US";
        }
    }
    
    /**
     * 检查国家代码是否有效（存在于t_country表中）
     */
    private boolean isValidCountryCode(String countryCode) {
        String[] validCodes = {"US", "EU", "CN", "KR", "JP", "AE", "IN", "TH", "SG", "TW", "AU", "CL", "MY", "PE", "ZA", "IL", "ID"};
        for (String code : validCodes) {
            if (code.equals(countryCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据设备名称爬取设备召回数据
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByDeviceName(String deviceName, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceRecallByDeviceName(deviceName, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据设备名称爬取设备召回数据（支持时间范围）
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByDeviceName(String deviceName, int maxRecords, int batchSize,
                                                                  String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (deviceName == null || deviceName.trim().isEmpty())
                ? ""
                : "product_description:\"" + deviceName.trim() + "\"";
        System.out.println("按设备名称搜索: " + term);
        return crawlDeviceRecall(term, maxRecords, batchSize, dateFrom, dateTo);
    }

    /**
     * 根据专业描述关键词爬取设备召回数据
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize)
            throws IOException, URISyntaxException, ParseException {
        return crawlDeviceRecallByMedicalSpecialty(specialtyKeywords, maxRecords, batchSize, null, null);
    }
    
    /**
     * 根据专业描述关键词爬取设备召回数据（支持时间范围）
     */
    public List<MedicalDeviceRecall> crawlDeviceRecallByMedicalSpecialty(String specialtyKeywords, int maxRecords, int batchSize,
                                                                        String dateFrom, String dateTo)
            throws IOException, URISyntaxException, ParseException {
        String term = (specialtyKeywords == null || specialtyKeywords.trim().isEmpty())
                ? ""
                : "openfda.medical_specialty_description:\"" + specialtyKeywords.trim() + "\"";
        System.out.println("按专业描述关键词搜索: " + term);
        return crawlDeviceRecall(term, maxRecords, batchSize, dateFrom, dateTo);
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
     * K编号去重与标准化（去空、去重、统一大写，保持原始顺序）。
     * 额外：仅保留合法格式（K+6位数字），并在发现重复/无效时输出提示日志。
     */
    private String joinDistinctKNumbers(List<String> kNumbers) {
        if (kNumbers == null || kNumbers.isEmpty()) return null;
        LinkedHashSet<String> set = new LinkedHashSet<>();
        int total = 0;
        int invalid = 0;
        int duplicates = 0;
        Pattern p = Pattern.compile("^K\\d{6}$"); // 典型510(k)编号格式
        for (String k : kNumbers) {
            if (k == null) { total++; invalid++; continue; }
            String v = k.trim();
            total++;
            if (v.isEmpty()) { invalid++; continue; }
            v = v.toUpperCase();
            if (!p.matcher(v).matches()) { invalid++; continue; }
            if (!set.add(v)) { duplicates++; }
        }
        if (set.isEmpty()) return null;
        if (invalid > 0 || duplicates > 0) {
            System.out.printf("K编号清洗：总计%d，过滤无效%d，去重%d，保留%d%n", total, invalid, duplicates, set.size());
        }
        return String.join(",", set);
    }
}

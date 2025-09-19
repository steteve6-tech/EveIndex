package com.certification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.http.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * FDA API 通用客户端
 * 支持获取设备注册信息、设备召回、设备不良事件、设备上市前审批和设备510k审批数据
 */
public class FDAApiCrawler {
    private static final String BASE_URL = "https://api.fda.gov";
    private static final String API_KEY = "xSSE0jrA316WGLwkRQzPhSlgmYbHIEsZck6H62ji";
    private static final int MAX_LIMIT = 1000;
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 5;
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FDAApiCrawler() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * FDA API 端点枚举
     */
    public enum FDAEndpoint {
        DEVICE_REGISTRATION("/device/registrationlisting.json"),
        DEVICE_RECALL("/device/recall.json"),
        DEVICE_EVENTS("/device/event.json"),
        DEVICE_PMA("/device/pma.json"),
        DEVICE_510K("/device/510k.json");

        private final String path;

        FDAEndpoint(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * 发送HTTP请求并获取数据，包含重试机制
     */
    private Map<String, Object> fetchData(FDAEndpoint endpoint, Map<String, String> params) 
            throws IOException, URISyntaxException, ParseException {
        URIBuilder uriBuilder = new URIBuilder(BASE_URL + endpoint.getPath());

        // 添加请求参数
        if (API_KEY != null && !API_KEY.isEmpty()) {
            params.put("api_key", API_KEY);
        }

        params.forEach(uriBuilder::addParameter);

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        System.out.println(httpGet);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        for (int retry = 0; retry < RETRY_COUNT; retry++) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String json = EntityUtils.toString(entity);
                        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                    }
                } else {
                    System.err.printf("请求失败，状态码: %d（第%d次重试）%n", statusCode, retry + 1);
                }
            } catch (IOException e) {
                System.err.printf("请求异常: %s（第%d次重试）%n", e.getMessage(), retry + 1);
            }

            if (retry < RETRY_COUNT - 1) {
                try {
                    TimeUnit.SECONDS.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 爬取设备注册信息数据
     */
    public void crawlDeviceRegistration(String searchTerm, int maxRecords, int batchSize, String outputFile) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备注册信息数据...");
        List<Map<String, Object>> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, Math.min(maxRecords - totalFetched, 1000));
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备注册数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData(FDAEndpoint.DEVICE_REGISTRATION, params);
            List<Map<String, Object>> results = data != null ? 
                    (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

            if (results.isEmpty()) {
                System.out.println("没有更多设备注册数据，爬取结束");
                break;
            }

            allResults.addAll(results);
            totalFetched += results.size();
            skip += currentLimit;

            // 检查是否已获取所有匹配记录
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
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
        }

        List<Map<String, Object>> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), finalResults);
        System.out.printf("设备注册信息爬取完成，共 %d 条数据，已保存至 %s%n", finalResults.size(), outputFile);
    }

    /**
     * 爬取设备召回数据
     */
    public void crawlDeviceRecall(String searchTerm, int maxRecords, int batchSize, String outputFile) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备召回数据...");
        List<Map<String, Object>> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, Math.min(maxRecords - totalFetched, 1000));
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备召回数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData(FDAEndpoint.DEVICE_RECALL, params);
            List<Map<String, Object>> results = data != null ? 
                    (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

            if (results.isEmpty()) {
                System.out.println("没有更多设备召回数据，爬取结束");
                break;
            }

            allResults.addAll(results);
            totalFetched += results.size();
            skip += currentLimit;

            // 检查是否已获取所有匹配记录
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
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
        }

        List<Map<String, Object>> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), finalResults);
        System.out.printf("设备召回数据爬取完成，共 %d 条数据，已保存至 %s%n", finalResults.size(), outputFile);
    }

    /**
     * 爬取设备不良事件数据
     */
    public void crawlDeviceEvents(String searchTerm, int maxRecords, int batchSize, String outputFile) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备不良事件数据...");
        List<Map<String, Object>> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, Math.min(maxRecords - totalFetched, 1000));
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备不良事件数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData(FDAEndpoint.DEVICE_EVENTS, params);
            List<Map<String, Object>> results = data != null ? 
                    (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

            if (results.isEmpty()) {
                System.out.println("没有更多设备不良事件数据，爬取结束");
                break;
            }

            allResults.addAll(results);
            totalFetched += results.size();
            skip += currentLimit;

            // 检查是否已获取所有匹配记录
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
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
        }

        List<Map<String, Object>> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), finalResults);
        System.out.printf("设备不良事件数据爬取完成，共 %d 条数据，已保存至 %s%n", finalResults.size(), outputFile);
    }

    /**
     * 爬取设备上市前审批数据
     */
    public void crawlDevicePMA(String searchTerm, int maxRecords, int batchSize, String outputFile) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备上市前审批数据...");
        List<Map<String, Object>> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, Math.min(maxRecords - totalFetched, 1000));
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备上市前审批数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData(FDAEndpoint.DEVICE_PMA, params);
            List<Map<String, Object>> results = data != null ? 
                    (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

            if (results.isEmpty()) {
                System.out.println("没有更多设备上市前审批数据，爬取结束");
                break;
            }

            allResults.addAll(results);
            totalFetched += results.size();
            skip += currentLimit;

            // 检查是否已获取所有匹配记录
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
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
        }

        List<Map<String, Object>> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), finalResults);
        System.out.printf("设备上市前审批数据爬取完成，共 %d 条数据，已保存至 %s%n", finalResults.size(), outputFile);
    }

    /**
     * 爬取设备510k审批数据
     */
    public void crawlDevice510K(String searchTerm, int maxRecords, int batchSize, String outputFile) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备510k审批数据...");
        List<Map<String, Object>> allResults = new ArrayList<>();
        int skip = 0;
        int totalFetched = 0;

        while (totalFetched < maxRecords) {
            int currentLimit = Math.min(batchSize, Math.min(maxRecords - totalFetched, 1000));
            if (currentLimit <= 0) {
                break;
            }

            System.out.printf("获取第 %d 页设备510k审批数据（偏移量: %d，数量: %d）%n", 
                    skip / batchSize + 1, skip, currentLimit);

            Map<String, String> params = new HashMap<>();
            params.put("search", searchTerm);
            params.put("limit", String.valueOf(currentLimit));
            params.put("skip", String.valueOf(skip));

            Map<String, Object> data = fetchData(FDAEndpoint.DEVICE_510K, params);
            List<Map<String, Object>> results = data != null ? 
                    (List<Map<String, Object>>) data.get("results") : new ArrayList<>();

            if (results.isEmpty()) {
                System.out.println("没有更多设备510k审批数据，爬取结束");
                break;
            }

            allResults.addAll(results);
            totalFetched += results.size();
            skip += currentLimit;

            // 检查是否已获取所有匹配记录
            if (data != null && data.containsKey("meta")) {
                Map<String, Object> meta = (Map<String, Object>) data.get("meta");
                if (meta.containsKey("results")) {
                    Map<String, Object> resultsMeta = (Map<String, Object>) meta.get("results");
                    Integer totalAvailable = (Integer) resultsMeta.get("total");
                    if (totalAvailable != null && totalFetched >= totalAvailable) {
                        System.out.printf("已获取所有匹配记录（共 %d 条）%n", totalAvailable);
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
        }

        List<Map<String, Object>> finalResults = allResults.subList(0, Math.min(allResults.size(), maxRecords));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), finalResults);
        System.out.printf("设备510k审批数据爬取完成，共 %d 条数据，已保存至 %s%n", finalResults.size(), outputFile);
    }

    /**
     * 通用设备召回查询方法 - 支持多种查询参数
     */
    public void crawlDeviceRecallAdvanced(String searchTerm, int maxRecords, int batchSize, String outputFile, String description) 
            throws IOException, URISyntaxException, ParseException {
        System.out.println("开始爬取设备召回数据: " + description);
        crawlDeviceRecall(searchTerm, maxRecords, batchSize, outputFile);
    }

    /**
     * 演示各种设备召回查询示例
     */
    public void demonstrateDeviceRecallQueries() throws IOException, URISyntaxException, ParseException {
        System.out.println("=== 设备召回查询示例演示 ===");
        
        // 示例1: 基本字段查询
        crawlDeviceRecallAdvanced(
            "root_cause_description.exact:\"Device Design\"", 
            50, 25, 
            "examples/device_recall_basic.json",
            "基本字段查询 - 设备设计问题"
        );
        
        // 示例2: 日期范围查询
        crawlDeviceRecallAdvanced(
            "recall_date:[20230101 TO 20231231]", 
            100, 25, 
            "examples/device_recall_date_range.json",
            "日期范围查询 - 2023年召回"
        );
        
        // 示例3: 多字段组合查询
        crawlDeviceRecallAdvanced(
            "product_type:\"Device\" AND recall_date:[20230101 TO 20231231]", 
            75, 25, 
            "examples/device_recall_combined.json",
            "多字段组合查询 - 2023年设备类型召回"
        );
        
        // 示例4: 模糊匹配查询
        crawlDeviceRecallAdvanced(
            "root_cause_description:\"Design\"", 
            60, 25, 
            "examples/device_recall_fuzzy.json",
            "模糊匹配查询 - 包含Design的召回原因"
        );
        
        // 示例5: 制造商查询
        crawlDeviceRecallAdvanced(
            "recalling_firm:\"Johnson\"", 
            40, 20, 
            "examples/device_recall_manufacturer.json",
            "制造商查询 - Johnson公司召回"
        );
        
        System.out.println("=== 设备召回查询示例演示完成 ===");
    }

    /**
     * 演示各种设备不良事件查询示例
     */
    public void demonstrateDeviceEventQueries() throws IOException, URISyntaxException, ParseException {
        System.out.println("=== 设备不良事件查询示例演示 ===");
        
        // 示例1: 基本设备名称查询
        System.out.println("--- 查询X射线设备不良事件 ---");
        crawlDeviceEvents("device.generic_name:x-ray", 50, 25, "examples/device_events_xray.json");
        
        // 示例2: 患者序列号查询
        System.out.println("--- 查询特定患者序列号的不良事件 ---");
        crawlDeviceEvents("patient.patient_sequence_number:1", 30, 15, "examples/device_events_patient.json");
        
        // 示例3: 设备类型查询
        System.out.println("--- 查询特定设备类型的不良事件 ---");
        crawlDeviceEvents("device.device_type:\"Device\"", 40, 20, "examples/device_events_type.json");
        
        // 示例4: 制造商查询
        System.out.println("--- 查询特定制造商设备的不良事件 ---");
        crawlDeviceEvents("device.manufacturer_name:\"Siemens\"", 35, 15, "examples/device_events_manufacturer.json");
        
        // 示例5: 复杂组合查询
        System.out.println("--- 查询X射线设备且特定制造商的不良事件 ---");
        crawlDeviceEvents("device.generic_name:x-ray AND device.manufacturer_name:\"Siemens\"", 25, 10, "examples/device_events_combined.json");
        
        System.out.println("=== 设备不良事件查询示例演示完成 ===");
    }

    /**
     * 演示各种设备上市前审批查询示例
     */
    public void demonstrateDevicePMAQueries() throws IOException, URISyntaxException, ParseException {
        System.out.println("=== 设备上市前审批查询示例演示 ===");
        
        // 示例1: 基本决策代码查询
        System.out.println("--- 查询批准的设备上市前审批 ---");
        crawlDevicePMA("decision_code:APPR", 50, 25, "examples/device_pma_approved.json");
        
        // 示例2: 特定年份查询
        System.out.println("--- 查询2023年的设备上市前审批 ---");
        crawlDevicePMA("decision_date:[20230101 TO 20231231]", 40, 20, "examples/device_pma_2023.json");
        
        // 示例3: 制造商查询
        System.out.println("--- 查询特定制造商的设备上市前审批 ---");
        crawlDevicePMA("applicant:\"Johnson & Johnson\"", 35, 15, "examples/device_pma_manufacturer.json");
        
        // 示例4: 设备类型查询
        System.out.println("--- 查询特定设备类型的上市前审批 ---");
        crawlDevicePMA("device_type:\"Device\"", 45, 20, "examples/device_pma_device_type.json");
        
        // 示例5: 复杂组合查询
        System.out.println("--- 查询2023年批准的特定制造商设备上市前审批 ---");
        crawlDevicePMA("decision_code:APPR AND decision_date:[20230101 TO 20231231] AND applicant:\"Johnson\"", 30, 15, "examples/device_pma_combined.json");
        
        System.out.println("=== 设备上市前审批查询示例演示完成 ===");
    }

    /**
     * 演示各种设备510k审批查询示例
     */
    public void demonstrateDevice510KQueries() throws IOException, URISyntaxException, ParseException {
        System.out.println("=== 设备510k审批查询示例演示 ===");
        
        // 示例1: 基本咨询委员会查询
        System.out.println("--- 查询心血管咨询委员会的510k审批 ---");
        crawlDevice510K("cv.advisory_committee:cv", 50, 25, "examples/device_510k_cv_committee.json");
        
        // 示例2: 特定年份查询
        System.out.println("--- 查询2023年的510k审批 ---");
        crawlDevice510K("decision_date:[20230101 TO 20231231]", 40, 20, "examples/device_510k_2023.json");
        
        // 示例3: 制造商查询
        System.out.println("--- 查询特定制造商的510k审批 ---");
        crawlDevice510K("applicant:\"Medtronic\"", 35, 15, "examples/device_510k_manufacturer.json");
        
        // 示例4: 设备类型查询
        System.out.println("--- 查询特定设备类型的510k审批 ---");
        crawlDevice510K("device_type:\"Device\"", 45, 20, "examples/device_510k_device_type.json");
        
        // 示例5: 复杂组合查询
        System.out.println("--- 查询2023年心血管咨询委员会的510k审批 ---");
        crawlDevice510K("cv.advisory_committee:cv AND decision_date:[20230101 TO 20231231]", 30, 15, "examples/device_510k_combined.json");
        
        System.out.println("=== 设备510k审批查询示例演示完成 ===");
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    public static void main(String[] args) {
        FDAApiCrawler crawler = new FDAApiCrawler();
        
        try {
            // 检查命令行参数
            if (args.length > 0) {
                switch (args[0]) {
                    case "--examples":
                        // 运行设备召回查询示例演示
                        crawler.demonstrateDeviceRecallQueries();
//                        break;
//                    case "--device-events":
//                        // 运行设备不良事件查询示例演示
//                        crawler.demonstrateDeviceEventQueries();
//                        break;
//                    case "--device-pma":
//                        // 运行设备上市前审批查询示例演示
//                        crawler.demonstrateDevicePMAQueries();
//                        break;
//                    case "--device-510k":
//                        // 运行设备510k审批查询示例演示
//                        crawler.demonstrateDevice510KQueries();
//                        break;
//                    case "--all-examples":
//                        // 运行所有查询示例演示
//                        crawler.demonstrateDeviceRecallQueries();
//                        System.out.println("\n" + "=".repeat(50) + "\n");
//                        crawler.demonstrateDeviceEventQueries();
//                        System.out.println("\n" + "=".repeat(50) + "\n");
//                        crawler.demonstrateDevicePMAQueries();
//                        System.out.println("\n" + "=".repeat(50) + "\n");
//                        crawler.demonstrateDevice510KQueries();
//                        break;
//                    default:
//                        // 运行完整的数据爬取
//                        runFullDataCrawling(crawler);
                }
            } else {
                // 运行完整的数据爬取
                runFullDataCrawling(crawler);
            }
            
        } catch (Exception e) {
            System.err.println("爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                crawler.close();
            } catch (IOException e) {
                System.err.println("关闭HTTP客户端时发生错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 运行完整的数据爬取流程
     */
    private static void runFullDataCrawling(FDAApiCrawler crawler) throws IOException, URISyntaxException, ParseException {
        // 创建JSON解析器
        FDAJsonParser jsonParser = new FDAJsonParser();
//        // 1. 爬取设备注册信息数据
//        System.out.println("=== 开始爬取设备注册信息数据 ===");
//        crawler.crawlDeviceRegistration("products.product_code:HQY", 500, 100, "device_reglist_results.json");
//
//        // 解析并分析设备注册信息数据
//        System.out.println("\n=== 解析并分析设备注册信息数据 ===");
//        SimpleJsonAnalyzer.analyzeDeviceRegistration("device_reglist_results.json");
//
//        System.out.println("\n" + "=".repeat(50) + "\n");
//
        // 2. 爬取设备召回数据 - 多种查询示例
        System.out.println("=== 开始爬取设备召回数据 ===");
        
        // 2.1 基本查询：设备设计问题
        System.out.println("--- 查询设备设计问题 ---");
        crawler.crawlDeviceRecall("reason_for_recall:\"Skin\"", 100, 50, "device_recall_design.json");
        
//        // 2.2 查询特定年份的召回
//        System.out.println("--- 查询2023年的召回 ---");
//        crawler.crawlDeviceRecall("recall_date:[20230101 TO 20231231]", 200, 50, "device_recall_2023.json");
//
        // 2.3 查询特定产品类型的召回
        System.out.println("--- 查询特定产品类型召回 ---");
        crawler.crawlDeviceRecall("product_type:\"Device\"", 150, 50, "device_recall_by_type.json");
        
        // 2.4 查询特定制造商的召回
        System.out.println("--- 查询特定制造商召回 ---");
        crawler.crawlDeviceRecall("recalling_firm:\"Johnson & Johnson\"", 100, 25, "device_recall_johnson.json");
        
//        // 2.5 复杂查询：设备设计问题且2023年
//        System.out.println("--- 查询2023年设备设计问题 ---");
//        crawler.crawlDeviceRecall("root_cause_description.exact:\"Device Design\" AND recall_date:[20230101 TO 20231231]", 80, 25, "device_recall_design_2023.json");
//
//        System.out.println("\n" + "=".repeat(50) + "\n");
//
        // 3. 爬取设备不良事件数据
        System.out.println("=== 开始爬取设备不良事件数据 ===");
        
        // 3.1 基本查询：X射线设备不良事件
        System.out.println("--- 查询X射线设备不良事件 ---");
        crawler.crawlDeviceEvents("device.generic_name:x-ray", 100, 50, "device_events_xray.json");
        
        // 3.2 查询特定患者序列号的不良事件
        System.out.println("--- 查询特定患者序列号的不良事件 ---");
        crawler.crawlDeviceEvents("patient.patient_sequence_number:1", 80, 40, "device_events_patient.json");
        
        // 3.3 查询特定制造商设备的不良事件
        System.out.println("--- 查询Siemens设备的不良事件 ---");
        crawler.crawlDeviceEvents("device.manufacturer_name:\"Siemens\"", 120, 60, "device_events_siemens.json");
        
        // 3.4 复杂查询：X射线设备且特定制造商
        System.out.println("--- 查询X射线设备且Siemens制造商的不良事件 ---");
        crawler.crawlDeviceEvents("device.generic_name:x-ray AND device.manufacturer_name:\"Siemens\"", 60, 30, "device_events_xray_siemens.json");
        
        // 解析并分析设备不良事件数据
        System.out.println("\n=== 解析并分析设备不良事件数据 ===");
        SimpleJsonAnalyzer.analyzeDeviceEvents("device_events_xray.json");
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. 爬取设备上市前审批数据
        System.out.println("=== 开始爬取设备上市前审批数据 ===");
        
        // 4.1 基本查询：批准的设备上市前审批
        System.out.println("--- 查询批准的设备上市前审批 ---");
        crawler.crawlDevicePMA("decision_code:APPR", 80, 40, "device_pma_approved.json");
        
        // 4.2 查询特定年份的设备上市前审批
        System.out.println("--- 查询2023年的设备上市前审批 ---");
        crawler.crawlDevicePMA("decision_date:[20230101 TO 20231231]", 100, 50, "device_pma_2023.json");
        
        // 4.3 查询特定制造商的设备上市前审批
        System.out.println("--- 查询Johnson & Johnson的设备上市前审批 ---");
        crawler.crawlDevicePMA("applicant:\"Johnson & Johnson\"", 60, 30, "device_pma_johnson.json");
        
        // 4.4 复杂查询：2023年批准的特定制造商设备上市前审批
        System.out.println("--- 查询2023年批准的Johnson设备上市前审批 ---");
        crawler.crawlDevicePMA("decision_code:APPR AND decision_date:[20230101 TO 20231231] AND applicant:\"Johnson\"", 40, 20, "device_pma_approved_2023_johnson.json");
        
        // 解析并分析设备上市前审批数据
        System.out.println("\n=== 解析并分析设备上市前审批数据 ===");
        SimpleJsonAnalyzer.analyzeDevicePMA("device_pma_approved.json");
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 5. 爬取设备510k审批数据
        System.out.println("=== 开始爬取设备510k审批数据 ===");
        
        // 5.1 基本查询：心血管咨询委员会的510k审批
        System.out.println("--- 查询心血管咨询委员会的510k审批 ---");
        crawler.crawlDevice510K("cv.advisory_committee:cv", 80, 40, "device_510k_cv_committee.json");
        
        // 5.2 查询特定年份的510k审批
        System.out.println("--- 查询2023年的510k审批 ---");
        crawler.crawlDevice510K("decision_date:[20230101 TO 20231231]", 100, 50, "device_510k_2023.json");
        
        // 5.3 查询特定制造商的510k审批
        System.out.println("--- 查询Medtronic的510k审批 ---");
        crawler.crawlDevice510K("applicant:\"Medtronic\"", 60, 30, "device_510k_medtronic.json");
        
        // 5.4 复杂查询：2023年心血管咨询委员会的510k审批
        System.out.println("--- 查询2023年心血管咨询委员会的510k审批 ---");
        crawler.crawlDevice510K("cv.advisory_committee:cv AND decision_date:[20230101 TO 20231231]", 50, 25, "device_510k_cv_2023.json");
        
        // 解析并分析设备510k审批数据
        System.out.println("\n=== 解析并分析设备510k审批数据 ===");
        SimpleJsonAnalyzer.analyzeDevice510K("device_510k_medtronic.json");
        
        System.out.println("\n=== 所有数据爬取完成 ===");
    }
}
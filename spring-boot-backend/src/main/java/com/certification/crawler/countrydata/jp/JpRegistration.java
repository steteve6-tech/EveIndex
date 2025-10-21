package com.certification.crawler.countrydata.jp;

import com.certification.analysis.analysisByai.TranslateAI;
import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * 日本PMDA医疗器械注册记录爬虫
 * 数据源: https://www.pmda.go.jp/PmdaSearch/kikiSearch
 */
@Slf4j
@Component
public class JpRegistration {

    private static final String BASE_URL = "https://www.pmda.go.jp/PmdaSearch/kikiSearch";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 30000;
    private static final int MAX_RETRIES = 3;
    private static final int BATCH_SIZE = 50;

    @Autowired
    private DeviceRegistrationRecordRepository registrationRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 日本注册数据模型
     * 字段说明（根据PMDA搜索结果页面的列）：
     * - approvalNumber: 承认番号（approval number）- 注册编号
     * - deviceName: 一般的名称（generic name）- 设备通用名称  
     * - salesName: 販売名（sales name）- 销售名称/商标名
     * - manufacturer: 製造販売業者（manufacturer）- 制造销售商
     * - approvalDate: 承認年月日（approval date）- 批准日期
     * - category: 分類（category）- 设备分类
     * - effectPurpose: 使用目的（effect/purpose）- 使用目的
     * - cautions: 警告·禁忌（cautions）- 警告和禁忌
     * - detailUrl: 详情链接
     */
    @Data
    public static class JapanRegistrationData {
        private String approvalNumber;     // 承认番号（列1）
        private String deviceName;         // 一般的名称（列2）
        private String salesName;          // 販売名（列6）
        private String manufacturer;       // 製造販売業者（列7）
        private LocalDate approvalDate;    // 承認年月日
        private String category;           // 分類
        private String effectPurpose;      // 使用目的（列13）
        private String cautions;           // 警告·禁忌（列14）
        private String detailUrl;          // 详情链接
    }

    /**
     * 基于关键词搜索爬取
     * 
     * @param keyword 搜索关键词（如：skin, heart, dental等）
     * @param maxRecords 最大记录数（默认100）
     * @return 爬取结果消息
     */
    public String crawlByKeyword(String keyword, int maxRecords) {
        log.info("📝 开始爬取日本PMDA注册记录，关键词: {}, 最大记录数: {}", keyword, maxRecords);
        
        try {
            // 构建请求参数
            Map<String, String> formData = buildSearchParams(keyword, maxRecords);
            
            // 发送POST请求
            Document doc = fetchSearchResults(formData);
            
            // 解析数据
            List<JapanRegistrationData> dataList = parseSearchResults(doc);
            
            if (dataList.isEmpty()) {
                log.warn("未找到任何注册记录，关键词: {}", keyword);
                return "未找到任何注册记录";
            }
            
            log.info("📊 日本注册记录爬取完成，共获取 {} 条数据", dataList.size());
            
            // 保存到数据库
            return saveToDatabase(dataList);
            
        } catch (Exception e) {
            log.error("爬取日本注册记录失败，关键词: {}", keyword, e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 基于公司名称搜索
     * 
     * @param companyName 公司名称（日文，如：株式会社Xenoma）
     * @param maxRecords 最大记录数
     * @return 爬取结果消息
     */
    public String crawlByCompany(String companyName, int maxRecords) {
        log.info("📝 开始爬取日本PMDA注册记录，公司名称: {}, 最大记录数: {}", companyName, maxRecords);
        
        try {
            // 构建请求参数（使用公司名称搜索）
            Map<String, String> formData = buildSearchParamsForCompany(companyName, maxRecords);
            
            // 发送POST请求
            Document doc = fetchSearchResults(formData);
            
            // 解析数据
            List<JapanRegistrationData> dataList = parseSearchResults(doc);
            
            if (dataList.isEmpty()) {
                log.warn("未找到任何注册记录，公司: {}", companyName);
                return "未找到任何注册记录";
            }
            
            log.info("📊 日本注册记录爬取完成，共获取 {} 条数据", dataList.size());
            
            // 保存到数据库
            return saveToDatabase(dataList);
            
        } catch (Exception e) {
            log.error("爬取日本注册记录失败，公司: {}", companyName, e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 构建搜索参数（关键词搜索）
     */
    private Map<String, String> buildSearchParams(String keyword, int maxRecords) {
        Map<String, String> params = new LinkedHashMap<>();
        
        // 基础参数
        params.put("nccharset", "A0D3C532");
        params.put("ListRows", String.valueOf(maxRecords));
        params.put("btnA.x", "78");
        params.put("btnA.y", "20");
        
        // 搜索关键词
        params.put("nameWord", keyword);
        params.put("targetBothWithItemRadioValue", "1");
        params.put("kikiXmlHowtoNameSearchRadioValue", "1_0");
        params.put("howtoMatchRadioValue", "1");
        
        // 显示列设置（选择需要显示的列）
        params.put("tglOpFlg", "");
        params.put("dispColumnsList[0]", "1");   // 承認番号
        params.put("_dispColumnsList[0]", "on");
        params.put("dispColumnsList[1]", "2");   // 一般的名称
        params.put("_dispColumnsList[1]", "on");
        params.put("dispColumnsList[2]", "6");   // 販売名
        params.put("_dispColumnsList[2]", "on");
        params.put("dispColumnsList[3]", "7");   // 製造販売業者
        params.put("_dispColumnsList[3]", "on");
        params.put("_dispColumnsList[4]", "on");
        params.put("_dispColumnsList[5]", "on");
        params.put("_dispColumnsList[6]", "on");
        params.put("_dispColumnsList[7]", "on");
        params.put("_dispColumnsList[8]", "on");
        params.put("dispColumnsList[9]", "13");  // 使用目的
        params.put("_dispColumnsList[9]", "on");
        params.put("dispColumnsList[10]", "14"); // 警告·禁忌
        params.put("_dispColumnsList[10]", "on");
        
        // 其他必需参数
        addCommonParams(params);
        
        return params;
    }

    /**
     * 构建搜索参数（公司名称搜索）
     */
    private Map<String, String> buildSearchParamsForCompany(String companyName, int maxRecords) {
        Map<String, String> params = buildSearchParams("", maxRecords);
        
        // 设置公司名称搜索
        params.put("compName", "5_0");
        params.put("txtCompName", companyName);
        
        return params;
    }

    /**
     * 添加通用参数
     */
    private void addCommonParams(Map<String, String> params) {
        params.put("category", "");
        params.put("txtEffect", "");
        params.put("txtEffectHowtoSearch", "and");
        params.put("cautions", "");
        params.put("cautionsHowtoSearch", "and");
        params.put("updateDocFrDt", "年月日 [YYYYMMDD]");
        params.put("updateDocToDt", "年月日 [YYYYMMDD]");
        params.put("txtApproval", "");
        params.put("barcode", "");
        params.put("txtBarcode", "");
        params.put("kikiXmlBarcodeRadioValue", "3_0");
        params.put("txtNameOfCountry", "");
        params.put("koumoku1Value", "");
        params.put("koumoku1Word", "");
        params.put("koumoku1HowtoSearch", "and");
        params.put("koumoku2Value", "");
        params.put("koumoku2Word", "");
        params.put("koumoku2HowtoSearch", "and");
        params.put("koumoku3Value", "");
        params.put("koumoku3Word", "");
        params.put("koumoku3HowtoSearch", "and");
        params.put("gs1code", "");
        params.put("howtoRdSearchSel", "or");
        
        // 关联文档参数
        addRelationDocParams(params);
        
        params.put("listCategory", "");
    }

    /**
     * 添加关联文档参数
     */
    private void addRelationDocParams(Map<String, String> params) {
        // 关联文档1
        params.put("relationDoc1Sel", "");
        params.put("relationDoc1check1", "on");
        params.put("_relationDoc1check1", "on");
        params.put("relationDoc1check2", "on");
        params.put("_relationDoc1check2", "on");
        params.put("relationDoc1Word", "検索語を入力");
        params.put("relationDoc1HowtoSearch", "and");
        params.put("relationDoc1FrDt", "年月 [YYYYMM]");
        params.put("relationDoc1ToDt", "年月 [YYYYMM]");
        params.put("relationDocHowtoSearchBetween12", "and");
        
        // 关联文档2
        params.put("relationDoc2Sel", "");
        params.put("relationDoc2check1", "on");
        params.put("_relationDoc2check1", "on");
        params.put("relationDoc2check2", "on");
        params.put("_relationDoc2check2", "on");
        params.put("relationDoc2Word", "検索語を入力");
        params.put("relationDoc2HowtoSearch", "and");
        params.put("relationDoc2FrDt", "年月 [YYYYMM]");
        params.put("relationDoc2ToDt", "年月 [YYYYMM]");
        params.put("relationDocHowtoSearchBetween23", "and");
        
        // 关联文档3
        params.put("relationDoc3Sel", "");
        params.put("relationDoc3check1", "on");
        params.put("_relationDoc3check1", "on");
        params.put("relationDoc3check2", "on");
        params.put("_relationDoc3check2", "on");
        params.put("relationDoc3Word", "検索語を入力");
        params.put("relationDoc3HowtoSearch", "and");
        params.put("relationDoc3FrDt", "年月 [YYYYMM]");
        params.put("relationDoc3ToDt", "年月 [YYYYMM]");
    }

    /**
     * 发送POST请求获取搜索结果
     */
    private Document fetchSearchResults(Map<String, String> formData) throws IOException {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("发送搜索请求，尝试 {}/{}", attempt, MAX_RETRIES);
                
                Connection.Response response = Jsoup.connect(BASE_URL)
                        .method(Connection.Method.POST)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Referer", "https://www.pmda.go.jp/PmdaSearch/kikiSearch/")
                        .data(formData)
                        .timeout(TIMEOUT)
                        .execute();
                
                Document doc = response.parse();
                
                log.debug("搜索请求成功，状态码: {}", response.statusCode());
                return doc;
                
            } catch (IOException e) {
                log.warn("搜索请求失败，尝试 {}/{}，错误: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                // 等待后重试
                try {
                    Thread.sleep(2000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("重试被中断", ie);
                }
            }
        }
        throw new IOException("搜索请求失败，已达到最大重试次数");
    }

    /**
     * 解析搜索结果页面
     */
    private List<JapanRegistrationData> parseSearchResults(Document doc) {
        List<JapanRegistrationData> dataList = new ArrayList<>();
        
        try {
            // 查找结果表格
            Element table = doc.select("table.list-table, table[class*=result], table[border='1']").first();
            
            if (table == null) {
                log.warn("未找到结果表格");
                // 尝试其他选择器
                table = doc.select("table").stream()
                        .filter(t -> t.select("tr").size() > 1)
                        .findFirst()
                        .orElse(null);
            }
            
            if (table == null) {
                log.error("无法找到数据表格，页面结构可能已更改");
                return dataList;
            }
            
            // 获取所有数据行（跳过表头）
            Elements rows = table.select("tr");
            
            if (rows.size() <= 1) {
                log.warn("表格中没有数据行");
                return dataList;
            }
            
            log.debug("找到 {} 行数据（包含表头）", rows.size());
            
            // 从第2行开始解析（跳过表头）
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                try {
                    JapanRegistrationData data = parseTableRow(row, i);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.error("解析第 {} 行数据失败: {}", i, e.getMessage());
                }
            }
            
            log.info("成功解析 {} 条注册记录", dataList.size());
            
        } catch (Exception e) {
            log.error("解析搜索结果失败", e);
        }
        
        return dataList;
    }

    /**
     * 解析表格行
     * 
     * 列顺序（根据dispColumnsList设置）：
     * 0: 承認番号（approval number）
     * 1: 一般的名称（generic name）
     * 2: 販売名（sales name）
     * 3: 製造販売業者（manufacturer）
     * 4-8: 其他可选列
     * 9: 使用目的（effect/purpose）
     * 10: 警告·禁忌（cautions）
     */
    private JapanRegistrationData parseTableRow(Element row, int rowIndex) {
        Elements cols = row.select("td");
        
        if (cols.isEmpty()) {
            log.debug("第 {} 行没有td元素，跳过", rowIndex);
            return null;
        }
        
        JapanRegistrationData data = new JapanRegistrationData();
        
        try {
            // 列0: 承認番号（approval number）- 作为唯一标识
            if (cols.size() > 0) {
                String approvalNumber = cols.get(0).text().trim();
                data.setApprovalNumber(approvalNumber);
                
                // 尝试提取详情链接
                Element link = cols.get(0).selectFirst("a");
                if (link != null && link.hasAttr("href")) {
                    String detailUrl = link.attr("abs:href");
                    data.setDetailUrl(detailUrl);
                }
            }
            
            // 列1: 一般的名称（generic name）
            if (cols.size() > 1) {
                String deviceName = cols.get(1).text().trim();
                data.setDeviceName(deviceName);
            }
            
            // 列2: 販売名（sales name）
            if (cols.size() > 2) {
                String salesName = cols.get(2).text().trim();
                data.setSalesName(salesName);
            }
            
            // 列3: 製造販売業者（manufacturer）
            if (cols.size() > 3) {
                String manufacturer = cols.get(3).text().trim();
                data.setManufacturer(manufacturer);
            }
            
            // 列9: 使用目的（effect/purpose）- 如果有足够的列
            if (cols.size() > 9) {
                String effectPurpose = cols.get(9).text().trim();
                data.setEffectPurpose(effectPurpose);
            }
            
            // 列10: 警告·禁忌（cautions）- 如果有足够的列
            if (cols.size() > 10) {
                String cautions = cols.get(10).text().trim();
                data.setCautions(cautions);
            }
            
            // 验证必填字段
            if (data.getApprovalNumber() == null || data.getApprovalNumber().isEmpty()) {
                log.warn("第 {} 行缺少承認番号，跳过", rowIndex);
                return null;
            }
            
            log.debug("成功解析第 {} 行: 承認番号={}, 一般的名称={}", 
                    rowIndex, data.getApprovalNumber(), data.getDeviceName());
            
            return data;
            
        } catch (Exception e) {
            log.error("解析第 {} 行失败: {}", rowIndex, e.getMessage());
            return null;
        }
    }

    /**
     * 保存到数据库
     */
    private String saveToDatabase(List<JapanRegistrationData> dataList) {
        int totalSaved = 0;
        int totalDuplicates = 0;
        int batchCount = 0;

        log.info("开始保存 {} 条注册记录到数据库", dataList.size());

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);

        // 分批处理
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, dataList.size());
            List<JapanRegistrationData> batch = dataList.subList(i, end);
            batchCount++;

            int saved = 0;
            int duplicates = 0;

            for (JapanRegistrationData data : batch) {
                try {
                    DeviceRegistrationRecord entity = convertToEntity(data);

                    // 检查是否已存在（使用承認番号作为唯一标识）
                    Optional<DeviceRegistrationRecord> existing =
                            registrationRepository.findByRegistrationNumberAndDataSource(
                                    entity.getRegistrationNumber(),
                                    entity.getDataSource());

                    if (existing.isEmpty()) {
                        registrationRepository.save(entity);
                        saved++;
                    } else {
                        duplicates++;
                        log.debug("记录已存在，跳过: {}", data.getApprovalNumber());
                    }

                } catch (Exception e) {
                    log.error("保存记录失败: {}, 错误: {}", data.getApprovalNumber(), e.getMessage());
                }
            }

            totalSaved += saved;
            totalDuplicates += duplicates;

            log.info("第 {} 批次保存完成，新增: {} 条，重复: {} 条", batchCount, saved, duplicates);

            // 批次检测：检查是否应该停止
            boolean shouldStop = detector.recordBatch(batch.size(), saved);
            if (shouldStop) {
                log.warn("⚠️ 检测到连续重复批次，停止保存剩余数据");
                break;
            }
        }

        // 打印最终统计
        detector.printFinalStats("JpRegistration");

        String resultMessage = String.format(
                "✅ 日本注册记录保存完成！总计: %d 条，新增: %d 条，重复: %d 条",
                dataList.size(), totalSaved, totalDuplicates);

        log.info(resultMessage);
        return resultMessage;
    }

    /**
     * 转换为实体对象
     * 
     * 字段映射：
     * - approvalNumber → registrationNumber (注册编号)
     * - deviceName → deviceName (设备通用名称)
     * - salesName → proprietaryName (商标名/销售名)
     * - manufacturer → manufacturerName (制造商名称)
     * - effectPurpose → remark (使用目的，存储到备注)
     * - cautions → remark (警告禁忌，追加到备注)
     */
    private DeviceRegistrationRecord convertToEntity(JapanRegistrationData src) {
        DeviceRegistrationRecord entity = new DeviceRegistrationRecord();
        
        // 数据源标识
        entity.setDataSource("日本PMDA 医薬品医療機器総合機構");
        entity.setJdCountry("JP");
        
        // 核心标识字段
        entity.setRegistrationNumber(src.getApprovalNumber());
        // feiNumber字段留空（PMDA没有对应的字段）
        
        // 制造商信息（翻译）
        String translatedManufacturer = translateText(src.getManufacturer());
        entity.setManufacturerName(translatedManufacturer);
        
        // 设备信息（翻译）
        String translatedDeviceName = translateText(src.getDeviceName());
        entity.setDeviceName(translatedDeviceName);
        
        String translatedSalesName = translateText(src.getSalesName());
        entity.setProprietaryName(translatedSalesName);
        
        // 状态信息
        entity.setStatusCode("APPROVED");  // PMDA搜索结果都是已批准的
        
        // 批准日期
        if (src.getApprovalDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            entity.setCreatedDate(src.getApprovalDate().format(formatter));
        }
        
        // 使用目的和警告信息存储到备注
        StringBuilder remarkBuilder = new StringBuilder();
        
        if (src.getEffectPurpose() != null && !src.getEffectPurpose().isEmpty()) {
            String translatedPurpose = translateText(src.getEffectPurpose());
            remarkBuilder.append("使用目的: ").append(translatedPurpose);
        }
        
        if (src.getCautions() != null && !src.getCautions().isEmpty()) {
            if (remarkBuilder.length() > 0) {
                remarkBuilder.append("\n\n");
            }
            String translatedCautions = translateText(src.getCautions());
            remarkBuilder.append("警告·禁忌: ").append(translatedCautions);
        }
        
        if (src.getDetailUrl() != null && !src.getDetailUrl().isEmpty()) {
            if (remarkBuilder.length() > 0) {
                remarkBuilder.append("\n\n");
            }
            remarkBuilder.append("详情链接: ").append(src.getDetailUrl());
        }
        
        if (remarkBuilder.length() > 0) {
            entity.setRemark(remarkBuilder.toString());
        }
        
        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setKeywords(null);
        entity.setDataStatus("ACTIVE");
        entity.setCrawlTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        return entity;
    }

    /**
     * 翻译文本（日文→英文）
     */
    private String translateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            // 使用TranslateAI翻译（日文→英文）
            String translated = translateAI.translateSingleText(text, "ja", "en");
            
            // 如果翻译成功且不等于原文，返回"原文Translation"格式
            if (translated != null && !translated.equals(text)) {
                log.debug("翻译完成: {} -> {}", text, translated);
                return text + translated;  // 原文 + 译文
            }
            
            return text;
        } catch (Exception e) {
            log.warn("翻译失败，使用原文: {}, 错误: {}", text, e.getMessage());
            return text;
        }
    }

    /**
     * 测试方法 - 搜索关键词
     */
    public static void main(String[] args) {
        System.out.println("=== 日本PMDA注册记录爬虫测试 ===");
        System.out.println("示例用法：");
        System.out.println("1. 按关键词搜索：crawlByKeyword(\"skin\", 100)");
        System.out.println("2. 按公司搜索：crawlByCompany(\"株式会社Xenoma\", 100)");
    }
}


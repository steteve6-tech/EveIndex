package com.certification.crawler.countrydata.jp;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;
import com.certification.analysis.analysisByai.TranslateAI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * 日本PMDA医疗器械召回记录爬虫
 * 数据源: https://www.info.pmda.go.jp/rsearch/
 */
@Slf4j
@Component
public class JpRecall {

    private static final String BASE_URL = "https://www.info.pmda.go.jp/rsearch/PackinsSearch";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private DeviceRecallRecordRepository recallRecordRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 日本召回数据模型
     * 字段说明：
     * - recallNumber: 召回编号（自动生成或从detailUrl获取）
     * - productName: 产品描述（第6列）
     * - seller: 设备名称（第7列，注意：虽然命名为seller，但实际存储的是设备名称）
     * - manufacturer: 制造商（第8列）
     * - recallDate: 召回日期（第4列）
     * - recallClass: 召回等级（第1列）
     * - recallReason: 召回原因（第9列，如果存在）
     * - detailUrl: 详情链接（从第6列的链接提取）
     */
    @Data
    public static class JapanRecallData {
        private String recallNumber;      // 召回编号
        private String productName;       // 产品描述（第6列）
        private String manufacturer;      // 制造商（第8列）
        private String seller;            // 设备名称（第7列，实际是设备名称而非贩卖商）
        private LocalDate recallDate;     // 召回日期（第4列）
        private String recallReason;      // 召回原因（第9列）
        private String recallClass;       // 召回等级（第1列）
        private String detailUrl;         // 详情链接
    }

    /**
     * 基于多字段参数爬取
     */
    public String crawlWithMultipleFields(
            List<String> sellers,
            List<String> manufacturers,
            List<String> years,
            int maxRecords,
            int batchSize,
            String dateFrom,
            String dateTo
    ) throws Exception {
        log.info("🚀 开始基于多字段参数爬取日本召回数据");
        log.info("📊 贩卖商数量: {}, 制造商数量: {}, 年份数量: {}", 
            sellers != null ? sellers.size() : 0,
            manufacturers != null ? manufacturers.size() : 0,
            years != null ? years.size() : 0);

        List<JapanRecallData> allData = new ArrayList<>();
        
        // 1. 优先使用贩卖商字段
        if (sellers != null && !sellers.isEmpty()) {
            for (String seller : sellers) {
                log.info("🔍 使用贩卖商: {}", seller);
                List<JapanRecallData> data = crawlRecallData(seller, null, null, maxRecords, batchSize);
                allData.addAll(data);
                if (maxRecords > 0 && allData.size() >= maxRecords) {
                    break;
                }
            }
        }
        // 2. 其次使用制造商字段
        else if (manufacturers != null && !manufacturers.isEmpty()) {
            for (String manufacturer : manufacturers) {
                log.info("🔍 使用制造商: {}", manufacturer);
                List<JapanRecallData> data = crawlRecallData(null, manufacturer, null, maxRecords, batchSize);
                allData.addAll(data);
                if (maxRecords > 0 && allData.size() >= maxRecords) {
                    break;
                }
            }
        }
        // 3. 最后使用年份字段
        else if (years != null && !years.isEmpty()) {
            for (String year : years) {
                log.info("🔍 使用年份: {}", year);
                List<JapanRecallData> data = crawlRecallData(null, null, year, maxRecords, batchSize);
                allData.addAll(data);
                if (maxRecords > 0 && allData.size() >= maxRecords) {
                    break;
                }
            }
        }
        // 4. 如果都没有，执行默认搜索
        else {
            log.info("🔍 执行默认搜索");
            allData = crawlRecallData(null, null, null, maxRecords, batchSize);
        }

        return saveToDatabase(allData, batchSize);
    }

    /**
     * 爬取召回数据
     */
    private List<JapanRecallData> crawlRecallData(
            String seller,
            String manufacturer,
            String year,
            int maxRecords,
            int batchSize
    ) throws Exception {
        
        List<JapanRecallData> allData = new ArrayList<>();
        Set<String> processedRecallNumbers = new HashSet<>();
        
        int start = 1;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords <= 0);
        
        int consecutiveEmptyPages = 0;
        int maxEmptyPages = 3;

        while (crawlAll || totalFetched < maxRecords) {
            try {
                log.info("📄 正在爬取第 {} 页（start={}）", (start - 1) / 10 + 1, start);
                
                String url = buildUrl(seller, manufacturer, year, start);
                log.debug("请求URL: {}", url);
                
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Referer", "https://www.info.pmda.go.jp/rsearch/html/menu_recall_kensaku.html")
                        .timeout(30000)
                        .get();

                List<JapanRecallData> pageData = parseRecallData(doc);
                
                if (pageData.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.info("第 {} 页没有数据，连续空页面: {}/{}", (start - 1) / 10 + 1, consecutiveEmptyPages, maxEmptyPages);
                    
                    if (consecutiveEmptyPages >= maxEmptyPages) {
                        log.info("连续 {} 页无数据，停止爬取", maxEmptyPages);
                        break;
                    }
                    
                    start += 10;
                    Thread.sleep(2000);
                    continue;
                }
                
                consecutiveEmptyPages = 0;
                
                int newDataCount = 0;
                for (JapanRecallData data : pageData) {
                    String uniqueId = data.getRecallNumber();
                    if (uniqueId == null || uniqueId.isEmpty()) {
                        uniqueId = data.getProductName() + "_" + data.getManufacturer();
                    }
                    
                    if (!processedRecallNumbers.contains(uniqueId)) {
                        processedRecallNumbers.add(uniqueId);
                        allData.add(data);
                        newDataCount++;
                        
                        if (!crawlAll && allData.size() >= maxRecords) {
                            break;
                        }
                    }
                }
                
                log.info("第 {} 页去重后: 新增 {} 条", (start - 1) / 10 + 1, newDataCount);
                
                totalFetched = allData.size();
                
                if (!crawlAll && totalFetched >= maxRecords) {
                    log.info("已达到最大记录数 {}，停止爬取", maxRecords);
                    break;
                }
                
                if (pageData.size() < 10) {
                    log.info("当前页数据少于10条，可能是最后一页");
                    break;
                }
                
                start += 10;
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("爬取第 {} 页时出错: {}", (start - 1) / 10 + 1, e.getMessage());
                start += 10;
                Thread.sleep(3000);
            }
        }

        log.info("📊 日本召回数据爬取完成，共获取 {} 条数据", allData.size());
        return allData;
    }

    /**
     * 构建请求URL
     */
    private String buildUrl(String seller, String manufacturer, String year, int start) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?nccharset=9FCD2B9D");
        url.append("&txtSaleName=");
        if (seller != null && !seller.trim().isEmpty()) {
            url.append(encodeShiftJIS(seller.trim()));
        }
        url.append("&rdoWhichName=name");
        url.append("&rdoMatch=false");
        url.append("&cboType=4");
        url.append("&txtCompName=");
        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            url.append(encodeShiftJIS(manufacturer.trim()));
        }
        url.append("&cboYear=");
        if (year != null && !year.trim().isEmpty()) {
            url.append(year.trim());
        }
        url.append("&cboClass=");
        url.append("&txtFullText=");
        url.append("&cboIsHold=1");
        url.append("&IsEnd=1");
        url.append("&btnSearch=%B8%A1%BA%F7%BC%C2%B9%D4");
        url.append("&cboDisCnt=10");
        url.append("&start=").append(start);
        
        return url.toString();
    }

    /**
     * 解析召回数据
     */
    private List<JapanRecallData> parseRecallData(Document doc) {
        List<JapanRecallData> dataList = new ArrayList<>();
        
        try {
            // 打印HTML结构用于调试
            log.debug("========== HTML内容调试开始 ==========");
            log.debug("页面标题: {}", doc.title());
            
            // 查找所有可能的表格
            Elements allTables = doc.select("table");
            log.debug("找到 {} 个表格", allTables.size());
            
            for (int i = 0; i < allTables.size(); i++) {
                Element table = allTables.get(i);
                log.debug("表格 {}: class={}, id={}, 行数={}", 
                    i, table.attr("class"), table.attr("id"), table.select("tr").size());
            }
            
            // 尝试多种选择器
            Elements rows = null;
            String[] selectors = {
                "table.resultTbl tr",           // 原始选择器
                "table[summary*='検索結果'] tr", // 带summary属性的表格
                "table tr",                      // 所有表格
                "form table tr",                 // form内的表格
                ".resultTbl tr",                 // class为resultTbl
                "#resultTbl tr"                  // id为resultTbl
            };
            
            for (String selector : selectors) {
                rows = doc.select(selector);
                if (rows.size() > 1) { // 至少有表头和一行数据
                    log.info("使用选择器 '{}' 找到 {} 行数据（包含表头）", selector, rows.size());
                    break;
                }
            }
            
            // 如果还是没找到，打印整个页面内容（前2000字符）
            if (rows == null || rows.size() <= 1) {
                log.warn("所有选择器都未找到数据表格");
                String bodyHtml = doc.body().html();
                if (bodyHtml.length() > 2000) {
                    log.debug("页面内容预览（前2000字符）:\n{}", bodyHtml.substring(0, 2000));
                } else {
                    log.debug("页面完整内容:\n{}", bodyHtml);
                }
                log.debug("========== HTML内容调试结束 ==========");
                return dataList;
            }
            
            log.debug("========== HTML内容调试结束 ==========");
            
            // 解析数据行
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                try {
                    JapanRecallData data = parseRow(row);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("解析行数据时出错: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("解析召回数据时出错: {}", e.getMessage(), e);
        }
        
        return dataList;
    }

    /**
     * 解析单行数据
     * 新的表格结构（至少8列）：
     * 第1列: recallClass (召回等级)
     * 第4列: recallDate (召回日期)
     * 第6列: product_description (产品描述)
     * 第7列: devicename (设备名称)
     * 第8列: manufacturer (制造商)
     */
    private JapanRecallData parseRow(Element row) {
        Elements cells = row.select("td");
        log.debug("当前行有 {} 列，内容预览: {}", cells.size(), 
            cells.size() > 0 ? cells.get(0).text().substring(0, Math.min(50, cells.get(0).text().length())) : "空");
        
        if (cells.size() < 8) {
            log.warn("行数据列数不足8列，实际: {} 列，跳过该行", cells.size());
            return null;
        }
        
        JapanRecallData data = new JapanRecallData();
        
        try {
            // 第1列: 召回分类 (recallClass)
            data.setRecallClass(cleanText(cells.get(0).text()));
            
            // 第4列: 召回日期 (recallDate)
            data.setRecallDate(parseDate(cleanText(cells.get(3).text())));
            
            // 第6列: 产品描述 (product_description)
            Element productDescCell = cells.get(5);
            Element link = productDescCell.selectFirst("a");
            if (link != null) {
                data.setProductName(cleanText(link.text()));
                String href = link.attr("href");
                if (href != null && !href.isEmpty()) {
                    if (href.startsWith("http")) {
                        data.setDetailUrl(href);
                    } else {
                        data.setDetailUrl("https://www.info.pmda.go.jp/rsearch/" + href);
                    }
                }
            } else {
                data.setProductName(cleanText(productDescCell.text()));
            }
            
            // 第7列: 设备名称 (devicename) - 作为seller存储
            data.setSeller(cleanText(cells.get(6).text()));
            
            // 第8列: 制造商 (manufacturer)
            data.setManufacturer(cleanText(cells.get(7).text()));
            
            // 如果还有第9列或其他列，可以作为召回原因
            if (cells.size() > 8) {
                data.setRecallReason(cleanText(cells.get(8).text()));
            }
            
            // 生成召回编号（使用详情URL作为唯一标识，如果没有则生成）
            if (data.getDetailUrl() != null && !data.getDetailUrl().isEmpty()) {
                data.setRecallNumber(data.getDetailUrl());
            } else if (data.getRecallNumber() == null || data.getRecallNumber().isEmpty()) {
                data.setRecallNumber("JP_RECALL_" + 
                    (data.getRecallDate() != null ? data.getRecallDate().toString().replace("-", "") : "NODATE") + "_" +
                    (data.getProductName() != null ? data.getProductName().hashCode() : System.currentTimeMillis()));
            }
            
            log.debug("✅ 成功解析行数据: 召回等级={}, 日期={}, 产品={}, 制造商={}", 
                data.getRecallClass(), data.getRecallDate(), 
                data.getProductName(), data.getManufacturer());
            
        } catch (Exception e) {
            log.warn("解析行数据时出错: {}", e.getMessage(), e);
            return null;
        }
        
        return data;
    }

    /**
     * 保存到数据库
     */
    @org.springframework.transaction.annotation.Transactional
    private String saveToDatabase(List<JapanRecallData> dataList, int batchSize) {
        if (dataList.isEmpty()) {
            return "没有爬取到数据";
        }

        log.info("成功爬取到 {} 条召回数据，开始保存到数据库", dataList.size());

        int totalSaved = 0;
        int totalDuplicates = 0;

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);
        int currentBatchSaved = 0;
        int processedInBatch = 0;

        for (JapanRecallData data : dataList) {
            try {
                DeviceRecallRecord entity = convertToEntity(data);

                if (recallRecordRepository.existsByCfresId(entity.getCfresId())) {
                    totalDuplicates++;
                } else {
                    recallRecordRepository.save(entity);
                    totalSaved++;
                    currentBatchSaved++;
                }

                processedInBatch++;

                // 每batchSize条检查一次
                if (processedInBatch >= batchSize) {
                    boolean shouldStop = detector.recordBatch(processedInBatch, currentBatchSaved);
                    if (shouldStop) {
                        log.warn("⚠️ 检测到连续重复批次，停止保存");
                        break;
                    }
                    // 重置批次计数
                    processedInBatch = 0;
                    currentBatchSaved = 0;
                }
            } catch (Exception e) {
                log.error("保存召回数据时出错: {}", e.getMessage());
            }
        }

        // 打印最终统计
        detector.printFinalStats("JpRecall");

        log.info("保存完成，新增: {} 条，重复: {} 条", totalSaved, totalDuplicates);
        return String.format("日本召回数据保存完成，新增: %d 条，重复: %d 条", totalSaved, totalDuplicates);
    }

    /**
     * 转换为实体对象
     * 字段映射说明：
     * - productName (第6列) → productDescription 产品描述
     * - seller (第7列，实际是设备名称) → deviceName 设备名称
     * - manufacturer (第8列) → recallingFirm 召回公司/制造商
     * - recallClass (第1列) → recallStatus 召回等级
     * - recallDate (第4列) → eventDatePosted 召回发布日期
     * - detailUrl → cfresId 召回事件ID（唯一标识）
     * - recallReason (第9列) → remark 备注
     */
    private DeviceRecallRecord convertToEntity(JapanRecallData src) {
        DeviceRecallRecord entity = new DeviceRecallRecord();
        
        // 使用详情URL作为唯一ID，如果没有则使用生成的召回编号
        entity.setCfresId(src.getRecallNumber());
        
        // 召回日期
        entity.setEventDatePosted(src.getRecallDate());
        
        // 数据源
        entity.setDataSource("日本PMDA 医薬品医療機器総合機構");
        
        // 第6列: 产品描述 (翻译)
        String translatedProductDesc = translateText(src.getProductName());
        entity.setProductDescription(translatedProductDesc);
        
        // 第7列: 设备名称 (翻译)
        String translatedDeviceName = translateText(src.getSeller());
        entity.setDeviceName(translatedDeviceName);
        
        // 第8列: 制造商 (翻译)
        String translatedManufacturer = translateText(src.getManufacturer());
        entity.setRecallingFirm(translatedManufacturer);
        
        // 第1列: 召回等级 (原文)
        entity.setRecallStatus(src.getRecallClass());
        
        // 产品代码暂时留空，或者使用召回等级
        entity.setProductCode(src.getRecallClass());
        
        // 召回原因存储到备注字段
        if (src.getRecallReason() != null && !src.getRecallReason().trim().isEmpty()) {
            String translatedReason = translateText(src.getRecallReason());
            entity.setRemark("召回原因: " + translatedReason);
        }
        
        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setKeywords(null);
        entity.setJdCountry("JP");
        entity.setDataStatus("ACTIVE");
        entity.setCountryCode("JP");
        entity.setCrawlTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        return entity;
    }

    /**
     * 翻译文本
     */
    private String translateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // 使用火山引擎翻译服务（日语->英语）
            return translateAI.translateAndAppend(text, "ja");
        } catch (Exception e) {
            log.warn("翻译失败，返回原文: {}", e.getMessage());
            return text;
        }
    }

    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            dateStr = dateStr.trim()
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "")
                .replace("/", "-");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }

    /**
     * 清理文本
     */
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * 编码为Shift-JIS格式
     */
    private String encodeShiftJIS(String text) {
        try {
            return java.net.URLEncoder.encode(text, "Shift_JIS");
        } catch (Exception e) {
            log.warn("Shift-JIS编码失败，使用UTF-8: {}", text);
            return java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}

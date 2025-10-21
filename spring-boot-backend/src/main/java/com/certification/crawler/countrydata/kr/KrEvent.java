package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.DeviceEventReport;
import com.certification.repository.common.DeviceEventReportRepository;
import com.certification.analysis.analysisByai.TranslateAI;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 韩国医疗器械不良事件数据爬虫
 * 数据来源：韩国食品药品安全处 (MFDS - Ministry of Food and Drug Safety)
 * API地址：https://emedi.mfds.go.kr/abcs/list/MNU20268
 */
@Slf4j
@Component
public class KrEvent {

    private static final String BASE_URL = "https://emedi.mfds.go.kr/abcs/list/MNU20268";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private DeviceEventReportRepository eventReportRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 韩国不良事件数据模型
     * 注意：MFDS网站的不良事件表格中不包含日期字段
     */
    public static class KoreaEventData {
        private String reportNumber;       // 보고번호 (报告编号)
        private String companyName;        // 제조/수입업체명 (企业名称)
        private String productName;        // 제품명 (产品名称)
        private String modelName;          // 모델명 (型号名称)
        private String eventType;          // 사례유형 (事件类型)
        private String eventDescription;   // 사례내용 (事件描述)
        private LocalDate reportDate;      // 보고일자 (报告日期) - 注意：表格中无此字段，保持为null
        private String eventGrade;         // 등급 (等级)
        private String processingStatus;   // 처리상태 (处理状态)

        // Getters and Setters
        public String getReportNumber() { return reportNumber; }
        public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getEventDescription() { return eventDescription; }
        public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

        public LocalDate getReportDate() { return reportDate; }
        public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

        public String getEventGrade() { return eventGrade; }
        public void setEventGrade(String eventGrade) { this.eventGrade = eventGrade; }

        public String getProcessingStatus() { return processingStatus; }
        public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }
    }

    /**
     * 基于多字段参数爬取数据
     * @param companyNames 企业名称列表 (searchPentpNm)
     * @param productNames 产品名称列表 (searchPrdtNm)
     * @param modelNames 型号名称列表 (searchModelnm)
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 报告日期起始 (searchRptDateStart)
     * @param dateTo 报告日期结束 (searchRptDateEnd)
     * @return 爬取结果
     */
    @Transactional
    public String crawlWithMultipleFields(List<String> companyNames, List<String> productNames,
                                         List<String> modelNames, int maxRecords, int batchSize, 
                                         String dateFrom, String dateTo) {
        log.info("🚀 开始基于多字段参数爬取韩国不良事件数据");
        log.info("📊 企业名称数量: {}, 产品名称数量: {}, 型号数量: {}, 日期范围: {} - {}", 
                companyNames != null ? companyNames.size() : 0,
                productNames != null ? productNames.size() : 0,
                modelNames != null ? modelNames.size() : 0,
                dateFrom, dateTo);

        int totalSaved = 0;
        
        // 如果都为空，执行默认搜索
        if ((companyNames == null || companyNames.isEmpty()) && 
            (productNames == null || productNames.isEmpty()) &&
            (modelNames == null || modelNames.isEmpty())) {
            return crawlAndSaveToDatabase(null, null, null, maxRecords, batchSize, dateFrom, dateTo);
        }

        // 遍历企业名称
        if (companyNames != null && !companyNames.isEmpty()) {
            for (String companyName : companyNames) {
                if (companyName == null || companyName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n🏢 处理企业名称: {}", companyName);
                    String result = crawlAndSaveToDatabase(companyName.trim(), null, null, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("处理企业名称 '{}' 失败: {}", companyName, e.getMessage());
                }
            }
        }

        // 遍历产品名称
        if (productNames != null && !productNames.isEmpty()) {
            for (String productName : productNames) {
                if (productName == null || productName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n📦 处理产品名称: {}", productName);
                    String result = crawlAndSaveToDatabase(null, productName.trim(), null, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("处理产品名称 '{}' 失败: {}", productName, e.getMessage());
                }
            }
        }

        // 遍历型号名称
        if (modelNames != null && !modelNames.isEmpty()) {
            for (String modelName : modelNames) {
                if (modelName == null || modelName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n🔧 处理型号名称: {}", modelName);
                    String result = crawlAndSaveToDatabase(null, null, modelName.trim(), maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("处理型号名称 '{}' 失败: {}", modelName, e.getMessage());
                }
            }
        }

        return String.format("多字段韩国不良事件数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取韩国不良事件数据并保存到数据库
     * @param companyName 企业名称 (searchPentpNm)
     * @param productName 产品名称 (searchPrdtNm)
     * @param modelName 型号名称 (searchModelnm)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 报告日期起始 (searchRptDateStart)
     * @param dateTo 报告日期结束 (searchRptDateEnd)
     * @return 保存结果
     */
    @Transactional
    public String crawlAndSaveToDatabase(String companyName, String productName, String modelName,
                                        int maxRecords, int batchSize, 
                                        String dateFrom, String dateTo) {
        log.info("🚀 开始爬取韩国MFDS不良事件数据");
        log.info("📊 企业名称: {}, 产品名称: {}, 型号: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                companyName, productName, modelName, 
                maxRecords == -1 ? "所有数据" : maxRecords, batchSize, dateFrom, dateTo);

        try {
            List<KoreaEventData> eventDataList = crawlEventData(
                companyName, productName, modelName, maxRecords, dateFrom, dateTo);
            
            if (eventDataList.isEmpty()) {
                log.warn("未获取到韩国不良事件数据");
                return "未获取到不良事件数据";
            }
            
            log.info("成功爬取到 {} 条不良事件数据，开始保存到数据库", eventDataList.size());
            
            return saveBatchToDatabase(eventDataList, batchSize);
            
        } catch (Exception e) {
            log.error("爬取韩国不良事件数据失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 爬取不良事件数据（核心方法）
     */
    private List<KoreaEventData> crawlEventData(String companyName, String productName, String modelName,
                                                int maxRecords, String dateFrom, String dateTo) throws Exception {
        List<KoreaEventData> allData = new ArrayList<>();
        int pageNum = 1;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords == -1);

        while (crawlAll || totalFetched < maxRecords) {
            try {
                log.info("📄 正在爬取第 {} 页", pageNum);
                
                String url = buildUrl(companyName, productName, modelName, pageNum, dateFrom, dateTo);
                log.debug("请求URL: {}", url);
                
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html, */*; q=0.01")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"Windows\"")
                        .header("sec-fetch-dest", "empty")
                        .header("sec-fetch-mode", "cors")
                        .header("sec-fetch-site", "same-origin")
                        .header("x-requested-with", "XMLHttpRequest")
                        .referrer("https://emedi.mfds.go.kr/abcs/MNU20268")
                        .timeout(30000)
                        .get();

                List<KoreaEventData> pageData = parseEventData(doc);
                
                if (pageData.isEmpty()) {
                    log.info("第 {} 页没有数据，停止爬取", pageNum);
                    break;
                }

                allData.addAll(pageData);
                totalFetched += pageData.size();
                
                log.info("✅ 第 {} 页爬取完成，获取 {} 条数据，累计: {}", pageNum, pageData.size(), totalFetched);

                // 检查是否达到最大记录数
                if (!crawlAll && totalFetched >= maxRecords) {
                    log.info("已达到最大记录数 {}，停止爬取", maxRecords);
                    break;
                }

                pageNum++;
                
                // 添加延迟避免请求过快
                Thread.sleep(1500);
                
            } catch (Exception e) {
                log.error("爬取第 {} 页时发生错误: {}", pageNum, e.getMessage());
                throw e;
            }
        }

        // 如果指定了最大记录数，则截取
        if (!crawlAll && allData.size() > maxRecords) {
            allData = allData.subList(0, maxRecords);
        }

        log.info("📊 韩国不良事件数据爬取完成，共获取 {} 条数据", allData.size());
        return allData;
    }

    /**
     * 构建请求URL
     * 
     * @param companyName 企业名称 (searchPentpNm)
     * @param productName 产品名称 (searchPrdtNm)
     * @param modelName 型号名称 (searchModelnm)
     * @param pageNum 页码
     * @param dateFrom 报告日期起始 (searchRptDateStart)
     * @param dateTo 报告日期结束 (searchRptDateEnd)
     * @return 完整的请求URL
     */
    private String buildUrl(String companyName, String productName, String modelName,
                           int pageNum, String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?");
        
        // 报告日期起始 (searchRptDateStart)
        if (dateFrom != null && !dateFrom.isEmpty()) {
            url.append("searchRptDateStart=").append(formatDate(dateFrom));
        } else {
            url.append("searchRptDateStart=");
        }
        
        // 报告日期结束 (searchRptDateEnd)
        if (dateTo != null && !dateTo.isEmpty()) {
            url.append("&searchRptDateEnd=").append(formatDate(dateTo));
        } else {
            url.append("&searchRptDateEnd=");
        }
        
        // 企业名称 (searchPentpNm)
        if (companyName != null && !companyName.isEmpty()) {
            url.append("&searchPentpNm=").append(encodeUrl(companyName));
        } else {
            url.append("&searchPentpNm=");
        }
        
        // 产品名称 (searchPrdtNm)
        if (productName != null && !productName.isEmpty()) {
            url.append("&searchPrdtNm=").append(encodeUrl(productName));
        } else {
            url.append("&searchPrdtNm=");
        }
        
        url.append("&searchPrdtNmCn=");
        
        // 型号名称 (searchModelnm)
        if (modelName != null && !modelName.isEmpty()) {
            url.append("&searchModelnm=").append(encodeUrl(modelName));
        } else {
            url.append("&searchModelnm=");
        }
        
        url.append("&searchFdaWordCn=");
        url.append("&pageNum=").append(pageNum);
        url.append("&searchYn=");
        url.append("&searchAfKey=");
        url.append("&apiRptSno=");
        
        return url.toString();
    }

    /**
     * 格式化日期（转为yyyy-MM-dd格式）
     */
    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        // 移除所有非数字字符
        String cleaned = date.replaceAll("[^0-9]", "");
        // 如果是yyyyMMdd格式，转换为yyyy-MM-dd
        if (cleaned.length() == 8) {
            return cleaned.substring(0, 4) + "-" + 
                   cleaned.substring(4, 6) + "-" + 
                   cleaned.substring(6, 8);
        }
        return date;
    }

    /**
     * URL编码
     */
    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 解析不良事件数据
     */
    private List<KoreaEventData> parseEventData(Document doc) {
        List<KoreaEventData> dataList = new ArrayList<>();
        
        try {
            // 查找数据表格
            Elements rows = doc.select("table tbody tr");
            
            if (rows.isEmpty()) {
                // 尝试其他可能的选择器
                rows = doc.select("tr[class*=data], tr[class*=row], .list-table tr");
            }
            
            log.debug("找到 {} 行数据", rows.size());

            for (Element row : rows) {
                try {
                    KoreaEventData data = parseRow(row);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("解析不良事件数据失败", e);
        }

        return dataList;
    }

    /**
     * 解析单行数据
     * 注意：韩国不良事件表格中不包含日期字段
     */
    private KoreaEventData parseRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 4) {
                return null;
            }

            KoreaEventData data = new KoreaEventData();
            
            // 根据实际表格列顺序调整索引
            // 典型列顺序：报告编号、企业名称、产品名称、型号、事件类型、等级、处理状态
            int colIndex = 0;
            
            if (cols.size() > colIndex) {
                data.setReportNumber(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setCompanyName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setProductName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setModelName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setEventType(cols.get(colIndex++).text().trim());
            }
            
            // 注意：韩国不良事件表格中没有报告日期字段，跳过日期解析
            
            if (cols.size() > colIndex) {
                data.setEventGrade(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setProcessingStatus(cols.get(colIndex++).text().trim());
            }

            return data;
            
        } catch (Exception e) {
            log.warn("解析行数据失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 批量保存到数据库
     */
    @Transactional
    private String saveBatchToDatabase(List<KoreaEventData> records, int batchSize) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<KoreaEventData> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceEventReport> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (KoreaEventData record : batch) {
                try {
                    // 使用报告编号作为唯一标识
                    String reportNumber = "KR_" + 
                        (record.getReportNumber() != null ? record.getReportNumber() : 
                         UUID.randomUUID().toString());
                    
                    // 检查是否已存在
                    boolean isDuplicate = eventReportRepository.existsByReportNumber(reportNumber);

                    if (!isDuplicate) {
                        DeviceEventReport entity = convertToEntity(record, reportNumber);
                        newRecords.add(entity);
                    } else {
                        batchDuplicateCount++;
                    }
                } catch (Exception e) {
                    log.error("处理记录时发生错误: {}", e.getMessage());
                    batchDuplicateCount++;
                }
            }

            // 保存新记录
            if (!newRecords.isEmpty()) {
                try {
                    List<DeviceEventReport> savedRecords = eventReportRepository.saveAll(newRecords);
                    savedCount += savedRecords.size();
                    totalSkipped += batchDuplicateCount;
                    log.info("第 {} 批次保存成功，新增: {} 条，重复: {} 条", batchCount, newRecords.size(), batchDuplicateCount);
                } catch (Exception e) {
                    log.error("第 {} 批次保存失败: {}", batchCount, e.getMessage());
                }
            } else {
                log.info("第 {} 批次全部重复，跳过: {} 条", batchCount, batchDuplicateCount);
                totalSkipped += batchDuplicateCount;
            }
        }

        return String.format("保存成功: %d 条新记录, 跳过重复: %d 条", savedCount, totalSkipped);
    }

    /**
     * 将韩国不良事件数据转换为实体
     */
    private DeviceEventReport convertToEntity(KoreaEventData src, String reportNumber) {
        if (src == null) return null;

        DeviceEventReport entity = new DeviceEventReport();

        // 设置唯一标识
        entity.setReportNumber(reportNumber);

        // 使用AI翻译服务翻译韩文字段
        String translatedProductName = translateIfNeeded(src.getProductName());
        String translatedCompanyName = translateIfNeeded(src.getCompanyName());
        String translatedModelName = translateIfNeeded(src.getModelName());
        String translatedEventType = translateIfNeeded(src.getEventType());
        String translatedEventDescription = translateIfNeeded(src.getEventDescription());

        // 设置基本信息（使用翻译后的数据）
        entity.setGenericName(truncateString(translatedProductName, 255));
        entity.setBrandName(truncateString(translatedModelName, 255));
        entity.setManufacturerName(truncateString(translatedCompanyName, 255));
        
        // 注意：韩国不良事件数据中没有日期字段，日期字段保持为空
        entity.setDateOfEvent(null);
        entity.setDateReceived(null);
        
        // 设置设备类别（从事件等级推断）
        entity.setDeviceClass(src.getEventGrade());
        
        // 设置数据源信息
        entity.setDataSource("MFDS");
        entity.setJdCountry("KR");
        
        // 设置爬取时间
        entity.setCrawlTime(LocalDateTime.now());

        // 设置风险等级为默认中风险
        entity.setRiskLevel(RiskLevel.MEDIUM);

        // 关键词字段初始为空
        entity.setKeywords(null);
        
        // 设置备注（包含事件类型和事件描述）
        StringBuilder remarkBuilder = new StringBuilder();
        
        if (translatedEventType != null && !translatedEventType.isEmpty()) {
            remarkBuilder.append("事件类型: ").append(translatedEventType);
        }
        
        if (translatedEventDescription != null && !translatedEventDescription.isEmpty()) {
            if (remarkBuilder.length() > 0) {
                remarkBuilder.append("\n");
            }
            remarkBuilder.append("事件描述: ").append(translatedEventDescription);
        }
        
        if (remarkBuilder.length() > 0) {
            entity.setRemark(remarkBuilder.toString());
        }

        return entity;
    }

    /**
     * 翻译韩文字段（如果需要）
     * 格式："한글원문English Translation"
     */
    private String translateIfNeeded(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // 使用火山引擎翻译服务（韩语->英语）
            String translated = translateAI.translateAndAppend(text, "ko");
            log.debug("翻译完成: {} -> {}",
                     text.substring(0, Math.min(20, text.length())),
                     translated.substring(0, Math.min(50, translated.length())));
            return translated;
        } catch (Exception e) {
            log.warn("翻译失败，使用原文: {} - {}", text, e.getMessage());
            return text;
        }
    }


    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        log.warn("字段内容过长，已截断至{}字符: {}", maxLength, str.substring(0, Math.min(50, str.length())));
        return str.substring(0, maxLength);
    }

    /**
     * 从结果字符串中提取保存的记录数
     */
    private int extractSavedCount(String result) {
        if (result == null || result.isEmpty()) {
            return 0;
        }

        try {
            // 查找 "保存成功: X 条" 模式
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:保存成功|新增|入库)[:：]?\\s*(\\d+)\\s*条");
            java.util.regex.Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 忽略解析错误
        }

        return 0;
    }
}


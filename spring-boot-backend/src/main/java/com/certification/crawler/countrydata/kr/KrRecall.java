package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;
import com.certification.util.KeywordUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 韩国医疗器械召回数据爬虫
 * 数据来源：韩国食品药品安全处 (MFDS - Ministry of Food and Drug Safety)
 * API地址：https://emedi.mfds.go.kr/recall/list/MNU20265
 */
@Slf4j
@Component
public class KrRecall {

    private static final String BASE_URL = "https://emedi.mfds.go.kr/recall/list/MNU20265";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;

    /**
     * 韩国召回数据模型
     */
    public static class KoreaRecallData {
        private String recallNumber;        // 회수번호 (召回编号)
        private String itemName;            // 제품명 (产品名称)
        private String modelName;           // 모델명 (型号)
        private String companyName;         // 업체명 (企业名称)
        private String itemNumber;          // 품목허가번호 (产品许可编号)
        private String recallGrade;         // 회수등급 (召回等级)
        private String recallReason;        // 회수사유 (召回事由)
        private LocalDate announcementDate; // 공표일 (公告日期)
        private String progress;            // 진행상태 (进行状态)

        // Getters and Setters
        public String getRecallNumber() { return recallNumber; }
        public void setRecallNumber(String recallNumber) { this.recallNumber = recallNumber; }
        
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        
        public String getItemNumber() { return itemNumber; }
        public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
        
        public String getRecallGrade() { return recallGrade; }
        public void setRecallGrade(String recallGrade) { this.recallGrade = recallGrade; }
        
        public String getRecallReason() { return recallReason; }
        public void setRecallReason(String recallReason) { this.recallReason = recallReason; }
        
        public LocalDate getAnnouncementDate() { return announcementDate; }
        public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }
        
        public String getProgress() { return progress; }
        public void setProgress(String progress) { this.progress = progress; }
    }

    /**
     * 爬取韩国召回数据并保存到数据库
     * @param searchTerm 搜索关键词（可选）
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 开始日期 (yyyy-MM-dd)
     * @param dateTo 结束日期 (yyyy-MM-dd)
     * @return 保存的记录数量
     */
    @Transactional
    public String crawlAndSaveToDatabase(String searchTerm, int maxRecords, int batchSize, 
                                         String dateFrom, String dateTo) {
        log.info("🚀 开始爬取韩国MFDS召回数据");
        log.info("📊 搜索词: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                searchTerm, maxRecords == -1 ? "所有数据" : maxRecords, batchSize, dateFrom, dateTo);

        try {
            List<KoreaRecallData> recallDataList = crawlRecallData(searchTerm, maxRecords, dateFrom, dateTo);
            
            if (recallDataList.isEmpty()) {
                log.warn("未获取到韩国召回数据");
                return "未获取到召回数据";
            }
            
            log.info("成功爬取到 {} 条召回数据，开始保存到数据库", recallDataList.size());
            
            return saveBatchToDatabase(recallDataList, batchSize);
            
        } catch (Exception e) {
            log.error("爬取韩国召回数据失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 基于关键词列表爬取数据
     * @param inputKeywords 关键词列表
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 爬取结果
     */
    @Transactional
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize,
                                          String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            log.info("关键词列表为空，使用默认搜索");
            return crawlAndSaveToDatabase("", maxRecords, batchSize, dateFrom, dateTo);
        }

        log.info("🚀 开始基于关键词列表爬取韩国召回数据");
        log.info("📊 关键词数量: {}, 日期范围: {} - {}", inputKeywords.size(), dateFrom, dateTo);

        int totalSaved = 0;

        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            keyword = keyword.trim();
            log.info("\n处理关键词: {}", keyword);

            try {
                String result = crawlAndSaveToDatabase(keyword, maxRecords, batchSize, dateFrom, dateTo);
                log.info("关键词 '{}' 爬取结果: {}", keyword, result);
                
                totalSaved += extractSavedCount(result);
                
                // 添加延迟避免请求过快
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("处理关键词 '{}' 时发生错误: {}", keyword, e.getMessage());
            }
        }

        return String.format("基于关键词列表的韩国召回数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取召回数据（核心方法）
     */
    private List<KoreaRecallData> crawlRecallData(String searchTerm, int maxRecords, 
                                                   String dateFrom, String dateTo) throws Exception {
        List<KoreaRecallData> allData = new ArrayList<>();
        int pageNum = 1;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords == -1);

        while (crawlAll || totalFetched < maxRecords) {
            try {
                log.info("📄 正在爬取第 {} 页", pageNum);
                
                String url = buildUrl(searchTerm, pageNum, dateFrom, dateTo);
                log.debug("请求URL: {}", url);
                
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html, */*; q=0.01")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", "https://emedi.mfds.go.kr/recall/MNU20265")
                        .timeout(30000)
                        .get();

                List<KoreaRecallData> pageData = parseRecallData(doc);
                
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

        log.info("📊 韩国召回数据爬取完成，共获取 {} 条数据", allData.size());
        return allData;
    }

    /**
     * 构建请求URL
     */
    private String buildUrl(String searchTerm, int pageNum, String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?mid=MNU20265");
        url.append("&searchYn=true");
        url.append("&searchAfKey=");
        url.append("&pageNum=").append(pageNum);
        
        if (dateFrom != null && !dateFrom.isEmpty()) {
            url.append("&startPlanSbmsnDt=").append(dateFrom.replace("-", ""));
        } else {
            url.append("&startPlanSbmsnDt=");
        }
        
        if (dateTo != null && !dateTo.isEmpty()) {
            url.append("&endPlanSbmsnDt=").append(dateTo.replace("-", ""));
        } else {
            url.append("&endPlanSbmsnDt=");
        }
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // 可以根据搜索类型选择不同的参数
            url.append("&entpName=");       // 企业名称
            url.append("&itemName=").append(encodeUrl(searchTerm));  // 产品名称
            url.append("&modelNm=");        // 型号
            url.append("&itemNoFullname="); // 产品许可编号
        } else {
            url.append("&entpName=");
            url.append("&itemName=");
            url.append("&modelNm=");
            url.append("&itemNoFullname=");
        }
        
        url.append("&part=");
        url.append("&progress=");
        
        return url.toString();
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
     * 解析召回数据（需要根据实际HTML结构调整）
     */
    private List<KoreaRecallData> parseRecallData(Document doc) {
        List<KoreaRecallData> dataList = new ArrayList<>();
        
        try {
            // 查找数据表格
            // 注意：需要根据实际HTML结构调整选择器
            Elements rows = doc.select("table tbody tr");
            
            if (rows.isEmpty()) {
                // 尝试其他可能的选择器
                rows = doc.select("tr[class*=data], tr[class*=row]");
            }
            
            log.debug("找到 {} 行数据", rows.size());

            for (Element row : rows) {
                try {
                    KoreaRecallData data = parseRow(row);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("解析召回数据失败", e);
        }

        return dataList;
    }

    /**
     * 解析单行数据（需要根据实际HTML结构调整）
     */
    private KoreaRecallData parseRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 6) {
                return null;
            }

            KoreaRecallData data = new KoreaRecallData();
            
            // 根据实际表格列顺序调整索引
            // 典型列顺序：编号、产品名称、型号、企业名称、召回等级、公告日期、进行状态
            int colIndex = 0;
            
            data.setRecallNumber(cols.get(colIndex++).text().trim());
            data.setItemName(cols.get(colIndex++).text().trim());
            data.setModelName(cols.get(colIndex++).text().trim());
            data.setCompanyName(cols.get(colIndex++).text().trim());
            
            if (cols.size() > colIndex) {
                data.setItemNumber(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setRecallGrade(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                String dateStr = cols.get(colIndex++).text().trim();
                data.setAnnouncementDate(parseDate(dateStr));
            }
            
            if (cols.size() > colIndex) {
                data.setProgress(cols.get(colIndex++).text().trim());
            }

            return data;
            
        } catch (Exception e) {
            log.warn("解析行数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // 尝试多种日期格式
            String[] patterns = {"yyyy-MM-dd", "yyyy.MM.dd", "yyyyMMdd", "yyyy/MM/dd"};
            
            for (String pattern : patterns) {
                try {
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
            
            log.warn("无法解析日期: {}", dateStr);
            return null;
            
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 批量保存到数据库
     */
    @Transactional
    private String saveBatchToDatabase(List<KoreaRecallData> records, int batchSize) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<KoreaRecallData> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceRecallRecord> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (KoreaRecallData record : batch) {
                try {
                    // 使用召回编号作为唯一标识
                    String cfresId = "KR_" + record.getRecallNumber();
                    
                    // 检查是否已存在
                    boolean isDuplicate = deviceRecallRecordRepository.existsByCfresId(cfresId);

                    if (!isDuplicate) {
                        DeviceRecallRecord entity = convertToEntity(record);
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
                    List<DeviceRecallRecord> savedRecords = deviceRecallRecordRepository.saveAll(newRecords);
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
     * 将韩国召回数据转换为实体
     */
    private DeviceRecallRecord convertToEntity(KoreaRecallData src) {
        if (src == null) return null;

        DeviceRecallRecord entity = new DeviceRecallRecord();

        // 设置唯一标识
        entity.setCfresId("KR_" + src.getRecallNumber());

        // 设置基本信息
        entity.setProductDescription(buildProductDescription(src));
        entity.setRecallingFirm(truncateString(src.getCompanyName(), 255));
        entity.setRecallStatus(src.getRecallGrade());
        entity.setEventDatePosted(src.getAnnouncementDate());
        entity.setDeviceName(truncateString(src.getItemName(), 255));
        entity.setProductCode(truncateString(src.getItemNumber(), 50));
        
        // 设置数据源信息
        entity.setDataSource("MFDS");
        entity.setCountryCode("KR");
        entity.setJdCountry("KR");
        
        // 设置爬取时间
        entity.setCrawlTime(java.time.LocalDateTime.now());

        // 计算风险等级（根据韩国召回等级）
        RiskLevel calculatedRiskLevel = calculateRiskLevelByKoreaGrade(src.getRecallGrade());
        entity.setRiskLevel(calculatedRiskLevel);

        // 提取关键词
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();

        // 从产品名称提取关键词
        if (src.getItemName() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromProductDescription(src.getItemName(), predefinedKeywords));
        }

        // 从型号提取关键词
        if (src.getModelName() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(src.getModelName(), predefinedKeywords));
        }

        // 从公司名提取关键词
        if (src.getCompanyName() != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(src.getCompanyName(), predefinedKeywords));
        }

        // 去重并转换为JSON存储
        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
        entity.setKeywords(KeywordUtil.keywordsToJson(uniqueKeywords));

        return entity;
    }

    /**
     * 构建产品描述
     */
    private String buildProductDescription(KoreaRecallData src) {
        StringBuilder desc = new StringBuilder();
        
        if (src.getItemName() != null && !src.getItemName().isEmpty()) {
            desc.append("产品: ").append(src.getItemName());
        }
        
        if (src.getModelName() != null && !src.getModelName().isEmpty()) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append("型号: ").append(src.getModelName());
        }
        
        if (src.getRecallReason() != null && !src.getRecallReason().isEmpty()) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append("召回原因: ").append(src.getRecallReason());
        }
        
        return desc.toString();
    }

    /**
     * 根据韩国召回等级计算风险等级
     * 韩国召回等级：1등급(1级-最严重), 2등급(2级), 3등급(3级)
     */
    private RiskLevel calculateRiskLevelByKoreaGrade(String recallGrade) {
        if (recallGrade == null || recallGrade.isEmpty()) {
            return RiskLevel.MEDIUM;
        }

        String grade = recallGrade.toUpperCase().trim();
        
        // 1级召回：健康危害严重
        if (grade.contains("1") || grade.contains("I") || grade.contains("ONE")) {
            return RiskLevel.HIGH;
        }
        // 2级召回：健康危害中等
        else if (grade.contains("2") || grade.contains("II") || grade.contains("TWO")) {
            return RiskLevel.MEDIUM;
        }
        // 3级召回：健康危害较低
        else if (grade.contains("3") || grade.contains("III") || grade.contains("THREE")) {
            return RiskLevel.LOW;
        }

        return RiskLevel.MEDIUM;
    }

    /**
     * 获取预定义关键词列表
     */
    private List<String> getPredefinedKeywords() {
        return Arrays.asList(
            "Skin", "Analyzer", "3D", "AI", "AIMYSKIN", "Facial", "Detector", "Scanner",
            "Care", "Portable", "Spectral", "Spectra", "Skin Analysis", "Skin Scanner",
            "3D skin imaging system", "Facial Imaging", "Skin pigmentation analysis system",
            "skin elasticity analysis", "monitor", "imaging", "medical device", "MFDS",
            "recall", "withdrawal", "defect", "safety", "hazard", "Korea"
        );
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

    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        log.warn("字段内容过长，已截断至{}字符: {}", maxLength, str.substring(0, Math.min(50, str.length())));
        return str.substring(0, maxLength);
    }
}


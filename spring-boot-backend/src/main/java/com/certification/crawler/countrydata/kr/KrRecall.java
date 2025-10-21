package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;
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

    @Autowired
    private TranslateAI translateAI;

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
     * 爬取韩国召回数据并保存到数据库（新版本：支持公司名称和产品名称分开搜索）
     * @param companyName 公司名称 (entpName)
     * @param itemName 产品名称 (itemName)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 开始日期 (yyyyMMdd 或 yyyy-MM-dd)
     * @param dateTo 结束日期 (yyyyMMdd 或 yyyy-MM-dd)
     * @return 保存的记录数量
     */
    @Transactional
    public String crawlAndSaveToDatabase(String companyName, String itemName, int maxRecords, int batchSize, 
                                         String dateFrom, String dateTo) {
        log.info("🚀 开始爬取韩国MFDS召回数据");
        log.info("📊 公司名称: {}, 产品名称: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                companyName, itemName, maxRecords == -1 ? "所有数据" : maxRecords, batchSize, dateFrom, dateTo);

        try {
            List<KoreaRecallData> recallDataList = crawlRecallData(companyName, itemName, maxRecords, dateFrom, dateTo);
            
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
     * 基于关键词列表爬取数据（旧方法，兼容保留）
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
            return crawlAndSaveToDatabase(null, null, maxRecords, batchSize, dateFrom, dateTo);
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
                String result = crawlAndSaveToDatabase(null, keyword, maxRecords, batchSize, dateFrom, dateTo);
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
     * 基于多字段参数爬取数据（新方法）
     * @param companyNames 公司名称列表
     * @param itemNames 产品名称列表
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 爬取结果
     */
    @Transactional
    public String crawlWithMultipleFields(List<String> companyNames, List<String> itemNames,
                                         int maxRecords, int batchSize, String dateFrom, String dateTo) {
        log.info("🚀 开始基于多字段参数爬取韩国召回数据");
        log.info("📊 公司名称数量: {}, 产品名称数量: {}, 日期范围: {} - {}", 
                companyNames != null ? companyNames.size() : 0,
                itemNames != null ? itemNames.size() : 0, 
                dateFrom, dateTo);

        int totalSaved = 0;
        
        // 如果都为空，执行默认搜索
        if ((companyNames == null || companyNames.isEmpty()) && 
            (itemNames == null || itemNames.isEmpty())) {
            return crawlAndSaveToDatabase(null, null, maxRecords, batchSize, dateFrom, dateTo);
        }

        // 遍历公司名称
        if (companyNames != null && !companyNames.isEmpty()) {
            for (String companyName : companyNames) {
                if (companyName == null || companyName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n🏢 处理公司名称: {}", companyName);
                    String result = crawlAndSaveToDatabase(companyName.trim(), null, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000); // 添加延迟
                } catch (Exception e) {
                    log.error("处理公司名称 '{}' 失败: {}", companyName, e.getMessage());
                }
            }
        }

        // 遍历产品名称
        if (itemNames != null && !itemNames.isEmpty()) {
            for (String itemName : itemNames) {
                if (itemName == null || itemName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n📦 处理产品名称: {}", itemName);
                    String result = crawlAndSaveToDatabase(null, itemName.trim(), maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000); // 添加延迟
                } catch (Exception e) {
                    log.error("处理产品名称 '{}' 失败: {}", itemName, e.getMessage());
                }
            }
        }

        return String.format("多字段韩国召回数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取召回数据（核心方法）
     */
    private List<KoreaRecallData> crawlRecallData(String companyName, String itemName, int maxRecords, 
                                                   String dateFrom, String dateTo) throws Exception {
        List<KoreaRecallData> allData = new ArrayList<>();
        Set<String> processedRecallNumbers = new HashSet<>(); // 用于去重
        int pageNum = 1;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords == -1);

        int consecutiveEmptyPages = 0; // 连续空页面计数
        int maxEmptyPages = 3; // 最大允许连续空页面数
        int consecutiveDuplicatePages = 0; // 连续重复页面计数
        int maxDuplicatePages = 2; // 最大允许连续重复页面数

        while (crawlAll || totalFetched < maxRecords) {
            try {
                log.info("📄 正在爬取第 {} 页", pageNum);
                
                String url = buildUrl(companyName, itemName, pageNum, dateFrom, dateTo);
                log.debug("请求URL: {}", url);
                
                // 动态referrer
                String referrer = (pageNum == 1) ? "https://emedi.mfds.go.kr/recall/MNU20265" : 
                    buildUrl(companyName, itemName, pageNum - 1, dateFrom, dateTo);
                
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
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", referrer)
                        .referrer(referrer)
                        .timeout(30000)
                        .get();

                List<KoreaRecallData> pageData = parseRecallData(doc);
                
                if (pageData.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.info("第 {} 页没有数据，连续空页面: {}/{}", pageNum, consecutiveEmptyPages, maxEmptyPages);
                    
                    if (consecutiveEmptyPages >= maxEmptyPages) {
                        log.info("连续 {} 页无数据，停止爬取", maxEmptyPages);
                        break;
                    }
                    
                    pageNum++;
                    Thread.sleep(1000);
                    continue;
                }
                
                // 重置连续空页面计数
                consecutiveEmptyPages = 0;
                
                // 去重处理：检查是否有新的召回记录
                List<KoreaRecallData> newData = new ArrayList<>();
                int duplicateCount = 0;
                for (KoreaRecallData data : pageData) {
                    // 使用召回编号或产品名称+公司名称组合作为唯一标识
                    String uniqueId = data.getRecallNumber() != null ? data.getRecallNumber() : 
                                    (data.getItemName() + "_" + data.getCompanyName());
                    
                    if (!processedRecallNumbers.contains(uniqueId)) {
                        processedRecallNumbers.add(uniqueId);
                        newData.add(data);
                    } else {
                        duplicateCount++;
                    }
                }
                
                if (newData.isEmpty()) {
                    consecutiveDuplicatePages++;
                    log.info("第 {} 页全部为重复数据，连续重复页面: {}/{}", pageNum, consecutiveDuplicatePages, maxDuplicatePages);
                    
                    if (consecutiveDuplicatePages >= maxDuplicatePages) {
                        log.info("连续 {} 页重复数据，停止爬取", maxDuplicatePages);
                        break;
                    }
                    
                    pageNum++;
                    Thread.sleep(1000);
                    continue;
                }
                
                // 重置连续重复页面计数
                consecutiveDuplicatePages = 0;
                
                log.info("第 {} 页去重后: 新增 {} 条，重复 {} 条", pageNum, newData.size(), duplicateCount);

                allData.addAll(newData);
                totalFetched += newData.size();
                
                log.info("✅ 第 {} 页爬取完成，获取 {} 条数据，累计: {}", pageNum, newData.size(), totalFetched);

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
                consecutiveEmptyPages++;
                if (consecutiveEmptyPages >= maxEmptyPages) {
                    log.error("连续 {} 页出错，停止爬取", maxEmptyPages);
                    break;
                }
                pageNum++;
                try {
                    Thread.sleep(2000); // 出错时增加延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 如果指定了最大记录数，则截取
        if (!crawlAll && allData.size() > maxRecords) {
            allData = allData.subList(0, maxRecords);
        }

        log.info("📊 韩国召回数据爬取完成，共获取 {} 条数据（已去重）", allData.size());
        return allData;
    }

    /**
     * 构建请求URL（新版本：支持公司名称和产品名称分开搜索）
     * 
     * @param companyName 公司名称 (entpName)
     * @param itemName 产品名称 (itemName)
     * @param pageNum 页码
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 完整的请求URL
     */
    private String buildUrl(String companyName, String itemName, int pageNum, String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?mid=MNU20265");
        
        // 日期参数
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
        
        // 公司名称 (entpName)
        if (companyName != null && !companyName.isEmpty()) {
            url.append("&entpName=").append(encodeUrl(companyName));
        } else {
            url.append("&entpName=");
        }
        
        // 产品名称 (itemName)
        if (itemName != null && !itemName.isEmpty()) {
            url.append("&itemName=").append(encodeUrl(itemName));
        } else {
            url.append("&itemName=");
        }
        
        // 其他参数（保持为空）
        url.append("&modelNm=");        // 型号
        url.append("&itemNoFullname="); // 产品许可编号
        url.append("&part=");           // 零件
        url.append("&progress=");       // 进度状态
        
        // 分页参数
        url.append("&pageNum=").append(pageNum);
        url.append("&searchYn=true");
        url.append("&searchAfKey=");
        
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

        // 使用AI翻译服务翻译韩文字段
        String translatedItemName = translateIfNeeded(src.getItemName());
        String translatedCompanyName = translateIfNeeded(src.getCompanyName());
        String translatedModelName = translateIfNeeded(src.getModelName());
        String translatedRecallReason = translateIfNeeded(src.getRecallReason());

        // 设置基本信息（使用翻译后的数据）
        entity.setProductDescription(buildProductDescriptionWithTranslation(
            translatedItemName, translatedModelName, translatedRecallReason));
        entity.setRecallingFirm(truncateString(translatedCompanyName, 255));
        entity.setRecallStatus(src.getRecallGrade());
        entity.setEventDatePosted(src.getAnnouncementDate());
        entity.setDeviceName(truncateString(translatedItemName, 255));
        entity.setProductCode(truncateString(src.getItemNumber(), 50));
        
        // 设置数据源信息
        entity.setDataSource("MFDS");
        entity.setCountryCode("KR");
        entity.setJdCountry("KR");
        
        // 设置爬取时间
        entity.setCrawlTime(java.time.LocalDateTime.now());

        // 设置风险等级为默认中风险
        entity.setRiskLevel(RiskLevel.MEDIUM);

        // 关键词字段初始为空
        entity.setKeywords(null);

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
            log.debug("翻译完成: {} -> {}", text.substring(0, Math.min(20, text.length())),
                     translated.substring(0, Math.min(50, translated.length())));
            return translated;
        } catch (Exception e) {
            log.warn("翻译失败，使用原文: {} - {}", text, e.getMessage());
            return text;
        }
    }

    /**
     * 构建产品描述（原始版本，保留用于兼容）
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
     * 构建产品描述（使用翻译后的字段）
     * 格式："产品: 한글원문English Translation | 型号: ... | 召回原因: ..."
     */
    private String buildProductDescriptionWithTranslation(String translatedItemName,
                                                          String translatedModelName,
                                                          String translatedRecallReason) {
        StringBuilder desc = new StringBuilder();

        if (translatedItemName != null && !translatedItemName.isEmpty()) {
            desc.append("产品: ").append(translatedItemName);
        }

        if (translatedModelName != null && !translatedModelName.isEmpty()) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append("型号: ").append(translatedModelName);
        }

        if (translatedRecallReason != null && !translatedRecallReason.isEmpty()) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append("召回原因: ").append(translatedRecallReason);
        }

        return desc.toString();
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


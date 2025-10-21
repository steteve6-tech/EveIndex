package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.CustomsCase;
import com.certification.repository.common.CustomsCaseRepository;
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
 * 韩国海关案例数据爬虫
 * 数据来源：韩国海关厅 관세법령정보포털 CLIP (Customs Law Information Portal)
 * API地址：https://unipass.customs.go.kr/clip/index.do
 */
@Slf4j
@Component
public class Kr_customcase {

    private static final String BASE_URL = "https://unipass.customs.go.kr/clip";
    private static final String SEARCH_URL = BASE_URL + "/prlstclsfsrch/retrieveDmstPrlstClsfCaseLst.do";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 韩国海关案例数据模型
     */
    public static class KoreaCustomsCaseData {
        private String caseNumber;           // 사례번호 (案例编号)
        private String enforcementInstitution; // 집행기관 (执行机构)
        private String hsCode;               // HS코드 (HS编码)
        private String productName;          // 상품명 (产品名称)
        private LocalDate enforcementDate;   // 집행일자 (执行日期)
        private String caseDescription;      // 사례설명 (案例描述)

        // Getters and Setters
        public String getCaseNumber() { return caseNumber; }
        public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
        
        public String getEnforcementInstitution() { return enforcementInstitution; }
        public void setEnforcementInstitution(String enforcementInstitution) { this.enforcementInstitution = enforcementInstitution; }
        
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public LocalDate getEnforcementDate() { return enforcementDate; }
        public void setEnforcementDate(LocalDate enforcementDate) { this.enforcementDate = enforcementDate; }
        
        public String getCaseDescription() { return caseDescription; }
        public void setCaseDescription(String caseDescription) { this.caseDescription = caseDescription; }
    }

    /**
     * 基于多字段参数爬取韩国海关案例数据
     */
    public String crawlWithMultipleFields(List<String> searchKeywords, List<String> productNames, 
                                         int maxRecords, int batchSize, String dateFrom, String dateTo) {
        log.info("🚀 开始基于多字段参数爬取韩国海关案例数据");
        log.info("📊 搜索关键词: {}, 产品名称数量: {}, 日期范围: {} - {}", 
                searchKeywords != null ? String.join(", ", searchKeywords) : "无", 
                productNames != null ? productNames.size() : 0, dateFrom, dateTo);

        int totalSaved = 0;
        
        // 如果都为空，执行默认搜索
        if ((searchKeywords == null || searchKeywords.isEmpty()) && 
            (productNames == null || productNames.isEmpty())) {
            try {
                return crawlAndSaveToDatabase(null, null, maxRecords, batchSize, dateFrom, dateTo);
            } catch (Exception e) {
                log.error("执行默认搜索失败: {}", e.getMessage());
                return "执行默认搜索失败: " + e.getMessage();
            }
        }

        // 遍历搜索关键词
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            for (String keyword : searchKeywords) {
                if (keyword == null || keyword.trim().isEmpty()) continue;
                
                try {
                    log.info("\n🔍 使用搜索关键词: {}", keyword);
                    String result = crawlAndSaveToDatabase(keyword.trim(), null, maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000); // 添加延迟
                } catch (Exception e) {
                    log.error("处理搜索关键词 '{}' 失败: {}", keyword, e.getMessage());
                }
            }
        }

        // 遍历产品名称
        if (productNames != null && !productNames.isEmpty()) {
            for (String productName : productNames) {
                if (productName == null || productName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n📦 处理产品名称: {}", productName);
                    String result = crawlAndSaveToDatabase(null, productName.trim(), maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000); // 添加延迟
                } catch (Exception e) {
                    log.error("处理产品名称 '{}' 失败: {}", productName, e.getMessage());
                }
            }
        }

        return String.format("多字段韩国海关案例数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取海关案例数据（核心方法）
     */
    private List<KoreaCustomsCaseData> crawlCustomsCaseData(String searchKeyword, String productName, int maxRecords, 
                                                           String dateFrom, String dateTo) throws Exception {
        List<KoreaCustomsCaseData> allData = new ArrayList<>();
        Set<String> processedCaseNumbers = new HashSet<>(); // 用于去重
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
                
                String postBody = buildPostBody(searchKeyword, productName, pageNum, dateFrom, dateTo);
                log.debug("请求体: {}", postBody);
                
                Document doc = Jsoup.connect(SEARCH_URL)
                        .userAgent(USER_AGENT)
                        .header("Accept", "application/json, text/javascript, */*; q=0.01")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .header("isajax", "true")
                        .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"Windows\"")
                        .header("sec-fetch-dest", "empty")
                        .header("sec-fetch-mode", "cors")
                        .header("sec-fetch-site", "same-origin")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", "https://unipass.customs.go.kr/clip/index.do")
                        .referrer("https://unipass.customs.go.kr/clip/index.do")
                        .requestBody(postBody)
                        .timeout(30000)
                        .post();

                List<KoreaCustomsCaseData> pageData = parseCustomsCaseData(doc);
                
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
                
                // 去重处理：检查是否有新的海关案例
                List<KoreaCustomsCaseData> newData = new ArrayList<>();
                int duplicateCount = 0;
                for (KoreaCustomsCaseData data : pageData) {
                    // 使用案例编号或产品名称+HS编码组合作为唯一标识
                    String uniqueId = data.getCaseNumber() != null ? data.getCaseNumber() : 
                                    (data.getProductName() + "_" + data.getHsCode());
                    
                    if (!processedCaseNumbers.contains(uniqueId)) {
                        processedCaseNumbers.add(uniqueId);
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

        log.info("📊 韩国海关案例数据爬取完成，共获取 {} 条数据（已去重）", allData.size());
        return allData;
    }

    /**
     * 构建POST请求体
     */
    private String buildPostBody(String searchKeyword, String productName, int pageIndex, String dateFrom, String dateTo) {
        StringBuilder body = new StringBuilder();
        
        // 基础参数
        body.append("pageIndex=").append(pageIndex);
        body.append("&pageUnit=10");
        body.append("&orderColumns=ENFR_DT+desc");
        body.append("&rrdcNo=0072025003114");
        body.append("&reffNo=");
        body.append("&dtrmHsSgn=");
        body.append("&stDt=");
        body.append("&edDt=");
        body.append("&srwr=");
        body.append("&srchYn=Y");
        body.append("&scrnTp=VRTC");
        body.append("&sortColm=");
        body.append("&sortOrdr=");
        body.append("&atntSrchTp=");
        body.append("&docId=");
        body.append("&srchReffNo=");
        body.append("&srchDtrmHsSgn=");
        body.append("&srchStDt=");
        body.append("&srchEdDt=");
        body.append("&srchSrwr=");
        body.append("&pagePerRecord=10");
        body.append("&initPageIndex=1");
        body.append("&ULS1002007S_F1_savedToken=8HLDCJJMBCXS1RG6598B6IJT1REQ7WON");
        body.append("&savedToken=ULS1002007S_F1_savedToken");
        body.append("&txtEnfrDt=");
        body.append("&txtDtrmHsSgn=");
        body.append("&attchFileGrpId=");
        
        // 添加搜索参数
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            body.append("&srwr=").append(java.net.URLEncoder.encode(searchKeyword.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        if (productName != null && !productName.trim().isEmpty()) {
            body.append("&srchSrwr=").append(java.net.URLEncoder.encode(productName.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        // 添加日期范围
        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            body.append("&stDt=").append(dateFrom.trim());
            body.append("&srchStDt=").append(dateFrom.trim());
        }
        
        if (dateTo != null && !dateTo.trim().isEmpty()) {
            body.append("&edDt=").append(dateTo.trim());
            body.append("&srchEdDt=").append(dateTo.trim());
        }
        
        return body.toString();
    }

    /**
     * 解析海关案例数据（JSON响应）
     */
    private List<KoreaCustomsCaseData> parseCustomsCaseData(Document doc) {
        List<KoreaCustomsCaseData> dataList = new ArrayList<>();
        
        try {
            // 获取响应文本（应该是JSON格式）
            String responseText = doc.text();
            log.debug("响应内容: {}", responseText);
            
            // 尝试解析JSON响应
            if (responseText != null && !responseText.trim().isEmpty()) {
                // 使用Jackson解析JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(responseText);
                
                // 查找数据数组 - 根据实际响应结构调整路径
                com.fasterxml.jackson.databind.JsonNode dataArray = rootNode.path("uls_dmst").path("itemList");
                if (dataArray.isArray()) {
                    log.debug("找到 {} 条JSON数据", dataArray.size());
                    
                    for (com.fasterxml.jackson.databind.JsonNode item : dataArray) {
                        try {
                            KoreaCustomsCaseData data = parseJsonItem(item);
                            if (data != null) {
                                dataList.add(data);
                            }
                        } catch (Exception e) {
                            log.warn("解析JSON项时出错: {}", e.getMessage());
                        }
                    }
                } else {
                    // 如果没有找到标准JSON结构，尝试解析HTML表格
                    Elements rows = doc.select("table tbody tr");
                    log.debug("JSON解析失败，尝试HTML解析，找到 {} 行数据", rows.size());
                    
                    for (Element row : rows) {
                        try {
                            KoreaCustomsCaseData data = parseRow(row);
                            if (data != null) {
                                dataList.add(data);
                            }
                        } catch (Exception e) {
                            log.warn("解析HTML行数据时出错: {}", e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("解析海关案例数据时出错: {}", e.getMessage());
            // 如果JSON解析失败，尝试HTML解析
            try {
                Elements rows = doc.select("table tbody tr");
                log.debug("JSON解析失败，尝试HTML解析，找到 {} 行数据", rows.size());
                
                for (Element row : rows) {
                    try {
                        KoreaCustomsCaseData data = parseRow(row);
                        if (data != null) {
                            dataList.add(data);
                        }
                    } catch (Exception ex) {
                        log.warn("解析HTML行数据时出错: {}", ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.error("HTML解析也失败: {}", ex.getMessage());
            }
        }
        
        return dataList;
    }

    /**
     * 解析JSON数据项
     */
    private KoreaCustomsCaseData parseJsonItem(com.fasterxml.jackson.databind.JsonNode item) {
        KoreaCustomsCaseData data = new KoreaCustomsCaseData();
        
        try {
            // 根据实际JSON字段名解析数据
            data.setCaseNumber(getJsonText(item, "REFF_NO", "reffNo", "caseNo", "id"));
            data.setEnforcementInstitution(getJsonText(item, "CSTM_NM", "enfrInstt", "institution", "org"));
            data.setHsCode(getJsonText(item, "DTRM_HS_SGN", "dtrmHsSgn", "hsCode", "hs"));
            data.setProductName(getJsonText(item, "CMDT_NM", "srwr", "productName", "item"));
            data.setEnforcementDate(parseJsonDate(item, "ENFR_DT", "enfrDt", "date", "enforcementDate"));
            
            // 如果没有案例编号，生成一个
            if (data.getCaseNumber() == null || data.getCaseNumber().trim().isEmpty()) {
                data.setCaseNumber("KR_CASE_" + System.currentTimeMillis() + "_" + 
                    (data.getHsCode() != null ? data.getHsCode() : "UNKNOWN"));
            }
            
            log.debug("解析JSON项: 案例编号={}, 执行机构={}, HS编码={}, 产品名称={}, 执行日期={}", 
                data.getCaseNumber(), data.getEnforcementInstitution(), data.getHsCode(), 
                data.getProductName(), data.getEnforcementDate());
            
        } catch (Exception e) {
            log.warn("解析JSON项时出错: {}", e.getMessage());
            return null;
        }
        
        return data;
    }
    
    /**
     * 从JSON节点获取文本值
     */
    private String getJsonText(com.fasterxml.jackson.databind.JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            com.fasterxml.jackson.databind.JsonNode field = node.path(fieldName);
            if (!field.isMissingNode() && !field.isNull()) {
                String value = field.asText();
                if (value != null && !value.trim().isEmpty()) {
                    return cleanText(value);
                }
            }
        }
        return null;
    }
    
    /**
     * 从JSON节点解析日期
     */
    private LocalDate parseJsonDate(com.fasterxml.jackson.databind.JsonNode node, String... fieldNames) {
        String dateStr = getJsonText(node, fieldNames);
        if (dateStr != null) {
            return parseDate(dateStr);
        }
        return null;
    }

    /**
     * 解析单行数据（HTML表格）
     */
    private KoreaCustomsCaseData parseRow(Element row) {
        Elements cells = row.select("td");
        if (cells.size() < 3) {
            return null;
        }
        
        KoreaCustomsCaseData data = new KoreaCustomsCaseData();
        
        try {
            // 根据表格结构解析数据
            // 假设表格结构：执行机构 | HS编码 | 产品名称 | 执行日期 | 案例编号
            if (cells.size() >= 5) {
                data.setEnforcementInstitution(cleanText(cells.get(0).text()));
                data.setHsCode(cleanText(cells.get(1).text()));
                data.setProductName(cleanText(cells.get(2).text()));
                data.setEnforcementDate(parseDate(cells.get(3).text()));
                data.setCaseNumber(cleanText(cells.get(4).text()));
            } else if (cells.size() >= 3) {
                // 简化结构：执行机构 | HS编码 | 产品名称
                data.setEnforcementInstitution(cleanText(cells.get(0).text()));
                data.setHsCode(cleanText(cells.get(1).text()));
                data.setProductName(cleanText(cells.get(2).text()));
            }
            
            // 如果没有案例编号，生成一个
            if (data.getCaseNumber() == null || data.getCaseNumber().trim().isEmpty()) {
                data.setCaseNumber("KR_CASE_" + System.currentTimeMillis() + "_" + data.getHsCode());
            }
            
        } catch (Exception e) {
            log.warn("解析行数据时出错: {}", e.getMessage());
            return null;
        }
        
        return data;
    }

    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试多种日期格式
            String[] patterns = {"yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy"};
            for (String pattern : patterns) {
                try {
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr);
        }
        
        return null;
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
     * 爬取并保存到数据库
     */
    private String crawlAndSaveToDatabase(String searchKeyword, String productName, int maxRecords, 
                                        int batchSize, String dateFrom, String dateTo) throws Exception {
        log.info("🚀 开始爬取韩国海关案例数据");
        log.info("📊 搜索关键词: {}, 产品名称: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                searchKeyword, productName, maxRecords, batchSize, dateFrom, dateTo);

        List<KoreaCustomsCaseData> crawledData = crawlCustomsCaseData(searchKeyword, productName, maxRecords, dateFrom, dateTo);
        
        if (crawledData.isEmpty()) {
            log.info("没有爬取到数据");
            return "没有爬取到数据";
        }

        log.info("成功爬取到 {} 条海关案例数据，开始保存到数据库", crawledData.size());
        
        return saveToDatabase(crawledData, batchSize);
    }

    /**
     * 保存到数据库
     */
    @Transactional
    private String saveToDatabase(List<KoreaCustomsCaseData> dataList, int batchSize) {
        int totalSaved = 0;
        int totalDuplicates = 0;
        int batchCount = 0;
        
        for (int i = 0; i < dataList.size(); i += batchSize) {
            batchCount++;
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<KoreaCustomsCaseData> batch = dataList.subList(i, endIndex);
            
            int batchSaved = 0;
            int batchDuplicates = 0;
            
            for (KoreaCustomsCaseData data : batch) {
                try {
                    CustomsCase entity = convertToEntity(data);
                    
                    // 检查是否已存在
                    if (customsCaseRepository.existsByCaseNumber(entity.getCaseNumber())) {
                        batchDuplicates++;
                        continue;
                    }
                    
                    customsCaseRepository.save(entity);
                    batchSaved++;
                    
                } catch (Exception e) {
                    log.error("保存海关案例数据时出错: {}", e.getMessage());
                }
            }
            
            totalSaved += batchSaved;
            totalDuplicates += batchDuplicates;
            
            log.info("第 {} 批次保存成功，新增: {} 条，重复: {} 条", batchCount, batchSaved, batchDuplicates);
            
            // 批次间延迟
            if (i + batchSize < dataList.size()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return String.format("韩国海关案例数据保存完成，新增: %d 条，重复: %d 条", totalSaved, totalDuplicates);
    }

    /**
     * 转换为实体对象
     */
    private CustomsCase convertToEntity(KoreaCustomsCaseData src) {
        CustomsCase entity = new CustomsCase();
        
        // 基本信息
        entity.setCaseNumber(src.getCaseNumber());
        entity.setCaseDate(src.getEnforcementDate());
        entity.setHsCodeUsed(src.getHsCode());
        entity.setDataSource("韩国海关厅 관세법령정보포털 CLIP");
        
        // 翻译产品名称
        String translatedProductName = translateText(src.getProductName());
        entity.setRulingResult(translatedProductName);
        
        // 不保存执行机构信息
        entity.setViolationType(null);
        
        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setKeywords(null);
        entity.setJdCountry("KR");
        entity.setDataStatus("ACTIVE");
        entity.setRemark(null); // remark保持为空
        entity.setPenaltyAmount(null); // 韩国数据没有处罚金额
        entity.setCrawlTime(java.time.LocalDateTime.now());
        entity.setCreateTime(java.time.LocalDateTime.now());
        entity.setUpdateTime(java.time.LocalDateTime.now());
        
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
            // 使用火山引擎翻译服务（韩语->英语）
            String translated = translateAI.translateAndAppend(text, "ko");
            log.debug("翻译完成: {} -> {}", text, translated);
            return translated;
        } catch (Exception e) {
            log.warn("翻译失败，返回原文: {}", e.getMessage());
            return text;
        }
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    /**
     * 从结果字符串中提取保存数量
     */
    private int extractSavedCount(String result) {
        if (result == null || result.isEmpty()) {
            return 0;
        }
        
        try {
            // 从 "新增: X 条" 中提取数字
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("新增:\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            log.warn("提取保存数量失败: {}", e.getMessage());
        }
        
        return 0;
    }
}
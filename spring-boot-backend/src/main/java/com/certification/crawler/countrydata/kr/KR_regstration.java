package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 韩国医疗器械注册记录爬虫
 * 数据来源：韩国食品药品安全处 (MFDS - Ministry of Food and Drug Safety)
 * API地址：https://emedi.mfds.go.kr/search/data/list
 */
@Slf4j
@Component
public class KR_regstration {

    private static final String BASE_URL = "https://emedi.mfds.go.kr/search/data/list";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private DeviceRegistrationRecordRepository registrationRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 韩国注册数据模型
     */
    public static class KoreaRegistrationData {
        private String itemName;           // 품목명 (产品名称)
        private String brandName;          // 상호명 (品牌名称)
        private String companyName;        // 업체명 (企业名称)
        private String manufacturerName;   // 제조업체 (制造商)
        private String itemNumber;         // 품목허가번호 (产品许可编号)
        private String approvalNumber;     // 인허가번호 (批准编号)
        private LocalDate approvalDate;    // 허가일자 (许可日期)
        private String deviceClass;        // 등급 (等级)
        private String status;             // 상태 (状态)
        private String validityPeriod;     // 유효기간 (有效期)

        // Getters and Setters
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getManufacturerName() { return manufacturerName; }
        public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }

        public String getItemNumber() { return itemNumber; }
        public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }

        public String getApprovalNumber() { return approvalNumber; }
        public void setApprovalNumber(String approvalNumber) { this.approvalNumber = approvalNumber; }

        public LocalDate getApprovalDate() { return approvalDate; }
        public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }

        public String getDeviceClass() { return deviceClass; }
        public void setDeviceClass(String deviceClass) { this.deviceClass = deviceClass; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getValidityPeriod() { return validityPeriod; }
        public void setValidityPeriod(String validityPeriod) { this.validityPeriod = validityPeriod; }
    }

    /**
     * 基于多字段参数爬取数据
     * @param searchQuery 搜索关键词 (query)
     * @param companyNames 企业名称列表 (entpName)
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 产品许可日期起始 (prdlPrmDtFrom)
     * @param dateTo 产品许可日期结束 (prdlPrmDtTo)
     * @return 爬取结果
     */
    @Transactional
    public String crawlWithMultipleFields(String searchQuery, List<String> companyNames,
                                         int maxRecords, int batchSize, 
                                         String dateFrom, String dateTo) {
        log.info("🚀 开始基于多字段参数爬取韩国注册数据");
        log.info("📊 搜索词: {}, 企业名称数量: {}, 日期范围: {} - {}", 
                searchQuery,
                companyNames != null ? companyNames.size() : 0, 
                dateFrom, dateTo);

        int totalSaved = 0;
        
        // 如果都为空，执行默认搜索
        if ((searchQuery == null || searchQuery.isEmpty()) && 
            (companyNames == null || companyNames.isEmpty())) {
            return crawlAndSaveToDatabase(null, null, maxRecords, batchSize, dateFrom, dateTo);
        }

        // 如果有搜索关键词，使用搜索关键词
        if (searchQuery != null && !searchQuery.isEmpty()) {
            try {
                log.info("\n🔍 使用搜索关键词: {}", searchQuery);
                String result = crawlAndSaveToDatabase(searchQuery, null, maxRecords, batchSize, dateFrom, dateTo);
                totalSaved += extractSavedCount(result);
            } catch (Exception e) {
                log.error("使用搜索关键词 '{}' 失败: {}", searchQuery, e.getMessage());
            }
        }

        // 遍历企业名称
        if (companyNames != null && !companyNames.isEmpty()) {
            for (String companyName : companyNames) {
                if (companyName == null || companyName.trim().isEmpty()) continue;
                
                try {
                    log.info("\n🏢 处理企业名称: {}", companyName);
                    String result = crawlAndSaveToDatabase(null, companyName.trim(), maxRecords, batchSize, dateFrom, dateTo);
                    totalSaved += extractSavedCount(result);
                    Thread.sleep(2000); // 添加延迟
                } catch (Exception e) {
                    log.error("处理企业名称 '{}' 失败: {}", companyName, e.getMessage());
                }
            }
        }

        return String.format("多字段韩国注册数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取韩国注册数据并保存到数据库
     * @param searchQuery 搜索关键词 (query)
     * @param companyName 企业名称 (entpName)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 产品许可日期起始 (prdlPrmDtFrom)
     * @param dateTo 产品许可日期结束 (prdlPrmDtTo)
     * @return 保存结果
     */
    @Transactional
    public String crawlAndSaveToDatabase(String searchQuery, String companyName, 
                                        int maxRecords, int batchSize, 
                                        String dateFrom, String dateTo) {
        log.info("🚀 开始爬取韩国MFDS注册数据");
        log.info("📊 搜索词: {}, 企业名称: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                searchQuery, companyName, maxRecords == -1 ? "所有数据" : maxRecords, batchSize, dateFrom, dateTo);

        try {
            List<KoreaRegistrationData> registrationDataList = crawlRegistrationData(
                searchQuery, companyName, maxRecords, dateFrom, dateTo);
            
            if (registrationDataList.isEmpty()) {
                log.warn("未获取到韩国注册数据");
                return "未获取到注册数据";
            }
            
            log.info("成功爬取到 {} 条注册数据，开始保存到数据库", registrationDataList.size());
            
            return saveBatchToDatabase(registrationDataList, batchSize);
            
        } catch (Exception e) {
            log.error("爬取韩国注册数据失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 爬取注册数据（核心方法）
     */
    private List<KoreaRegistrationData> crawlRegistrationData(String searchQuery, String companyName, 
                                                              int maxRecords, String dateFrom, String dateTo) throws Exception {
        List<KoreaRegistrationData> allData = new ArrayList<>();
        Set<String> processedRegistrationNumbers = new HashSet<>(); // 用于去重
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
                
                String url = buildUrl(searchQuery, companyName, pageNum, dateFrom, dateTo);
                log.debug("请求URL: {}", url);
                
                // 构建正确的referrer（第一页没有referrer，后续页面使用前一页）
                String referrer = (pageNum == 1) ? "https://emedi.mfds.go.kr/search/data/MNU20237" : 
                    buildUrl(searchQuery, companyName, pageNum - 1, dateFrom, dateTo);
                
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
                        .referrer(referrer)  // 修改：使用动态referrer
                        .timeout(30000)
                        .get();

                List<KoreaRegistrationData> pageData = parseRegistrationData(doc);
                
                if (pageData.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.info("第 {} 页没有数据，连续空页面: {}/{}", pageNum, consecutiveEmptyPages, maxEmptyPages);
                    
                    if (consecutiveEmptyPages >= maxEmptyPages) {
                        log.info("连续 {} 页无数据，停止爬取", maxEmptyPages);
                        break;
                    }
                    
                    pageNum++;
                    Thread.sleep(1000); // 空页面时减少延迟
                    continue;
                }
                
                // 重置连续空页面计数
                consecutiveEmptyPages = 0;
                
                // 去重处理：检查是否有新的注册号
                List<KoreaRegistrationData> newData = new ArrayList<>();
                int duplicateCount = 0;
                for (KoreaRegistrationData data : pageData) {
                    // 使用批准编号或产品编号作为唯一标识
                    String uniqueId = data.getApprovalNumber() != null ? data.getApprovalNumber() : 
                                    data.getItemNumber() != null ? data.getItemNumber() : null;
                    
                    if (uniqueId != null && !processedRegistrationNumbers.contains(uniqueId)) {
                        processedRegistrationNumbers.add(uniqueId);
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

                // 检查是否还有下一页（通过分页控件判断）
                boolean hasNextPage = checkHasNextPage(doc, pageNum);
                if (!hasNextPage && newData.size() < 10) { // 如果页面数据少于10条且没有下一页，可能已到最后一页
                    log.info("第 {} 页数据较少且无下一页，可能已到最后一页", pageNum);
                    break; // 直接停止，避免无限循环
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
                Thread.sleep(2000); // 出错时增加延迟
            }
        }

        // 如果指定了最大记录数，则截取
        if (!crawlAll && allData.size() > maxRecords) {
            allData = allData.subList(0, maxRecords);
        }

        log.info("📊 韩国注册数据爬取完成，共获取 {} 条数据（已去重）", allData.size());
        return allData;
    }

    /**
     * 构建请求URL
     * 
     * @param searchQuery 搜索关键词 (query)
     * @param companyName 企业名称 (entpName)
     * @param pageNum 页码
     * @param dateFrom 产品许可日期起始 (prdlPrmDtFrom)
     * @param dateTo 产品许可日期结束 (prdlPrmDtTo)
     * @return 完整的请求URL
     */
    private String buildUrl(String searchQuery, String companyName, int pageNum, 
                           String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?chkList=1");
        url.append("&toggleBtnState=");
        url.append("&nowPageNum=").append(pageNum);
        url.append("&tabGubun=1");
        url.append("&tcsbizRsmptSeCdNm=");
        url.append("&indtyCdNm=");
        url.append("&itemStateNm=");
        url.append("&mnftrNtnCdNm=");
        url.append("&tmpQrBarcode=");
        url.append("&query2=");
        url.append("&udidiCode=");
        url.append("&grade=0");
        url.append("&itemState=");
        url.append("&itemNoFullname=");
        
        // 企业名称 (entpName)
        if (companyName != null && !companyName.isEmpty()) {
            url.append("&entpName=").append(encodeUrl(companyName));
        } else {
            url.append("&entpName=");
        }
        
        url.append("&indtyCd=1%7C2%7C21%7C22");  // 行业代码（固定）
        url.append("&tcsbizRsmptSeCd=");
        url.append("&mdentpPrmno=");
        url.append("&mnfacrNm=");
        url.append("&typeName=");
        url.append("&brandName=");
        url.append("&itemName=");
        
        // 搜索关键词 (query)
        if (searchQuery != null && !searchQuery.isEmpty()) {
            url.append("&query=").append(encodeUrl(searchQuery));
        } else {
            url.append("&query=");
        }
        
        url.append("&rcprslryCdInptvl=");
        url.append("&mdClsfNo=");
        
        // 产品许可日期起始 (prdlPrmDtFrom)
        if (dateFrom != null && !dateFrom.isEmpty()) {
            url.append("&prdlPrmDtFrom=").append(formatDate(dateFrom));
        } else {
            url.append("&prdlPrmDtFrom=");
        }
        
        // 产品许可日期结束 (prdlPrmDtTo)
        if (dateTo != null && !dateTo.isEmpty()) {
            url.append("&prdlPrmDtTo=").append(formatDate(dateTo));
        } else {
            url.append("&prdlPrmDtTo=");
        }
        
        url.append("&validDateFrom=");
        url.append("&validDateTo=");
        url.append("&rcprslryTrgtYn=");
        url.append("&traceManageTargetYn=");
        url.append("&xprtppYn=");
        url.append("&hmnbdTspnttyMdYn=");
        url.append("&chkGroup=GROUP_BY_FIELD_01");
        url.append("&pageNum=").append(pageNum);
        url.append("&searchYn=");  // 修改：searchYn应该为空，不是true
        url.append("&searchAfKey=");
        url.append("&sort=");
        url.append("&sortOrder=");
        url.append("&searchOn=Y");
        url.append("&ean13=");
        url.append("&searchUdiCode=");
        
        return url.toString();
    }

    /**
     * 格式化日期（从yyyy-MM-dd转为yyyy-MM-dd格式）
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
     * 解析注册数据
     */
    private List<KoreaRegistrationData> parseRegistrationData(Document doc) {
        List<KoreaRegistrationData> dataList = new ArrayList<>();
        
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
                    KoreaRegistrationData data = parseRow(row);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("解析注册数据失败", e);
        }

        return dataList;
    }

    /**
     * 解析单行数据
     */
    private KoreaRegistrationData parseRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 4) {
                return null;
            }

            KoreaRegistrationData data = new KoreaRegistrationData();
            
            // 根据实际表格列顺序调整索引
            // 典型列顺序：品目名、企业名、批准号、许可日期、状态等
            int colIndex = 0;
            
            if (cols.size() > colIndex) {
                data.setItemName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setCompanyName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setBrandName(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setApprovalNumber(cols.get(colIndex++).text().trim());
            }
            
            // 使用特定的选择器提取日期：document.querySelector("#item_1 > font > font")
            String dateStr = extractDateFromRow(row);
            data.setApprovalDate(parseDate(dateStr));
            
            if (cols.size() > colIndex) {
                data.setDeviceClass(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setStatus(cols.get(colIndex++).text().trim());
            }
            
            if (cols.size() > colIndex) {
                data.setManufacturerName(cols.get(colIndex++).text().trim());
            }

            return data;
            
        } catch (Exception e) {
            log.warn("解析行数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从行中提取日期，使用特定的选择器
     * 对应 document.querySelector("#item_1 > font > font")
     */
    private String extractDateFromRow(Element row) {
        try {
            // 尝试使用特定的选择器提取日期
            Element dateElement = row.selectFirst("#item_1 > font > font");
            if (dateElement != null) {
                String dateStr = dateElement.text().trim();
                log.debug("从特定选择器提取到日期: {}", dateStr);
                return dateStr;
            }
            
            // 如果特定选择器没有找到，尝试其他可能的选择器
            // 尝试查找包含日期的font元素
            Elements fontElements = row.select("font");
            for (Element font : fontElements) {
                String text = font.text().trim();
                if (isDateString(text)) {
                    log.debug("从font元素提取到日期: {}", text);
                    return text;
                }
            }
            
            // 尝试查找包含日期的td元素
            Elements cols = row.select("td");
            for (Element col : cols) {
                String text = col.text().trim();
                if (isDateString(text)) {
                    log.debug("从td元素提取到日期: {}", text);
                    return text;
                }
            }
            
            log.debug("未找到日期信息");
            return null;
            
        } catch (Exception e) {
            log.warn("提取日期失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断字符串是否为日期格式
     */
    private boolean isDateString(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 检查是否包含日期格式的字符
        return text.matches(".*\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}.*") ||
               text.matches(".*\\d{4}\\d{2}\\d{2}.*") ||
               text.matches(".*\\d{1,2}[-./]\\d{1,2}[-./]\\d{4}.*");
    }

    /**
     * 检查是否还有下一页
     */
    private boolean checkHasNextPage(Document doc, int currentPage) {
        try {
            // 查找分页控件
            Elements pagination = doc.select(".pagination, .paging, .page-navigation, .pager");
            
            if (pagination.isEmpty()) {
                // 如果没有分页控件，尝试查找"下一页"链接
                Elements nextLinks = doc.select("a:contains(다음), a:contains(下一页), a:contains(Next)");
                return !nextLinks.isEmpty();
            }
            
            // 检查分页控件中是否有下一页按钮
            Elements nextButtons = pagination.select("a:contains(다음), a:contains(下一页), a:contains(Next), .next");
            if (!nextButtons.isEmpty()) {
                // 检查下一页按钮是否被禁用
                for (Element nextBtn : nextButtons) {
                    if (!nextBtn.hasClass("disabled") && !nextBtn.hasClass("inactive")) {
                        return true;
                    }
                }
            }
            
            // 检查是否有页码大于当前页
            Elements pageNumbers = pagination.select("a[href*='pageNum'], a[href*='nowPageNum']");
            for (Element pageLink : pageNumbers) {
                String href = pageLink.attr("href");
                if (href.contains("pageNum=") || href.contains("nowPageNum=")) {
                    try {
                        String pageStr;
                        if (href.contains("pageNum=")) {
                            pageStr = href.substring(href.indexOf("pageNum=") + 8);
                        } else {
                            pageStr = href.substring(href.indexOf("nowPageNum=") + 11);
                        }
                        if (pageStr.contains("&")) {
                            pageStr = pageStr.substring(0, pageStr.indexOf("&"));
                        }
                        int pageNum = Integer.parseInt(pageStr);
                        if (pageNum > currentPage) {
                            return true;
                        }
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.warn("检查下一页时发生错误: {}", e.getMessage());
            return true; // 出错时假设还有下一页，让主循环自然结束
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
    private String saveBatchToDatabase(List<KoreaRegistrationData> records, int batchSize) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<KoreaRegistrationData> batch = records.subList(i, endIndex);
            batchCount++;

            List<DeviceRegistrationRecord> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (KoreaRegistrationData record : batch) {
                try {
                    // 使用批准编号作为唯一标识
                    String registrationNumber = "KR_" + 
                        (record.getApprovalNumber() != null ? record.getApprovalNumber() : 
                         record.getItemNumber() != null ? record.getItemNumber() : 
                         UUID.randomUUID().toString());
                    
                    // 检查是否已存在（使用registrationNumber字段）
                    boolean isDuplicate = registrationRepository.existsByRegistrationNumber(registrationNumber);

                    if (!isDuplicate) {
                        DeviceRegistrationRecord entity = convertToEntity(record, registrationNumber);
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
                    List<DeviceRegistrationRecord> savedRecords = registrationRepository.saveAll(newRecords);
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
     * 将韩国注册数据转换为实体
     */
    private DeviceRegistrationRecord convertToEntity(KoreaRegistrationData src, String registrationNumber) {
        if (src == null) return null;

        DeviceRegistrationRecord entity = new DeviceRegistrationRecord();

        // 设置唯一标识
        entity.setRegistrationNumber(registrationNumber);
        entity.setFeiNumber(src.getItemNumber());

        // 使用AI翻译服务翻译韩文字段
        String translatedItemName = translateIfNeeded(src.getItemName());
        String translatedCompanyName = translateIfNeeded(src.getCompanyName());
        String translatedBrandName = translateIfNeeded(src.getBrandName());
        String translatedManufacturerName = translateIfNeeded(src.getManufacturerName());

        // 设置基本信息（使用翻译后的数据）
        // deviceName 和 proprietaryName 设置为相同的值
        String deviceAndProprietaryName = translatedBrandName != null ? translatedBrandName : translatedItemName;
        entity.setDeviceName(truncateText(deviceAndProprietaryName));
        entity.setProprietaryName(truncateLongText(deviceAndProprietaryName));
        entity.setManufacturerName(truncateText(translatedCompanyName != null ? translatedCompanyName : translatedManufacturerName));
        entity.setDeviceClass(truncateLongText(src.getDeviceClass()));
        entity.setStatusCode(truncateString(src.getStatus(), 100));
        
        // 设置日期
        if (src.getApprovalDate() != null) {
            entity.setCreatedDate(src.getApprovalDate().toString());
        }
        
        // 设置数据源信息
        entity.setDataSource("MFDS");
        entity.setJdCountry("KR");
        
        // 设置爬取时间
        entity.setCrawlTime(LocalDateTime.now());

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
     * 截断字符串到TEXT字段长度（TEXT类型）
     */
    private String truncateText(String str) {
        return truncateToLength(str, 65535); // TEXT最大长度
    }

    /**
     * 截断字符串到LONGTEXT字段长度
     */
    private String truncateLongText(String str) {
        return truncateToLength(str, 16777215); // LONGTEXT最大长度，但实际会更小
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        return truncateToLength(str, maxLength);
    }

    /**
     * 通用截断方法
     */
    private String truncateToLength(String str, int maxLength) {
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

package com.certification.crawler.countrydata.tw;

import com.certification.entity.common.DeviceEventReport;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.DeviceEventReportRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 台湾FDA不良事件爬虫
 * 数据源: https://www.fda.gov.tw/TC/siteList.aspx?sid=4275
 * 
 * 两步爬取：
 * 1. 列表页获取器材名称和详情链接
 * 2. 详情页获取完整信息（产品名、型号、制造商、日期）
 * 
 * HTML结构：
 * 列表页：
 * - 器材名称: #ContentPlaceHolder1_PageType1_Panel > table > tbody > tr > td:nth-child(2) > a
 * - 详情链接: 从上述a标签的href提取
 * 
 * 详情页：
 * - 产品名称: #ContentPlaceHolder1_PageContentBox_PnlCms > div > p > strong > span
 * - 型号: #ContentPlaceHolder1_PageContentBox_PnlCms > div > table > tbody > tr > td
 * - 制造商: #ContentPlaceHolder1_PageContentBox_PnlCms > div > div > span
 * - 日期: #ContentPlaceHolder1_PageContentBox_TitlePanel > h3 > span.orangeText
 */
@Slf4j
@Component
public class TwEvent {

    private static final String LIST_URL = "https://www.fda.gov.tw/TC/siteList.aspx?sid=4275";
    private static final String DETAIL_BASE_URL = "https://www.fda.gov.tw/TC/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 60000; // 60秒超时
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final int DETAIL_DELAY = 2000; // 详情页请求延迟2秒

    @Autowired
    private DeviceEventReportRepository eventRepository;

    /**
     * 台湾不良事件数据模型
     */
    @Data
    public static class TaiwanEventData {
        private String genericName;       // 器材名称（从列表获取）
        private String productName;       // 产品名称（从详情获取）
        private String modelNumber;       // 型号（从详情获取）
        private String manufacturerName;  // 制造商名称（从详情获取）
        private String eventDate;         // 事件日期（从详情获取）
        private String detailUrl;         // 详情链接
        private String reportNumber;      // 报告编号（从URL提取）
    }

    /**
     * 爬取最新事件
     */
    public String crawlLatestEvents(int maxRecords) {
        log.info("开始爬取台湾FDA不良事件，最大记录数: {}", maxRecords);
        
        try {
            return crawlByKeyword("", maxRecords);
        } catch (Exception e) {
            log.error("爬取台湾不良事件失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按器材名称搜索
     */
    public String crawlByDeviceName(String deviceName, int maxRecords) {
        log.info("开始爬取台湾不良事件，器材名称: {}, 最大记录数: {}", deviceName, maxRecords);
        
        try {
            return crawlByKeyword(deviceName, maxRecords);
        } catch (Exception e) {
            log.error("爬取台湾不良事件失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按日期范围搜索（由于FDA网站不支持日期筛选，此方法仅爬取最新数据后过滤）
     */
    public String crawlByDateRange(String startDate, String endDate, int maxRecords) {
        log.info("按日期范围爬取台湾不良事件: {} - {}", startDate, endDate);
        // 台湾FDA不良事件网站不支持日期范围搜索，只能爬取所有数据
        return crawlLatestEvents(maxRecords);
    }

    /**
     * 按关键词爬取
     */
    private String crawlByKeyword(String keyword, int maxRecords) throws IOException {
        List<TaiwanEventData> allData = new ArrayList<>();
        int currentPage = 1;
        boolean hasMore = true;
        boolean crawlAll = (maxRecords == -1); // maxRecords = -1 表示不限制数量

        while (hasMore && (crawlAll || allData.size() < maxRecords)) {
            log.info("正在爬取第{}页...", currentPage);
            
            // 构建URL
            String url = buildListUrl(keyword, currentPage);
            
            // 获取列表页
            Document listDoc = fetchPageWithRetry(url);
            if (listDoc == null) {
                log.error("获取列表页失败");
                break;
            }
            
            // 解析列表，获取器材名称和详情链接
            List<TaiwanEventData> pageData = parseEventList(listDoc);
            log.info("第{}页解析到 {} 条记录", currentPage, pageData.size());
            
            if (pageData.isEmpty()) {
                hasMore = false;
            } else {
                // 获取每条记录的详情
                for (TaiwanEventData data : pageData) {
                    // 仅在限制数量时检查（crawlAll=false时才限制）
                    if (!crawlAll && allData.size() >= maxRecords) {
                        break;
                    }
                    
                    try {
                        // 获取详情信息
                        fetchEventDetail(data);
                        allData.add(data);
                        
                        // 延迟，避免请求过快
                        Thread.sleep(DETAIL_DELAY);
                    } catch (Exception e) {
                        log.error("获取详情失败: {} - {}", data.getDetailUrl(), e.getMessage());
                    }
                }
                
                // 检查是否还有下一页
                hasMore = checkHasNextPage(listDoc, currentPage);
                currentPage++;
                
                // 页面间延迟
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        log.info("总共爬取到 {} 条不良事件数据", allData.size());
        
        // 保存到数据库
        return saveToDatabase(allData);
    }

    /**
     * 构建列表URL
     */
    private String buildListUrl(String keyword, int page) {
        StringBuilder url = new StringBuilder(LIST_URL);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
                url.append("&key=").append(encodedKeyword);
            } catch (Exception e) {
                log.warn("关键词编码失败: {}", keyword);
            }
        }
        
        if (page > 1) {
            url.append("&pn=").append(page);
        }
        
        return url.toString();
    }

    /**
     * 获取页面（带重试）
     */
    private Document fetchPageWithRetry(String url) {
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                return fetchPage(url);
            } catch (IOException e) {
                log.warn("第{}次请求失败: {}", retry + 1, e.getMessage());
                if (retry == MAX_RETRIES - 1) {
                    log.error("重试{}次后仍然失败", MAX_RETRIES);
                    return null;
                }
                try {
                    Thread.sleep(2000 * (retry + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    /**
     * 获取页面
     */
    private Document fetchPage(String url) throws IOException {
        log.debug("正在获取页面: {}", url);
        
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Cache-Control", "max-age=0")
                .referrer("https://www.fda.gov.tw/")
                .timeout(TIMEOUT)
                .get();
    }

    /**
     * 解析事件列表
     * 
     * HTML结构：
     * #ContentPlaceHolder1_PageType1_Panel > table > tbody > tr
     *   - td:nth-child(1) - 序号
     *   - td:nth-child(2) > a - 器材名称 + 详情链接
     */
    private List<TaiwanEventData> parseEventList(Document doc) {
        List<TaiwanEventData> dataList = new ArrayList<>();
        
        try {
            // 查找主内容面板
            Element mainPanel = doc.selectFirst("#ContentPlaceHolder1_PageType1_Panel");
            if (mainPanel == null) {
                log.warn("未找到主内容面板");
                return dataList;
            }
            
            // 查找表格
            Element table = mainPanel.selectFirst("table");
            if (table == null) {
                log.warn("未找到事件列表表格");
                return dataList;
            }
            
            // 查找所有数据行
            Elements rows = table.select("tbody tr");
            log.debug("找到 {} 行事件数据", rows.size());
            
            for (Element row : rows) {
                TaiwanEventData data = parseListRow(row);
                if (data != null) {
                    dataList.add(data);
                }
            }
            
        } catch (Exception e) {
            log.error("解析事件列表失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }

    /**
     * 解析列表行
     */
    private TaiwanEventData parseListRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 2) {
                log.debug("列数不足，跳过此行");
                return null;
            }
            
            TaiwanEventData data = new TaiwanEventData();
            
            // 第2列：器材名称和详情链接
            Element nameCol = cols.get(1);
            if (nameCol != null) {
                Element link = nameCol.selectFirst("a");
                if (link != null) {
                    // 器材名称
                    String genericName = link.text().trim();
                    data.setGenericName(genericName);
                    
                    // 详情链接
                    String href = link.attr("href");
                    if (href != null && !href.isEmpty()) {
                        // 处理相对路径
                        if (!href.startsWith("http")) {
                            // href格式: siteListContent.aspx?sid=4275&id=24116
                            // 或 siteListContent.aspx?sid=4275&amp;id=24116
                            href = href.replace("&amp;", "&");
                            href = DETAIL_BASE_URL + href;
                        }
                        data.setDetailUrl(href);
                        
                        // 从URL提取报告编号（id参数）
                        String reportNumber = extractIdFromUrl(href);
                        data.setReportNumber(reportNumber);
                    }
                }
            }
            
            // 验证必填字段
            if (data.getGenericName() == null || data.getGenericName().isEmpty()) {
                log.debug("跳过无效行：缺少器材名称");
                return null;
            }
            
            if (data.getDetailUrl() == null || data.getDetailUrl().isEmpty()) {
                log.debug("跳过无效行：缺少详情链接");
                return null;
            }
            
            log.debug("解析到事件 - 器材: {}, 链接: {}", data.getGenericName(), data.getDetailUrl());
            
            return data;
            
        } catch (Exception e) {
            log.debug("解析列表行失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从URL提取ID作为报告编号
     */
    private String extractIdFromUrl(String url) {
        try {
            String[] parts = url.split("[?&]");
            for (String part : parts) {
                if (part.startsWith("id=")) {
                    return "TW-" + part.substring(3);
                }
            }
        } catch (Exception e) {
            log.debug("提取ID失败: {}", url);
        }
        return "TW-" + System.currentTimeMillis();
    }

    /**
     * 获取事件详情
     * 
     * HTML结构：
     * - 产品名称: #ContentPlaceHolder1_PageContentBox_PnlCms > div > p > strong > span
     * - 型号: #ContentPlaceHolder1_PageContentBox_PnlCms > div > table > tbody > tr:nth-child(2) > td:nth-child(1)
     * - 制造商: #ContentPlaceHolder1_PageContentBox_PnlCms > div > div > span
     * - 日期: #ContentPlaceHolder1_PageContentBox_TitlePanel > h3 > span.orangeText
     */
    private void fetchEventDetail(TaiwanEventData data) throws IOException {
        log.debug("获取事件详情: {}", data.getDetailUrl());
        
        Document doc = fetchPageWithRetry(data.getDetailUrl());
        if (doc == null) {
            log.warn("获取详情页失败: {}", data.getDetailUrl());
            return;
        }
        
        try {
            // 1. 日期（从标题中提取）
            Element dateSpan = doc.selectFirst("#ContentPlaceHolder1_PageContentBox_TitlePanel > h3 > span.orangeText");
            if (dateSpan == null) {
                dateSpan = doc.selectFirst("span.orangeText");
            }
            if (dateSpan != null) {
                String dateStr = dateSpan.text().trim();
                data.setEventDate(dateStr);
                log.debug("提取到日期: {}", dateStr);
            }
            
            // 2. 产品名称
            Element contentBox = doc.selectFirst("#ContentPlaceHolder1_PageContentBox_PnlCms");
            if (contentBox != null) {
                // 查找产品名称（可能在多个位置）
                Elements strongSpans = contentBox.select("p strong span, p > strong > span");
                if (!strongSpans.isEmpty()) {
                    String productName = strongSpans.first().text().trim();
                    data.setProductName(productName);
                    log.debug("提取到产品名称: {}", productName);
                }
                
                // 3. 型号（从表格中提取）
                Elements tables = contentBox.select("table");
                if (!tables.isEmpty()) {
                    for (Element table : tables) {
                        Elements cells = table.select("tbody tr td");
                        if (!cells.isEmpty()) {
                            String modelNumber = cells.first().text().trim();
                            if (!modelNumber.isEmpty()) {
                                data.setModelNumber(modelNumber);
                                log.debug("提取到型号: {}", modelNumber);
                                break;
                            }
                        }
                    }
                }
                
                // 4. 制造商名称
                Elements divSpans = contentBox.select("div span");
                for (Element span : divSpans) {
                    String text = span.text().trim();
                    // 寻找可能的制造商信息（通常包含"公司"、"股份"等关键词）
                    if (text.contains("公司") || text.contains("股份") || text.contains("有限") || 
                        text.length() > 5) { // 或者较长的文本
                        data.setManufacturerName(text);
                        log.debug("提取到制造商: {}", text);
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("解析详情页失败: {} - {}", data.getDetailUrl(), e.getMessage(), e);
        }
    }

    /**
     * 检查是否有下一页
     */
    private boolean checkHasNextPage(Document doc, int currentPage) {
        try {
            // 查找分页链接
            Elements pageLinks = doc.select("a[href*=pn=]");
            for (Element link : pageLinks) {
                String href = link.attr("href");
                if (href.contains("pn=" + (currentPage + 1))) {
                    return true;
                }
            }
            
            // 或者查找"下一页"链接
            Elements nextLinks = doc.select("a:contains(下一頁), a:contains(下一页), a:contains(next)");
            return !nextLinks.isEmpty();
            
        } catch (Exception e) {
            log.debug("检查下一页失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 转换为实体对象
     */
    private DeviceEventReport convertToEntity(TaiwanEventData src) {
        DeviceEventReport entity = new DeviceEventReport();
        
        // 基本信息（继承自BaseDeviceEntity）
        entity.setJdCountry("TW");
        entity.setDataSource("台湾食品药物管理署");
        
        // 报告编号
        entity.setReportNumber(src.getReportNumber());
        
        // 器材信息
        entity.setGenericName(src.getGenericName());
        
        // 制造商
        if (src.getManufacturerName() != null && !src.getManufacturerName().isEmpty()) {
            entity.setManufacturerName(src.getManufacturerName());
        }
        
        // 日期信息（将民国年转换为西元年）
        if (src.getEventDate() != null && !src.getEventDate().isEmpty()) {
            try {
                LocalDate date = parseROCDate(src.getEventDate());
                if (date != null) {
                    entity.setDateOfEvent(date);
                    entity.setDateReceived(date);
                }
            } catch (Exception e) {
                log.debug("日期解析失败: {}", src.getEventDate());
            }
        }
        
        // 备注：组合产品名称、型号等信息
        StringBuilder remarkBuilder = new StringBuilder();
        
        if (src.getProductName() != null && !src.getProductName().isEmpty()) {
            remarkBuilder.append("产品名称: ").append(src.getProductName()).append("\n");
        }
        
        if (src.getModelNumber() != null && !src.getModelNumber().isEmpty()) {
            remarkBuilder.append("型号: ").append(src.getModelNumber()).append("\n");
        }
        
        if (src.getDetailUrl() != null && !src.getDetailUrl().isEmpty()) {
            remarkBuilder.append("详情: ").append(src.getDetailUrl());
        }
        
        if (remarkBuilder.length() > 0) {
            entity.setRemark(remarkBuilder.toString());
        }
        
        // 风险等级（台湾爬虫统一为中等）
        entity.setRiskLevel(RiskLevel.MEDIUM);
        
        // 时间戳
        entity.setCrawlTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        return entity;
    }

    /**
     * 解析民国年日期
     * 支持多种格式
     */
    private LocalDate parseROCDate(String dateStr) {
        try {
            dateStr = dateStr.trim().replace(" ", "");
            
            if (dateStr.isEmpty()) {
                return null;
            }
            
            int year, month, day;
            
            // 格式1: 109/08/10 或 109.08.10 或 109-08-10
            if (dateStr.contains("/") || dateStr.contains(".") || dateStr.contains("-")) {
                String[] parts = dateStr.split("[/.\\-]");
                if (parts.length != 3) {
                    return null;
                }
                
                year = Integer.parseInt(parts[0]) + 1911;
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            }
            // 格式2: 109年8月10日
            else if (dateStr.contains("年")) {
                dateStr = dateStr.replace("年", "/").replace("月", "/").replace("日", "");
                String[] parts = dateStr.split("/");
                if (parts.length < 2) {
                    return null;
                }
                
                year = Integer.parseInt(parts[0]) + 1911;
                month = Integer.parseInt(parts[1]);
                day = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
            }
            // 格式3: 1090810 (7位数字)
            else if (dateStr.length() == 7 && dateStr.matches("\\d{7}")) {
                year = Integer.parseInt(dateStr.substring(0, 3)) + 1911;
                month = Integer.parseInt(dateStr.substring(3, 5));
                day = Integer.parseInt(dateStr.substring(5, 7));
            }
            else {
                return null;
            }
            
            LocalDate date = LocalDate.of(year, month, day);
            log.debug("民国年 {} 转换为西元年 {}", dateStr, date);
            return date;
            
        } catch (Exception e) {
            log.debug("解析民国年日期失败: {} - {}", dateStr, e.getMessage());
            return null;
        }
    }

    /**
     * 保存到数据库
     */
    private String saveToDatabase(List<TaiwanEventData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "⚠️ 没有数据需要保存";
        }
        
        int newCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        
        // 分批处理
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, dataList.size());
            List<TaiwanEventData> batch = dataList.subList(i, end);
            
            for (TaiwanEventData data : batch) {
                try {
                    DeviceEventReport entity = convertToEntity(data);
                    
                    // 去重（按报告编号）
                    Optional<DeviceEventReport> existing = eventRepository
                            .findByReportNumber(entity.getReportNumber());
                    
                    if (existing.isPresent()) {
                        DeviceEventReport existingEntity = existing.get();
                        existingEntity.setRemark(entity.getRemark());
                        existingEntity.setDateOfEvent(entity.getDateOfEvent());
                        existingEntity.setDateReceived(entity.getDateReceived());
                        existingEntity.setUpdateTime(LocalDateTime.now());
                        eventRepository.save(existingEntity);
                        updateCount++;
                    } else {
                        eventRepository.save(entity);
                        newCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("保存不良事件失败: {}", data.getReportNumber(), e);
                    skipCount++;
                }
            }
            
            log.info("已处理 {}/{} 条记录", Math.min(i + BATCH_SIZE, dataList.size()), dataList.size());
        }
        
        String result = String.format("✅ 台湾不良事件保存完成！总计: %d 条，新增: %d 条，更新: %d 条，跳过: %d 条",
                dataList.size(), newCount, updateCount, skipCount);
        log.info(result);
        return result;
    }
}

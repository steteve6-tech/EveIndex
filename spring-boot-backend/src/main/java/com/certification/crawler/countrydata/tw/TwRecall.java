package com.certification.crawler.countrydata.tw;

import com.certification.entity.common.DeviceRecallRecord;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.DeviceRecallRecordRepository;
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
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * 台湾FDA召回记录爬虫
 * 数据源: https://www.fda.gov.tw/TC/siteList.aspx?sid=4275
 * 
 * 两步爬取：
 * 1. 列表页获取设备名称和详情链接
 * 2. 详情页获取完整召回信息
 * 
 * HTML结构：
 * 列表页：
 * - 设备名称: #ContentPlaceHolder1_PageType1_Panel > table > tbody > tr > td:nth-child(2) > a
 * - 详情链接: 从a标签的href提取
 * 
 * 详情页：
 * - 日期: #ContentPlaceHolder1_PageContentBox_TitlePanel > h3 > span.orangeText
 * - 产品英文名称: p:nth-child(7) > span:nth-child(2)
 * - 产品规格: table中"規格"列的内容
 * - 型号: table中"型號"列的内容
 * - 召回公司: "廠商聯絡資訊"下面的公司名称
 * - 产品描述: "警訊說明："下面的内容
 */
@Slf4j
@Component
public class TwRecall {

    private static final String LIST_URL = "https://www.fda.gov.tw/TC/siteList.aspx?sid=4275";
    private static final String DETAIL_BASE_URL = "https://www.fda.gov.tw/TC/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 60000; // 60秒超时
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final int DETAIL_DELAY = 2000; // 详情页请求延迟2秒

    @Autowired
    private DeviceRecallRecordRepository recallRepository;

    /**
     * 台湾召回记录数据模型
     */
    @Data
    public static class TaiwanRecallData {
        private String deviceName;        // 设备名称（从列表获取）
        private String productNameEn;     // 产品英文名称（从详情获取）
        private String productSpec;       // 产品规格（从详情表格获取）
        private String modelNumber;       // 型号（从详情表格获取）
        private String recallingFirm;     // 召回公司（从详情获取）
        private String productDescription;// 产品描述（警讯说明）
        private String eventDate;         // 召回日期（从详情获取）
        private String detailUrl;         // 详情链接
        private String cfresId;           // 召回事件ID（从URL提取）
    }

    /**
     * 爬取最新召回记录
     */
    public String crawlLatestRecalls(int maxRecords) {
        log.info("开始爬取台湾FDA召回记录，最大记录数: {}", maxRecords);
        
        try {
            return crawlByKeyword("", maxRecords);
        } catch (Exception e) {
            log.error("爬取台湾召回记录失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按设备名称搜索
     */
    public String crawlByDeviceName(String deviceName, int maxRecords) {
        log.info("开始爬取台湾召回记录，设备名称: {}, 最大记录数: {}", deviceName, maxRecords);
        
        try {
            return crawlByKeyword(deviceName, maxRecords);
        } catch (Exception e) {
            log.error("爬取台湾召回记录失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按日期范围搜索（由于FDA网站不支持日期筛选，此方法仅爬取最新数据后过滤）
     */
    public String crawlByDateRange(String startDate, String endDate, int maxRecords) {
        log.info("按日期范围爬取台湾召回记录: {} - {}", startDate, endDate);
        // 台湾FDA召回网站不支持日期范围搜索，只能爬取所有数据
        return crawlLatestRecalls(maxRecords);
    }

    /**
     * 按关键词爬取
     */
    private String crawlByKeyword(String keyword, int maxRecords) throws IOException {
        List<TaiwanRecallData> allData = new ArrayList<>();
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
            
            // 解析列表，获取设备名称和详情链接
            List<TaiwanRecallData> pageData = parseRecallList(listDoc);
            log.info("第{}页解析到 {} 条记录", currentPage, pageData.size());
            
            if (pageData.isEmpty()) {
                hasMore = false;
            } else {
                // 获取每条记录的详情
                for (TaiwanRecallData data : pageData) {
                    // 仅在限制数量时检查（crawlAll=false时才限制）
                    if (!crawlAll && allData.size() >= maxRecords) {
                        break;
                    }
                    
                    try {
                        // 获取详情信息
                        fetchRecallDetail(data);
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
        
        log.info("总共爬取到 {} 条召回记录数据", allData.size());
        
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
     * 解析召回列表
     * 
     * HTML结构：
     * #ContentPlaceHolder1_PageType1_Panel > table > tbody > tr
     *   - td:nth-child(1) - 序号
     *   - td:nth-child(2) > a - 设备名称 + 详情链接
     */
    private List<TaiwanRecallData> parseRecallList(Document doc) {
        List<TaiwanRecallData> dataList = new ArrayList<>();
        
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
                log.warn("未找到召回列表表格");
                return dataList;
            }
            
            // 查找所有数据行
            Elements rows = table.select("tbody tr");
            log.debug("找到 {} 行召回数据", rows.size());
            
            for (Element row : rows) {
                TaiwanRecallData data = parseListRow(row);
                if (data != null) {
                    dataList.add(data);
                }
            }
            
        } catch (Exception e) {
            log.error("解析召回列表失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }

    /**
     * 解析列表行
     */
    private TaiwanRecallData parseListRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 2) {
                log.debug("列数不足，跳过此行");
                return null;
            }
            
            TaiwanRecallData data = new TaiwanRecallData();
            
            // 第2列：设备名称和详情链接
            Element nameCol = cols.get(1);
            if (nameCol != null) {
                Element link = nameCol.selectFirst("a");
                if (link != null) {
                    // 设备名称
                    String deviceName = link.text().trim();
                    data.setDeviceName(deviceName);
                    
                    // 详情链接
                    String href = link.attr("href");
                    if (href != null && !href.isEmpty()) {
                        // 处理相对路径和转义字符
                        if (!href.startsWith("http")) {
                            href = href.replace("&amp;", "&");
                            href = DETAIL_BASE_URL + href;
                        }
                        data.setDetailUrl(href);
                        
                        // 从URL提取召回ID（id参数）
                        String cfresId = extractIdFromUrl(href);
                        data.setCfresId(cfresId);
                    }
                }
            }
            
            // 验证必填字段
            if (data.getDeviceName() == null || data.getDeviceName().isEmpty()) {
                log.debug("跳过无效行：缺少设备名称");
                return null;
            }
            
            if (data.getDetailUrl() == null || data.getDetailUrl().isEmpty()) {
                log.debug("跳过无效行：缺少详情链接");
                return null;
            }
            
            log.debug("解析到召回 - 设备: {}, 链接: {}", data.getDeviceName(), data.getDetailUrl());
            
            return data;
            
        } catch (Exception e) {
            log.debug("解析列表行失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从URL提取ID作为召回事件ID
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
     * 获取召回详情
     * 
     * HTML结构：
     * - 日期: #ContentPlaceHolder1_PageContentBox_TitlePanel > h3 > span.orangeText
     * - 产品英文名称: p:nth-child(7) > span:nth-child(2)
     * - 产品规格: table中"規格"列
     * - 型号: table中"型號"列
     * - 召回公司: "廠商聯絡資訊"后面的公司名
     * - 产品描述: "警訊說明："后面的内容
     */
    private void fetchRecallDetail(TaiwanRecallData data) throws IOException {
        log.debug("获取召回详情: {}", data.getDetailUrl());
        
        Document doc = fetchPageWithRetry(data.getDetailUrl());
        if (doc == null) {
            log.warn("获取详情页失败: {}", data.getDetailUrl());
            return;
        }
        
        try {
            Element contentBox = doc.selectFirst("#ContentPlaceHolder1_PageContentBox_PnlCms");
            if (contentBox == null) {
                log.warn("未找到内容区域");
                return;
            }
            
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
            
            // 2. 产品英文名称（第7个p标签的第2个span）
            Elements paragraphs = contentBox.select("p");
            for (int i = 0; i < paragraphs.size(); i++) {
                Element p = paragraphs.get(i);
                String text = p.text();
                
                // 查找包含"產品英文名稱"或"英文名稱"的段落
                if (text.contains("產品英文名稱") || text.contains("英文名稱")) {
                    // 下一个段落通常包含实际值
                    if (i + 1 < paragraphs.size()) {
                        String productNameEn = paragraphs.get(i + 1).text().trim();
                        data.setProductNameEn(productNameEn);
                        log.debug("提取到产品英文名称: {}", productNameEn);
                    }
                    break;
                }
            }
            
            // 3. 从表格提取产品规格和型号
            Elements tables = contentBox.select("table");
            if (!tables.isEmpty()) {
                for (Element table : tables) {
                    Elements headerCells = table.select("th");
                    
                    // 检查是否是包含"規格"、"型號"的表格
                    boolean hasSpec = false;
                    boolean hasModel = false;
                    int specIndex = -1;
                    int modelIndex = -1;
                    
                    for (int i = 0; i < headerCells.size(); i++) {
                        String headerText = headerCells.get(i).text().trim();
                        if (headerText.contains("規格")) {
                            hasSpec = true;
                            specIndex = i;
                        }
                        if (headerText.contains("型號")) {
                            hasModel = true;
                            modelIndex = i;
                        }
                    }
                    
                    if (hasSpec || hasModel) {
                        // 提取第一行数据
                        Elements dataRows = table.select("tbody tr");
                        if (!dataRows.isEmpty()) {
                            Element firstRow = dataRows.first();
                            Elements dataCells = firstRow.select("td");
                            
                            // 提取规格
                            if (specIndex >= 0 && specIndex < dataCells.size()) {
                                String spec = dataCells.get(specIndex).text().trim();
                                data.setProductSpec(spec);
                                log.debug("提取到产品规格: {}", spec);
                            }
                            
                            // 提取型号
                            if (modelIndex >= 0 && modelIndex < dataCells.size()) {
                                String model = dataCells.get(modelIndex).text().trim();
                                data.setModelNumber(model);
                                log.debug("提取到型号: {}", model);
                            }
                        }
                        break; // 找到目标表格后退出
                    }
                }
            }
            
            // 4. 提取召回公司（"廠商聯絡資訊"后面的公司名）
            Elements allParagraphs = contentBox.select("p");
            for (int i = 0; i < allParagraphs.size(); i++) {
                Element p = allParagraphs.get(i);
                String text = p.text();
                
                if (text.contains("廠商聯絡資訊") || text.contains("厂商联络资讯")) {
                    // 下一个段落通常是公司名称
                    if (i + 1 < allParagraphs.size()) {
                        String firmName = allParagraphs.get(i + 1).text().trim();
                        // 过滤掉"联络电话"等非公司名的内容
                        if (!firmName.contains("聯絡電話") && !firmName.contains("電子郵件")) {
                            data.setRecallingFirm(firmName);
                            log.debug("提取到召回公司: {}", firmName);
                        }
                    }
                    break;
                }
            }
            
            // 5. 提取产品描述（"警訊說明："后面的内容）
            for (int i = 0; i < allParagraphs.size(); i++) {
                Element p = allParagraphs.get(i);
                String text = p.text();
                
                if (text.contains("警訊說明") || text.contains("警讯说明")) {
                    // 下一个段落通常是警讯说明内容
                    if (i + 1 < allParagraphs.size()) {
                        String description = allParagraphs.get(i + 1).text().trim();
                        data.setProductDescription(description);
                        log.debug("提取到产品描述: {}", description.substring(0, Math.min(50, description.length())) + "...");
                    }
                    break;
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
    private DeviceRecallRecord convertToEntity(TaiwanRecallData src) {
        DeviceRecallRecord entity = new DeviceRecallRecord();
        
        // 基本信息（继承自BaseDeviceEntity）
        entity.setJdCountry("TW");
        entity.setDataSource("台湾食品药物管理署");
        entity.setCountryCode("TW");
        
        // 召回事件ID
        entity.setCfresId(src.getCfresId());
        
        // 设备名称
        entity.setDeviceName(src.getDeviceName());
        
        // 产品描述（警讯说明）
        if (src.getProductDescription() != null && !src.getProductDescription().isEmpty()) {
            entity.setProductDescription(src.getProductDescription());
        }
        
        // 召回公司
        if (src.getRecallingFirm() != null && !src.getRecallingFirm().isEmpty()) {
            entity.setRecallingFirm(src.getRecallingFirm());
        }
        
        // 产品代码（使用型号）
        if (src.getModelNumber() != null && !src.getModelNumber().isEmpty()) {
            entity.setProductCode(src.getModelNumber());
        }
        
        // 召回等级（台湾FDA的召回警讯默认为"回收警訊"）
        entity.setRecallStatus("回收警訊");
        
        // 召回发布日期（将民国年转换为西元年）
        if (src.getEventDate() != null && !src.getEventDate().isEmpty()) {
            try {
                LocalDate date = parseROCDate(src.getEventDate());
                if (date != null) {
                    entity.setEventDatePosted(date);
                }
            } catch (Exception e) {
                log.debug("日期解析失败: {}", src.getEventDate());
            }
        }
        
        // 风险等级（台湾爬虫统一为中等）
        entity.setRiskLevel(RiskLevel.MEDIUM);
        
        // 备注：默认为空（不设置）
        // entity.setRemark(null);
        
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
    private String saveToDatabase(List<TaiwanRecallData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "⚠️ 没有数据需要保存";
        }

        int newCount = 0;
        int updateCount = 0;
        int skipCount = 0;

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);

        // 分批处理
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, dataList.size());
            List<TaiwanRecallData> batch = dataList.subList(i, end);

            int batchNewCount = 0;

            for (TaiwanRecallData data : batch) {
                try {
                    DeviceRecallRecord entity = convertToEntity(data);

                    // 去重（按召回事件ID）
                    Optional<DeviceRecallRecord> existing = recallRepository
                            .findByCfresId(entity.getCfresId());

                    if (existing.isPresent()) {
                        DeviceRecallRecord existingEntity = existing.get();
                        existingEntity.setProductDescription(entity.getProductDescription());
                        existingEntity.setRecallingFirm(entity.getRecallingFirm());
                        existingEntity.setProductCode(entity.getProductCode());
                        existingEntity.setEventDatePosted(entity.getEventDatePosted());
                        existingEntity.setUpdateTime(LocalDateTime.now());
                        recallRepository.save(existingEntity);
                        updateCount++;
                    } else {
                        recallRepository.save(entity);
                        newCount++;
                        batchNewCount++;
                    }

                } catch (Exception e) {
                    log.error("保存召回记录失败: {}", data.getCfresId(), e);
                    skipCount++;
                }
            }

            log.info("已处理 {}/{} 条记录", Math.min(i + BATCH_SIZE, dataList.size()), dataList.size());

            // 批次检测：检查是否应该停止
            boolean shouldStop = detector.recordBatch(batch.size(), batchNewCount);
            if (shouldStop) {
                log.warn("⚠️ 检测到连续重复批次，停止保存剩余数据");
                break;
            }
        }

        // 打印最终统计
        detector.printFinalStats("TwRecall");

        String result = String.format("✅ 台湾召回记录保存完成！总计: %d 条，新增: %d 条，更新: %d 条，跳过: %d 条",
                dataList.size(), newCount, updateCount, skipCount);
        log.info(result);
        return result;
    }
}


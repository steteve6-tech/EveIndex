package com.certification.crawler.countrydata.tw;

import com.certification.entity.common.GuidanceDocument;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.GuidanceDocumentRepository;
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
 * 台湾FDA法规文档爬虫
 * 数据源: https://www.fda.gov.tw/TC/law.aspx?cid=55&scid=59
 * 
 * 类别说明：
 * - cid=55: 药品、医疗器材及化粧品类
 * - scid=59: 医疗器材管理
 * 
 * HTML结构：
 * - 标题: #mp-pusher > div > div.mainContentWrap.withLeft > table > tbody > tr > td:nth-child(2)
 * - 发布日期: #mp-pusher > div > div.mainContentWrap.withLeft > table > tbody > tr > td:nth-child(3)
 */
@Slf4j
@Component
public class TwGuidance {

    private static final String BASE_URL = "https://www.fda.gov.tw/TC/law.aspx";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 60000; // 60秒超时
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    
    // 医疗器材管理法规
    private static final String DEFAULT_CID = "55";  // 药品、医疗器材及化粧品类
    private static final String DEFAULT_SCID = "59"; // 医疗器材管理

    @Autowired
    private GuidanceDocumentRepository guidanceRepository;

    /**
     * 台湾法规数据模型
     */
    @Data
    public static class TaiwanGuidanceData {
        private String title;           // 法规标题（第2列）
        private String publishDate;     // 发布日期（第3列）
        private String detailUrl;       // 详情链接
    }

    /**
     * 爬取所有医疗器材法规
     */
    public String crawlAllMedicalDeviceLaws(int maxRecords) {
        log.info("开始爬取台湾FDA医疗器材法规，最大记录数: {}", maxRecords);
        
        try {
            return crawlByCategory(DEFAULT_CID, DEFAULT_SCID, maxRecords);
        } catch (Exception e) {
            log.error("爬取台湾法规失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按类别爬取法规（支持分页）
     * 
     * 分页参数: pn=1, pn=2, pn=3...
     * 例如: https://www.fda.gov.tw/TC/law.aspx?cid=55&scid=59&pn=2
     */
    public String crawlByCategory(String cid, String scid, int maxRecords) {
        log.info("开始爬取台湾法规，类别: cid={}, scid={}, 最大记录数: {}", cid, scid, maxRecords);
        
        try {
            List<TaiwanGuidanceData> allData = new ArrayList<>();
            int currentPage = 1;
            boolean hasMore = true;
            boolean crawlAll = (maxRecords == -1); // maxRecords = -1 表示不限制数量
            
            // 分页循环（使用 pn 参数）
            while (hasMore && (crawlAll || allData.size() < maxRecords)) {
                log.info("正在爬取第 {} 页...", currentPage);
                
                // 构建URL（使用 pn=N 作为分页参数）
                String url = BASE_URL + "?cid=" + cid + "&scid=" + scid;
                if (currentPage > 1) {
                    url += "&pn=" + currentPage;  // ✅ 使用 pn 而不是 page
                }
                
                log.debug("请求URL: {}", url);
                
                // 获取页面
                Document doc = fetchPageWithRetry(url);
                if (doc == null) {
                    log.warn("获取第 {} 页失败，停止爬取", currentPage);
                    break;
                }
                
                // 解析法规列表
                List<TaiwanGuidanceData> pageData = parseGuidanceTable(doc);
                log.info("第 {} 页解析到 {} 条法规记录", currentPage, pageData.size());
                
                if (pageData.isEmpty()) {
                    log.info("第 {} 页无数据，停止爬取", currentPage);
                    hasMore = false;
                    break;
                }
                
                // 检查数据是否与上一页重复（验证分页是否有效）
                if (currentPage > 1 && allData.size() > 0 && pageData.size() > 0) {
                    // 简单检查：对比第一条数据的标题
                    String lastTitle = allData.get(allData.size() - 1).getTitle();
                    String firstTitle = pageData.get(0).getTitle();
                    if (lastTitle.equals(firstTitle)) {
                        log.warn("⚠️ 检测到重复数据，可能已到达最后一页");
                        break;
                    }
                }
                
                // 检查是否需要截断数据（达到maxRecords限制）
                if (!crawlAll && allData.size() + pageData.size() > maxRecords) {
                    int remainingCount = maxRecords - allData.size();
                    if (remainingCount > 0) {
                        pageData = pageData.subList(0, Math.min(remainingCount, pageData.size()));
                        log.info("第 {} 页数据截取前 {} 条（达到maxRecords限制）", currentPage, remainingCount);
                        allData.addAll(pageData);
                    }
                    break; // 达到限制，停止
                }
                
                allData.addAll(pageData);
                
                // 检查是否有下一页链接
                hasMore = checkHasNextPage(doc);
                if (!hasMore) {
                    log.info("未找到下一页链接，停止爬取");
                    break;
                }
                
                currentPage++;
                
                // 页面间延迟，避免请求过快
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("爬取被中断");
                    break;
                }
            }
            
            log.info("总共爬取到 {} 条法规数据", allData.size());
            
            // 保存到数据库
            return saveToDatabase(allData);
            
        } catch (Exception e) {
            log.error("爬取台湾法规失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }
    
    /**
     * 检查是否有下一页
     */
    private boolean checkHasNextPage(Document doc) {
        try {
            // 查找"下一頁"链接
            Elements nextPageLinks = doc.select("a:contains(下一頁), a:contains(下页), a.pageHighlight + a");
            if (!nextPageLinks.isEmpty()) {
                log.debug("找到下一页链接");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.debug("检查下一页失败: {}", e.getMessage());
            return false;
        }
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
                    Thread.sleep(2000 * (retry + 1)); // 递增延迟
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
     * 解析法规表格
     * 
     * HTML结构：
     * #mp-pusher > div > div.mainContentWrap.withLeft > table > tbody > tr
     *   - td:nth-child(1) - 序号
     *   - td:nth-child(2) - 法规标题
     *   - td:nth-child(3) - 发布日期
     */
    private List<TaiwanGuidanceData> parseGuidanceTable(Document doc) {
        List<TaiwanGuidanceData> dataList = new ArrayList<>();
        
        try {
            // 查找主内容区域
            Element mainContent = doc.selectFirst("#mp-pusher > div > div.mainContentWrap.withLeft");
            if (mainContent == null) {
                // 尝试其他可能的选择器
                mainContent = doc.selectFirst(".mainContentWrap.withLeft");
            }
            if (mainContent == null) {
                // 再尝试直接查找table
                mainContent = doc.selectFirst("table");
            }
            
            if (mainContent == null) {
                log.warn("未找到主内容区域");
                return dataList;
            }
            
            // 查找表格
            Element table = mainContent.selectFirst("table");
            if (table == null) {
                log.warn("未找到法规表格");
                return dataList;
            }
            
            // 查找所有数据行
            Elements rows = table.select("tbody tr");
            log.debug("找到 {} 行法规数据", rows.size());
            
            for (Element row : rows) {
                TaiwanGuidanceData data = parseTableRow(row);
                if (data != null) {
                    dataList.add(data);
                }
            }
            
        } catch (Exception e) {
            log.error("解析法规表格失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }

    /**
     * 解析表格行
     * 
     * 列结构：
     * 1. td:nth-child(1) - 序号
     * 2. td:nth-child(2) - 法规标题
     * 3. td:nth-child(3) - 发布日期
     */
    private TaiwanGuidanceData parseTableRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 3) {
                log.debug("列数不足，跳过此行");
                return null;
            }
            
            TaiwanGuidanceData data = new TaiwanGuidanceData();
            
            // 1. 序号（第1列，跳过）
            
            // 2. 法规标题（第2列）
            // HTML结构: td:nth-child(2) > a
            Element titleCol = cols.get(1);
            if (titleCol != null) {
                // 提取链接（优先）
                Element link = titleCol.selectFirst("a");
                if (link != null) {
                    // 标题文本
                    String title = link.text().trim();
                    data.setTitle(title);
                    
                    // 详情链接
                    String href = link.attr("href");
                    if (href != null && !href.isEmpty()) {
                        // 处理相对路径
                        if (!href.startsWith("http")) {
                            href = "https://www.fda.gov.tw" + (href.startsWith("/") ? "" : "/") + href;
                        }
                        data.setDetailUrl(href);
                    }
                } else {
                    // 如果没有链接，直接取文本
                    String title = titleCol.text().trim();
                    data.setTitle(title);
                }
            }
            
            // 3. 发布日期（第3列）
            Element dateCol = cols.get(2);
            if (dateCol != null) {
                String dateStr = dateCol.text().trim();
                data.setPublishDate(dateStr);
            }
            
            // 验证必填字段
            if (data.getTitle() == null || data.getTitle().isEmpty()) {
                log.debug("跳过无效行：缺少标题");
                return null;
            }
            
            log.debug("解析到法规 - 标题: {}, 日期: {}", data.getTitle(), data.getPublishDate());
            
            return data;
            
        } catch (Exception e) {
            log.debug("解析表格行失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 转换为实体对象
     */
    private GuidanceDocument convertToEntity(TaiwanGuidanceData src) {
        GuidanceDocument entity = new GuidanceDocument();
        
        // 基本信息（继承自BaseDeviceEntity）
        entity.setJdCountry("TW");
        entity.setDataSource("台湾食品药物管理署");
        
        // 文档标题
        entity.setTitle(src.getTitle());
        
        // 文档类型
        entity.setDocumentType("医疗器材管理");
        
        // 发布日期（将民国年转换为西元年）
        if (src.getPublishDate() != null && !src.getPublishDate().isEmpty()) {
            try {
                LocalDate date = parseROCDate(src.getPublishDate());
                if (date != null) {
                    entity.setPublicationDate(date);
                }
            } catch (Exception e) {
                log.debug("日期解析失败: {}", src.getPublishDate());
            }
        }
        
        // 指导状态（默认为现行）
        entity.setGuidanceStatus("现行");
        
        // URL
        if (src.getDetailUrl() != null && !src.getDetailUrl().isEmpty()) {
            entity.setDocumentUrl(src.getDetailUrl());
            entity.setSourceUrl(src.getDetailUrl());
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
     * 支持多种格式：
     * - 109/08/10
     * - 109.08.10
     * - 109-08-10
     * - 1090810
     * - 109年8月10日
     */
    private LocalDate parseROCDate(String dateStr) {
        try {
            // 移除空格
            dateStr = dateStr.trim().replace(" ", "");
            
            if (dateStr.isEmpty()) {
                return null;
            }
            
            int year, month, day;
            
            // 格式1: 109/08/10 或 109.08.10 或 109-08-10
            if (dateStr.contains("/") || dateStr.contains(".") || dateStr.contains("-")) {
                String[] parts = dateStr.split("[/.\\-]");
                if (parts.length != 3) {
                    log.debug("日期格式不正确: {}", dateStr);
                    return null;
                }
                
                year = Integer.parseInt(parts[0]) + 1911; // 转换为西元年
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            }
            // 格式2: 109年8月10日
            else if (dateStr.contains("年")) {
                dateStr = dateStr.replace("年", "/").replace("月", "/").replace("日", "");
                String[] parts = dateStr.split("/");
                if (parts.length < 2) {
                    log.debug("日期格式不正确: {}", dateStr);
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
                log.debug("不支持的日期格式: {}", dateStr);
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
    private String saveToDatabase(List<TaiwanGuidanceData> dataList) {
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
            List<TaiwanGuidanceData> batch = dataList.subList(i, end);

            int batchNewCount = 0;

            for (TaiwanGuidanceData data : batch) {
                try {
                    GuidanceDocument entity = convertToEntity(data);

                    // 去重（按标题和数据源）
                    List<GuidanceDocument> existingList = guidanceRepository
                            .findByTitleAndDataSource(entity.getTitle(), "台湾食品药物管理署");

                    if (!existingList.isEmpty()) {
                        // 更新第一条记录
                        GuidanceDocument existingEntity = existingList.get(0);
                        existingEntity.setPublicationDate(entity.getPublicationDate());
                        existingEntity.setGuidanceStatus(entity.getGuidanceStatus());
                        existingEntity.setDocumentUrl(entity.getDocumentUrl());
                        existingEntity.setUpdateTime(LocalDateTime.now());
                        guidanceRepository.save(existingEntity);
                        updateCount++;
                    } else {
                        guidanceRepository.save(entity);
                        newCount++;
                        batchNewCount++;
                    }

                } catch (Exception e) {
                    log.error("保存法规文档失败: {}", data.getTitle(), e);
                    skipCount++;
                }
            }

            log.info("已处理 {}/{} 条记录", Math.min(i + BATCH_SIZE, dataList.size()), dataList.size());

            // 批次检测：检查是否应该停止（只统计新增数据）
            boolean shouldStop = detector.recordBatch(batch.size(), batchNewCount);
            if (shouldStop) {
                log.warn("⚠️ 检测到连续重复批次，停止保存剩余数据");
                break;
            }
        }

        // 打印最终统计
        detector.printFinalStats("TwGuidance");

        String result = String.format("✅ 台湾法规文档保存完成！总计: %d 条，新增: %d 条，更新: %d 条，跳过: %d 条",
                dataList.size(), newCount, updateCount, skipCount);
        log.info(result);
        return result;
    }
}

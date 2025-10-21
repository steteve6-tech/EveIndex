package com.certification.crawler.countrydata.tw;

import com.certification.entity.common.CustomsCase;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.CustomsCaseRepository;
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
import java.util.*;

/**
 * 台湾海关判例爬虫
 * 数据源: https://fbfh.trade.gov.tw/fh/ap/queryModFormf.do
 * 
 * API说明：
 * - 方法: POST (application/x-www-form-urlencoded)
 * - 关键参数:
 *   - q_goodCode: 货品代码（如9018表示医疗器械）
 *   - q_fromYear/q_fromMonth: 起始日期（民国年，如087=1998年）
 *   - q_toYear/q_toMonth: 结束日期（民国年，999表示最新）
 *   - currentPage: 当前页
 *   - pageSize: 每页显示数量
 * 
 * HTML结构：
 * - HS Code: #q1List > div > table > tbody > tr > td.text-nowrap
 * - 日期: #q1List > div > table > tbody > tr > td:nth-child(6) (民国年格式: 109/08/10)
 * - Ruling Result: #q1List > div > table > tbody > tr > td.arWebFont + 详情内容
 * - Violation Type: #q1List > div > table > tbody > tr > td:nth-child(1)
 */
@Slf4j
@Component
public class TwCustomsCase {

    private static final String SEARCH_URL = "https://fbfh.trade.gov.tw/fh/ap/queryModFormf.do";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 60000; // 60秒超时
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    
    // 医疗器械相关的CCC代码前缀
    private static final String DEFAULT_GOOD_CODE = "9018"; // HS编码90章：医疗器械

    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    /**
     * 台湾海关案例数据模型
     */
    @Data
    public static class TaiwanCustomsData {
        private String violationType;     // 异动类型（第1列）
        private String hsCode;            // HS编码（text-nowrap列）
        private String announceDate;      // 公告日期（第6列，民国年）
        private String rulingResult;      // 裁定结果（arWebFont + 详情）
        private String detailUrl;         // 详情链接
    }

    /**
     * 按货品代码搜索（医疗器械默认为9018开头）
     */
    public String crawlByGoodCode(String goodCode, int maxRecords) {
        log.info("开始爬取台湾海关判例，货品代码: {}, 最大记录数: {}", goodCode, maxRecords);
        
        try {
            // 默认查询最近3年的数据
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear() - 1911; // 转换为民国年
            int fromYear = currentYear - 3;
            
            return crawlByParams(goodCode, fromYear, 1, 999, 12, maxRecords);
            
        } catch (Exception e) {
            log.error("爬取台湾海关判例失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 按日期范围搜索
     * @param startDate 格式: yyyyMMdd (西元年)
     * @param endDate 格式: yyyyMMdd (西元年)
     */
    public String crawlByDateRange(String startDate, String endDate, int maxRecords) {
        log.info("开始爬取台湾海关判例，日期范围: {} - {}", startDate, endDate);
        
        try {
            // 转换日期格式
            LocalDate start = LocalDate.parse(startDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate end = LocalDate.parse(endDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 转换为民国年
            int fromYear = start.getYear() - 1911;
            int fromMonth = start.getMonthValue();
            int toYear = end.getYear() - 1911;
            int toMonth = end.getMonthValue();
            
            return crawlByParams(DEFAULT_GOOD_CODE, fromYear, fromMonth, toYear, toMonth, maxRecords);
            
        } catch (Exception e) {
            log.error("爬取台湾海关判例失败: {}", e.getMessage(), e);
            return "❌ 爬取失败: " + e.getMessage();
        }
    }

    /**
     * 核心爬取方法
     */
    private String crawlByParams(String goodCode, int fromYear, int fromMonth, 
                                  int toYear, int toMonth, int maxRecords) throws IOException {
        
        List<TaiwanCustomsData> allData = new ArrayList<>();
        int currentPage = 1;
        int pageSize = 100; // 每页100条
        boolean hasMore = true;
        boolean crawlAll = (maxRecords == -1); // maxRecords = -1 表示不限制数量

        while (hasMore && (crawlAll || allData.size() < maxRecords)) {
            log.info("正在爬取第{}页...", currentPage);
            
            // 构建POST请求
            Document doc = executeSearchWithRetry(goodCode, fromYear, fromMonth, 
                                                  toYear, toMonth, currentPage, pageSize);
            
            if (doc == null) {
                log.error("获取页面失败");
                break;
            }
            
            // 解析数据
            List<TaiwanCustomsData> pageData = parseResultTable(doc);
            log.info("第{}页解析到 {} 条记录", currentPage, pageData.size());
            
            if (pageData.isEmpty()) {
                hasMore = false;
            } else {
                allData.addAll(pageData);
                
                // 检查是否还有下一页
                hasMore = checkHasNextPage(doc, currentPage);
                currentPage++;
                
                // 延迟，避免请求过快
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // 达到最大记录数（仅当maxRecords为正数时才限制）
            if (!crawlAll && allData.size() >= maxRecords) {
                // 使用Math.min确保不会超出列表范围
                int endIndex = Math.min(maxRecords, allData.size());
                allData = allData.subList(0, endIndex);
                break;
            }
        }
        
        log.info("总共爬取到 {} 条海关判例数据", allData.size());
        
        // 保存到数据库
        return saveToDatabase(allData);
    }

    /**
     * 执行POST搜索请求（带重试）
     */
    private Document executeSearchWithRetry(String goodCode, int fromYear, int fromMonth,
                                           int toYear, int toMonth, int page, int pageSize) {
        
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                return executeSearch(goodCode, fromYear, fromMonth, toYear, toMonth, page, pageSize);
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
     * 执行POST搜索请求
     */
    private Document executeSearch(String goodCode, int fromYear, int fromMonth,
                                   int toYear, int toMonth, int page, int pageSize) throws IOException {
        
        log.debug("搜索参数 - 货品代码: {}, 日期: {}/{} - {}/{}, 页码: {}", 
                  goodCode, fromYear, fromMonth, toYear, toMonth, page);
        
        // 构建表单参数（完全按照实际请求）
        Map<String, String> data = new HashMap<>();
        data.put("id", "");
        data.put("state", "queryAll");
        data.put("queryAllFlag", "true");
        data.put("userID", "");
        data.put("userName", "");
        data.put("userDept", "");
        data.put("progID", "QueryModFormF");
        data.put("progName", "公告異動資料查詢");
        data.put("filestoreLocation", "D:\\app\\FileStore\\FH");
        data.put("uploadKey", "");
        data.put("editID", "");
        data.put("editDate", "");
        data.put("isFromPrint", "N");
        
        // 查询参数
        data.put("q_query_type", "1");
        data.put("q_fromYear", String.format("%03d", fromYear)); // 补齐3位
        data.put("q_fromMonth", String.format("%02d", fromMonth)); // 补齐2位
        data.put("q_toYear", String.format("%03d", toYear));
        data.put("q_toMonth", String.format("%02d", toMonth));
        data.put("q_goodCode", goodCode);
        data.put("q_sort", "2"); // 排序方式
        data.put("querySubmit", "查　　　詢");
        
        // 分页参数
        data.put("currentPageSize", String.valueOf(pageSize));
        data.put("currentPage", String.valueOf(page));
        data.put("listContainerActiveRowId", "");
        data.put("pageSize", String.valueOf(pageSize));
        data.put("currentPage1", String.valueOf(page));
        data.put("pageSize1", String.valueOf(pageSize));
        
        // 发送POST请求
        Connection.Response response = Jsoup.connect(SEARCH_URL)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Referer", SEARCH_URL)
                .data(data)
                .method(Connection.Method.POST)
                .timeout(TIMEOUT)
                .execute();
        
        return response.parse();
    }

    /**
     * 解析结果表格
     * 
     * HTML结构：
     * #q1List > div > table > tbody > tr
     *   - td:nth-child(1) - 异动类型 (violation_type)
     *   - td.text-nowrap - HS编码 (hs_code)
     *   - td:nth-child(6) - 日期 (民国年格式)
     *   - td.arWebFont - 裁定结果简述
     */
    private List<TaiwanCustomsData> parseResultTable(Document doc) {
        List<TaiwanCustomsData> dataList = new ArrayList<>();
        
        try {
            // 查找主表格容器
            Element q1List = doc.selectFirst("#q1List");
            if (q1List == null) {
                log.warn("未找到#q1List容器");
                return dataList;
            }
            
            // 查找表格中的所有数据行
            Elements rows = q1List.select("table tbody tr");
            log.debug("找到 {} 行数据", rows.size());
            
            for (Element row : rows) {
                TaiwanCustomsData data = parseTableRow(row);
                if (data != null) {
                    dataList.add(data);
                }
            }
            
        } catch (Exception e) {
            log.error("解析表格失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }

    /**
     * 解析表格行
     * 
     * 列结构：
     * 1. td:nth-child(1) - 异动类型
     * 2. td.text-nowrap - HS编码
     * 3. td:nth-child(6) - 公告日期（民国年）
     * 4. td.arWebFont - 裁定结果简述
     */
    private TaiwanCustomsData parseTableRow(Element row) {
        try {
            TaiwanCustomsData data = new TaiwanCustomsData();
            
            // 1. 异动类型 (第1列)
            Element col1 = row.selectFirst("td:nth-child(1)");
            if (col1 != null) {
                data.setViolationType(col1.text().trim());
            }
            
            // 2. HS编码 (text-nowrap列)
            Element hsCodeCol = row.selectFirst("td.text-nowrap");
            if (hsCodeCol != null) {
                data.setHsCode(hsCodeCol.text().trim());
            }
            
            // 3. 公告日期 (第6列，民国年格式)
            Element dateCol = row.selectFirst("td:nth-child(6)");
            if (dateCol != null) {
                data.setAnnounceDate(dateCol.text().trim());
            }
            
            // 4. 裁定结果简述 (arWebFont列)
            Element resultCol = row.selectFirst("td.arWebFont");
            if (resultCol != null) {
                data.setRulingResult(resultCol.text().trim());
            }
            
            // 5. 尝试获取详情链接
            Element detailLink = row.selectFirst("a[href]");
            if (detailLink != null) {
                String href = detailLink.attr("href");
                if (href != null && !href.isEmpty()) {
                    data.setDetailUrl(href);
                }
            }
            
            // 验证必填字段
            if (data.getHsCode() == null || data.getHsCode().isEmpty()) {
                log.debug("跳过无效行：缺少HS编码");
                return null;
            }
            
            log.debug("解析到数据 - HS编码: {}, 异动类型: {}, 日期: {}", 
                     data.getHsCode(), data.getViolationType(), data.getAnnounceDate());
            
            return data;
            
        } catch (Exception e) {
            log.debug("解析表格行失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否有下一页
     */
    private boolean checkHasNextPage(Document doc, int currentPage) {
        try {
            // 查找分页信息
            Elements pageInfo = doc.select(".pageinfo, .pagination, span:contains(页), span:contains(頁)");
            if (!pageInfo.isEmpty()) {
                String text = pageInfo.text();
                // 检查是否包含"下一页"或当前页不是最后一页
                return text.contains("下一頁") || text.contains("下一页") || text.contains("next");
            }
            
            // 查找"下一页"链接
            Elements nextLink = doc.select("a:contains(下一頁), a:contains(下一页), a:contains(next)");
            return !nextLink.isEmpty() && !nextLink.hasClass("disabled");
            
        } catch (Exception e) {
            log.debug("检查下一页失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 转换为实体对象
     */
    private CustomsCase convertToEntity(TaiwanCustomsData src) {
        CustomsCase entity = new CustomsCase();
        
        // 基本信息（继承自BaseDeviceEntity）
        entity.setJdCountry("TW");
        entity.setDataSource("台湾经济部国际贸易局");
        
        // 案例编号（使用HS编码作为唯一标识）
        entity.setCaseNumber(src.getHsCode());
        
        // HS编码
        entity.setHsCodeUsed(src.getHsCode());
        
        // 案例日期（将民国年转换为西元年）
        if (src.getAnnounceDate() != null && !src.getAnnounceDate().isEmpty()) {
            try {
                LocalDate date = parseROCDate(src.getAnnounceDate());
                if (date != null) {
                    entity.setCaseDate(date);
                }
            } catch (Exception e) {
                log.debug("日期解析失败: {}", src.getAnnounceDate());
            }
        }
        
        // 裁定结果（不翻译，直接使用原文）
        if (src.getRulingResult() != null && !src.getRulingResult().isEmpty()) {
            entity.setRulingResult(src.getRulingResult());
        }
        
        // 违规类型（异动类型）
        if (src.getViolationType() != null && !src.getViolationType().isEmpty()) {
            entity.setViolationType(src.getViolationType());
        } else {
            entity.setViolationType("货品号列异动");
        }
        
        // 风险等级（默认中等）
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
     * 格式: 109/08/10 (民国年/月/日)
     * 
     * 民国年 = 西元年 - 1911
     * 例如：109年 = 2020年
     */
    private LocalDate parseROCDate(String rocDateStr) {
        try {
            // 移除空格
            rocDateStr = rocDateStr.trim().replace(" ", "");
            
            if (rocDateStr.isEmpty()) {
                return null;
            }
            
            int year, month, day;
            
            if (rocDateStr.contains("/")) {
                // 格式: 109/08/10
                String[] parts = rocDateStr.split("/");
                if (parts.length != 3) {
                    log.debug("日期格式不正确: {}", rocDateStr);
                    return null;
                }
                
                year = Integer.parseInt(parts[0]) + 1911; // 转换为西元年
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            } else if (rocDateStr.length() == 7) {
                // 格式: 1090810
                year = Integer.parseInt(rocDateStr.substring(0, 3)) + 1911;
                month = Integer.parseInt(rocDateStr.substring(3, 5));
                day = Integer.parseInt(rocDateStr.substring(5, 7));
            } else {
                log.debug("不支持的日期格式: {}", rocDateStr);
                return null;
            }
            
            LocalDate date = LocalDate.of(year, month, day);
            log.debug("民国年 {} 转换为西元年 {}", rocDateStr, date);
            return date;
            
        } catch (Exception e) {
            log.debug("解析民国年日期失败: {} - {}", rocDateStr, e.getMessage());
            return null;
        }
    }

    /**
     * 保存到数据库
     */
    private String saveToDatabase(List<TaiwanCustomsData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "⚠️ 没有数据需要保存";
        }
        
        int newCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        
        // 分批处理
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, dataList.size());
            List<TaiwanCustomsData> batch = dataList.subList(i, end);
            
            for (TaiwanCustomsData data : batch) {
                try {
                    // 转换为实体
                    CustomsCase entity = convertToEntity(data);
                    
                    // 检查是否已存在（按HS编码去重）
                    Optional<CustomsCase> existing = customsCaseRepository
                            .findByCaseNumber(entity.getCaseNumber());
                    
                    if (existing.isPresent()) {
                        // 更新现有记录
                        CustomsCase existingEntity = existing.get();
                        existingEntity.setRulingResult(entity.getRulingResult());
                        existingEntity.setViolationType(entity.getViolationType());
                        existingEntity.setCaseDate(entity.getCaseDate());
                        // remark保持原值，不更新
                        existingEntity.setUpdateTime(LocalDateTime.now());
                        customsCaseRepository.save(existingEntity);
                        updateCount++;
                    } else {
                        // 保存新记录
                        customsCaseRepository.save(entity);
                        newCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("保存海关判例失败: HS编码={}", data.getHsCode(), e);
                    skipCount++;
                }
            }
            
            log.info("已处理 {}/{} 条记录", Math.min(i + BATCH_SIZE, dataList.size()), dataList.size());
        }
        
        String result = String.format("✅ 台湾海关判例保存完成！总计: %d 条，新增: %d 条，更新: %d 条，跳过: %d 条",
                dataList.size(), newCount, updateCount, skipCount);
        log.info(result);
        return result;
    }
}

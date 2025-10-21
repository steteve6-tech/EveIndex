package com.certification.crawler.countrydata.jp;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.GuidanceDocument;
import com.certification.repository.common.GuidanceDocumentRepository;
import lombok.Data;
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
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * 日本PMDA法规指导文档爬虫
 * 数据源: https://www.std.pmda.go.jp/scripts/stdDB_en/kijyun/
 */
@Slf4j
@Component
public class JpGuidance {

    private static final String BASE_SEARCH_URL = "https://www.std.pmda.go.jp/scripts/stdDB_en/kijyun/stdDB_kijyun_resl.cgi";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;

    /**
     * 日本法规数据模型
     * 
     * 标题格式示例：
     * "CC3-139: Skin electrical conductivity measuring instrument criteria"
     * "CC3-523: Skin grafting dilator criteria"
     */
    @Data
    public static class JapanGuidanceData {
        private String criteriaId;          // 法规ID（从URL的ID参数提取，如：1300523）
        private String title;               // 完整标题（如：CC3-139: Skin electrical conductivity...）
        private String intendedUse;         // 用途和适应症 (从详情页提取)
        private String criteriaNumber;      // 基准番号（如：CC3-139，从标题提取）
        private String status;              // 状态
        private String detailUrl;           // 详情页URL（完整URL）
        private LocalDate publicationDate;  // 发布日期（从详情页提取）
    }

    /**
     * 基于两个搜索参数爬取法规数据
     * @param criteriaName 基准名称 (Q_kjn_kname)
     * @param effectName 功效 (Q_kjn_effect_name)
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @return 爬取结果
     * 
     * 注意：不使用方法级事务，每条记录独立保存
     */
    public String crawlWithParams(String criteriaName, String effectName, int maxRecords, int batchSize) {
        log.info("🚀 开始爬取日本PMDA法规指导文档数据");
        log.info("📊 基准名称(Q_kjn_kname): {}, 功效(Q_kjn_effect_name): {}, 最大记录数: {}", 
            criteriaName, effectName, maxRecords);

        try {
            // 1. 爬取搜索列表页，获取所有法规列表
            List<JapanGuidanceData> guidanceList = crawlSearchResults(criteriaName, effectName, maxRecords);
            
            if (guidanceList.isEmpty()) {
                return "未找到任何法规数据";
            }

            log.info("✅ 获取到 {} 条法规列表", guidanceList.size());

            // 2. 对每条法规，访问详情页获取完整信息
            List<JapanGuidanceData> completeDataList = new ArrayList<>();
            for (int i = 0; i < guidanceList.size(); i++) {
                JapanGuidanceData guidance = guidanceList.get(i);
                try {
                    log.info("📄 正在爬取详情 [{}/{}]: {}", i + 1, guidanceList.size(), guidance.getTitle());
                    JapanGuidanceData completeData = crawlDetailPage(guidance);
                    if (completeData != null) {
                        completeDataList.add(completeData);
                    }
                    
                    // 避免请求过快
                    if (i < guidanceList.size() - 1) {
                        Thread.sleep(1500);
                    }
                } catch (Exception e) {
                    log.error("爬取详情页失败: {} - {}", guidance.getTitle(), e.getMessage());
                }
            }

            // 3. 保存到数据库
            return saveToDatabase(completeDataList, batchSize);

        } catch (Exception e) {
            log.error("爬取日本法规数据失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 基于关键词爬取法规数据（兼容旧接口）
     * @deprecated 使用 crawlWithParams(String, String, int, int) 代替
     */
    @Deprecated
    public String crawlWithKeyword(String searchKeyword, int maxRecords, int batchSize) {
        return crawlWithParams(searchKeyword, null, maxRecords, batchSize);
    }

    /**
     * 爬取搜索结果列表页
     */
    private List<JapanGuidanceData> crawlSearchResults(String criteriaName, String effectName, int maxRecords) throws Exception {
        List<JapanGuidanceData> dataList = new ArrayList<>();
        
        String url = buildSearchUrl(criteriaName, effectName);
        log.info("🔍 请求搜索URL: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "frame")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("upgrade-insecure-requests", "1")
                .referrer("https://www.std.pmda.go.jp/scripts/stdDB_en/kijyun/stdDB_kijyun_resframe_main.cgi")
                .timeout(30000)
                .get();

        // 调试：打印页面结构
        log.debug("========== 搜索结果页面结构调试 ==========");
        log.debug("页面标题: {}", doc.title());
        
        // 查找搜索结果容器
        Element searchList = doc.selectFirst("div#searchlist");
        if (searchList == null) {
            log.warn("未找到搜索结果容器 div#searchlist");
            return dataList;
        }
        
        // 查找包含结果的表格行
        Elements rows = searchList.select("table tbody tr");
        log.info("✅ 在 div#searchlist 中找到 {} 行结果", rows.size());
        
        if (rows.isEmpty()) {
            log.warn("未找到结果数据，页面内容预览:");
            String bodyText = doc.body().text();
            log.warn(bodyText.length() > 500 ? bodyText.substring(0, 500) : bodyText);
            return dataList;
        }

        // 解析每一行（所有行都是数据行，没有表头）
        int count = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (maxRecords > 0 && count >= maxRecords) {
                break;
            }
            
            Element row = rows.get(i);
            try {
                JapanGuidanceData data = parseSearchResultRow(row);
                if (data != null) {
                    dataList.add(data);
                    count++;
                    log.debug("✅ 解析行 {}: {}", i + 1, data.getTitle());
                }
            } catch (Exception e) {
                log.warn("解析行 {} 失败: {}", i + 1, e.getMessage());
            }
        }

        log.info("📊 搜索结果页解析完成，获取 {} 条记录", dataList.size());
        return dataList;
    }

    /**
     * 构建搜索URL
     * @param criteriaName 基准名称 (Q_kjn_kname)
     * @param effectName 功效 (Q_kjn_effect_name)
     */
    private String buildSearchUrl(String criteriaName, String effectName) {
        StringBuilder url = new StringBuilder(BASE_SEARCH_URL);
        url.append("?");
        
        // 添加复选框参数（搜索所有类型）
        url.append("chk_kjn=&chk_kjn=ninsyou1&chk_kjn=ninsyou2&chk_kjn=ninsyou3&chk_kjn=syounin&chk_kjn=guideline");
        
        // 参数1: 基准名称 (Q_kjn_kname)
        url.append("&Q_kjn_kname=");
        if (criteriaName != null && !criteriaName.trim().isEmpty()) {
            url.append(encodeUrl(criteriaName.trim()));
        }
        
        // 参数2: 功效 (Q_kjn_effect_name)
        url.append("&Q_kjn_effect_name=");
        if (effectName != null && !effectName.trim().isEmpty()) {
            url.append(encodeUrl(effectName.trim()));
        }
        
        // 其他参数
        url.append("&Q_kt_num_select_list=");
        url.append("&mode=0");
        url.append("&allchk=");
        
        return url.toString();
    }

    /**
     * 解析搜索结果行
     * HTML结构: <tr><td><font><a href="...">CC3-139: Skin electrical conductivity...</a></font></td></tr>
     */
    private JapanGuidanceData parseSearchResultRow(Element row) {
        try {
            // 查找链接元素（在 td -> font -> a 中）
            Element link = row.selectFirst("td a[href]");
            
            if (link == null) {
                log.debug("该行没有链接，跳过");
                return null;
            }

            // 提取标题文本
            // 格式: "CC3-139: Skin electrical conductivity measuring instrument criteria"
            String fullTitle = link.text().trim();
            
            if (fullTitle.isEmpty()) {
                return null;
            }

            JapanGuidanceData data = new JapanGuidanceData();
            data.setTitle(fullTitle);
            
            // 提取基准番号（例如：CC3-139）
            if (fullTitle.contains(":")) {
                String criteriaNumber = fullTitle.substring(0, fullTitle.indexOf(":")).trim();
                data.setCriteriaNumber(criteriaNumber);
            }
            
            // 提取详情URL
            String href = link.attr("href");
            if (href.isEmpty()) {
                return null;
            }
            
            // 构建完整的详情URL
            if (href.startsWith("/")) {
                data.setDetailUrl("https://www.std.pmda.go.jp" + href);
            } else if (!href.startsWith("http")) {
                data.setDetailUrl("https://www.std.pmda.go.jp/scripts/stdDB_en/kijyun/" + href);
            } else {
                data.setDetailUrl(href);
            }
            
            // 从URL提取参数
            // URL格式: /scripts/stdDB_en/kijyun/stdDB_kijyun_resr.cgi?Sig=1&kjn_betsunum=3;kjn_no_parm=523;kjn=ninsyou&ID=1300523
            data.setCriteriaId(extractUrlParam(href, "ID"));
            
            String kjnNoParm = extractUrlParam(href, "kjn_no_parm");
            if (kjnNoParm != null) {
                // kjn_no_parm 可能包含分号，需要提取
                if (kjnNoParm.contains(";")) {
                    kjnNoParm = kjnNoParm.substring(0, kjnNoParm.indexOf(";"));
                }
                log.debug("提取到 kjn_no_parm: {}, ID: {}", kjnNoParm, data.getCriteriaId());
            }

            return data;

        } catch (Exception e) {
            log.warn("解析搜索结果行失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从URL中提取参数值
     */
    private String extractUrlParam(String url, String paramName) {
        try {
            int startIdx = url.indexOf(paramName + "=");
            if (startIdx == -1) {
                return null;
            }
            
            startIdx += paramName.length() + 1; // 跳过 "paramName="
            
            int endIdx = url.indexOf("&", startIdx);
            if (endIdx == -1) {
                return url.substring(startIdx);
            } else {
                return url.substring(startIdx, endIdx);
            }
        } catch (Exception e) {
            log.warn("提取URL参数失败: {} from {}", paramName, url);
            return null;
        }
    }

    /**
     * 爬取详情页获取完整信息
     */
    private JapanGuidanceData crawlDetailPage(JapanGuidanceData basicData) throws Exception {
        if (basicData.getDetailUrl() == null || basicData.getDetailUrl().isEmpty()) {
            log.warn("详情URL为空，跳过");
            return basicData;
        }

        log.debug("🔍 请求详情URL: {}", basicData.getDetailUrl());

        Document doc = Jsoup.connect(basicData.getDetailUrl())
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "frame")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .referrer(BASE_SEARCH_URL)
                .timeout(30000)
                .get();

        // 解析详情页内容
        parseDetailPage(doc, basicData);

        return basicData;
    }

    /**
     * 解析详情页内容
     * 提取：
     * 1. publication_date - 发布日期 (Last Updated)
     * 2. topic - 定义 (Definition from JMDN section)
     * 3. description - 预期用途 (Intended use and indication)
     */
    private void parseDetailPage(Document doc, JapanGuidanceData data) {
        try {
            // 1. 提取发布日期 (Last Updated)
            // 查找包含 "(Last Updated)" 的元素
            Elements allElements = doc.select("font, div");
            for (Element elem : allElements) {
                String text = elem.text().trim();
                if (text.contains("(Last Updated)") || text.contains("(最終改訂)")) {
                    // 提取日期部分，格式：2024/02/13  (Last Updated)
                    String dateStr = text.split("\\s+")[0]; // 取第一个空格前的部分
                    LocalDate date = parseDate(dateStr);
                    if (date != null) {
                        data.setPublicationDate(date);
                        log.debug("提取到发布日期: {}", dateStr);
                        break;
                    }
                }
            }
            
            // 2. 提取定义 (Definition) 作为 topic
            // 在 JMDN 表格中查找 Definition 行
            Elements jmdnTables = doc.select("table[id^=jmdn_table]");
            for (Element table : jmdnTables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Element th = row.selectFirst("td[bgcolor=#e0e0e0]");
                    Element td = row.selectFirst("td[bgcolor=#ffffff]");
                    
                    if (th != null && td != null) {
                        String label = th.text().trim();
                        if ("Definition".equals(label)) {
                            String definition = td.text().trim();
                            // 保存原始定义作为 intendedUse 的一部分
                            if (data.getIntendedUse() == null || data.getIntendedUse().isEmpty()) {
                                data.setIntendedUse(definition);
                            }
                            log.debug("提取到定义 (Definition): {}", 
                                definition.length() > 100 ? definition.substring(0, 100) + "..." : definition);
                            break;
                        }
                    }
                }
            }
            
            // 3. 提取预期用途 (Intended use and indication)
            // 查找 "［Intended use and indication］" 部分
            Elements tables = doc.select("table");
            boolean foundIntendedUse = false;
            
            for (int i = 0; i < tables.size(); i++) {
                Element table = tables.get(i);
                String tableText = table.text();
                
                if (tableText.contains("［Intended use and indication］") || 
                    tableText.contains("[Intended use and indication]")) {
                    
                    // 找到标题后，下一个 td 包含实际内容
                    Elements cells = table.select("td");
                    for (Element cell : cells) {
                        String cellText = cell.text().trim();
                        // 跳过标题行
                        if (!cellText.contains("［Intended use and indication］") && 
                            !cellText.contains("[Intended use and indication]") &&
                            !cellText.isEmpty() &&
                            cellText.length() > 20) { // 确保是有效内容
                            
                            data.setIntendedUse(cellText);
                            foundIntendedUse = true;
                            log.debug("提取到预期用途: {}", 
                                cellText.length() > 100 ? cellText.substring(0, 100) + "..." : cellText);
                            break;
                        }
                    }
                    
                    if (foundIntendedUse) {
                        break;
                    }
                }
            }
            
            // 4. 如果没有提取到预期用途，尝试从 div#kijyun_info_03 中提取
            if (!foundIntendedUse) {
                Element intendedUseDiv = doc.selectFirst("div#kijyun_info_03:contains(To measure)");
                if (intendedUseDiv != null) {
                    String intendedUse = intendedUseDiv.text().trim();
                    if (!intendedUse.isEmpty()) {
                        data.setIntendedUse(intendedUse);
                        log.debug("从 div 提取到预期用途: {}", 
                            intendedUse.length() > 100 ? intendedUse.substring(0, 100) + "..." : intendedUse);
                    }
                }
            }

            log.debug("✅ 详情页解析完成 - 标题: {}, 日期: {}, 用途: {}", 
                data.getTitle(), 
                data.getPublicationDate(),
                data.getIntendedUse() != null ? data.getIntendedUse().substring(0, Math.min(50, data.getIntendedUse().length())) : "无");

        } catch (Exception e) {
            log.error("解析详情页失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存到数据库
     * 注意：不使用@Transactional，每条记录独立保存，避免一条失败影响全部
     */
    private String saveToDatabase(List<JapanGuidanceData> dataList, int batchSize) {
        if (dataList.isEmpty()) {
            return "没有数据需要保存";
        }

        log.info("成功爬取到 {} 条法规数据，开始保存到数据库", dataList.size());

        int totalSaved = 0;
        int totalDuplicates = 0;
        int totalErrors = 0;

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);
        int currentBatchSaved = 0;
        int processedInBatch = 0;

        for (JapanGuidanceData data : dataList) {
            try {
                // 每条记录独立保存
                boolean saved = saveOneRecord(data);
                if (saved) {
                    totalSaved++;
                    currentBatchSaved++;
                } else {
                    totalDuplicates++;
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
                totalErrors++;
                log.error("保存法规数据时出错: {} - {}", data.getTitle(), e.getMessage());
            }
        }

        // 打印最终统计
        detector.printFinalStats("JpGuidance");

        log.info("保存完成，新增: {} 条，重复: {} 条，失败: {} 条", totalSaved, totalDuplicates, totalErrors);
        return String.format("日本法规数据保存完成，新增: %d 条，重复: %d 条，失败: %d 条",
            totalSaved, totalDuplicates, totalErrors);
    }
    
    /**
     * 保存单条记录（独立事务）
     * @return true=保存成功, false=重复跳过
     */
    @Transactional
    private boolean saveOneRecord(JapanGuidanceData data) {
        try {
            GuidanceDocument entity = convertToEntity(data);

            // 检查重复（使用标题和数据源）
            if (checkDuplicate(entity)) {
                return false; // 重复，跳过
            }

            guidanceDocumentRepository.save(entity);
            return true; // 保存成功
            
        } catch (Exception e) {
            log.error("保存单条记录失败: {} - {}", data.getTitle(), e.getMessage());
            throw e; // 抛出异常，让调用方捕获
        }
    }

    /**
     * 转换为实体对象
     * 字段映射：
     * - title: 完整标题（CC3-139: Skin electrical conductivity...）
     * - topic: Definition（定义） - 如果长度超过255则截断
     * - description: Intended use and indication（预期用途）- 完整内容
     * - publicationDate: Last Updated 日期
     * - guidanceStatus: 基准番号（CC3-139）
     * 
     * 注意：不进行翻译，直接保存英文原文
     */
    private GuidanceDocument convertToEntity(JapanGuidanceData src) {
        GuidanceDocument entity = new GuidanceDocument();

        // 设置文档类型
        entity.setDocumentType("GUIDANCE");

        // 设置标题（完整标题，如：CC3-139: Skin electrical conductivity...）
        // 不翻译，直接使用原文
        entity.setTitle(truncateString(src.getTitle(), 500));

        // 设置预期用途和定义
        // intendedUse 包含了 Definition 或 Intended use and indication
        // 不翻译，直接使用英文原文
        String intendedUse = src.getIntendedUse();
        
        if (intendedUse != null && !intendedUse.isEmpty()) {
            // topic: 存储简短版本（最多255字符）
            entity.setTopic(truncateString(intendedUse, 255));
            
            // description: 存储完整内容
            entity.setDescription(intendedUse);
        }

        // 设置URL
        entity.setDocumentUrl(src.getDetailUrl());
        entity.setSourceUrl(BASE_SEARCH_URL);

        // 设置发布日期（Last Updated）
        entity.setPublicationDate(src.getPublicationDate());

        // 设置基准番号（如：CC3-139）
        if (src.getCriteriaNumber() != null && !src.getCriteriaNumber().isEmpty()) {
            entity.setGuidanceStatus("Criteria No: " + src.getCriteriaNumber());
        }

        // 设置数据源（缩短以适应数据库字段长度限制）
        entity.setDataSource("PMDA Japan");
        entity.setJdCountry("JP");

        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setKeywords(null);
        entity.setDataStatus("ACTIVE");
        entity.setCrawlTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        return entity;
    }

    /**
     * 检查是否重复
     * 使用标题和数据源进行去重
     */
    private boolean checkDuplicate(GuidanceDocument entity) {
        if (entity.getTitle() == null || entity.getTitle().isEmpty()) {
            return false;
        }

        try {
            // 使用标题和数据源查找
            List<GuidanceDocument> existing = guidanceDocumentRepository.findByTitleAndDataSource(
                entity.getTitle(), entity.getDataSource());

            if (!existing.isEmpty()) {
                log.debug("发现重复记录: {}", entity.getTitle());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("检查重复时出错: {} - {}", entity.getTitle(), e.getMessage());
            return false; // 出错时假设不重复，尝试保存
        }
    }

    /**
     * 解析日期
     * 支持格式：
     * - 2024/02/13
     * - 2024-02-13
     * - 2024年2月13日
     * - 20240213
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            String originalDateStr = dateStr;
            dateStr = dateStr.trim();
            
            // 先尝试直接解析斜杠格式（如 2024/02/13）
            String[] slashPatterns = {"yyyy/M/d", "yyyy/MM/dd", "yyyy/M/dd", "yyyy/MM/d"};
            for (String pattern : slashPatterns) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }
            
            // 处理日语日期格式
            dateStr = dateStr
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "")
                .replace("/", "-")
                .replace(".", "-");

            String[] patterns = {"yyyy-M-d", "yyyy-MM-dd", "yyyyMMdd", "yyyy-M", "yyyy-MM"};
            for (String pattern : patterns) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
                } catch (Exception ignored) {
                }
            }

            log.warn("日期解析失败: {}", originalDateStr);
            return null;

        } catch (Exception e) {
            log.warn("日期解析异常: {} - {}", dateStr, e.getMessage());
            return null;
        }
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        log.warn("字段内容过长，已截断至{}字符", maxLength);
        return str.substring(0, maxLength);
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
}


package com.certification.crawler.countrydata.kr;

import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.entity.common.GuidanceDocument;
import com.certification.repository.common.GuidanceDocumentRepository;
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
 * 韩国医疗器械指导文档爬虫
 * 数据来源：韩国食品药品安全处 (MFDS - Ministry of Food and Drug Safety)
 * API地址：https://emedi.mfds.go.kr/brd/MNU20456
 */
@Slf4j
@Component
public class KrGuidance {

    private static final String BASE_URL = "https://emedi.mfds.go.kr/brd/MNU20456";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;

    @Autowired
    private TranslateAI translateAI;

    /**
     * 韩国指导文档数据模型
     */
    public static class KoreaGuidanceData {
        private String documentNumber;     // 문서번호 (文档编号)
        private String title;             // 제목 (标题)
        private String content;           // 내용 (内容)
        private String documentType;      // 문서유형 (文档类型)
        private String category;          // 분류 (分类)
        private LocalDate publicationDate; // 게시일 (发布日期)
        private String author;            // 작성자 (作者)
        private String documentUrl;       // 문서 URL (文档链接)
        private String status;            // 상태 (状态)

        // Getters and Setters
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDate getPublicationDate() { return publicationDate; }
        public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getDocumentUrl() { return documentUrl; }
        public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 基于关键词列表爬取数据
     * @param searchKeywords 搜索关键词列表
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @return 爬取结果
     */
    @Transactional
    public String crawlWithKeywords(List<String> searchKeywords, int maxRecords, int batchSize) {
        log.info("🚀 开始基于关键词列表爬取韩国指导文档数据");
        log.info("📊 关键词数量: {}, 最大记录数: {}", 
                searchKeywords != null ? searchKeywords.size() : 0,
                maxRecords == -1 ? "所有数据" : maxRecords);

        int totalSaved = 0;
        
        // 如果关键词列表为空，执行默认搜索
        if (searchKeywords == null || searchKeywords.isEmpty()) {
            return crawlAndSaveToDatabase(null, maxRecords, batchSize);
        }

        // 遍历关键词
        for (String keyword : searchKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) continue;
            
            try {
                log.info("\n🔍 处理搜索关键词: {}", keyword);
                String result = crawlAndSaveToDatabase(keyword.trim(), maxRecords, batchSize);
                totalSaved += extractSavedCount(result);
                Thread.sleep(2000); // 添加延迟
            } catch (Exception e) {
                log.error("处理搜索关键词 '{}' 失败: {}", keyword, e.getMessage());
            }
        }

        return String.format("韩国指导文档数据爬取完成，总共保存: %d 条记录", totalSaved);
    }

    /**
     * 爬取韩国指导文档数据并保存到数据库
     * @param searchKeyword 搜索关键词 (searchKwd)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @return 保存结果
     */
    @Transactional
    public String crawlAndSaveToDatabase(String searchKeyword, int maxRecords, int batchSize) {
        log.info("🚀 开始爬取韩国MFDS指导文档数据");
        log.info("📊 搜索关键词: {}, 最大记录数: {}, 批次大小: {}", 
                searchKeyword, maxRecords == -1 ? "所有数据" : maxRecords, batchSize);

        try {
            List<KoreaGuidanceData> guidanceDataList = crawlGuidanceData(searchKeyword, maxRecords);
            
            if (guidanceDataList.isEmpty()) {
                log.warn("未获取到韩国指导文档数据");
                return "未获取到指导文档数据";
            }
            
            log.info("成功爬取到 {} 条指导文档数据，开始保存到数据库", guidanceDataList.size());
            
            return saveBatchToDatabase(guidanceDataList, batchSize);
            
        } catch (Exception e) {
            log.error("爬取韩国指导文档数据失败", e);
            return "爬取失败: " + e.getMessage();
        }
    }

    /**
     * 爬取指导文档数据（核心方法）
     */
    private List<KoreaGuidanceData> crawlGuidanceData(String searchKeyword, int maxRecords) throws Exception {
        List<KoreaGuidanceData> allData = new ArrayList<>();
        int pageNum = 1;
        int totalFetched = 0;
        boolean crawlAll = (maxRecords == -1);

        int consecutiveEmptyPages = 0; // 连续空页面计数
        int maxEmptyPages = 3; // 最大允许连续空页面数
        
        while (crawlAll || totalFetched < maxRecords) {
            try {
                log.info("📄 正在爬取第 {} 页", pageNum);
                
                String url = buildUrl(searchKeyword, pageNum);
                log.debug("请求URL: {}", url);
                
                // 构建正确的referrer（第一页没有referrer，后续页面使用前一页）
                String referrer = (pageNum == 1) ? null : buildUrl(searchKeyword, pageNum - 1);
                
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"Windows\"")
                        .header("sec-fetch-dest", "document")
                        .header("sec-fetch-mode", "navigate")
                        .header("sec-fetch-site", "same-origin")
                        .header("sec-fetch-user", "?1")
                        .header("upgrade-insecure-requests", "1")
                        .referrer(referrer)  // 修改：使用动态referrer
                        .timeout(30000)
                        .get();

                List<KoreaGuidanceData> pageData = parseGuidanceData(doc);
                
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
                
                // 检查是否还有下一页（通过分页控件判断）
                boolean hasNextPage = checkHasNextPage(doc, pageNum);
                if (!hasNextPage && pageData.size() < 10) { // 如果页面数据少于10条且没有下一页，可能已到最后一页
                    log.info("第 {} 页数据较少且无下一页，可能已到最后一页", pageNum);
                    // 不立即break，继续处理当前页数据
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

        log.info("📊 韩国指导文档数据爬取完成，共获取 {} 条数据", allData.size());
        return allData;
    }

    /**
     * 构建请求URL
     * 
     * @param searchKeyword 搜索关键词 (searchKwd)
     * @param pageNum 页码
     * @return 完整的请求URL
     */
    private String buildUrl(String searchKeyword, int pageNum) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?pageNum=").append(pageNum);
        url.append("&searchYn=");  // 修改：searchYn应该为空，不是true
        url.append("&searchType=ALL");
        
        // 搜索关键词 (searchKwd)
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            url.append("&searchKwd=").append(encodeUrl(searchKeyword));
        } else {
            url.append("&searchKwd=");
        }
        
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
            Elements pageNumbers = pagination.select("a[href*='pageNum']");
            for (Element pageLink : pageNumbers) {
                String href = pageLink.attr("href");
                if (href.contains("pageNum=")) {
                    try {
                        String pageStr = href.substring(href.indexOf("pageNum=") + 8);
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
     * 解析指导文档数据
     */
    private List<KoreaGuidanceData> parseGuidanceData(Document doc) {
        List<KoreaGuidanceData> dataList = new ArrayList<>();
        
        try {
            // 查找数据表格或列表
            Elements rows = doc.select("table tbody tr");
            
            if (rows.isEmpty()) {
                // 尝试其他可能的选择器
                rows = doc.select("tr[class*=data], tr[class*=row], .list-table tr, .board-list tr");
            }
            
            // 也可能是列表形式
            if (rows.isEmpty()) {
                Elements items = doc.select(".board-list li, .notice-list li, .list-item");
                if (!items.isEmpty()) {
                    return parseListItems(items);
                }
            }
            
            log.debug("找到 {} 行数据", rows.size());

            for (Element row : rows) {
                try {
                    KoreaGuidanceData data = parseRow(row);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("解析指导文档数据失败", e);
        }

        return dataList;
    }

    /**
     * 解析单行数据（表格形式）
     */
    private KoreaGuidanceData parseRow(Element row) {
        try {
            Elements cols = row.select("td");
            
            if (cols.size() < 2) {
                return null;
            }

            KoreaGuidanceData data = new KoreaGuidanceData();
            
            // 根据实际表格列顺序调整索引
            // 实际列顺序：编号、标题、发布日期、作者、分类、状态等
            int colIndex = 0;
            
            // 可能第一列是编号
            if (cols.size() > colIndex) {
                String firstCol = cols.get(colIndex).text().trim();
                // 如果第一列是数字，可能是编号
                if (firstCol.matches("\\d+")) {
                    data.setDocumentNumber(firstCol);
                    colIndex++;
                }
            }
            
            // 标题（通常包含链接）
            if (cols.size() > colIndex) {
                Element titleCol = cols.get(colIndex);
                data.setTitle(titleCol.text().trim());
                
                // 尝试提取文档链接
                Element link = titleCol.selectFirst("a[href]");
                if (link != null) {
                    String href = link.attr("href");
                    if (!href.isEmpty()) {
                        // 构建完整URL
                        if (href.startsWith("/")) {
                            data.setDocumentUrl("https://emedi.mfds.go.kr" + href);
                        } else if (!href.startsWith("http")) {
                            data.setDocumentUrl("https://emedi.mfds.go.kr/brd/" + href);
                        } else {
                            data.setDocumentUrl(href);
                        }
                    }
                }
                colIndex++;
            }
            
            // 发布日期（原先被误认为是分类的列实际是日期）
            if (cols.size() > colIndex) {
                String dateStr = cols.get(colIndex++).text().trim();
                data.setPublicationDate(parseDate(dateStr));
            }
            
            // 作者
            if (cols.size() > colIndex) {
                data.setAuthor(cols.get(colIndex++).text().trim());
            }
            
            // 分类或文档类型（如果还有其他列）
            if (cols.size() > colIndex) {
                data.setCategory(cols.get(colIndex++).text().trim());
            }
            
            // 状态
            if (cols.size() > colIndex) {
                data.setStatus(cols.get(colIndex++).text().trim());
            }

            return data;
            
        } catch (Exception e) {
            log.warn("解析行数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析列表项数据（列表形式）
     */
    private List<KoreaGuidanceData> parseListItems(Elements items) {
        List<KoreaGuidanceData> dataList = new ArrayList<>();
        
        for (Element item : items) {
            try {
                KoreaGuidanceData data = new KoreaGuidanceData();
                
                // 提取标题
                Element titleElem = item.selectFirst(".title, .subject, h3, h4");
                if (titleElem != null) {
                    data.setTitle(titleElem.text().trim());
                }
                
                // 提取链接
                Element linkElem = item.selectFirst("a[href]");
                if (linkElem != null) {
                    String href = linkElem.attr("href");
                    if (!href.isEmpty()) {
                        if (href.startsWith("/")) {
                            data.setDocumentUrl("https://emedi.mfds.go.kr" + href);
                        } else if (!href.startsWith("http")) {
                            data.setDocumentUrl("https://emedi.mfds.go.kr/brd/" + href);
                        } else {
                            data.setDocumentUrl(href);
                        }
                    }
                }
                
                // 提取日期
                Element dateElem = item.selectFirst(".date, .reg-date, time");
                if (dateElem != null) {
                    data.setPublicationDate(parseDate(dateElem.text().trim()));
                }
                
                // 提取分类
                Element categoryElem = item.selectFirst(".category, .type");
                if (categoryElem != null) {
                    data.setCategory(categoryElem.text().trim());
                }
                
                if (data.getTitle() != null && !data.getTitle().isEmpty()) {
                    dataList.add(data);
                }
                
            } catch (Exception e) {
                log.warn("解析列表项失败: {}", e.getMessage());
            }
        }
        
        return dataList;
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
    private String saveBatchToDatabase(List<KoreaGuidanceData> records, int batchSize) {
        if (records == null || records.isEmpty()) {
            return "0 条记录";
        }

        int savedCount = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        for (int i = 0; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            List<KoreaGuidanceData> batch = records.subList(i, endIndex);
            batchCount++;

            List<GuidanceDocument> newRecords = new ArrayList<>();
            int batchDuplicateCount = 0;

            for (KoreaGuidanceData record : batch) {
                try {
                    GuidanceDocument entity = convertToEntity(record);
                    
                    // 使用标题+日期组合检查重复（因为可能没有唯一编号）
                    boolean isDuplicate = checkDuplicate(entity);

                    if (!isDuplicate) {
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
                    List<GuidanceDocument> savedRecords = guidanceDocumentRepository.saveAll(newRecords);
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
     * 检查是否重复
     */
    private boolean checkDuplicate(GuidanceDocument entity) {
        if (entity.getTitle() == null || entity.getTitle().isEmpty()) {
            return false;
        }
        
        // 使用标题和来源查找
        List<GuidanceDocument> existing = guidanceDocumentRepository.findByTitleAndDataSource(
            entity.getTitle(), "MFDS");
        
        return !existing.isEmpty();
    }

    /**
     * 将韩国指导文档数据转换为实体
     */
    private GuidanceDocument convertToEntity(KoreaGuidanceData src) {
        if (src == null) return null;

        GuidanceDocument entity = new GuidanceDocument();

        // 使用AI翻译服务翻译韩文字段
        String translatedTitle = translateIfNeeded(src.getTitle());
        String translatedContent = translateIfNeeded(src.getContent());
        String translatedCategory = translateIfNeeded(src.getCategory());

        // 设置文档类型
        entity.setDocumentType("GUIDANCE");

        // 设置基本信息（使用翻译后的数据）
        entity.setTitle(truncateString(translatedTitle, 500));
        entity.setPublicationDate(src.getPublicationDate());  // 直接使用已解析的日期
        entity.setDocumentUrl(truncateString(src.getDocumentUrl(), 1000));
        entity.setSourceUrl(BASE_URL);
        entity.setGuidanceStatus(src.getStatus());
        
        // 如果有分类信息，设置到topic字段
        if (translatedCategory != null && !translatedCategory.isEmpty()) {
            entity.setTopic(truncateString(translatedCategory, 255));
        }
        
        // 设置描述（如果有内容）
        if (translatedContent != null && !translatedContent.isEmpty()) {
            entity.setDescription(translatedContent);
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


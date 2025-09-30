package com.certification.crawler.countrydata.eu;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.entity.common.CustomsCase;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.exception.AllDataDuplicateException;
import com.certification.repository.common.CustomsCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Eu_customcase {
    
    private static final String BASE_URL = "https://ec.europa.eu/taxation_customs/dds2/taric/measures.jsp";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    @Autowired
    private CustomsCaseRepository customsCaseRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;
    
    /**
     * 爬取指定TARIC编码的关税措施信息并保存到数据库（支持全量爬取）
     * @param taricCode TARIC编码，如"9018"
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @return 保存的记录数量
     */
    @Transactional
    public int crawlAndSaveToDatabase(String taricCode, int maxRecords, int batchSize) {
        log.info("🚀 开始爬取TARIC编码 {} 的商品编码信息...", taricCode);
        log.info("📊 批次大小: {}，最大记录数: {}", batchSize, maxRecords == -1 ? "所有数据" : maxRecords);
        log.info("🌐 目标URL: {}", buildUrl(taricCode));
        
        boolean crawlAll = (maxRecords == -1);
        int actualBatchSize = Math.min(batchSize, crawlerConfig.getCrawl().getApiLimits().getEuCustomCaseMaxPerPage());
        
        int totalSaved = 0;
        int consecutiveDuplicateBatches = 0;
        List<Map<String, String>> currentBatch = new ArrayList<>();
        
        try {
            String url = buildUrl(taricCode);
            Document doc = fetchDocumentWithRetry(url);
            
            // 解析商品编码条目
            Elements codeElements = doc.select(".nomenclaturecode");
            System.out.println("📋 发现 " + codeElements.size() + " 个商品编码条目");
            
            // 如果没有找到nomenclaturecode元素，尝试其他选择器
            if (codeElements.isEmpty()) {
                System.out.println("🔍 尝试其他选择器...");
                
                // 尝试查找包含商品编码的其他元素
                Elements allDivs = doc.select("div");
                System.out.println("🔍 页面中总共有 " + allDivs.size() + " 个div元素");
                
                // 查找包含数字编码的元素
                Elements elementsWithNumbers = doc.select("div:contains(9018)");
                System.out.println("🔍 包含9018的元素数量: " + elementsWithNumbers.size());
                
                // 查找包含"code"类的元素
                Elements codeElements2 = doc.select(".code");
                System.out.println("🔍 找到 " + codeElements2.size() + " 个.code元素");
                
                // 查找包含"nomenclature"的元素
                Elements nomenclatureElements = doc.select("*:contains(nomenclature)");
                System.out.println("🔍 包含nomenclature的元素数量: " + nomenclatureElements.size());
                
                // 查找包含"nobr"的元素
                Elements nobrElements = doc.select("nobr");
                System.out.println("🔍 找到 " + nobrElements.size() + " 个nobr元素");
                
                // 打印页面的一些关键信息
                System.out.println("🔍 页面标题: " + doc.title());
                System.out.println("🔍 页面body内容长度: " + doc.body().text().length());
                
                // 如果仍然没有找到，尝试使用所有div元素
                if (codeElements.isEmpty()) {
                    System.out.println("⚠️ 使用所有div元素作为备选方案");
                    codeElements = allDivs;
                }
            }
            
            for (Element element : codeElements) {
                Map<String, String> data = parseCodeElement(element);
                if (data != null && !data.isEmpty()) {
                    currentBatch.add(data);
                    
                    if (currentBatch.size() >= actualBatchSize) {
                        int savedInBatch = saveBatchToDatabase(currentBatch);
                        totalSaved += savedInBatch;
                        
                        if (savedInBatch == 0) {
                            consecutiveDuplicateBatches++;
                            System.out.println("🔄 批次完全重复，连续重复批次数: " + consecutiveDuplicateBatches);
                            
                            if (consecutiveDuplicateBatches >= 3) {
                                System.out.println("🛑 连续 " + 3 + " 个批次完全重复，停止爬取");
                                break;
                            }
                        } else {
                            consecutiveDuplicateBatches = 0;
                            System.out.println("✅ 批次保存成功，保存了 " + savedInBatch + " 条新记录");
                        }
                        
                        currentBatch.clear();
                    }
                }
            }
            
            // 处理最后一批数据
            if (!currentBatch.isEmpty()) {
                int savedInBatch = saveBatchToDatabase(currentBatch);
                totalSaved += savedInBatch;
                System.out.println("✅ 最后批次保存完成，保存了 " + savedInBatch + " 条记录");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🎉 爬取完成！");
        System.out.println("📊 爬取汇总:");
        System.out.println("   ├─ TARIC编码: " + taricCode);
        System.out.println("   ├─ 总保存记录: " + totalSaved);
        System.out.println("   ├─ 连续重复批次: " + consecutiveDuplicateBatches);
        System.out.println("   └─ 完成时间: " + LocalDateTime.now().toString());
        return totalSaved;
    }
    
    /**
     * 向后兼容的方法
     * @param taricCode TARIC编码
     * @return 保存的记录数量
     */
    @Transactional
    public int crawlAndSaveToDatabase(String taricCode) {
        return crawlAndSaveToDatabase(taricCode, -1, crawlerConfig.getBatch().getSmallSaveSize());
    }
    
    /**
     * 批量爬取多个TARIC编码的关税措施信息
     * @param taricCodes TARIC编码列表
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @return 爬取结果汇总
     */
    @Transactional
    public Map<String, Object> crawlAndSaveWithTaricCodes(List<String> taricCodes, int maxRecords, int batchSize) {
        log.info("🚀 开始批量爬取TARIC编码列表，共 {} 个编码", taricCodes.size());
        log.info("📊 批次大小: {}，最大记录数: {}", batchSize, maxRecords == -1 ? "所有数据" : maxRecords);
        
        Map<String, Object> result = new HashMap<>();
        int totalSaved = 0;
        int totalSkipped = 0;
        int successCount = 0;
        int failureCount = 0;
        List<String> failedCodes = new ArrayList<>();
        Map<String, Integer> codeResults = new HashMap<>();
        
        for (String taricCode : taricCodes) {
            try {
                log.info("🔄 正在爬取TARIC编码: {}", taricCode);
                int savedCount = crawlAndSaveToDatabase(taricCode, maxRecords, batchSize);
                
                if (savedCount >= 0) {
                    totalSaved += savedCount;
                    successCount++;
                    codeResults.put(taricCode, savedCount);
                    log.info("✅ TARIC编码 {} 爬取成功，保存 {} 条记录", taricCode, savedCount);
                } else {
                    failureCount++;
                    failedCodes.add(taricCode);
                    codeResults.put(taricCode, -1);
                    log.error("❌ TARIC编码 {} 爬取失败", taricCode);
                }
                
                // 添加延迟避免请求过快
                Thread.sleep(crawlerConfig.getRetry().getDelayMilliseconds() / 2);
                
            } catch (Exception e) {
                failureCount++;
                failedCodes.add(taricCode);
                codeResults.put(taricCode, -1);
                log.error("❌ TARIC编码 {} 爬取异常: {}", taricCode, e.getMessage());
            }
        }
        
        result.put("totalProcessed", taricCodes.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("failedCodes", failedCodes);
        result.put("codeResults", codeResults);
        result.put("success", failureCount == 0);
        result.put("message", String.format("批量爬取完成：成功 %d 个，失败 %d 个，共保存 %d 条记录", 
                successCount, failureCount, totalSaved));
        
        log.info("📊 批量爬取汇总:");
        log.info("   ├─ 总处理编码: {}", taricCodes.size());
        log.info("   ├─ 成功: {}", successCount);
        log.info("   ├─ 失败: {}", failureCount);
        log.info("   ├─ 总保存记录: {}", totalSaved);
        log.info("   └─ 失败编码: {}", failedCodes);
        
        return result;
    }
    
    /**
     * 构建请求URL
     */
    private String buildUrl(String taricCode) {
        return BASE_URL + "?Lang=en&SimDate=20250925&Area=&MeasType=&StartPub=&EndPub=" +
               "&MeasText=&GoodsText=&op=&Taric=" + taricCode + 
               "&AdditionalCode=&search_text=goods&textSearch=&LangDescr=en" +
               "&OrderNum=&Regulation=&measStartDat=&measEndDat=&DatePicker=25-09-2025";
    }
    
    /**
     * 解析单个商品编码元素
     */
    private Map<String, String> parseCodeElement(Element element) {
        Map<String, String> data = new HashMap<>();
        
        try {
            // 根据您提供的选择器提取商品编码
            String hsCode = "";
            
            // 尝试从 span > nobr 中提取编码（如：9018 90 50）
            Element nobrElement = element.select("span nobr").first();
            if (nobrElement != null) {
                hsCode = nobrElement.text().trim();
                System.out.println("🔍 从span nobr提取到编码: " + hsCode);
            }
            
            // 如果为空，尝试从其他可能的nobr标签中提取
            if (hsCode.isEmpty()) {
                Element altNobrElement = element.select("nobr").first();
                if (altNobrElement != null) {
                    hsCode = altNobrElement.text().trim();
                    System.out.println("🔍 从nobr提取到编码: " + hsCode);
                }
            }
            
            // 提取商品描述 - 根据您提供的XPath路径
            String description = "";
            
            // 尝试从 .tddescription .to_highlight 中提取描述
            Element descElement = element.select(".tddescription .to_highlight").first();
            if (descElement != null) {
                description = descElement.text().trim();
                System.out.println("🔍 从.tddescription .to_highlight提取到描述: " + description);
            }
            
            // 如果描述为空，尝试从其他选择器提取
            if (description.isEmpty()) {
                Element altDescElement = element.select(".tddescription").first();
                if (altDescElement != null) {
                    description = altDescElement.text().trim();
                    System.out.println("🔍 从.tddescription提取到描述: " + description);
                }
            }
            
            // 如果描述还是为空，尝试从整个元素的文本中提取（排除编码部分）
            if (description.isEmpty() && !hsCode.isEmpty()) {
                String elementText = element.text().trim();
                description = elementText.replace(hsCode, "").trim();
                System.out.println("🔍 从元素文本中提取到描述: " + description);
            }
            
            // 提取编码级别
            String codeLevel = extractCodeLevel(element);
            
            // 提取缩进信息（用于判断层级）
            String indent = "";
            Element indentElement = element.select(".tddescription nobr").first();
            if (indentElement != null) {
                indent = indentElement.text().trim();
            }
            
            // 只要有商品编码就保存，描述可以为空
            if (!hsCode.isEmpty()) {
                data.put("hsCode", hsCode);
                data.put("description", description);
                data.put("codeLevel", codeLevel);
                data.put("indent", indent);
                data.put("crawlTime", LocalDateTime.now().toString());
                
                // 详细打印爬取的字段
                System.out.println("📦 提取到商品编码: " + hsCode + " - " + description);
                System.out.println("   ├─ HS编码: " + hsCode);
                System.out.println("   ├─ 商品描述: " + description);
                System.out.println("   ├─ 编码级别: " + codeLevel);
                System.out.println("   ├─ 缩进信息: " + indent);
                System.out.println("   └─ 爬取时间: " + LocalDateTime.now().toString());
            } else {
                System.out.println("⚠️ 警告 - 未能提取到商品编码，跳过此元素");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 解析商品编码元素时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return data;
    }
    
    
    /**
     * 提取编码级别
     */
    private String extractCodeLevel(Element element) {
        String className = element.className();
        Pattern pattern = Pattern.compile("codelev(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(className);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0";
    }
    
    /**
     * 保存批次数据到数据库
     */
    @Transactional
    private int saveBatchToDatabase(List<Map<String, String>> batchData) {
        int savedCount = 0;
        int duplicateCount = 0;
        
        System.out.println("📦 开始处理批次数据，共 " + batchData.size() + " 条记录");
        
        for (Map<String, String> rawData : batchData) {
            try {
                String hsCode = rawData.get("hsCode");
                String description = rawData.get("description");
                
                // 检查是否已存在
                if (checkIfCustomsCaseExists(hsCode, description)) {
                    duplicateCount++;
                    System.out.println("🔄 跳过重复记录: " + hsCode + " - " + description);
                    continue; // 跳过重复记录
                }
                
                // 转换为CustomsCase实体
                CustomsCase customsCase = createCustomsCaseFromData(rawData);
                if (customsCase != null) {
                    customsCaseRepository.save(customsCase);
                    savedCount++;
                    System.out.println("✅ 成功保存: " + hsCode + " - " + description);
                }
                
            } catch (Exception e) {
                System.err.println("❌ 保存CustomsCase记录时出错: " + e.getMessage());
            }
        }
        
        System.out.println("📊 批次处理完成:");
        System.out.println("   ├─ 总记录数: " + batchData.size());
        System.out.println("   ├─ 新增记录: " + savedCount);
        System.out.println("   ├─ 重复记录: " + duplicateCount);
        System.out.println("   └─ 处理时间: " + LocalDateTime.now().toString());
        
        return savedCount;
    }
    
    /**
     * 检查CustomsCase是否已存在
     */
    private boolean checkIfCustomsCaseExists(String hsCode, String description) {
        try {
            // 根据HS编码和描述检查是否存在
            List<CustomsCase> existing = customsCaseRepository.findByHsCodeUsedContainingAndRulingResultContaining(
                hsCode, description.substring(0, Math.min(50, description.length()))
            );
            return !existing.isEmpty();
        } catch (Exception e) {
            System.err.println("检查CustomsCase是否存在时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从爬取数据创建CustomsCase实体
     */
    private CustomsCase createCustomsCaseFromData(Map<String, String> rawData) {
        try {
            CustomsCase customsCase = new CustomsCase();
            
            // 基本字段映射
            customsCase.setHsCodeUsed(rawData.get("hsCode"));
            customsCase.setRulingResult(rawData.get("description"));
            customsCase.setViolationType("TARIC商品编码");
            customsCase.setCaseNumber(buildCaseNumber(rawData.get("hsCode")));
            customsCase.setCaseDate(LocalDate.now());
            customsCase.setDataSource("EU TARIC Database");
            customsCase.setJdCountry("EU");
            customsCase.setCrawlTime(LocalDateTime.now());
            customsCase.setDataStatus("ACTIVE");
            customsCase.setRiskLevel(RiskLevel.MEDIUM);
            customsCase.setKeywords(""); // 默认为空
            
            // 设置处罚金额为0（商品编码信息，非违规案例）
            customsCase.setPenaltyAmount(BigDecimal.ZERO);
            
            // 打印保存到数据库的字段信息
            System.out.println("💾 保存到数据库的字段:");
            System.out.println("   ├─ HS编码: " + customsCase.getHsCodeUsed());
            System.out.println("   ├─ 商品描述: " + customsCase.getRulingResult());
            System.out.println("   ├─ 违规类型: " + customsCase.getViolationType());
            System.out.println("   ├─ 判例编号: " + customsCase.getCaseNumber());
            System.out.println("   ├─ 判例日期: " + customsCase.getCaseDate());
            System.out.println("   ├─ 数据来源: " + customsCase.getDataSource());
            System.out.println("   ├─ 国家: " + customsCase.getJdCountry());
            System.out.println("   ├─ 爬取时间: " + customsCase.getCrawlTime());
            System.out.println("   ├─ 数据状态: " + customsCase.getDataStatus());
            System.out.println("   ├─ 风险等级: " + customsCase.getRiskLevel());
            System.out.println("   ├─ 关键词: " + customsCase.getKeywords());
            System.out.println("   └─ 处罚金额: " + customsCase.getPenaltyAmount());
            
            return customsCase;
            
        } catch (Exception e) {
            System.err.println("❌ 创建CustomsCase实体时出错: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 构建判例编号
     */
    private String buildCaseNumber(String hsCode) {
        return "TARIC-" + hsCode + "-" + System.currentTimeMillis();
    }
    
    /**
     * 提取关键词列表（独立功能）
     * @param text 要提取关键词的文本
     * @return 关键词列表
     */
    public List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> keywords = new ArrayList<>();
        
        try {
            // 转换为小写并清理文本
            String cleanText = text.toLowerCase()
                    .replaceAll("[^a-zA-Z\\s]", " ")
                    .replaceAll("\\s+", " ");
            
            // 分割单词
            String[] words = cleanText.split("\\s+");
            
            // 停用词列表
            Set<String> stopWords = Set.of(
                "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
                "a", "an", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
                "do", "does", "did", "will", "would", "could", "should", "may", "might", "must",
                "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they",
                "me", "him", "her", "us", "them", "my", "your", "his", "her", "its", "our", "their"
            );
            
            // 提取关键词
            for (String word : words) {
                if (word.length() >= 3 && 
                    !stopWords.contains(word) && 
                    !keywords.contains(word)) {
                    keywords.add(word);
                }
            }
            
            // 限制关键词数量
            if (keywords.size() > 10) {
                keywords = keywords.subList(0, 10);
            }
            
        } catch (Exception e) {
            System.err.println("❌ 提取关键词时出错: " + e.getMessage());
        }
        
        return keywords;
    }
    
    /**
     * 批量提取关键词并更新数据库
     * @param customsCaseId CustomsCase的ID
     * @return 提取的关键词数量
     */
    @Transactional
    public int updateKeywordsForCustomsCase(Long customsCaseId) {
        try {
            Optional<CustomsCase> optional = customsCaseRepository.findById(customsCaseId);
            if (optional.isPresent()) {
                CustomsCase customsCase = optional.get();
                String description = customsCase.getRulingResult();
                
                if (description != null && !description.trim().isEmpty()) {
                    List<String> keywords = extractKeywords(description);
                    String keywordsJson = "[\"" + String.join("\",\"", keywords) + "\"]";
                    customsCase.setKeywords(keywordsJson);
                    customsCaseRepository.save(customsCase);
                    
                    System.out.println("✅ 为CustomsCase ID " + customsCaseId + " 提取了 " + keywords.size() + " 个关键词");
                    return keywords.size();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 更新关键词时出错: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * 批量更新所有CustomsCase的关键词
     * @return 更新的记录数量
     */
    @Transactional
    public int updateAllKeywords() {
        int updatedCount = 0;
        
        try {
            List<CustomsCase> allCases = customsCaseRepository.findAll();
            
            for (CustomsCase customsCase : allCases) {
                if (customsCase.getKeywords() == null || customsCase.getKeywords().isEmpty()) {
                    String description = customsCase.getRulingResult();
                    if (description != null && !description.trim().isEmpty()) {
                        List<String> keywords = extractKeywords(description);
                        String keywordsJson = "[\"" + String.join("\",\"", keywords) + "\"]";
                        customsCase.setKeywords(keywordsJson);
                        customsCaseRepository.save(customsCase);
                        updatedCount++;
                    }
                }
            }
            
            System.out.println("✅ 批量更新完成，共更新了 " + updatedCount + " 条记录的关键词");
            
        } catch (Exception e) {
            System.err.println("❌ 批量更新关键词时出错: " + e.getMessage());
        }
        
        return updatedCount;
    }
    
    /**
     * 基于关键词列表爬取TARIC数据（单一参数策略）
     * 每个关键词作为TARIC编码进行搜索，每20个关键词为一批
     * @param keywords 关键词列表
     * @param maxRecords 最大记录数，-1表示所有数据
     * @param batchSize 批次大小（关键词批次，不是数据批次）
     * @return 爬取结果描述
     */
    @Transactional
    public String crawlAndSaveWithKeywords(List<String> keywords, int maxRecords, int batchSize) {
        if (keywords == null || keywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认TARIC编码9018搜索");
            return crawlAndSaveToDatabase("9018") + " 条记录";
        }
        
        System.out.println("🚀 开始基于关键词列表爬取TARIC数据...");
        System.out.println("📋 关键词数量: " + keywords.size());
        System.out.println("🔍 搜索策略: 每个关键词作为TARIC编码进行搜索");
        System.out.println("📊 最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));
        System.out.println("📦 关键词批次大小: " + batchSize);
        
        int totalSaved = 0;
        int consecutiveDuplicateBatches = 0;
        List<String> currentBatch = new ArrayList<>();
        
        // 将关键词分批处理
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            currentBatch.add(keyword);
            
            // 当批次达到指定大小或是最后一批时，处理当前批次
            if (currentBatch.size() >= batchSize || i == keywords.size() - 1) {
                System.out.println("\n📦 处理关键词批次: " + currentBatch);
                
                int batchSaved = 0;
                for (String batchKeyword : currentBatch) {
                    try {
                        System.out.println("🔍 正在处理关键词: " + batchKeyword);
                        
                        // 使用关键词作为TARIC编码进行搜索
                        int result = crawlAndSaveToDatabase(batchKeyword);
                        batchSaved += result;
                        System.out.println("关键词 '" + batchKeyword + "' 搜索结果: " + result + " 条记录");
                        
                        // 添加延迟避免请求过于频繁
                        smartDelay();
                        
                    } catch (Exception e) {
                        System.err.println("❌ 处理关键词 '" + batchKeyword + "' 时发生错误: " + e.getMessage());
                    }
                }
                
                totalSaved += batchSaved;
                
                // 检查是否完全重复
                if (batchSaved == 0) {
                    consecutiveDuplicateBatches++;
                    System.out.println("🔄 关键词批次完全重复，连续重复批次数: " + consecutiveDuplicateBatches);
                    
                    if (consecutiveDuplicateBatches >= 3) {
                        System.out.println("🛑 连续 " + 3 + " 个关键词批次完全重复，停止爬取");
                        break;
                    }
                } else {
                    consecutiveDuplicateBatches = 0;
                    System.out.println("✅ 关键词批次处理完成，保存了 " + batchSaved + " 条新记录");
                }
                
                currentBatch.clear();
                
                // 检查是否达到最大记录数
                if (maxRecords > 0 && totalSaved >= maxRecords) {
                    System.out.println("📊 已达到最大记录数限制: " + maxRecords);
                    break;
                }
            }
        }
        
        String result = String.format("基于关键词列表的TARIC数据爬取完成，总共保存: %d 条记录", totalSaved);
        System.out.println("🎉 " + result);
        return result;
    }
    
    /**
     * 智能关键词搜索 - 根据关键词类型选择最佳搜索策略
     * 由于TARIC只有一个搜索参数，这里简化为直接使用关键词作为TARIC编码
     */
    @Transactional
    public String crawlAndSaveWithSmartKeywords(List<String> keywords, int maxRecords, int batchSize) {
        if (keywords == null || keywords.isEmpty()) {
            System.out.println("关键词列表为空，使用默认TARIC编码9018搜索");
            return crawlAndSaveToDatabase("9018") + " 条记录";
        }
        
        System.out.println("🧠 开始基于智能关键词策略爬取TARIC数据...");
        System.out.println("📋 关键词数量: " + keywords.size());
        System.out.println("🔍 搜索策略: 每个关键词作为TARIC编码进行搜索");
        
        int totalSaved = 0;
        int consecutiveDuplicateBatches = 0;
        List<String> currentBatch = new ArrayList<>();
        
        // 将关键词分批处理
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            currentBatch.add(keyword);
            
            // 当批次达到指定大小或是最后一批时，处理当前批次
            if (currentBatch.size() >= batchSize || i == keywords.size() - 1) {
                System.out.println("\n📦 处理智能关键词批次: " + currentBatch);
                
                int batchSaved = 0;
                for (String batchKeyword : currentBatch) {
                    try {
                        System.out.println("🔍 智能处理关键词: " + batchKeyword);
                        
                        // 直接使用关键词作为TARIC编码进行搜索
                        int result = crawlAndSaveToDatabase(batchKeyword);
                        batchSaved += result;
                        System.out.println("关键词 '" + batchKeyword + "' 搜索结果: " + result + " 条记录");
                        
                        // 添加延迟避免请求过于频繁
                        smartDelay();
                        
                    } catch (Exception e) {
                        System.err.println("❌ 智能处理关键词 '" + batchKeyword + "' 时发生错误: " + e.getMessage());
                    }
                }
                
                totalSaved += batchSaved;
                
                // 检查是否完全重复
                if (batchSaved == 0) {
                    consecutiveDuplicateBatches++;
                    System.out.println("🔄 智能关键词批次完全重复，连续重复批次数: " + consecutiveDuplicateBatches);
                    
                    if (consecutiveDuplicateBatches >= 3) {
                        System.out.println("🛑 连续 " + 3 + " 个智能关键词批次完全重复，停止爬取");
                        break;
                    }
                } else {
                    consecutiveDuplicateBatches = 0;
                    System.out.println("✅ 智能关键词批次处理完成，保存了 " + batchSaved + " 条新记录");
                }
                
                currentBatch.clear();
                
                // 检查是否达到最大记录数
                if (maxRecords > 0 && totalSaved >= maxRecords) {
                    System.out.println("📊 已达到最大记录数限制: " + maxRecords);
                    break;
                }
            }
        }
        
        String result = String.format("基于智能关键词策略的TARIC数据爬取完成，总共保存: %d 条记录", totalSaved);
        System.out.println("🎉 " + result);
        return result;
    }
    
    /**
     * 带重试机制的文档获取
     */
    private Document fetchDocumentWithRetry(String url) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= crawlerConfig.getRetry().getMaxAttempts(); attempt++) {
            try {
                System.out.println("🌐 尝试获取文档 (第" + attempt + "次): " + url);
                
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(20000)
                        .followRedirects(true)
                        .maxBodySize(0)
                        .get();
                
                System.out.println("✅ 文档获取成功");
                return doc;
                
            } catch (org.jsoup.HttpStatusException e) {
                lastException = e;
                int statusCode = e.getStatusCode();
                
                if (statusCode == 429) {
                    // Too Many Requests - 需要更长的延迟
                    int delay = calculateBackoffDelay(attempt);
                    System.out.println("⚠️ HTTP 429 (Too Many Requests)，等待 " + delay + " 毫秒后重试...");
                    Thread.sleep(delay);
                } else if (statusCode >= 500) {
                    // 服务器错误 - 中等延迟
                    int delay = crawlerConfig.getRetry().getDelayMilliseconds() * attempt;
                    System.out.println("⚠️ HTTP " + statusCode + " (服务器错误)，等待 " + delay + " 毫秒后重试...");
                    Thread.sleep(delay);
                } else {
                    // 其他HTTP错误 - 不重试
                    System.err.println("❌ HTTP错误 " + statusCode + ": " + e.getMessage());
                    throw e;
                }
                
            } catch (java.net.SocketTimeoutException e) {
                lastException = e;
                int delay = crawlerConfig.getRetry().getDelayMilliseconds() * attempt;
                System.out.println("⚠️ 连接超时，等待 " + delay + " 毫秒后重试...");
                Thread.sleep(delay);
                
            } catch (java.net.ConnectException e) {
                lastException = e;
                int delay = crawlerConfig.getRetry().getDelayMilliseconds() * attempt;
                System.out.println("⚠️ 连接异常，等待 " + delay + " 毫秒后重试...");
                Thread.sleep(delay);
                
            } catch (Exception e) {
                lastException = e;
                System.err.println("❌ 获取文档时发生未知错误: " + e.getMessage());
                if (attempt < crawlerConfig.getRetry().getMaxAttempts()) {
                    int delay = crawlerConfig.getRetry().getDelayMilliseconds() * attempt;
                    System.out.println("等待 " + delay + " 毫秒后重试...");
                    Thread.sleep(delay);
                }
            }
        }
        
        System.err.println("❌ 经过 " + crawlerConfig.getRetry().getMaxAttempts() + " 次重试后仍然失败");
        throw new Exception("获取文档失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }
    
    /**
     * 计算退避延迟时间
     */
    private int calculateBackoffDelay(int attempt) {
        // 指数退避：2秒, 4秒, 8秒, 最大10秒
        int delay = crawlerConfig.getRetry().getDelayMilliseconds() * (int) Math.pow(2, attempt - 1);
        return Math.min(delay, crawlerConfig.getRetry().getDelayMilliseconds() * 2);
    }
    
    /**
     * 智能延迟 - 根据请求频率动态调整延迟时间
     */
    private void smartDelay() {
        try {
            // 基础延迟2秒，避免429错误
            int delay = crawlerConfig.getRetry().getDelayMilliseconds();
            
            // 添加随机延迟，避免请求过于规律
            int randomDelay = (int) (Math.random() * 1000); // 0-1秒随机延迟
            delay += randomDelay;
            
            System.out.println("⏳ 智能延迟 " + delay + " 毫秒...");
            Thread.sleep(delay);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ 延迟被中断: " + e.getMessage());
        }
    }
    
}

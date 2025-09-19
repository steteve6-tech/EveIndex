package com.certification.standards;

import com.certification.entity.common.CrawlerData;
import com.certification.repository.CrawlerDataRepository;
import com.certification.dto.CrawlerDataQueryRequest;
import com.certification.dto.CrawlerDataSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准查询服务
 * 基于keywordconfig关键词查询crawler_data表
 */
@Slf4j
@Service
public class StandardQueryService {

    @Autowired
    private CrawlerDataRepository crawlerDataRepository;

    @Autowired
    private KeywordConfig keywordConfig;

    /**
     * 根据关键词配置查询爬虫数据
     */
    public CrawlerDataSearchResult queryCrawlerDataByKeywords(CrawlerDataQueryRequest request) {
        log.info("根据关键词配置查询爬虫数据: {}", request);

        // 获取关键词配置
        Map<String, KeywordConfig.MarketKeyword> marketKeywords = keywordConfig.getMarketKeywords();
        if (marketKeywords == null || marketKeywords.isEmpty()) {
            log.warn("关键词配置为空");
            return createEmptyResult();
        }

        // 构建查询关键词列表
        List<String> searchKeywords = buildSearchKeywords(request, marketKeywords);
        if (searchKeywords.isEmpty()) {
            log.warn("未找到匹配的关键词");
            return createEmptyResult();
        }

        // 执行查询 - 使用第一个关键词进行搜索
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<CrawlerData> crawlerDataPage = crawlerDataRepository.searchByKeyword(
                searchKeywords.get(0), pageable);

        // 构建结果
        CrawlerDataSearchResult result = new CrawlerDataSearchResult();
        result.setCrawlerDataList(crawlerDataPage.getContent());
        result.setTotal(crawlerDataPage.getTotalElements());
        result.setPage(request.getPage());
        result.setSize(request.getSize());
        result.setTotalPages(crawlerDataPage.getTotalPages());
        result.setSearchKeywords(searchKeywords);
        result.setMatchedKeywords(getMatchedKeywords(crawlerDataPage.getContent(), searchKeywords));

        log.info("查询完成，找到 {} 条数据", crawlerDataPage.getTotalElements());
        return result;
    }

    /**
     * 根据市场关键词查询爬虫数据
     */
    public CrawlerDataSearchResult queryCrawlerDataByMarket(String marketCode, CrawlerDataQueryRequest request) {
        log.info("根据市场关键词查询爬虫数据: marketCode={}", marketCode);

        Map<String, KeywordConfig.MarketKeyword> marketKeywords = keywordConfig.getMarketKeywords();
        if (marketKeywords == null || !marketKeywords.containsKey(marketCode)) {
            log.warn("未找到市场配置: {}", marketCode);
            return createEmptyResult();
        }

        KeywordConfig.MarketKeyword marketKeyword = marketKeywords.get(marketCode);
        List<String> searchKeywords = extractKeywordsFromMarketKeyword(marketKeyword);
        
        if (searchKeywords.isEmpty()) {
            log.warn("市场关键词为空: {}", marketCode);
            return createEmptyResult();
        }

        // 执行查询 - 使用第一个关键词进行搜索
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<CrawlerData> crawlerDataPage = crawlerDataRepository.searchByKeyword(
                searchKeywords.get(0), pageable);

        // 构建结果
        CrawlerDataSearchResult result = new CrawlerDataSearchResult();
        result.setCrawlerDataList(crawlerDataPage.getContent());
        result.setTotal(crawlerDataPage.getTotalElements());
        result.setPage(request.getPage());
        result.setSize(request.getSize());
        result.setTotalPages(crawlerDataPage.getTotalPages());
        result.setSearchKeywords(searchKeywords);
        result.setMarketCode(marketCode);
        result.setMarketName(marketKeyword.getRegionName());
        result.setAuthority(marketKeyword.getAuthority());

        log.info("查询完成，找到 {} 条数据", crawlerDataPage.getTotalElements());
        return result;
    }

    /**
     * 根据高优先级监测项查询爬虫数据
     */
    public CrawlerDataSearchResult queryCrawlerDataByHighPriority(CrawlerDataQueryRequest request) {
        log.info("根据高优先级监测项查询爬虫数据");

        List<String> highPriorityItems = keywordConfig.getHighPriorityWatchItems();
        if (highPriorityItems == null || highPriorityItems.isEmpty()) {
            log.warn("高优先级监测项为空");
            return createEmptyResult();
        }

        // 提取关键词
        List<String> searchKeywords = highPriorityItems.stream()
                .map(this::extractKeywordsFromPriorityItem)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        if (searchKeywords.isEmpty()) {
            log.warn("未提取到有效关键词");
            return createEmptyResult();
        }

        // 执行查询 - 使用第一个关键词进行搜索
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<CrawlerData> crawlerDataPage = crawlerDataRepository.searchByKeyword(
                searchKeywords.get(0), pageable);

        // 构建结果
        CrawlerDataSearchResult result = new CrawlerDataSearchResult();
        result.setCrawlerDataList(crawlerDataPage.getContent());
        result.setTotal(crawlerDataPage.getTotalElements());
        result.setPage(request.getPage());
        result.setSize(request.getSize());
        result.setTotalPages(crawlerDataPage.getTotalPages());
        result.setSearchKeywords(searchKeywords);
        result.setHighPriorityItems(highPriorityItems);

        log.info("查询完成，找到 {} 条数据", crawlerDataPage.getTotalElements());
        return result;
    }

    /**
     * 构建搜索关键词列表
     */
    private List<String> buildSearchKeywords(CrawlerDataQueryRequest request, 
                                           Map<String, KeywordConfig.MarketKeyword> marketKeywords) {
        List<String> keywords = new ArrayList<>();

        // 如果指定了市场代码
        if (StringUtils.hasText(request.getMarketCode())) {
            KeywordConfig.MarketKeyword marketKeyword = marketKeywords.get(request.getMarketCode());
            if (marketKeyword != null) {
                keywords.addAll(extractKeywordsFromMarketKeyword(marketKeyword));
            }
        } else {
            // 否则使用所有市场的关键词
            for (KeywordConfig.MarketKeyword marketKeyword : marketKeywords.values()) {
                keywords.addAll(extractKeywordsFromMarketKeyword(marketKeyword));
            }
        }

        // 添加用户自定义关键词
        if (StringUtils.hasText(request.getCustomKeywords())) {
            keywords.addAll(Arrays.asList(request.getCustomKeywords().split(",")));
        }

        return keywords.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从市场关键词中提取搜索关键词
     */
    private List<String> extractKeywordsFromMarketKeyword(KeywordConfig.MarketKeyword marketKeyword) {
        List<String> keywords = new ArrayList<>();

        // 主要关键词
        if (StringUtils.hasText(marketKeyword.getPrimaryKeywords())) {
            keywords.addAll(Arrays.asList(marketKeyword.getPrimaryKeywords().split(",")));
        }

        // 本地语言关键词
        if (StringUtils.hasText(marketKeyword.getLocalLangKeywords())) {
            keywords.addAll(Arrays.asList(marketKeyword.getLocalLangKeywords().split(",")));
        }

        // 延伸技术词
        if (StringUtils.hasText(marketKeyword.getExtendTechTerms())) {
            keywords.addAll(Arrays.asList(marketKeyword.getExtendTechTerms().split(",")));
        }

        return keywords.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 从高优先级监测项中提取关键词
     */
    private List<String> extractKeywordsFromPriorityItem(String priorityItem) {
        List<String> keywords = new ArrayList<>();
        
        // 简单的关键词提取逻辑，可以根据需要优化
        String[] parts = priorityItem.split("\\s+");
        for (String part : parts) {
            if (part.length() > 2 && !part.matches(".*[0-9]+.*")) {
                keywords.add(part);
            }
        }
        
        return keywords;
    }

    /**
     * 获取匹配的关键词
     */
    private Map<String, Integer> getMatchedKeywords(List<CrawlerData> crawlerDataList, List<String> searchKeywords) {
        return searchKeywords.stream()
                .collect(Collectors.toMap(
                        keyword -> keyword,
                        keyword -> (int) crawlerDataList.stream()
                                .filter(data -> containsKeyword(data, keyword))
                                .count()
                ));
    }

    /**
     * 检查数据是否包含关键词
     */
    private boolean containsKeyword(CrawlerData data, String keyword) {
        if (data == null || !StringUtils.hasText(keyword)) {
            return false;
        }

        String lowerKeyword = keyword.toLowerCase();
        return (data.getTitle() != null && data.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (data.getSummary() != null && data.getSummary().toLowerCase().contains(lowerKeyword)) ||
               (data.getContent() != null && data.getContent().toLowerCase().contains(lowerKeyword));
    }

    /**
     * 创建空结果
     */
    private CrawlerDataSearchResult createEmptyResult() {
        CrawlerDataSearchResult result = new CrawlerDataSearchResult();
        result.setCrawlerDataList(new ArrayList<>());
        result.setTotal(0L);
        result.setPage(1);
        result.setSize(20);
        result.setTotalPages(0);
        result.setSearchKeywords(new ArrayList<>());
        return result;
    }
}

package com.certification.standards;

import com.certification.entity.common.CrawlerData;
import com.certification.entity.common.CrawlerData.RiskLevel;
import com.certification.repository.CrawlerDataRepository;
import com.certification.standards.KeywordService;
import com.certification.service.DateFormatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 爬虫数据服务类
 */
@Slf4j
@Service
@Transactional
public class CrawlerDataService {
    
    @Autowired
    private CrawlerDataRepository crawlerDataRepository;
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private DateFormatService dateFormatService;
    
    /**
     * 根据ID获取爬虫数据
     */
    public CrawlerData getCrawlerDataById(String id) {
        log.info("根据ID获取爬虫数据: {}", id);
        return crawlerDataRepository.findById(id).orElse(null);
    }
    
    /**
     * 保存爬虫数据
     */
    public CrawlerData saveCrawlerData(CrawlerData crawlerData) {
        log.info("保存爬虫数据: {}", crawlerData.getTitle());
        
        // 检查是否已存在相同URL的数据
        Optional<CrawlerData> existingData = crawlerDataRepository.findByUrlAndDeleted(crawlerData.getUrl(), 0);
        if (existingData.isPresent()) {
            log.info("发现重复数据，URL: {}", crawlerData.getUrl());
            crawlerData.setStatus(CrawlerData.DataStatus.DUPLICATE);
            crawlerData.setRemarks("检测到重复URL: " + crawlerData.getUrl());
        }
        
        return crawlerDataRepository.save(crawlerData);
    }
    
    /**
     * 批量保��爬虫数据（优化版本，高效去重）
     */
    public List<CrawlerData> saveCrawlerDataList(List<CrawlerData> crawlerDataList) {
        log.info("批量保存爬虫数据，数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取所有URL
        List<String> urls = crawlerDataList.stream()
                .map(CrawlerData::getUrl)
                .distinct()
                .toList();
        
        // 批量查询已存在的URL
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(urls);
        Set<String> existingUrlSet = new HashSet<>(existingUrls);
        
        log.info("发现 {} 个重复URL，{} 个新URL", existingUrls.size(), urls.size() - existingUrls.size());
        
        List<CrawlerData> savedDataList = new ArrayList<>();
        List<CrawlerData> newDataList = new ArrayList<>();
        
        // 分离新数据和重复数据
        for (CrawlerData crawlerData : crawlerDataList) {
            if (existingUrlSet.contains(crawlerData.getUrl())) {
                // 重复数据，只记录日志，不保存
                log.debug("跳过重复数据，URL: {}", crawlerData.getUrl());
            } else {
                // 新数据，设置为新建状态
                if (crawlerData.getStatus() == null) {
                    crawlerData.setStatus(CrawlerData.DataStatus.NEW);
                }
                log.debug("准备保存新数据，URL: {}", crawlerData.getUrl());
                newDataList.add(crawlerData);
            }
        }
        
        // 批量保存新数据
        if (!newDataList.isEmpty()) {
            try {
                List<CrawlerData> savedBatch = crawlerDataRepository.saveAll(newDataList);
                savedDataList.addAll(savedBatch);
                log.info("成功保存 {} 条新数据", savedBatch.size());
            } catch (Exception e) {
                log.error("批量保存数据时发生错误: {}", e.getMessage(), e);
                // 如果批量保存失败，尝试逐个保存
                for (CrawlerData data : newDataList) {
                    try {
                        CrawlerData savedData = crawlerDataRepository.save(data);
                        savedDataList.add(savedData);
                    } catch (Exception ex) {
                        log.error("保存单条数据失败，URL: {}, 错误: {}", data.getUrl(), ex.getMessage());
                    }
                }
            }
        }
        
        return savedDataList;
    }
    
    /**
     * 批量保存爬虫数据（按数据源去重）
     */
    public List<CrawlerData> saveCrawlerDataListBySource(List<CrawlerData> crawlerDataList) {
        log.info("按数据源批量保存爬虫数据，数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按数据源分组
        Map<String, List<CrawlerData>> sourceGroups = crawlerDataList.stream()
                .collect(Collectors.groupingBy(CrawlerData::getSourceName));
        
        List<CrawlerData> allSavedData = new ArrayList<>();
        
        for (Map.Entry<String, List<CrawlerData>> entry : sourceGroups.entrySet()) {
            String sourceName = entry.getKey();
            List<CrawlerData> sourceDataList = entry.getValue();
            
            log.info("处理数据源: {}，数据量: {}", sourceName, sourceDataList.size());
            
            // 提取当前数据源的URL
            List<String> urls = sourceDataList.stream()
                    .map(CrawlerData::getUrl)
                    .distinct()
                    .toList();
            
            // 查询当前数据源下已存在的URL
            List<String> existingUrls = crawlerDataRepository.findUrlsBySourceName(sourceName);
            Set<String> existingUrlSet = new HashSet<>(existingUrls);
            
            log.info("数据源 {} 发现 {} 个重复URL，{} 个新URL", 
                    sourceName, existingUrls.size(), urls.size() - existingUrls.size());
            
            List<CrawlerData> newDataList = new ArrayList<>();
            
            for (CrawlerData crawlerData : sourceDataList) {
                if (existingUrlSet.contains(crawlerData.getUrl())) {
                    // 重复数据，只记录日志，不保存
                    log.debug("跳过重复数据，数据源: {}，URL: {}", sourceName, crawlerData.getUrl());
                } else {
                    // 新数据，设置为新建状态
                    if (crawlerData.getStatus() == null) {
                        crawlerData.setStatus(CrawlerData.DataStatus.NEW);
                    }
                    log.debug("准备保存新数据，数据源: {}，URL: {}", sourceName, crawlerData.getUrl());
                    newDataList.add(crawlerData);
                }
            }
            
            // 批量保存新数据
            if (!newDataList.isEmpty()) {
                try {
                    List<CrawlerData> savedBatch = crawlerDataRepository.saveAll(newDataList);
                    allSavedData.addAll(savedBatch);
                    log.info("数据源 {} 成功�����存 {} 条新数据", sourceName, savedBatch.size());
                } catch (Exception e) {
                    log.error("数据源 {} 批量保存数据时发生错误: {}", sourceName, e.getMessage(), e);
                    // 如果批量保存失败，尝试逐个保存
                    for (CrawlerData data : newDataList) {
                        try {
                            CrawlerData savedData = crawlerDataRepository.save(data);
                            allSavedData.add(savedData);
                        } catch (Exception ex) {
                            log.error("数据源 {} 保存单条数据失败，URL: {}, 错误: {}", sourceName, data.getUrl(), ex.getMessage());
                        }
                    }
                }
            }
        }
        
        return allSavedData;
    }
    
    /**
     * 过滤重复URL（返回去重后的数据列表）
     */
    public List<CrawlerData> filterDuplicateUrls(List<CrawlerData> crawlerDataList) {
        log.info("过滤重复URL，原始数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取所有URL
        List<String> urls = crawlerDataList.stream()
                .map(CrawlerData::getUrl)
                .distinct()
                .toList();
        
        // 批量查询已存在的URL
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(urls);
        Set<String> existingUrlSet = new HashSet<>(existingUrls);
        
        // 过滤掉重复的URL，filteredList只保留数据库中不存在的（即新）数据
        List<CrawlerData> filteredList = new ArrayList<>();
        for (CrawlerData data : crawlerDataList) {
            if (!existingUrlSet.contains(data.getUrl())) {

//                if (data.getStatus() == null) {
//                    data.setStatus(CrawlerData.DataStatus.NEW);
//                }
                filteredList.add(data);
            }
        }
        log.info("过滤后数量: {}，去重数量: {}", filteredList.size(), existingUrls.size());
        
        return filteredList;
    }
    
    /**
     * 过滤重复URL（按数据源）
     */
    public List<CrawlerData> filterDuplicateUrlsBySource(List<CrawlerData> crawlerDataList) {
        log.info("按数据源过滤重复URL，原始数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按数据源分组
        Map<String, List<CrawlerData>> sourceGroups = crawlerDataList.stream()
                .collect(Collectors.groupingBy(CrawlerData::getSourceName));
        
        List<CrawlerData> allFilteredData = new ArrayList<>();
        
        for (Map.Entry<String, List<CrawlerData>> entry : sourceGroups.entrySet()) {
            String sourceName = entry.getKey();
            List<CrawlerData> sourceDataList = entry.getValue();
            
            log.info("处理数据源: {}，原始数量: {}", sourceName, sourceDataList.size());
            
            // 提取���前数据源的URL
            List<String> urls = sourceDataList.stream()
                    .map(CrawlerData::getUrl)
                    .distinct()
                    .toList();
            
            // 查询当前数据源下已存在的URL
            List<String> existingUrls = crawlerDataRepository.findUrlsBySourceName(sourceName);
            Set<String> existingUrlSet = new HashSet<>(existingUrls);
            
            // 过滤掉重复的URL
            List<CrawlerData> filteredList = new ArrayList<>();
            for (CrawlerData data : sourceDataList) {
                if (!existingUrlSet.contains(data.getUrl())) {
                    if (data.getStatus() == null) {
                        data.setStatus(CrawlerData.DataStatus.NEW);
                    }
                    filteredList.add(data);
                }
            }

            log.info("数据源 {} 过滤后数量: {}，去重数量: {}", 
                    sourceName, filteredList.size(), existingUrls.size());
            
            allFilteredData.addAll(filteredList);
        }
        
        return allFilteredData;
    }
    
    /**
     * 检查URL是否已存在
     */
    public boolean isUrlExists(String url) {
        Optional<CrawlerData> existingData = crawlerDataRepository.findByUrlAndDeleted(url, 0);
        return existingData.isPresent();
    }
    
    /**
     * 批量检查URL是否存在
     */
    public Map<String, Boolean> checkUrlsExist(List<String> urls) {
        if (urls.isEmpty()) {
            return new HashMap<>();
        }
        
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(urls);
        Set<String> existingUrlSet = new HashSet<>(existingUrls);
        
        Map<String, Boolean> result = new HashMap<>();
        for (String url : urls) {
            result.put(url, existingUrlSet.contains(url));
        }
        
        return result;
    }
    
    /**
     * 获取重复URL统计信息（统计真实新增和重复数）
     */
    public Map<String, Object> getDuplicateUrlStats(List<CrawlerData> crawlerDataList) {
        Map<String, Object> stats = new HashMap<>();
        if (crawlerDataList.isEmpty()) {
            stats.put("totalCount", 0);
            stats.put("duplicateCount", 0);
            stats.put("newCount", 0);
            stats.put("duplicateUrls", new ArrayList<>());
            stats.put("newUrls", new ArrayList<>());
            return stats;
        }
        // 提取所有URL
        List<String> urls = crawlerDataList.stream()
                .map(CrawlerData::getUrl)
                .distinct()
                .toList();
        // 批量查询已存在的URL
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(urls);
        stats.put("totalCount", urls.size());
        stats.put("duplicateCount", existingUrls.size());
        stats.put("newCount", urls.size() - existingUrls.size());
        stats.put("duplicateUrls", existingUrls);

        return stats;
    }
    
    /**
     * 根据URL查找数据
     */
    public CrawlerData findByUrl(String url) {
        log.info("根据URL查找数据: {}", url);
        Optional<CrawlerData> data = crawlerDataRepository.findByUrlAndDeleted(url, 0);
        return data.orElse(null);
    }
    
    /**
     * 根据数据源名称查找数据
     */
    public List<CrawlerData> findBySourceName(String sourceName) {
        log.info("根据数据源名称查找数据: {}", sourceName);
        return crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0);
    }
    
    /**
     * 查找未处理的数据
     */
    public List<CrawlerData> findUnprocessedData() {
        log.info("查找未处理的数据");
        return crawlerDataRepository.findByIsProcessedAndDeleted(false, 0);
    }
    
    /**
     * 查找指定时间范围内的数据
     */
    public List<CrawlerData> findByCrawlTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查找指定时间范围内的数据: {} - {}", startTime, endTime);
        return crawlerDataRepository.findByCrawlTimeBetweenAndDeleted(startTime, endTime, 0);
    }
    
    /**
     * 统计指定时间范围内的数据数量
     */
    public long countByCrawlTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("统计指定时间范围内的数据数量: {} - {}", startTime, endTime);
        return crawlerDataRepository.countByCrawlTimeBetweenAndDeleted(startTime, endTime, 0);
    }
    
    /**
     * 统计各数据源的数据数量
     */
    public List<Map<String, Object>> countBySourceName() {
        log.info("统计各数据源的数据数量");
        return crawlerDataRepository.countBySource();
    }
    
    /**
     * 统计各状态的数据数量
     */
    public List<Map<String, Object>> countByStatus() {
        log.info("统计各状态的数据数量");
        return crawlerDataRepository.countByStatus();
    }
    
    /**
     * 根据数据源名称统计数据数量
     */
    public long getCountBySourceName(String sourceName) {
        log.info("根据数据源名称统计数据数量: {}", sourceName);
        return crawlerDataRepository.countBySourceNameAndDeleted(sourceName, 0);
    }
    
    /**
     * 根据状态统计数据数量
     */
    public long getCountByStatus(CrawlerData.DataStatus status) {
        log.info("根据状态统计数据数量: {}", status);
        return crawlerDataRepository.countByStatusAndDeleted(status, 0);
    }
    
    /**
     * 分页查询所有数据
     */
    public Map<String, Object> findAllWithPagination(long current, long size) {
        log.info("分页查询所有数据: current={}, size={}", current, size);
        
        Pageable pageable = PageRequest.of((int) (current - 1), (int) size);
        Page<CrawlerData> page = crawlerDataRepository.findAll(pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", page.getTotalPages());
        
        return result;
    }
    
    /**
     * 根据ID获取数据
     */
    public CrawlerData getById(String id) {
        log.info("根据ID获取数据: {}", id);
        Optional<CrawlerData> data = crawlerDataRepository.findById(id);
        return data.orElse(null);
    }
    
    /**
     * 根据数据源名称分页查询数据
     */
    public Map<String, Object> findBySourceNameWithPagination(String sourceName, long current, long size) {
        log.info("根据数据源名称分页查询数据: sourceName={}, current={}, size={}", sourceName, current, size);
        
        Pageable pageable = PageRequest.of((int) (current - 1), (int) size);
        Page<CrawlerData> page = crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", page.getTotalPages());
        
        return result;
    }
    
    /**
     * 根据状态分页查询数据
     */
    public Map<String, Object> findByStatusWithPagination(CrawlerData.DataStatus status, long current, long size) {
        log.info("根据状态分页查询数据: status={}, current={}, size={}", status, current, size);
        
        Pageable pageable = PageRequest.of((int) (current - 1), (int) size);
        Page<CrawlerData> page = crawlerDataRepository.findByStatusAndDeleted(status, 0, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", page.getTotalPages());
        
        return result;
    }
    
    /**
     * 根据关键词搜索数据
     */
    public Map<String, Object> searchByKeyword(String keyword, long current, long size) {
        log.info("根据关键词搜索数据: keyword={}, current={}, size={}", keyword, current, size);
        
        Pageable pageable = PageRequest.of((int) (current - 1), (int) size);
        Page<CrawlerData> page = crawlerDataRepository.searchByKeyword(keyword, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", page.getTotalPages());
        
        return result;
    }
    
    /**
     * 根据国家查询数据
     */
    public List<CrawlerData> findByCountry(String country) {
        log.info("根据国家查询数据: {}", country);
        return crawlerDataRepository.findByCountryAndDeleted(country, 0);
    }
    
    /**
     * 根据类型查询数据
     */
    public List<CrawlerData> findByType(String type) {
        log.info("根据类型查询数据: {}", type);
        return crawlerDataRepository.findByTypeAndDeleted(type, 0);
    }
    
    /**
     * 查找最新数据
     */
    public List<CrawlerData> findLatestData(long limit) {
        log.info("查找最新数据: limit={}", limit);
        Pageable pageable = PageRequest.of(0, (int) limit);
        return crawlerDataRepository.findRecentData(pageable);
    }
    
    /**
     * 根据数据源查找最新数据
     */
    public List<CrawlerData> findLatestDataBySource(String sourceName, long limit) {
        log.info("根据数据源查找最新数据: sourceName={}, limit={}", sourceName, limit);
        Pageable pageable = PageRequest.of(0, (int) limit);
        return crawlerDataRepository.findRecentDataBySource(sourceName, pageable);
    }
    
    /**
     * 更新数据状态
     */
    public boolean updateStatus(Long id, CrawlerData.DataStatus status) {
        log.info("更新数据状态: id={}, status={}", id, status);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.updateStatus(id, status, now);
        return result > 0;
    }
    
    /**
     * 标记为已处理
     */
    public boolean markAsProcessed(Long id) {
        log.info("标记为已处理: {}", id);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.markAsProcessed(id, now, now);
        return result > 0;
    }
    
    /**
     * 标记为处理中
     */
    public boolean markAsProcessing(Long id) {
        log.info("标记为处理中: {}", id);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.markAsProcessing(id, now);
        return result > 0;
    }
    
    /**
     * 标记为错误
     */
    public boolean markAsError(Long id, String errorMessage) {
        log.info("标记为错误: id={}, errorMessage={}", id, errorMessage);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.markAsError(id, errorMessage, now);
        return result > 0;
    }
    
    /**
     * 根据ID删除数据
     */
    public boolean removeById(Long id) {
        log.info("根据ID删除数据: {}", id);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.softDelete(id, now);
        return result > 0;
    }
    
    /**
     * 获取总数据量
     */
    public long getTotalCount() {
        log.info("获取总数据��");
        return crawlerDataRepository.count();
    }
    
    /**
     * 删除旧数据
     */
    public boolean deleteOldData(LocalDateTime cutoffTime) {
        log.info("删除旧数据: cutoffTime={}", cutoffTime);
        LocalDateTime now = LocalDateTime.now();
        int result = crawlerDataRepository.deleteOldData(cutoffTime, now);
        return result > 0;
    }
    
    /**
     * 获取趋势数据
     */
    public Map<String, Object> getTrendData(int days) {
        log.info("获取趋势数据: days={}", days);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> dailyStats = crawlerDataRepository.getDailyTrend(startDate);
        
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("totalCount", getTotalCount());
        trendData.put("dailyStats", dailyStats);
        trendData.put("startDate", startDate.toLocalDate().toString());
        trendData.put("endDate", LocalDateTime.now().toLocalDate().toString());
        
        return trendData;
    }
    
    /**
     * 获取数据统计信息（用于可视化）
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总数据量
        statistics.put("totalCount", getTotalCount());
        
        // 各数据源统计
        statistics.put("sourceStats", countBySourceName());
        
        // 各状态统计
        statistics.put("statusStats", countByStatus());
        
        // 今日数据量
        statistics.put("todayCount", crawlerDataRepository.countTodayData());
        
        // 本周数据量
        statistics.put("weekCount", crawlerDataRepository.countWeekData());
        
        // 本月数据量
        statistics.put("monthCount", crawlerDataRepository.countMonthData());
        
        return statistics;
    }
    
    /**
     * 获取所有数据
     */
    public List<CrawlerData> list() {
        log.info("获取所有数据");
        return crawlerDataRepository.findAll();
    }
    
    /**
     * 批量更新状态
     */
    public boolean batchUpdateStatus(List<Long> ids, CrawlerData.DataStatus status) {
        log.info("批量更新状态: ids={}, status={}", ids, status);
        LocalDateTime now = LocalDateTime.now();
        
        for (Long id : ids) {
            crawlerDataRepository.updateStatus(id, status, now);
        }
        
        return true;
    }
    
    /**
     * 批量标记为已处理
     */
    public boolean batchMarkAsProcessed(List<Long> ids) {
        log.info("批量标记为已处理: ids={}", ids);
        LocalDateTime now = LocalDateTime.now();
        
        for (Long id : ids) {
            crawlerDataRepository.markAsProcessed(id, now, now);
        }
        
        return true;
    }
    
    /**
     * 批量删除数据
     */
    public boolean batchRemoveByIds(List<Long> ids) {
        log.info("批量删除数据: ids={}", ids);
        LocalDateTime now = LocalDateTime.now();
        
        for (Long id : ids) {
            crawlerDataRepository.softDelete(id, now);
        }
        
        return true;
    }
    
    /**
     * 根据条件查询数据
     */
    public List<CrawlerData> findByConditions(String sourceName, CrawlerData.DataStatus status, String country, String type) {
        log.info("根据条件查询数据: sourceName={}, status={}, country={}, type={}", sourceName, status, country, type);
        
        List<CrawlerData> result = new ArrayList<>();
        
        if (sourceName != null && status != null) {
            result = crawlerDataRepository.findBySourceNameAndStatusAndDeleted(sourceName, status, 0);
        } else if (sourceName != null) {
            result = crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0);
        } else if (status != null) {
            result = crawlerDataRepository.findByStatusAndDeleted(status, 0);
        } else if (country != null) {
            result = crawlerDataRepository.findByCountryAndDeleted(country, 0);
        } else if (type != null) {
            result = crawlerDataRepository.findByTypeAndDeleted(type, 0);
        } else {
            result = crawlerDataRepository.findAll();
        }
        
        return result;
    }
    
    /**
     * 综合搜索爬虫数据（支持分页）
     */
    public Page<CrawlerData> searchCrawlerData(String keyword, String country, Boolean related, 
                                              String sourceName, String type, String startDate, 
                                              String endDate, String riskLevel, String matchedKeyword, Pageable pageable) {
        log.info("综合搜索爬虫数据: keyword={}, country={}, related={}, sourceName={}, type={}, startDate={}, endDate={}, riskLevel={}, matchedKeyword={}", 
                keyword, country, related, sourceName, type, startDate, endDate, riskLevel, matchedKeyword);
        
        // 处理日期转换
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            } catch (Exception e) {
                log.warn("开始日期格式解析失败: {}", startDate);
            }
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            } catch (Exception e) {
                log.warn("结束日期格式解析失败: {}", endDate);
            }
        }
        
        // 检查是否所有搜索条件都为空（查询所有数据）
        boolean isAllEmpty = (keyword == null || keyword.trim().isEmpty()) &&
                           (country == null || country.trim().isEmpty()) &&
                           (related == null) &&
                           (sourceName == null || sourceName.trim().isEmpty()) &&
                           (type == null || type.trim().isEmpty()) &&
                           (startDateTime == null) &&
                           (endDateTime == null) &&
                           (riskLevel == null || riskLevel.trim().isEmpty());
        
        if (isAllEmpty) {
            log.info("所有搜索条件都为空，查询所有爬虫数据");
        }
        
        // 检查排序字段，处理字段名映射
        String sortField = pageable.getSort().stream()
                .map(sort -> sort.getProperty())
                .findFirst()
                .orElse("");
        
        // 处理字段名映射：前端可能传递publishTime，但实体类中是publishDate
        String mappedSortField = sortField;
        if ("publishTime".equals(sortField)) {
            mappedSortField = "publishDate";
            log.info("字段名映射: publishTime -> publishDate");
        }
        
        // 重新构建Pageable对象，使用正确的字段名
        Pageable correctedPageable = pageable;
        if (!sortField.equals(mappedSortField)) {
            Sort.Direction direction = pageable.getSort().stream()
                    .map(sort -> sort.getDirection())
                    .findFirst()
                    .orElse(Sort.Direction.DESC);
            correctedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                    Sort.by(direction, mappedSortField));
        }
        
        Page<CrawlerData> result;
        if ("publishDate".equals(mappedSortField)) {
            log.info("使用按发布时间排序的查询方法");
            // 获取排序方向
            Sort.Direction direction = correctedPageable.getSort().stream()
                    .map(sort -> sort.getDirection())
                    .findFirst()
                    .orElse(Sort.Direction.DESC);
            
            log.info("发布时间排序方向: {}", direction);
            
            // 为按发布时间排序创建专门的分页对象，不包含排序信息
            Pageable publishDatePageable = PageRequest.of(correctedPageable.getPageNumber(), correctedPageable.getPageSize());
            result = crawlerDataRepository.searchCrawlerDataOrderByPublishDate(
                keyword, country, related, sourceName, type, startDateTime, endDateTime, riskLevel, matchedKeyword, publishDatePageable
            );
        } else {
            log.info("使用标准查询方法，排序字段: {}", mappedSortField);
            result = crawlerDataRepository.searchCrawlerData(
                keyword, country, related, sourceName, type, startDateTime, endDateTime, riskLevel, matchedKeyword, correctedPageable
            );
        }
        
        log.info("查询结果: 总数={}, 当前页={}, 每页大小={}, 返回数据条数={}", 
                result.getTotalElements(), result.getNumber(), result.getSize(), result.getContent().size());
        
        return result;
    }

    /**
     * 查询指定数据源下所有未删除的URL
     */
    public List<String> findUrlsBySourceName(String sourceName) {
        return crawlerDataRepository.findUrlsBySourceName(sourceName);
    }
    
    /**
     * 高级URL去重（包含URL标准化）
     */
    public List<CrawlerData> advancedUrlDeduplication(List<CrawlerData> crawlerDataList) {
        log.info("执行高级URL去重，原始数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 提取并标准化所有URL
        Map<String, CrawlerData> normalizedUrlMap = new HashMap<>();
        for (CrawlerData data : crawlerDataList) {
            String normalizedUrl = normalizeUrl(data.getUrl());
            if (normalizedUrl != null && !normalizedUrl.isEmpty()) {
                normalizedUrlMap.put(normalizedUrl, data);
            }
        }
        
        // 2. 批量查询已存在的URL（使用标准化后的URL）
        List<String> normalizedUrls = new ArrayList<>(normalizedUrlMap.keySet());
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(normalizedUrls);
        Set<String> existingUrlSet = new HashSet<>(existingUrls);
        
        // 3. 过滤重复URL
        List<CrawlerData> filteredList = normalizedUrlMap.values().stream()
                .filter(data -> !existingUrlSet.contains(normalizeUrl(data.getUrl())))
                .peek(data -> {
                    if (data.getStatus() == null) {
                        data.setStatus(CrawlerData.DataStatus.NEW);
                    }
                })
                .toList();
        
        log.info("高级去重后数量: {}，去重数量: {}", filteredList.size(), existingUrls.size());
        
        return new ArrayList<>(filteredList);
    }
    
    /**
     * URL标准化处理
     */
    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 去除前后空格
            String normalized = url.trim();
            
            // 统一协议（http/https）
            if (normalized.startsWith("http://")) {
                normalized = normalized.replaceFirst("http://", "https://");
            }
            
            // 去除末尾斜杠
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            
            // 转换为小写
            normalized = normalized.toLowerCase();
            
            return normalized;
        } catch (Exception e) {
            log.warn("URL标准化失败: {}", url, e);
            return url;
        }
    }
    
    /**
     * 批量URL标准化
     */
    public Map<String, String> normalizeUrls(List<String> urls) {
        Map<String, String> result = new HashMap<>();
        
        for (String url : urls) {
            String normalized = normalizeUrl(url);
            if (normalized != null) {
                result.put(url, normalized);
            }
        }
        
        return result;
    }
    
    /**
     * 获取URL去重报告
     */
    public Map<String, Object> getUrlDeduplicationReport(List<CrawlerData> crawlerDataList) {
        Map<String, Object> report = new HashMap<>();
        
        if (crawlerDataList.isEmpty()) {
            report.put("totalCount", 0);
            report.put("uniqueCount", 0);
            report.put("duplicateCount", 0);
            report.put("duplicateUrls", new ArrayList<>());
            report.put("uniqueUrls", new ArrayList<>());
            return report;
        }
        
        // 1. 原始URL统计
        List<String> originalUrls = crawlerDataList.stream()
                .map(CrawlerData::getUrl)
                .toList();
        
        Set<String> uniqueOriginalUrls = new HashSet<>(originalUrls);
        
        // 2. 标准化URL统计
        Map<String, String> normalizedUrlMap = normalizeUrls(originalUrls);
        Set<String> uniqueNormalizedUrls = new HashSet<>(normalizedUrlMap.values());
        
        // 3. 数据库重复检查
        List<String> existingUrls = crawlerDataRepository.findExistingUrls(originalUrls);
        Set<String> existingUrlSet = new HashSet<>(existingUrls);
        
        // 4. 构建报告
        report.put("totalCount", originalUrls.size());
        report.put("uniqueOriginalCount", uniqueOriginalUrls.size());
        report.put("uniqueNormalizedCount", uniqueNormalizedUrls.size());
        report.put("databaseDuplicateCount", existingUrls.size());
        report.put("duplicateUrls", existingUrls);
        report.put("uniqueUrls", originalUrls.stream()
                .filter(url -> !existingUrlSet.contains(url))
                .toList());
        
        // 5. 去重效果分析
        int originalDuplicates = originalUrls.size() - uniqueOriginalUrls.size();
        int normalizedDuplicates = originalUrls.size() - uniqueNormalizedUrls.size();
        
        report.put("originalDuplicateCount", originalDuplicates);
        report.put("normalizedDuplicateCount", normalizedDuplicates);
        report.put("deduplicationEffectiveness", String.format("%.2f%%", 
                (double) (originalDuplicates + existingUrls.size()) / originalUrls.size() * 100));
        
        return report;
    }
    
    /**
     * 智能URL去重（结合多种策略）
     */
    public List<CrawlerData> intelligentUrlDeduplication(List<CrawlerData> crawlerDataList) {
        log.info("执行智能URL去重，原始数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 获取去重报告
        Map<String, Object> report = getUrlDeduplicationReport(crawlerDataList);
        
        // 2. 执行高级去重
        List<CrawlerData> deduplicatedList = advancedUrlDeduplication(crawlerDataList);
        
        // 3. 记录去重统计
        log.info("智能去重完成 - 原始: {}, 去重后: {}, 去重率: {}", 
                report.get("totalCount"), 
                deduplicatedList.size(), 
                report.get("deduplicationEffectiveness"));
        
        return deduplicatedList;
    }
    
    /**
     * 安全批量保存爬虫数据（带异常处理和重试机制）
     */
    public List<CrawlerData> safeSaveCrawlerDataList(List<CrawlerData> crawlerDataList) {
        return safeSaveCrawlerDataList(crawlerDataList, 50); // 默认每批50条
    }
    
    /**
     * 安全批量保存爬虫数据（带异常处理和重试机制）
     * @param crawlerDataList 要保存的数据列表
     * @param batchSize 每批保存的数据数量
     * @return 成功保存的数据列表
     */
    public List<CrawlerData> safeSaveCrawlerDataList(List<CrawlerData> crawlerDataList, int batchSize) {
        log.info("安全批量保存爬虫数据，数量: {}，批次大小: {}", crawlerDataList.size(), batchSize);
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 执行去重
        List<CrawlerData> deduplicatedList = filterDuplicateUrls(crawlerDataList);
        
        if (deduplicatedList.isEmpty()) {
            log.info("所有数据都是重复的，无需保存");
            return new ArrayList<>();
        }
        
        // 2. 分批保存，避免一次性处理过多数据
        List<CrawlerData> allSavedData = new ArrayList<>();
        
        for (int i = 0; i < deduplicatedList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, deduplicatedList.size());
            List<CrawlerData> batch = deduplicatedList.subList(i, endIndex);
            
            log.info("保存批次 {}/{}，数量: {}", 
                    (i / batchSize) + 1, 
                    (deduplicatedList.size() + batchSize - 1) / batchSize, 
                    batch.size());
            
            List<CrawlerData> savedBatch = safeSaveBatch(batch);
            allSavedData.addAll(savedBatch);
        }
        
        log.info("安全批量保存完成，成功保存 {} 条数据", allSavedData.size());
        return allSavedData;
    }
    
    /**
     * 安全保存批次数据
     */
    private List<CrawlerData> safeSaveBatch(List<CrawlerData> batch) {
        List<CrawlerData> savedData = new ArrayList<>();
        
        // 首先尝试批量保存
        try {
            List<CrawlerData> savedBatch = crawlerDataRepository.saveAll(batch);
            savedData.addAll(savedBatch);
            log.debug("批次批量保存成功，数量: {}", savedBatch.size());
        } catch (Exception e) {
            log.warn("批次批量保存失败，尝试逐个保存: {}", e.getMessage());

            // 批量保存失败，逐个保存
            for (CrawlerData data : batch) {
                try {
                    CrawlerData savedDataItem = crawlerDataRepository.save(data);
                    savedData.add(savedDataItem);
                } catch (Exception ex) {
                    // 检查是否是重复键错误
                    if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("uk_url")) {
                        log.debug("跳过重复数据，URL: {}", data.getUrl());
                    } else {
                        log.error("保存数据失败，URL: {}, 错误: {}", data.getUrl(), ex.getMessage());
                    }
                }
            }
        }
        
        return savedData;
    }
    
    /**
     * 使用INSERT IGNORE或ON DUPLICATE KEY UPDATE的保存方法
     * 注意：这需要数据库支持，MySQL支持，其他数据库可能需要调整
     */
    public List<CrawlerData> saveWithIgnoreDuplicates(List<CrawlerData> crawlerDataList) {
        log.info("使用忽略重复键的方式保存数据，数量: {}", crawlerDataList.size());
        
        if (crawlerDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 执行去重
        List<CrawlerData> deduplicatedList = filterDuplicateUrls(crawlerDataList);
        
        if (deduplicatedList.isEmpty()) {
            log.info("所有数据都是重复的，无需保存");
            return new ArrayList<>();
        }
        
        List<CrawlerData> savedData = new ArrayList<>();
        
        // 逐个保存，忽略重复键错误
        for (CrawlerData data : deduplicatedList) {
            try {
                CrawlerData savedDataItem = crawlerDataRepository.save(data);
                savedData.add(savedDataItem);
            } catch (Exception e) {
                // 检查���否是重复键错误
                if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("uk_url")) {
                    log.debug("忽略重复数据，URL: {}", data.getUrl());
                } else {
                    log.error("保存数据失败，URL: {}, 错误: {}", data.getUrl(), e.getMessage());
                }
            }
        }
        
        log.info("忽略重复键保存完成，成功保存 {} 条数据", savedData.size());
        return savedData;
    }
    
    // ==================== 产品相关方法 ====================
    
    /**
     * 根据产品名称查询数据
     */
    public List<CrawlerData> findByProduct(String product) {
        log.info("根据产品名称查询数据: {}", product);
        return crawlerDataRepository.findByProductAndDeleted(product, 0);
    }
    
    /**
     * 根据产品名称分页查询
     */
    public Page<CrawlerData> findByProduct(String product, Pageable pageable) {
        log.info("根据产品名称分页查询: {}, 页码: {}, 大小: {}", product, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByProductAndDeleted(product, 0, pageable);
    }
    
    /**
     * 根据产品名称模糊查询
     */
    public Page<CrawlerData> findByProductContaining(String product, Pageable pageable) {
        log.info("根据产品名称模糊查询: {}, 页码: {}, 大小: {}", product, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByProductContaining(product, pageable);
    }
    
    /**
     * 根据产品名称模糊查询（不分页）
     */
    public List<CrawlerData> findByProductContaining(String product) {
        log.info("根据产品名称模糊查询（不分页）: {}", product);
        return crawlerDataRepository.findByProductContaining(product);
    }
    
    /**
     * 根据产品名称统计数据量
     */
    public long countByProduct(String product) {
        log.info("统计产品数据量: {}", product);
        return crawlerDataRepository.countByProductAndDeleted(product, 0);
    }
    
    /**
     * 统计各产品的数据量
     */
    public List<Map<String, Object>> countByProduct() {
        log.info("统计各产品的数据量");
        return crawlerDataRepository.countByProduct();
    }
    
    /**
     * 根据数据源和产品名称查询
     */
    public List<CrawlerData> findBySourceNameAndProduct(String sourceName, String product) {
        log.info("根据数据源和产品名称查询: {} - {}", sourceName, product);
        return crawlerDataRepository.findBySourceNameAndProduct(sourceName, product);
    }
    
    /**
     * 根据数据源和产品名称分页查询
     */
    public Page<CrawlerData> findBySourceNameAndProduct(String sourceName, String product, Pageable pageable) {
        log.info("根据数据源和产品名称分页查询: {} - {}, 页码: {}, 大小: {}", sourceName, product, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findBySourceNameAndProduct(sourceName, product, pageable);
    }
    
    /**
     * 根据产品名称和关键词查询
     */
    public Page<CrawlerData> findByProductAndKeyword(String product, String keyword, Pageable pageable) {
        log.info("根据产品名称和关键词查询: {} - {}, 页码: {}, 大小: {}", product, keyword, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByProductAndKeyword(product, keyword, pageable);
    }
    
    /**
     * 获取热门产品列表（按数据量排序）
     */
    public List<String> getPopularProducts(int limit) {
        log.info("获取热门产品列表，限制数量: {}", limit);
        List<Map<String, Object>> productCounts = crawlerDataRepository.countByProduct();
        
        return productCounts.stream()
                .limit(limit)
                .map(map -> (String) map.get("product"))
                .toList();
    }
    
    /**
     * 根据产品名称获取相关数据统计
     */
    public Map<String, Object> getProductStatistics(String product) {
        log.info("获取产品统计信息: {}", product);
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计数据量
        long totalCount = countByProduct(product);
        statistics.put("totalCount", totalCount);
        
        // 统计各数据源的数据量
        List<CrawlerData> productData = findByProduct(product);
        Map<String, Long> sourceCounts = productData.stream()
                .collect(Collectors.groupingBy(
                        CrawlerData::getSourceName,
                        Collectors.counting()
                ));
        statistics.put("sourceCounts", sourceCounts);
        
        // 统计各状态的数据量
        Map<CrawlerData.DataStatus, Long> statusCounts = productData.stream()
                .collect(Collectors.groupingBy(
                        CrawlerData::getStatus,
                        Collectors.counting()
                ));
        statistics.put("statusCounts", statusCounts);
        
        // 获取最近的数据
        List<CrawlerData> recentData = productData.stream()
                .sorted((a, b) -> b.getCrawlTime().compareTo(a.getCrawlTime()))
                .limit(5)
                .toList();
        statistics.put("recentData", recentData);
        
        return statistics;
    }
    
    // ==================== release_date JSON字段相关服务方法 ====================
    
    /**
     * 查询有发布时间列表的数据
     */
    public List<CrawlerData> findByReleaseDateNotEmpty() {
        log.info("查询有发布时间列表的数据");
        return crawlerDataRepository.findByReleaseDateNotEmpty();
    }
    
    /**
     * 根据数据源查询有发布时间列表的数据
     */
    public List<CrawlerData> findBySourceNameAndReleaseDateNotEmpty(String sourceName) {
        log.info("根据数据源查询有发布时间列表的数据: {}", sourceName);
        return crawlerDataRepository.findBySourceNameAndReleaseDateNotEmpty(sourceName);
    }
    
    // ==================== execution_date JSON字段相关服务方法 ====================
    
    /**
     * 查询有执行时间列表的数据
     */
    public List<CrawlerData> findByExecutionDateNotEmpty() {
        log.info("查询有执行时间列表的数据");
        return crawlerDataRepository.findByExecutionDateNotEmpty();
    }
    
    /**
     * 根据数据源查询有执行时间列表的数据
     */
    public List<CrawlerData> findBySourceNameAndExecutionDateNotEmpty(String sourceName) {
        log.info("根据数据源查询有执行时间列表的数据: {}", sourceName);
        return crawlerDataRepository.findBySourceNameAndExecutionDateNotEmpty(sourceName);
    }
    
    // ==================== 组合查询服务方法 ====================
    
    /**
     * 根据数据源查询同时有发布时间和执行时间的数据
     */
    public List<CrawlerData> findBySourceNameAndBothDatesNotEmpty(String sourceName) {
        log.info("根据数据源查询同时有发布时间和执行时间的数据: {}", sourceName);
        return crawlerDataRepository.findBySourceNameAndBothDatesNotEmpty(sourceName);
    }
    
    // ==================== 统计服务方法 ====================
    
    /**
     * 获取JSON时间字段统计信息
     */
    public Map<String, Object> getJsonTimeStatistics() {
        log.info("获取JSON时间字段统计信息");
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计有发布时间列表的数据量
        List<CrawlerData> releaseData = findByReleaseDateNotEmpty();
        statistics.put("releaseDateCount", releaseData.size());
        
        // 统计有执行时间列表的数据量
        List<CrawlerData> executionData = findByExecutionDateNotEmpty();
        statistics.put("executionDateCount", executionData.size());
        
        // 统计各数据源的时间字段使用情况
        Map<String, Object> sourceStats = new HashMap<>();
        sourceStats.put("ulSolutions", findBySourceNameAndBothDatesNotEmpty("UL Solutions").size());
        sourceStats.put("sgs", findBySourceNameAndBothDatesNotEmpty("SGS").size());
        statistics.put("sourceStatistics", sourceStats);
        
        return statistics;
    }
    
    // ==================== related字段相关服务方法 ====================
    
    /**
     * 根据相关状态查询数据
     */
    public List<CrawlerData> findByRelated(Boolean related) {
        log.info("根据相关状态查询数据: {}", related);
        return crawlerDataRepository.findByRelatedAndDeleted(related, 0);
    }
    
    /**
     * 根据相关状态分页查询
     */
    public Page<CrawlerData> findByRelated(Boolean related, Pageable pageable) {
        log.info("根据相关状态分页查询: {}, 页码: {}, 大小: {}", related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByRelatedAndDeleted(related, 0, pageable);
    }
    
    /**
     * 根据相关状态统计数据量
     */
    public long countByRelated(Boolean related) {
        log.info("统计相关状态数据量: {}", related);
        return crawlerDataRepository.countByRelatedAndDeleted(related, 0);
    }
    
    /**
     * 查询相关数据（related = true）
     */
    public List<CrawlerData> findRelatedData() {
        log.info("查询相关数据");
        return crawlerDataRepository.findRelatedData();
    }
    
    /**
     * 查询相关数据（related = true）分页
     */
    public Page<CrawlerData> findRelatedData(Pageable pageable) {
        log.info("查询相关数据分页，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findRelatedData(pageable);
    }
    
    /**
     * 查询不相关数据（related = false）
     */
    public List<CrawlerData> findUnrelatedData() {
        log.info("查询不相关数据");
        return crawlerDataRepository.findUnrelatedData();
    }
    
    /**
     * 查询不相关数据（related = false）分页
     */
    public Page<CrawlerData> findUnrelatedData(Pageable pageable) {
        log.info("查询不相关数据分页，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findUnrelatedData(pageable);
    }
    
    /**
     * 查询未确定相关性的数据（related = null）
     */
    public List<CrawlerData> findUndeterminedData() {
        log.info("查询未确定相关性的数据");
        return crawlerDataRepository.findUndeterminedData();
    }
    
    /**
     * 查询未确定相关性的数据（related = null）分页
     */
    public Page<CrawlerData> findUndeterminedData(Pageable pageable) {
        log.info("查询未确定相关性数据分页，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findUndeterminedData(pageable);
    }
    
    /**
     * 根据数据源和相关状态查询
     */
    public List<CrawlerData> findBySourceNameAndRelated(String sourceName, Boolean related) {
        log.info("根据数据源和相关状态查询: {} - {}", sourceName, related);
        return crawlerDataRepository.findBySourceNameAndRelatedAndDeleted(sourceName, related, 0);
    }
    
    /**
     * 根据数据源和相关状态分页查询
     */
    public Page<CrawlerData> findBySourceNameAndRelated(String sourceName, Boolean related, Pageable pageable) {
        log.info("根据数据源和相关状态分页查询: {} - {}, 页码: {}, 大小: {}", sourceName, related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findBySourceNameAndRelatedAndDeleted(sourceName, related, 0, pageable);
    }
    
    /**
     * 根据国家相关状态查询
     */
    public List<CrawlerData> findByCountryAndRelated(String country, Boolean related) {
        log.info("根据国家相关状态查询: {} - {}", country, related);
        return crawlerDataRepository.findByCountryAndRelatedAndDeleted(country, related, 0);
    }
    
    /**
     * 根据国家相关状态分页查询
     */
    public Page<CrawlerData> findByCountryAndRelated(String country, Boolean related, Pageable pageable) {
        log.info("根据国家相关状态分页查询: {} - {}, 页码: {}, 大小: {}", country, related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByCountryAndRelatedAndDeleted(country, related, 0, pageable);
    }
    
    /**
     * 根据产品名称和相关状态查询
     */
    public List<CrawlerData> findByProductAndRelated(String product, Boolean related) {
        log.info("根据产品名称和相关状态查询: {} - {}", product, related);
        return crawlerDataRepository.findByProductAndRelatedAndDeleted(product, related, 0);
    }
    
    /**
     * 根据产品名称和相关状态分页查询
     */
    public Page<CrawlerData> findByProductAndRelated(String product, Boolean related, Pageable pageable) {
        log.info("根据产品名称和相关状态分页查询: {} - {}, 页码: {}, 大小: {}", product, related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.findByProductAndRelatedAndDeleted(product, related, 0, pageable);
    }
    
    /**
     * 统计各相关状态的数据量
     */
    public List<Map<String, Object>> countByRelated() {
        log.info("统计各相关状态的数据量");
        return crawlerDataRepository.countByRelated();
    }
    
    /**
     * 获取所有数据源名称
     */
    public List<String> getAllSourceNames() {
        log.info("获取所有数据源名称");
        return crawlerDataRepository.findAllSourceNames();
    }
    
    /**
     * 根据数据源统计各相关状态的数据量
     */
    public List<Map<String, Object>> countBySourceAndRelated() {
        log.info("根据数据源统计各相关状态的数据量");
        return crawlerDataRepository.countBySourceAndRelated();
    }
    
    /**
     * 根据国家统计各相关状态的数据量
     */
    public List<Map<String, Object>> countByCountryAndRelated() {
        log.info("根据国家统计各相关状态的数据量");
        return crawlerDataRepository.countByCountryAndRelated();
    }
    
    /**
     * 更新相关状态
     */
    public boolean updateRelatedStatus(String id, Boolean related) {
        log.info("更新相关状态: {} - {}", id, related);
        try {
            int result = crawlerDataRepository.updateRelatedStatus(id, related, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("更新相关状态时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新相关状态和匹配的关键词
     */
    public boolean updateRelatedStatusWithKeywords(String id, Boolean related, List<String> matchedKeywords) {
        log.info("更新相关状态和匹配关键词: {} - {} - {}", id, related, matchedKeywords);
        try {
            // 将关键词列表转换为逗号分隔的字符串
            String keywordsString = matchedKeywords != null && !matchedKeywords.isEmpty() 
                ? String.join(", ", matchedKeywords) 
                : null;
            
            int result = crawlerDataRepository.updateRelatedStatusWithKeywords(id, related, keywordsString, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("更新相关状态和匹配关键词时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 更新风险等级和风险说明
     */
    public boolean updateRiskLevel(String id, RiskLevel riskLevel, String riskDescription) {
        log.info("更新风险等级: {} - {} - {}", id, riskLevel, riskDescription);
        try {
            int result = crawlerDataRepository.updateRiskLevel(id, riskLevel, riskDescription, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("更新风险等级时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 批量更新相关状态
     */
    public boolean batchUpdateRelatedStatus(List<String> ids, Boolean related) {
        log.info("批量更新相关状态: {} 条记录 - {}", ids.size(), related);
        try {
            int result = crawlerDataRepository.batchUpdateRelatedStatus(ids, related, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("批量更新相关状态时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 根据关键词搜索并过滤相关状态
     */
    public Page<CrawlerData> searchByKeywordAndRelated(String keyword, Boolean related, Pageable pageable) {
        log.info("根据关键词搜索并过滤相关状态: {} - {}, 页码: {}, 大小: {}", keyword, related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.searchByKeywordAndRelated(keyword, related, pageable);
    }
    
    /**
     * 根据多个关键词搜索并过滤相关状态
     */
    public Page<CrawlerData> searchByKeywordsAndRelated(String keyword, String keyword2, String keyword3, 
                                                       String country, String sourceName, Boolean related, Pageable pageable) {
        log.info("根据多个关键词搜索并过滤相关状态: {} - {} - {} - {} - {} - {}, 页码: {}, 大小: {}", 
                keyword, keyword2, keyword3, country, sourceName, related, pageable.getPageNumber(), pageable.getPageSize());
        return crawlerDataRepository.searchByKeywordsAndRelated(keyword, keyword2, keyword3, country, sourceName, related, pageable);
    }
    
    /**
     * 获取相关状态统计信息
     */
    public Map<String, Object> getRelatedStatistics() {
        log.info("获取相关状态统计信息");
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计各相关状态的数据量
        List<Map<String, Object>> relatedCounts = countByRelated();
        statistics.put("relatedCounts", relatedCounts);
        
        // 统计各数据源的相关状态分布
        List<Map<String, Object>> sourceRelatedCounts = countBySourceAndRelated();
        statistics.put("sourceRelatedCounts", sourceRelatedCounts);
        
        // 统计各国家的相关状态分布
        List<Map<String, Object>> countryRelatedCounts = countByCountryAndRelated();
        statistics.put("countryRelatedCounts", countryRelatedCounts);
        
        // 计算总体统计
        long totalCount = crawlerDataRepository.count();
        long relatedCount = countByRelated(true);
        long unrelatedCount = countByRelated(false);
        long undeterminedCount = countByRelated(null);
        
        statistics.put("totalCount", totalCount);
        statistics.put("relatedCount", relatedCount);
        statistics.put("unrelatedCount", unrelatedCount);
        statistics.put("undeterminedCount", undeterminedCount);
        statistics.put("relatedPercentage", totalCount > 0 ? (double) relatedCount / totalCount * 100 : 0);
        statistics.put("unrelatedPercentage", totalCount > 0 ? (double) unrelatedCount / totalCount * 100 : 0);
        statistics.put("undeterminedPercentage", totalCount > 0 ? (double) undeterminedCount / totalCount * 100 : 0);
        
        return statistics;
    }
    
    /**
     * 自动处理相关状态
     * 根据关键词匹配自动设置相关状态
     */
    public Map<String, Object> autoProcessRelatedStatus() {
        return autoProcessRelatedStatus(null);
    }
    
    /**
     * 自动处理相关状态（带关键词参数）
     * 根据关键词匹配自动设置相关状态
     */
    public Map<String, Object> autoProcessRelatedStatus(List<String> customKeywords) {
        log.info("开始自动处理所有数据的相关状态，使用关键词: {}", customKeywords);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有未删除的数据
            List<CrawlerData> allData = crawlerDataRepository.findByDeleted(0);
            log.info("找到 {} 条数据需要处理", allData.size());
            
            int processedCount = 0;
            int relatedCount = 0;
            int unrelatedCount = 0;
            int unchangedCount = 0;
            int riskProcessedCount = 0; // 新增：风险等级处理计数
            
            for (CrawlerData data : allData) {
                // 检查标题、内容、摘要是否包含关键词
                String searchText = "";
                if (data.getTitle() != null) {
                    searchText += data.getTitle() + " ";
                }
                if (data.getContent() != null) {
                    searchText += data.getContent() + " ";
                }
                if (data.getSummary() != null) {
                    searchText += data.getSummary() + " ";
                }
                if (data.getProduct() != null) {
                    searchText += data.getProduct() + " ";
                }
                
                // 检查是否包含关键词
                boolean isRelated;
                List<String> matchedKeywords = new ArrayList<>();
                
                if (customKeywords != null && !customKeywords.isEmpty()) {
                    // 使用自定义关键词进行匹配
                    matchedKeywords = getMatchedKeywords(searchText, customKeywords);
                    isRelated = !matchedKeywords.isEmpty();
                } else {
                    // 使用默认关键词服务
                    matchedKeywords = keywordService.getContainedKeywords(searchText);
                    isRelated = !matchedKeywords.isEmpty();
                }
                
                // 检查当前状态是否需要更新
                boolean needsUpdate = data.getRelated() == null || data.getRelated() != isRelated;
                
                if (needsUpdate) {
                    // 更新相关状态和匹配的关键词
                    boolean updateSuccess = updateRelatedStatusWithKeywords(data.getId(), isRelated, matchedKeywords);
                    if (updateSuccess) {
                        // 如果标记为相关，同时设置为高风险
                        if (isRelated) {
                            try {
                                boolean riskUpdateSuccess = updateRiskLevel(data.getId(), RiskLevel.HIGH, "自动处理时设置为高风险");
                                if (riskUpdateSuccess) {
                                    riskProcessedCount++;
                                    log.debug("已将相关数据 {} 设置为高风险", data.getId());
                                }
                            } catch (Exception e) {
                                log.warn("设置数据 {} 风险等级失败: {}", data.getId(), e.getMessage());
                            }
                        }
                        
                        processedCount++;
                        if (isRelated) {
                            relatedCount++;
                        } else {
                            unrelatedCount++;
                        }
                    }
                } else {
                    unchangedCount++;
                }
            }
            
            result.put("success", true);
            result.put("totalProcessed", processedCount);
            result.put("relatedCount", relatedCount);
            result.put("unrelatedCount", unrelatedCount);
            result.put("unchangedCount", unchangedCount);
            result.put("riskProcessedCount", riskProcessedCount); // 新增：风险等级处理计数
            result.put("totalData", allData.size());
            result.put("usedKeywords", customKeywords != null ? customKeywords.size() : keywordService.getAllEnabledKeywords().size());
            result.put("message", String.format("自动处理完成，共处理 %d 条数据，相关 %d 条（设置为高风险 %d 条），不相关 %d 条，未变更 %d 条，使用 %d 个关键词", 
                processedCount, relatedCount, riskProcessedCount, unrelatedCount, unchangedCount, 
                customKeywords != null ? customKeywords.size() : keywordService.getAllEnabledKeywords().size()));
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("自动处理相关状态完成: 处理 {} 条，相关 {} 条（设置为高风险 {} 条），不相关 {} 条，未变更 {} 条", 
                processedCount, relatedCount, riskProcessedCount, unrelatedCount, unchangedCount);
            
        } catch (Exception e) {
            log.error("自动处理相关状态时发生错误: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 检查文本是否包含任何关键词
     */
    private boolean containsAnyKeyword(String text, List<String> keywords) {
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取文本中匹配的关键词列表
     */
    private List<String> getMatchedKeywords(String text, List<String> keywords) {
        List<String> matchedKeywords = new ArrayList<>();
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) {
            return matchedKeywords;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    matchedKeywords.add(keyword);
                }
            }
        }
        return matchedKeywords;
    }
    
    /**
     * 根据数据源自动处理相关状态
     */
    public Map<String, Object> autoProcessRelatedStatusBySource(String sourceName) {
        log.info("开始根据数据源自动处理所有数据的相关状态: {}", sourceName);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取指定数据源的所有未删除数据
            List<CrawlerData> allData = crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0);
            log.info("找到数据源 {} 的 {} 条数据需要处理", sourceName, allData.size());
            
            int processedCount = 0;
            int relatedCount = 0;
            int unrelatedCount = 0;
            int unchangedCount = 0;
            
            for (CrawlerData data : allData) {
                // 检查标题、内容、摘要是否包含关键词
                String searchText = "";
                if (data.getTitle() != null) {
                    searchText += data.getTitle() + " ";
                }
                if (data.getContent() != null) {
                    searchText += data.getContent() + " ";
                }
                if (data.getSummary() != null) {
                    searchText += data.getSummary() + " ";
                }
                if (data.getProduct() != null) {
                    searchText += data.getProduct() + " ";
                }
                
                // 检查是否包含关键词
                boolean isRelated = keywordService.containsAnyKeyword(searchText);
                
                // 检查当前状态是否需要更新
                boolean needsUpdate = data.getRelated() == null || data.getRelated() != isRelated;
                
                if (needsUpdate) {
                    // 更新相关状态
                    boolean updateSuccess = updateRelatedStatus(data.getId(), isRelated);
                    if (updateSuccess) {
                        processedCount++;
                        if (isRelated) {
                            relatedCount++;
                        } else {
                            unrelatedCount++;
                        }
                    }
                } else {
                    unchangedCount++;
                }
            }
            
            result.put("success", true);
            result.put("sourceName", sourceName);
            result.put("totalProcessed", processedCount);
            result.put("relatedCount", relatedCount);
            result.put("unrelatedCount", unrelatedCount);
            result.put("unchangedCount", unchangedCount);
            result.put("totalData", allData.size());
            result.put("message", String.format("数据源 %s 自动处理完成，共处理 %d 条数据，相关 %d 条，不相关 %d 条，未变更 %d 条", 
                sourceName, processedCount, relatedCount, unrelatedCount, unchangedCount));
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("数据源 {} 自动处理相关状态完成: 处理 {} 条，相关 {} 条，不相关 {} 条，未变更 {} 条", 
                sourceName, processedCount, relatedCount, unrelatedCount, unchangedCount);
            
        } catch (Exception e) {
            log.error("根据数据源自动处理相关状态时发生错误: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 根据标题和内容自动更新国家字段
     */
    public Map<String, Object> autoUpdateCountryFromContent() {
        log.info("开始根据标题和内容自动更新国家字段");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有未删除的数据
            List<CrawlerData> allData = crawlerDataRepository.findAll().stream()
                .filter(data -> data.getDeleted() == 0)
                .toList();
            
            log.info("找到 {} 条数据需要处理", allData.size());
            
            int processedCount = 0;
            int updatedCount = 0;
            int unchangedCount = 0;
            Map<String, Integer> countryUpdates = new HashMap<>();
            
            for (CrawlerData data : allData) {
                // 分析标题和内容中的国家信息
                String detectedCountry = detectCountryFromContent(data.getTitle(), data.getContent());
                
                // 添加调试日志
                if (processedCount < 10) { // 记录前10条数据的调试信息
                    String titlePreview = data.getTitle() != null ? 
                        data.getTitle().substring(0, Math.min(100, data.getTitle().length())) : "null";
                    String contentPreview = data.getContent() != null ? 
                        data.getContent().substring(0, Math.min(100, data.getContent().length())) : "null";
                    
                    log.info("数据ID: {}, 标题: {}, 内容预览: {}, 当前国家: {}, 检测到的国家: {}", 
                        data.getId(), 
                        titlePreview,
                        contentPreview,
                        data.getCountry(),
                        detectedCountry);
                }
                
                if (detectedCountry != null && !detectedCountry.equals(data.getCountry())) {
                    // 更新国家字段
                    data.setCountry(detectedCountry);
                    data.setUpdatedAt(LocalDateTime.now());
                    crawlerDataRepository.save(data);
                    
                    updatedCount++;
                    countryUpdates.put(detectedCountry, countryUpdates.getOrDefault(detectedCountry, 0) + 1);
                    
                    log.info("更新数据ID: {}, 从 {} 更新为 {}", data.getId(), data.getCountry(), detectedCountry);
                } else {
                    unchangedCount++;
                }
                processedCount++;
            }
            
            result.put("success", true);
            result.put("totalProcessed", processedCount);
            result.put("updatedCount", updatedCount);
            result.put("unchangedCount", unchangedCount);
            result.put("countryUpdates", countryUpdates);
            result.put("message", String.format("自动更新国家字段完成，共处理 %d 条数据，更新 %d 条，未变更 %d 条", 
                processedCount, updatedCount, unchangedCount));
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("自动更新国家字段完成: 处理 {} 条，更新 {} 条，未变更 {} 条", 
                processedCount, updatedCount, unchangedCount);
            
        } catch (Exception e) {
            log.error("自动更新国家字段时发生错误: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动更新国家字段失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 获取当前数据的国家分布统计
     */
    public Map<String, Object> getCountryDistribution() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始统计当前数据的国家分布");
            
            // 获取所有未删除的数据
            List<CrawlerData> allData = crawlerDataRepository.findAll().stream()
                .filter(data -> data.getDeleted() == 0)
                .toList();
            
            Map<String, Integer> countryStats = new HashMap<>();
            int nullCountryCount = 0;
            int emptyCountryCount = 0;
            
            for (CrawlerData data : allData) {
                String country = data.getCountry();
                if (country == null) {
                    nullCountryCount++;
                } else if (country.trim().isEmpty()) {
                    emptyCountryCount++;
                } else {
                    countryStats.put(country, countryStats.getOrDefault(country, 0) + 1);
                }
            }
            
            result.put("success", true);
            result.put("totalCount", allData.size());
            result.put("countryStats", countryStats);
            result.put("nullCountryCount", nullCountryCount);
            result.put("emptyCountryCount", emptyCountryCount);
            result.put("message", String.format("国家分布统计完成，总数: %d", allData.size()));
            
            log.info("国家分布统计完成: 总数={}, 空值={}, 空字符串={}, 分布={}", 
                allData.size(), nullCountryCount, emptyCountryCount, countryStats);
            
        } catch (Exception e) {
            log.error("获取国家分布统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家分布统计失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从标题和内容中检测国家信息
     */
    private String detectCountryFromContent(String title, String content) {
        if ((title == null || title.trim().isEmpty()) && 
            (content == null || content.trim().isEmpty())) {
            return null;
        }
        
        String fullText = "";
        if (title != null) {
            fullText += title + " ";
        }
        if (content != null) {
            fullText += content + " ";
        }
        
        fullText = fullText.toLowerCase();
        
        // 国家关键词映射 - 统一使用中文格式
        Map<String, String> countryKeywords = new HashMap<>();
        countryKeywords.put("中国", "中国");
        countryKeywords.put("中华人民共和国", "中国");
        countryKeywords.put("china", "中国");
        countryKeywords.put("chinese", "中国");
        countryKeywords.put("cn", "中国");
        countryKeywords.put("美国", "美国");
        countryKeywords.put("美利坚合众国", "美国");
        countryKeywords.put("united states", "美国");
        countryKeywords.put("usa", "美国");
        countryKeywords.put("america", "美国");
        countryKeywords.put("us", "美国");
        countryKeywords.put("欧盟", "欧盟");
        countryKeywords.put("european union", "欧盟");
        countryKeywords.put("eu", "欧盟");
        countryKeywords.put("europe", "欧盟");
        countryKeywords.put("日本", "日本");
        countryKeywords.put("japan", "日本");
        countryKeywords.put("japanese", "日本");
        countryKeywords.put("jp", "日本");
        countryKeywords.put("韩国", "韩国");
        countryKeywords.put("korea", "韩国");
        countryKeywords.put("korean", "韩国");
        countryKeywords.put("kr", "韩国");
        countryKeywords.put("印度", "印度");
        countryKeywords.put("india", "印度");
        countryKeywords.put("indian", "印度");
        countryKeywords.put("in", "印度");
        countryKeywords.put("泰国", "泰国");
        countryKeywords.put("thailand", "泰国");
        countryKeywords.put("thai", "泰国");
        countryKeywords.put("th", "泰国");
        countryKeywords.put("新加坡", "新加坡");
        countryKeywords.put("singapore", "新加坡");
        countryKeywords.put("sg", "新加坡");
        countryKeywords.put("台湾", "台湾");
        countryKeywords.put("taiwan", "台湾");
        countryKeywords.put("tw", "台湾");
        countryKeywords.put("澳大利亚", "澳大利亚");
        countryKeywords.put("australia", "澳大利亚");
        countryKeywords.put("australian", "澳大利亚");
        countryKeywords.put("au", "澳大利亚");
        countryKeywords.put("智利", "智利");
        countryKeywords.put("chile", "智利");
        countryKeywords.put("cl", "智利");
        countryKeywords.put("马来西亚", "马来西亚");
        countryKeywords.put("malaysia", "马来西亚");
        countryKeywords.put("my", "马来西亚");
        countryKeywords.put("阿联酋", "阿联酋");
        countryKeywords.put("uae", "阿联酋");
        countryKeywords.put("united arab emirates", "阿联酋");
        countryKeywords.put("ae", "阿联酋");
        countryKeywords.put("秘鲁", "秘鲁");
        countryKeywords.put("peru", "秘鲁");
        countryKeywords.put("pe", "秘鲁");
        countryKeywords.put("南非", "南非");
        countryKeywords.put("south africa", "南非");
        countryKeywords.put("za", "南非");
        countryKeywords.put("以色列", "以色列");
        countryKeywords.put("israel", "以色列");
        countryKeywords.put("il", "以色列");
        countryKeywords.put("印度尼西亚", "印度尼西亚");
        countryKeywords.put("indonesia", "印度尼西亚");
        countryKeywords.put("id", "印度尼西亚");
        countryKeywords.put("加拿大", "加拿大");
        countryKeywords.put("canada", "加拿大");
        countryKeywords.put("canadian", "加拿大");
        countryKeywords.put("ca", "加拿大");
        countryKeywords.put("海外", "海外");
        countryKeywords.put("overseas", "海外");
        countryKeywords.put("国际", "海外");
        countryKeywords.put("international", "海外");
        countryKeywords.put("global", "海外");
        countryKeywords.put("世界", "海外");
        countryKeywords.put("world", "海外");
        
        // 按优先级排序（更具体的词优先）
        List<String> priorityKeywords = Arrays.asList(
            "中华人民共和国", "美利坚合众国", "european union", "united arab emirates",
            "中国", "美国", "欧盟", "日本", "韩国", "印度", "泰国", "新加坡", "台湾", "澳大利亚", 
            "智利", "马来西亚", "阿联酋", "秘鲁", "南非", "以色列", "印度尼西亚", "加拿大", "海外",
            "china", "united states", "usa", "america", "eu", "japan", "korea", "india", 
            "thailand", "singapore", "taiwan", "australia", "chile", "malaysia", "uae", 
            "peru", "south africa", "israel", "indonesia", "canada", "overseas",
            "chinese", "japanese", "korean", "indian", "thai", "australian", "canadian",
            "cn", "us", "europe", "jp", "kr", "in", "th", "sg", "tw", "au", "cl", "my", 
            "ae", "pe", "za", "il", "id", "ca", "international", "global", "世界", "world"
        );
        
        // 按优先级检查关键词
        for (String keyword : priorityKeywords) {
            if (fullText.contains(keyword.toLowerCase())) {
                return countryKeywords.get(keyword);
            }
        }
        
        return null;
    }

    /**
     * 批量更新数据库中SGS和UL数据的发布日期格式
     * 将非标准格式的日期统一转换为yyyy-MM-dd格式
     */
    public Map<String, Object> batchUpdatePublishDateFormats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始批量更新数据库中SGS和UL数据的日期格式");
            
            // 动态获取所有数据源名称
            List<String> allSourceNames = crawlerDataRepository.findAllSourceNames();
            log.info("数据库中所有数据源名称: {}", allSourceNames);
            
            // 筛选出SGS和UL相关的数据源
            List<String> targetSources = allSourceNames.stream()
                    .filter(sourceName -> sourceName != null && 
                            (sourceName.equals("SGS") || 
                             sourceName.startsWith("UL") || 
                             sourceName.contains("UL")))
                    .collect(Collectors.toList());
            
            log.info("需要处理的数据源: {}", targetSources);
            
            // 获取所有目标数据源的数据
            List<CrawlerData> allData = new ArrayList<>();
            Map<String, Integer> sourceCounts = new HashMap<>();
            
            for (String sourceName : targetSources) {
                List<CrawlerData> sourceData = crawlerDataRepository.findBySourceNameAndDeleted(sourceName, 0);
                allData.addAll(sourceData);
                sourceCounts.put(sourceName, sourceData.size());
                log.info("找到{}数据: {} 条", sourceName, sourceData.size());
            }
            
            log.info("总计需要处理的数据: {} 条", allData.size());
            
            int totalProcessed = 0;
            int totalUpdated = 0;
            int totalUnchanged = 0;
            int totalFailed = 0;
            
            Map<String, Integer> sourceStats = new HashMap<>();
            // 初始化所有目标数据源的统计
            for (String sourceName : targetSources) {
                sourceStats.put(sourceName, 0);
            }
            
            for (CrawlerData data : allData) {
                totalProcessed++;
                
                String originalDate = data.getPublishDate();
                if (originalDate == null || originalDate.trim().isEmpty()) {
                    totalUnchanged++;
                    continue;
                }
                
                // 检查是否已经是标准格式
                if (dateFormatService.isValidDate(originalDate) && originalDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    totalUnchanged++;
                    continue;
                }
                
                // 统一日期格式
                String standardizedDate = dateFormatService.standardizeDate(originalDate);
                if (standardizedDate != null && !standardizedDate.equals(originalDate)) {
                    data.setPublishDate(standardizedDate);
                    crawlerDataRepository.save(data);
                    totalUpdated++;
                    
                    // 统计各数据源的更新数量
                    String sourceName = data.getSourceName();
                    if (sourceStats.containsKey(sourceName)) {
                        sourceStats.put(sourceName, sourceStats.get(sourceName) + 1);
                    }
                    
                    log.debug("更新数据ID: {}, 源: {}, 日期: {} -> {}", 
                            data.getId(), sourceName, originalDate, standardizedDate);
                } else if (standardizedDate == null) {
                    totalFailed++;
                    log.warn("无法解析日期格式，数据ID: {}, 源: {}, 日期: {}", 
                            data.getId(), data.getSourceName(), originalDate);
                } else {
                    totalUnchanged++;
                }
                
                // 每处理100条数据记录一次进度
                if (totalProcessed % 100 == 0) {
                    log.info("已处理: {} 条数据，更新: {} 条，未变更: {} 条，失败: {} 条", 
                            totalProcessed, totalUpdated, totalUnchanged, totalFailed);
                }
            }
            
            result.put("totalProcessed", totalProcessed);
            result.put("totalUpdated", totalUpdated);
            result.put("totalUnchanged", totalUnchanged);
            result.put("totalFailed", totalFailed);
            result.put("sourceStats", sourceStats);
            result.put("message", String.format("批量更新完成，处理: %d 条，更新: %d 条，未变更: %d 条，失败: %d 条", 
                    totalProcessed, totalUpdated, totalUnchanged, totalFailed));
            
            log.info("批量更新日期格式完成，处理: {} 条，更新: {} 条，未变更: {} 条，失败: {} 条", 
                    totalProcessed, totalUpdated, totalUnchanged, totalFailed);
            log.info("各数据源更新统计: {}", sourceStats);
            
        } catch (Exception e) {
            log.error("批量更新日期格式失败", e);
            result.put("error", "批量更新日期格式失败: " + e.getMessage());
        }
        
        return result;
    }

}

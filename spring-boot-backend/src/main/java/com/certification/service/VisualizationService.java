package com.certification.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.certification.entity.common.CrawlerData;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 可视化服务类
 * 用于生成图表和导出数据
 */
@Slf4j
@Service
public class VisualizationService {
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Value("${app.visualization.export.excel-path:./exports/}")
    private String excelExportPath;
    
    @Value("${app.visualization.export.chart-path:./charts/}")
    private String chartExportPath;
    
    @Value("${app.visualization.chart.width:800}")
    private int chartWidth;
    
    @Value("${app.visualization.chart.height:600}")
    private int chartHeight;
    
    /**
     * 生成数据源分布饼图
     */
    public String generateSourcePieChart() {
        try {
            List<Map<String, Object>> sourceStats = crawlerDataService.countBySourceName();
            
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map<String, Object> stat : sourceStats) {
                String sourceName = (String) stat.get("source_name");
                Long count = (Long) stat.get("count");
                dataset.setValue(sourceName, count);
            }
            
            JFreeChart chart = ChartFactory.createPieChart(
                "数据源分布", 
                dataset, 
                true, 
                true, 
                false
            );
            
            String fileName = "source_pie_chart_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            String filePath = chartExportPath + fileName;
            
            // 确保目录存在
            new File(chartExportPath).mkdirs();
            
            ChartUtils.saveChartAsPNG(new File(filePath), chart, chartWidth, chartHeight);
            
            log.info("数据源分布饼图已生成: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("生成数据源分布饼图失败", e);
            return null;
        }
    }
    
    /**
     * 生成数据状态分布柱状图
     */
    public String generateStatusBarChart() {
        try {
            List<Map<String, Object>> statusStats = crawlerDataService.countByStatus();
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map<String, Object> stat : statusStats) {
                String status = (String) stat.get("status");
                Long count = (Long) stat.get("count");
                dataset.addValue(count, "数据量", status);
            }
            
            JFreeChart chart = ChartFactory.createBarChart(
                "数据状态分布", 
                "状态", 
                "数量", 
                dataset, 
                PlotOrientation.VERTICAL, 
                true, 
                true, 
                false
            );
            
            String fileName = "status_bar_chart_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            String filePath = chartExportPath + fileName;
            
            // 确保目录存在
            new File(chartExportPath).mkdirs();
            
            ChartUtils.saveChartAsPNG(new File(filePath), chart, chartWidth, chartHeight);
            
            log.info("数据状态分布柱状图已生成: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("生成数据状态分布柱状图失败", e);
            return null;
        }
    }
    
    /**
     * 生成时间趋势图
     */
    public String generateTrendChart(int days) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // 按天统计数据量
            for (int i = 0; i < days; i++) {
                LocalDateTime dayStart = startDate.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);
                
                long count = crawlerDataService.countByCrawlTimeBetween(dayStart, dayEnd);
                String dateStr = dayStart.format(DateTimeFormatter.ofPattern("MM-dd"));
                
                dataset.addValue(count, "数据量", dateStr);
            }
            
            JFreeChart chart = ChartFactory.createLineChart(
                "数据趋势图 (" + days + "天)", 
                "日期", 
                "数量", 
                dataset, 
                PlotOrientation.VERTICAL, 
                true, 
                true, 
                false
            );
            
            String fileName = "trend_chart_" + days + "days_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            String filePath = chartExportPath + fileName;
            
            // 确保目录存在
            new File(chartExportPath).mkdirs();
            
            ChartUtils.saveChartAsPNG(new File(filePath), chart, chartWidth, chartHeight);
            
            log.info("数据趋势图已生成: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("生成数据趋势图失败", e);
            return null;
        }
    }
    
    /**
     * 导出数据到Excel
     */
    public String exportToExcel(List<CrawlerData> dataList, String fileName) {
        try {
            // 确保目录存在
            new File(excelExportPath).mkdirs();
            
            String filePath = excelExportPath + fileName + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            EasyExcel.write(filePath, CrawlerData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("爬虫数据")
                .doWrite(dataList);
            
            log.info("数据已导出到Excel: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            return null;
        }
    }
    
    /**
     * 导出所有数据到Excel
     */
    public String exportAllDataToExcel() {
        List<CrawlerData> allData = crawlerDataService.list();
        return exportToExcel(allData, "all_crawler_data");
    }
    
    /**
     * 根据数据源导出数据到Excel
     */
    public String exportDataBySourceToExcel(String sourceName) {
        List<CrawlerData> dataList = crawlerDataService.findBySourceName(sourceName);
        return exportToExcel(dataList, "crawler_data_" + sourceName);
    }
    
    /**
     * 根据状态导出数据到Excel
     */
    public String exportDataByStatusToExcel(CrawlerData.DataStatus status) {
        // 简化版本：暂时返回所有数据，实际实现需要根据状态过滤
        List<CrawlerData> dataList = crawlerDataService.list();
        return exportToExcel(dataList, "crawler_data_status_" + status.name());
    }
    
    /**
     * 生成仪表板数据
     */
    public Map<String, Object> generateDashboardData() {
        Map<String, Object> dashboardData = new java.util.HashMap<>();
        
        // 基础统计信息
        Map<String, Object> statistics = crawlerDataService.getStatistics();
        dashboardData.put("statistics", statistics);
        
        // 图表文件路径
        dashboardData.put("sourcePieChart", generateSourcePieChart());
        dashboardData.put("statusBarChart", generateStatusBarChart());
        dashboardData.put("trendChart", generateTrendChart(30));
        
        // 最新数据
        List<CrawlerData> latestData = crawlerDataService.findLatestData(10);
        dashboardData.put("latestData", latestData);
        
        // 各数据源最新数据
        Map<String, List<CrawlerData>> latestBySource = new java.util.HashMap<>();
        List<Map<String, Object>> sourceStats = crawlerDataService.countBySourceName();
        for (Map<String, Object> stat : sourceStats) {
            String sourceName = (String) stat.get("source_name");
            List<CrawlerData> sourceData = crawlerDataService.findLatestDataBySource(sourceName, 5);
            latestBySource.put(sourceName, sourceData);
        }
        dashboardData.put("latestBySource", latestBySource);
        
        return dashboardData;
    }
}

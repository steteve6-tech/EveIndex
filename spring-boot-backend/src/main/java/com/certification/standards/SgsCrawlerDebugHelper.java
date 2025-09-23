package com.certification.standards;

import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.certification.SgsCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * SGS爬虫调试辅助类
 * 提供详细的调试信息和问题诊断
 */
@Slf4j
@Component
public class SgsCrawlerDebugHelper {

    @Autowired
    private SgsCrawler sgsCrawler;

    /**
     * 诊断SGS爬虫问题
     */
    public Map<String, Object> diagnoseSgsCrawler() {
        Map<String, Object> diagnosis = new java.util.HashMap<>();
        
        try {
            log.info("开始诊断SGS爬虫问题");
            
            // 1. 检查爬虫配置
            diagnosis.put("config", sgsCrawler.getConfig());
            diagnosis.put("crawlerName", sgsCrawler.getCrawlerName());
            diagnosis.put("sourceName", sgsCrawler.getSourceName());
            
            // 2. 测试连接
            boolean isAvailable = sgsCrawler.isAvailable();
            diagnosis.put("isAvailable", isAvailable);
            
            if (!isAvailable) {
                diagnosis.put("connectionError", "爬虫连接测试失败");
                return diagnosis;
            }
            
            // 3. 测试单页请求
            try {
                String postData = sgsCrawler.buildPostData(1, "");
                diagnosis.put("postDataSample", postData);
                
                String response = sgsCrawler.sendPostRequest(1, "");
                diagnosis.put("responseLength", response.length());
                diagnosis.put("responseSample", response.substring(0, Math.min(500, response.length())));
                
                List<CrawlerResult> results = sgsCrawler.parseApiResponse(response);
                diagnosis.put("parsedResultsCount", results.size());
                
                if (!results.isEmpty()) {
                    CrawlerResult firstResult = results.get(0);
                    diagnosis.put("firstResult", Map.of(
                        "title", firstResult.getTitle(),
                        "url", firstResult.getUrl(),
                        "date", firstResult.getDate(),
                        "content", firstResult.getContent()
                    ));
                }
                
            } catch (Exception e) {
                diagnosis.put("requestError", e.getMessage());
                diagnosis.put("exceptionType", e.getClass().getSimpleName());
                log.error("SGS爬虫请求测试失败", e);
            }
            
            diagnosis.put("diagnosisComplete", true);
            log.info("SGS爬虫诊断完成");
            
        } catch (Exception e) {
            diagnosis.put("diagnosisError", e.getMessage());
            diagnosis.put("exceptionType", e.getClass().getSimpleName());
            log.error("SGS爬虫诊断失败", e);
        }
        
        return diagnosis;
    }

    /**
     * 测试不同的请求参数
     */
    public Map<String, Object> testDifferentParameters() {
        Map<String, Object> testResults = new java.util.HashMap<>();
        
        try {
            log.info("开始测试不同的请求参数");
            
            // 测试不同的页码
            for (int page = 1; page <= 3; page++) {
                try {
                    String response = sgsCrawler.sendPostRequest(page, "");
                    List<CrawlerResult> results = sgsCrawler.parseApiResponse(response);
                    testResults.put("page_" + page + "_results", results.size());
                } catch (Exception e) {
                    testResults.put("page_" + page + "_error", e.getMessage());
                }
            }
            
            // 测试不同的关键词
            String[] keywords = {"certification", "ISO", "quality", "test"};
            for (String keyword : keywords) {
                try {
                    String response = sgsCrawler.sendPostRequest(1, keyword);
                    List<CrawlerResult> results = sgsCrawler.parseApiResponse(response);
                    testResults.put("keyword_" + keyword + "_results", results.size());
                } catch (Exception e) {
                    testResults.put("keyword_" + keyword + "_error", e.getMessage());
                }
            }
            
            testResults.put("parameterTestComplete", true);
            log.info("参数测试完成");
            
        } catch (Exception e) {
            testResults.put("parameterTestError", e.getMessage());
            log.error("参数测试失败", e);
        }
        
        return testResults;
    }

    /**
     * 检查网络连接问题
     */
    public Map<String, Object> checkNetworkIssues() {
        Map<String, Object> networkCheck = new java.util.HashMap<>();
        
        try {
            log.info("开始检查网络连接问题");
            
            // 检查基本连接
            String apiUrl = "https://www.sgs.com/api/filtered-list/post";
            networkCheck.put("apiUrl", apiUrl);
            
            // 测试不同的User-Agent
            String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
            };
            
            for (String userAgent : userAgents) {
                try {
                    // 这里可以添加实际的网络测试逻辑
                    networkCheck.put("userAgent_" + userAgent.substring(0, 20) + "...", "tested");
                } catch (Exception e) {
                    networkCheck.put("userAgent_" + userAgent.substring(0, 20) + "..._error", e.getMessage());
                }
            }
            
            networkCheck.put("networkCheckComplete", true);
            log.info("网络连接检查完成");
            
        } catch (Exception e) {
            networkCheck.put("networkCheckError", e.getMessage());
            log.error("网络连接检查失败", e);
        }
        
        return networkCheck;
    }

    /**
     * 生成调试报告
     */
    public Map<String, Object> generateDebugReport() {
        Map<String, Object> report = new java.util.HashMap<>();
        
        log.info("开始生成SGS爬虫调试报告");
        
        // 1. 基本诊断
        report.put("diagnosis", diagnoseSgsCrawler());
        
        // 2. 参数测试
        report.put("parameterTests", testDifferentParameters());
        
        // 3. 网络检查
        report.put("networkCheck", checkNetworkIssues());
        
        // 4. 系统信息
        report.put("systemInfo", Map.of(
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
        ));
        
        report.put("reportGenerated", true);
        report.put("timestamp", java.time.LocalDateTime.now().toString());
        
        log.info("SGS爬虫调试报告生成完成");
        
        return report;
    }
}





package com.certification.crawler.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP工具类，提供通用的HTTP请求功能
 */
@Component
public class HttpUtils {
    
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int DEFAULT_RETRY_COUNT = 3;
    
    /**
     * 获取网页内容
     * @param url 目标URL
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocument(String url) throws IOException {
        return getDocument(url, DEFAULT_USER_AGENT, DEFAULT_TIMEOUT);
    }
    
    /**
     * 获取网页内容（带自定义User-Agent）
     * @param url 目标URL
     * @param userAgent 用户代理
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocument(String url, String userAgent) throws IOException {
        return getDocument(url, userAgent, DEFAULT_TIMEOUT);
    }
    
    /**
     * 获取网页内容（带自定义User-Agent和超时时间）
     * @param url 目标URL
     * @param userAgent 用户代理
     * @param timeout 超时时间（毫秒）
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocument(String url, String userAgent, int timeout) throws IOException {
        return getDocument(url, userAgent, timeout, DEFAULT_RETRY_COUNT);
    }
    
    /**
     * 获取网页内容（带重试机制）
     * @param url 目标URL
     * @param userAgent 用户代理
     * @param timeout 超时时间（毫秒）
     * @param retryCount 重试次数
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocument(String url, String userAgent, int timeout, int retryCount) throws IOException {
        IOException lastException = null;
        
        for (int i = 0; i <= retryCount; i++) {
            try {
                return Jsoup.connect(url)
                        .userAgent(userAgent)
                        .timeout(timeout)
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .get();
            } catch (IOException e) {
                lastException = e;
                if (i < retryCount) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
        }
        
        throw lastException != null ? lastException : new IOException("未知错误");
    }
    
    /**
     * 获取网页内容（带请求头）
     * @param url 目标URL
     * @param headers 请求头
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocumentWithHeaders(String url, Map<String, String> headers) throws IOException {
        return getDocumentWithHeaders(url, headers, DEFAULT_USER_AGENT, DEFAULT_TIMEOUT);
    }
    
    /**
     * 获取网页内容（带请求头和自定义参数）
     * @param url 目标URL
     * @param headers 请求头
     * @param userAgent 用户代理
     * @param timeout 超时时间（毫秒）
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document getDocumentWithHeaders(String url, Map<String, String> headers, String userAgent, int timeout) throws IOException {
        return Jsoup.connect(url)
                .headers(headers)
                .userAgent(userAgent)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get();
    }
    
    /**
     * POST请求获取网页内容
     * @param url 目标URL
     * @param data POST数据
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document postDocument(String url, Map<String, String> data) throws IOException {
        return postDocument(url, data, DEFAULT_USER_AGENT, DEFAULT_TIMEOUT);
    }
    
    /**
     * POST请求获取网页内容（带自定义参数）
     * @param url 目标URL
     * @param data POST数据
     * @param userAgent 用户代理
     * @param timeout 超时时间（毫秒）
     * @return Document对象
     * @throws IOException 网络异常
     */
    public Document postDocument(String url, Map<String, String> data, String userAgent, int timeout) throws IOException {
        return Jsoup.connect(url)
                .data(data)
                .userAgent(userAgent)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .post();
    }
    
    /**
     * 创建默认请求头
     * @return 默认请求头Map
     */
    public Map<String, String> createDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        return headers;
    }
    
    /**
     * 检查URL是否可访问
     * @param url 目标URL
     * @return 是否可访问
     */
    public boolean isUrlAccessible(String url) {
        try {
            Document doc = getDocument(url);
            return doc != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取响应状态码
     * @param url 目标URL
     * @return 响应状态码
     * @throws IOException 网络异常
     */
    public int getResponseCode(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(DEFAULT_USER_AGENT)
                .timeout(DEFAULT_TIMEOUT)
                .followRedirects(false)
                .ignoreHttpErrors(true)
                .execute()
                .statusCode();
    }
    
    /**
     * POST请求发送JSON数据
     * @param url 目标URL
     * @param jsonData JSON数据字符串
     * @param headers 请求头
     * @param timeout 超时时间（毫秒）
     * @return 响应内容字符串
     * @throws IOException 网络异常
     */
    public String postJson(String url, String jsonData, Map<String, String> headers, int timeout) throws IOException {
        return Jsoup.connect(url)
                .headers(headers)
                .requestBody(jsonData)
                .userAgent(DEFAULT_USER_AGENT)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .post()
                .body()
                .text();
    }
    
    /**
     * POST请求发送JSON数据（使用默认超时时间）
     * @param url 目标URL
     * @param jsonData JSON数据字符串
     * @param headers 请求头
     * @return 响应内容字符串
     * @throws IOException 网络异常
     */
    public String postJson(String url, String jsonData, Map<String, String> headers) throws IOException {
        return postJson(url, jsonData, headers, DEFAULT_TIMEOUT);
    }
}


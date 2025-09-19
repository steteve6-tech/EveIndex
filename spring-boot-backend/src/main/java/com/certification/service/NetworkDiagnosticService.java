package com.certification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络诊断服务
 * 用于诊断网络连接问题
 */
@Slf4j
@Service
public class NetworkDiagnosticService {

    /**
     * 诊断指定URL的网络连接
     */
    public Map<String, Object> diagnoseUrl(String url) {
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            
            // 1. DNS解析测试
            Map<String, Object> dnsResult = testDnsResolution(host);
            result.put("dnsResolution", dnsResult);
            
            // 2. 连接测试
            Map<String, Object> connectionResult = testConnection(url);
            result.put("connection", connectionResult);
            
            // 3. HTTP请求测试
            Map<String, Object> httpResult = testHttpRequest(url);
            result.put("httpRequest", httpResult);
            
            // 4. 总体评估
            boolean dnsSuccess = (Boolean) dnsResult.get("success");
            boolean connectionSuccess = (Boolean) connectionResult.get("success");
            boolean httpSuccess = (Boolean) httpResult.get("success");
            
            result.put("overallSuccess", dnsSuccess && connectionSuccess && httpSuccess);
            result.put("diagnosisComplete", true);
            
        } catch (Exception e) {
            result.put("error", "诊断过程中发生错误: " + e.getMessage());
            result.put("diagnosisComplete", false);
        }
        
        return result;
    }

    /**
     * 测试DNS解析
     */
    private Map<String, Object> testDnsResolution(String host) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            InetAddress[] addresses = InetAddress.getAllByName(host);
            long endTime = System.currentTimeMillis();
            
            result.put("success", true);
            result.put("resolvedAddresses", addresses.length);
            result.put("responseTime", endTime - startTime);
            
            String[] ipAddresses = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ipAddresses[i] = addresses[i].getHostAddress();
            }
            result.put("ipAddresses", ipAddresses);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 测试TCP连接
     */
    private Map<String, Object> testConnection(String url) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : (uri.getScheme().equals("https") ? 443 : 80);
            
            long startTime = System.currentTimeMillis();
            
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 10000); // 10秒超时
                long endTime = System.currentTimeMillis();
                
                result.put("success", true);
                result.put("responseTime", endTime - startTime);
                result.put("connected", socket.isConnected());
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 测试HTTP请求
     */
    private Map<String, Object> testHttpRequest(String url) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            
            result.put("success", true);
            result.put("statusCode", response.statusCode());
            result.put("responseTime", endTime - startTime);
            result.put("contentLength", response.body().length());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 诊断SGS网站连接
     */
    public Map<String, Object> diagnoseSgsConnection() {
        return diagnoseUrl("https://www.sgs.com");
    }

    /**
     * 诊断UL网站连接
     */
    public Map<String, Object> diagnoseUlConnection() {
        return diagnoseUrl("https://www.ul.com");
    }

    /**
     * 获取网络诊断建议
     */
    public Map<String, Object> getNetworkAdvice(Map<String, Object> diagnosisResult) {
        Map<String, Object> advice = new HashMap<>();
        
        if (diagnosisResult == null || !(Boolean) diagnosisResult.getOrDefault("diagnosisComplete", false)) {
            advice.put("summary", "诊断未完成，无法提供建议");
            return advice;
        }
        
        Map<String, Object> dnsResult = (Map<String, Object>) diagnosisResult.get("dnsResolution");
        Map<String, Object> connectionResult = (Map<String, Object>) diagnosisResult.get("connection");
        Map<String, Object> httpResult = (Map<String, Object>) diagnosisResult.get("httpRequest");
        
        boolean dnsSuccess = (Boolean) dnsResult.getOrDefault("success", false);
        boolean connectionSuccess = (Boolean) connectionResult.getOrDefault("success", false);
        boolean httpSuccess = (Boolean) httpResult.getOrDefault("success", false);
        
        if (!dnsSuccess) {
            advice.put("dnsIssue", "DNS解析失败，请检查网络配置或DNS服务器设置");
        }
        
        if (!connectionSuccess) {
            advice.put("connectionIssue", "TCP连接失败，可能是防火墙阻止或网络不通");
        }
        
        if (!httpSuccess) {
            advice.put("httpIssue", "HTTP请求失败，可能是服务器问题或网络配置问题");
        }
        
        if (dnsSuccess && connectionSuccess && httpSuccess) {
            advice.put("summary", "网络连接正常");
        } else {
            advice.put("summary", "存在网络连接问题，请检查网络配置");
        }
        
        return advice;
    }
}

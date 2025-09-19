package com.certification.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CORS过滤器
 * 确保所有CORS请求都能正确处理
 */
// @Component
// @Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3100,http://127.0.0.1:3000,http://127.0.0.1:3100}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 获取请求的Origin
        String origin = request.getHeader("Origin");
        
        // 检查Origin是否在允许列表中
        List<String> allowedOriginList = Arrays.asList(allowedOrigins.split(","));
        String allowedOrigin = "*";
        if (origin != null && allowedOriginList.contains(origin)) {
            allowedOrigin = origin;
        }

        // 设置CORS响应头
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        response.setHeader("Access-Control-Max-Age", String.valueOf(maxAge));
        response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        response.setHeader("Access-Control-Expose-Headers", 
            "Access-Control-Allow-Origin, Access-Control-Allow-Methods, Access-Control-Allow-Headers, " +
            "Access-Control-Max-Age, Access-Control-Request-Headers, Access-Control-Request-Method, " +
            "Content-Type, Authorization, X-Requested-With, Accept, Origin, Cache-Control, Pragma, Expires");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // 初始化方法
    }

    @Override
    public void destroy() {
        // 销毁方法
    }
}

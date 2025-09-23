package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.util.KeywordUtil;

/**
 * 医疗文档实体类
 * 对应数据库表：t_guidance_document
 * 支持多种数据源：FDA指导文档、EU医疗设备新闻等
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_guidance_document")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "医疗文档实体")
public class GuidanceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 文档类型（GUIDANCE-指导文档, NEWS-新闻, NOTICE-通知等）
    @Column(name = "document_type", length = 20)
    private String documentType;

    // 文档标题
    @Column(name = "title", length = 500, nullable = false)
    private String title;

    // 发布日期
    @Column(name = "publication_date")
    private LocalDate publicationDate;

    // 话题/主题
    @Column(name = "topic", length = 255)
    private String topic;

    // 指导状态（Final, Draft, Withdrawn等）
    @Column(name = "guidance_status", length = 50)
    private String guidanceStatus;

    // 新增：风险等级
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    // 新增：关键词数组
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // JSON格式存储关键词数组

    // 指导文档URL
    @Column(name = "document_url", length = 1000)
    private String documentUrl;

    // 数据来源URL
    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    // 新增：数据来源（如 FDA、CBP 等）
    @Column(name = "data_source", length = 50)
    private String dataSource;

    // 新增：来源国家（如 US、EU 等）
    @Column(name = "jd_country", length = 10)
    private String jdCountry;

    // 爬取时间
    @Column(name = "crawl_time")
    private LocalDateTime crawlTime;

    // 数据状态（ACTIVE, INACTIVE等）
    @Column(name = "data_status", length = 20)
    private String dataStatus = "ACTIVE";

    // 创建时间
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // ========== EU 新闻特有字段 ==========
    
    /**
     * 新闻类型（EU特有）
     */
    @Column(name = "news_type", length = 100)
    private String newsType;
    
    /**
     * 文章描述（EU特有）
     */
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 阅读时间（EU特有）
     */
    @Column(name = "read_time", length = 50)
    private String readTime;
    
    /**
     * 图片URL（EU特有）
     */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;
    
    /**
     * 图片alt文本（EU特有）
     */
    @Column(name = "image_alt", length = 500)
    private String imageAlt;
    
    /**
     * 文章序号（EU特有）
     */
    @Column(name = "article_index")
    private Integer articleIndex;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (crawlTime == null) {
            crawlTime = LocalDateTime.now();
        }
        // 默认来源与国家
        if (dataSource == null || dataSource.isBlank()) {
            dataSource = "FDA";
        }
        if (jdCountry == null || jdCountry.isBlank()) {
            jdCountry = "US";
        }
        // 默认文档类型
        if (documentType == null || documentType.isBlank()) {
            documentType = "GUIDANCE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // ========== 数据映射方法 ==========
    
    /**
     * 从EU新闻数据创建GuidanceDocument实体
     */
    public static GuidanceDocument fromEuNews(Map<String, String> euNewsData) {
        if (euNewsData == null || euNewsData.isEmpty()) {
            return null;
        }

        GuidanceDocument entity = new GuidanceDocument();

        // 设置文档类型
        entity.setDocumentType("NEWS");

        // 核心字段映射
        entity.setTitle(getStringValue(euNewsData, "title"));
        entity.setPublicationDate(parseDate(getStringValue(euNewsData, "publish_date")));
        entity.setDocumentUrl(getStringValue(euNewsData, "detail_url"));
        entity.setSourceUrl("https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en");
        entity.setDataSource("EU");
        entity.setJdCountry("EU");

        // EU新闻特有字段
        entity.setNewsType(getStringValue(euNewsData, "news_type"));
        entity.setDescription(getStringValue(euNewsData, "description"));
        entity.setReadTime(getStringValue(euNewsData, "read_time"));
        entity.setImageUrl(getStringValue(euNewsData, "image_url"));
        entity.setImageAlt(getStringValue(euNewsData, "image_alt"));
        entity.setArticleIndex(parseInteger(getStringValue(euNewsData, "article_index")));

        // 计算风险等级
        entity.setRiskLevel(calculateRiskLevelFromEuNews(euNewsData));

        // 提取关键词
        entity.setKeywords(extractKeywordsFromEuNews(euNewsData));

        return entity;
    }

    /**
     * 从FDA指导文档数据创建GuidanceDocument实体
     */
    public static GuidanceDocument fromFdaGuidance(
            String title, 
            LocalDate publicationDate, 
            String topic, 
            String guidanceStatus, 
            String documentUrl) {
        
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        GuidanceDocument entity = new GuidanceDocument();

        // 设置文档类型
        entity.setDocumentType("GUIDANCE");

        // 核心字段映射
        entity.setTitle(title.trim());
        entity.setPublicationDate(publicationDate);
        entity.setTopic(topic);
        entity.setGuidanceStatus(guidanceStatus);
        entity.setDocumentUrl(documentUrl);
        entity.setSourceUrl("https://www.fda.gov/medical-devices/device-advice-comprehensive-regulatory-assistance/guidance-documents-medical-devices-and-radiation-emitting-products");
        entity.setDataSource("FDA");
        entity.setJdCountry("US");

        // 计算风险等级
        entity.setRiskLevel(calculateRiskLevelFromFdaGuidance(topic, guidanceStatus));

        // 提取关键词
        entity.setKeywords(extractKeywordsFromFdaGuidance(title, topic));

        return entity;
    }

    /**
     * 从EU新闻数据计算风险等级
     */
    private static RiskLevel calculateRiskLevelFromEuNews(Map<String, String> euNewsData) {
        String newsType = getStringValue(euNewsData, "news_type");
        String title = getStringValue(euNewsData, "title");
        String description = getStringValue(euNewsData, "description");
        
        // 根据新闻类型判断风险等级
        if (newsType != null) {
            String typeLower = newsType.toLowerCase();
            if (typeLower.contains("alert") || typeLower.contains("warning") || typeLower.contains("recall")) {
                return RiskLevel.HIGH;
            } else if (typeLower.contains("update") || typeLower.contains("notice")) {
                return RiskLevel.MEDIUM;
            } else if (typeLower.contains("news") || typeLower.contains("announcement")) {
                return RiskLevel.LOW;
            }
        }
        
        // 根据标题和描述内容判断风险等级
        String content = (title + " " + description).toLowerCase();
        if (content.contains("serious") || content.contains("severe") || content.contains("death") || 
            content.contains("recall") || content.contains("withdrawal")) {
            return RiskLevel.HIGH;
        } else if (content.contains("safety") || content.contains("risk") || content.contains("adverse")) {
            return RiskLevel.MEDIUM;
        }
        
        return RiskLevel.LOW; // 默认低风险
    }

    /**
     * 从FDA指导文档数据计算风险等级
     */
    private static RiskLevel calculateRiskLevelFromFdaGuidance(String topic, String guidanceStatus) {
        // 根据指导状态判断风险等级
        if (guidanceStatus != null) {
            String statusLower = guidanceStatus.toLowerCase();
            if (statusLower.contains("withdrawn") || statusLower.contains("superseded")) {
                return RiskLevel.HIGH;
            } else if (statusLower.contains("draft")) {
                return RiskLevel.MEDIUM;
            } else if (statusLower.contains("final")) {
                return RiskLevel.LOW;
            }
        }
        
        // 根据话题判断风险等级
        if (topic != null) {
            String topicLower = topic.toLowerCase();
            if (topicLower.contains("safety") || topicLower.contains("adverse") || 
                topicLower.contains("recall") || topicLower.contains("risk")) {
                return RiskLevel.HIGH;
            } else if (topicLower.contains("clinical") || topicLower.contains("testing")) {
                return RiskLevel.MEDIUM;
            }
        }
        
        return RiskLevel.MEDIUM; // 默认中等风险
    }

    /**
     * 从EU新闻数据提取关键词
     */
    private static String extractKeywordsFromEuNews(Map<String, String> euNewsData) {
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();
        
        // 从标题提取关键词
        String title = getStringValue(euNewsData, "title");
        if (title != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(title, predefinedKeywords));
        }
        
        // 从描述提取关键词
        String description = getStringValue(euNewsData, "description");
        if (description != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(description, predefinedKeywords));
        }
        
        // 从新闻类型提取关键词
        String newsType = getStringValue(euNewsData, "news_type");
        if (newsType != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(newsType, predefinedKeywords));
        }
        
        // 去重并转换为JSON存储
        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
        return KeywordUtil.keywordsToJson(uniqueKeywords);
    }

    /**
     * 从FDA指导文档数据提取关键词
     */
    private static String extractKeywordsFromFdaGuidance(String title, String topic) {
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();
        
        // 从标题提取关键词
        if (title != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(title, predefinedKeywords));
        }
        
        // 从话题提取关键词
        if (topic != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(topic, predefinedKeywords));
        }
        
        // 去重并转换为JSON存储
        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
        return KeywordUtil.keywordsToJson(uniqueKeywords);
    }

    /**
     * 获取预定义关键词列表
     */
    private static List<String> getPredefinedKeywords() {
        return Arrays.asList(
            "Skin", "Analyzer", "3D", "AI", "AIMYSKIN", "Facial", "Detector", "Scanner",
            "Care", "Portable", "Spectral", "Spectra", "Skin Analysis", "Skin Scanner",
            "3D skin imaging system", "Facial Imaging", "Skin pigmentation analysis system",
            "skin elasticity analysis", "monitor", "imaging", "medical device", "FDA", "EU",
            "guidance", "document", "news", "update", "safety", "alert", "recall", "warning",
            "hazard", "risk", "clinical", "testing", "premarket", "postmarket", "biologics",
            "drugs", "digital health", "radiation", "emitting", "products", "regulatory",
            "compliance", "approval", "clearance", "510k", "PMA", "HDE", "de novo"
        );
    }

    /**
     * 工具方法：安全获取字符串值
     */
    private static String getStringValue(Map<String, String> map, String key) {
        if (map == null || key == null) return null;
        String value = map.get(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    /**
     * 工具方法：解析日期
     */
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd'T'HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignore) {}
        }
        return null;
    }

    /**
     * 工具方法：解析整数
     */
    private static Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

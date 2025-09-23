package com.certification.util;

import com.certification.crawler.countrydata.us.US_event_api;
import com.certification.entity.common.DeviceEventReport;
import com.certification.entity.common.CertNewsData.RiskLevel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 设备事件数据映射工具类
 * 用于将不同数据源的事件数据映射到统一的DeviceEventReport实体
 */
public class DeviceEventMapper {

    /**
     * 将EU Safety Gate数据映射到DeviceEventReport实体
     */
    public static DeviceEventReport mapEuEventToEntity(Map<String, String> euEventData) {
        if (euEventData == null || euEventData.isEmpty()) {
            return null;
        }

        DeviceEventReport entity = new DeviceEventReport();

        // 核心标识字段
        entity.setReportNumber(getStringValue(euEventData, "alert_number"));
        entity.setDateReceived(parseDate(getStringValue(euEventData, "publication_date")));
        entity.setDataSource("EU");
        entity.setJdCountry("EU");

        // 产品信息映射
        entity.setGenericName(getStringValue(euEventData, "product"));
        entity.setProductNameSpecific(getStringValue(euEventData, "product_name_specific"));
        entity.setProductDescription(getStringValue(euEventData, "product_description"));
        entity.setModelNumber(getStringValue(euEventData, "product_model"));
        entity.setBrandName(getStringValue(euEventData, "brand"));
        entity.setBrandsList(getStringValue(euEventData, "brands"));

        // 类别信息
        entity.setProductCategory(getStringValue(euEventData, "category"));
        entity.setProductSubcategory(getStringValue(euEventData, "subcategory"));
        entity.setDeviceClass(getStringValue(euEventData, "category")); // 使用category作为device_class

        // 风险信息
        entity.setRiskType(getStringValue(euEventData, "risk_type"));
        entity.setRiskDescription(getStringValue(euEventData, "risk"));
        entity.setRisksList(getStringValue(euEventData, "risks"));

        // 地理位置信息
        entity.setManufacturerCountry(getStringValue(euEventData, "country"));
        entity.setNotifyingCountry(getStringValue(euEventData, "notifying_country"));

        // 措施和描述
        entity.setMeasuresDescription(getStringValue(euEventData, "measures"));
        entity.setMdrTextDescription(getStringValue(euEventData, "description"));

        // URL信息
        entity.setDetailUrl(getStringValue(euEventData, "url"));
        entity.setImageUrl(getStringValue(euEventData, "image_url"));

        // 计算风险等级
        entity.setRiskLevel(calculateRiskLevelFromEuData(euEventData));

        // 提取关键词
        entity.setKeywords(extractKeywordsFromEuData(euEventData));

        return entity;
    }

    /**
     * 将FDA设备事件数据映射到DeviceEventReport实体
     */
    public static DeviceEventReport mapFdaEventToEntity(US_event_api.DeviceEvent fdaEventData) {
        if (fdaEventData == null) {
            return null;
        }

        DeviceEventReport entity = new DeviceEventReport();

        // 核心标识字段
        entity.setReportNumber(fdaEventData.getReportNumber());
        entity.setEventType(fdaEventData.getEventType());
        entity.setDateReceived(parseDate(fdaEventData.getDateReceived()));
        entity.setDateOfEvent(parseDate(fdaEventData.getDateOfEvent()));
        entity.setDateReportToFda(parseDate(fdaEventData.getDateReportToFda()));
        entity.setDataSource("FDA");
        entity.setJdCountry("US");

        // 报告类型和来源
        entity.setTypeOfReport(joinList(fdaEventData.getTypeOfReport()));
        entity.setSourceType(joinList(fdaEventData.getSourceType()));

        // FDA特有字段
        entity.setAdverseEventFlag(fdaEventData.getAdverseEventFlag());
        entity.setReportToFda(fdaEventData.getReportToFda());
        entity.setReportToManufacturer(fdaEventData.getReportToManufacturer());
        entity.setMdrReportKey(fdaEventData.getMdrReportKey());
        entity.setEventLocation(fdaEventData.getEventLocation());
        entity.setEventKey(fdaEventData.getEventKey());
        entity.setNumberDevicesInEvent(parseInteger(fdaEventData.getNumberDevicesInEvent()));
        entity.setProductProblemFlag(fdaEventData.getProductProblemFlag());
        entity.setProductProblemsList(joinList(fdaEventData.getProductProblems()));
        entity.setRemedialActionList(joinList(fdaEventData.getRemedialAction()));

        // 患者数量
        entity.setPatientCount(parseInteger(fdaEventData.getNumberPatientsInEvent()));

        // 从设备信息中提取字段
        if (fdaEventData.getDevices() != null && !fdaEventData.getDevices().isEmpty()) {
            var device = fdaEventData.getDevices().get(0); // 取第一个设备
            entity.setBrandName(device.getDeviceName());
            entity.setModelNumber(device.getModelNumber());
            entity.setGenericName(device.getGenericName());
            entity.setManufacturerName(device.getManufacturerName());
            entity.setDeviceClass(device.getDeviceClass());
        }

        // 从MDR文本中提取描述和行动
        if (fdaEventData.getMdrText() != null && !fdaEventData.getMdrText().isEmpty()) {
            StringBuilder description = new StringBuilder();
            StringBuilder action = new StringBuilder();
            
            for (Map<String, Object> textItem : fdaEventData.getMdrText()) {
                if (textItem.containsKey("text_type_code") && textItem.containsKey("text")) {
                    String textType = textItem.get("text_type_code").toString();
                    String text = textItem.get("text").toString();
                    
                    if ("NARRATIVE_1".equals(textType) || "DESCRIPTION".equals(textType)) {
                        if (description.length() > 0) description.append(" ");
                        description.append(text);
                    } else if ("ACTION".equals(textType) || "REMEDIAL_ACTION".equals(textType)) {
                        if (action.length() > 0) action.append(" ");
                        action.append(text);
                    }
                }
            }
            
            entity.setMdrTextDescription(description.length() > 0 ? description.toString() : null);
            entity.setMdrTextAction(action.length() > 0 ? action.toString() : null);
        }

        // 计算风险等级
        entity.setRiskLevel(RiskLevelUtil.calculateRiskLevelByEventType(fdaEventData.getEventType()));

        // 提取关键词
        entity.setKeywords(extractKeywordsFromFdaData(fdaEventData));

        return entity;
    }

    /**
     * 从EU数据计算风险等级
     */
    private static RiskLevel calculateRiskLevelFromEuData(Map<String, String> euEventData) {
        String riskType = getStringValue(euEventData, "risk_type");
        String risk = getStringValue(euEventData, "risk");
        
        if (riskType != null && !riskType.isEmpty()) {
            String riskLower = riskType.toLowerCase();
            if (riskLower.contains("serious") || riskLower.contains("severe") || riskLower.contains("death")) {
                return RiskLevel.HIGH;
            } else if (riskLower.contains("moderate") || riskLower.contains("medium")) {
                return RiskLevel.MEDIUM;
            } else if (riskLower.contains("minor") || riskLower.contains("low")) {
                return RiskLevel.LOW;
            }
        }
        
        if (risk != null && !risk.isEmpty()) {
            String riskLower = risk.toLowerCase();
            if (riskLower.contains("serious") || riskLower.contains("severe") || riskLower.contains("death")) {
                return RiskLevel.HIGH;
            } else if (riskLower.contains("moderate") || riskLower.contains("medium")) {
                return RiskLevel.MEDIUM;
            } else if (riskLower.contains("minor") || riskLower.contains("low")) {
                return RiskLevel.LOW;
            }
        }
        
        return RiskLevel.MEDIUM; // 默认中等风险
    }

    /**
     * 从EU数据提取关键词
     */
    private static String extractKeywordsFromEuData(Map<String, String> euEventData) {
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();
        
        // 从产品名称提取关键词
        String product = getStringValue(euEventData, "product");
        if (product != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(product, predefinedKeywords));
        }
        
        // 从产品具体名称提取关键词
        String productSpecific = getStringValue(euEventData, "product_name_specific");
        if (productSpecific != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(productSpecific, predefinedKeywords));
        }
        
        // 从品牌提取关键词
        String brand = getStringValue(euEventData, "brand");
        if (brand != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(brand, predefinedKeywords));
        }
        
        // 从风险描述提取关键词
        String risk = getStringValue(euEventData, "risk");
        if (risk != null) {
            extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(risk, predefinedKeywords));
        }
        
        // 去重并转换为JSON存储
        List<String> uniqueKeywords = KeywordUtil.filterValidKeywords(extractedKeywords);
        return KeywordUtil.keywordsToJson(uniqueKeywords);
    }

    /**
     * 从FDA数据提取关键词
     */
    private static String extractKeywordsFromFdaData(US_event_api.DeviceEvent fdaEventData) {
        List<String> predefinedKeywords = getPredefinedKeywords();
        List<String> extractedKeywords = new ArrayList<>();
        
        // 从设备信息提取关键词
        if (fdaEventData.getDevices() != null && !fdaEventData.getDevices().isEmpty()) {
            for (var device : fdaEventData.getDevices()) {
                if (device.getDeviceName() != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromDeviceName(device.getDeviceName(), predefinedKeywords));
                }
                if (device.getManufacturerName() != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromCompanyName(device.getManufacturerName(), predefinedKeywords));
                }
            }
        }
        
        // 从产品问题提取关键词
        if (fdaEventData.getProductProblems() != null) {
            for (String problem : fdaEventData.getProductProblems()) {
                if (problem != null) {
                    extractedKeywords.addAll(KeywordUtil.extractKeywordsFromText(problem, predefinedKeywords));
                }
            }
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
            "event", "adverse", "malfunction", "injury", "death", "medical specialty", "device class",
            "safety", "alert", "recall", "warning", "hazard", "risk"
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
        
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy", "dd/MM/yyyy"};
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

    /**
     * 工具方法：连接列表为字符串
     */
    private static String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list.stream().filter(Objects::nonNull).toArray(String[]::new));
    }
}

package com.certification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期格式统一服务
 * 用于将SGS和UL爬虫的不同日期格式统一转换为标准格式
 */
@Slf4j
@Service
public class DateFormatService {

    // 标准日期格式
    private static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    private static final String STANDARD_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // 各种日期格式的正则表达式
    private static final Pattern CHINESE_DATE_PATTERN = Pattern.compile("(\\d{4})[\\s　]*年[\\s　]*(\\d{1,2})[\\s　]*月[\\s　]*(\\d{1,2})[\\s　]*日");
    private static final Pattern CHINESE_DATE_WITH_PREFIX = Pattern.compile("發布日期：[\\s　]*(\\d{4})[\\s　]*年[\\s　]*(\\d{1,2})[\\s　]*月[\\s　]*(\\d{1,2})[\\s　]*日");
    private static final Pattern ENGLISH_DATE_PATTERN = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
    private static final Pattern SLASH_DATE_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
    private static final Pattern MONTH_NAME_PATTERN = Pattern.compile("(\\w+)\\s+(\\d{1,2}),?\\s+(\\d{4})");
    
    // 月份名称映射
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    /**
     * 统一日期格式
     * @param dateString 原始日期字符串
     * @return 标准格式的日期字符串 (yyyy-MM-dd)
     */
    public String standardizeDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmedDate = dateString.trim();
        
        try {
            // 1. 尝试解析中文日期格式 (2024年12月31日)
            Matcher chineseMatcher = CHINESE_DATE_PATTERN.matcher(trimmedDate);
            if (chineseMatcher.find()) {
                String year = chineseMatcher.group(1);
                String month = String.format("%02d", Integer.parseInt(chineseMatcher.group(2)));
                String day = String.format("%02d", Integer.parseInt(chineseMatcher.group(3)));
                return year + "-" + month + "-" + day;
            }
            
            // 2. 尝试解析带前缀的中文日期格式 (發布日期：2024年12月31日)
            Matcher chinesePrefixMatcher = CHINESE_DATE_WITH_PREFIX.matcher(trimmedDate);
            if (chinesePrefixMatcher.find()) {
                String year = chinesePrefixMatcher.group(1);
                String month = String.format("%02d", Integer.parseInt(chinesePrefixMatcher.group(2)));
                String day = String.format("%02d", Integer.parseInt(chinesePrefixMatcher.group(3)));
                return year + "-" + month + "-" + day;
            }
            
            // 3. 尝试解析英文日期格式 (2024-12-31)
            Matcher englishMatcher = ENGLISH_DATE_PATTERN.matcher(trimmedDate);
            if (englishMatcher.find()) {
                String year = englishMatcher.group(1);
                String month = String.format("%02d", Integer.parseInt(englishMatcher.group(2)));
                String day = String.format("%02d", Integer.parseInt(englishMatcher.group(3)));
                return year + "-" + month + "-" + day;
            }
            
            // 4. 尝试解析斜杠日期格式 (12/31/2024)
            Matcher slashMatcher = SLASH_DATE_PATTERN.matcher(trimmedDate);
            if (slashMatcher.find()) {
                String month = String.format("%02d", Integer.parseInt(slashMatcher.group(1)));
                String day = String.format("%02d", Integer.parseInt(slashMatcher.group(2)));
                String year = slashMatcher.group(3);
                return year + "-" + month + "-" + day;
            }
            
            // 5. 尝试解析月份名称格式 (December 31, 2024)
            Matcher monthNameMatcher = MONTH_NAME_PATTERN.matcher(trimmedDate);
            if (monthNameMatcher.find()) {
                String monthName = monthNameMatcher.group(1);
                String day = String.format("%02d", Integer.parseInt(monthNameMatcher.group(2)));
                String year = monthNameMatcher.group(3);
                int monthIndex = getMonthIndex(monthName);
                if (monthIndex >= 0) {
                    String month = String.format("%02d", monthIndex + 1);
                    return year + "-" + month + "-" + day;
                }
            }
            
            // 6. 尝试直接解析标准格式
            LocalDate.parse(trimmedDate, DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT));
            return trimmedDate;
            
        } catch (DateTimeParseException e) {
            log.warn("无法解析日期格式: {}", dateString);
            return null;
        } catch (Exception e) {
            log.error("日期格式转换异常: {}", dateString, e);
            return null;
        }
    }

    /**
     * 统一日期时间格式
     * @param dateTimeString 原始日期时间字符串
     * @return 标准格式的日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     */
    public String standardizeDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        String trimmedDateTime = dateTimeString.trim();
        
        try {
            // 尝试解析标准日期时间格式
            LocalDateTime.parse(trimmedDateTime, DateTimeFormatter.ofPattern(STANDARD_DATETIME_FORMAT));
            return trimmedDateTime;
        } catch (DateTimeParseException e) {
            // 如果不是标准格式，尝试只解析日期部分
            String dateOnly = standardizeDate(trimmedDateTime);
            if (dateOnly != null) {
                return dateOnly + " 00:00:00";
            }
            log.warn("无法解析日期时间格式: {}", dateTimeString);
            return null;
        }
    }

    /**
     * 获取月份索引
     * @param monthName 月份名称
     * @return 月份索引 (0-11)
     */
    private int getMonthIndex(String monthName) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(monthName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 验证日期格式是否有效
     * @param dateString 日期字符串
     * @return 是否有效
     */
    public boolean isValidDate(String dateString) {
        return standardizeDate(dateString) != null;
    }

    /**
     * 获取当前日期字符串
     * @return 当前日期字符串 (yyyy-MM-dd)
     */
    public String getCurrentDateString() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT));
    }

    /**
     * 获取当前日期时间字符串
     * @return 当前日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     */
    public String getCurrentDateTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(STANDARD_DATETIME_FORMAT));
    }
}

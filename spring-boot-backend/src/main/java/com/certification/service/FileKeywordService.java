package com.certification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件关键词服务类
 * 负责处理关键词文件的读写操作
 */
@Slf4j
@Service
public class FileKeywordService {

    // 关键词文件路径 - 支持多种路径
    private static final String[] KEYWORDS_FILE_PATHS = {
        "CertNewsKeywords.txt",  // 运行环境路径
        "src/main/java/com/certification/analysis/CertNewsKeywords.txt"  // 开发环境路径
    };
    private static final String FILE_HEADER = "# 认证新闻关键词列表\n# 每行一个关键词，以#开头的行为注释\n# 生成时间: ";

    /**
     * 从文件读取关键词列表
     */
    public List<String> readKeywordsFromFile() {
        // 尝试多个路径
        for (String filePath : KEYWORDS_FILE_PATHS) {
            try {
                Path path = Paths.get(filePath);
                
                if (Files.exists(path)) {
                    List<String> keywords = Files.readAllLines(path, StandardCharsets.UTF_8)
                            .stream()
                            .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    log.info("从文件读取关键词成功，共 {} 个关键词，文件路径: {}", keywords.size(), filePath);
                    return keywords;
                }
            } catch (IOException e) {
                log.warn("尝试读取关键词文件失败: {}, 错误: {}", filePath, e.getMessage());
            }
        }
        
        // 如果所有路径都失败，记录警告
        log.warn("所有关键词文件路径都不存在: {}", Arrays.toString(KEYWORDS_FILE_PATHS));
        return new ArrayList<>();
    }

    /**
     * 将关键词列表写入文件
     */
    public boolean writeKeywordsToFile(List<String> keywords) {
        try {
            // 优先使用运行环境路径
            Path filePath = Paths.get(KEYWORDS_FILE_PATHS[0]);
            
            // 确保目录存在
            Files.createDirectories(filePath.getParent());

            // 构建文件内容
            StringBuilder content = new StringBuilder();
            content.append(FILE_HEADER).append(new Date().toString()).append("\n\n");
            
            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    content.append(keyword.trim()).append("\n");
                }
            }

            // 写入文件
            Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));
            
            log.info("写入关键词文件成功，共 {} 个关键词，文件路径: {}", keywords.size(), filePath);
            return true;

        } catch (IOException e) {
            log.error("写入关键词文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加关键词到文件
     */
    public boolean addKeywordToFile(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        List<String> keywords = readKeywordsFromFile();
        String trimmedKeyword = keyword.trim();
        
        // 检查是否已存在
        if (keywords.contains(trimmedKeyword)) {
            log.warn("关键词已存在: {}", trimmedKeyword);
            return false;
        }

        keywords.add(trimmedKeyword);
        return writeKeywordsToFile(keywords);
    }

    /**
     * 从文件删除关键词
     */
    public boolean deleteKeywordFromFile(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        List<String> keywords = readKeywordsFromFile();
        String trimmedKeyword = keyword.trim();
        
        boolean removed = keywords.remove(trimmedKeyword);
        if (!removed) {
            log.warn("关键词不存在: {}", trimmedKeyword);
            return false;
        }

        return writeKeywordsToFile(keywords);
    }

    /**
     * 更新文件中的关键词
     */
    public boolean updateKeywordInFile(String oldKeyword, String newKeyword) {
        if (oldKeyword == null || oldKeyword.trim().isEmpty() || 
            newKeyword == null || newKeyword.trim().isEmpty()) {
            return false;
        }

        List<String> keywords = readKeywordsFromFile();
        String trimmedOldKeyword = oldKeyword.trim();
        String trimmedNewKeyword = newKeyword.trim();
        
        int index = keywords.indexOf(trimmedOldKeyword);
        if (index == -1) {
            log.warn("要更新的关键词不存在: {}", trimmedOldKeyword);
            return false;
        }

        // 检查新关键词是否已存在
        if (keywords.contains(trimmedNewKeyword)) {
            log.warn("新关键词已存在: {}", trimmedNewKeyword);
            return false;
        }

        keywords.set(index, trimmedNewKeyword);
        return writeKeywordsToFile(keywords);
    }

    /**
     * 检查关键词是否存在于文件中
     */
    public boolean keywordExistsInFile(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        List<String> keywords = readKeywordsFromFile();
        return keywords.contains(keyword.trim());
    }

    /**
     * 获取文件中的关键词总数
     */
    public int getKeywordCountInFile() {
        return readKeywordsFromFile().size();
    }

    /**
     * 清空文件中的所有关键词
     */
    public boolean clearKeywordsInFile() {
        return writeKeywordsToFile(new ArrayList<>());
    }

    /**
     * 批量添加关键词到文件
     */
    public boolean addKeywordsToFile(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }

        List<String> existingKeywords = readKeywordsFromFile();
        List<String> newKeywords = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .map(String::trim)
                .filter(keyword -> !existingKeywords.contains(keyword))
                .collect(Collectors.toList());

        if (newKeywords.isEmpty()) {
            log.info("没有新的关键词需要添加");
            return true;
        }

        existingKeywords.addAll(newKeywords);
        return writeKeywordsToFile(existingKeywords);
    }

    /**
     * 批量删除关键词
     */
    public boolean deleteKeywordsFromFile(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }

        List<String> existingKeywords = readKeywordsFromFile();
        List<String> keywordsToDelete = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        boolean removed = existingKeywords.removeAll(keywordsToDelete);
        if (!removed) {
            log.info("没有找到要删除的关键词");
            return true;
        }

        return writeKeywordsToFile(existingKeywords);
    }
}

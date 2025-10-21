package com.certification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词管理控制器
 * 用于管理爬虫搜索关键词
 */
@Slf4j
@RestController
@RequestMapping("/keyword-management")
@Tag(name = "关键词管理", description = "爬虫搜索关键词管理接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
public class KeywordsForDeviceCrawlController {

    private static final String KEYWORDS_FILE_PATH = "src/main/java/com/certification/crawler/countrydata/us/searchkeywords.txt";

    /**
     * 获取所有关键词
     */
    @GetMapping("/keywords")
    @Operation(summary = "获取所有关键词", description = "获取爬虫搜索关键词列表")
    public ResponseEntity<Map<String, Object>> getAllKeywords() {
        try {
            log.info("开始获取关键词列表");
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(filePath)) {
                log.warn("关键词文件不存在: {}", KEYWORDS_FILE_PATH);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "keywords", new ArrayList<>(),
                    "message", "关键词文件不存在，已创建空列表"
                ));
            }
            
            List<String> keywords = Files.readAllLines(filePath)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
            
            log.info("成功获取关键词列表，共 {} 个关键词", keywords.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "keywords", keywords,
                "total", keywords.size(),
                "message", "获取关键词列表成功"
            ));
            
        } catch (Exception e) {
            log.error("获取关键词列表失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "获取关键词列表失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 添加关键词
     */
    @PostMapping("/keywords")
    @Operation(summary = "添加关键词", description = "添加新的搜索关键词")
    public ResponseEntity<Map<String, Object>> addKeyword(@RequestBody Map<String, String> request) {
        try {
            String keyword = request.get("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始添加关键词: {}", keyword);
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            List<String> keywords = new ArrayList<>();
            
            // 如果文件存在，先读取现有关键词
            if (Files.exists(filePath)) {
                keywords = Files.readAllLines(filePath)
                    .stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
            }
            
            // 检查关键词是否已存在
            if (keywords.contains(keyword)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词已存在: " + keyword
                ));
            }
            
            // 添加新关键词
            keywords.add(keyword);
            
            // 确保目录存在
            Files.createDirectories(filePath.getParent());
            
            // 写入文件
            Files.write(filePath, keywords);
            
            log.info("成功添加关键词: {}，当前总数: {}", keyword, keywords.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "关键词添加成功",
                "keyword", keyword,
                "total", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("添加关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "添加关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 更新关键词
     */
    @PutMapping("/keywords/{index}")
    @Operation(summary = "更新关键词", description = "根据索引更新关键词")
    public ResponseEntity<Map<String, Object>> updateKeyword(
            @PathVariable int index, 
            @RequestBody Map<String, String> request) {
        try {
            String newKeyword = request.get("keyword");
            if (newKeyword == null || newKeyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            newKeyword = newKeyword.trim();
            log.info("开始更新关键词，索引: {}, 新关键词: {}", index, newKeyword);
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词文件不存在"
                ));
            }
            
            List<String> keywords = Files.readAllLines(filePath)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
            
            if (index < 0 || index >= keywords.size()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "索引超出范围: " + index
                ));
            }
            
            String oldKeyword = keywords.get(index);
            
            // 检查新关键词是否已存在（排除当前索引）
            for (int i = 0; i < keywords.size(); i++) {
                if (i != index && keywords.get(i).equals(newKeyword)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "关键词已存在: " + newKeyword
                    ));
                }
            }
            
            // 更新关键词
            keywords.set(index, newKeyword);
            
            // 写入文件
            Files.write(filePath, keywords);
            
            log.info("成功更新关键词，索引: {}, 原关键词: {}, 新关键词: {}", index, oldKeyword, newKeyword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "关键词更新成功",
                "oldKeyword", oldKeyword,
                "newKeyword", newKeyword,
                "index", index
            ));
            
        } catch (Exception e) {
            log.error("更新关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "更新关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 删除关键词
     */
    @DeleteMapping("/keywords/{index}")
    @Operation(summary = "删除关键词", description = "根据索引删除关键词")
    public ResponseEntity<Map<String, Object>> deleteKeyword(@PathVariable int index) {
        try {
            log.info("开始删除关键词，索引: {}", index);
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词文件不存在"
                ));
            }
            
            List<String> keywords = Files.readAllLines(filePath)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
            
            if (index < 0 || index >= keywords.size()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "索引超出范围: " + index
                ));
            }
            
            String deletedKeyword = keywords.remove(index);
            
            // 写入文件
            Files.write(filePath, keywords);
            
            log.info("成功删除关键词，索引: {}, 删除的关键词: {}", index, deletedKeyword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "关键词删除成功",
                "deletedKeyword", deletedKeyword,
                "index", index,
                "total", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("删除关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "删除关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 批量更新关键词
     */
    @PutMapping("/keywords/batch")
    @Operation(summary = "批量更新关键词", description = "批量更新所有关键词")
    public ResponseEntity<Map<String, Object>> batchUpdateKeywords(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) request.get("keywords");
            
            if (keywords == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词列表不能为空"
                ));
            }
            
            // 过滤空关键词并去重
            List<String> filteredKeywords = keywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .collect(Collectors.toList());
            
            log.info("开始批量更新关键词，数量: {}", filteredKeywords.size());
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            
            // 确保目录存在
            Files.createDirectories(filePath.getParent());
            
            // 写入文件
            Files.write(filePath, filteredKeywords);
            
            log.info("成功批量更新关键词，总数: {}", filteredKeywords.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新关键词成功",
                "total", filteredKeywords.size(),
                "keywords", filteredKeywords
            ));
            
        } catch (Exception e) {
            log.error("批量更新关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "批量更新关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 清空所有关键词
     */
    @DeleteMapping("/keywords")
    @Operation(summary = "清空所有关键词", description = "清空所有搜索关键词")
    public ResponseEntity<Map<String, Object>> clearAllKeywords() {
        try {
            log.info("开始清空所有关键词");
            
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            
            // 确保目录存在
            Files.createDirectories(filePath.getParent());
            
            // 写入空文件
            Files.write(filePath, new ArrayList<>());
            
            log.info("成功清空所有关键词");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "清空所有关键词成功",
                "total", 0
            ));
            
        } catch (Exception e) {
            log.error("清空关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "清空关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查关键词管理服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            boolean fileExists = Files.exists(filePath);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "关键词管理服务正常运行",
                "fileExists", fileExists,
                "filePath", KEYWORDS_FILE_PATH,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "健康检查失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }
}
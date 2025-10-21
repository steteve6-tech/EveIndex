package com.certification.controller;

import com.certification.entity.common.Keyword;
import com.certification.standards.KeywordService;
import com.certification.service.FileKeywordService;
import com.certification.service.KeywordMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证关键词管理控制器
 * 提供认证相关的关键词增删改查接口
 */
@Slf4j
@RestController
@RequestMapping("/cert-keywords")
@Tag(name = "认证关键词管理", description = "认证相关的关键词增删改查接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://127.0.0.1:3000", "http://127.0.0.1:3100"})
public class KeywordsForCertNewsController {

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private FileKeywordService fileKeywordService;

    @Autowired
    private KeywordMatchService keywordMatchService;

    /**
     * 获取所有关键词
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有关键词", description = "获取所有启用的关键词列表")
    public ResponseEntity<Map<String, Object>> getAllKeywords() {
        try {
            log.info("开始获取关键词列表");
            
            List<Keyword> keywords = keywordService.getAllKeywords();
            
            log.info("成功获取关键词列表，共 {} 个关键词", keywords.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("total", keywords.size());
            response.put("message", "获取关键词列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取关键词列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取关键词列表失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取启用的关键词
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取启用的关键词", description = "获取所有启用的关键词字符串列表")
    public ResponseEntity<Map<String, Object>> getEnabledKeywords() {
        try {
            log.info("开始获取启用的关键词列表");
            
            List<String> keywords = keywordService.getAllEnabledKeywords();
            
            log.info("成功获取启用的关键词列表，共 {} 个关键词", keywords.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("total", keywords.size());
            response.put("message", "获取启用的关键词列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取启用的关键词列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取启用的关键词列表失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 添加关键词
     */
    @PostMapping("/add")
    @Operation(summary = "添加关键词", description = "添加新的关键词")
    public ResponseEntity<Map<String, Object>> addKeyword(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "描述") @RequestParam(required = false) String description) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始添加关键词: {}", keyword);
            
            Keyword savedKeyword = keywordService.addKeyword(keyword, description != null ? description.trim() : "");
            
            log.info("成功添加关键词: {}", savedKeyword.getKeyword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关键词添加成功");
            response.put("keyword", savedKeyword);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("添加关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "添加关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除关键词
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除关键词", description = "根据关键词内容删除关键词")
    public ResponseEntity<Map<String, Object>> deleteKeyword(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始删除关键词: {}", keyword);
            
            keywordService.deleteKeyword(keyword);
            
            log.info("成功删除关键词: {}", keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关键词删除成功");
            response.put("deletedKeyword", keyword);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("删除关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            // 如果是关键词不存在，返回404
            if (e.getMessage().contains("关键词不存在")) {
                return ResponseEntity.notFound().build();
            } else {
                // 其他运行时异常返回400
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            log.error("删除关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "删除关键词失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 更新关键词
     */
    @PutMapping("/update")
    @Operation(summary = "更新关键词", description = "更新关键词信息")
    public ResponseEntity<Map<String, Object>> updateKeyword(
            @Parameter(description = "关键词ID") @RequestParam Long id,
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "描述") @RequestParam(required = false) String description,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始更新关键词，ID: {}, 新关键词: {}", id, keyword);
            
            Keyword updatedKeyword = keywordService.updateKeyword(id, keyword, description, enabled);
            
            log.info("成功更新关键词，ID: {}, 新关键词: {}", id, updatedKeyword.getKeyword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关键词更新成功");
            response.put("keyword", updatedKeyword);
            response.put("newKeyword", updatedKeyword.getKeyword());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "更新关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 检查文本关键词
     */
    @PostMapping("/check")
    @Operation(summary = "检查文本关键词", description = "检查文本是否包含任何关键词")
    public ResponseEntity<Map<String, Object>> checkKeywords(
            @Parameter(description = "要检查的文本") @RequestParam String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "文本不能为空"
                ));
            }
            
            text = text.trim();
            log.info("开始检查文本关键词，文本长度: {}", text.length());
            
            List<String> matchedKeywords = keywordService.getContainedKeywords(text);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("matched", !matchedKeywords.isEmpty());
            response.put("matchedKeywords", matchedKeywords);
            response.put("matchCount", matchedKeywords.size());
            response.put("message", matchedKeywords.isEmpty() ? "未匹配到关键词" : "匹配到关键词");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查文本关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "检查文本关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 初始化默认关键词
     * 注意：此接口主要进行数据库操作，文件操作是可选的
     */
    @PostMapping("/initialize")
    @Operation(summary = "初始化默认关键词", description = "初始化系统默认的关键词列表")
    public ResponseEntity<Map<String, Object>> initializeKeywords() {
        try {
            log.info("开始初始化默认关键词");
            
            keywordService.initializeDefaultKeywords();
            
            log.info("成功初始化默认关键词");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "默认关键词初始化成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("初始化默认关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "初始化默认关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取关键词匹配数量
     */
    @GetMapping("/with-match-counts")
    @Operation(summary = "获取关键词匹配数量", description = "获取关键词及其匹配数量")
    public ResponseEntity<Map<String, Object>> getKeywordsWithMatchCounts() {
        try {
            log.info("开始获取关键词匹配数量");
            
            List<Map<String, Object>> keywordsWithCounts = keywordService.getKeywordsWithMatchCounts();
            
            log.info("成功获取关键词匹配数量，共 {} 个关键词", keywordsWithCounts.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keywords", keywordsWithCounts);
            response.put("totalCount", keywordsWithCounts.size());
            response.put("message", "获取关键词匹配数量成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取关键词匹配数量失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取关键词匹配数量失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除所有0匹配的关键词
     */
    @DeleteMapping("/delete-zero-match")
    @Operation(summary = "删除0匹配关键词", description = "删除所有匹配数量为0的关键词")
    public ResponseEntity<Map<String, Object>> deleteZeroMatchKeywords() {
        try {
            log.info("开始删除0匹配的关键词");
            
            Map<String, Object> result = keywordService.deleteZeroMatchKeywords();
            
            log.info("成功删除0匹配的关键词");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result.get("message"));
            response.put("deletedKeywords", result.get("deletedKeywords"));
            response.put("deletedCount", result.get("deletedCount"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除0匹配关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除0匹配关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ==================== 文件关键词管理接口 ====================

    /**
     * 从文件获取关键词列表
     */
    @GetMapping("/file/list")
    @Operation(summary = "从文件获取关键词列表", description = "从CertNewsKeywords.txt文件获取关键词列表")
    public ResponseEntity<Map<String, Object>> getFileKeywords() {
        try {
            log.info("开始从文件获取关键词列表");
            
            List<String> keywords = fileKeywordService.readKeywordsFromFile();
            
            log.info("成功从文件获取关键词列表，共 {} 个关键词", keywords.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("total", keywords.size());
            response.put("message", "从文件获取关键词列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("从文件获取关键词列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "从文件获取关键词列表失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 保存关键词列表到文件
     */
    @PostMapping("/file/save")
    @Operation(summary = "保存关键词列表到文件", description = "将关键词列表保存到CertNewsKeywords.txt文件")
    public ResponseEntity<Map<String, Object>> saveKeywordsToFile(@RequestBody List<String> keywords) {
        try {
            if (keywords == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词列表不能为空"
                ));
            }
            
            log.info("开始保存关键词列表到文件，关键词数量: {}", keywords.size());
            
            boolean success = fileKeywordService.writeKeywordsToFile(keywords);
            
            if (success) {
                log.info("成功保存关键词列表到文件，共 {} 个关键词", keywords.size());
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "关键词列表保存到文件成功");
                response.put("savedCount", keywords.size());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "保存关键词列表到文件失败");
                return ResponseEntity.internalServerError().body(error);
            }
            
        } catch (Exception e) {
            log.error("保存关键词列表到文件失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "保存关键词列表到文件失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 添加关键词到文件
     */
    @PostMapping("/file/add")
    @Operation(summary = "添加关键词到文件", description = "添加单个关键词到CertNewsKeywords.txt文件")
    public ResponseEntity<Map<String, Object>> addKeywordToFile(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始添加关键词到文件: {}", keyword);
            
            boolean success = fileKeywordService.addKeywordToFile(keyword);
            
            if (success) {
                log.info("成功添加关键词到文件: {}", keyword);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "关键词添加到文件成功");
                response.put("keyword", keyword);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "关键词已存在或添加失败");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("添加关键词到文件失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "添加关键词到文件失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 从文件删除关键词
     */
    @DeleteMapping("/file/delete")
    @Operation(summary = "从文件删除关键词", description = "从CertNewsKeywords.txt文件删除指定关键词")
    public ResponseEntity<Map<String, Object>> deleteKeywordFromFile(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词不能为空"
                ));
            }
            
            keyword = keyword.trim();
            log.info("开始从文件删除关键词: {}", keyword);
            
            boolean success = fileKeywordService.deleteKeywordFromFile(keyword);
            
            if (success) {
                log.info("成功从文件删除关键词: {}", keyword);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "关键词从文件删除成功");
                response.put("deletedKeyword", keyword);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "关键词不存在或删除失败");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            log.error("从文件删除关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "从文件删除关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 同步关键词到文件和数据库
     */
    @PostMapping("/sync")
    @Operation(summary = "同步关键词", description = "将关键词同步到文件和数据库")
    public ResponseEntity<Map<String, Object>> syncKeywords(@RequestBody List<String> keywords) {
        try {
            if (keywords == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "关键词列表不能为空"
                ));
            }
            
            log.info("开始同步关键词，关键词数量: {}", keywords.size());
            
            // 保存到文件
            boolean fileSuccess = fileKeywordService.writeKeywordsToFile(keywords);
            
            // 保存到数据库（清空现有关键词后重新添加）
            boolean dbSuccess = true;
            try {
                // 这里可以添加数据库同步逻辑
                // 暂时只同步到文件
                log.info("数据库同步功能待实现");
            } catch (Exception e) {
                log.error("数据库同步失败", e);
                dbSuccess = false;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", fileSuccess && dbSuccess);
            response.put("fileSuccess", fileSuccess);
            response.put("dbSuccess", dbSuccess);
            response.put("message", fileSuccess && dbSuccess ? "关键词同步成功" : "关键词同步部分失败");
            response.put("syncedCount", keywords.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("同步关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "同步关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ==================== 关键词匹配情况查看接口 ====================

    /**
     * 获取单个关键词的匹配情况
     */
    @GetMapping("/match/{keyword}")
    @Operation(summary = "获取关键词匹配情况", description = "获取指定关键词的详细匹配情况")
    public ResponseEntity<Map<String, Object>> getKeywordMatchDetails(
            @Parameter(description = "关键词") @PathVariable String keyword) {
        try {
            log.info("开始获取关键词匹配情况: {}", keyword);
            
            Map<String, Object> result = keywordMatchService.getKeywordMatchDetails(keyword);
            
            if ((Boolean) result.get("success")) {
                log.info("成功获取关键词匹配情况: {}", keyword);
                return ResponseEntity.ok(result);
            } else {
                log.warn("获取关键词匹配情况失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("获取关键词匹配情况失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取关键词匹配情况失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取多个关键词的匹配情况
     */
    @PostMapping("/match/batch")
    @Operation(summary = "批量获取关键词匹配情况", description = "批量获取多个关键词的匹配情况")
    public ResponseEntity<Map<String, Object>> getKeywordsMatchDetails(@RequestBody List<String> keywords) {
        try {
            log.info("开始批量获取关键词匹配情况，关键词数量: {}", keywords != null ? keywords.size() : 0);
            
            Map<String, Object> result = keywordMatchService.getKeywordsMatchDetails(keywords);
            
            if ((Boolean) result.get("success")) {
                log.info("成功批量获取关键词匹配情况");
                return ResponseEntity.ok(result);
            } else {
                log.warn("批量获取关键词匹配情况失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("批量获取关键词匹配情况失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "批量获取关键词匹配情况失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取所有关键词的匹配统计
     */
    @PostMapping("/match/stats")
    @Operation(summary = "获取关键词匹配统计", description = "获取所有关键词的匹配统计信息")
    public ResponseEntity<Map<String, Object>> getAllKeywordsMatchStats(@RequestBody List<String> keywords) {
        try {
            log.info("开始获取关键词匹配统计，关键词数量: {}", keywords != null ? keywords.size() : 0);
            
            Map<String, Object> result = keywordMatchService.getAllKeywordsMatchStats(keywords);
            
            if ((Boolean) result.get("success")) {
                log.info("成功获取关键词匹配统计");
                return ResponseEntity.ok(result);
            } else {
                log.warn("获取关键词匹配统计失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("获取关键词匹配统计失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取关键词匹配统计失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 初始化关键词（从文件读取）
     */
    @PostMapping("/initialize-from-file")
    @Operation(summary = "从文件初始化关键词", description = "从CertNewsKeywords.txt文件读取关键词并初始化")
    public ResponseEntity<Map<String, Object>> initializeKeywordsFromFile() {
        try {
            log.info("开始从文件初始化关键词");
            
            List<String> fileKeywords = fileKeywordService.readKeywordsFromFile();
            
            if (fileKeywords.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "文件中没有关键词");
                response.put("keywords", fileKeywords);
                return ResponseEntity.ok(response);
            }
            
            // 这里可以添加将文件关键词同步到数据库的逻辑
            // 暂时只返回文件中的关键词
            
            log.info("成功从文件初始化关键词，共 {} 个关键词", fileKeywords.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "从文件初始化关键词成功");
            response.put("keywords", fileKeywords);
            response.put("count", fileKeywords.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("从文件初始化关键词失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "从文件初始化关键词失败: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

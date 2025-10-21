package com.certification.controller;

import com.certification.entity.common.DeviceMatchKeywords;
import com.certification.service.DeviceMatchKeywordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备匹配关键词Controller
 * 为DeviceData.vue中的统一关键词搜索功能提供API接口
 */
@RestController
@RequestMapping("/device-match-keywords")
@Tag(name = "设备匹配关键词管理", description = "为DeviceData.vue提供统一关键词搜索功能")
public class KeywordsForDeviceMatchController {

    @Autowired
    private DeviceMatchKeywordsService deviceMatchKeywordsService;

    @GetMapping("/normal")
    @Operation(summary = "获取普通关键词列表")
    public ResponseEntity<List<DeviceMatchKeywords>> getNormalKeywords() {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.getNormalKeywords();
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/blacklist")
    @Operation(summary = "获取黑名单关键词列表")
    public ResponseEntity<List<DeviceMatchKeywords>> getBlacklistKeywords() {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.getBlacklistKeywords();
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/whitelist")
    @Operation(summary = "获取白名单关键词列表")
    public ResponseEntity<List<DeviceMatchKeywords>> getWhitelistKeywords() {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.getWhitelistKeywords();
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有关键词")
    public ResponseEntity<List<DeviceMatchKeywords>> getAllKeywords(
            @Parameter(description = "关键词类型") @RequestParam DeviceMatchKeywords.KeywordType keywordType) {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.getAllKeywords(keywordType);
        return ResponseEntity.ok(keywords);
    }

    @PostMapping("/add")
    @Operation(summary = "添加关键词")
    public ResponseEntity<?> addKeyword(@RequestBody AddKeywordRequest request) {
        try {
            DeviceMatchKeywordsService.AddKeywordResult result = deviceMatchKeywordsService.addKeyword(
                    request.getKeyword(), request.getKeywordType());

            String message = "添加关键词成功";
            if (result.isRemovedFromBlacklist()) {
                message = "添加白名单成功，已自动从黑名单中移除该关键词";
            }

            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", message,
                "keyword", result.getKeyword(),
                "removedFromBlacklist", result.isRemovedFromBlacklist()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "添加关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "更新关键词")
    public ResponseEntity<?> updateKeyword(
            @PathVariable Long id, @RequestBody UpdateKeywordRequest request) {
        try {
            DeviceMatchKeywords keyword = deviceMatchKeywordsService.updateKeyword(
                    id, request.getKeyword(), request.getEnabled());
            return ResponseEntity.ok(keyword);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "更新关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除关键词")
    public ResponseEntity<?> deleteKeyword(@PathVariable Long id) {
        try {
            deviceMatchKeywordsService.deleteKeyword(id);
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "删除关键词失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/batch-add")
    @Operation(summary = "批量添加关键词")
    public ResponseEntity<List<DeviceMatchKeywords>> batchAddKeywords(@RequestBody BatchAddKeywordsRequest request) {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.batchAddKeywords(
                request.getKeywords(), request.getKeywordType());
        return ResponseEntity.ok(keywords);
    }

    @PostMapping("/whitelist/batch-add")
    @Operation(summary = "批量添加白名单关键词")
    public ResponseEntity<?> batchAddWhitelistKeywords(@RequestBody List<String> keywords) {
        try {
            int added = deviceMatchKeywordsService.smartAddWhitelistKeywords(keywords);
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "添加成功",
                "addedCount", added
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "添加失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/toggle-type/{id}")
    @Operation(summary = "切换关键词类型")
    public ResponseEntity<DeviceMatchKeywords> toggleKeywordType(@PathVariable Long id) {
        try {
            DeviceMatchKeywords keyword = deviceMatchKeywordsService.toggleKeywordType(id);
            return ResponseEntity.ok(keyword);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/strings")
    @Operation(summary = "获取关键词字符串列表")
    public ResponseEntity<List<String>> getKeywordStrings(
            @Parameter(description = "关键词类型") @RequestParam DeviceMatchKeywords.KeywordType keywordType) {
        List<String> keywords = deviceMatchKeywordsService.getKeywordStrings(keywordType);
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取关键词")
    public ResponseEntity<DeviceMatchKeywords> getKeywordById(@PathVariable Long id) {
        return deviceMatchKeywordsService.getKeywordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "搜索关键词")
    public ResponseEntity<List<DeviceMatchKeywords>> searchKeywords(
            @Parameter(description = "搜索文本") @RequestParam String searchText) {
        List<DeviceMatchKeywords> keywords = deviceMatchKeywordsService.searchKeywords(searchText);
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/unified-config")
    @Operation(summary = "获取统一关键词配置")
    public ResponseEntity<DeviceMatchKeywordsService.UnifiedKeywordConfig> getUnifiedKeywordConfig() {
        DeviceMatchKeywordsService.UnifiedKeywordConfig config = deviceMatchKeywordsService.getUnifiedKeywordConfig();
        return ResponseEntity.ok(config);
    }

    @PostMapping("/unified-config")
    @Operation(summary = "保存统一关键词配置")
    public ResponseEntity<?> saveUnifiedKeywordConfig(@RequestBody SaveUnifiedConfigRequest request) {
        try {
            deviceMatchKeywordsService.saveUnifiedKeywordConfig(
                    request.getNormalKeywords(), request.getBlacklistKeywords());
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "保存配置成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", "保存配置失败: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }
}

/**
 * 请求DTO
 */
class AddKeywordRequest {
    private String keyword;
    private DeviceMatchKeywords.KeywordType keywordType;
    
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public DeviceMatchKeywords.KeywordType getKeywordType() { return keywordType; }
    public void setKeywordType(DeviceMatchKeywords.KeywordType keywordType) { this.keywordType = keywordType; }
}

class UpdateKeywordRequest {
    private String keyword;
    private Boolean enabled;
    
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

class BatchAddKeywordsRequest {
    private List<String> keywords;
    private DeviceMatchKeywords.KeywordType keywordType;
    
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public DeviceMatchKeywords.KeywordType getKeywordType() { return keywordType; }
    public void setKeywordType(DeviceMatchKeywords.KeywordType keywordType) { this.keywordType = keywordType; }
}

class SaveUnifiedConfigRequest {
    private List<String> normalKeywords;
    private List<String> blacklistKeywords;
    
    public List<String> getNormalKeywords() { return normalKeywords; }
    public void setNormalKeywords(List<String> normalKeywords) { this.normalKeywords = normalKeywords; }
    public List<String> getBlacklistKeywords() { return blacklistKeywords; }
    public void setBlacklistKeywords(List<String> blacklistKeywords) { this.blacklistKeywords = blacklistKeywords; }
}

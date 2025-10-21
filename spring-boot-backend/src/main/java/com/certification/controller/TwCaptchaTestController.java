package com.certification.controller;

import com.certification.crawler.countrydata.tw.TwCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 台湾FDA验证码测试控制器
 * 用于调试和测试验证码服务
 */
@Slf4j
@RestController
@RequestMapping("/api/tw/captcha")
@Tag(name = "台湾验证码测试", description = "台湾FDA验证码服务测试接口")
public class TwCaptchaTestController {

    @Autowired
    private TwCaptchaService captchaService;

    /**
     * 测试验证码服务
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    @Operation(summary = "测试验证码服务", description = "测试台湾FDA验证码获取功能，会保存验证码图片到 logs/captcha 目录")
    public ResponseEntity<Map<String, Object>> testCaptchaService() {
        log.info("收到验证码服务测试请求");

        Map<String, Object> response = new HashMap<>();

        try {
            String testResult = captchaService.testCaptchaService();

            response.put("success", true);
            response.put("message", "验证码服务测试完成");
            response.put("result", testResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("验证码服务测试失败", e);

            response.put("success", false);
            response.put("message", "测试失败: " + e.getMessage());
            response.put("error", e.toString());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 调试验证码API
     *
     * @return 调试信息
     */
    @GetMapping("/debug")
    @Operation(summary = "调试验证码API", description = "详细查看台湾FDA验证码API的响应格式，用于分析和排查问题")
    public ResponseEntity<Map<String, Object>> debugCaptchaApi() {
        log.info("收到验证码API调试请求");

        Map<String, Object> response = new HashMap<>();

        try {
            String debugInfo = captchaService.debugCaptchaApi();

            response.put("success", true);
            response.put("message", "调试信息收集完成");
            response.put("debugInfo", debugInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("验证码API调试失败", e);

            response.put("success", false);
            response.put("message", "调试失败: " + e.getMessage());
            response.put("error", e.toString());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取新验证码
     *
     * @return 验证码信息（不包含图片数据）
     */
    @GetMapping("/get")
    @Operation(summary = "获取验证码", description = "获取新的验证码（不返回图片数据，图片会保存到服务器）")
    public ResponseEntity<Map<String, Object>> getCaptcha() {
        log.info("收到获取验证码请求");

        Map<String, Object> response = new HashMap<>();

        try {
            TwCaptchaService.CaptchaInfo captcha = captchaService.getCaptcha();

            Map<String, Object> captchaInfo = new HashMap<>();
            captchaInfo.put("code", captcha.getCode());
            captchaInfo.put("verifyCode", captcha.getVerifyCode() != null ?
                    captcha.getVerifyCode().substring(0, Math.min(50, captcha.getVerifyCode().length())) + "..." :
                    null);
            captchaInfo.put("timestamp", captcha.getTimestamp());
            captchaInfo.put("expired", captcha.isExpired());
            captchaInfo.put("hasCookies", captcha.getCookies() != null && !captcha.getCookies().isEmpty());
            captchaInfo.put("hasImage", captcha.getImageData() != null && captcha.getImageData().length > 0);

            response.put("success", true);
            response.put("message", "验证码获取成功");
            response.put("captcha", captchaInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取验证码失败", e);

            response.put("success", false);
            response.put("message", "获取验证码失败: " + e.getMessage());
            response.put("error", e.toString());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 创建手动验证码
     * 用于测试时手动设置验证码
     *
     * @param code 验证码（4位数字）
     * @param verifyCode 验证码哈希（从浏览器开发者工具中获取）
     * @return 操作结果
     */
    @PostMapping("/manual")
    @Operation(summary = "手动设置验证码", description = "用于测试时手动设置验证码和哈希值")
    public ResponseEntity<Map<String, Object>> createManualCaptcha(
            @RequestParam String code,
            @RequestParam String verifyCode) {

        log.info("收到手动设置验证码请求: code={}", code);

        Map<String, Object> response = new HashMap<>();

        try {
            TwCaptchaService.CaptchaInfo captcha = captchaService.createManualCaptcha(code, verifyCode);

            response.put("success", true);
            response.put("message", "验证码设置成功");
            response.put("code", captcha.getCode());
            response.put("verifyCodeLength", captcha.getVerifyCode().length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("手动设置验证码失败", e);

            response.put("success", false);
            response.put("message", "设置失败: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取验证码统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计信息", description = "查看验证码缓存统计信息")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("收到统计信息请求");

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> stats = captchaService.getStatistics();

            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取统计信息失败", e);

            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }
}

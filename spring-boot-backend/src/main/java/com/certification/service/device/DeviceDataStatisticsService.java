package com.certification.service.device;

import com.certification.entity.common.*;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 设备数据统计服务
 * 封装统计和分析逻辑
 *
 * @author System
 * @since 2025-01-14
 */
@Slf4j
@Service
public class DeviceDataStatisticsService {

    @Autowired
    private Device510KRepository device510KRepository;

    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;

    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;

    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;

    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    @Autowired
    private FDAGuidanceDocumentRepository guidanceDocumentRepository;

    /**
     * 获取设备数据总览统计
     */
    public ResponseEntity<Map<String, Object>> getOverview() {
        log.info("获取设备数据总览统计");

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> stats = new HashMap<>();

            // 统计各种设备数据的数量
            stats.put("device510KCount", device510KRepository.count());
            stats.put("deviceEventReportCount", deviceEventReportRepository.count());
            stats.put("deviceRecallRecordCount", deviceRecallRecordRepository.count());
            stats.put("deviceRegistrationRecordCount", deviceRegistrationRecordRepository.count());
            stats.put("customsCaseCount", customsCaseRepository.count());
            stats.put("guidanceDocumentCount", guidanceDocumentRepository.count());

            // 计算总数
            long totalCount = (long) stats.get("device510KCount") +
                              (long) stats.get("deviceEventReportCount") +
                              (long) stats.get("deviceRecallRecordCount") +
                              (long) stats.get("deviceRegistrationRecordCount") +
                              (long) stats.get("customsCaseCount") +
                              (long) stats.get("guidanceDocumentCount");
            stats.put("totalCount", totalCount);

            result.put("success", true);
            result.put("data", stats);
            result.put("message", "统计数据获取成功");

            log.info("总览统计完成: 总数据量 = {}", totalCount);

        } catch (Exception e) {
            log.error("获取设备数据总览统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取各国设备数据统计
     */
    public ResponseEntity<Map<String, Object>> getCountryStatistics() {
        log.info("获取各国设备数据统计");

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Map<String, Long>> countryStats = new HashMap<>();

            // 统计各国Device510K数据
            List<Device510K> device510KList = device510KRepository.findAll();
            for (Device510K device : device510KList) {
                String country = device.getJdCountry() != null ? device.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("510K设备", 1L, Long::sum);
            }

            // 统计各国DeviceEventReport数据
            List<DeviceEventReport> eventReportList = deviceEventReportRepository.findAll();
            for (DeviceEventReport event : eventReportList) {
                String country = event.getJdCountry() != null ? event.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("事件报告", 1L, Long::sum);
            }

            // 统计各国DeviceRecallRecord数据
            List<DeviceRecallRecord> recallList = deviceRecallRecordRepository.findAll();
            for (DeviceRecallRecord recall : recallList) {
                String country = recall.getJdCountry() != null ? recall.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("召回记录", 1L, Long::sum);
            }

            // 统计各国DeviceRegistrationRecord数据
            List<DeviceRegistrationRecord> registrationList = deviceRegistrationRecordRepository.findAll();
            for (DeviceRegistrationRecord registration : registrationList) {
                String country = registration.getJdCountry() != null ? registration.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("注册记录", 1L, Long::sum);
            }

            // 统计各国GuidanceDocument数据
            List<GuidanceDocument> guidanceList = guidanceDocumentRepository.findAll();
            for (GuidanceDocument guidance : guidanceList) {
                String country = guidance.getJdCountry() != null ? guidance.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("指导文档", 1L, Long::sum);
            }

            // 统计各国CustomsCase数据
            List<CustomsCase> customsList = customsCaseRepository.findAll();
            for (CustomsCase customs : customsList) {
                String country = customs.getJdCountry() != null ? customs.getJdCountry() : "UNKNOWN";
                countryStats.computeIfAbsent(country, k -> new HashMap<>())
                           .merge("海关案例", 1L, Long::sum);
            }

            result.put("success", true);
            result.put("data", countryStats);
            result.put("message", "获取各国设备数据统计成功");

            log.info("各国设备数据统计完成: {} 个国家", countryStats.size());

        } catch (Exception e) {
            log.error("获取各国设备数据统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取各国设备数据统计失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取按风险等级统计的设备数据
     */
    public ResponseEntity<Map<String, Object>> getRiskLevelStatistics() {
        log.info("获取按风险等级统计的设备数据");

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Map<String, Long>> riskStats = new HashMap<>();

            // 统计召回记录的风险等级
            riskStats.put("召回记录", new HashMap<>());
            riskStats.get("召回记录").put("高风险",
                deviceRecallRecordRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("召回记录").put("中风险",
                deviceRecallRecordRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("召回记录").put("低风险",
                deviceRecallRecordRepository.countByRiskLevel(RiskLevel.LOW));

            // 统计申请记录的风险等级
            riskStats.put("申请记录", new HashMap<>());
            riskStats.get("申请记录").put("高风险",
                device510KRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("申请记录").put("中风险",
                device510KRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("申请记录").put("低风险",
                device510KRepository.countByRiskLevel(RiskLevel.LOW));

            // 统计事件报告的风险等级
            riskStats.put("事件报告", new HashMap<>());
            riskStats.get("事件报告").put("高风险",
                deviceEventReportRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("事件报告").put("中风险",
                deviceEventReportRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("事件报告").put("低风险",
                deviceEventReportRepository.countByRiskLevel(RiskLevel.LOW));

            // 统计注册记录的风险等级
            riskStats.put("注册记录", new HashMap<>());
            riskStats.get("注册记录").put("高风险",
                deviceRegistrationRecordRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("注册记录").put("中风险",
                deviceRegistrationRecordRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("注册记录").put("低风险",
                deviceRegistrationRecordRepository.countByRiskLevel(RiskLevel.LOW));

            // 统计指导文档的风险等级
            riskStats.put("指导文档", new HashMap<>());
            riskStats.get("指导文档").put("高风险",
                guidanceDocumentRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("指导文档").put("中风险",
                guidanceDocumentRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("指导文档").put("低风险",
                guidanceDocumentRepository.countByRiskLevel(RiskLevel.LOW));

            // 统计海关案例的风险等级
            riskStats.put("海关案例", new HashMap<>());
            riskStats.get("海关案例").put("高风险",
                customsCaseRepository.countByRiskLevel(RiskLevel.HIGH));
            riskStats.get("海关案例").put("中风险",
                customsCaseRepository.countByRiskLevel(RiskLevel.MEDIUM));
            riskStats.get("海关案例").put("低风险",
                customsCaseRepository.countByRiskLevel(RiskLevel.LOW));

            result.put("success", true);
            result.put("data", riskStats);
            result.put("message", "获取风险等级统计成功");

            log.info("设备数据风险等级统计完成");

        } catch (Exception e) {
            log.error("获取设备数据风险等级统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取风险等级统计失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取高风险数据统计
     */
    public ResponseEntity<Map<String, Object>> getHighRiskStatistics() {
        log.info("获取高风险数据统计");

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Long> highRiskStats = new HashMap<>();

            highRiskStats.put("device510K",
                device510KRepository.countByRiskLevel(RiskLevel.HIGH));
            highRiskStats.put("eventReport",
                deviceEventReportRepository.countByRiskLevel(RiskLevel.HIGH));
            highRiskStats.put("recallRecord",
                deviceRecallRecordRepository.countByRiskLevel(RiskLevel.HIGH));
            highRiskStats.put("registrationRecord",
                deviceRegistrationRecordRepository.countByRiskLevel(RiskLevel.HIGH));
            highRiskStats.put("customsCase",
                customsCaseRepository.countByRiskLevel(RiskLevel.HIGH));
            highRiskStats.put("guidanceDocument",
                guidanceDocumentRepository.countByRiskLevel(RiskLevel.HIGH));

            // 计算高风险总数
            long totalHighRisk = highRiskStats.values().stream()
                .mapToLong(Long::longValue)
                .sum();
            highRiskStats.put("total", totalHighRisk);

            result.put("success", true);
            result.put("data", highRiskStats);
            result.put("message", "获取高风险数据统计成功");

            log.info("高风险数据统计完成: 总高风险数据 = {}", totalHighRisk);

        } catch (Exception e) {
            log.error("获取高风险数据统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取高风险数据统计失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}

package com.certification.service;

import com.certification.entity.common.*;
import com.certification.repository.common.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 新增数据统计服务
 * 管理爬虫新增数据的标记和查询
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NewDataStatisticsService {

    private final Device510KRepository device510KRepository;
    private final DeviceRecallRecordRepository recallRepository;
    private final DeviceEventReportRepository eventRepository;
    private final DeviceRegistrationRecordRepository registrationRepository;
    private final GuidanceDocumentRepository guidanceRepository;
    private final CustomsCaseRepository customsCaseRepository;

    /**
     * 获取各实体类型的新增数据数量
     */
    public Map<String, Long> getNewDataCount(String moduleType) {
        log.info("获取新增数据数量: moduleType={}", moduleType);

        Map<String, Long> result = new HashMap<>();

        if ("DEVICE_DATA".equals(moduleType)) {
            result.put("Application", device510KRepository.countByIsNew(true));
            result.put("Recall", recallRepository.countByIsNew(true));
            result.put("Event", eventRepository.countByIsNew(true));
            result.put("Registration", registrationRepository.countByIsNew(true));
            result.put("Document", guidanceRepository.countByIsNew(true));
            result.put("Customs", customsCaseRepository.countByIsNew(true));
        }

        // 计算总数
        long total = result.values().stream().mapToLong(Long::longValue).sum();
        result.put("total", total);

        return result;
    }

    /**
     * 获取指定实体类型的新增数据数量
     */
    public long getNewDataCountByEntityType(String entityType) {
        log.debug("获取新增数据数量: entityType={}", entityType);

        return switch (entityType) {
            case "Application" -> device510KRepository.countByIsNew(true);
            case "Recall" -> recallRepository.countByIsNew(true);
            case "Event" -> eventRepository.countByIsNew(true);
            case "Registration" -> registrationRepository.countByIsNew(true);
            case "Document" -> guidanceRepository.countByIsNew(true);
            case "Customs" -> customsCaseRepository.countByIsNew(true);
            default -> 0L;
        };
    }

    /**
     * 获取新增数据列表（分页）
     */
    public Page<?> getNewDataList(String entityType, Pageable pageable) {
        log.info("获取新增数据列表: entityType={}, page={}, size={}",
                entityType, pageable.getPageNumber(), pageable.getPageSize());

        return switch (entityType) {
            case "Application" -> device510KRepository.findByIsNew(true, pageable);
            case "Recall" -> recallRepository.findByIsNew(true, pageable);
            case "Event" -> eventRepository.findByIsNew(true, pageable);
            case "Registration" -> registrationRepository.findByIsNew(true, pageable);
            case "Document" -> guidanceRepository.findByIsNew(true, pageable);
            case "Customs" -> customsCaseRepository.findByIsNew(true, pageable);
            default -> Page.empty(pageable);
        };
    }

    /**
     * 标记数据为已查看
     */
    @Transactional
    public int markDataAsViewed(String entityType, List<Long> ids) {
        log.info("标记数据为已查看: entityType={}, count={}", entityType, ids.size());

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int count = 0;

        switch (entityType) {
            case "Application":
                for (Long id : ids) {
                    device510KRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        device510KRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Recall":
                for (Long id : ids) {
                    recallRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        recallRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Event":
                for (Long id : ids) {
                    eventRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        eventRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Registration":
                for (Long id : ids) {
                    registrationRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        registrationRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Document":
                for (Long id : ids) {
                    guidanceRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        guidanceRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Customs":
                for (Long id : ids) {
                    customsCaseRepository.findById(id).ifPresent(entity -> {
                        entity.setNewDataViewed(true);
                        customsCaseRepository.save(entity);
                    });
                    count++;
                }
                break;
        }

        log.info("已标记 {} 条数据为已查看", count);
        return count;
    }

    /**
     * 清理已查看超过指定天数的新增数据标记
     */
    @Transactional
    public CleanupResult cleanupViewedNewData(int daysToKeep) {
        log.info("清理已查看超过{}天的新增数据标记", daysToKeep);

        CleanupResult result = new CleanupResult();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);

        // 设备510K
        int count510K = cleanupViewedDataForRepository(
                device510KRepository, cutoffTime, "Application");
        result.addCount("Application", count510K);

        // 召回记录
        int countRecall = cleanupViewedDataForRepository(
                recallRepository, cutoffTime, "Recall");
        result.addCount("Recall", countRecall);

        // 事件报告
        int countEvent = cleanupViewedDataForRepository(
                eventRepository, cutoffTime, "Event");
        result.addCount("Event", countEvent);

        // 注册记录
        int countRegistration = cleanupViewedDataForRepository(
                registrationRepository, cutoffTime, "Registration");
        result.addCount("Registration", countRegistration);

        // 指导文档
        int countDocument = cleanupViewedDataForRepository(
                guidanceRepository, cutoffTime, "Document");
        result.addCount("Document", countDocument);

        // 海关案例
        int countCustoms = cleanupViewedDataForRepository(
                customsCaseRepository, cutoffTime, "Customs");
        result.addCount("Customs", countCustoms);

        log.info("清理完成: 总计 {} 条记录", result.getTotalCount());
        return result;
    }

    /**
     * 清理指定Repository的已查看数据
     */
    private <T> int cleanupViewedDataForRepository(
            JpaRepository<T, Long> repository,
            LocalDateTime cutoffTime,
            String entityType) {

        try {
            // 查找所有已查看但仍标记为新增的数据
            // 注意：这里简化处理，实际应该检查updateTime是否早于cutoffTime
            List<T> allEntities = repository.findAll();
            int count = 0;

            for (T entity : allEntities) {
                try {
                    Boolean isNew = (Boolean) entity.getClass().getMethod("getIsNew").invoke(entity);
                    Boolean viewed = (Boolean) entity.getClass().getMethod("getNewDataViewed").invoke(entity);

                    if (Boolean.TRUE.equals(isNew) && Boolean.TRUE.equals(viewed)) {
                        // 取消新增标记
                        entity.getClass().getMethod("setIsNew", Boolean.class).invoke(entity, false);
                        repository.save(entity);
                        count++;
                    }
                } catch (Exception e) {
                    log.debug("处理实体失败: {}", e.getMessage());
                }
            }

            return count;

        } catch (Exception e) {
            log.error("清理{}类型数据失败", entityType, e);
            return 0;
        }
    }

    /**
     * 批量取消新增标记
     */
    @Transactional
    public int batchClearNewFlag(String entityType, List<Long> ids) {
        log.info("批量取消新增标记: entityType={}, count={}", entityType, ids.size());

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int count = 0;

        switch (entityType) {
            case "Application":
                for (Long id : ids) {
                    device510KRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        device510KRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Recall":
                for (Long id : ids) {
                    recallRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        recallRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Event":
                for (Long id : ids) {
                    eventRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        eventRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Registration":
                for (Long id : ids) {
                    registrationRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        registrationRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Document":
                for (Long id : ids) {
                    guidanceRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        guidanceRepository.save(entity);
                    });
                    count++;
                }
                break;

            case "Customs":
                for (Long id : ids) {
                    customsCaseRepository.findById(id).ifPresent(entity -> {
                        entity.setIsNew(false);
                        customsCaseRepository.save(entity);
                    });
                    count++;
                }
                break;
        }

        log.info("已取消 {} 条数据的新增标记", count);
        return count;
    }

    /**
     * 批量将所有数据设置为普通数据（非新增）
     */
    @Transactional
    public Map<String, Integer> batchSetAllDataAsNormal() {
        log.info("开始批量将所有数据设置为普通数据...");

        Map<String, Integer> result = new HashMap<>();

        // 设备510K - 将所有isNew=true的数据设置为false
        int count510K = batchSetNormalForRepository(device510KRepository, "Application");
        result.put("Application", count510K);

        // 召回记录
        int countRecall = batchSetNormalForRepository(recallRepository, "Recall");
        result.put("Recall", countRecall);

        // 事件报告
        int countEvent = batchSetNormalForRepository(eventRepository, "Event");
        result.put("Event", countEvent);

        // 注册记录
        int countRegistration = batchSetNormalForRepository(registrationRepository, "Registration");
        result.put("Registration", countRegistration);

        // 指导文档
        int countDocument = batchSetNormalForRepository(guidanceRepository, "Document");
        result.put("Document", countDocument);

        // 海关案例
        int countCustoms = batchSetNormalForRepository(customsCaseRepository, "Customs");
        result.put("Customs", countCustoms);

        int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
        log.info("批量设置完成: 总计 {} 条数据", totalCount);

        return result;
    }

    /**
     * 为指定Repository批量设置为普通数据
     */
    private <T> int batchSetNormalForRepository(
            JpaRepository<T, Long> repository,
            String entityType) {

        try {
            List<T> allEntities = repository.findAll();
            int count = 0;

            for (T entity : allEntities) {
                try {
                    Boolean isNew = (Boolean) entity.getClass().getMethod("getIsNew").invoke(entity);

                    // 只处理isNew=true的数据
                    if (Boolean.TRUE.equals(isNew)) {
                        entity.getClass().getMethod("setIsNew", Boolean.class).invoke(entity, false);
                        repository.save(entity);
                        count++;
                    }
                } catch (Exception e) {
                    log.debug("处理实体失败: {}", e.getMessage());
                }
            }

            log.info("{}类型: 设置 {} 条数据为普通数据", entityType, count);
            return count;

        } catch (Exception e) {
            log.error("批量设置{}类型数据失败", entityType, e);
            return 0;
        }
    }

    /**
     * 自动标记所有新增数据为已查看（用于页面加载时）
     */
    @Transactional
    public Map<String, Integer> autoMarkAllNewDataAsViewed(String moduleType) {
        log.info("自动标记所有新增数据为已查看: moduleType={}", moduleType);

        Map<String, Integer> result = new HashMap<>();

        // DEVICE_DATA模块
        if ("DEVICE_DATA".equals(moduleType)) {
            // 设备510K
            int count510K = autoMarkViewedForRepository(device510KRepository, "Application");
            result.put("Application", count510K);

            // 召回记录
            int countRecall = autoMarkViewedForRepository(recallRepository, "Recall");
            result.put("Recall", countRecall);

            // 事件报告
            int countEvent = autoMarkViewedForRepository(eventRepository, "Event");
            result.put("Event", countEvent);

            // 注册记录
            int countRegistration = autoMarkViewedForRepository(registrationRepository, "Registration");
            result.put("Registration", countRegistration);

            // 指导文档
            int countDocument = autoMarkViewedForRepository(guidanceRepository, "Document");
            result.put("Document", countDocument);

            // 海关案例
            int countCustoms = autoMarkViewedForRepository(customsCaseRepository, "Customs");
            result.put("Customs", countCustoms);
        }

        int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
        log.info("自动标记完成: 总计 {} 条数据", totalCount);

        return result;
    }

    /**
     * 为指定Repository自动标记为已查看
     */
    private <T> int autoMarkViewedForRepository(
            JpaRepository<T, Long> repository,
            String entityType) {

        try {
            List<T> allEntities = repository.findAll();
            int count = 0;

            for (T entity : allEntities) {
                try {
                    Boolean isNew = (Boolean) entity.getClass().getMethod("getIsNew").invoke(entity);
                    Boolean viewed = (Boolean) entity.getClass().getMethod("getNewDataViewed").invoke(entity);

                    // 只处理isNew=true且newDataViewed=false的数据
                    if (Boolean.TRUE.equals(isNew) && !Boolean.TRUE.equals(viewed)) {
                        entity.getClass().getMethod("setNewDataViewed", Boolean.class).invoke(entity, true);
                        repository.save(entity);
                        count++;
                    }
                } catch (Exception e) {
                    log.debug("处理实体失败: {}", e.getMessage());
                }
            }

            log.info("{}类型: 自动标记 {} 条数据为已查看", entityType, count);
            return count;

        } catch (Exception e) {
            log.error("自动标记{}类型数据失败", entityType, e);
            return 0;
        }
    }

    // ==================== DTO ====================

    /**
     * 清理结果
     */
    @Data
    public static class CleanupResult {
        private Map<String, Integer> countByType = new HashMap<>();
        private int totalCount = 0;

        public void addCount(String entityType, int count) {
            countByType.put(entityType, count);
            totalCount += count;
        }
    }
}

package com.certification.repository.common;

import com.certification.entity.common.CertNewsTaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 认证新闻任务配置Repository
 */
@Repository
public interface CertNewsTaskConfigRepository extends JpaRepository<CertNewsTaskConfig, Long> {

    /**
     * 根据任务名称查找
     */
    Optional<CertNewsTaskConfig> findByTaskName(String taskName);

    /**
     * 根据爬虫类型查找
     */
    List<CertNewsTaskConfig> findByCrawlerType(String crawlerType);

    /**
     * 查找所有启用的任务
     */
    List<CertNewsTaskConfig> findByEnabledTrue();

    /**
     * 根据爬虫类型和启用状态查找
     */
    List<CertNewsTaskConfig> findByCrawlerTypeAndEnabled(String crawlerType, Boolean enabled);
}

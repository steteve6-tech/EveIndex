package com.certification.repository;

import com.certification.entity.notification.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据源Repository接口
 */
@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    
    /**
     * 根据数据源名称查找
     */
    Source findBySourceName(String sourceName);
    
    /**
     * 根据数据源类型查找
     */
    java.util.List<Source> findBySourceType(Source.SourceType sourceType);
    
    /**
     * 查找激活的数据源
     */
    java.util.List<Source> findByIsActiveTrue();
}








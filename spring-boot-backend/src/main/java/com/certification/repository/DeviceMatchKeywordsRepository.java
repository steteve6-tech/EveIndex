package com.certification.repository;

import com.certification.entity.common.DeviceMatchKeywords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备匹配关键词Repository
 */
@Repository
public interface DeviceMatchKeywordsRepository extends JpaRepository<DeviceMatchKeywords, Long> {

    /**
     * 根据类型获取启用的关键词
     */
    List<DeviceMatchKeywords> findByKeywordTypeAndEnabledTrue(DeviceMatchKeywords.KeywordType keywordType);

    /**
     * 根据关键词内容和类型查找
     */
    Optional<DeviceMatchKeywords> findByKeywordAndKeywordType(String keyword, DeviceMatchKeywords.KeywordType keywordType);

    /**
     * 检查关键词是否存在（排除指定ID）
     */
    boolean existsByKeywordAndKeywordTypeAndIdNot(String keyword, DeviceMatchKeywords.KeywordType keywordType, Long id);

    /**
     * 根据类型获取所有关键词（包括禁用的）
     */
    List<DeviceMatchKeywords> findByKeywordType(DeviceMatchKeywords.KeywordType keywordType);

    /**
     * 根据关键词内容模糊搜索
     */
    List<DeviceMatchKeywords> findByKeywordContainingIgnoreCaseAndEnabledTrue(String keyword);

    /**
     * 根据关键词内容模糊搜索（包括禁用的）
     */
    List<DeviceMatchKeywords> findByKeywordContainingIgnoreCase(String keyword);
    
    /**
     * 根据关键词精确查找
     */
    Optional<DeviceMatchKeywords> findByKeyword(String keyword);
}

package com.certification.repository;

import com.certification.entity.common.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关键词数据访问层
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    /**
     * 根据关键词内容查找
     */
    Optional<Keyword> findByKeyword(String keyword);

    /**
     * 根据是否启用查找
     */
    List<Keyword> findByEnabledOrderBySortOrderAsc(Boolean enabled);

    /**
     * 查找所有启用的关键词
     */
    @Query("SELECT k.keyword FROM Keyword k WHERE k.enabled = true ORDER BY k.sortOrder ASC")
    List<String> findAllEnabledKeywords();

    /**
     * 查找所有启用的关键词对象
     */
    @Query("SELECT k FROM Keyword k WHERE k.enabled = true ORDER BY k.sortOrder ASC")
    List<Keyword> findAllEnabledKeywordObjects();

    /**
     * 统计启用的关键词数量
     */
    @Query("SELECT COUNT(k) FROM Keyword k WHERE k.enabled = true")
    long countByEnabledTrue();

    /**
     * 检查关键词是否存在
     */
    boolean existsByKeyword(String keyword);

    /**
     * 根据关键词内容删除
     */
    void deleteByKeyword(String keyword);
}

package com.certification.repository.ai;

import com.certification.entity.ai.AIPendingJudgment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI判断待审核Repository
 */
@Repository
public interface AIPendingJudgmentRepository extends JpaRepository<AIPendingJudgment, Long> {

    /**
     * 根据模块类型和状态查询待审核列表
     */
    List<AIPendingJudgment> findByModuleTypeAndStatusOrderByCreatedTimeDesc(
            String moduleType, String status);

    /**
     * 根据模块类型查询所有待审核记录
     */
    List<AIPendingJudgment> findByModuleTypeAndStatusOrderByCreatedTimeDesc(
            String moduleType, String status, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计模块待审核数量
     */
    @Query("SELECT COUNT(p) FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.status = 'PENDING'")
    long countPendingByModuleType(@Param("moduleType") String moduleType);

    /**
     * 统计实体类型待审核数量
     */
    @Query("SELECT COUNT(p) FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.entityType = :entityType AND p.status = 'PENDING'")
    long countPendingByModuleAndEntityType(
            @Param("moduleType") String moduleType,
            @Param("entityType") String entityType);

    /**
     * 查找过期的待审核记录
     */
    @Query("SELECT p FROM AIPendingJudgment p WHERE p.status = 'PENDING' AND p.expireTime < :now")
    List<AIPendingJudgment> findExpiredPendingJudgments(@Param("now") LocalDateTime now);

    /**
     * 根据实体ID和类型查找待审核记录
     */
    Optional<AIPendingJudgment> findByModuleTypeAndEntityTypeAndEntityIdAndStatus(
            String moduleType, String entityType, Long entityId, String status);

    /**
     * 查找指定模块的所有状态记录
     */
    List<AIPendingJudgment> findByModuleTypeOrderByCreatedTimeDesc(String moduleType);

    /**
     * 统计被黑名单过滤的数量
     */
    @Query("SELECT COUNT(p) FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.filteredByBlacklist = true AND p.status = 'PENDING'")
    long countFilteredByBlacklist(@Param("moduleType") String moduleType);

    /**
     * 查询被黑名单过滤的记录
     */
    @Query("SELECT p FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.filteredByBlacklist = true AND p.status = 'PENDING'")
    List<AIPendingJudgment> findFilteredByBlacklist(@Param("moduleType") String moduleType);

    /**
     * 查询将被设置为高风险的记录
     */
    @Query("SELECT p FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.suggestedRiskLevel = 'HIGH' AND p.status = 'PENDING'")
    List<AIPendingJudgment> findHighRiskJudgments(@Param("moduleType") String moduleType);

    /**
     * 统计将被设置为高风险的数量
     */
    @Query("SELECT COUNT(p) FROM AIPendingJudgment p WHERE p.moduleType = :moduleType AND p.suggestedRiskLevel = 'HIGH' AND p.status = 'PENDING'")
    long countHighRiskJudgments(@Param("moduleType") String moduleType);

    /**
     * 批量更新状态
     */
    @Query("UPDATE AIPendingJudgment p SET p.status = :newStatus, p.confirmedTime = :confirmedTime, p.confirmedBy = :confirmedBy WHERE p.id IN :ids")
    int batchUpdateStatus(
            @Param("ids") List<Long> ids,
            @Param("newStatus") String newStatus,
            @Param("confirmedTime") LocalDateTime confirmedTime,
            @Param("confirmedBy") String confirmedBy);

    /**
     * 删除已确认或已拒绝超过指定天数的记录
     */
    @Query("DELETE FROM AIPendingJudgment p WHERE p.status IN ('CONFIRMED', 'REJECTED', 'EXPIRED') AND p.confirmedTime < :cutoffTime")
    int deleteOldCompletedJudgments(@Param("cutoffTime") LocalDateTime cutoffTime);
}

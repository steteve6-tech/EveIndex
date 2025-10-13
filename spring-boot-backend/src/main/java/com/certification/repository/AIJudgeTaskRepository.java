package com.certification.repository;

import com.certification.entity.common.AIJudgeTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI判断任务Repository
 */
@Repository
public interface AIJudgeTaskRepository extends JpaRepository<AIJudgeTask, Long> {
    
    /**
     * 根据任务ID查询
     */
    Optional<AIJudgeTask> findByTaskId(String taskId);
    
    /**
     * 删除指定天数之前的已完成任务
     */
    void deleteByStatusAndCreateTimeBefore(String status, java.time.LocalDateTime before);
}


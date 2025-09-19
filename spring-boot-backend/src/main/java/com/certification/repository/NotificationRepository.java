package com.certification.repository;

import com.certification.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知Repository接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据通知类型查询通知
     */
    List<Notification> findByNotificationTypeAndDeletedOrderByCreatedAtDesc(Notification.NotificationType notificationType, Integer deleted);
    
    /**
     * 根据优先级查询通知
     */
    List<Notification> findByPriorityAndDeletedOrderByCreatedAtDesc(Notification.Priority priority, Integer deleted);
    
    /**
     * 查询未读通知
     */
    List<Notification> findByIsReadAndDeletedOrderByCreatedAtDesc(Boolean isRead, Integer deleted);
    
    /**
     * 查询高优先级通知
     */
    @Query("SELECT n FROM Notification n WHERE n.priority IN :priorities AND n.deleted = 0 ORDER BY n.createdAt DESC")
    List<Notification> findHighPriorityNotifications(@Param("priorities") List<Notification.Priority> priorities);
    
    /**
     * 统计各类型通知数量
     */
    @Query("SELECT n.notificationType as notificationType, COUNT(n) as count FROM Notification n WHERE n.deleted = 0 GROUP BY n.notificationType")
    List<Map<String, Object>> countByNotificationType();
    
    /**
     * 统计各优先级通知数量
     */
    @Query("SELECT n.priority as priority, COUNT(n) as count FROM Notification n WHERE n.deleted = 0 GROUP BY n.priority")
    List<Map<String, Object>> countByPriority();
    
    /**
     * 查询指定时间范围内的通知
     */
    List<Notification> findByCreatedAtBetweenAndDeletedOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    
    /**
     * 根据接收者查询通知
     */
    List<Notification> findByRecipientAndDeleted(String recipient, Integer deleted);
    

    
    /**
     * 查询最近的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.deleted = 0 ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications();
}

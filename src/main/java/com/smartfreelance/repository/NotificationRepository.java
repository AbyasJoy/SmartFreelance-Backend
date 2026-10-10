package com.smartfreelance.repository;

import com.smartfreelance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndRead(Long userId, boolean read);

    long countByUserIdAndRead(Long userId, boolean read);
}

package com.ngaleano.lol_manager.repository;

import com.ngaleano.lol_manager.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);
}

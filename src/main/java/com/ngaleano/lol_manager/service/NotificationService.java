package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.model.Notification;
import java.util.List;

public interface NotificationService {
    void sendNotification(User recipient, String message, String type, Long referenceId);

    List<Notification> getUnreadNotifications(Long userId);

    void markAsRead(Long notificationId);
}

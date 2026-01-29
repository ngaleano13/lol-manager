package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import com.ngaleano.lol_manager.model.Notification;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void sendNotification(User recipient, String message, String type, Long referenceId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}

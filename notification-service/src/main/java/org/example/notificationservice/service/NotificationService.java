package org.example.notificationservice.service;

import org.example.notificationservice.dto.UserEventDTO;

public interface NotificationService {
    void handleUserEvent(UserEventDTO event);
    void sendCustomEmail(String to, String subject, String message);
}
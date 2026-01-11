package org.example.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.UserEventDTO;
import org.example.notificationservice.service.EmailService;
import org.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Value("${website.url}")
    private String websiteUrl;

    @Value("${email.subject.created}")
    private String createdSubject;

    @Value("${email.subject.deleted}")
    private String deletedSubject;

    @Override
    public void handleUserEvent(UserEventDTO event) {
        log.info("Processing user event: {} for email: {}", event.getEventType(), event.getEmail());

        switch (event.getEventType()) {
            case USER_CREATED:
                sendUserCreatedEmail(event.getEmail(), event.getName());
                break;
            case USER_DELETED:
                sendUserDeletedEmail(event.getEmail(), event.getName());
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void sendUserCreatedEmail(String email, String name) {
        String subject = createdSubject;
        String text = String.format("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");

        emailService.sendEmail(email, subject, text);
    }

    private void sendUserDeletedEmail(String email, String name) {
        String subject = deletedSubject;
        String text = String.format(" Здравствуйте! Ваш аккаунт был удалён.");

        emailService.sendEmail(email, subject, text);
    }

    @Override
    public void sendCustomEmail(String to, String subject, String message) {
        emailService.sendEmail(to, subject, message);
    }
}
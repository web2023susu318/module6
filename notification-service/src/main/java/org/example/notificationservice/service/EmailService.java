package org.example.notificationservice.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
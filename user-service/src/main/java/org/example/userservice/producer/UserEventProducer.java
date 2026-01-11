package org.example.userservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.event.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    public void sendUserEvent(UserEvent event) {
        try {
            kafkaTemplate.send(userEventsTopic, event);
            log.info("Sent user event: {} for email: {}", event.getEventType(), event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send user event: {}", e.getMessage(), e);
        }
    }
}
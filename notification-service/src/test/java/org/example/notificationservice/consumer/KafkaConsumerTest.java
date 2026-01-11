package org.example.notificationservice.consumer;

import org.example.notificationservice.dto.UserEventDTO;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void consumeUserEvent_ShouldProcessCreatedEvent() {
        UserEventDTO event = UserEventDTO.builder()
                .eventType(UserEventDTO.EventType.USER_CREATED)
                .email("test@example.com")
                .name("Test User")
                .userId(1L)
                .build();

        kafkaConsumer.consumeUserEvent(event);

        verify(notificationService, times(1)).handleUserEvent(event);
    }

    @Test
    void consumeUserEvent_ShouldProcessDeletedEvent() {
        UserEventDTO event = UserEventDTO.builder()
                .eventType(UserEventDTO.EventType.USER_DELETED)
                .email("test@example.com")
                .name("Test User")
                .userId(1L)
                .build();

        kafkaConsumer.consumeUserEvent(event);

        verify(notificationService, times(1)).handleUserEvent(event);
    }
}
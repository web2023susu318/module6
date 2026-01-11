package org.example.notificationservice.integration;

import org.example.notificationservice.NotificationApplication;
import org.example.notificationservice.dto.UserEventDTO;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = NotificationApplication.class)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
@ActiveProfiles("test")
@DirtiesContext
class KafkaEmailIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private NotificationService notificationService;

    @Test
    void whenUserCreatedEventSent_thenEmailShouldBeSent() throws Exception {
        // Given
        UserEventDTO event = UserEventDTO.builder()
                .eventType(UserEventDTO.EventType.USER_CREATED)
                .email("integration.test@example.com")
                .name("Integration User")
                .userId(100L)
                .build();

        kafkaTemplate.send("user-events", event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, times(1)).handleUserEvent(any(UserEventDTO.class));
        });
    }

    @Test
    void whenUserDeletedEventSent_thenEmailShouldBeSent() throws Exception {
        UserEventDTO event = UserEventDTO.builder()
                .eventType(UserEventDTO.EventType.USER_DELETED)
                .email("integration.delete@example.com")
                .name("Deleted User")
                .userId(200L)
                .build();

        kafkaTemplate.send("user-events", event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, times(1)).handleUserEvent(any(UserEventDTO.class));
        });
    }
}
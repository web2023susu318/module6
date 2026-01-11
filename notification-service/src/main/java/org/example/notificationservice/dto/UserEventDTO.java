package org.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDTO {
    private EventType eventType;
    private String email;
    private Long userId;
    private String name;

    public enum EventType {
        USER_CREATED,
        USER_DELETED
    }
}
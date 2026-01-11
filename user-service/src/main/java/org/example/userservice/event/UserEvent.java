package org.example.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private EventType eventType;
    private String email;
    private Long userId;
    private String name;

    public enum EventType {
        USER_CREATED,
        USER_DELETED
    }
}
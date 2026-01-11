package org.example.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationservice.dto.EmailRequest;
import org.example.notificationservice.service.EmailService;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class EmailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;


    @Test
    void sendEmail_ShouldReturnAccepted() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .message("Test Message")
                .build();

        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        Mockito.verify(notificationService, times(1))
                .sendCustomEmail(eq("test@example.com"), eq("Test Subject"), eq("Test Message"));
    }

    @Test
    void sendEmail_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .to("invalid-email")
                .subject("")
                .message("")
                .build();

        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.to").exists())
                .andExpect(jsonPath("$.errors.subject").exists())
                .andExpect(jsonPath("$.errors.message").exists());

        verify(notificationService, never())
                .sendCustomEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendEmail_WithValidData_ShouldCallServiceWithCorrectParameters() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .to("valid@example.com")
                .subject("Valid Subject")
                .message("Valid Message")
                .build();

        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1))
                .sendCustomEmail(emailCaptor.capture(), subjectCaptor.capture(), messageCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo("valid@example.com");
        assertThat(subjectCaptor.getValue()).isEqualTo("Valid Subject");
        assertThat(messageCaptor.getValue()).isEqualTo("Valid Message");
    }
}
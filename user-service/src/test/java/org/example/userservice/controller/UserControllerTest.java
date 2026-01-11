package org.example.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.controller.hateoas.UserControllerHateoas;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserControllerHateoas userControllerHateoas;

    private UserRequest validUserRequest;
    private UserResponse userResponse;
    private EntityModel<UserResponse> userEntityModel;

    @BeforeEach
    void setUp() {
        validUserRequest = UserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .build();

        userResponse = UserResponse.builder()
                .userId(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userEntityModel = EntityModel.of(userResponse,
                Link.of("http://localhost:8080/users/1").withSelfRel(),
                Link.of("http://localhost:8080/users/1").withRel("update"),
                Link.of("http://localhost:8080/users/1").withRel("delete"),
                Link.of("http://localhost:8080/users").withRel("users")
        );
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);
        when(userControllerHateoas.toModel(any(UserResponse.class))).thenReturn(userEntityModel);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L)) // Изменено с $.userId на $.id
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists())
                .andExpect(jsonPath("$._links.users.href").exists());
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = UserRequest.builder()
                .name("")
                .email("invalid-email")
                .age(200)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getUserById_ShouldReturnUserWithHateoasLinks() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);
        when(userControllerHateoas.toModel(any(UserResponse.class))).thenReturn(userEntityModel);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)) // $.id вместо $.userId
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost:8080/users/1"))
                .andExpect(jsonPath("$._links.update.href").value("http://localhost:8080/users/1"))
                .andExpect(jsonPath("$._links.delete.href").value("http://localhost:8080/users/1"))
                .andExpect(jsonPath("$._links.users.href").value("http://localhost:8080/users"));
    }

    @Test
    void getAllUsers_ShouldReturnUserListWithHateoas() throws Exception {
        UserResponse userResponse2 = UserResponse.builder()
                .userId(2L)
                .name("Jane Doe")
                .email("jane@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EntityModel<UserResponse> userEntityModel2 = EntityModel.of(userResponse2,
                Link.of("/users/2").withSelfRel(), // Используем относительные пути
                Link.of("/users/2").withRel("update"),
                Link.of("/users/2").withRel("delete"),
                Link.of("/users").withRel("users")
        );

        EntityModel<UserResponse> userEntityModel = EntityModel.of(userResponse,
                Link.of("/users/1").withSelfRel(),
                Link.of("/users/1").withRel("update"),
                Link.of("/users/1").withRel("delete"),
                Link.of("/users").withRel("users")
        );

        List<UserResponse> users = Arrays.asList(userResponse, userResponse2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userControllerHateoas.toModel(userResponse)).thenReturn(userEntityModel);
        when(userControllerHateoas.toModel(userResponse2)).thenReturn(userEntityModel2);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users.length()").value(2))
                .andExpect(jsonPath("$._embedded.users[0].id").value(1L))
                .andExpect(jsonPath("$._embedded.users[0].name").value("John Doe"))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href").value("/users/1")) // Проверяем относительный путь
                .andExpect(jsonPath("$._embedded.users[1].id").value(2L))
                .andExpect(jsonPath("$._embedded.users[1].name").value("Jane Doe"))
                .andExpect(jsonPath("$._embedded.users[1]._links.self.href").value("/users/2")) // Проверяем относительный путь
                .andExpect(jsonPath("$._links.self.href").exists()); // Проверяем относительный путь
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserWithLinks() throws Exception {
        UserRequest updateRequest = UserRequest.builder()
                .name("John Updated")
                .email("john.updated@example.com")
                .age(31)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .userId(1L)
                .name("John Updated")
                .email("john.updated@example.com")
                .age(31)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EntityModel<UserResponse> updatedEntityModel = EntityModel.of(updatedResponse,
                Link.of("http://localhost:8080/users/1").withSelfRel(),
                Link.of("http://localhost:8080/users/1").withRel("update"),
                Link.of("http://localhost:8080/users/1").withRel("delete"),
                Link.of("http://localhost:8080/users").withRel("users")
        );

        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(updatedResponse);
        when(userControllerHateoas.toModel(any(UserResponse.class))).thenReturn(updatedEntityModel);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)) // $.id вместо $.userId
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        when(userService.getUserById(999L)).thenThrow(
                new org.example.userservice.exception.ResourceNotFoundException("User not found")
        );

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_DuplicateEmail_ShouldReturn409() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenThrow(
                new org.example.userservice.exception.DuplicateEmailException("Email already exists")
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isConflict());
    }
}
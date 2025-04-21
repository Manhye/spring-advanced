package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.jsonwebtoken.Claims;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ManagerController.class)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthUserArgumentResolver authUserArgumentResolver() {
            return new AuthUserArgumentResolver();
        }
    }

    @Autowired
    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ManagerController(managerService, jwtUtil))
                .setCustomArgumentResolvers(authUserArgumentResolver)
                .build();
    }

    @Test
    public void Given_ValidRequest_When_SaveManager_Then_ReturnsOkResponse() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 100L;
        long managerUserId = 2L;

        ManagerSaveRequest request = new ManagerSaveRequest(managerUserId);
        UserResponse userResponse = new UserResponse(managerUserId, "manager@a.com");
        ManagerSaveResponse response = new ManagerSaveResponse(managerId, userResponse);

        given(managerService.saveManager(
                ArgumentMatchers.any(AuthUser.class),
                ArgumentMatchers.eq(todoId),
                ArgumentMatchers.any(ManagerSaveRequest.class))
        ).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "test@a.com")
                        .requestAttr("userRole", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(managerId))
                .andExpect(jsonPath("$.user.id").value(managerUserId))
                .andExpect(jsonPath("$.user.email").value("manager@a.com"));
    }

    @Test
    public void Given_ValidRequest_When_GetMembers_Then_ReturnsOkResponse() throws Exception {
        // given
        long todoId = 1L;
        long managerId1 = 100L;
        long managerUserId1 = 10L;
        long managerId2 = 101L;
        long managerUserId2 = 11L;

        List<ManagerResponse> responseList = List.of(
                new ManagerResponse(managerId1, new UserResponse(managerUserId1, "manager1@example.com")),
                new ManagerResponse(managerId2, new UserResponse(managerUserId2, "manager2@example.com"))
        );

        // when
        given(managerService.getManagers(todoId)).willReturn(responseList);

        // then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(managerId1))
                .andExpect(jsonPath("$[0].user.id").value(managerUserId1))
                .andExpect(jsonPath("$[0].user.email").value("manager1@example.com"))
                .andExpect(jsonPath("$[1].id").value(managerId2))
                .andExpect(jsonPath("$[1].user.id").value(managerUserId2))
                .andExpect(jsonPath("$[1].user.email").value("manager2@example.com")); mockMvc.perform(get("/todos/{todoId}/managers", todoId));

    }

    @Test
    public void Given_ValidRequest_When_DeleteManager_Then_ReturnsOkResponse() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;
        String token = "Bearer faketoken";
        Claims mockClaims = mock(Claims.class);

        given(jwtUtil.extractClaims("faketoken")).willReturn(mockClaims);
        given(mockClaims.getSubject()).willReturn(String.valueOf(userId));

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

}
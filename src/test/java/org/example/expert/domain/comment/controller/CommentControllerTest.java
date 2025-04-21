package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.controller.ManagerController;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    @MockBean
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void Given_ValidRequest_When_SaveComment_Then_SaveComment() throws Exception {
        // given
        long todoId = 2L;
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("test contents");

        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(3L, "test contents",
                new UserResponse(authUser.getId(), authUser.getEmail()));

        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class)))
        .willReturn(commentSaveResponse);

        String json = objectMapper.writeValueAsString(commentSaveRequest);

        // when & then
        mockMvc.perform(post("/todos/" + todoId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .requestAttr("userId", authUser.getId())
                .requestAttr("email", authUser.getEmail())
                .requestAttr("userRole",authUser.getUserRole().name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentSaveResponse.getId()))
                .andExpect(jsonPath("$.contents").value(commentSaveResponse.getContents()))
                .andExpect(jsonPath("$.user.id").value(authUser.getId()))
                .andExpect(jsonPath("$.user.email").value(authUser.getEmail()));
    }

    @Test
    public void Given_ValidRequest_When_GetComments_Then_GetComments() throws Exception {
        // given
        long todoId = 1L;

        List<CommentResponse> comments = List.of(
                new CommentResponse(1L, "First comment", new UserResponse(1L, "user1@test.com")),
                new CommentResponse(2L, "Second comment", new UserResponse(2L, "user2@test.com"))
        );

        given(commentService.getComments(todoId)).willReturn(comments);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(comments.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contents").value("First comment"))
                .andExpect(jsonPath("$[0].user.id").value(1L))
                .andExpect(jsonPath("$[0].user.email").value("user1@test.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].contents").value("Second comment"))
                .andExpect(jsonPath("$[1].user.id").value(2L))
                .andExpect(jsonPath("$[1].user.email").value("user2@test.com"));

    }
}
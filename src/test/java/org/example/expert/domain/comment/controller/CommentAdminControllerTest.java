package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentAdminController.class)
class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentAdminService commentAdminService;
    @MockBean
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void Given_CommentId_When_DeleteComment_Then_ReturnOk() throws Exception {
        // given
        long commentId = 10L;
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);


        willDoNothing().given(commentAdminService).deleteComment(commentId);

        // when & then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer mockToken")
                        .requestAttr("userId", authUser.getId())
                        .requestAttr("email", authUser.getEmail())
                        .requestAttr("userRole",authUser.getUserRole().name()))
                .andExpect(status().isOk());
    }
}
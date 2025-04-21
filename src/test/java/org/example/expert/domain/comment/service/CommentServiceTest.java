package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void Given_TodoDoesNotExist_When_SaveComment_Then_InvalidRequestException() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void Given_ValidRequest_When_SaveComment_Then_ValidResponse() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);

        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    public void Given_ValidRequest_When_DeleteComment_Then_ValidResponse() {
        // given
        long todoId = 1;
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);

        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);

        long commentId1 = 1L;
        long commentId2 = 2L;
        Comment comment1 = new Comment("Test Comments1", user, todo);
        Comment comment2 = new Comment("Test Comments2", user, null);
        ReflectionTestUtils.setField(comment1, "id", commentId1);
        ReflectionTestUtils.setField(comment2, "id", commentId2);

        given(commentRepository.findByTodoIdWithUser(todoId))
                .willReturn(Arrays.asList(comment1, comment2));

        // when
        List<CommentResponse> commentResponses = commentService.getComments(todoId);

        //then
        assertThat(commentResponses).hasSize(2);
        assertThat(commentResponses.get(0).getContents()).isEqualTo("Test Comments1");
        assertThat(commentResponses.get(0).getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(commentResponses.get(1).getContents()).isEqualTo("Test Comments2");
    }

}

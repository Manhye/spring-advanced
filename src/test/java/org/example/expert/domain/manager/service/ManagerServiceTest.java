package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void Given_TodoNotExists_When_SaveManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);

        long todoId = 999L;

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(userId);

        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("Todo not found", exception.getMessage());
    }


    @Test
    public void Given_TodoExistsButUserIsNotOwner_When_SaveManager_Then_ThrowsInvalidRequestException(){
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        AuthUser ownerUser = new AuthUser(2L, "b@b.com", UserRole.USER);
        User user2 = User.fromAuthUser(ownerUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user2);
        ReflectionTestUtils.setField(todo, "id", todoId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(user.getId());

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("Not the Owner of Todo", exception.getMessage());
    }

    @Test
    void Given_ManagerNotExists_When_SaveManager_Then_ThrowsInvalidRequestException() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long invalidManagerUserId = 999L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(invalidManagerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(invalidManagerUserId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void Given_ManagerCantBeOwner_When_SaveManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(userId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("Can't assign self owner", exception.getMessage());
    }

    @Test
    void Given_ValidRequest_When_SaveManager_Then_ReturnsManagerSaveResponse(){
        // given
        long userId = 1L;
        long managerUserId = 2L;
        long todoId = 1L;

        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        User managerUser = new User("b@b.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        Todo todo = new Todo("Clean the  house", "Detailed cleaning", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", 999L);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willReturn(manager);

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(999L, response.getId());
        assertEquals(managerUserId, response.getUser().getId());
        assertEquals("b@b.com",response.getUser().getEmail());
    }

    @Test
    public void Given_TodoDoesNotExist_When_GetManagers_Then_ThrowsInvalidRequestException() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void Given_ValidRequest_When_GetManagers_Then_ReturnManagerResponseList(){
        // given
        long todoId = 1L;

        User owner = new User("owner@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);
        Todo todo = new Todo("Test Title", "Test Content", "Sunny", owner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        User managerUser = new User("manager@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", 2L);

        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", 999L);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(List.of(manager));

        // when
        List<ManagerResponse> responseList = managerService.getManagers(todoId);

        // then
        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        ManagerResponse managerResponse = responseList.get(0);
        assertEquals(999L, managerResponse.getId());
        assertEquals("manager@a.com",responseList.get(0).getUser().getEmail());

    }

    @Test
    public void Given_UserDoesNotExist_When_DeleteManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, 1L, 1L));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void Given_TodoDoesNotExist_When_DeleteManager_Then_ThrowsInvalidRequest(){
        // given
        long userId = 1L;
        User user = new User("a@a.com","password", UserRole.USER);

        long todoId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, 1L));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void Given_UserIsNotOwner_When_DeleteManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;
        User user = new User("a@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long ownerId = 2L;
        User owner = new User("owner@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", ownerId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test contents", "Sunny", owner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, 1L));
        assertEquals("Not the Owner of Todo", exception.getMessage());
    }

    @Test
    public void Given_ManagerDoesNotExist_When_DeleteManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long  managerId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId));
        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    public void Given_ManagerIsNotOwner_When_DeleteManager_Then_ThrowsInvalidRequestException() {
        // given
        long userId = 1L;
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long invalidTodoId = 2L;
        Todo invalidTodo = new Todo("Test Title", "Test contents", "Sunny", user);
        ReflectionTestUtils.setField(invalidTodo,"id",invalidTodoId);

        long invalidManagerUserId = 2L;
        User invalidManagerUser = new User("invalidManager@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(invalidManagerUser, "id", invalidManagerUserId);

        long managerUserId = 3L;
        User managerUser = new User("manager@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long  managerId = 999L;
        Manager manager = new Manager(managerUser,todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        long invalidManagerId = 1000L;
        Manager invalidManager = new Manager(invalidManagerUser, invalidTodo);
        ReflectionTestUtils.setField(invalidManager, "id", invalidManagerId);



        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(invalidManagerId)).willReturn(Optional.of(invalidManager));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, invalidManagerId));

        assertEquals("Not a Manager of Todo", exception.getMessage());
    }


    @Test
    public void Given_ValidRequest_When_DeleteManager_Then_DeleteManager() {
        // given
        long userId = 1L;
        User user = new User("a@a.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 3L;
        User managerUser = new User("manager@a.com","password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long  managerId = 999L;
        Manager manager = new Manager(managerUser,todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        assertDoesNotThrow(() -> managerService.deleteManager(userId, todoId, managerId));

        // Then
        verify(managerRepository, times(1)).delete(manager);
    }
}

package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public ManagerSaveResponse saveManager(
            AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest
    ) {

        User user = getUserFromAuth(authUser);
        Todo todo = getTodoByIdOrElseThrow(todoId);
        validateTodoOwner(todo, user);

        User managerUser = getUserByIdOrElseThrow(managerSaveRequest.getManagerUserId());
        validateNotSelfAssign(user, managerUser);

        Manager savedManagerUser = managerRepository.save(new Manager(managerUser, todo));

        return new ManagerSaveResponse(
                savedManagerUser.getId(),
                new UserResponse(managerUser.getId(), managerUser.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public List<ManagerResponse> getManagers(long todoId) {
        Todo todo = getTodoByIdOrElseThrow(todoId);

        return managerRepository.findByTodoIdWithUser(todo.getId())
                .stream()
                .map(manager -> {
                    User user = manager.getUser();
                    return new ManagerResponse(
                            manager.getId(),
                            new UserResponse(user.getId(), user.getEmail())
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteManager(long userId, long todoId, long managerId) {
        User user = getUserByIdOrElseThrow(userId);
        Todo todo = getTodoByIdOrElseThrow(todoId);
        validateTodoOwner(todo,user);

        Manager manager = getManagerByIdOrElseThrow(managerId);
        validateManagerBelongsToTodo(manager, todo);

        managerRepository.delete(manager);

    }

    private User getUserFromAuth(AuthUser authUser) {
        return User.fromAuthUser(authUser);
    }

    private User getUserByIdOrElseThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));
    }

    private Todo getTodoByIdOrElseThrow(long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
    }

    private Manager getManagerByIdOrElseThrow(long managerId) {
        return managerRepository.findById(managerId)
                .orElseThrow(() -> new InvalidRequestException("Manager not found"));
    }

    private void validateTodoOwner(Todo todo, User user) {
        if (!Objects.equals(todo.getUser().getId(), user.getId())) {
            throw new InvalidRequestException("Not the Owner of Todo");
        }
    }

    private void validateNotSelfAssign(User user, User managerUser) {
        if (Objects.equals(user.getId(), managerUser.getId())) {
            throw new InvalidRequestException("Can't assign self owner");
        }
    }

    private void validateManagerBelongsToTodo(Manager manager, Todo todo) {
        if (!Objects.equals(manager.getTodo().getId(), todo.getId())) {
            throw new InvalidRequestException("Not a Manager of Todo");
        }
    }
}

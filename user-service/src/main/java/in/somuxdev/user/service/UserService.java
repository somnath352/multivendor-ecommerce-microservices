package in.somuxdev.user.service;

import in.somuxdev.user.dto.request.UpdateUserRequest;
import in.somuxdev.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID id);
    void disableUser(UUID id);
}

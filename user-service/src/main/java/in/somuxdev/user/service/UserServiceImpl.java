package in.somuxdev.user.service;

import in.somuxdev.common.exception.BadRequestException;
import in.somuxdev.common.exception.ResourceNotFoundException;
import in.somuxdev.user.dto.request.UpdateUserRequest;
import in.somuxdev.user.dto.response.UserResponse;
import in.somuxdev.user.entity.User;
import in.somuxdev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id.toString()));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if(request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if(request.getPassword() != null && !request.getPassword().isBlank()) {
            if(request.getPassword().length() < 8)
                throw new BadRequestException("Password must be at least 8 characters long");
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        userRepository.delete(user);
    }

    @Override
    public void disableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: ", id.toString()
                ));

        user.setEnabled(false);
        userRepository.save(user);

    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

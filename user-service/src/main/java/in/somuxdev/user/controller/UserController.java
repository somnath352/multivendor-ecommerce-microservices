package in.somuxdev.user.controller;

import in.somuxdev.common.dto.ApiResponse;
import in.somuxdev.user.dto.request.UpdateUserRequest;
import in.somuxdev.user.dto.response.UserResponse;
import in.somuxdev.user.security.CustomUserDetails;
import in.somuxdev.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // Get own profile
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@PathVariable UUID userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Profile fetched successfully"));
    }

    // Update own profile
    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@PathVariable UUID userId,
                                                                     @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response = userService.updateUser(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Profile updated successfully"));
    }

    // Delete own account
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {

        userService.deleteUser(currentUser.getUserId());
        return ResponseEntity.ok(
                ApiResponse.success(null,"Account deleted successfully"));
    }

    // ──---------------------------------------- ADMIN ONLY ----------------------------------------------

    // Get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success(response, "Users fetched successfully")
        );
    }

    // Get any user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {

        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "User fetched successfully"));
    }

    // Disable any user account
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable UUID id) {

        userService.disableUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,"User updated successfully"));
    }

    // Delete any user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,"User deleted successfully"));
    }
}

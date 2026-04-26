package in.somuxdev.user.service;

import in.somuxdev.user.dto.request.LoginRequest;
import in.somuxdev.user.dto.request.RefreshTokenRequest;
import in.somuxdev.user.dto.request.RegisterRequest;
import in.somuxdev.user.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String authHeader);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String authHeader);
}

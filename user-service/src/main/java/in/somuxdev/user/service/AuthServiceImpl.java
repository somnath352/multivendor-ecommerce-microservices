package in.somuxdev.user.service;

import in.somuxdev.common.exception.BadRequestException;
import in.somuxdev.common.exception.ResourceNotFoundException;
import in.somuxdev.user.dto.request.LoginRequest;
import in.somuxdev.user.dto.request.RefreshTokenRequest;
import in.somuxdev.user.dto.request.RegisterRequest;
import in.somuxdev.user.dto.response.AuthResponse;
import in.somuxdev.user.entity.RefreshToken;
import in.somuxdev.user.entity.User;
import in.somuxdev.user.repository.RefreshTokenRepository;
import in.somuxdev.user.repository.UserRepository;
import in.somuxdev.user.security.JwtService;
import in.somuxdev.user.security.TokenBlacklistService;
import in.somuxdev.user.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService blacklistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = saveRefreshToken(userDetails, user);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String authHeader) {

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getEmail()));

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String oldToken = authHeader.substring(7);
            try {
                long remainingExpiry = jwtService.getRemainingExpiryMillis(oldToken);
                blacklistService.blacklistToken(oldToken, remainingExpiry);
                log.info("Old accessToken blacklisted");
            } catch(Exception e) {
                log.info("Old token already expired — skipping blacklist");
            }
        }

        // Remove old refresh token
        refreshTokenRepository.deleteByUser(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = saveRefreshToken(userDetails, user);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if(storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new BadRequestException("Refresh token expired. Please login again");
        }

        User user = storedToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        return buildAuthResponse(newAccessToken, storedToken.getToken(), user);
    }

    @Override
    @Transactional
    public void logout(String authHeader) {
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                long remainingExpiry = jwtService.getRemainingExpiryMillis(token);
                blacklistService.blacklistToken(token, remainingExpiry);

                String email = jwtService.extractUsername(token);
                userRepository.findByEmail(email)
                        .ifPresent(refreshTokenRepository::deleteByUser);
                log.info("User logged out successfully");
            } catch (Exception e) {
                log.info("Token already expired during logout");
            }
        }
    }

    private String saveRefreshToken(UserDetails userDetails, User user) {
        String token = jwtService.generateRefreshToken(userDetails);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

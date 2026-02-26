package com.trikaar.module.auth.service.impl;

import com.trikaar.config.SecurityProperties;
import com.trikaar.module.auth.dto.*;
import com.trikaar.module.auth.entity.Business;
import com.trikaar.module.auth.entity.RefreshToken;
import com.trikaar.module.auth.entity.User;
import com.trikaar.module.auth.repository.BusinessRepository;
import com.trikaar.module.auth.repository.RefreshTokenRepository;
import com.trikaar.module.auth.repository.UserRepository;
import com.trikaar.module.auth.security.JwtTokenProvider;
import com.trikaar.module.auth.service.AuthService;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.enums.Role;
import com.trikaar.shared.exception.BusinessRuleException;
import com.trikaar.shared.exception.DuplicateResourceException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Resolve tenant
        Business business = businessRepository.findBySlugAndDeletedFalse(request.getBusinessSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Business", "slug", request.getBusinessSlug()));

        if (!business.isActive()) {
            throw new BusinessRuleException("BUSINESS_INACTIVE", "This business account is currently inactive");
        }

        // 2. Find user within tenant
        User user = userRepository.findByUsernameAndBusinessIdAndDeletedFalse(
                request.getUsernameOrEmail(), business.getId())
                .or(() -> userRepository.findByEmailAndBusinessIdAndDeletedFalse(
                        request.getUsernameOrEmail(), business.getId()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // 3. Check account status
        if (user.isLocked()) {
            throw new BusinessRuleException("ACCOUNT_LOCKED",
                    "Account is locked due to too many failed login attempts. Contact your administrator.");
        }

        if (!user.isActive()) {
            throw new BusinessRuleException("ACCOUNT_DISABLED", "Account is disabled");
        }

        // 4. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Increment failed attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLocked(true);
                log.warn("User '{}' locked after {} failed login attempts",
                        user.getUsername(), MAX_FAILED_LOGIN_ATTEMPTS);
            }
            userRepository.save(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // 5. Reset failed attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // 6. Generate tokens
        return buildAuthResponse(user, business);
    }

    @Override
    @Transactional
    public AuthResponse registerBusiness(RegisterBusinessRequest request) {
        // 1. Validate uniqueness
        if (businessRepository.existsBySlugAndDeletedFalse(request.getSlug())) {
            throw new DuplicateResourceException("Business", "slug", request.getSlug());
        }

        if (request.getRegistrationNumber() != null &&
                businessRepository.existsByRegistrationNumberAndDeletedFalse(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Business", "registrationNumber",
                    request.getRegistrationNumber());
        }

        // 2. Create business
        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .slug(request.getSlug().toLowerCase().trim())
                .registrationNumber(request.getRegistrationNumber())
                .gstin(request.getGstin())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        // Business entity self-references its own ID as businessId
        // We need to save first to get the ID, then update businessId
        business.setBusinessId(UUID.randomUUID()); // Temporary - will be replaced
        business = businessRepository.save(business);
        business.setBusinessId(business.getId());
        business = businessRepository.save(business);

        // 3. Create admin user
        User adminUser = User.builder()
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .fullName(request.getAdminFullName())
                .role(Role.ADMIN)
                .active(true)
                .build();
        adminUser.setBusinessId(business.getId());
        adminUser = userRepository.save(adminUser);

        // 4. Update business owner reference
        business.setOwnerUserId(adminUser.getId());
        businessRepository.save(business);

        log.info("Business '{}' registered with admin user '{}'",
                business.getBusinessName(), adminUser.getUsername());

        return buildAuthResponse(adminUser, business);
    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        // Validate uniqueness within tenant
        if (userRepository.existsByEmailAndBusinessIdAndDeletedFalse(request.getEmail(), businessId)) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByUsernameAndBusinessIdAndDeletedFalse(request.getUsername(), businessId)) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        Business business = businessRepository.findByIdAndDeletedFalse(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", businessId));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();
        user.setBusinessId(businessId);
        user = userRepository.save(user);

        log.info("User '{}' registered in business '{}'", user.getUsername(), business.getBusinessName());

        return buildAuthResponse(user, business);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new BusinessRuleException("INVALID_REFRESH_TOKEN",
                        "Invalid or revoked refresh token"));

        if (storedToken.isExpired()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BusinessRuleException("TOKEN_EXPIRED", "Refresh token has expired. Please login again.");
        }

        User user = userRepository.findByIdAndBusinessIdAndDeletedFalse(
                storedToken.getUserId(), storedToken.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", storedToken.getUserId()));

        Business business = businessRepository.findByIdAndDeletedFalse(storedToken.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", storedToken.getBusinessId()));

        // Rotate: revoke old token
        storedToken.setRevoked(true);

        // Generate new tokens
        AuthResponse response = buildAuthResponse(user, business);
        storedToken.setReplacedByToken(response.getRefreshToken());
        refreshTokenRepository.save(storedToken);

        return response;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User '{}' logged out, refresh token revoked", token.getUserId());
                });
    }

    // ═══════════════════ Private Helpers ═══════════════════

    private AuthResponse buildAuthResponse(User user, Business business) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), business.getId(), user.getRole());

        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId(), business.getId());

        // Store refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusSeconds(
                        securityProperties.getJwt().getRefreshTokenExpirationMs() / 1000))
                .build();
        refreshToken.setBusinessId(business.getId());
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(securityProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .businessId(business.getId().toString())
                        .businessName(business.getBusinessName())
                        .build())
                .build();
    }
}

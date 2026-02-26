package com.trikaar.module.auth.service;

import com.trikaar.module.auth.dto.*;

/**
 * Authentication service contract.
 */
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse registerBusiness(RegisterBusinessRequest request);

    AuthResponse registerUser(RegisterUserRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}

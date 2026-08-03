package com.sportx.backend.service;

import com.sportx.backend.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    String forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}

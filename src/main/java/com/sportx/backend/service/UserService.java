package com.sportx.backend.service;

import com.sportx.backend.dto.UserDTO;
import com.sportx.backend.dto.UserUpdateRequest;

public interface UserService {
    UserDTO getUserById(Long id);
    UserDTO getCurrentUser(String email);
    UserDTO updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void uploadProfileImage(Long id, String imageUrl);
}

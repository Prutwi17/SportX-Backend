package com.sportx.backend.service;

import com.sportx.backend.dto.AdminUserDTO;
import com.sportx.backend.dto.AdminUserUpdateRequest;
import com.sportx.backend.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    PagedResponse<AdminUserDTO> getUsers(String keyword, String role, Boolean enabled, Pageable pageable);
    AdminUserDTO getUser(Long id);
    AdminUserDTO updateUser(Long id, AdminUserUpdateRequest request);
    AdminUserDTO resetPassword(Long id, String newPassword);
    AdminUserDTO setEnabled(Long id, boolean enabled);
}

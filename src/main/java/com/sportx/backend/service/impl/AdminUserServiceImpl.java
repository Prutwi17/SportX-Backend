package com.sportx.backend.service.impl;

import com.sportx.backend.dto.AdminUserDTO;
import com.sportx.backend.dto.AdminUserUpdateRequest;
import com.sportx.backend.dto.PagedResponse;
import com.sportx.backend.entity.User;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.DuplicateResourceException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;

    @Override
    public PagedResponse<AdminUserDTO> getUsers(String keyword, String role, Boolean enabled, Pageable pageable) {
        UserRole roleFilter = parseRole(role);
        Page<User> page = userRepository.filterUsers(
                keyword == null || keyword.isBlank() ? null : keyword.trim().toLowerCase(),
                roleFilter, enabled, pageable);
        return new PagedResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public AdminUserDTO getUser(Long id) {
        User user = findUser(id);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUser(Long id, AdminUserUpdateRequest request) {
        User user = findUser(id);
        String email = request.getEmail().trim().toLowerCase();

        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        UserRole newRole = parseRole(request.getRole());
        if (newRole == null) {
            throw new BadRequestException("Invalid role");
        }
        if (isCurrentAdmin(id) && newRole != UserRole.ROLE_ADMIN) {
            throw new BadRequestException("Cannot demote your own admin account");
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setRole(newRole);
        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO resetPassword(Long id, String newPassword) {
        User user = findUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO setEnabled(Long id, boolean enabled) {
        User user = findUser(id);
        if (isCurrentAdmin(id) && !enabled) {
            throw new BadRequestException("Cannot disable your own admin account");
        }
        user.setEnabled(enabled);
        user = userRepository.save(user);
        return mapToDTO(user);
    }

    private boolean isCurrentAdmin(Long id) {
        try {
            return securityUtil.getCurrentUserId().equals(id);
        } catch (Exception e) {
            return false;
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) return null;
        String normalized = role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private AdminUserDTO mapToDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setProfileImage(user.getProfileImage());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setTotalOrders(orderRepository.countByUserId(user.getId()));
        dto.setTotalSpending(orderRepository.sumTotalByUserId(user.getId()));
        return dto;
    }
}

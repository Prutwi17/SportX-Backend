package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.AdminUserDTO;
import com.sportx.backend.dto.AdminUserUpdateRequest;
import com.sportx.backend.dto.AdminPasswordRequest;
import com.sportx.backend.dto.PagedResponse;
import com.sportx.backend.dto.UserStatusRequest;
import com.sportx.backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.ADMIN + "/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<PagedResponse<AdminUserDTO>> getUsers(
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getUsers(keyword, role, enabled, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDTO> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<AdminUserDTO> resetPassword(@PathVariable Long id,
                                                       @Valid @RequestBody AdminPasswordRequest request) {
        return ResponseEntity.ok(adminUserService.resetPassword(id, request.getNewPassword()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserDTO> setEnabled(@PathVariable Long id,
                                                    @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(adminUserService.setEnabled(id, request.getEnabled()));
    }
}

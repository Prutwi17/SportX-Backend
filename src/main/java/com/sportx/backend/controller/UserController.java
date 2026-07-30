package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.UserDTO;
import com.sportx.backend.dto.UserUpdateRequest;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.USERS)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUser(securityUtil.getCurrentUser().getEmail()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(securityUtil.getCurrentUserId(), request));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile() {
        userService.deleteUser(securityUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profile/image")
    public ResponseEntity<Void> uploadImage(@RequestParam String imageUrl) {
        userService.uploadProfileImage(securityUtil.getCurrentUserId(), imageUrl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}

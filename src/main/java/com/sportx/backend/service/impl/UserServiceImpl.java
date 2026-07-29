package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.AddressRequest;
import com.sportx.backend.dto.request.UpdateProfileRequest;
import com.sportx.backend.dto.response.AddressResponse;
import com.sportx.backend.dto.response.UserResponse;
import com.sportx.backend.entity.Address;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.AddressRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.UserService;
import com.sportx.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Value("${sportx.upload.dir}")
    private String uploadDir;

    private User getCurrentUser() {
        return userRepository.findByEmail(AuthUtil.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        return EntityMapper.toUserResponse(getCurrentUser());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Profile photo file is required");
        }

        User user = getCurrentUser();
        try {
            Path uploadPath = Paths.get(uploadDir, "profiles").toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String extension = getExtension(file.getOriginalFilename());
            String filename = user.getId() + "-" + UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);

            user.setProfileImageUrl("/uploads/profiles/" + filename);
            return EntityMapper.toUserResponse(userRepository.save(user));
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload profile photo");
        }
    }

    @Override
    @Transactional
    public void deleteProfile() {
        User user = getCurrentUser();
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(EntityMapper::toAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        User user = getCurrentUser();
        if (request.isDefault()) {
            clearDefaultAddress(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .label(request.getLabel())
                .isDefault(request.isDefault())
                .build();

        return EntityMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (request.isDefault()) {
            clearDefaultAddress(user.getId());
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setLabel(request.getLabel());
        address.setDefault(request.isDefault());

        return EntityMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepository.delete(address);
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserId(userId).forEach(address -> {
            if (address.isDefault()) {
                address.setDefault(false);
                addressRepository.save(address);
            }
        });
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}

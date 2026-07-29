package com.sportx.backend.service;

import com.sportx.backend.dto.request.AddressRequest;
import com.sportx.backend.dto.request.UpdateProfileRequest;
import com.sportx.backend.dto.response.AddressResponse;
import com.sportx.backend.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse getProfile();

    UserResponse updateProfile(UpdateProfileRequest request);

    UserResponse uploadProfilePhoto(MultipartFile file);

    void deleteProfile();

    List<AddressResponse> getAddresses();

    AddressResponse addAddress(AddressRequest request);

    AddressResponse updateAddress(Long addressId, AddressRequest request);

    void deleteAddress(Long addressId);
}

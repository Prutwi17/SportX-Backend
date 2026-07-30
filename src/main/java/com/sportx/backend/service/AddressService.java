package com.sportx.backend.service;

import com.sportx.backend.dto.AddressDTO;
import com.sportx.backend.dto.AddressRequest;

import java.util.List;

public interface AddressService {
    List<AddressDTO> getUserAddresses(Long userId);
    AddressDTO getAddressById(Long addressId);
    AddressDTO createAddress(Long userId, AddressRequest request);
    AddressDTO updateAddress(Long addressId, AddressRequest request);
    void deleteAddress(Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}

package com.sportx.backend.service.impl;

import com.sportx.backend.dto.AddressDTO;
import com.sportx.backend.dto.AddressRequest;
import com.sportx.backend.entity.Address;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.AddressRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressDTO> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return mapToDTO(address);
    }

    @Override
    public AddressDTO createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.isDefault() || addressRepository.countByUserId(userId) == 0)
                .build();

        if (address.isDefault()) {
            clearOtherDefaults(userId);
        }

        address = addressRepository.save(address);
        return mapToDTO(address);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());

        if (request.isDefault() && !address.isDefault()) {
            clearOtherDefaults(address.getUser().getId());
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        return mapToDTO(address);
    }

    @Override
    public void deleteAddress(Long addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new ResourceNotFoundException("Address not found");
        }
        addressRepository.deleteById(addressId);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        clearOtherDefaults(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setDefault(true);
        addressRepository.save(address);
    }

    private void clearOtherDefaults(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        addresses.forEach(a -> a.setDefault(false));
        addressRepository.saveAll(addresses);
    }

    private AddressDTO mapToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUser().getId());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setDefault(address.isDefault());
        return dto;
    }
}

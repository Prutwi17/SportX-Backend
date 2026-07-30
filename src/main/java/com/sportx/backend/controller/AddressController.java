package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.AddressDTO;
import com.sportx.backend.dto.AddressRequest;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.ADDRESSES)
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        return ResponseEntity.ok(addressService.getUserAddresses(securityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(securityUtil.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id,
                                                     @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(@PathVariable Long id) {
        addressService.setDefaultAddress(securityUtil.getCurrentUserId(), id);
        return ResponseEntity.ok().build();
    }
}

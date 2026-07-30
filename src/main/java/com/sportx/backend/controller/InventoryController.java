package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.ProductDTO;
import com.sportx.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.INVENTORY)
@RequiredArgsConstructor
public class InventoryController {

    private final ProductService productService;

    @PutMapping("/{productId}/stock")
    public ResponseEntity<Void> updateStock(@PathVariable Long productId,
                                             @RequestParam int quantity) {
        // Stock updates are handled via product update
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        // Endpoint concept - low stock logic lives in DashboardService/ProductService
        return ResponseEntity.ok().build();
    }
}

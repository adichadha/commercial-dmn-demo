package com.example.commercial.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.commercial.model.ProductAvailabilityRequest;
import com.example.commercial.model.ProductAvailabilityResponse;
import com.example.commercial.service.ProductAvailabilityService;

@RestController
@RequestMapping("/api/v1/commercial/products")
public class ProductAvailabilityController {
    private final ProductAvailabilityService service;

    public ProductAvailabilityController(ProductAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/available")
    public ResponseEntity<ProductAvailabilityResponse> availableProducts(
            @RequestBody ProductAvailabilityRequest request) {
        return ResponseEntity.ok(service.findProducts(request));
    }
}

package com.example.commercial.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import com.example.commercial.model.Product;

import java.io.IOException;
import java.util.List;

@Repository
public class PricingMatrixRepository {
    private final List<Product> products;

    public PricingMatrixRepository(ObjectMapper objectMapper) throws IOException {
        var resource = new ClassPathResource("pricing-matrix.json");
        this.products = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Product>>() {}
        );
    }

    public List<Product> findAll() {
        return products;
    }
}

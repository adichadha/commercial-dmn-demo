package com.example.commercial.service;

import org.springframework.stereotype.Service;
import com.example.commercial.model.EligibleProduct;
import com.example.commercial.model.Product;
import com.example.commercial.model.ProductAvailabilityRequest;
import com.example.commercial.model.ProductAvailabilityResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
public class ProductAvailabilityService {
    private final PricingMatrixRepository pricingMatrixRepository;
    private final DmnProductEligibilityService dmnService;

    public ProductAvailabilityService(
            PricingMatrixRepository pricingMatrixRepository,
            DmnProductEligibilityService dmnService) {
        this.pricingMatrixRepository = pricingMatrixRepository;
        this.dmnService = dmnService;
    }

    public ProductAvailabilityResponse findProducts(ProductAvailabilityRequest request) {
        validate(request);
        BigDecimal ltv = calculateLtv(request.requestedLoan(), request.propertyValue());
        var pricingMatrix = pricingMatrixRepository.findAll();
        var eligibleProducts = new ArrayList<EligibleProduct>();

        for (Product product : pricingMatrix) {
            var decision = dmnService.evaluate(request, product, ltv);
            if (decision.eligible()) {
                eligibleProducts.add(new EligibleProduct(product, decision.reason()));
            }
        }

        return new ProductAvailabilityResponse(
                ltv,
                pricingMatrix.size(),
                eligibleProducts.size(),
                eligibleProducts
        );
    }

    private BigDecimal calculateLtv(BigDecimal loan, BigDecimal value) {
        return loan.divide(value, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validate(ProductAvailabilityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.securityClass() == null || request.securityClass().isBlank()) {
            throw new IllegalArgumentException("securityClass is required");
        }
        if (request.repaymentType() == null || request.repaymentType().isBlank()) {
            throw new IllegalArgumentException("repaymentType is required");
        }
        if (request.propertyValue() == null || request.propertyValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("propertyValue must be greater than zero");
        }
        if (request.requestedLoan() == null || request.requestedLoan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("requestedLoan must be greater than zero");
        }
    }
}

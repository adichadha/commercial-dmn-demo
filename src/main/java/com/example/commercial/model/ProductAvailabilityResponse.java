package com.example.commercial.model;

import java.math.BigDecimal;
import java.util.List;

public record ProductAvailabilityResponse(
        BigDecimal calculatedLtv,
        int pricingMatrixProducts,
        int eligibleProductCount,
        List<EligibleProduct> products
) {}

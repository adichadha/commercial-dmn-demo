package com.example.commercial.model;

import java.math.BigDecimal;

public record ProductAvailabilityRequest(
        String securityClass,
        String repaymentType,
        BigDecimal propertyValue,
        BigDecimal requestedLoan
) {}

package com.example.commercial.model;

import java.math.BigDecimal;

public record Product(
        String productCode,
        String productName,
        String securityClass,
        String repaymentType,
        BigDecimal interestRate,
        BigDecimal maxLtv,
        BigDecimal minLoan,
        BigDecimal maxLoan
) {}

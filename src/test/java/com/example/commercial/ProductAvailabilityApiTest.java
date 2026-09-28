package com.example.commercial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductAvailabilityApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsOnlyEligibleProducts() throws Exception {
        String request = """
                {
                  "securityClass": "COMMERCIAL_INVESTMENT",
                  "repaymentType": "INTEREST_ONLY",
                  "propertyValue": 1000000,
                  "requestedLoan": 650000
                }
                """;

        mockMvc.perform(post("/api/v1/commercial/products/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedLtv").value(65.00))
                .andExpect(jsonPath("$.pricingMatrixProducts").value(5))
                .andExpect(jsonPath("$.eligibleProductCount").value(2))
                .andExpect(jsonPath("$.products[0].product.productCode").value("CI-IO-001"))
                .andExpect(jsonPath("$.products[1].product.productCode").value("CI-IO-002"));
    }
}

package com.example.commercial.service;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.springframework.stereotype.Service;
import com.example.commercial.model.Product;
import com.example.commercial.model.ProductAvailabilityRequest;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class DmnProductEligibilityService {
    private static final String DMN_RESOURCE = "commercial-product-availability.dmn";
    private static final String NAMESPACE =
            "https://example.com/dmn/commercial/product-availability/v1";
    private static final String MODEL = "CommercialProductAvailability";

    private final DMNRuntime runtime;
    private final DMNModel model;

    public DmnProductEligibilityService() {
        this.runtime = DMNRuntimeBuilder.fromDefaults()
                .buildConfiguration()
                .fromClasspathResource(DMN_RESOURCE, DmnProductEligibilityService.class)
                .getOrElseThrow(cause -> new IllegalStateException(
                        "Unable to load DMN resource: " + DMN_RESOURCE, cause));
        this.model = runtime.getModel(NAMESPACE, MODEL);

        if (model == null) {
            throw new IllegalStateException("DMN model not found: " + MODEL);
        }
    }

    public Decision evaluate(ProductAvailabilityRequest request, Product product, BigDecimal ltv) {
        DMNContext context = runtime.newContext();
        context.set("securityClass", request.securityClass());
        context.set("repaymentType", request.repaymentType());
        context.set("ltv", ltv);
        context.set("requestedLoan", request.requestedLoan());
        context.set("productSecurityClass", product.securityClass());
        context.set("productRepaymentType", product.repaymentType());
        context.set("productMaxLtv", product.maxLtv());
        context.set("productMinLoan", product.minLoan());
        context.set("productMaxLoan", product.maxLoan());

        DMNResult result = runtime.evaluateAll(model, context);
        if (result.hasErrors()) {
            String errors = result.getMessages().stream()
                    .map(Object::toString)
                    .reduce("", (a, b) -> a + System.lineSeparator() + b);
            throw new IllegalStateException("DMN evaluation failed:" + errors);
        }

        DMNDecisionResult decisionResult = result.getDecisionResultByName("Eligibility");
        if (decisionResult == null) {
            throw new IllegalStateException("Eligibility decision not found");
        }

        Object raw = decisionResult.getResult();
        if (!(raw instanceof Map<?, ?> values)) {
            throw new IllegalStateException("Unexpected Eligibility result: " + raw);
        }

        boolean eligible = Boolean.TRUE.equals(values.get("eligible"));
        String reason = String.valueOf(values.get("reason"));
        return new Decision(eligible, reason);
    }

    public record Decision(boolean eligible, String reason) {}
}

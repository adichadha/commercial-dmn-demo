# Commercial Product Availability - DMN Demo

A deliberately small demo showing how a Spring Boot service can filter a pricing matrix using DMN business rules.

## Demo architecture

1. The API receives application selection criteria.
2. Java calculates LTV.
3. Java loads a small pricing matrix from `pricing-matrix.json`.
4. Java loops through every product.
5. Each product is evaluated against one DMN model.
6. The DMN contains exactly two decision tables:
   - **Basic Product Match** - security class + repayment type.
   - **Eligibility** - depends on Basic Product Match and then checks LTV and min/max loan.
7. The API returns only products where the final DMN decision is eligible.

The point of the demo is the separation of responsibilities:

- **Pricing matrix JSON** = product data.
- **Java** = API, orchestration and calculation.
- **DMN** = business decision logic.

The application builds its DMN runtime directly from
`commercial-product-availability.dmn`. It intentionally has no
`META-INF/kmodule.xml`, classpath `KieContainer`, or Maven-backed `kie-ci`
dependency because this demo contains only one self-contained DMN model.

## Technology

- Java 21
- Spring Boot 3.5.16
- Gradle 8.14.3 (standard Gradle wrapper)
- Drools/KIE DMN 10.1.0

## API

`POST /api/v1/commercial/products/available`

Example request:

```json
{
  "securityClass": "COMMERCIAL_INVESTMENT",
  "repaymentType": "INTEREST_ONLY",
  "propertyValue": 1000000,
  "requestedLoan": 650000
}
```

Java calculates LTV as:

`650000 / 1000000 * 100 = 65%`

For this request the demo returns two eligible products:

- `CI-IO-001` - max LTV 75%
- `CI-IO-002` - max LTV 65%

Other products are filtered out because their security class or repayment type does not match.

## Decision table 1 - Basic Product Match

Inputs:

- `securityClass = productSecurityClass`
- `repaymentType = productRepaymentType`

Output:

- `true` when both match
- `false` otherwise

## Decision table 2 - Eligibility

This decision explicitly depends on **Basic Product Match**.

It checks:

- basic product match
- application LTV <= product max LTV
- requested loan >= product minimum loan
- requested loan <= product maximum loan

Possible decision reasons:

- `PRODUCT_TYPE_MISMATCH`
- `LTV_TOO_HIGH`
- `BELOW_MINIMUM_LOAN`
- `ABOVE_MAXIMUM_LOAN`
- `ELIGIBLE`

## Project structure

```text
src/main/java/com/example/commercial
  Application.java
  api/
  model/
  service/

src/main/resources
  commercial-product-availability.dmn
  pricing-matrix.json
```

## Run

Check that Java 21 is active. The standard `./gradlew` wrapper downloads Gradle 8.14.3 on first use:

```bash
java -version
./gradlew --version
```

Run the tests:

```bash
./gradlew clean test
```

Start the API:

```bash
./gradlew bootRun
```

## Call the API

From the project root:

```bash
curl --location \
  'http://localhost:8080/api/v1/commercial/products/available' \
  --header 'Content-Type: application/json' \
  --data-binary @example-request.json
```

Expected shape:

```json
{
  "calculatedLtv": 65.00,
  "pricingMatrixProducts": 5,
  "eligibleProductCount": 2,
  "products": [
    {
      "product": {
        "productCode": "CI-IO-001"
      },
      "decision": "ELIGIBLE"
    },
    {
      "product": {
        "productCode": "CI-IO-002"
      },
      "decision": "ELIGIBLE"
    }
  ]
}
```

## Team-demo talking point

The Java code does **not** hard-code lending rules such as `if (ltv <= 75)` for product selection. Java supplies application and product facts to the DMN. The DMN owns the decision logic, which makes the business rules much easier to visualise and evolve.

This is intentionally a demo rather than the complete Commercial Lending policy. The same pattern can later be extended to ICR, stress rates, income rules, package affordability and other product-selection criteria.

# RideLink Fare & Payment Service (`payment-service`)

Enterprise-grade microservice for the **RideLink** ride-sharing platform responsible for dynamic upfront fare estimation, final ride fare calculation, multi-method simulated payment processing, payment status management (including failure simulation and refunds), and digital receipt generation.

---

## 🏛️ Architecture & Clean Code Principles

- **Architecture:** Layered Clean Architecture adhering to **SOLID** principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion via Service interfaces).
- **Isolation:** Independent microservice with its own dedicated MongoDB persistence layer. No cross-database dependencies.
- **Validation:** Bean Validation (`@Valid`, `@NotNull`, `@Positive`, `@NotBlank`, `@Min`, `@Max`) on all API request models.
- **Error Handling:** Centralized `@RestControllerAdvice` yielding consistent structured JSON error responses with standard HTTP status codes (`400`, `404`, `422`, `500`).
- **OpenAPI Documentation:** Integrated Swagger UI / OpenAPI 3.0 via `springdoc-openapi`.

---

## 🚀 Tech Stack

- **Java:** JDK 17+ (tested with Java 17 and Java 23)
- **Framework:** Spring Boot 3.2.3 (Spring Web, Spring Data MongoDB, Spring Validation)
- **Database:** MongoDB (Local, Docker container, or MongoDB Atlas SRV)
- **Documentation:** SpringDoc OpenAPI 2.3.0 / Swagger UI
- **Testing:** JUnit 5, Mockito, Spring MockMvc

---

## 📁 Directory Structure

```
payment-service/
├── .env.example
├── pom.xml
├── Dockerfile
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridelink/payment/
    │   │   ├── PaymentServiceApplication.java
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── DataInitializer.java
    │   │   ├── controller/
    │   │   │   ├── FareController.java
    │   │   │   └── PaymentController.java
    │   │   ├── dto/
    │   │   │   ├── CoordinateDto.java
    │   │   │   ├── FareEstimateRequest.java
    │   │   │   ├── FareEstimateResponse.java
    │   │   │   ├── FinalFareRequest.java
    │   │   │   ├── FinalFareResponse.java
    │   │   │   ├── PaymentRefundRequestDto.java
    │   │   │   ├── PaymentRequestDto.java
    │   │   │   ├── PaymentResponseDto.java
    │   │   │   └── ReceiptDto.java
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── PaymentProcessingException.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── model/
    │   │   │   ├── FareRule.java
    │   │   │   ├── Payment.java
    │   │   │   ├── PaymentMethod.java
    │   │   │   ├── PaymentStatus.java
    │   │   │   └── Receipt.java
    │   │   ├── repository/
    │   │   │   ├── FareRuleRepository.java
    │   │   │   ├── PaymentRepository.java
    │   │   │   └── ReceiptRepository.java
    │   │   └── service/
    │   │       ├── FareCalculationService.java
    │   │       ├── PaymentService.java
    │   │       └── impl/
    │   │           ├── FareCalculationServiceImpl.java
    │   │           └── PaymentServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/ridelink/payment/
            ├── controller/
            │   ├── FareControllerTest.java
            │   └── PaymentControllerTest.java
            └── service/
                ├── FareCalculationServiceTest.java
                └── PaymentServiceTest.java
```

---

## 🧮 Fare Calculation Formula & Rules

### 1. Distance Calculation (Haversine Formula)
When GPS coordinates are passed:
$$\Delta\text{lat} = \text{lat}_2 - \text{lat}_1, \quad \Delta\text{lon} = \text{lon}_2 - \text{lon}_1$$
$$a = \sin^2\left(\frac{\Delta\text{lat}}{2}\right) + \cos(\text{lat}_1)\cos(\text{lat}_2)\sin^2\left(\frac{\Delta\text{lon}}{2}\right)$$
$$c = 2 \cdot \text{atan2}(\sqrt{a}, \sqrt{1-a})$$
$$\text{Distance (KM)} = 6371.0 \times c$$

### 2. Fare Breakdown
- **Distance Fare** = $\text{Distance (KM)} \times \text{PerKmRate}$
- **Time Fare** = $\text{Duration (Minutes)} \times \text{PerMinuteRate}$
- **Waiting Fare** = $\text{WaitingTime (Minutes)} \times \text{PerMinuteRate}$
- **Subtotal** = $\text{BaseFare} + \text{DistanceFare} + \text{TimeFare} + \text{WaitingFare}$
- **Surged Subtotal** = $\max(\text{Subtotal} \times \text{SurgeMultiplier}, \text{MinimumFare})$
- **Tax Amount** = $\text{Surged Subtotal} \times (\text{TaxRatePercent} / 100)$
- **Final Payable** = $\max(0.0, \text{Surged Subtotal} + \text{TaxAmount} - \text{DiscountAmount})$

---

## 📡 REST API Endpoints Summary

### Fare APIs (`/api/fare`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/fare/estimate` | Estimate ride fare using distance or pickup/destination coordinates |
| `POST` | `/api/fare/calculate-final` | Calculate finalized fare on trip completion |
| `GET` | `/api/fare/rules` | Retrieve all vehicle fare rule categories |
| `GET` | `/api/fare/rules/{category}` | Retrieve fare rate for specific vehicle category (e.g. `ECONOMY`, `PREMIUM`, `BIKE`) |

### Payment & Receipt APIs (`/api/payments`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/process` | Record/simulate payment transaction (`CARD`, `CASH`, `WALLET`, `UPI`). Generates receipt |
| `GET` | `/api/payments/{id}` | Get payment by payment ID |
| `GET` | `/api/payments/ride/{rideId}` | Get payment details by ride ID |
| `GET` | `/api/payments/passenger/{passengerId}` | Get payment history for passenger |
| `GET` | `/api/payments/driver/{driverId}` | Get earnings/payments for driver |
| `POST` | `/api/payments/{id}/refund` | Refund completed payment transaction |
| `GET` | `/api/payments/receipt/{rideId}` | Retrieve receipt by ride ID |
| `GET` | `/api/payments/receipt/number/{receiptNumber}` | Retrieve receipt by receipt number |
| `GET` | `/api/payments/receipt/passenger/{passengerId}` | Retrieve all receipts for passenger |
| `GET` | `/api/payments/receipt/driver/{driverId}` | Retrieve all receipts for driver |

---

## ⚙️ Prerequisites & Running the Service

### Prerequisites
- Java 17+ (or JDK 21 / 23)
- Maven 3.8+ (or use the provided `./mvnw` / `mvnw.cmd`)
- MongoDB instance (local or MongoDB Atlas connection string)

### 1. Configure Environment Variables
Copy `.env.example` or adjust `application.properties`:
```properties
SERVER_PORT=8084
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/payment_db
```

### 2. Run the Service
```bash
# From workspace root
./mvnw spring-boot:run -pl payment-service

# Or on Windows PowerShell:
.\mvnw.cmd spring-boot:run -pl payment-service
```

### 3. Run Unit & Controller Tests
```bash
.\mvnw.cmd test -pl payment-service
```

### 4. Access OpenAPI / Swagger UI
Once started, open your browser and navigate to:
```
http://localhost:8084/swagger-ui.html
http://localhost:8084/v3/api-docs
```

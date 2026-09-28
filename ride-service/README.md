# RideLink - Ride Management Service (Microservice)

The **Ride Management Service** is an enterprise-grade microservice responsible for orchestrating the entire lifecycle of a ride in the **RideLink** ride-sharing platform. Built with **Spring Boot 3**, **Java 17+**, **Spring Data MongoDB**, and **Springdoc OpenAPI**, it enforces a strict state machine, decoupled inter-service communications, robust input validation, and centralized exception handling.

---

## 🏛️ Architecture & Clean Design Principles

The service strictly adheres to **Clean Architecture** and **SOLID Principles**:
- **Separation of Concerns:** Distinct controller, service, domain model, repository, state machine, client, DTO, and exception handling layers.
- **Single Responsibility Principle (SRP):** Each class encapsulates one specific responsibility (e.g., `RideStateMachine` strictly manages transition invariants; `DriverClient` handles driver HTTP communications).
- **Open/Closed Principle (OCP):** State machine and exception handlers are easily extensible for new ride states without rewriting core transaction logic.
- **Dependency Inversion Principle (DIP):** Services rely on abstractions and Spring-managed reactive clients rather than hardcoded connections.
- **Isolated Persistence:** Dedicated MongoDB database (`ride_db`) with no direct cross-database dependencies with other services.

---

## 🔄 Ride Lifecycle State Machine

The service implements a finite state machine ensuring valid transitions across the ride lifecycle:

```
                  ┌──────────────┐
                  │  REQUESTED   │
                  └──────┬───────┘
                         │
           ┌─────────────┴─────────────┐
           ▼                           ▼
    ┌──────────────┐            ┌──────────────┐
    │   ASSIGNED   ├───────────►│  CANCELLED   │ (Terminal)
    └──────┬───────┘            └──────▲───────┘
           │                           │
           ▼                           │
    ┌──────────────┐                   │
    │   ACCEPTED   ├───────────────────┤
    └──────┬───────┘                   │
           │ (or via DRIVER_ARRIVED)   │
           ▼                           │
    ┌──────────────┐                   │
    │ IN_PROGRESS  ├───────────────────┘
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  COMPLETED   │ (Terminal)
    └──────────────┘
```

### State Transition Validation Matrix

| Current State | Permitted Target States | Trigger / Notes |
| :--- | :--- | :--- |
| `REQUESTED` | `ASSIGNED`, `ACCEPTED`, `CANCELLED` | Driver matched or assigned; passenger cancels |
| `ASSIGNED` | `ACCEPTED`, `REQUESTED`, `CANCELLED` | Driver accepts, rejects (redispatch), or cancels |
| `ACCEPTED` | `DRIVER_ARRIVED`, `IN_PROGRESS`, `CANCELLED` | Driver arrives or trip starts |
| `DRIVER_ARRIVED` | `IN_PROGRESS`, `CANCELLED` | Trip commences upon passenger boarding |
| `IN_PROGRESS` | `COMPLETED`, `CANCELLED` | Destination reached or emergency cancel |
| `COMPLETED` | *None* | Terminal state. Triggers payment & driver release |
| `CANCELLED` | *None* | Terminal state. Resets driver availability |

*Any attempt to execute an invalid transition (e.g., `REQUESTED` -> `COMPLETED`) is rejected with HTTP `422 Unprocessable Entity` or `400 Bad Request`.*

---

## 📁 Project Directory Structure

```
ride-service/
├── .env.example                               # Sample environment variables
├── Dockerfile                                 # Multi-stage container definition
├── pom.xml                                    # Maven dependencies & build configuration
├── README.md                                  # Service documentation
└── src/
    ├── main/
    │   ├── java/com/ridelink/ride/
    │   │   ├── RideServiceApplication.java    # Spring Boot entrypoint
    │   │   ├── client/                        # Inter-service WebClients (Account, Driver, Payment)
    │   │   │   ├── AccountClient.java
    │   │   │   ├── DriverClient.java
    │   │   │   └── PaymentClient.java
    │   │   ├── config/                        # OpenAPI & WebClient configurations
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── WebClientConfig.java
    │   │   ├── controller/                    # REST Controller with Swagger annotations
    │   │   │   └── RideController.java
    │   │   ├── dto/                           # Request, Response & Error DTOs
    │   │   │   ├── DriverAssignDto.java
    │   │   │   ├── ErrorResponseDto.java
    │   │   │   ├── RideCancelDto.java
    │   │   │   ├── RideRequestDto.java
    │   │   │   ├── RideResponseDto.java
    │   │   │   └── RideStatusUpdateDto.java
    │   │   ├── exception/                     # Custom exceptions & Centralized Handler
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── InvalidStateTransitionException.java
    │   │   │   ├── RideNotFoundException.java
    │   │   │   └── UnauthorizedActionException.java
    │   │   ├── model/                         # MongoDB Document Models
    │   │   │   ├── Location.java
    │   │   │   ├── Ride.java
    │   │   │   └── RideStatus.java
    │   │   ├── repository/                    # Spring Data MongoDB Repositories
    │   │   │   └── RideRepository.java
    │   │   ├── service/                       # Ride domain business logic
    │   │   │   └── RideService.java
    │   │   └── statemachine/                  # Finite State Machine Engine
    │   │       └── RideStateMachine.java
    │   └── resources/
    │       └── application.properties         # Application configuration & env bindings
    └── test/
        └── java/com/ridelink/ride/
            ├── controller/
            │   └── RideControllerTest.java    # MockMvc REST API tests
            ├── service/
            │   └── RideServiceTest.java       # Mockito unit tests covering all workflows
            └── statemachine/
                └── RideStateMachineTest.java  # Exhaustive state transition matrix tests
```

---

## ⚙️ Environment Configuration

Set the environment variables or copy `.env.example`:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8083` | HTTP Port for Ride Service |
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/ride_db` | MongoDB connection URI |
| `SERVICES_ACCOUNT_URL` | `http://localhost:8081` | Account Service Base URL |
| `SERVICES_DRIVER_URL` | `http://localhost:8082` | Driver Service Base URL |
| `SERVICES_PAYMENT_URL` | `http://localhost:8084` | Payment Service Base URL |

---

## 🚀 Running the Service

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **MongoDB** (Local or MongoDB Atlas)

### Option 1: Run with Maven
```bash
# From the repository root
./mvnw spring-boot:run -pl ride-service

# Or inside the ride-service directory
cd ride-service
mvn spring-boot:run
```

### Option 2: Run with Docker Compose (Entire System)
```bash
docker-compose up --build ride-service
```

---

## 🧪 Running Unit & Integration Tests

The test suite includes **60 automated tests** covering happy paths, negative state transitions, validation boundaries, and REST API controller endpoints:

```bash
# Run ride-service test suite
./mvnw test -pl ride-service
```

Test coverage includes:
- `RideStateMachineTest`: Permitted vs prohibited status transitions, terminal state enforcement, business invariant validations.
- `RideServiceTest`: Auto driver selection, fallback pricing, payment triggers, driver availability updates, authorized cancellations.
- `RideControllerTest`: Input validation (400), non-existent ride handling (404), invalid state transition handling (422), unauthorized actions (403).

---

## 📖 API Documentation & Swagger UI

Once running, interactive Swagger UI documentation and OpenAPI 3.0 specification are available at:

- **Swagger UI:** [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- **OpenAPI JSON Docs:** [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)

---

## 📡 REST API Reference

### 1. Request a Ride
- **`POST /api/rides/request`**
- **Payload:**
```json
{
  "passengerId": "pass_65f1a2b3c4d5e6f7a8b9c0d1",
  "pickupLocation": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8510
  },
  "dropoffLocation": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8920,
    "longitude": 79.8550
  }
}
```

### 2. Get Ride Details
- **`GET /api/rides/{id}`**

### 3. Assign Driver
- **`PUT /api/rides/{id}/assign`**
- **Payload:**
```json
{
  "driverId": "drv_89a1b2c3d4e5f6a7b8c9d0e1"
}
```

### 4. Accept Ride
- **`PUT /api/rides/{id}/accept?driverId=drv_89a1b2c3d4e5f6a7b8c9d0e1`**

### 5. Start Trip
- **`PUT /api/rides/{id}/start?driverId=drv_89a1b2c3d4e5f6a7b8c9d0e1`**

### 6. Complete Trip
- **`PUT /api/rides/{id}/complete?driverId=drv_89a1b2c3d4e5f6a7b8c9d0e1`**

### 7. Cancel Ride
- **`PUT /api/rides/{id}/cancel`**
- **Payload:**
```json
{
  "userId": "pass_65f1a2b3c4d5e6f7a8b9c0d1",
  "reason": "Passenger found alternative transport"
}
```

### 8. Query History
- **`GET /api/rides/passenger/{passengerId}`** - Passenger ride history
- **`GET /api/rides/driver/{driverId}`** - Driver ride history
- **`GET /api/rides?status=REQUESTED`** - Filter rides by status

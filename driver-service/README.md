# RideLink - Driver & Vehicle Service (Microservice)

The **Driver & Vehicle Service** is an enterprise-grade microservice responsible for managing driver operational profiles, vehicle fleet registrations, live availability status toggles, simulated geolocation tracking, and real-time eligible driver discovery for the **RideLink** platform. Built with **Spring Boot 3**, **Java 17+**, **Spring Data MongoDB**, and **Springdoc OpenAPI**, it adheres strictly to Clean Architecture, SOLID principles, and microservice persistence isolation.

---

## 🏛️ Architecture & Clean Design Principles

- **Isolated Persistence:** Dedicated MongoDB database (`driver_db`) with independent collections for `drivers` and `vehicles`. No direct cross-service database access.
- **Single Responsibility Principle (SRP):**
  - `DriverService`: Manages driver profile records, operational metrics, and nearest driver discovery algorithms.
  - `VehicleService`: Manages vehicle fleet specifications, license plate validation, and driver-vehicle relationships.
  - `LocationService`: Encapsulates Haversine distance computations and GPS coordinate updates.
- **Open/Closed Principle (OCP):** Easily extensible for new vehicle categories or availability status flows without altering core service signatures.
- **Centralized Error Handling:** `@RestControllerAdvice` delivering consistent JSON error responses with standard HTTP status codes (`400`, `404`, `409`, `422`, `500`).

---

## 📁 Project Directory Structure

```
driver-service/
├── .env.example                               # Environment variable templates
├── Dockerfile                                 # Container build definition
├── pom.xml                                    # Maven project configuration & dependencies
├── README.md                                  # Microservice documentation
└── src/
    ├── main/
    │   ├── java/com/ridelink/driver/
    │   │   ├── DriverServiceApplication.java  # Main application bootstrap
    │   │   ├── config/                        # OpenAPI & Bean configuration
    │   │   │   └── OpenApiConfig.java
    │   │   ├── controller/                    # REST Controllers
    │   │   │   ├── DriverController.java
    │   │   │   └── VehicleController.java
    │   │   ├── dto/                           # Request, Response & Error DTOs
    │   │   │   ├── DriverAvailabilityDto.java
    │   │   │   ├── DriverProfileDto.java
    │   │   │   ├── DriverResponseDto.java
    │   │   │   ├── ErrorResponseDto.java
    │   │   │   ├── LocationUpdateDto.java
    │   │   │   ├── ServiceAreaUpdateDto.java
    │   │   │   ├── VehicleRegistrationDto.java
    │   │   │   └── VehicleResponseDto.java
    │   │   ├── exception/                     # Custom domain exceptions & advice
    │   │   │   ├── DriverNotFoundException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── InvalidDriverStateException.java
    │   │   │   └── VehicleNotFoundException.java
    │   │   ├── model/                         # MongoDB Document entities & Enums
    │   │   │   ├── Driver.java
    │   │   │   ├── DriverStatus.java          # ONLINE, AVAILABLE, OFFLINE, BUSY, SUSPENDED
    │   │   │   ├── Vehicle.java
    │   │   │   └── VehicleCategory.java       # SEDAN, SUV, HATCHBACK, TUK_TUK, VAN, PREMIUM, MOTORBIKE
    │   │   ├── repository/                    # Spring Data MongoDB Repositories
    │   │   │   ├── DriverRepository.java
    │   │   │   └── VehicleRepository.java
    │   │   └── service/                       # Business Logic services
    │   │       ├── DriverService.java
    │   │       ├── LocationService.java
    │   │       └── VehicleService.java
    │   └── resources/
    │       └── application.properties         # Properties with ${ENV} configuration
    └── test/
        └── java/com/ridelink/driver/
            ├── controller/
            │   ├── DriverControllerTest.java  # MockMvc REST API tests for DriverController
            │   └── VehicleControllerTest.java # MockMvc REST API tests for VehicleController
            └── service/
                ├── DriverServiceTest.java     # Unit tests covering profile, availability, and proximity search
                └── VehicleServiceTest.java    # Unit tests covering vehicle registration and fleet linking
```

---

## ⚙️ Environment Configuration

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8082` | HTTP port for Driver Service |
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/driver_db` | MongoDB connection URI |

---

## 🚀 Running the Service

### Option 1: Run Locally with Maven
```bash
# From repository root
./mvnw spring-boot:run -pl driver-service

# Or within driver-service directory
cd driver-service
mvn spring-boot:run
```

### Option 2: Run via Docker Compose
```bash
docker-compose up --build driver-service
```

---

## 🧪 Running Unit & Integration Tests

The test suite includes **31 automated tests** covering happy paths, validation failures, duplicate checks, and proximity filtering:

```bash
./mvnw test -pl driver-service
```

---

## 📖 API Documentation & Swagger UI

- **Interactive Swagger UI:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **OpenAPI 3.0 Specification:** [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

---

## 📡 REST API Reference

### Driver Operational Profile APIs

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/drivers` | Register or update driver operational profile |
| `GET` | `/api/drivers/{id}` | Get driver profile by ID (with active vehicle) |
| `GET` | `/api/drivers/user/{userId}` | Get driver profile by account User ID |
| `GET` | `/api/drivers` | Get all drivers (optional `?status=` and `?city=`) |
| `PUT` | `/api/drivers/{id}/availability` | Update availability status (`ONLINE`, `AVAILABLE`, `OFFLINE`, `BUSY`, `SUSPENDED`) |
| `PUT` | `/api/drivers/{id}/toggle-status` | Toggle availability status (`OFFLINE` ⟷ `AVAILABLE`) |
| `PUT` | `/api/drivers/{id}/location` | Update GPS coordinates and current landmark |
| `PUT` | `/api/drivers/{id}/service-area` | Update operational city and region |
| `GET` | `/api/drivers/available` | Find nearest available eligible drivers (with `latitude`, `longitude`, `radiusKm`, `category`, `minSeats`) |

### Vehicle Management APIs

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/vehicles` | Register vehicle for driver |
| `GET` | `/api/vehicles/{id}` | Get vehicle details by ID |
| `GET` | `/api/vehicles/driver/{driverId}` | Get all vehicles registered to a driver |
| `GET` | `/api/vehicles/driver/{driverId}/active` | Get active vehicle for a driver |
| `PUT` | `/api/vehicles/{id}` | Update vehicle specifications |
| `DELETE` | `/api/vehicles/{id}` | Delete vehicle |

# RideLink Backend - 4-Member Microservices System

A complete microservices-based ride-sharing backend built with **Spring Boot 3**, **Spring Data MongoDB**, **JWT Authentication**, and **Docker**.




##  Team & Service Folder Breakdown

Each team member owns an isolated microservice folder designed for independent development and deployment:

| Service Folder | Member Owner | Default Port | Description |
| :--- | :--- | :--- | :--- |
| **`account-service/`** | **Member 1 IT24100103 - G.K.A.N.L.Kariyawasam** | `8081` | Authentication (JWT), User registration, Passenger & Driver profiles |
| **`driver-service/`** | **Member 2 IT24101690 - S.A.C.Herath** | `8082` | Driver availability, vehicle management, and geolocation updates |
| **`ride-service/`** | **Member 3 IT24102930 - R.Gangeshwaran** | `8083` | Ride request lifecycle, driver matching orchestrator, and status updates |
| **`payment-service/`** | **Member 4 IT24102198 - K.L.W.A.Ranhalida** | `8084` | Fare estimation calculation, payment processing, and digital receipts |

---

##  Repository Structure

```text
ridelink-backend/                         # Root Git Repository
│
├── .github/workflows/                    # CI Pipeline configuration (Shared)
├── docker-compose.yml                    # Local MongoDB & microservices configuration
├── pom.xml                               # Root parent POM
└── README.md                             # Setup instructions and endpoint details
│
├── account-service/                      # [Member 1's IT24100103 - G.K.A.N.L.KariyawasamKariyawasam] Account Service
│   ├── src/main/java/com/ridelink/account/
│   │   ├── controller/                   # REST Controllers (Register, Login, Profile)
│   │   ├── service/                      # Auth, Role, User logic
│   │   ├── repository/                   # MongoDB Repositories
│   │   ├── model/                        # User, Role documents
│   │   ├── dto/                          # Request/Response payloads
│   │   ├── security/                     # JWT filter & Spring Security config
│   │   └── AccountServiceApplication.java
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── driver-service/                       # [Member 2's IT24101690 - S.A.C.Herath] Driver & Vehicle Service
│   ├── src/main/java/com/ridelink/driver/
│   │   ├── controller/                   # Driver profile & vehicle APIs
│   │   ├── service/                      # Location & availability logic
│   │   ├── repository/                   # Driver & Vehicle MongoDB Repositories
│   │   ├── model/                        # Driver, Vehicle documents
│   │   ├── dto/                          # Status & Location DTOs
│   │   └── DriverServiceApplication.java
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── ride-service/                         # [Member 3's IT24102930 - R.Gangeshwaran] Ride Management Service
│   ├── src/main/java/com/ridelink/ride/
│   │   ├── controller/                   # Ride booking & status APIs
│   │   ├── service/                      # Ride lifecycle management
│   │   ├── repository/                   # Ride transaction MongoDB Repositories
│   │   ├── model/                        # Ride & Location documents
│   │   ├── client/                       # Inter-service REST clients (Account, Driver, Payment)
│   │   ├── dto/                          # Ride request & response DTOs
│   │   └── RideServiceApplication.java
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
└── payment-service/                      # [Member 4's IT24102198 - K.L.W.A.Ranhalida] Fare & Payment Service
    ├── src/main/java/com/ridelink/payment/
    │   ├── controller/                   # Fare estimation & payment APIs
    │   ├── service/                      # Fare calculation & receipt logic
    │   ├── repository/                   # Payment & Receipt MongoDB Repositories
    │   ├── model/                        # FareRule, Payment, Receipt documents
    │   ├── dto/                          # Estimation & transaction DTOs
    │   └── PaymentServiceApplication.java
    ├── src/main/resources/application.properties
    ├── Dockerfile
    └── pom.xml
```

---

## Getting Started

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose**

### Running with Docker Compose
Start MongoDB and all 4 microservices simultaneously:
```bash
docker-compose up --build
```

### Running a Single Service Locally
Each member can navigate to their service folder and run:
```bash
cd account-service
mvn spring-boot:run
```

---

## Key Endpoints Summary

### Member 1 [IT24100103 - G.K.A.N.L.Kariyawasam]: Account Service (`http://localhost:8081`)
- `POST /api/auth/register` - Register a new user (RIDER or DRIVER)
- `POST /api/auth/login` - Authenticate and get JWT token
- `GET /api/users/{id}` - Get user profile

### Member 2 [IT24101690 - S.A.C.Herath]: Driver Service (`http://localhost:8082`)
- `POST /api/drivers` - Register or update driver profile
- `PUT /api/drivers/{id}/availability` - Toggle ONLINE / OFFLINE / BUSY
- `PUT /api/drivers/{id}/location` - Update GPS location coordinates
- `GET /api/drivers/available` - Find nearby online drivers

### Member 3 [IT24102930 - R.Gangeshwaran]: Ride Service (`http://localhost:8083`)
- `POST /api/rides/request` - Request a new ride
- `GET /api/rides/{id}` - Get ride details and tracking status
- `PUT /api/rides/{id}/status` - Update status (`ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)

### Member 4 [IT24102198 - K.L.W.A.Ranhalida]: Payment Service (`http://localhost:8084`)
- `POST /api/fare/estimate` - Estimate ride fare based on distance & duration
- `POST /api/payments/process` - Process ride payment
- `GET /api/payments/receipt/{rideId}` - Get digital receipt

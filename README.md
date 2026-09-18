# RideLink — Backend Microservices Platform

**Module**: IT3130 – Application Development | Group Assignment (30%)  
**Architecture**: Microservices Architecture (Node.js, Express, MongoDB, RabbitMQ, Docker)

---

## Scenario Summary
RideLink is a scalable, resilient ride-sharing backend platform engineered using a decentralized microservices architecture. The system orchestrates account authentication, driver onboarding and vehicle verification, real-time ride matching and lifecycle tracking, and dynamic fare computation with payment settlement. Each core service operates with strict database-per-service isolation and communicates through synchronous RESTful interfaces and asynchronous message queues via RabbitMQ.

---

## Service Decomposition & Ownership Matrix

| # | Service Name | Directory | Default Port | Primary Owner | Database Ownership |
|---|:---|:---|:---:|:---|:---|
| 1 | **Account Service** | `services/account-service` | `5001` | Member 1 | Dedicated MongoDB (Member 1 Atlas/Local) |
| 2 | **Driver & Vehicle Service** | `services/driver-service` | `5002` | Member 2 | Dedicated MongoDB (Member 2 Atlas/Local) |
| 3 | **Ride Management Service** | `services/ride-service` | `5003` | Member 3 | Dedicated MongoDB (Member 3 Atlas/Local) |
| 4 | **Fare & Payment Service** | `services/payment-service` | `5004` | Member 4 | Dedicated MongoDB (Member 4 Atlas/Local) |
| — | **API Gateway** | `gateway` | `5000` | Shared / Infrastructure | Stateless Reverse Proxy & Routing |

> **Note**: Per assignment specifications, the API Gateway is supporting infrastructure and does not count as one of the four core business microservices.

---

## Critical Note on Database Configuration & Secrets

> [!IMPORTANT]
> **Each team member configures their own `.env` inside their service folder with their own MongoDB connection URL.**
>
> - **Strict Database Isolation**: No service may query, connect to, or alter another service's database.
> - **Zero Committed Secrets**: `.env` files are ignored by git in `.gitignore`. **NEVER commit a real `.env` file or secret to GitHub.**
> - Use the provided `.env.example` templates in each folder to configure your environment variables.

---

## Prerequisites
- **Node.js**: `v20.x LTS`
- **npm**: `v10.x` or higher
- **Docker & Docker Compose**: Docker Desktop 4.x+ (optional for local standalone, required for multi-container orchestration)
- **MongoDB**: MongoDB Atlas cluster or local MongoDB instance (one instance/URL per member/service)

---

## Getting Started

### 1. Repository Setup
```bash
# Clone the repository
git clone <repository-url>
cd ridelink-backend

# Install all workspace dependencies from the root
npm install
```

### 2. Environment Configuration
Copy the `.env.example` in each service folder and fill in your dedicated database connection strings and configuration:
```bash
cp services/account-service/.env.example services/account-service/.env
cp services/driver-service/.env.example services/driver-service/.env
cp services/ride-service/.env.example services/ride-service/.env
cp services/payment-service/.env.example services/payment-service/.env
cp gateway/.env.example gateway/.env
```

### 3. Recommended Startup Order
When booting services manually or during end-to-end integration:
1. **RabbitMQ Broker** (`amqp://localhost:5672`, Management UI at `http://localhost:15672`)
2. **Account Service** (`http://localhost:5001`)
3. **Driver & Vehicle Service** (`http://localhost:5002`)
4. **Ride Management Service** (`http://localhost:5003`)
5. **Fare & Payment Service** (`http://localhost:5004`)
6. **API Gateway** (`http://localhost:5000`)

---

## Running the System

### Option A: Docker Compose (Full Stack Orchestration)
```bash
# Start all microservices, gateway, and RabbitMQ
docker compose up --build

# Stop all containers
docker compose down
```

### Option B: Running Individual Services Standalone
```bash
# Run a specific service in development mode from root using workspaces:
npm run dev:account
npm run dev:driver
npm run dev:ride
npm run dev:payment
npm run dev:gateway
```

---

## Testing & Quality Assurance
```bash
# Run unit & integration tests across all workspaces
npm test

# Run tests in a specific service folder
cd services/account-service && npm test
```

---

## Documentation & API References

- **Architecture Diagrams**: [`docs/architecture/architecture-diagram.md`](file:///c:/Users/U/Desktop/test/docs/architecture/architecture-diagram.md)
- **Sequence Diagrams**: [`docs/architecture/sequence-diagram.md`](file:///c:/Users/U/Desktop/test/docs/architecture/sequence-diagram.md)
- **Async Event Contracts**: [`docs/async-contracts/README.md`](file:///c:/Users/U/Desktop/test/docs/async-contracts/README.md)
- **Postman Collections**: [`docs/postman/README.md`](file:///c:/Users/U/Desktop/test/docs/postman/README.md)

### Swagger UI Documentation (Links to be activated upon endpoint implementation)
- **Account Service**: `http://localhost:5001/api-docs`
- **Driver Service**: `http://localhost:5002/api-docs`
- **Ride Service**: `http://localhost:5003/api-docs`
- **Payment Service**: `http://localhost:5004/api-docs`
- **API Gateway Aggregated Docs**: `http://localhost:5000/api-docs`

---

## Sample Credentials & Test Fixtures
*(To be populated during Step 2–5 with mock accounts, driver IDs, and test tokens)*

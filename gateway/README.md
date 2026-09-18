# RideLink - API Gateway

## Overview
- **Service Name**: `gateway`
- **Port**: `5000`
- **Role**: Reverse proxy, unified entry point, and future rate limiting/routing layer for the 4 core business microservices.

> **Note**: Per assignment guidelines, the API Gateway serves as supporting infrastructure and does not count as one of the four core business microservices.

## Downstream Microservices
| Microservice | Default Local URL |
| :--- | :--- |
| **Account Service** | `http://localhost:5001` |
| **Driver & Vehicle Service** | `http://localhost:5002` |
| **Ride Management Service** | `http://localhost:5003` |
| **Fare & Payment Service** | `http://localhost:5004` |

## Environment Variables
Create a local `.env` file based on `.env.example`:

```env
PORT=5000
ACCOUNT_SERVICE_URL=http://localhost:5001
DRIVER_SERVICE_URL=http://localhost:5002
RIDE_SERVICE_URL=http://localhost:5003
PAYMENT_SERVICE_URL=http://localhost:5004
```

## Running Standalone

### Local Development
```bash
# Install dependencies
npm install

# Start in development mode with nodemon
npm run dev

# Run tests
npm test
```

### Docker
```bash
# Build image
docker build -t ridelink-gateway .

# Run container
docker run -p 5000:5000 --env-file .env ridelink-gateway
```

## Endpoints
- `GET /health` - API Gateway health check and downstream routing table
- `/api/v1/accounts/*` - Forwarded to Account Service
- `/api/v1/drivers/*` - Forwarded to Driver & Vehicle Service
- `/api/v1/rides/*` - Forwarded to Ride Management Service
- `/api/v1/payments/*` - Forwarded to Fare & Payment Service

# RideLink - Ride Management Service

## Service Overview
- **Primary Owner**: Member 3
- **Service Name**: `ride-service`
- **Port**: `5003`
- **Description**: Handles ride booking, driver matching orchestration, ride lifecycle state transitions (REQUESTED, ACCEPTED, ONGOING, COMPLETED, CANCELLED), and asynchronous event publishing.

## Database Isolation
Per microservices architectural principles and assignment requirements, this service owns its database exclusively. No other service may query or alter the Ride Management database directly.

## Inter-Service Communication
- **Synchronous HTTP (REST via `src/clients`)**: Queries `driver-service` for available drivers and calls `payment-service` for fare estimates / payment status where needed.
- **Asynchronous Messaging (RabbitMQ)**: Publishes lifecycle events such as `RideCompletedEvent` to the message broker.

## Environment Variables
Create a local `.env` file based on `.env.example`:

```env
PORT=5003
MONGO_URI=mongodb+srv://<username>:<password>@<cluster-m3>.mongodb.net/ridelink_ride_db?retryWrites=true&w=majority
RABBITMQ_URL=amqp://guest:guest@localhost:5672
DRIVER_SERVICE_URL=http://localhost:5002
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
docker build -t ridelink-ride-service .

# Run container
docker run -p 5003:5003 --env-file .env ridelink-ride-service
```

## Endpoints
- `GET /health` - Service health status check

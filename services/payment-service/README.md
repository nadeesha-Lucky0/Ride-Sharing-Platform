# RideLink - Fare & Payment Service

## Service Overview
- **Primary Owner**: Member 4
- **Service Name**: `payment-service`
- **Port**: `5004`
- **Description**: Manages dynamic fare estimation, transaction settlement, payment gateway simulation, and event-driven payment confirmation.

## Database Isolation
Per microservices architectural principles and assignment requirements, this service owns its database exclusively. No other service may query or alter the Fare & Payment database directly.

## Asynchronous Event Handling
- Subscribes to events (such as `RideCompletedEvent` via RabbitMQ in `src/events/`).
- Publishes payment status events (such as `PaymentRecordedEvent`).

## Environment Variables
Create a local `.env` file based on `.env.example`:

```env
PORT=5004
MONGO_URI=mongodb+srv://<username>:<password>@<cluster-m4>.mongodb.net/ridelink_payment_db?retryWrites=true&w=majority
RABBITMQ_URL=amqp://guest:guest@localhost:5672
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
docker build -t ridelink-payment-service .

# Run container
docker run -p 5004:5004 --env-file .env ridelink-payment-service
```

## Endpoints
- `GET /health` - Service health status check

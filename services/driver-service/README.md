# RideLink - Driver & Vehicle Service

## Service Overview
- **Primary Owner**: Member 2
- **Service Name**: `driver-service`
- **Port**: `5002`
- **Description**: Manages driver registration, vehicle details verification, availability status, and geolocation/matching readiness.

## Database Isolation
Per microservices architectural principles and assignment requirements, this service owns its database exclusively. No other service may query or alter the Driver & Vehicle database directly.

## Environment Variables
Create a local `.env` file based on `.env.example`:

```env
PORT=5002
MONGO_URI=mongodb+srv://<username>:<password>@<cluster-m2>.mongodb.net/ridelink_driver_db?retryWrites=true&w=majority
JWT_SECRET=your_driver_jwt_secret_key_here
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
docker build -t ridelink-driver-service .

# Run container
docker run -p 5002:5002 --env-file .env ridelink-driver-service
```

## Endpoints
- `GET /health` - Service health status check

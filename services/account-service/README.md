# RideLink - Account Service

## Service Overview
- **Primary Owner**: Member 1
- **Service Name**: `account-service`
- **Port**: `5001`
- **Description**: Handles passenger account management, user authentication, profile lifecycle, and JWT token issuance.

## Database Isolation
Per microservices architectural principles and assignment requirements, this service owns its database exclusively. No other service may query or alter the Account database directly.

## Environment Variables
Create a local `.env` file based on `.env.example`:

```env
PORT=5001
MONGO_URI=mongodb+srv://<username>:<password>@<cluster-m1>.mongodb.net/ridelink_account_db?retryWrites=true&w=majority
JWT_SECRET=your_account_jwt_secret_key_here
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
docker build -t ridelink-account-service .

# Run container
docker run -p 5001:5001 --env-file .env ridelink-account-service
```

## Endpoints
- `GET /health` - Service health status check

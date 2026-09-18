const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;

const ACCOUNT_SERVICE_URL = process.env.ACCOUNT_SERVICE_URL || 'http://localhost:5001';
const DRIVER_SERVICE_URL = process.env.DRIVER_SERVICE_URL || 'http://localhost:5002';
const RIDE_SERVICE_URL = process.env.RIDE_SERVICE_URL || 'http://localhost:5003';
const PAYMENT_SERVICE_URL = process.env.PAYMENT_SERVICE_URL || 'http://localhost:5004';

// Middleware
app.use(cors());
app.use(express.json());

// Gateway health check
app.get('/health', (req, res) => {
  res.status(200).json({
    service: 'api-gateway',
    status: 'up',
    timestamp: new Date().toISOString(),
    routes: {
      accounts: ACCOUNT_SERVICE_URL,
      drivers: DRIVER_SERVICE_URL,
      rides: RIDE_SERVICE_URL,
      payments: PAYMENT_SERVICE_URL
    }
  });
});

// Placeholder Route Definitions (Proxying middleware will be hooked here)
app.use('/api/v1/accounts', (req, res) => {
  res.status(501).json({
    message: 'Account Service route scaffold placeholder',
    target: ACCOUNT_SERVICE_URL,
    path: req.originalUrl
  });
});

app.use('/api/v1/drivers', (req, res) => {
  res.status(501).json({
    message: 'Driver Service route scaffold placeholder',
    target: DRIVER_SERVICE_URL,
    path: req.originalUrl
  });
});

app.use('/api/v1/rides', (req, res) => {
  res.status(501).json({
    message: 'Ride Service route scaffold placeholder',
    target: RIDE_SERVICE_URL,
    path: req.originalUrl
  });
});

app.use('/api/v1/payments', (req, res) => {
  res.status(501).json({
    message: 'Payment Service route scaffold placeholder',
    target: PAYMENT_SERVICE_URL,
    path: req.originalUrl
  });
});

// Server bootstrap
const server = app.listen(PORT, () => {
  console.log(`[API Gateway] Running on port ${PORT}`);
  console.log(`[API Gateway] Configured Downstream Services:`);
  console.log(`  - Account Service: ${ACCOUNT_SERVICE_URL}`);
  console.log(`  - Driver Service:  ${DRIVER_SERVICE_URL}`);
  console.log(`  - Ride Service:    ${RIDE_SERVICE_URL}`);
  console.log(`  - Payment Service: ${PAYMENT_SERVICE_URL}`);
});

module.exports = { app, server };

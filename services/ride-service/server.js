const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5003;
const MONGO_URI = process.env.MONGO_URI;

// Middleware
app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    service: 'ride-service',
    status: 'up',
    timestamp: new Date().toISOString()
  });
});

// Database connection
if (MONGO_URI) {
  mongoose
    .connect(MONGO_URI)
    .then(() => {
      console.log('[Ride Service] Connected to dedicated MongoDB instance');
    })
    .catch((err) => {
      console.error('[Ride Service] MongoDB connection error:', err.message);
    });
} else {
  console.warn('[Ride Service] Warning: MONGO_URI is not defined in environment variables.');
}

// Server bootstrap
const server = app.listen(PORT, () => {
  console.log(`[Ride Service] Running on port ${PORT}`);
});

module.exports = { app, server };

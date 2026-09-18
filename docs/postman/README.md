# RideLink - Postman Collections & Environments

## Overview
This directory serves as the repository for shared Postman API testing artifacts.

## Contents (To be added during service implementation)
- `RideLink_API_Gateway.postman_collection.json` - Complete request collection covering all gateway routes and service endpoints.
- `RideLink_Local.postman_environment.json` - Environment configuration containing `baseUrl` (`http://localhost:5000`), JWT tokens, and sample entity IDs.
- `RideLink_Docker.postman_environment.json` - Environment configuration for multi-container Docker testing.

## Guidelines
- Ensure all sensitive data (passwords, JWT secrets, real database credentials) are scrubbed before exporting and committing Postman artifacts.
- Use Postman dynamic environment variables (e.g. `{{jwt_token}}`, `{{ride_id}}`) for chained endpoint testing.

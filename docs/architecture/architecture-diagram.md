# RideLink - Architecture Diagram

## High-Level Microservices Architecture

The diagram below illustrates the 4 core business microservices, their supporting API Gateway, dedicated database ownership per service, and the asynchronous messaging broker.

```mermaid
graph TD
    Client["Client / API Consumer (Postman / Swagger)"]

    subgraph Infrastructure ["Supporting Infrastructure"]
        Gateway["API Gateway (:5000)"]
        RabbitMQ[("RabbitMQ Broker (:5672)")]
    end

    subgraph CoreServices ["Core Business Microservices"]
        AccountService["Account Service (:5001)<br/>Owner: Member 1"]
        DriverService["Driver & Vehicle Service (:5002)<br/>Owner: Member 2"]
        RideService["Ride Management Service (:5003)<br/>Owner: Member 3"]
        PaymentService["Fare & Payment Service (:5004)<br/>Owner: Member 4"]
    end

    subgraph Databases ["Independent Dedicated Databases (DB-per-service)"]
        AccountDB[(MongoDB Account DB<br/>Cluster M1)]
        DriverDB[(MongoDB Driver DB<br/>Cluster M2)]
        RideDB[(MongoDB Ride DB<br/>Cluster M3)]
        PaymentDB[(MongoDB Payment DB<br/>Cluster M4)]
    end

    %% Client Routing
    Client -->|HTTP REST| Gateway

    %% Gateway Routing
    Gateway -->|/api/v1/accounts| AccountService
    Gateway -->|/api/v1/drivers| DriverService
    Gateway -->|/api/v1/rides| RideService
    Gateway -->|/api/v1/payments| PaymentService

    %% Synchronous Inter-Service Calls
    RideService -.->|HTTP GET /drivers/available| DriverService
    RideService -.->|HTTP POST /payments/estimate| PaymentService

    %% Asynchronous Event Flows
    RideService ==>|Publish RideCompletedEvent| RabbitMQ
    RabbitMQ ==>|Consume RideCompletedEvent| PaymentService
    RabbitMQ ==>|Consume RideCompletedEvent| DriverService

    %% Dedicated Database Connections
    AccountService --- AccountDB
    DriverService --- DriverDB
    RideService --- RideDB
    PaymentService --- PaymentDB
```

> **TODO**: Refine specific internal routing rules, exchange names, and queue configurations during individual service implementation phases.

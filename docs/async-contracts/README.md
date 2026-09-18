# Asynchronous Event Contracts

## Purpose
This directory documents the asynchronous event contracts exchanged via RabbitMQ across the RideLink microservices. Each contract specification establishes a clear protocol between publishers and consumers.

## Structure
During Steps 4–5 of development, a dedicated Markdown file must be created for each event (e.g., `RideCompletedEvent.md`, `PaymentRecordedEvent.md`, `DriverAssignedEvent.md`).

### Contract Specification Template
Each contract document must define:
1. **Event Name & Routing Key**: e.g., `ride.completed`
2. **Exchange Name & Type**: e.g., `ridelink.topic (topic)`
3. **Publisher**: The emitting microservice (e.g., `ride-service`)
4. **Consumer(s)**: The subscribing microservice(s) (e.g., `payment-service`, `driver-service`)
5. **Trigger Condition**: Business event triggering publication
6. **Payload JSON Schema**: Exact payload attributes, data types, and required fields.

---

### Planned Event Contracts (To be drafted in follow-up phases)
- `RideRequestedEvent.md`
- `RideAcceptedEvent.md`
- `RideCompletedEvent.md`
- `PaymentRecordedEvent.md`

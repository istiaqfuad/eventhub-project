# EventHub

EventHub is a modern, scalable event management and booking platform built with Spring Boot and React. It provides robust features for event organizers and attendees, including real-time seat holds, transactional outbox pattern, rate limiting, and a virtual waiting room for high-demand events.

## 🚀 Live Demo

**Backend URL:** [http://213.136.80.53:8080](http://213.136.80.53:8080)
**Frontend URL:** [http://213.136.80.53:3000](http://213.136.80.53:3000)

## 🛠 Features

* **Advanced Booking System:** Concurrent seat booking with optimistic locking and bounded retry mechanisms to prevent conflicts.
* **Virtual Waiting Room:** A fair queuing system backed by Redis Sorted Sets (ZSET) to handle traffic spikes for high-demand events.
* **Resilient Infrastructure:**
  * **Caching & Rate Limiting:** Redis-backed sliding window rate limiter via Lua scripts to protect critical APIs.
  * **Transactional Outbox & Messaging:** Guarantees eventual consistency using PostgreSQL outbox polling and RabbitMQ event streaming.
* **Background Processing:** Background consumers for asynchronous tasks like Analytics aggregation and Elasticsearch syncing.
* **Search:** Fast, event-driven syncing of event data to Elasticsearch for robust search capabilities.
* **Security & Auth:** Secure JWT-based authentication with role-based access control (Admin, Organizer, Customer).

## 💻 Tech Stack

* **Backend**: Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, Spring Data Redis, Spring Retry, RabbitMQ (AMQP), Elasticsearch
* **Database**: PostgreSQL (Neon Serverless), Redis (Caching & Rate Limiting), Elasticsearch (Search)
* **Message Broker**: RabbitMQ
* **Frontend**: React (Next.js/Vite based on configuration), Tailwind CSS
* **Deployment**: Docker, Docker Compose, Dokploy

## 📂 Project Structure

* `/eventhub`: The core Spring Boot backend application.
* `/eventhub-frontend`: The React frontend application.

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- JDK 21
- Node.js (for frontend)

### Running Locally

1. **Start the Infrastructure**
   ```bash
   docker-compose up -d postgres redis rabbitmq elasticsearch
   ```

2. **Run the Backend**
   ```bash
   cd eventhub
   ./mvnw spring-boot:run
   ```

3. **Run the Frontend**
   ```bash
   cd eventhub-frontend
   npm install
   npm run dev
   ```

## 🏗 System Architecture Highlight
- **Booking Flow**: Uses Redis TTL for temporary seat holds. Upon confirmation, Stripe Webhook updates the booking state, and the Outbox service records the `BookingConfirmed` event which is relayed to RabbitMQ to update `EventStat` and `DailySale` asynchronously.
- **Search Syncing**: `EventCreated`, `EventUpdated`, and `EventDeleted` events are pushed to RabbitMQ. The `EventSearchSyncConsumer` independently indexes the updated data into Elasticsearch.

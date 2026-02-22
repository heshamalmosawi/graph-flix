# GraphFlix

A movie recommendation platform built with Spring Boot microservices, Angular, and Neo4j graph database.

## Architecture

| Service | Description | Port |
|---|---|---|
| **API Gateway** | Routes traffic to backend services (HTTPS) | 8443 |
| **Eureka Server** | Service discovery registry | 8761 |
| **User Service** | Auth, registration, JWT, 2FA (TOTP), watchlist | -- |
| **Movie Service** | Movie catalog CRUD | -- |
| **Rating Service** | Per-user movie ratings, averages | -- |
| **Recommendation Service** | Graph-based movie recommendations | -- |
| **Frontend** | Angular SPA | 2122 |
| **Kafka** | Event bus between user-service and ratingservice | 9092 |

Backend services register with Eureka and are accessed through the API Gateway. No fixed host ports are exposed for individual services.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Cloud 2025.0
- **Frontend:** Angular 20, Angular Material, TypeScript 5.8
- **Database:** Neo4j (graph database)
- **Messaging:** Apache Kafka (Confluent 7.4)
- **Auth:** JWT (jjwt 0.12.6), Google Authenticator TOTP for 2FA
- **Infra:** Docker Compose, Netflix Eureka, TLS/SSL (PKCS12)

## API Routes

All traffic goes through the API Gateway at `https://localhost:8443`.

| Route | Service |
|---|---|
| `/users/**` | user-service |
| `/movies/**` | movie-service |
| `/ratings/**` | rating-service |
| `/watchlist/**` | user-service |
| `/recommendations/**` | recommendation-service |

## Prerequisites

- Docker and Docker Compose
- Neo4j instance (configure connection in each service's `.env` file)

## Running

```bash
docker compose up --build
```

Services start in dependency order: Zookeeper -> Kafka -> Eureka -> microservices -> API Gateway -> Frontend.

Each backend service requires a `.env` file with Neo4j credentials and any service-specific config. See individual service directories for details.

## Data Model

Neo4j graph with three node types:

- **Movie** -- title (unique), released, tagline
- **Person** -- name (unique), born
- **User** -- id, name, email (unique), password, createdAt

Relationships: `ACTED_IN`, `DIRECTED`, `PRODUCED`, `WROTE`, `REVIEWED`, `RATED` (among others defined in `DATASET_SPEC.md`).

## Project Structure

```
backend/
  apigateway/          API Gateway (Spring Cloud Gateway)
  eureka-service-discovery/  Eureka Server
  movieservice/        Movie catalog service
  user-service/        Auth and user management
  ratingservice/       Rating service
  recommendationservice/ Recommendation engine
frontend/              Angular 20 SPA
kafka/                 Kafka Docker Compose and topic scripts
```

## License

This project is for educational and portfolio purposes.

# Your Car Your Way - Real-Time Chat PoC 🚗💬

[![CI/CD Pipeline](https://github.com/MaxLeblc/yourCarYourWay_chat/actions/workflows/ci.yml/badge.svg)](https://github.com/MaxLeblc/yourCarYourWay_chat/actions)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen?logo=springboot)
![Angular](https://img.shields.io/badge/Angular-22-red?logo=angular)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)

## Overview

This repository contains the **Proof of Concept (PoC)** for the real-time customer support chat module of **"Your Car Your Way"**, a modern car rental SaaS platform.

The PoC demonstrates bi-directional, persistent real-time messaging between customers and agency support representatives, built with modern architecture standards:
- **Real-Time Communication**: WebSocket over STOMP protocol for low-latency messaging.
- **Relational Data Persistence**: PostgreSQL 16 storing chat message history and domain relations.
- **Modern Angular Frontend**: Standalone Angular SPA with dark theme UI and automatic SockJS fallback.
- **Enterprise Security & Compliance**: Zero hardcoded secrets, Spring Security RBAC-ready integration, isolated Docker networks.
- **Automated Quality Gate**: GitHub Actions CI/CD pipeline running backend (JUnit 5 + Mockito) and frontend (Vitest) test suites on every push and Pull Request.

---

## Architecture & Stack

```
                     ┌────────────────────────┐
                     │   Angular 22 SPA UI    │
                     │ (Port 4200 / Static)   │
                     └───────────┬────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │ HTTP REST / STOMP WebSocket   │
                 └───────────────┬───────────────┘
                                 ▼
                     ┌────────────────────────┐
                     │  Spring Boot 4 Backend │
                     │   (Port 8080 - Java 21)│
                     └───────────┬────────────┘
                                 │
                        SQL / Spring Data JPA
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ PostgreSQL 16 Database │
                     │   (Docker - Port 5432) │
                     └────────────────────────┘
```

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Backend** | Java 21, Spring Boot 4, Spring WebSocket, Spring Data JPA, Spring Security | Core REST & STOMP Broker API |
| **Frontend** | Angular 22, `@stomp/stompjs`, `sockjs-client`, RxJS, Vitest | Reactive Single Page Application |
| **Database** | PostgreSQL 16 Alpine (via Docker Compose) | Relational message persistence |
| **CI/CD** | GitHub Actions (JDK 21, Node 22) | Automated build & unit test quality gate |

---

## Prerequisites

Before running the project locally, ensure you have installed:
- **Java OpenJDK 21** (`java -version`)
- **Node.js 20+ / 22** (`node -v`) & `npm`
- **Docker** & **Docker Compose** (`docker compose version`)
- **Git**

---

## Quick Start

### 1. Environment Setup

Clone the repository and copy the environment template:

```bash
git clone git@github.com:MaxLeblc/yourCarYourWay_chat.git
cd yourCarYourWay_chat
cp .env.example .env
```

*Note: Update `.env` with your desired PostgreSQL credentials if needed.*

### 2. Start PostgreSQL Database

Launch the isolated PostgreSQL 16 container:

```bash
docker compose up -d
```

Verify that PostgreSQL is healthy on port `5432`:

```bash
docker compose ps
```

### 3. Start Backend (Spring Boot)

Run the backend server using the root startup script:

```bash
./start-backend.sh
```

*Or manually:*
```bash
cd backend/chatPOC
./mvnw spring-boot:run
```
*The Spring Boot server will start on `http://localhost:8080`.*

### 4. Start Frontend (Angular UI)

In a separate terminal, launch the Angular development server:

```bash
./start-frontend.sh
```

*Or manually:*
```bash
cd frontend/chatUI
npm install
npm start
```
*Open `http://localhost:4200` in your web browser to start chatting!*

---

## Running Tests

### Backend Unit & Integration Tests (JUnit 5 + Mockito + H2)

```bash
cd backend/chatPOC
./mvnw clean test
```

### Frontend Unit Tests (Vitest)

```bash
cd frontend/chatUI
npx vitest run
```

---

## CI/CD Pipeline (GitHub Actions)

The automated workflow (`.github/workflows/ci.yml`) runs on every push and Pull Request to `main`:

- **`Backend Build & Unit Tests`**: Validates Spring Boot compilation and runs JUnit 5 / Mockito unit tests with an in-memory H2 database.
- **`Frontend Build & Unit Tests`**: Installs dependencies, runs Vitest unit tests, and verifies production Angular compilation.

---

## Repository Structure

```
yourCarYourWay_chat/
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI/CD Pipeline
├── backend/
│   └── chatPOC/                 # Spring Boot 4 Application
│       ├── src/main/java/       # WebSocket Config, Controllers, Services, JPA Entities
│       ├── src/main/resources/  # application.yaml & DB configurations
│       └── src/test/            # JUnit 5 & Mockito test suites (H2 in-memory DB)
├── frontend/
│   └── chatUI/                  # Angular 22 Application
│       ├── src/app/             # Chat Component, ChatService (STOMP), Models
│       └── src/app/app.spec.ts  # Vitest unit test suite
├── .env.example                 # Template for database environment variables
├── docker-compose.yml           # PostgreSQL 16 container setup with bridge network
├── start-backend.sh             # Helper script to launch Spring Boot backend
├── start-frontend.sh            # Helper script to launch Angular frontend
└── README.md                    # Project documentation
```

---

## License

This project is for educational purposes.

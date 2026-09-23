# QA Automation Framework

End-to-end and API test automation framework for [Gitea](https://about.gitea.com/),
a self-hosted Git service, built with Java, Playwright and JUnit.

The goal of this project is to demonstrate a maintainable, scalable test framework:
UI tests, API tests, API-driven test data setup, reporting and CI.

## Tech stack

- Java 25
- Playwright for Java
- JUnit 6
- Maven
- Docker Compose (Gitea test environment)

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (Docker Desktop or OrbStack)

## Getting started

### 1. Start the test environment

    docker compose up -d

Gitea will be available at http://localhost:3000.

### 2. Create the admin user

    docker exec -u git gitea gitea admin user create --admin \
      --username gitea_admin --password 'Admin123!' \
      --email admin@example.com --must-change-password=false

> The environment is disposable: `docker compose down` removes all data,
> including the admin user. Use `docker compose stop` to keep data between sessions.

### 3. Run the tests

    mvn test

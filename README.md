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

## Framework design

### Project structure

    src/test/java/io/github/asuujx/
    ├── ui/
    │   └── pages/    Page objects: one class per page, locators + user actions
    └── tests/
        └── ui/       UI test classes: test flow and assertions

Framework code (`ui/`) and tests (`tests/`) are kept in separate packages.
Tests depend on page objects, never the other way around.

### Page Object Model rules

1. **Page objects receive Playwright `Page` in the constructor and don't manage the browser.**
   Why: <!-- TODO -->
2. **Locators are private fields, initialized once in the constructor.**
   Why: <!-- TODO -->
3. **Methods express user intent** (e.g. `loginAs(username, password)`), not low-level steps.
   Why: <!-- TODO -->
4. **Navigation methods return the next page object** (e.g. `loginAs` → `DashboardPage`).
   Why: <!-- TODO -->
5. **No assertions in page objects.** Tests assert using `PlaywrightAssertions`.
   Why: <!-- TODO -->

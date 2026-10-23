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
- Allure Report
- Docker Compose (Gitea test environment)

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (Docker Desktop or OrbStack)
- Node.js 20+ (only for viewing the Allure report)

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

### 4. View the test report

    npx allure@3.20.0 open target/allure-results

This generates an Allure report from the latest test run and opens it in the browser.

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

   The test owns the browser lifecycle (launch, context, cleanup). Page objects stay
   reusable across tests and share one session, so a login in one page object is
   visible to the next. Isolation and parallel runs are handled in one place.

2. **Locators are private fields, initialized once in the constructor.**

   Each selector is defined in exactly one place. When the UI changes, only the page
   object changes. Tests can't depend on page internals; they use the page's public
   methods instead.

3. **Methods express user intent** (e.g. `loginAs(username, password)`), not low-level steps.

   Tests read like user scenarios, and the steps of a flow live in one method. If the
   login form gains a field, one method changes instead of every test that logs in.

4. **Navigation methods return the next page object** (e.g. `loginAs` → `DashboardPage`).

   The return type documents where an action leads, and it makes flows easy to chain.
   The compiler rejects code that calls dashboard actions while still on the login page.

5. **No assertions in page objects.** Tests assert using `PlaywrightAssertions`.

   Page objects describe what the page *can do*. Tests decide what the *expected
   outcome* is. That lets the same page object serve both positive and negative tests,
   and a failing test points straight at the check that failed.

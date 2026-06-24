## Context

The project already has a working Selenide + Serenity BDD + JUnit 5 test framework. Sample tests target JetBrains.com. The goal is to point the framework at a real Shopware instance running locally via Docker, and add an initial smoke test suite that verifies the storefront is functional end-to-end.

Dockware (`dockware/dev`) is a community-maintained Docker image that ships a fully configured Shopware 6 instance ready to run with a single `docker compose up`. It exposes the storefront on port 80 by default.

## Goals / Non-Goals

**Goals:**
- Start Shopware via dockware automatically from within the test using Testcontainers — no manual Docker commands or separate Gradle task required
- Remove `MainPageTest`, `MainPage`, and any other JetBrains sample files that are no longer relevant
- Implement a `StorefrontPage` page object covering the Shopware storefront homepage, navigation, and search bar
- Implement a `StorefrontSmokeTest` with three smoke scenarios: homepage loads, category navigation works, search returns results

**Non-Goals:**
- A `docker-compose.yml` for local manual use (Testcontainers replaces this need)
- A Gradle task that starts the Docker container
- Admin/backend UI tests
- Checkout or payment flow tests
- CI pipeline integration (deferred)
- Testing against a remote/cloud Shopware instance

## Decisions

### Use dockware/dev image
**Decision:** Use `dockware/dev:latest` as the Shopware container.  
**Rationale:** dockware is purpose-built for Shopware development/testing, includes demo data, and starts without any additional configuration. Alternative would be building a custom Shopware image from scratch, which adds significant maintenance overhead.  
**Alternatives considered:** Official Shopware Docker setup (requires separate MySQL + Elasticsearch containers and manual install steps).

### Start container via Testcontainers, not a Gradle task or docker-compose
**Decision:** Use Testcontainers (`org.testcontainers:testcontainers`) to start the dockware container from within the test's `@BeforeAll`.  
**Rationale:** Testcontainers keeps the container lifecycle coupled to the test run — no manual `docker compose up`, no Gradle task, and no leftover containers after the test. The dynamic port assigned by Testcontainers is fed directly into `Configuration.baseUrl`, so port 80 conflicts on the developer machine are also eliminated.  
**Alternatives considered:** A Gradle `exec` task that runs `docker compose up -d` before the `test` task — rejected because it leaks containers and requires manual teardown.

### Remove JetBrains sample files
**Decision:** Delete `MainPageTest.java`, `MainPage.java`, and any other files that only target JetBrains.com.  
**Rationale:** These files were scaffolding for the initial project setup. Keeping them alongside Shopware tests would cause unrelated test failures if JetBrains.com is unreachable in the test environment.  
**Alternatives considered:** Marking them `@Disabled` — rejected because they add noise with no benefit.

### Page Object per page area
**Decision:** Create one `StorefrontPage` class covering homepage-level elements.  
**Rationale:** At smoke-test scale a single page object is sufficient. As the suite grows, individual page objects (e.g., `SearchResultsPage`, `CategoryPage`) should be extracted.

## Risks / Trade-offs

- **Dockware startup time** → dockware can take 30–60 seconds to be ready. Mitigation: use Testcontainers' `waitingFor(Wait.forHttp("/"))` so the test blocks until the storefront responds before Selenide opens the browser.
- **Demo data dependency** → smoke tests rely on dockware's pre-loaded demo data (e.g., known category names). If dockware changes its demo data, selectors or assertions may break. Mitigation: use resilient selectors (e.g., "first category link") rather than hardcoded text where possible.
- **Docker daemon required** → Testcontainers requires a running Docker daemon. This is already a prerequisite for any local Shopware testing, so it adds no new constraint.

## Migration Plan

1. Add Testcontainers dependency to `build.gradle`
2. Delete `MainPage.java`, `MainPageTest.java`, and any other JetBrains sample files
3. Add `StorefrontPage.java` under the existing test source tree
4. Add `StorefrontSmokeTest.java` with a `@BeforeAll` that starts the dockware container via Testcontainers and sets `Configuration.baseUrl` from the mapped port
5. Run `./gradlew test` — the container starts and stops automatically with the test run

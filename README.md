# aiqe-shopware-tests

Browser automation test suite for the [Shopware](https://www.shopware.com/) e-commerce platform, built with **Selenide** and **Serenity BDD**.

## About

This project is part of [Comsysto Reply](https://comsystoreply.de/)'s **AI Quality Engineering (AIQE)** initiative, which explores how AI-assisted tooling can raise the bar for software quality. The repository demonstrates three practices in combination:

- **OpenSpec as plan-mode on steroids** — instead of relying on ad-hoc AI prompting, changes are first specified as structured proposals (design doc + acceptance criteria + task breakdown) in `openspec/changes/`. The AI operates against this spec, keeping implementation grounded and reviewable.
- **Continuous review via Pull Requests** — every generated artifact (code, tests, ADRs, diagrams) goes through a GitHub PR. This creates a tight feedback loop where human reviewers can catch, correct, and rework AI output before it lands, making the process auditable and incremental rather than a one-shot generation.
- **Skills for organisation-wide consistency** — Claude Code skills encode Comsysto's coding guidelines, commit conventions, ADR format, and C4 diagram style. Invoking a skill applies these rules automatically, so the AI's output fits the organization's standards without repeating the same instructions in every prompt.

## Stack

| Library | Version | Role |
|---|---|---|
| Selenide | 7.6.0 | Fluent Selenium wrapper |
| Serenity BDD | 4.2.8 | Test reporting framework |
| JUnit 5 | 5.11.3 | Test runner |
| AssertJ | 3.26.3 | Fluent assertions |
| Testcontainers | 1.20.4 | Manages the Shopware Docker container |

Tests run against a **dockware/dev** Shopware container that is started automatically via Testcontainers — no manual Docker setup required.

## Prerequisites

- Java 17+
- Docker (for the Shopware container)

## Running tests

```bash
# Run all tests
./gradlew test

# Build the project
./gradlew build

# Generate Serenity HTML report
./gradlew aggregate
```

Reports are written to `build/reports/` after each test run (Serenity aggregation runs automatically via the Gradle plugin).

## Discovery

A scriptable crawler can inspect the live Shopware storefront and write JSON + PNG snapshots to `build/discovery/`:

```bash
./gradlew discover
# Pass a discovery script:
./gradlew discover -Pargs=discovery-scripts/my-script.yaml
```

## Architecture

Tests follow the **Page Object Model (POM)** pattern. All sources live under:

```
src/test/java/de/comsystoreply/aiqe/aiqeshopwaretests/
```

| Class | Purpose |
|---|---|
| `DockwareContainer` | Starts/stops the Shopware Testcontainer and patches the sales channel URL |
| `StorefrontPage` | Page object for the Shopware storefront |
| `CartPage` | Page object for the shopping cart |
| `StorefrontSmokeTest` | Smoke tests verifying basic storefront behaviour |
| `CartManagementTest` | Tests covering add / update / remove cart flow |
| `DiscoveryRunner` | Entry point for the scriptable crawler |

## Planning

This project uses [OpenSpec](openspec/config.yaml) for planning and tracking changes. Proposals live in `openspec/changes/`.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Browser automation test suite for Shopware e-commerce platform, built with Selenide (Selenium wrapper) and Serenity BDD for automated reporting. Currently contains sample tests against JetBrains.com as a template.

## Planning

* Openspec is used to plan for this project. Please make sure to always follow the rules specified in openspec/config.yml. 

## Commands

```bash
# Run all tests
./gradlew test

# Build project
./gradlew build

# Generate Serenity reports (runs after test automatically via plugin)
./gradlew aggregate
```

Test reports are generated in `build/reports/` by Serenity BDD.

## Architecture

Tests follow the **Page Object Model (POM)** pattern:

- **Page Objects** (e.g., `MainPage.java`) — encapsulate element selectors using Selenide's `$()` / `$$()` with XPath or CSS selectors
- **Test Classes** (e.g., `MainPageTest.java`) — JUnit 5 classes that use page objects, with `@BeforeAll` for global config (browser size) and `@BeforeEach` for navigation

Key stack:
- **Selenide 7.6.0** — fluent Selenium wrapper; use `$()`, `shouldBe()`, `shouldHave()` for assertions on elements
- **Serenity BDD 4.2.8** — wraps JUnit to produce HTML reports; apply `@SerenityTest` when adding reporting steps
- **JUnit 5** — test runner (`useJUnitPlatform()` in build)
- **AssertJ** — fluent assertions for non-element checks

Browser configuration (`browsers.json`) targets Selenoid with Chrome 99.0 on port 4444. To run against a remote Selenoid grid, set `Configuration.remote` in test setup.

Main package: `de.comsystoreply.aiqe.aiqeshopwaretests`



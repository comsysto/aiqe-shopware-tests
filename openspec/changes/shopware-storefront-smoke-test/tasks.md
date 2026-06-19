## 1. Cleanup — Remove JetBrains Sample Files

- [x] 1.1 Delete `MainPage.java` and `MainPageTest.java` (and any other files that only target JetBrains.com) from the test source tree

## 2. Testcontainers Setup

- [x] 2.1 Add the Testcontainers BOM and `testcontainers` artifact to `build.gradle` (test scope)
- [x] 2.2 Verify that `./gradlew dependencies` resolves Testcontainers without conflicts

## 3. Page Object

- [x] 3.1 Create `StorefrontPage.java` in `src/test/java/de/comsystoreply/aiqe/aiqeshopwaretests/` with Selenide element fields: `mainNavigation`, `searchInput`, `searchButton`
- [x] 3.2 Inspect the running dockware storefront to identify correct CSS/XPath selectors for each element and set them in `StorefrontPage`

## 4. Smoke Tests

- [x] 4.1 Create `StorefrontSmokeTest.java` in the same package with a `@BeforeAll` that:
  - starts a `GenericContainer("dockware/dev:latest")` with port 80 exposed and `waitingFor(Wait.forHttp("/"))`
  - sets `Configuration.baseUrl` to `http://<host>:<mappedPort>`
  - sets `Configuration.browserSize = "1280x800"`
- [x] 4.2 Implement `homepageLoads()` test: open base URL, assert `Selenide.title()` is not blank, assert `mainNavigation` is visible
- [x] 4.3 Implement `categoryNavigationWorks()` test: click the first top-level nav link, assert the URL changed, assert at least one product card or listing element is visible
- [x] 4.4 Implement `searchReturnsResults()` test: type "Shirt" into `searchInput`, click `searchButton`, assert at least one product result element is visible

## 5. Verification

- [ ] 5.1 Run `./gradlew test` and confirm all three smoke tests pass (container starts and stops automatically)
- [ ] 5.2 Confirm Serenity report is generated in `build/reports/` with test results

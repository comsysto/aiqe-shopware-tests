## Why

The project needs an initial automated test suite targeting the Shopware storefront to verify that core user-facing functionality is working after deployments. Running Shopware via the dockware Docker container provides a reproducible local environment for these tests.

## What Changes

- Add Docker Compose configuration to run Shopware via the `dockware/dev` container
- Add a `StorefrontPage` page object encapsulating selectors for the Shopware storefront
- Add a `StorefrontSmokeTest` JUnit 5 test class with smoke tests covering critical storefront paths

## Capabilities

### New Capabilities

- `shopware-docker-setup`: Docker Compose setup to spin up Shopware via the dockware container, making the storefront available for test runs
- `storefront-smoke-test`: Initial smoke test suite for the Shopware storefront covering homepage load, navigation, and search

### Modified Capabilities

_(none)_

## Impact

- **New files**: `docker-compose.yml`, `src/test/java/.../StorefrontPage.java`, `src/test/java/.../StorefrontSmokeTest.java`
- **Dependencies**: Requires Docker and Docker Compose available in the test environment; dockware image pulled from Docker Hub
- **Test target URL**: Changes from JetBrains.com (sample) to `http://localhost` (Shopware dockware default)

## Requirements

### Requirement: Docker Compose file starts Shopware via dockware
The repository SHALL include a `docker-compose.yml` at the project root that starts a Shopware 6 instance using the `dockware/dev:latest` image. The storefront SHALL be accessible at `http://localhost` after the container is healthy.

#### Scenario: Storefront is reachable after compose up
- **WHEN** `docker compose up -d` is executed and the container reaches the healthy state
- **THEN** an HTTP GET to `http://localhost` returns a 200 response

#### Scenario: Container exposes port 80
- **WHEN** the Docker Compose file is parsed
- **THEN** it maps container port 80 to host port 80

### Requirement: Compose file includes a health check
The `docker-compose.yml` SHALL define a `healthcheck` for the Shopware service so that dependent tooling can wait for readiness before running tests.

#### Scenario: Health check passes once Shopware is ready
- **WHEN** the dockware container has fully initialized Shopware
- **THEN** the Docker health check reports `healthy`

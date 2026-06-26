## Requirements

### Requirement: Bootstrap script reproduces the previous default crawl
A YAML script at `discovery-scripts/bootstrap.yml` SHALL be committed to the repository. When executed via `./gradlew discover --args="discovery-scripts/bootstrap.yml"`, it SHALL visit the same set of pages that the previous hardcoded `DiscoveryRunner.crawl()` covered: the homepage, all top-level navigation targets, one product page per navigation target, and the authenticated account pages.

#### Scenario: Bootstrap produces the same snapshots as the old hardcoded crawl
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** `build/discovery/` contains JSON and PNG snapshots for: homepage, each top-level navigation page, one product per nav page, account overview, and account order history

### Requirement: Bootstrap script uses dockware default credentials for authenticated pages
The `bootstrap.yml` script SHALL authenticate using the dockware default customer credentials (`customer@example.com` / `shopware`) via the Shopware login form before visiting account pages.

#### Scenario: Authenticated pages are reached via login form
- **WHEN** the bootstrap script reaches the login step
- **THEN** it fills `input[name='email']` with `customer@example.com` and `input[name='password']` with `shopware`
- **THEN** it clicks the submit button and the account overview page is reached

### Requirement: Bootstrap script is the default entry point for new discovery runs
When a user runs discovery without specifying a script, the documentation and skill SHALL direct them to use `bootstrap.yml`. There is no implicit default — the script path is always explicit.

#### Scenario: User is guided to bootstrap.yml
- **WHEN** the `/discover-tests` skill begins a new round
- **THEN** if no prior snapshots exist, it runs `./gradlew discover --args="discovery-scripts/bootstrap.yml"` as the first step

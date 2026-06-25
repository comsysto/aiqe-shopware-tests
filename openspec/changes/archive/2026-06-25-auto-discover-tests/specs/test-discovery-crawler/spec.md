## ADDED Requirements

### Requirement: Crawler starts Shopware container automatically
The `DiscoveryRunner` SHALL start the `dockware/dev:latest` container via Testcontainers and wait for it to be ready before crawling, using the same startup and sales channel domain patching logic as `StorefrontSmokeTest`.

#### Scenario: Container starts and becomes reachable
- **WHEN** `./gradlew discover` is executed
- **THEN** the dockware container starts and Shopware is reachable at the mapped port before any page is visited

### Requirement: Crawler visits guest-accessible pages
The `DiscoveryRunner` SHALL crawl the homepage, all top-level navigation links, and one level of pages reachable from each navigation link without requiring authentication.

#### Scenario: Homepage and nav pages are visited
- **WHEN** the crawler runs
- **THEN** the homepage (`/`) is visited
- **THEN** each top-level navigation link is followed and its target page is visited

#### Scenario: One level deep from each nav page
- **WHEN** the crawler visits a navigation target page
- **THEN** it follows the first product or subcategory link found on that page
- **THEN** that linked page is visited and snapshotted

### Requirement: Crawler visits authenticated pages using dockware default credentials
The `DiscoveryRunner` SHALL log in using the dockware default customer credentials (`customer@example.com` / `shopware`) and visit the account section (account overview, order history).

#### Scenario: Login succeeds with default credentials
- **WHEN** the crawler navigates to the login page and submits the default credentials
- **THEN** the account overview page is reached without an error state

#### Scenario: Account pages are snapshotted after login
- **WHEN** the crawler is authenticated
- **THEN** it visits the account overview and order history pages and captures snapshots for each

### Requirement: Crawler emits a JSON snapshot per visited page
For each visited page the `DiscoveryRunner` SHALL write a JSON file to `build/discovery/` containing: the page URL, page title, a journey hint (a short label derived from the URL path), a list of visible interactive elements (navigation links, buttons, form fields, headings), and whether the page required authentication.

#### Scenario: JSON file is written for each page
- **WHEN** a page has been visited and rendered
- **THEN** a file named `<slug>.json` exists in `build/discovery/`
- **THEN** the file contains at minimum: `url`, `title`, `journey_hint`, `elements`, `auth_required`

### Requirement: Crawler captures a full-page screenshot per visited page
For each visited page the `DiscoveryRunner` SHALL capture a full-page screenshot and write it as `<slug>.png` alongside the JSON snapshot in `build/discovery/`.

#### Scenario: Screenshot file is written for each page
- **WHEN** a page has been visited
- **THEN** a file named `<slug>.png` exists in `build/discovery/`
- **THEN** the file is a valid PNG image of the rendered page

### Requirement: Crawler is invoked via a dedicated Gradle task
A Gradle task named `discover` SHALL be registered in `build.gradle` that runs the `DiscoveryRunner` independently of the `test` task. It SHALL NOT appear in Serenity BDD reports.

#### Scenario: Discover task runs without triggering normal tests
- **WHEN** `./gradlew discover` is executed
- **THEN** no `@Test`-annotated test methods from `StorefrontSmokeTest` or other test classes are executed
- **THEN** no Serenity report entries are created for the discovery run

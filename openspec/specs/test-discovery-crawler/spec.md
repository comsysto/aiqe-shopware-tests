## Requirements

### Requirement: Crawler starts Shopware container automatically
The `DiscoveryRunner` SHALL start the `dockware/dev:latest` container via Testcontainers and wait for it to be ready before crawling, using the same startup and sales channel domain patching logic as `StorefrontSmokeTest`.

#### Scenario: Container starts and becomes reachable
- **WHEN** `./gradlew discover` is executed
- **THEN** the dockware container starts and Shopware is reachable at the mapped port before any page is visited

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
A Gradle task named `discover` SHALL be registered in `build.gradle` that runs the `DiscoveryRunner` independently of the `test` task. It SHALL NOT appear in Serenity BDD reports. The task SHALL accept a required script path via `--args` and pass it as the first argument to `DiscoveryRunner.main()`.

#### Scenario: Discover task runs without triggering normal tests
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** no `@Test`-annotated test methods from `StorefrontSmokeTest` or other test classes are executed
- **THEN** no Serenity report entries are created for the discovery run

#### Scenario: Discover task forwards the script path argument
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** `DiscoveryRunner.main()` receives `["discovery-scripts/bootstrap.yml"]` as its `args` array

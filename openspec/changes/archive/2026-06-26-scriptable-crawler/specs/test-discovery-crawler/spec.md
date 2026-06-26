## MODIFIED Requirements

### Requirement: Crawler is invoked via a dedicated Gradle task
A Gradle task named `discover` SHALL be registered in `build.gradle` that runs the `DiscoveryRunner` independently of the `test` task. It SHALL NOT appear in Serenity BDD reports. The task SHALL accept a required script path via `--args` and pass it as the first argument to `DiscoveryRunner.main()`.

#### Scenario: Discover task runs without triggering normal tests
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** no `@Test`-annotated test methods from `StorefrontSmokeTest` or other test classes are executed
- **THEN** no Serenity report entries are created for the discovery run

#### Scenario: Discover task forwards the script path argument
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** `DiscoveryRunner.main()` receives `["discovery-scripts/bootstrap.yml"]` as its `args` array

## REMOVED Requirements

### Requirement: Crawler visits guest-accessible pages
**Reason:** Hardcoded crawl logic replaced by YAML script interpreter. The `bootstrap.yml` script covers the same pages via explicit steps.
**Migration:** Use `discovery-scripts/bootstrap.yml` to reproduce the previous guest-page crawl.

### Requirement: Crawler visits authenticated pages using dockware default credentials
**Reason:** Hardcoded authentication flow replaced by `fill` and `click` steps in YAML scripts. `bootstrap.yml` includes the login flow.
**Migration:** Use `discovery-scripts/bootstrap.yml` to reproduce the previous authenticated crawl.

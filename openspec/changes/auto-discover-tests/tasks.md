## 1. Gradle Task Setup

- [x] 1.1 Add a `discover` source set (or `JavaExec` task) to `build.gradle` that runs `DiscoveryRunner` independently of the `test` task
- [x] 1.2 Ensure `build/discovery/` is excluded from Serenity report aggregation and add it to `.gitignore`

## 2. DiscoveryRunner — Container & Auth

- [x] 2.1 Create `DiscoveryRunner.java` in the test source tree; bootstrap Testcontainers + Selenide startup (reuse logic from `StorefrontSmokeTest`)
- [x] 2.2 Implement sales channel domain patching (same `configureSalesChannelDomain` logic)
- [ ] 2.3 Implement guest crawl entry: visit homepage and collect all top-level navigation link URLs

## 3. DiscoveryRunner — Crawling Logic

- [ ] 3.1 Implement navigation crawl: for each top-level nav link, visit the page and collect one level of child links (first product or subcategory link)
- [ ] 3.2 Implement authenticated crawl: navigate to login, submit dockware default credentials (`customer@example.com` / `shopware`), then visit account overview and order history
- [ ] 3.3 Implement DOM element extraction per page: nav links, buttons, form fields, headings — collected into a structured map

## 4. DiscoveryRunner — Snapshot Output

- [ ] 4.1 Implement JSON snapshot writer: serialize URL, title, journey hint, elements, auth_required to `build/discovery/<slug>.json`
- [ ] 4.2 Implement screenshot capture per page using `Selenide.screenshot()` writing to `build/discovery/<slug>.png`
- [ ] 4.3 Implement slug generation from URL path (URL-safe, collision-free)

## 5. Claude Code Skill

- [ ] 5.1 Create the `/discover-tests` skill file with orchestration logic: run `./gradlew discover`, read snapshots, read existing specs, cluster into journeys, generate proposals with human approval gate per proposal

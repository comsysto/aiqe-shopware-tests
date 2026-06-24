## Context

The test suite currently relies entirely on human-authored OpenSpec proposals. There is no mechanism to systematically discover what user journeys Shopware exposes and which are not yet covered by specs. The existing `StorefrontSmokeTest` already solves the hardest infrastructure problem: spinning up a dockware container via Testcontainers and patching the sales channel domain. This design reuses that infrastructure as the foundation for a crawling pipeline.

```
┌─────────────────────────────────────────────────────────────────┐
│  C4 Context: Auto-Discover Tests Pipeline                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Developer  ──/discover-tests──▶  Claude Code Skill           │
│                                          │                      │
│                          ┌───────────────┼──────────────────┐  │
│                          │               ▼                  │  │
│                          │     ./gradlew discover           │  │
│                          │           │                      │  │
│                          │           ▼                      │  │
│                          │   DiscoveryRunner.java           │  │
│                          │   (Selenide + Testcontainers)    │  │
│                          │           │                      │  │
│                          │           ▼                      │  │
│                          │   dockware/dev container         │  │
│                          │   Shopware Storefront            │  │
│                          │           │                      │  │
│                          │           ▼                      │  │
│                          │   build/discovery/               │  │
│                          │   *.json + *.png                 │  │
│                          └──────────────────────────────────┘  │
│                                    │                            │
│                          openspec/specs/** (existing)          │
│                                    │                            │
│                          Claude reads + diffs                   │
│                                    │                            │
│                          openspec/changes/<journey>/            │
│                          proposal.md  ◀── human reviews        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Goals / Non-Goals

**Goals:**
- Crawl the Shopware storefront (guest + authenticated, nav + 1 level deep) and produce structured snapshots
- Generate OpenSpec proposals for user journeys not yet covered (or only partially covered) by existing specs
- Be re-runnable: on subsequent runs, propose gaps and updates rather than duplicating existing work
- Zero new runtime dependencies beyond what's already in the project

**Non-Goals:**
- Full-depth BFS crawling (too slow, too noisy)
- Automatic implementation of discovered proposals (human approval gates each one)
- CI/CD integration (local dev only for now)
- Making credentials configurable (dockware defaults are sufficient)

## Decisions

### Decision 1: Reuse Selenide for crawling (not Playwright or WebFetch)

**Chosen**: `DiscoveryRunner` is a JUnit 5 class using Selenide — same stack as existing tests.

**Rationale**: Shopware's storefront is JavaScript-rendered. Raw HTML fetching (WebFetch) would miss dynamically injected content (nav items, product listings). Playwright would work but introduces a Node.js dependency with no other benefit. Selenide already renders JS, supports screenshots natively via `Selenide.screenshot()`, and the Testcontainers infrastructure already exists in the project.

**Alternative considered**: Playwright via `npx` subprocess. Rejected: adds Node.js runtime dependency, requires process management, no advantage over Selenide for this use case.

### Decision 2: DiscoveryRunner as a separate Gradle task, not part of the test suite

**Chosen**: A dedicated `discover` Gradle task (via `JavaExec` or a separate test source set) that runs `DiscoveryRunner` independently of `./gradlew test`.

**Rationale**: Discovery is not a test — it has no assertions and should not contribute to test reports or fail the build. Mixing it with the test suite would pollute Serenity reports. A named Gradle task makes the intent explicit and keeps the pipeline invocable without running the full test suite.

**Alternative considered**: A `@Test`-annotated discovery method in a separate test class tagged with a JUnit tag, excluded by default. Rejected: still participates in Serenity report generation and requires tag management.

### Decision 3: Snapshot format is JSON + PNG per page

**Chosen**: Each visited page produces two files in `build/discovery/`:
- `<slug>.json`: URL, page title, journey hint, key DOM elements (nav links, buttons, form fields, headings), auth state
- `<slug>.png`: full-page screenshot via `Selenide.screenshot()`

**Rationale**: JSON gives Claude structured, reliable data for reasoning about UI surface. Screenshots give visual context for understanding layout and relative importance (above-the-fold elements) without requiring visual-only processing. Together they allow high-quality journey inference without either being sufficient alone.

### Decision 4: Claude Code skill orchestrates the full pipeline

**Chosen**: A Claude Code skill (`/discover-tests`) that runs the Gradle task, reads snapshots, reads existing specs, and writes proposals.

**Rationale**: The intelligence layer (journey clustering, gap detection, proposal writing) belongs to Claude, not to Java code. The skill is the natural place to orchestrate multi-step AI reasoning. The Gradle task is responsible only for deterministic data collection; Claude handles interpretation.

### Decision 5: Journey clustering, not page-per-proposal

**Chosen**: Claude groups pages into user journey clusters (e.g. browse→PDP→cart→checkout) and writes one proposal per journey, not one per URL.

**Rationale**: Shopware has many URLs that are variations of the same journey (e.g. `/category/clothing`, `/category/electronics` are both "product listing" flows). Per-URL proposals would explode in count and result in near-duplicate specs. Per-journey proposals are more maintainable and map better to real user behavior.

## Risks / Trade-offs

- **dockware startup time** (~3 min) makes the `discover` task slow. → Mitigation: document this expectation; no workaround needed for local-only use.
- **DOM snapshot completeness**: Selenide captures the DOM at a single point in time. Dynamic content loaded after scroll or interaction may be missed. → Mitigation: crawler interacts with key UI elements (hover nav, expand accordions) before snapshotting.
- **Snapshot staleness**: Snapshots in `build/discovery/` reflect the state at last run. Claude reads stale snapshots if the developer doesn't re-run the crawler. → Mitigation: skill always re-runs `./gradlew discover` before reading snapshots; never reads stale data.
- **Proposal quality depends on prompt quality**: Claude's ability to cluster journeys and identify gaps depends on how well the skill prompt is written. → Mitigation: the skill prompt is a first-class artifact, iterable independently of the Java crawler.

## Open Questions

- Should `build/discovery/` be `.gitignore`d? (Snapshots are ephemeral build artifacts — yes, likely.)
- What is the right crawl entry point for authenticated flows — `/account/login` directly, or through the nav?

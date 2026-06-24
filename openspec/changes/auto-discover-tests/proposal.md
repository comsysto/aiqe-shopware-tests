## Why

The test suite grows manually today — someone has to decide what to test, write specs, and implement them. This is slow and incomplete. By crawling a live Shopware storefront and having an AI agent reason over its structure, we can automatically discover user journeys worth testing and generate OpenSpec proposals, turning test coverage into an observable, repeatable process.

## What Changes

- A `DiscoveryRunner` Selenide-based crawler (new Gradle task `discover`) starts the dockware container, crawls public and authenticated pages (nav + one level deep), and emits structured JSON snapshots and screenshots to `build/discovery/`.
- A new Claude Code skill `/discover-tests` orchestrates the full pipeline: runs the Gradle crawler, reads the snapshots, diffs against existing `openspec/specs/`, and writes one OpenSpec proposal per discovered user journey gap.
- On re-runs, the skill compares live page structure against existing specs and proposes updates or gap-filling changes rather than duplicating covered ground.

## Capabilities

### New Capabilities

- `test-discovery-crawler`: A Gradle-invocable Selenide crawler that starts the dockware Shopware container, navigates the storefront (guest + authenticated flows), and captures DOM snapshots and screenshots per page into `build/discovery/`.
- `test-discovery-skill`: A Claude Code skill (`/discover-tests`) that runs the crawler, reads its output, diffs against existing specs, and generates OpenSpec proposals for uncovered or partially covered user journeys.

### Modified Capabilities

<!-- none -->

## Impact

- New Gradle task `discover` added to `build.gradle`
- New Java class `DiscoveryRunner` in `src/test/java/...` (reuses Testcontainers + Selenide infrastructure)
- New skill file registered in the Claude Code skills system
- No changes to existing test classes or page objects
- No new runtime dependencies — dockware/dev:latest already in use; screenshots via Selenide's built-in capture API

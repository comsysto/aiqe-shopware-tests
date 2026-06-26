## Why

The current `DiscoveryRunner` has all its crawl logic hardcoded — it can only follow links, never interact with the page. This makes entire categories of pages (cart, checkout, modal flows, AJAX-driven state) invisible to the discovery pipeline. By replacing the hardcoded logic with a YAML script interpreter, Claude becomes the intelligence that decides what to explore, while the runner stays a dumb executor.

## What Changes

- **BREAKING** `DiscoveryRunner` loses all hardcoded crawl methods (`collectNavUrls`, `firstChildUrl`, `crawlAuthenticated`). It becomes a pure YAML step interpreter.
- A `discovery-scripts/` directory is added to the repo, holding committed YAML scripts that define exploration paths.
- A `discovery-scripts/bootstrap.yml` script is added, replacing the previous hardcoded link-following and authenticated crawl.
- The `discover` Gradle task gains a required script path argument: `./gradlew discover --args="discovery-scripts/bootstrap.yml"`.
- The `/discover-tests` skill is updated to follow a **round model**: per invocation, Claude inspects existing snapshots, writes one new interaction script, executes it, reads the resulting snapshots, and hands back to the user with findings and suggested next steps.

## Capabilities

### New Capabilities

- `crawler-script-runner`: The YAML step interpreter in `DiscoveryRunner` — reads a script file and executes steps (`open`, `click`, `fill`, `snapshot`, `wait`) against the live browser.
- `crawler-bootstrap-script`: The committed `discovery-scripts/bootstrap.yml` that reproduces the previous default crawl (homepage, nav pages, one product per nav, authenticated account pages).

### Modified Capabilities

- `test-discovery-crawler`: Requirement changes — the crawler now requires a script path argument rather than running a fixed hardcoded crawl. The Gradle task invocation changes accordingly.
- `test-discovery-skill`: Requirement changes — the skill now follows a round model (one script per invocation, hand back to user) rather than a single one-shot pipeline.

## Impact

- `DiscoveryRunner.java`: major rewrite — all crawl methods removed, YAML reader + step executor added
- `build.gradle`: `discover` task gains `args` support
- New directory `discovery-scripts/` with `bootstrap.yml`
- `.claude/skills/discover-tests/SKILL.md`: updated round model
- New dependency: SnakeYAML (or similar) for YAML parsing in the test classpath
- `openspec/specs/test-discovery-crawler/spec.md`: delta spec required
- `openspec/specs/test-discovery-skill/spec.md`: delta spec required

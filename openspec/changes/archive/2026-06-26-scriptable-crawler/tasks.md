## 1. Dependency and Build Setup

- [x] 1.1 Add SnakeYAML dependency to `build.gradle` under `testImplementation` and verify `./gradlew testClasses` compiles cleanly
- [x] 1.2 Add `args` support to the `discover` Gradle task so `--args` is forwarded to `DiscoveryRunner.main()`

## 2. YAML Script Interpreter

- [x] 2.1 Rewrite `DiscoveryRunner` to read the script path from `args[0]`, fail fast with a descriptive error if absent or file not found
- [x] 2.2 Implement the `open` step: resolve relative paths against base URL, navigate via `Selenide.open()`
- [x] 2.3 Implement the `click` step: locate element by CSS selector and click; throw descriptive exception on timeout
- [x] 2.4 Implement the `fill` step: locate element by `selector` field, call `setValue(value)`
- [x] 2.5 Implement the `snapshot` step: write JSON + PNG to `build/discovery/` using the step's `name` as slug; apply collision-safe deduplication suffix
- [x] 2.6 Implement the `wait` step: `Thread.sleep(ms)` for the given milliseconds
- [x] 2.7 Implement unknown-step detection: exit with non-zero code and message before executing further steps

## 3. Bootstrap Script

- [x] 3.1 Create `discovery-scripts/bootstrap.yml` that reproduces the previous hardcoded crawl: open homepage, follow each top-level nav link with a snapshot, open one product per nav page with a snapshot, log in with dockware credentials, snapshot account overview and order history

## 4. Skill Update

- [x] 4.1 Update `.claude/skills/discover-tests/SKILL.md` to follow the round model: inspect snapshots → write one script → execute → read new snapshots → hand back to user with findings and suggested next steps

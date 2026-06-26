## Context

`DiscoveryRunner` currently hardcodes three crawl strategies: link-following for guest pages, first-child product traversal, and a fixed authenticated account flow. This makes entire categories of pages — cart, checkout, modal flows, AJAX-driven state — structurally unreachable. The runner is intelligent code trying to be a general-purpose explorer; it should instead be a dumb executor driven by explicit instructions.

The scriptable crawler redesign separates concerns cleanly: Claude writes the exploration plan (YAML script), `DiscoveryRunner` executes it. The round model (one script per skill invocation, then hand back) keeps humans in the loop and avoids runaway discovery sessions.

## Goals / Non-Goals

**Goals:**
- Replace hardcoded crawl logic with a general YAML step interpreter
- Reproduce existing crawl behavior via a committed `bootstrap.yml` script
- Enable Claude to reach any page reachable by mouse interaction (click, fill, submit)
- Make the `discover` Gradle task accept a script path argument
- Update the `/discover-tests` skill to follow a round model

**Non-Goals:**
- Loops, conditionals, or branching in YAML scripts — Claude writes explicit steps
- Browser JavaScript injection or devtools-level interaction
- Parallel crawling or multi-tab execution
- Automatic script generation without human review

## Decisions

### ADR 1: Simple flat vocabulary — no loops or conditionals

**Decision:** The YAML step vocabulary is intentionally minimal: `open`, `click`, `fill`, `snapshot`, `wait`. No `for`, `if`, or `repeat` constructs.

**Alternatives considered:**
- **Turing-complete scripting (Groovy/JS embedded)**: Too complex to audit, easy to write infinite loops, obscures intent.
- **Conditional steps (`if_visible`, `skip_if`)**: Adds parser complexity for marginal gain — Claude can just write two separate scripts for the two branches.

**Rationale:** Claude is already good at writing explicit sequences. A flat list of steps is auditable by humans, safe to execute, and trivially parseable. If a flow requires branching, Claude writes two scripts and runs them in separate rounds.

### ADR 2: Scripts committed to `discovery-scripts/` directory

**Decision:** YAML scripts are version-controlled files in `discovery-scripts/`. The `discover` task takes a script path argument.

**Alternatives considered:**
- **Inline script passed as CLI argument**: Too unwieldy for multi-step scripts.
- **Scripts generated at runtime (not committed)**: Loses auditability; can't diff changes to exploration strategy over time.

**Rationale:** Committed scripts act as a record of exploration decisions. `bootstrap.yml` is the baseline — future scripts for cart, checkout, admin, etc. accumulate in the directory and document what has been explored.

### ADR 3: `bootstrap.yml` replaces hardcoded crawl — no parallel paths

**Decision:** The existing hardcoded `crawl()`, `crawlAuthenticated()`, and `firstChildUrl()` methods are removed entirely. `bootstrap.yml` replicates their behavior as explicit steps.

**Alternatives considered:**
- **Keep hardcoded as default, scripts as extension**: Two code paths to maintain; unclear which runs when.

**Rationale:** A clean break avoids ambiguity. `bootstrap.yml` is the single source of truth for the baseline crawl. If it's wrong, it's fixed in the YAML, not in Java.

### ADR 4: Round model for the skill

**Decision:** Per skill invocation, Claude inspects existing snapshots, writes exactly one new script, executes it, reads resulting snapshots, and hands back to the user with findings.

**Alternatives considered:**
- **One-shot (all scripts in one session)**: Risks runaway sessions, no human checkpoints, harder to debug mid-run failures.
- **Fully automated loop**: Removes human judgment from exploration direction.

**Rationale:** The round model makes Claude the intelligence and keeps humans in control of exploration direction. Each round produces observable output (snapshots) that the user can inspect before committing to the next step.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    /discover-tests skill                      │
│                                                              │
│  1. Read build/discovery/ (existing snapshots)               │
│  2. Decide what to explore next                              │
│  3. Write discovery-scripts/<name>.yml                       │
│  4. ./gradlew discover --args="discovery-scripts/<name>.yml" │
│  5. Read new snapshots from build/discovery/                 │
│  6. Hand back to user with findings                          │
└──────────────────┬───────────────────────────────────────────┘
                   │ Gradle JavaExec
                   ▼
┌──────────────────────────────────────────────────────────────┐
│                   DiscoveryRunner (main)                      │
│                                                              │
│  ┌─────────────────┐    ┌──────────────────────────────────┐ │
│  │ DockwareContainer│    │     YAML Script Interpreter      │ │
│  │   .start()       │    │                                  │ │
│  │   .stop()        │    │  open  → Selenide.open()         │ │
│  └─────────────────┘    │  click → $(selector).click()     │ │
│                          │  fill  → $(sel).setValue(val)    │ │
│                          │  snapshot → writeJson+writePng   │ │
│                          │  wait  → Thread.sleep(ms)        │ │
│                          └──────────────────────────────────┘ │
│                                            │                  │
│                                            ▼                  │
│                                  build/discovery/             │
│                                  ├── <slug>.json              │
│                                  └── <slug>.png               │
└──────────────────────────────────────────────────────────────┘
```

## YAML Script Vocabulary

```yaml
steps:
  - open: /path/or/full-url
  - click: "CSS selector"
  - fill:
      selector: "CSS selector"
      value: "text to enter"
  - snapshot:
      name: journey-hint-slug
      auth_required: false
  - wait: 500   # milliseconds
```

`snapshot.name` becomes the `journey_hint` in the JSON output and the `<slug>` in filenames. It MUST be unique within a script run; the runner appends a counter suffix on collision (same behavior as current slug deduplication).

## Risks / Trade-offs

- **Selector fragility** → Shopware updates may break selectors in committed scripts. Mitigation: scripts are version-controlled; failures are visible immediately in CI output. Bootstrap.yml uses stable nav selectors already validated by existing tests.
- **SnakeYAML dependency** → Adds a transitive dependency to the test classpath. Mitigation: SnakeYAML is already a transitive dependency of Spring (present in many Java projects); risk of version conflict is low but should be verified at integration time.
- **Round model is slower** → Exploring the full storefront requires multiple skill invocations. Mitigation: this is a feature, not a bug — human checkpoints prevent wasted work on dead-end paths.
- **Bootstrap.yml behavioral parity** → If `bootstrap.yml` omits a page the old hardcoded crawl covered, that page disappears from discovery. Mitigation: write bootstrap.yml from the existing crawl logic step-by-step, run both old and new side-by-side once to compare snapshot sets.

## Migration Plan

1. Add SnakeYAML dependency to `build.gradle` (`testImplementation`)
2. Rewrite `DiscoveryRunner`: remove all crawl methods, add YAML reader + step dispatcher
3. Add `args` support to the `discover` Gradle task
4. Commit `discovery-scripts/bootstrap.yml` replicating the current crawl
5. Update `.claude/skills/discover-tests/SKILL.md` to follow the round model
6. Update delta specs for `test-discovery-crawler` and `test-discovery-skill`

Rollback: `bootstrap.yml` can be executed unchanged after any failed step; the old hardcoded behavior is reproduced by its content. If the YAML interpreter is broken, revert `DiscoveryRunner.java` — the script files remain harmless on disk.

## Open Questions

- SnakeYAML vs Jackson YAML: project already uses Gson for JSON; Jackson YAML would add a second JSON library as a side effect. SnakeYAML is lighter. Decision deferred to implementation task.
- Should `wait` accept a CSS selector condition (`wait: {visible: ".selector"}`) rather than a fixed millisecond count? Deferred — fixed-ms is simpler for v1; upgrade if flakiness is observed.

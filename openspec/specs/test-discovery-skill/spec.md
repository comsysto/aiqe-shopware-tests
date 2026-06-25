## Requirements

### Requirement: Skill triggers the crawler before reading snapshots
The `/discover-tests` skill SHALL always run `./gradlew discover` to produce fresh snapshots before reading `build/discovery/`. It SHALL NOT read stale snapshots from a previous run.

#### Scenario: Fresh snapshots are produced on each skill invocation
- **WHEN** the user runs `/discover-tests`
- **THEN** `./gradlew discover` is executed and completes before any snapshot file is read

### Requirement: Skill reads existing specs to determine coverage
Before generating proposals, the skill SHALL read all files under `openspec/specs/` to understand which capabilities are already specified.

#### Scenario: Existing specs are loaded before gap analysis
- **WHEN** snapshots have been collected
- **THEN** all `openspec/specs/**/*.md` files are read
- **THEN** their requirements and scenarios are used as the baseline for gap detection

### Requirement: Skill clusters pages into user journeys
The skill SHALL group the visited pages into logical user journey clusters (e.g. "product browsing", "cart and checkout", "account management") before proposing specs. Each cluster produces at most one proposal.

#### Scenario: Pages are grouped, not proposed one-per-URL
- **WHEN** multiple pages belong to the same user journey
- **THEN** a single proposal is generated for the entire journey, not one per page

### Requirement: Skill generates proposals only for uncovered or partially covered journeys
The skill SHALL compare discovered journeys against existing specs and:
- Write a new OpenSpec proposal for journeys with no existing spec
- Write a gap-update proposal for journeys where the existing spec does not cover behaviors visible in the snapshot

#### Scenario: New journey produces a new proposal
- **WHEN** a discovered journey has no corresponding spec in `openspec/specs/`
- **THEN** the skill creates a new OpenSpec change with a `proposal.md` for that journey

#### Scenario: Partially covered journey produces an update proposal
- **WHEN** a discovered journey has an existing spec but the snapshot reveals additional behaviors not captured in it
- **THEN** the skill creates an OpenSpec change proposing additions to the existing spec

#### Scenario: Fully covered journey is skipped
- **WHEN** a discovered journey is fully covered by an existing spec
- **THEN** no proposal is generated for that journey

### Requirement: Each generated proposal is presented to the user for approval before the next one
The skill SHALL present each generated proposal to the user and wait for explicit approval or rejection before proceeding to the next proposal.

#### Scenario: User approves a proposal
- **WHEN** the skill presents a proposal and the user approves
- **THEN** the proposal files are committed to the change directory and the skill proceeds to the next journey

#### Scenario: User rejects a proposal
- **WHEN** the skill presents a proposal and the user rejects it
- **THEN** the proposal files are discarded and the skill proceeds to the next journey without creating any artifacts

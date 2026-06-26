## Requirements

### Requirement: Skill triggers the crawler before reading snapshots
The `/discover-tests` skill SHALL always run `./gradlew discover -Pargs="<script-path>"` to produce fresh snapshots before reading `build/discovery/`. The script path is chosen by Claude based on the current round's exploration goal. It SHALL NOT read stale snapshots from a previous run without executing a script first.

#### Scenario: Fresh snapshots are produced on each skill invocation
- **WHEN** the user runs `/discover-tests`
- **THEN** Claude selects or writes a YAML script appropriate for the current exploration goal
- **THEN** `./gradlew discover -Pargs="<script-path>"` is executed and completes before any snapshot file is read

#### Scenario: First invocation uses bootstrap.yml
- **WHEN** no snapshots exist in `build/discovery/`
- **THEN** Claude runs `./gradlew discover -Pargs="discovery-scripts/bootstrap.yml"` as the first step

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

### Requirement: Skill follows a round model — one script per invocation
Per invocation, the `/discover-tests` skill SHALL: inspect existing snapshots, determine what to explore next, write exactly one new YAML script to `discovery-scripts/`, execute it, read the resulting snapshots, and hand control back to the user with findings and suggested next steps. It SHALL NOT execute multiple scripts or loop autonomously within a single invocation.

#### Scenario: Single script per round
- **WHEN** the user runs `/discover-tests`
- **THEN** exactly one YAML script is written and executed during that invocation
- **THEN** after reading the new snapshots, the skill presents findings and stops — it does not proceed to a next script automatically

#### Scenario: User steers subsequent rounds
- **WHEN** the skill hands back findings and suggested next steps
- **THEN** the user decides which direction to explore next and re-invokes `/discover-tests`

### Requirement: Skill writes new interaction scripts to `discovery-scripts/`
When the skill determines that an interactive flow (e.g. add-to-cart, login, modal) must be explored, it SHALL write a new YAML script to `discovery-scripts/<descriptive-name>.yml` containing explicit `open`, `click`, `fill`, `snapshot`, and `wait` steps that drive the browser through that flow.

#### Scenario: Cart exploration script is written and executed
- **WHEN** the skill identifies that the cart page has not been snapshotted
- **THEN** it writes `discovery-scripts/explore-cart.yml` with steps to open a product page, click "Add to shopping cart", and snapshot the resulting cart page
- **THEN** it executes `./gradlew discover -Pargs="discovery-scripts/explore-cart.yml"`
- **THEN** `build/discovery/` contains a snapshot of the cart page after the run

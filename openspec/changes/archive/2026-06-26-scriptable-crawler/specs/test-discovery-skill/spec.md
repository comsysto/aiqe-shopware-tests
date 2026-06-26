## MODIFIED Requirements

### Requirement: Skill triggers the crawler before reading snapshots
The `/discover-tests` skill SHALL always run `./gradlew discover --args="<script-path>"` to produce fresh snapshots before reading `build/discovery/`. The script path is chosen by Claude based on the current round's exploration goal. It SHALL NOT read stale snapshots from a previous run without executing a script first.

#### Scenario: Fresh snapshots are produced on each skill invocation
- **WHEN** the user runs `/discover-tests`
- **THEN** Claude selects or writes a YAML script appropriate for the current exploration goal
- **THEN** `./gradlew discover --args="<script-path>"` is executed and completes before any snapshot file is read

#### Scenario: First invocation uses bootstrap.yml
- **WHEN** no snapshots exist in `build/discovery/`
- **THEN** Claude runs `./gradlew discover --args="discovery-scripts/bootstrap.yml"` as the first step

## ADDED Requirements

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
- **THEN** it executes `./gradlew discover --args="discovery-scripts/explore-cart.yml"`
- **THEN** `build/discovery/` contains a snapshot of the cart page after the run

## REMOVED Requirements

### Requirement: Each generated proposal is presented to the user for approval before the next one
**Reason:** The round model makes this implicit — the skill always hands back to the user between rounds, so there is a natural checkpoint before any next action.
**Migration:** Proposals are still created per discovered journey. The user reviews findings at the end of each round and decides what to explore next.

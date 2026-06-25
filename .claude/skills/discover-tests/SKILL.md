---
name: discover-tests
description: Crawl the live Shopware storefront, cluster visited pages into user journeys, diff against existing OpenSpec specs, and interactively propose new or updated specs for gaps found.
---

Auto-discover testable user journeys from a live Shopware storefront and generate OpenSpec proposals for uncovered or partially covered flows.

## Steps

### 1. Run the crawler

Execute the Gradle discovery task. This starts the dockware container, crawls guest and authenticated pages, and writes `build/discovery/*.json` + `*.png`.

Warn the user upfront: container startup takes up to 3 minutes.

```bash
./gradlew discover
```

If the task fails, report the error and stop.

### 2. Read snapshots

Glob `build/discovery/*.json` and read every file. Each snapshot has this structure:

```json
{
  "url": "...",
  "title": "...",
  "journey_hint": "...",
  "auth_required": false,
  "elements": {
    "navLinks": [...],
    "buttons": [...],
    "formFields": [...],
    "headings": [...]
  }
}
```

### 3. Read existing specs

Read all files matching `openspec/specs/**/*.md`. These are the currently specified capabilities. Use them as the baseline — do not propose work that is already fully covered.

### 4. Cluster snapshots into user journeys

Group the snapshots by user intent, not by URL. Use `journey_hint`, page title, headings, and URL patterns to identify clusters. Aim for 3–8 journeys total. Examples:

| Journey | Typical URLs / hints |
|---|---|
| Product browsing & category navigation | `/navigation/*`, nav pages |
| Product detail | `/detail/*` |
| Search | search results pages |
| Cart management | `/checkout/cart` |
| Checkout flow | `/checkout/confirm`, `/checkout/register` |
| Account & authentication | `/account/login`, `/account`, `/account/order` |
| Homepage | `/` |

Merge snapshots that clearly belong to the same flow. Discard snapshots with no user-visible content.

### 5. Diff against existing specs

For each journey cluster, compare the behaviors visible in the snapshots (buttons, forms, headings, flows) against the requirements in existing specs.

Classify each journey as one of:
- **new**: no existing spec covers this journey
- **gap**: an existing spec covers the journey but misses behaviors visible in the snapshots
- **covered**: fully covered — skip, do not propose

### 6. For each uncovered or gap journey — propose interactively

Work through journeys one at a time. For each:

a. **Show a proposal summary** to the user:
   - Journey name (kebab-case, e.g. `cart-management`)
   - Whether it is new or a gap update
   - 2–4 bullet points describing what would be specified
   - Which snapshot files informed it

b. **Ask the user** to approve or reject this proposal using the **AskUserQuestion tool**.

c. **If approved**:
   - Run `openspec new change "<journey-name>"` to scaffold the change directory
   - Write `openspec/changes/<journey-name>/proposal.md` following the spec-driven schema (Why / What Changes / Capabilities / Impact)
   - Announce: "Proposal created at `openspec/changes/<journey-name>/proposal.md`"

d. **If rejected**: skip and continue to the next journey.

### 7. Final summary

After all journeys have been processed, output:
- How many proposals were created (with links to their change directories)
- How many were skipped
- A reminder: "Run `/opsx:apply` on any proposal to start implementation."

## Guidelines

- Always re-run `./gradlew discover` — never read stale snapshots from a previous run.
- One proposal per journey cluster, not one per URL.
- Proposals are starting points: keep them concise (1 page). Full specs and tasks come during `/opsx:apply`.
- If a journey is ambiguous (could be new or gap), prefer treating it as a gap and noting what is missing.
- Do not create proposals for admin/back-office flows unless snapshots from `/admin` are present.
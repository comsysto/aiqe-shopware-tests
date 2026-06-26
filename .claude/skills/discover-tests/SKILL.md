---
name: discover-tests
description: Crawl the live Shopware storefront using a YAML script, inspect resulting snapshots, and hand back findings with suggested next steps. Follows a round model — one script per invocation.
---

Explore the live Shopware storefront by executing a YAML discovery script, then analyse the resulting snapshots and propose next steps or OpenSpec changes.

## Round model

Each invocation is one round:
1. Inspect existing snapshots (if any)
2. Decide what to explore next
3. Write or select a YAML script in `discovery-scripts/`
4. Execute it via `./gradlew discover -Pargs=<script-path>`
5. Read the new snapshots
6. Hand back to the user with findings and suggested next steps

Do **not** execute multiple scripts or loop autonomously within a single invocation.

---

## Steps

### 1. Inspect existing snapshots

Glob `build/discovery/*.json` and read every file found. Each snapshot has this structure:

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

If no snapshots exist, skip to step 3 and use `bootstrap.yml`.

### 2. Read existing specs

Read all files matching `openspec/specs/**/*.md`. Use them as the baseline — do not propose work that is already fully covered.

### 3. Decide what to explore next

Based on existing snapshots and specs, choose one of:
- **No prior snapshots** → run `discovery-scripts/bootstrap.yml`
- **Gap in coverage** → write a new script targeting the uncovered flow (e.g. add-to-cart, search, admin login)
- **New journey requested by user** → write a script for that specific flow

### 4. Write or select a YAML script

If writing a new script, save it to `discovery-scripts/<descriptive-name>.yml`.

Supported steps:

```yaml
steps:
  - open: /path-or-full-url
  - click: "CSS selector"
  - fill:
      selector: "CSS selector"
      value: "text to enter"
  - snapshot:
      name: journey-hint-slug    # used as filename and journey_hint
      auth_required: false
  - wait: 500                    # milliseconds
```

Keep scripts flat — no loops or conditionals. Write explicit steps for each action. If a flow branches, write two separate scripts and run them in separate rounds.

### 5. Execute the script

```bash
./gradlew discover -Pargs=<script-path>
```

Warn the user upfront: container startup takes up to 3 minutes.

If the task fails, report the error and stop.

### 6. Read new snapshots

Glob `build/discovery/*.json` again and read the newly created files. Note which `journey_hint` values are new.

### 7. Hand back to the user

Present:
- **What was explored**: which pages were visited (journey hints + URLs)
- **What was found**: notable elements, forms, buttons — anything that suggests a testable flow
- **Gap analysis**: compare against existing specs; call out uncovered behaviours
- **Suggested next steps**: specific scripts or flows to explore in the next round, or an OpenSpec proposal if coverage is clear

Do not proceed to the next round automatically. Wait for the user to steer.

---

## Guidelines

- Always execute a script before reading snapshots — never read stale snapshots from a previous run without running the crawler first.
- One script per round. If multiple flows need exploring, suggest them as options and let the user pick.
- Keep scripts small and focused — one journey per script.
- Use `discovery-scripts/bootstrap.yml` as the baseline for a fresh start.
- Do not create OpenSpec proposals automatically — present findings and let the user decide.
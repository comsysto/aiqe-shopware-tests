## MODIFIED Requirements

### Requirement: Runner supports the `snapshot` step
The `snapshot` step SHALL capture the current page state: write a JSON file and a PNG screenshot to `build/discovery/`, using the step's `name` field as the slug. The `auth_required` field (boolean, defaults to `false`) is written into the JSON. The optional `capture` field (list of CSS selectors) SHALL cause the outer HTML of every element matching each selector to be written into the JSON under `captured`, keyed by selector; a selector without matches SHALL map to an empty list rather than fail the step.

#### Scenario: Snapshot writes both JSON and PNG
- **WHEN** a snapshot step with `name: cart-page` is executed
- **THEN** `build/discovery/cart-page.json` is written with `url`, `title`, `journey_hint`, `elements`, and `auth_required`
- **THEN** `build/discovery/cart-page.png` is written as a valid PNG

#### Scenario: Duplicate snapshot names are deduplicated
- **WHEN** two snapshot steps share the same `name`
- **THEN** the second file is written as `<name>-2.json` / `<name>-2.png` without overwriting the first

#### Scenario: Captured selectors are written into the snapshot
- **WHEN** a snapshot step with `name: checkout-confirm` and `capture: [".checkout-aside-summary"]` is executed on a page containing one matching element
- **THEN** `build/discovery/checkout-confirm.json` contains `captured` with key `.checkout-aside-summary` mapping to a list holding that element's outer HTML

#### Scenario: Unmatched capture selector does not fail the snapshot
- **WHEN** a snapshot step captures a selector that matches no element
- **THEN** the snapshot is written and `captured` maps that selector to an empty list

#### Scenario: Snapshot without capture is unchanged
- **WHEN** a snapshot step has no `capture` field
- **THEN** the JSON is written exactly as before, without a `captured` entry

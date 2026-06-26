## Requirements

### Requirement: Runner reads and executes a YAML script file
The `DiscoveryRunner` SHALL accept a script file path as its first command-line argument and parse it as a YAML document containing a `steps` list. If no argument is provided or the file does not exist, the runner SHALL exit with a non-zero status and a descriptive error message.

#### Scenario: Valid script path executes successfully
- **WHEN** `./gradlew discover --args="discovery-scripts/bootstrap.yml"` is executed
- **THEN** the runner reads the YAML file and executes each step in order

#### Scenario: Missing script path fails fast
- **WHEN** `./gradlew discover` is executed without `--args`
- **THEN** the runner prints an error message indicating a script path is required and exits with a non-zero code

#### Scenario: Non-existent script file fails fast
- **WHEN** `./gradlew discover --args="discovery-scripts/nonexistent.yml"` is executed
- **THEN** the runner prints an error message indicating the file was not found and exits with a non-zero code

### Requirement: Runner supports the `open` step
The `open` step SHALL navigate the browser to the given URL or path. If the value starts with `http`, it is treated as an absolute URL; otherwise it is appended to the Shopware base URL.

#### Scenario: Relative path is resolved against base URL
- **WHEN** a step `open: /account/login` is executed
- **THEN** the browser navigates to `<baseUrl>/account/login`

#### Scenario: Absolute URL is used as-is
- **WHEN** a step `open: http://example.com/page` is executed
- **THEN** the browser navigates to `http://example.com/page`

### Requirement: Runner supports the `click` step
The `click` step SHALL locate the element matching the given CSS selector and click it. If no matching element is found within the Selenide default timeout, the runner SHALL throw a descriptive exception that includes the selector.

#### Scenario: Element is found and clicked
- **WHEN** a step `click: "button.btn-primary"` is executed
- **THEN** the matching button is clicked

### Requirement: Runner supports the `fill` step
The `fill` step SHALL locate the element matching the given `selector` field and set its value to the given `value` field using Selenide's `setValue()`.

#### Scenario: Input field is filled
- **WHEN** a step with `selector: "input[name='email']"` and `value: "user@example.com"` is executed
- **THEN** the input field contains `user@example.com`

### Requirement: Runner supports the `snapshot` step
The `snapshot` step SHALL capture the current page state: write a JSON file and a PNG screenshot to `build/discovery/`, using the step's `name` field as the slug. The `auth_required` field (boolean, defaults to `false`) is written into the JSON.

#### Scenario: Snapshot writes both JSON and PNG
- **WHEN** a snapshot step with `name: cart-page` is executed
- **THEN** `build/discovery/cart-page.json` is written with `url`, `title`, `journey_hint`, `elements`, and `auth_required`
- **THEN** `build/discovery/cart-page.png` is written as a valid PNG

#### Scenario: Duplicate snapshot names are deduplicated
- **WHEN** two snapshot steps share the same `name`
- **THEN** the second file is written as `<name>-2.json` / `<name>-2.png` without overwriting the first

### Requirement: Runner supports the `wait` step
The `wait` step SHALL pause execution for the given number of milliseconds. This is used to allow AJAX-driven state changes to settle before taking a snapshot.

#### Scenario: Wait pauses execution
- **WHEN** a step `wait: 500` is executed
- **THEN** the runner pauses for approximately 500 milliseconds before proceeding to the next step

### Requirement: Runner reports unknown step types as errors
If a YAML step contains a key that is not in the supported vocabulary (`open`, `click`, `fill`, `snapshot`, `wait`), the runner SHALL exit with a non-zero code and a message identifying the unknown step type.

#### Scenario: Unknown step causes early exit
- **WHEN** a script contains a step with key `hover` (unsupported)
- **THEN** the runner exits with an error message before executing any further steps

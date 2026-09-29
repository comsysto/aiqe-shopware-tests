## ADDED Requirements

### Requirement: Any test journey can log in as a given customer
The test suite SHALL provide a single entry point, `CustomerSession.loginAs(Customer)`, that logs the browser session in as the given customer. Test journeys SHALL use this entry point instead of interacting with the login form directly.

#### Scenario: Journey logs in as the demo customer
- **WHEN** a test calls `CustomerSession.loginAs(Customer.DEMO)` from a logged-out browser session
- **THEN** the storefront session is authenticated as `test@example.com` and the account overview (`/account`) is reachable without a login prompt

### Requirement: Demo customer is available as a predefined customer
The test suite SHALL expose the dockware seeded storefront customer as `Customer.DEMO` with email `test@example.com` and password `shopware`, defined in one place.

#### Scenario: Demo credentials are centralised
- **WHEN** a test needs the demo customer
- **THEN** it references `Customer.DEMO` rather than repeating the credentials

### Requirement: Login page object encapsulates the login form
A `LoginPage` page object SHALL encapsulate the `/account/login` form using the ID selectors `#loginMail`, `#loginPassword` and the submit button `.login-submit button[type='submit']`.

#### Scenario: Login form submission lands on the account overview
- **WHEN** valid customer credentials are entered via `LoginPage` and submitted
- **THEN** the browser is on `/account` showing the "Overview" heading

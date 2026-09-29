## ADDED Requirements

### Requirement: Every test class reports narrated, screenshotted steps
Each of `CustomerSessionTest`, `CartManagementTest`, `StorefrontSmokeTest` and `CheckoutTest` SHALL
produce a Serenity report in which its business-meaningful actions appear as named steps, each with
at least one screenshot, using the `@Steps`/`@Step` field-injection mechanism (no AspectJ, no new
dependency).

#### Scenario: A test's report lists its business steps by name
- **WHEN** `CheckoutTest`'s `CO-1` test runs
- **THEN** the Serenity JSON result for that test contains a `testSteps` entry for adding the
  product to the cart, for reading the confirm summary, for accepting the terms and conditions, for
  submitting the order, for reading the finish page, and for finding the order in the order history

#### Scenario: Every reported step carries a screenshot
- **WHEN** any narrated step in any of the four test classes completes successfully
- **THEN** its `testSteps` entry in the Serenity JSON result contains at least one screenshot entry,
  and the referenced PNG file exists and shows the storefront page at that point

### Requirement: The Selenide-managed driver is bridged into Serenity before narrated steps run
Each test class SHALL register the live Selenide WebDriver with Serenity
(`Serenity.useDriver(WebDriverRunner.getWebDriver())`) once the driver exists and before the first
narrated `@Step` executes, so that step gets a screenshot too.

#### Scenario: The first narrated step of a test still gets a screenshot
- **WHEN** a test's very first `@Step`-annotated action runs
- **THEN** its `testSteps` entry has at least one screenshot, not zero

### Requirement: Login is narrated as a step in every test that logs in
`CustomerSession.loginAs(Customer)` SHALL be callable through an injected `@Steps` field and SHALL
appear as a named, screenshotted step in the report of every test that calls it.

#### Scenario: Login appears in the report
- **WHEN** a test calls `customerSession.loginAs(Customer.DEMO)` through an injected
  `@Steps CustomerSession` field
- **THEN** the report shows a step describing the login, with a screenshot of the resulting page

### Requirement: Plumbing actions are not narrated as report steps
Clearing browser cookies, dismissing the cookie consent banner, and starting the Dockware container
SHALL NOT appear as named steps in the report.

#### Scenario: Cookie banner dismissal is invisible in the report
- **WHEN** a test's setup clears cookies and dismisses the cookie banner before its first business
  action
- **THEN** the report's `testSteps` list does not contain an entry for either of these actions

### Requirement: Business page-transition actions have a page-object home and are narrated
Opening a category listing, opening a product's detail page, reading its name and number, and
adding it to the cart SHALL each be exposed as an `@Step`-annotated page-object method, reachable
through an injected `@Steps` field, so they are narrated wherever a test performs them.

#### Scenario: Add-to-cart is narrated in the checkout journey
- **WHEN** `CheckoutTest` adds a product to the cart via an injected page object's `addToCart()`
  step
- **THEN** the report shows a step for adding the product, with a screenshot of the cart or product
  page reflecting the action

#### Scenario: Add-to-cart is narrated in the cart-management journey
- **WHEN** `CartManagementTest`'s setup adds a product to the cart via the same page-object step
- **THEN** the report shows the same kind of narrated step for that test too

### Requirement: Emptying a restored cart is narrated as a step
`CheckoutTest`'s guarantee that checkout starts from an empty cart SHALL be exposed as an
`@Step`-annotated `CartPage` method, not a private loop inside the test, and SHALL appear as a
named step when it actually removes a leftover item.

#### Scenario: A leftover cart item being removed is visible in the report
- **WHEN** the demo customer's restored cart contains an item at the start of `CheckoutTest`
- **THEN** the report shows a step describing the cart being emptied, with a screenshot of the
  resulting empty cart

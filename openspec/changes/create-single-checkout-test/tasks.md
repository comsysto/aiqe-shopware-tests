## 1. Discovery tooling

- [x] 1.1 Extend `DiscoveryRunner`'s `snapshot` step with the optional `capture` selector list, writing outer HTML under `captured` (crawler-script-runner: captured selectors, unmatched selector, snapshot without capture).
- [x] 1.2 Update the `discover-tests` skill's supported-steps documentation with the `capture` option.

## 2. Discovery

- [x] 2.1 Write and run `discovery-scripts/explore-login.yml`: confirmed login route, selectors, demo credentials, and empty cart after login.
- [x] 2.2 Write `discovery-scripts/explore-checkout.yml`: log in → add product (snapshot product detail with name/number capture) → `/checkout/confirm` (snapshot with capture of line items, totals, payment/shipping method, addresses, T&C checkbox, submit button) → accept T&C → submit → `/checkout/finish` (snapshot with capture of order number, line items, totals, methods, addresses) → `/account/order` (snapshot with capture of the order list).
- [x] 2.3 Run `explore-checkout.yml` via `/discover-tests` and derive the selectors for all `OrderSummary` fields on the confirm and finish pages, the order number, and the order-history entries. Note formatting differences between confirm and finish.
- [x] 2.4 Determine how to empty a cart restored on login (reuse `CartPage` remove action or clear via the cart page) and confirm selectors if new ones are needed.

## 3. Customer session (cross-cutting login)

- [x] 3.1 Write an ADR in `docs/adr/` for login as a cross-cutting concern (`CustomerSession.loginAs(Customer)`, UI login now, swappable mechanism later).
- [x] 3.2 Add `Customer` (email, password) with `Customer.DEMO`.
- [x] 3.3 Add `LoginPage` page object for `/account/login` using `#loginMail`, `#loginPassword`, `.login-submit button[type='submit']`.
- [x] 3.4 Add `CustomerSession.loginAs(Customer)` using `LoginPage` and asserting arrival on the account overview.

## 4. Checkout page objects

- [x] 4.1 Add `OrderSummary` value object (line items with name, product number, quantity, line total, line tax; subtotal; shipping cost; tax; grand total; payment method; shipping method; billing address; shipping address).
- [x] 4.2 Add `CheckoutPage` (`/checkout/confirm`): read `OrderSummary`, accept T&C, submit order.
- [x] 4.3 Add `FinishPage` (`/checkout/finish`): read `OrderSummary` and order number.
- [x] 4.4 Add `AccountOrderPage` (`/account/order`): open and check whether an order number is listed.

## 5. Test implementation

- [x] 5.1 Add `CheckoutTest` with `@BeforeAll` mirroring `CartManagementTest` (base URL + browser size) and `@BeforeEach`: `clearBrowserCookies()`, dismiss cookie banner, `CustomerSession.loginAs(Customer.DEMO)`, ensure empty cart (checkout-flow: checkout starts from an empty cart).
- [x] 5.2 Implement the happy-path test: add a product from its detail page (remember name and number), proceed to checkout, assert confirm line items against the product (checkout-flow: cart contents carried into checkout confirm), read the confirm `OrderSummary`, accept T&C, submit, assert finish page reached (successful order placement), assert finish `OrderSummary` equals confirm via `usingRecursiveComparison()` and order number is non-empty (finish page matches confirm page), open `/account/order` and assert the order number is listed (order number found in order history).

## 6. Verification

- [x] 6.1 Run `./gradlew test` and confirm `CheckoutTest` passes alongside the full existing suite in one shared container (no regressions in `CartManagementTest` or `StorefrontSmokeTest`).
- [ ] 6.2 Review the Serenity report for the new test to confirm reporting/screenshots render as expected.
  - Reviewed (2026-09-28), not confirmed as expected. Before this change Serenity recorded nothing for any test (JUnit 5 tests, only the JUnit 4 `serenity-junit` on the classpath). Now `serenity-junit5` is added and all four test classes carry `@ExtendWith(SerenityJUnit5Extension.class)`; `./gradlew aggregate --rerun` regenerates the report (plain `aggregate` is skipped as UP-TO-DATE). The `CheckoutTest` page renders outcome SUCCESS, 5.01 s, breadcrumbs and a duration tag, but the title is shortened to "Co-1" (the `@DisplayName` text is only in the result JSON) and the Steps table is empty with no screenshots, because the tests record no Serenity steps and Serenity is not hooked into Selenide. Decision needed: accept outcomes-only reporting, or follow up with step/screenshot integration.

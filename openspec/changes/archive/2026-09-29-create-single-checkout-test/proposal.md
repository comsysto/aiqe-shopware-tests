## Why

Cart management is covered end-to-end (add, update quantity, remove) but no test exercises the checkout flow itself. Without it, a regression in the confirm step, order placement, or order persistence would go undetected even though checkout is the single most business-critical journey in the storefront.

## What Changes

- Add a reusable, cross-cutting login entry point (`CustomerSession.loginAs(Customer)`) backed by a `LoginPage` page object, callable from any test journey. The dockware demo customer (`test@example.com` / `shopware`) is exposed as `Customer.DEMO` — credentials and login selectors were confirmed by the `explore-login.yml` discovery round.
- Add a `CheckoutPage` (`/checkout/confirm`), a `FinishPage` (`/checkout/finish`) and an `AccountOrderPage` (`/account/order`) page object.
- Add an `OrderSummary` value object (line items, totals, payment/shipping method, addresses) read from both the confirm and the finish page, so the test can compare them field by field.
- Add a `CheckoutTest` covering the happy path for the logged-in demo customer: log in → ensure empty cart → add product → proceed to checkout → verify confirm page against the added product → accept T&C → submit → verify finish page matches the confirm page and shows an order number → look up that order number in the account order history.
- Extend the discovery runner's `snapshot` step with an optional list of selectors whose HTML is captured, so price, address and order-number selectors can be grounded before the page objects are written.
- Record an ADR for login as a cross-cutting concern.

## Capabilities

### New Capabilities
- `checkout-flow`: Tests covering the logged-in shopper's checkout journey from cart through order placement to the order appearing in the account order history.
- `customer-session`: A reusable login entry point that any test journey can call to start as a given customer.

### Modified Capabilities
- `crawler-script-runner`: The `snapshot` step accepts an optional `capture` list of CSS selectors whose outer HTML is written into the snapshot JSON.

## Impact

- New files (test sources, package `de.comsystoreply.aiqe.aiqeshopwaretests`): `Customer`, `CustomerSession`, `LoginPage`, `CheckoutPage`, `FinishPage`, `AccountOrderPage`, `OrderSummary`, `CheckoutTest`.
- Modified: `DiscoveryRunner` (snapshot `capture` option).
- New discovery scripts: `discovery-scripts/explore-login.yml` (already run), `discovery-scripts/explore-checkout.yml`.
- New ADR under `docs/adr/`.
- No changes to `CartPage` or `DockwareContainer`. The shared per-JVM container is kept; no fresh-container mechanism is introduced.
- Modified: `build.gradle` adds `serenity-junit5`, and every test class (`CartManagementTest`, `StorefrontSmokeTest`, `CustomerSessionTest`, `CheckoutTest`) gets `@ExtendWith(SerenityJUnit5Extension.class)` so the suite appears in the Serenity report. No test logic changes.
- Modified: `build.gradle` starts Chrome with `--force-prefers-reduced-motion` for the `test` and `discover` tasks, which switches off the storefront's smooth scrolling that made clicks on initially off-screen elements intermittently fail in every journey.

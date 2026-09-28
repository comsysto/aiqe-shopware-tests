## Context

`CartManagementTest` already gets a shopper as far as `/checkout/cart` and exposes `CartPage.proceedToCheckout` (`a[href*='/checkout/confirm']`). Nothing beyond that point — the checkout-confirm page, order placement, or order history — has been tested yet.

A first discovery round (`discovery-scripts/explore-login.yml`, run 2026-09-28 against a fresh `dockware/dev:latest` container) confirmed:

- Login route is `/account/login`; selectors `#loginMail`, `#loginPassword`, `.login-submit button[type='submit']` work. The email input's `name` is `username` — the same page also hosts a registration form with its own `email` field, so name-based selectors are ambiguous; use the IDs.
- The seeded demo customer `test@example.com` / `shopware` exists; login lands on `/account` ("Overview").
- On a fresh database the demo customer's cart is empty right after login.

The confirm, finish and account-order pages are **not yet grounded**. The existing `build/discovery/account-order` snapshot was taken while logged out and shows the login page, not the order history. The current snapshot format (nav links, buttons, form field names, headings) also cannot ground selectors for prices, line items, addresses or order numbers.

## Goals / Non-Goals

**Goals:**
- One reliable happy-path test: logged-in customer, add product, checkout, and a thorough check of what the storefront reports at each step, ending with the order found in the account order history.
- Login usable from any journey through one entry point.
- All new selectors grounded by discovery before any page object is written.

**Non-Goals:**
- Guest checkout / address form entry (possible follow-up `guest-checkout` capability).
- Payment or shipping method variation — the test uses the demo customer's defaults and records whatever they are.
- Multi-item carts, coupons.
- A per-test or per-class fresh-container mechanism (see Decisions).

## Decisions

- **Logged-in demo customer over guest checkout**: avoids the multi-field address form, the main flakiness source in checkout E2E tests. Credentials are confirmed (see Context).

- **Login as a cross-cutting concern — `CustomerSession.loginAs(Customer)`**:
  ```
  any journey's @BeforeEach
          │
          ▼
  CustomerSession.loginAs(Customer.DEMO)      Customer: email, password
          │ uses                              DEMO = test@example.com / shopware
          ▼
  LoginPage (POM for /account/login)
  ```
  Journeys depend only on `CustomerSession`, never on login-form selectors. The mechanism can later switch (e.g. Store API / cookie login for speed) without touching any journey, and `Customer` as a parameter allows per-journey customers if sharing the demo customer ever causes interference. Recorded as an ADR (project rule: non-trivial architectural decisions get an ADR).

- **One page object per route**: `CartPage` (`/checkout/cart`, existing) → `CheckoutPage` (`/checkout/confirm`) → `FinishPage` (`/checkout/finish`) → `AccountOrderPage` (`/account/order`). Keeps the existing one-page-object-per-route pattern.

- **Thorough comparison via an `OrderSummary` value object**:
  ```
  ProductPage ──▶ CheckoutPage ──────────────▶ FinishPage ──────────▶ AccountOrderPage
  name, number    OrderSummary (confirm)  ==   OrderSummary (finish)   order number listed
                  line items match product      + order number present
  ```
  `OrderSummary` holds line items (name, product number, quantity, unit price, line total), subtotal, shipping cost, tax, grand total, payment method, shipping method, billing and shipping address. `CheckoutPage` and `FinishPage` each build one from their page; the test compares them with AssertJ `usingRecursiveComparison()`, so any field that differs is reported by name. The confirm-page line items are additionally checked against the product name/number read on the product detail page and quantity 1. Monetary values are compared as displayed strings unless discovery shows confirm and finish format them differently, in which case they are parsed to `BigDecimal`.

- **Order persistence checked via account order history**: after the finish page, the test opens `/account/order` and asserts the order number from the finish page is listed. This proves the order was persisted, not only rendered.

- **Shared environment by default; fresh only when required**: `DockwareContainer` stays a per-JVM singleton; tests run sequentially and share it. Checkout does not require a fresh container. Its only real interference with other journeys is the demo customer's persisted cart: Shopware restores a logged-in customer's saved cart on the next login, so a checkout test that fails after add-to-cart would leave an item behind. `CheckoutTest` therefore guarantees a clean start itself — after login it empties the cart if it is not empty — instead of relying on database freshness. Placed orders and stock decrements do not affect other tests. A fresh-container mechanism is deferred until a test genuinely requires one.

- **Session isolation via `clearBrowserCookies()`**: reused from `CartManagementTest`'s `@BeforeEach` so each test starts logged out and exercises login.

- **Discovery snapshot `capture` option**: the `snapshot` step gains an optional `capture` list of CSS selectors; the outer HTML of each match is written into the snapshot JSON under `captured`. This grounds selectors for text-bearing elements (prices, addresses, order number) that the fixed element categories do not record. Small, backward-compatible tooling change; existing scripts are unaffected.

- **New `discovery-scripts/explore-checkout.yml`**: log in → add product → cart → `/checkout/confirm` (snapshot with capture) → accept T&C → submit → `/checkout/finish` (snapshot with capture) → `/account/order` (snapshot with capture). Ephemeral container, so the placed order is harmless.

## Risks / Trade-offs

- [Confirm page has AJAX-driven totals/payment widgets] → Follow the `wait`-after-AJAX pattern in discovery; in tests use Selenide conditions, not sleeps.
- [Confirm and finish pages render the same value in different formats or markup] → Discovery captures both; `OrderSummary` readers normalise per page, comparison stays on the value object.
- [Account order list shows only recent orders / is paginated] → A fresh-per-JVM container has few orders; if discovery shows pagination, assert on the first page (newest first).
- [`baseUrl` override points at a long-lived shop] → State may accumulate; the empty-cart precondition keeps the test valid, order lookup by exact order number is unaffected.
- [Demo customer credentials change in a future dockware image] → Centralised in `Customer.DEMO`; one place to update.

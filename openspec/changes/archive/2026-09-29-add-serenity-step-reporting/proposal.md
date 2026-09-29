## Why

The Serenity report currently shows only pass/fail and duration per test — no narrative, no
screenshots. It doesn't stand on its own: someone who wasn't there needs the code to understand
what a test actually did. Two spikes (branch `spike/serenity-step-screenshots`, not merged) proved
that a fully narrated, illustrated report is achievable with what's already on the classpath — no
AspectJ, no new dependency — via Serenity's `@Steps` field-injection mechanism plus a small bridge
that registers Selenide's externally-managed WebDriver with Serenity. This change applies that
proven mechanism across the whole suite so the report becomes a shareable artifact: every step
named, every step illustrated with a real screenshot of the page at that point.

## What Changes

- Every test class (`CustomerSessionTest`, `CartManagementTest`, `StorefrontSmokeTest`,
  `CheckoutTest`) narrates its actions as named Serenity steps, each producing a screenshot.
- Relevant page-object methods gain `@Step("...")` annotations; each test class replaces its plain
  page-object fields with `@Steps`-annotated fields so Serenity can inject its reporting proxy.
- Each test's setup calls `Serenity.useDriver(WebDriverRunner.getWebDriver())` once the Selenide
  driver actually exists (it's created lazily on the first `open()`) and before the first step that
  should be screenshotted.
- **BREAKING (design decision, not yet made)**: `CustomerSession.loginAs(Customer)` is `static`
  (per ADR 0001) and cannot be proxied by this mechanism. `design.md` resolves whether
  `CustomerSession` becomes instance-based and `@Steps`-injectable (revising ADR 0001), or stays
  static with the login narrative coming from `LoginPage` directly.
- `design.md` also resolves the plumbing-vs-narrated-step boundary (cookie banner dismissal, cookie
  clearing, container startup, and `CheckoutTest`'s loose inline add-to-cart logic, which currently
  has no page-object home at all) — whether these become narrated+screenshotted steps or stay silent
  setup.
- No production code changes; no new dependency (`serenity-junit5` is already present); no
  `build.gradle` changes expected.

## Capabilities

### New Capabilities
- `test-step-reporting`: the convention and mechanism by which the test suite narrates its actions
  as named, screenshotted Serenity report steps — the `@Steps`/`@Step` pattern, the driver-bridge
  call, and the plumbing-vs-narrated boundary. This documents how tests report, the same way
  `test-discovery-crawler` documents how discovery runs, not a shopper-facing behavior.

### Modified Capabilities
(none — `cart-management` and `storefront-smoke-test` describe shopper-facing behavior and page
objects; this change narrates the same verified behavior, it does not change what is verified or
required)

## Impact

- Modified (test sources only, package `de.comsystoreply.aiqe.aiqeshopwaretests`): `CustomerSession`
  (design pending), `LoginPage`, `CartPage`, `CheckoutPage`, `FinishPage`, `AccountOrderPage`, and
  all four test classes.
- Possible new page object/class to give the currently-loose "add product to cart" logic in
  `CheckoutTest` a steppable home (design pending).
- No changes to `DockwareContainer`, `DiscoveryRunner`, or any production/non-test code.
- No new dependency; `net.serenitybdd.annotations.{Step,Steps}` and `net.serenitybdd.core.Serenity`
  are already reachable via the existing `serenity-junit5`/`serenity-core` dependencies.

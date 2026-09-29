## Context

The Serenity report today records only outcome and duration per test (added in the
`create-single-checkout-test` change). No test anywhere in the suite has ever used a Serenity
`@Step`. Two spikes (branch `spike/serenity-step-screenshots`, commit `0d39a57`, not merged) proved
a mechanism that produces named, screenshotted steps with nothing already on the classpath:

- Serenity's `SerenityJUnit5Extension` (already a dependency) injects a dynamic proxy into any field
  annotated `@Steps`. A call to an `@Step`-annotated method through that field becomes a named
  report step; no AspectJ, no new dependency. This only intercepts **instance** methods called
  *through the injected field* — a call bypassing the field, or a `static` method, is invisible to it.
- Screenshots require `Serenity.useDriver(WebDriverRunner.getWebDriver())`, called once the Selenide
  driver actually exists (it's created lazily on the first `open()`) and before the first step that
  should get a screenshot. Verified on both a 1-object/2-step test and a 3-object/6-step test
  (`CheckoutPage`, `FinishPage`, `AccountOrderPage` interleaved), with correct chronological
  ordering across objects and no interference with existing assertions (including
  `usingRecursiveComparison()`).
- `target/site/serenity` (where screenshots land) is gitignored; this is a local/CI artifact, never
  committed.

Two things stand in the way of applying this everywhere as-is:
1. `CustomerSession.loginAs(Customer)` (ADR 0001) is a `static` utility method — structurally
   incompatible with proxy-based step interception.
2. Several actions have no page-object home at all today. Most notably, `CheckoutTest`'s "given"
   block — navigate to a category, open a product, read its name/number, add it to the cart — is
   raw Selenide calls inline in the test method, and `CheckoutTest.ensureEmptyCart()` is a private
   loop calling `CartPage` repeatedly, not a step of its own.

## Goals / Non-Goals

**Goals:**
- Every one of the four test classes (`CustomerSessionTest`, `CartManagementTest`,
  `StorefrontSmokeTest`, `CheckoutTest`) produces a Serenity report with named steps and a real
  screenshot per step, standing on its own without needing the code to interpret it.
- The mechanism proven in the spikes is reused as-is: `@Steps` field injection +
  `Serenity.useDriver(...)` bridge. No AspectJ, no new dependency.
- Business-meaningful actions currently living as raw inline Selenide calls get a proper
  page-object home so they can be narrated, consolidating duplicated code in the process (the
  "click first nav link" sequence, for instance, is currently duplicated near-identically in three
  test classes).

**Non-Goals:**
- No rewrite to the Screenplay pattern (actors/tasks/abilities). This stays Page Object Model,
  extended with `@Step` annotations.
- No change to what any test asserts or verifies — this narrates existing behavior, it does not
  change it. No spec in `openspec/specs/` has its requirements modified by this change.
- No attempt to capture screenshots on failure specifically, or to change Serenity's default
  screenshot-on-step-completion behavior — the spikes show the default behavior is already what's
  wanted.
- No production code is touched.

## Decisions

### `CustomerSession` becomes instance-based and `@Steps`-injectable (amends ADR 0001)

Login happens in every one of the four test classes — it is the first, and often most important,
step in every journey's story. Leaving it out of the narrative because of an implementation detail
(`static`) would mean every report's story silently skips its own opening step. `CustomerSession`
is changed from a static utility class to an ordinary class with an instance `loginAs(Customer)`
method, annotated `@Step("Log in as {0}")`, delegating to an injected `LoginPage`. Tests declare
`@Steps CustomerSession customerSession;` instead of calling `CustomerSession.loginAs(...)`
statically.

This **amends, but does not reverse, ADR 0001**: the core intent — journeys depend on
`CustomerSession` alone, never touch `LoginPage`'s selectors, and the login mechanism can be
swapped later without touching any journey — is fully preserved. Only the Java-level mechanics
change (instance rather than static), because ADR 0001 was written before step reporting existed as
a requirement. Recorded as ADR 0002.

*Alternative considered*: keep `CustomerSession` static, let the narrative start at the first
`LoginPage`/page-object call after login returns. Rejected: login is not incidental — it is exactly
the kind of step a shareable report should show, and every one of the four test classes pays this
cost, so the gap would appear in the whole suite's report, not just one test.

### Plumbing stays silent; page-transition actions are narrated

Not every line of setup earns a named step with a screenshot. The line is drawn by what a
screenshot would actually communicate:

| Action | Narrated? | Why |
|---|---|---|
| `DockwareContainer.start()` (`@BeforeAll`) | No | One-time JVM setup, no browser exists yet — there is nothing to screenshot |
| `clearBrowserCookies()` | No | No visual effect; a before/after screenshot pair would be identical |
| Dismissing the cookie banner | No | Runs identically in every test; a near-duplicate screenshot in every report adds noise, not narrative |
| "Click first nav link" → category listing | **Yes** | A real page transition; currently duplicated raw Selenide across 3 test classes |
| Open a product, read name/number | **Yes** | Core to the checkout story (`checkout-flow` spec's "cart contents carried into checkout confirm" scenario depends on this exact data) |
| Add product to cart | **Yes** | The central action of both `cart-management` and `checkout-flow` |
| Ensure the cart is empty (`CheckoutTest.ensureEmptyCart`) | **Yes** | This is not incidental plumbing — it's the exact behavior the `checkout-flow` spec's "Leftover cart item is removed after login" requirement describes. It deserves to be shown, not hidden inside a private loop. |

*Alternative considered*: narrate everything literally, including cookie banner and cookie
clearing. Rejected: this produces reports where roughly a third of the steps are the same generic
"dismiss banner" screenshot repeated across every test, which actively hides the parts of the story
that differ between tests. A report that shows everything shows nothing in particular.

### New/changed page objects to give loose logic a steppable home

- **`StorefrontPage`** (existing, currently 3 plain fields) gains `@Step` methods:
  `openFirstCategory()` (wraps the nav-link click currently duplicated in `CartManagementTest`,
  `StorefrontSmokeTest`, and `CheckoutTest`'s given block) and `search(String term)` (wraps
  `StorefrontSmokeTest`'s inline search sequence).
- **New `ProductDetailPage`**: `readName()`, `readNumber()`, `addToCart()` — replaces the raw
  `$(".product-detail-name")`/`$(".product-detail-ordernumber")`/`button.btn-buy` calls currently
  inline in `CartManagementTest.addProductToCart()` and `CheckoutTest`'s given block.
- **`CartPage`** (existing) gains `ensureEmpty()` as a `@Step`-annotated method, moving
  `CheckoutTest.ensureEmptyCart()`'s while-loop body into the page object it already operates on.
- All page objects gaining `@Step` methods are referenced from tests via `@Steps` fields, replacing
  the current `private final PageObject x = new PageObject();` pattern.
- The driver-bridge call (`Serenity.useDriver(WebDriverRunner.getWebDriver())`) is added once per
  test class, in `@BeforeEach`, right after the first `open(...)` call and before any `@Step` method
  is invoked — the exact placement verified in both spikes.

### Retrofit is applied to, and independently verified on, all four test classes

`CartManagementTest` and `StorefrontSmokeTest` were not touched by either spike. The recipe is
expected to transfer directly (it is the same mechanism, applied to simpler, single-page tests), but
`tasks.md` verifies each class independently — compiles, passes, and produces the expected
`testSteps`/screenshots in the Serenity JSON output — rather than assuming the CheckoutTest result
generalizes.

## Risks / Trade-offs

- [Screenshot volume] → With ~3-6 narrated steps per test across four test classes, expect on the
  order of 10-40 PNGs per full suite run, each roughly 100KB (measured in the spikes). `target/site/serenity`
  is gitignored, so this is a local/CI artifact concern only, not a repository size concern.
- [Per-step screenshot count is not perfectly predictable] → Spikes showed 1-2 screenshots per step
  depending on whether it's the first step after the driver bridge or a later one; this is Serenity's
  internal step-lifecycle behavior, not something this change controls or needs to control. Tasks
  verify screenshots exist and are correct, not an exact count.
- [`@Steps` field injection silently does nothing if a call bypasses the field] → e.g. calling
  `new ProductDetailPage().readName()` directly instead of through the injected field produces no
  step at all, with no error. Mitigated by code review per task (see `tasks.md`) checking every
  narrated action is actually routed through an `@Steps` field.
- [Amending ADR 0001 touches an already-reviewed architectural decision] → Scoped narrowly (static
  → instance, same external contract for callers) and recorded as an explicit new ADR (0002)
  rather than silently edited, so the history of *why* stays intact.

## Open Questions

None — the mechanism is proven by spike, and the decisions above resolve every ambiguity the
proposal raised. `tasks.md` is expected to surface line-level naming questions (exact step wording)
that don't need a design-level decision.

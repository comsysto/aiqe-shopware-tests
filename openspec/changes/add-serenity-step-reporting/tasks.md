## 1. Architecture decision

- [x] 1.1 Write ADR 0002 in `docs/adr/` amending ADR 0001: `CustomerSession` becomes instance-based
      and `@Steps`-injectable so login can be narrated; the single-entry-point intent of ADR 0001 is
      preserved.

## 2. Cross-cutting session

- [x] 2.1 Convert `CustomerSession` from a static utility to an instance class holding an injected
      `LoginPage`, with an instance `loginAs(Customer)` method annotated `@Step("Log in as {0}")`.
- [x] 2.2 Add `@Step` to `LoginPage.open()` and `LoginPage.logInAs(Customer)`.

## 3. Shared page objects for narrated business actions

- [x] 3.1 Add `@Step`-annotated `openFirstCategory()` and `search(String term)` methods to
      `StorefrontPage`, replacing the near-duplicated raw nav-click/search Selenide calls currently
      inline in `CartManagementTest`, `StorefrontSmokeTest`, and `CheckoutTest`.
- [x] 3.2 Add a new `ProductDetailPage` page object with `@Step`-annotated `readName()`,
      `readNumber()`, and `addToCart()` methods, replacing the raw `.product-detail-name` /
      `.product-detail-ordernumber` / `button.btn-buy` calls currently inline in
      `CartManagementTest.addProductToCart()` and `CheckoutTest`'s given block.
      - Also added `openFirstListedProduct()`, wrapping the `.product-box a.product-name` click.
        Not literally listed in this task, but design.md's plumbing table calls for "Open a
        product" to be narrated too, and this is the page object it naturally belongs on.
- [x] 3.3 Add `@Step`-annotated `increaseQuantity(int index)` and `removeItem(int index)` methods to
      `CartPage` (wrapping the click+assert pairs `CartManagementTest`'s CM-2/CM-3 currently do
      inline), and a `@Step`-annotated `ensureEmpty()` method that moves
      `CheckoutTest.ensureEmptyCart()`'s while-loop body into `CartPage` itself.
      - `ensureEmpty()` does NOT call `removeItem()` internally (a step's own internal call on
        `this` bypasses the `@Steps` proxy and would not be narrated), so it keeps its own copy of
        the remove-and-wait logic to stay a single reported step, per the spec's "narrated as a
        step" (singular) requirement.
      - Purely additive: no existing `CartPage` method signature changed, so `CartManagementTest`
        and `CheckoutTest` are unaffected until their own retrofit tasks touch them.

## 4. CustomerSessionTest

- [x] 4.1 Replace the static `CustomerSession.loginAs(...)` call with an injected
      `@Steps CustomerSession customerSession` field; add
      `Serenity.useDriver(WebDriverRunner.getWebDriver())` in `@BeforeEach`, after the driver exists
      and before the login step runs (test-step-reporting: driver bridged before narrated steps run).
- [x] 4.2 Run `./gradlew cleanTest test --tests '*CustomerSessionTest'`; confirm CS-1 still passes
      and its Serenity JSON result shows a login step with at least one screenshot
      (test-step-reporting: login is narrated).
      - Verified: full suite (`./gradlew cleanTest test`) compiles and passes. CS-1's JSON shows a
        parent step "Log in as Customer[...]" (2 screenshots) with two nested child steps, "Open
        the login page" and "Enter credentials for ..." (each with their own screenshots) — richer
        than the minimum required, since `CustomerSession.loginAs` itself delegates through an
        injected `@Steps LoginPage`.

## 5. CartManagementTest

- [x] 5.1 Replace the inline add-to-cart logic in `addProductToCart()` with
      `storefrontPage.openFirstCategory()` and `productDetailPage.readName()/readNumber()/addToCart()`
      through injected `@Steps` fields; replace the inline quantity-increase and remove calls in
      CM-2/CM-3 with `cartPage.increaseQuantity(0)`/`cartPage.removeItem(0)` through an injected
      `@Steps CartPage` field; add the driver-bridge call in `@BeforeEach`.
      - Used `productDetailPage.openFirstListedProduct()` (added in task 3.2) instead of
        `readName()/readNumber()` for the navigation itself; name/number aren't read in this test.
- [x] 5.2 Run `./gradlew cleanTest test --tests '*CartManagementTest'`; confirm CM-1/2/3 still pass
      and each test's Serenity JSON result shows the expected narrated steps with screenshots
      (test-step-reporting: add-to-cart narrated in the cart-management journey).
      - **Found and fixed a cross-class bug during full-suite verification** (not caught by the
        spikes or by per-class `--tests` filters, which never exercise two `Serenity.useDriver()`
        classes in the same JVM run): once any test registers a driver with
        `Serenity.useDriver()`, the *next* test whose setup calls `clearBrowserCookies()` before
        `open(...)` fails with `NoSuchSessionException: ... after calling quit()?` —
        `clearBrowserCookies()` is a raw driver command with no recovery, while Selenide's `open()`
        transparently recovers a dead/replaced session. Fixed by reordering every affected
        `@BeforeEach` to call `open(...)` before `clearBrowserCookies()`, in `CartManagementTest`,
        `CustomerSessionTest` and `CheckoutTest` (all three, since the failure is cross-class: a
        driver killed by one class's test breaks the next class's first `clearBrowserCookies()`
        call, regardless of which class). Verified: 3 consecutive full-suite runs
        (`./gradlew cleanTest test`), same execution order that failed before the fix, all green.
        Root cause of *why* the driver becomes unusable after `useDriver()` was not fully pinned
        down (Serenity's `SerenityJUnit5Extension.postProcessTestInstance` re-injects driver state
        for every new test instance, which JUnit5 creates per test method) — not needed once every
        test tolerates it via `open()`-first ordering. Recorded in `design.md`.

## 6. StorefrontSmokeTest

- [x] 6.1 Replace the inline nav-click and search logic with `storefrontPage.openFirstCategory()`
      and `storefrontPage.search("Shirt")` through an injected `@Steps StorefrontPage` field; add the
      driver-bridge call in `@BeforeAll`/`@BeforeEach` as appropriate.
      - Added a `@BeforeEach` (this class had none before) that does `open("/")` then the driver
        bridge, and removed each test's own now-redundant `open("/")` — matches the driver-bridge
        placement used in the other three classes and avoids navigating three times over.
        `homepage_loads` calls no `@Step` method (it only asserts on plain page state), so it
        correctly has no narrated steps at all — consistent with "plumbing stays silent."
- [x] 6.2 Run `./gradlew cleanTest test --tests '*StorefrontSmokeTest'`; confirm all three tests
      still pass and each shows its narrated navigation/search step with a screenshot.
      - Verified standalone (`--tests '*StorefrontSmokeTest'`, no driver pre-existing from another
        class) and in 3 consecutive full-suite runs. `category_navigation_works` and
        `search_returns_results` each show their one narrated step with 2 screenshots;
        `homepage_loads` has none, as expected.

## 7. CheckoutTest

- [x] 7.1 Replace the plain `cartPage`, `checkoutPage`, `finishPage`, `accountOrderPage` fields with
      `@Steps`-annotated fields (the exact pattern proven for `checkoutPage`/`finishPage`/
      `accountOrderPage` in the spike; extended here to `cartPage` too).
      - **Found a real gap while verifying this task**: `CheckoutPage`, `FinishPage` and
        `AccountOrderPage` never actually had `@Step` annotations added to their methods in the
        real codebase — only on the throwaway spike branch, which was never merged. Task 7.1's
        wording ("the exact pattern proven... in the spike") assumed this was already done; it
        wasn't, and no earlier task covered it. Symptom: the `@Steps` field proxies were confirmed
        correctly injected (checked via `.getClass()` — real ByteBuddy proxy classes), but calling
        an *unannotated* method through a proxy just passes through silently — no step, no error,
        no warning anywhere in stdout/stderr. `CO-1` passed throughout (the real actions all
        worked), but only showed 7 of the expected 13 steps. Fixed by adding the missing `@Step`
        annotations to all six methods across the three classes (see their own diffs).
- [x] 7.2 Replace the given block's raw Selenide calls with `storefrontPage.openFirstCategory()` and
      `productDetailPage.readName()/readNumber()/addToCart()`.
- [x] 7.3 Replace the private `ensureEmptyCart()` method with a call to `cartPage.ensureEmpty()`.
- [x] 7.4 Add the driver-bridge call in `@BeforeEach`, right after the first `open(...)` and before
      any `@Step` runs (matches the spike's verified placement).
- [x] 7.5 Run `./gradlew cleanTest test --tests '*CheckoutTest'`; confirm CO-1 still passes and its
      Serenity JSON result shows every narrated step from the spec (login, cart-emptying if
      triggered, add-to-cart, confirm summary, accept T&C, submit, finish page, order history) in
      the correct order, each with a screenshot.
      - Verified: all 13 steps present in the correct order, each with 1-2 screenshots. 3
        consecutive full-suite runs, all green. Visually spot-checked the confirm-page and
        order-history screenshots — both correct.

## 8. Full-suite verification

- [ ] 8.1 Run `./gradlew cleanTest test` for the whole suite at least twice in a row in the shared
      per-JVM container; confirm no regressions in any of the four test classes.
- [ ] 8.2 Regenerate the Serenity HTML report (`./gradlew aggregate --rerun`) and open each of the
      four test result pages; confirm steps render with visible screenshot thumbnails and no step
      shows zero screenshots.
- [ ] 8.3 Spot-check screenshot content for at least one step per test class (open the PNG, confirm
      it shows the correct page state, not a blank or error page) — the same verification approach
      used in the spikes.

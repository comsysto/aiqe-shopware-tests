## 1. Architecture decision

- [x] 1.1 Write ADR 0002 in `docs/adr/` amending ADR 0001: `CustomerSession` becomes instance-based
      and `@Steps`-injectable so login can be narrated; the single-entry-point intent of ADR 0001 is
      preserved.

## 2. Cross-cutting session

- [x] 2.1 Convert `CustomerSession` from a static utility to an instance class holding an injected
      `LoginPage`, with an instance `loginAs(Customer)` method annotated `@Step("Log in as {0}")`.
- [x] 2.2 Add `@Step` to `LoginPage.open()` and `LoginPage.logInAs(Customer)`.

## 3. Shared page objects for narrated business actions

- [ ] 3.1 Add `@Step`-annotated `openFirstCategory()` and `search(String term)` methods to
      `StorefrontPage`, replacing the near-duplicated raw nav-click/search Selenide calls currently
      inline in `CartManagementTest`, `StorefrontSmokeTest`, and `CheckoutTest`.
- [ ] 3.2 Add a new `ProductDetailPage` page object with `@Step`-annotated `readName()`,
      `readNumber()`, and `addToCart()` methods, replacing the raw `.product-detail-name` /
      `.product-detail-ordernumber` / `button.btn-buy` calls currently inline in
      `CartManagementTest.addProductToCart()` and `CheckoutTest`'s given block.
- [ ] 3.3 Add `@Step`-annotated `increaseQuantity(int index)` and `removeItem(int index)` methods to
      `CartPage` (wrapping the click+assert pairs `CartManagementTest`'s CM-2/CM-3 currently do
      inline), and a `@Step`-annotated `ensureEmpty()` method that moves
      `CheckoutTest.ensureEmptyCart()`'s while-loop body into `CartPage` itself.

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

- [ ] 5.1 Replace the inline add-to-cart logic in `addProductToCart()` with
      `storefrontPage.openFirstCategory()` and `productDetailPage.readName()/readNumber()/addToCart()`
      through injected `@Steps` fields; replace the inline quantity-increase and remove calls in
      CM-2/CM-3 with `cartPage.increaseQuantity(0)`/`cartPage.removeItem(0)` through an injected
      `@Steps CartPage` field; add the driver-bridge call in `@BeforeEach`.
- [ ] 5.2 Run `./gradlew cleanTest test --tests '*CartManagementTest'`; confirm CM-1/2/3 still pass
      and each test's Serenity JSON result shows the expected narrated steps with screenshots
      (test-step-reporting: add-to-cart narrated in the cart-management journey).

## 6. StorefrontSmokeTest

- [ ] 6.1 Replace the inline nav-click and search logic with `storefrontPage.openFirstCategory()`
      and `storefrontPage.search("Shirt")` through an injected `@Steps StorefrontPage` field; add the
      driver-bridge call in `@BeforeAll`/`@BeforeEach` as appropriate.
- [ ] 6.2 Run `./gradlew cleanTest test --tests '*StorefrontSmokeTest'`; confirm all three tests
      still pass and each shows its narrated navigation/search step with a screenshot.

## 7. CheckoutTest

- [ ] 7.1 Replace the plain `cartPage`, `checkoutPage`, `finishPage`, `accountOrderPage` fields with
      `@Steps`-annotated fields (the exact pattern proven for `checkoutPage`/`finishPage`/
      `accountOrderPage` in the spike; extended here to `cartPage` too).
- [ ] 7.2 Replace the given block's raw Selenide calls with `storefrontPage.openFirstCategory()` and
      `productDetailPage.readName()/readNumber()/addToCart()`.
- [ ] 7.3 Replace the private `ensureEmptyCart()` method with a call to `cartPage.ensureEmpty()`.
- [ ] 7.4 Add the driver-bridge call in `@BeforeEach`, right after the first `open(...)` and before
      any `@Step` runs (matches the spike's verified placement).
- [ ] 7.5 Run `./gradlew cleanTest test --tests '*CheckoutTest'`; confirm CO-1 still passes and its
      Serenity JSON result shows every narrated step from the spec (login, cart-emptying if
      triggered, add-to-cart, confirm summary, accept T&C, submit, finish page, order history) in
      the correct order, each with a screenshot.

## 8. Full-suite verification

- [ ] 8.1 Run `./gradlew cleanTest test` for the whole suite at least twice in a row in the shared
      per-JVM container; confirm no regressions in any of the four test classes.
- [ ] 8.2 Regenerate the Serenity HTML report (`./gradlew aggregate --rerun`) and open each of the
      four test result pages; confirm steps render with visible screenshot thumbnails and no step
      shows zero screenshots.
- [ ] 8.3 Spot-check screenshot content for at least one step per test class (open the PNG, confirm
      it shows the correct page state, not a blank or error page) — the same verification approach
      used in the spikes.

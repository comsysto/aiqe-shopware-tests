## Why

No tests currently verify that a shopper can add products to the cart, view and manage cart contents, or proceed toward checkout. The cart flow requires button interactions — not just link-following — to reach, which makes it a natural first exercise of the scriptable crawler's `click` step.

Running `/discover-tests` before implementation produces a real snapshot of the cart page with confirmed selectors, grounding `CartPage` in observed page state rather than assumed class names.

## What Changes

- **Discovery prerequisite**: Run `/discover-tests` to write and execute `discovery-scripts/explore-cart.yml`, snapshot the cart page, and confirm selectors before writing any test code.
- A `CartPage` page object encapsulates selectors for cart line items, quantity inputs, remove buttons, subtotal, and the "Proceed to checkout" button — validated against the discovery snapshot.
- A `CartManagementTest` test class covers: adding a product to the cart from the product detail page, verifying it appears in the cart, updating its quantity, removing it, and confirming the cart reflects each change.

## Capabilities

### New Capabilities

- `cart-management`: Tests covering the full add-to-cart → view cart → update → remove flow, verifying cart state at each step.

### Modified Capabilities

<!-- none -->

## Impact

- New `CartPage.java` page object in the test source tree
- New `CartManagementTest.java` test class
- `discovery-scripts/explore-cart.yml` authored by `/discover-tests` skill during the prerequisite step (not a deliverable of this change)
- No new dependencies required
- Requires the dockware container to be running (same as existing smoke tests)

## Why

No tests currently verify that a shopper can add products to the cart, view and manage cart contents, or proceed toward checkout. The category listing and product detail pages both expose an "Add to shopping cart" button and a quantity input, but the resulting cart state is untested. Without cart coverage, regressions in one of the most critical user flows go undetected.

## What Changes

- A `CartPage` page object encapsulates selectors for the cart line items, quantity inputs, remove buttons, subtotal, and the "Proceed to checkout" button.
- A `CartManagementTest` test class covers: adding a product to the cart from the product detail page, verifying it appears in the cart, updating its quantity, removing it, and confirming the cart reflects each change.

## Capabilities

### New Capabilities

- `cart-management`: Tests covering the full add-to-cart → view cart → update → remove flow, verifying cart state at each step.

### Modified Capabilities

<!-- none -->

## Impact

- New `CartPage.java` page object in the test source tree
- New `CartManagementTest.java` test class
- No new dependencies required
- Requires the dockware container to be running (same as existing smoke tests)

> **Note:** The `DiscoveryRunner` crawler currently cannot reach `/checkout/cart` because it only follows links, not button interactions. To improve future discovery runs, consider extending the crawler to click "Add to shopping cart" on a product detail page and snapshot the resulting cart page.

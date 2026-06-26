# Design: Cart Management Tests

## Selectors (grounded in discovery snapshots)

Confirmed by `explore-cart.yml` and `explore-cart-actions.yml` runs:

| Element | Selector | Source |
|---|---|---|
| Cart line items | `.line-item` | Shopware 6 storefront template |
| Quantity input | `input[name='quantity']` within `.line-item` | `cart-page.json` formFields |
| Remove button | `button.btn.line-item-remove-product` within `.line-item` | Shopware 6 storefront template |
| Proceed to checkout | `a[href*='/checkout/confirm']` | Shopware 6 URL pattern |

Note: the remove button and checkout link are not captured by `extractElements()` (icon-only button, anchor CTA) — selectors are from Shopware 6's standard storefront Twig templates, consistent with the confirmed page structure.

## CartPage

```
CartPage
  lineItems: ElementsCollection         // $$(".line-item")
  quantityInput(index): SelenideElement // lineItems.get(i).$("input[name='quantity']")
  removeButton(index): SelenideElement  // lineItems.get(i).$("button.btn.line-item-remove-product")
  proceedToCheckout: SelenideElement    // $("a[href*='/checkout/confirm']")
```

## CartManagementTest setup

- `@BeforeAll`: set `Configuration.baseUrl` (system property or DockwareContainer) + `Configuration.browserSize`
- Cookie banner dismissed at the start of each test via `$(".cookie-permission-container button.btn-primary")` — checked for existence before clicking, since the banner only appears on fresh sessions
- Navigation to add a product: homepage → first non-home nav link → first `.product-box a.product-name` → `button.btn-buy` → `/checkout/cart`

## Test cases

1. **add_product_appears_in_cart** — navigates the add-to-cart flow, opens `/checkout/cart`, asserts `lineItems.shouldHave(sizeGreaterThan(0))`
2. **update_quantity_reflects_in_cart** — adds product, changes quantity input to `2`, submits, asserts input still shows `2`
3. **remove_product_empties_cart** — adds product, clicks remove, asserts `lineItems.shouldHave(size(0))`

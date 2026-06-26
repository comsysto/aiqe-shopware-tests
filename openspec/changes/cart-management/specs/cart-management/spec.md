# Spec: cart-management

Tests covering the shopper's add-to-cart → view cart → update quantity → remove flow.

## Requirements

- **CM-1**: A shopper can add a product to the cart from the product detail page, and the product appears as a line item on the cart page.
- **CM-2**: A shopper can update the quantity of a cart line item, and the cart reflects the new quantity.
- **CM-3**: A shopper can remove a line item from the cart, and the cart contains no line items afterward.

## Page Object

`CartPage` encapsulates cart selectors:
- `lineItems` — all `.line-item` elements
- `quantityInput(index)` — the `input[name='quantity']` within a given line item
- `removeButton(index)` — the `button.btn.line-item-remove-product` within a given line item
- `proceedToCheckout` — the `a[href*='/checkout/confirm']` checkout CTA

## Test Class

`CartManagementTest` covers CM-1, CM-2, CM-3 as individual `@Test` methods.

Cookie banner (`div.cookie-permission-container`) must be dismissed before any click-based interaction; handled per-test before navigating.

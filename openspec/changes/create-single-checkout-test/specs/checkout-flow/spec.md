## ADDED Requirements

### Requirement: Checkout starts from an empty cart
Before adding products, the checkout test SHALL ensure the logged-in customer's cart is empty, removing any line items restored from a previous session, so the test does not depend on database freshness.

#### Scenario: Leftover cart item is removed after login
- **WHEN** the demo customer logs in and the cart contains items from a previous session
- **THEN** the items are removed before the test adds its product, and the cart shows it is empty

### Requirement: Checkout confirm page displays the cart contents
The checkout confirm page SHALL display the line item added to the cart, matching the product name and product number shown on the product detail page, with quantity 1.

#### Scenario: Cart contents carried into checkout confirm
- **WHEN** a logged-in customer who added one product from its detail page opens `/checkout/confirm`
- **THEN** the confirm page shows exactly one line item whose name and product number match that product and whose quantity is 1

### Requirement: Logged-in customer can complete checkout
A logged-in customer SHALL be able to accept the terms and conditions on the confirm page and submit the order, arriving at the order finish page.

#### Scenario: Successful order placement
- **WHEN** a logged-in customer with one product in the cart accepts the terms and conditions on `/checkout/confirm` and submits the order
- **THEN** the storefront navigates to `/checkout/finish`

### Requirement: Finish page reflects the confirmed order
The finish page SHALL show an order number and the same order details that were shown on the confirm page: line items (name, product number, quantity, line total, line tax), subtotal, shipping cost, tax, grand total, payment method, shipping method, billing address and shipping address.

#### Scenario: Finish page matches confirm page
- **WHEN** the order is submitted from the confirm page
- **THEN** the finish page shows a non-empty order number
- **THEN** every order detail on the finish page equals the corresponding detail read on the confirm page

### Requirement: Placed order appears in the account order history
The order placed during checkout SHALL be listed in the customer's account order history.

#### Scenario: Order number found in order history
- **WHEN** the customer opens `/account/order` after the finish page is shown
- **THEN** the order history lists an order with the order number shown on the finish page

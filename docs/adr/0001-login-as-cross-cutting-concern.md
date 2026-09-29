# ADR 0001: Login as a cross-cutting concern

* Status: accepted
* Date: 2026-09-28
* Change: `create-single-checkout-test`

## Context

Checkout, order history and any later account journey need a logged-in shopper. Each of these
tests would otherwise repeat the same steps: open `/account/login`, fill `#loginMail` and
`#loginPassword`, submit, and wait for the account overview. That duplicates login selectors across
test classes and ties every journey to the way login happens today, through the storefront form.

The dockware demo image seeds one storefront customer (`test@example.com` / `shopware`). The login
form, its selectors and the landing page (`/account`) were confirmed by the `explore-login.yml`
discovery round.

## Decision

Login is a cross-cutting concern with a single entry point:

```
any journey's @BeforeEach
        │
        ▼
CustomerSession.loginAs(Customer.DEMO)      Customer: email, password
        │ uses                              DEMO = test@example.com / shopware
        ▼
LoginPage (page object for /account/login)
```

* `Customer` is a value object (email, password). The dockware demo customer is exposed once as
  `Customer.DEMO`.
* `LoginPage` is the only class that knows the login form selectors.
* `CustomerSession.loginAs(Customer)` logs the browser session in through the `LoginPage` and
  asserts that the account overview is reached, so a failed login fails at the login step and not
  later in the journey.
* Journeys depend on `CustomerSession` only. They never touch the login form.

The mechanism is deliberately the UI form for now. It is the most faithful path and needs no extra
knowledge of the Store API.

## Consequences

* The login mechanism can be replaced without touching any journey, for example by a Store API or
  session cookie login for speed. Only `CustomerSession` changes.
* Taking a `Customer` as a parameter lets a journey use its own customer if sharing the demo
  customer ever causes interference, for example through its persisted cart.
* Credentials live in one place. A change to the dockware image's demo customer is a one-line edit.
* Every journey pays the cost of a UI login in its setup, which is a few seconds. This is accepted
  until it measurably hurts.
* A test that needs to exercise the login form itself, such as a negative login test, uses
  `LoginPage` directly and is the one legitimate exception.

## Alternatives considered

* **Log in inline in each test's `@BeforeEach`**: simplest, but duplicates selectors and couples
  journeys to the form.
* **Store API or cookie login now**: faster, but adds API knowledge and a second path that has to
  be kept in sync with the storefront before anything needs the speed.
* **Guest checkout instead of a customer**: avoids login, but replaces it with a multi-field address
  form, the main flakiness source in checkout tests. Kept as a possible follow-up.

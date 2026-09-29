# ADR 0002: CustomerSession becomes instance-based for step reporting

* Status: accepted
* Date: 2026-09-29
* Change: `add-serenity-step-reporting`
* Amends: ADR 0001

## Context

ADR 0001 made `CustomerSession.loginAs(Customer)` a `static` method, deliberately, so any journey
could call it as a single entry point without instantiating anything. That decision predates
step-level Serenity reporting.

Serenity's `@Steps` field-injection mechanism (proven by two spikes, see `design.md` of this
change) narrates a method call as a report step only when the call goes through a dynamic proxy
injected into a field annotated `@Steps`. That mechanism can only proxy **instance** methods called
through the field — a `static` method call is invisible to it, by construction, regardless of
annotations.

Login happens in every one of the four test classes. If `CustomerSession` stays static, every
report's narrative silently starts one step too late — missing its own opening step — in all four
of them, not just one edge case.

## Decision

`CustomerSession` changes from a static utility class to an ordinary class:

```
any journey's @BeforeEach
        │
        ▼
@Steps CustomerSession customerSession;    (injected by Serenity)
        │
        ▼
customerSession.loginAs(Customer.DEMO)      @Step("Log in as {0}")
        │ uses (injected @Steps field)
        ▼
LoginPage (page object for /account/login)  @Step on open() and logInAs()
```

* `CustomerSession` holds an injected `@Steps LoginPage loginPage` field instead of constructing a
  `new LoginPage()` itself.
* `loginAs(Customer)` is now an instance method, annotated `@Step("Log in as {0}")`.
* Every test class declares `@Steps CustomerSession customerSession;` and calls
  `customerSession.loginAs(Customer.DEMO)` instead of the static
  `CustomerSession.loginAs(Customer.DEMO)`.

This **amends, not reverses**, ADR 0001. Every consequence ADR 0001 recorded still holds:

* Journeys still depend on `CustomerSession` alone; they still never touch `LoginPage`'s selectors.
* The login mechanism can still be swapped later (Store API, cookie login) without touching any
  journey — only `CustomerSession`'s internals change.
* `Customer` as a parameter still allows a per-journey customer if needed.

Only the Java-level mechanics change: an injected field and an instance method call, instead of a
static one. The one-line callsite difference (`customerSession.loginAs(...)` vs.
`CustomerSession.loginAs(...)`) is the entire migration cost for every existing caller.

## Consequences

* Every test class gains one field (`@Steps CustomerSession customerSession;`), replacing a
  no-import static call with a field-mediated one.
* Login now appears as a named, screenshotted step in every test's Serenity report.
* A test that constructs `new CustomerSession()` directly (bypassing `@Steps` injection) gets no
  step reporting and no error — the same silent-bypass risk any `@Steps` field carries, called out
  in this change's `design.md`.

## Alternatives considered

* **Keep `CustomerSession` static, accept the narrative gap**: rejected in `design.md` — login is
  not incidental, and every one of the four test classes would carry the same gap.
* **Make only `LoginPage`'s calls narrated, leave `CustomerSession` as a thin static pass-through**:
  does not work — a static method's body executing instance calls on a *locally constructed*
  `LoginPage` (`new LoginPage()`) is exactly what a `@Steps`-injected field is not; the proxy has no
  way to intercept a static method Serenity was never told about.

## ADDED Requirements

### Requirement: StorefrontPage page object encapsulates selectors
A `StorefrontPage` class SHALL be created in the test source tree that exposes Selenide `SelenideElement` fields for: the site logo/homepage header, the main navigation menu, the search input, and the search submit button.

#### Scenario: Page object fields are accessible from test class
- **WHEN** a test instantiates `StorefrontPage`
- **THEN** it can reference `storefrontPage.searchInput`, `storefrontPage.searchButton`, and `storefrontPage.mainNavigation` without NullPointerException

### Requirement: Smoke test verifies homepage loads
The `StorefrontSmokeTest` SHALL include a test that opens the Shopware storefront and verifies the page title is non-empty and the main navigation is visible.

#### Scenario: Homepage loads successfully
- **WHEN** the test opens `http://localhost` (or the configured `shopware.baseUrl`)
- **THEN** `Selenide.title()` is non-blank
- **THEN** the main navigation element is visible on the page

### Requirement: Smoke test verifies category navigation
The `StorefrontSmokeTest` SHALL include a test that clicks the first available top-level navigation link and verifies a product listing or category page is displayed.

#### Scenario: Clicking first navigation item loads a category page
- **WHEN** the test clicks the first top-level navigation link
- **THEN** the resulting page URL changes from the homepage URL
- **THEN** at least one product card or category listing element is visible

### Requirement: Smoke test verifies search returns results
The `StorefrontSmokeTest` SHALL include a test that types a generic search term into the search bar, submits it, and verifies the search results page shows at least one result.

#### Scenario: Search with a known term returns results
- **WHEN** the test types "Shirt" into the search input and submits
- **THEN** the search results page is displayed
- **THEN** at least one product result element is visible on the page

### Requirement: Base URL is configurable
The `StorefrontSmokeTest` SHALL read the target URL from the JVM system property `shopware.baseUrl`, defaulting to `http://localhost` when the property is not set.

#### Scenario: Default URL is used when property is absent
- **WHEN** no `shopware.baseUrl` system property is set
- **THEN** the test opens `http://localhost`

#### Scenario: Custom URL overrides default
- **WHEN** `shopware.baseUrl` is set to `http://myshopware.local`
- **THEN** the test opens `http://myshopware.local`

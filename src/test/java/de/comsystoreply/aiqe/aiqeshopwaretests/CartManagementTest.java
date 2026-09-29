package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import net.serenitybdd.annotations.Steps;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;

@ExtendWith(SerenityJUnit5Extension.class)
class CartManagementTest {

    @Steps
    StorefrontPage storefrontPage;
    @Steps
    ProductDetailPage productDetailPage;
    @Steps
    CartPage cartPage;

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void addProductToCart() {
        // open() first: it recovers a dead/replaced driver session; clearBrowserCookies() does not
        // and fails hard if a previous test's driver registration left the session unusable
        open("/");
        // Fresh session ensures an empty cart regardless of test order
        clearBrowserCookies();
        open("/");
        dismissCookieBanner();
        // Bridges Selenide's externally-managed driver into Serenity, once it exists, and before
        // any @Step runs, so every narrated step (including the first) gets a screenshot
        Serenity.useDriver(WebDriverRunner.getWebDriver());

        // Navigate: homepage → first category → first product → add to cart
        storefrontPage.openFirstCategory();
        productDetailPage.openFirstListedProduct();
        productDetailPage.addToCart();

        // Navigate to cart
        open("/checkout/cart");
    }

    @Test
    @DisplayName("CM-1: Adding a product to the cart shows it as a line item on the cart page")
    void add_product_appears_in_cart() {
        cartPage.lineItems.shouldHave(sizeGreaterThan(0));
    }

    @Test
    @DisplayName("CM-2: Updating line item quantity is reflected in the cart")
    void update_quantity_reflects_in_cart() {
        // when
        cartPage.increaseQuantity(0);
    }

    @Test
    @DisplayName("CM-3: Removing the only line item leaves the cart empty")
    void remove_product_empties_cart() {
        // when
        cartPage.removeItem(0);
    }

    private static void dismissCookieBanner() {
        final var acceptButton = $(".cookie-permission-container button.btn-primary");
        if (acceptButton.is(visible)) {
            acceptButton.click();
        }
    }
}

package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

class CartManagementTest {

    private final CartPage cartPage = new CartPage();

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void addProductToCart() {
        open("/");
        dismissCookieBanner();

        // Navigate: homepage → first category → first product → add to cart
        $$("nav.main-navigation-menu a.main-navigation-link:not(.home-link)").first().click();
        $(".product-box a.product-name").click();
        $("button.btn-buy").click();

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
        cartPage.quantityInput(0).setValue("2");
        $("button[type='submit']").click();

        open("/checkout/cart");
        cartPage.quantityInput(0).shouldHave(value("2"));
    }

    @Test
    @DisplayName("CM-3: Removing the only line item leaves the cart empty")
    void remove_product_empties_cart() {
        cartPage.removeButton(0).click();

        cartPage.lineItems.shouldHave(size(0));
    }

    private static void dismissCookieBanner() {
        final var acceptButton = $(".cookie-permission-container button.btn-primary");
        if (acceptButton.exists()) {
            acceptButton.click();
        }
    }
}

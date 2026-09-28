package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.assertj.core.api.Assertions.assertThat;

class CheckoutTest {

    private final CartPage cartPage = new CartPage();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final FinishPage finishPage = new FinishPage();
    private final AccountOrderPage accountOrderPage = new AccountOrderPage();

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void loginWithEmptyCart() {
        // Fresh session so every test exercises the login
        clearBrowserCookies();
        open("/");
        dismissCookieBanner();
        CustomerSession.loginAs(Customer.DEMO);
        ensureEmptyCart();
    }

    @Test
    @DisplayName("CO-1: A logged-in customer checks out one product and finds the order in the order history")
    void should_place_order_and_find_it_in_order_history() {
        // given
        $$("nav.main-navigation-menu a.main-navigation-link:not(.home-link)").first().click();
        $(".product-box a.product-name").click();
        final var productName = $(".product-detail-name").shouldBe(visible).getText().strip();
        final var productNumber = $(".product-detail-ordernumber").getText().strip();
        $("button.btn-buy").click();
        open("/checkout/cart");
        cartPage.proceedToCheckout.click();

        // when
        final var confirmSummary = checkoutPage.readSummary();
        checkoutPage.acceptTermsAndConditions();
        checkoutPage.submitOrder();
        webdriver().shouldHave(urlContaining("/checkout/finish"));
        final var orderNumber = finishPage.orderNumber();
        final var finishSummary = finishPage.readSummary();
        accountOrderPage.open();

        // then
        assertThat(confirmSummary.lineItems()).hasSize(1);
        final var lineItem = confirmSummary.lineItems().getFirst();
        assertThat(lineItem.name()).isEqualTo(productName);
        assertThat(lineItem.productNumber()).isEqualTo(productNumber);
        assertThat(lineItem.quantity()).isEqualTo(1);

        assertThat(orderNumber).isNotBlank();
        assertThat(finishSummary).usingRecursiveComparison().isEqualTo(confirmSummary);

        accountOrderPage.orderNumbers.findBy(exactText(orderNumber)).shouldBe(visible);
    }

    private void ensureEmptyCart() {
        // Shopware restores a logged-in customer's saved cart, so a leftover item must not leak in
        open("/checkout/cart");
        while (cartPage.lineItems.size() > 0) {
            final var remaining = cartPage.lineItems.size() - 1;
            cartPage.removeButton(0).click();
            cartPage.lineItems.shouldHave(size(remaining));
        }
    }

    private static void dismissCookieBanner() {
        final var acceptButton = $(".cookie-permission-container button.btn-primary");
        if (acceptButton.is(visible)) {
            acceptButton.click();
        }
    }
}

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

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SerenityJUnit5Extension.class)
class CheckoutTest {

    @Steps
    CustomerSession customerSession;
    @Steps
    StorefrontPage storefrontPage;
    @Steps
    ProductDetailPage productDetailPage;
    @Steps
    CartPage cartPage;
    @Steps
    CheckoutPage checkoutPage;
    @Steps
    FinishPage finishPage;
    @Steps
    AccountOrderPage accountOrderPage;

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void loginWithEmptyCart() {
        // open() first: it recovers a dead/replaced driver session; clearBrowserCookies() does not
        // and fails hard if a previous test's driver registration left the session unusable
        open("/");
        clearBrowserCookies();
        open("/");
        dismissCookieBanner();
        // Bridges Selenide's externally-managed driver into Serenity, once it exists, and before
        // any @Step runs, so every narrated step (including the first) gets a screenshot
        Serenity.useDriver(WebDriverRunner.getWebDriver());
        customerSession.loginAs(Customer.DEMO);
        cartPage.ensureEmpty();
    }

    @Test
    @DisplayName("CO-1: A logged-in customer checks out one product and finds the order in the order history")
    void should_place_order_and_find_it_in_order_history() {
        // given
        storefrontPage.openFirstCategory();
        productDetailPage.openFirstListedProduct();
        final var productName = productDetailPage.readName();
        final var productNumber = productDetailPage.readNumber();
        productDetailPage.addToCart();
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

    private static void dismissCookieBanner() {
        final var acceptButton = $(".cookie-permission-container button.btn-primary");
        if (acceptButton.is(visible)) {
            acceptButton.click();
        }
    }
}

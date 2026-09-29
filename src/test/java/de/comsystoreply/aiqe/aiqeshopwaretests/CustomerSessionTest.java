package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

@ExtendWith(SerenityJUnit5Extension.class)
class CustomerSessionTest {

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void logOut() {
        clearBrowserCookies();
    }

    @Test
    @DisplayName("CS-1: Logging in as the demo customer makes the account overview reachable")
    void should_reach_account_overview_after_login_as_demo_customer() {
        // when
        CustomerSession.loginAs(Customer.DEMO);

        // then
        open("/account");
        webdriver().shouldNotHave(urlContaining("/account/login"));
        $$("h1, h2, h3").findBy(text("Overview")).shouldBe(visible);
    }
}

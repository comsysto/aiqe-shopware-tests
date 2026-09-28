package de.comsystoreply.aiqe.aiqeshopwaretests;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Single entry point for logging the browser session in, so journeys never touch the login form.
 */
public final class CustomerSession {

    private CustomerSession() {
        // NOOP
    }

    public static void loginAs(final Customer customer) {
        new LoginPage().open().logInAs(customer);

        // Fails here, at the login step, rather than later in the journey
        webdriver().shouldNotHave(urlContaining("/account/login"));
        $$("h1, h2, h3").findBy(text("Overview")).shouldBe(visible);
    }
}

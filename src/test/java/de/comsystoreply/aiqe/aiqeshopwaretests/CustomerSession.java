package de.comsystoreply.aiqe.aiqeshopwaretests;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.annotations.Steps;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Single entry point for logging the browser session in, so journeys never touch the login form.
 * Instance-based (see ADR 0002) so it can itself be a {@code @Steps} field, letting login appear
 * as a named, screenshotted step in the Serenity report.
 */
public class CustomerSession {

    @Steps
    LoginPage loginPage;

    @Step("Log in as {0}")
    public void loginAs(final Customer customer) {
        loginPage.open().logInAs(customer);

        // Fails here, at the login step, rather than later in the journey
        webdriver().shouldNotHave(urlContaining("/account/login"));
        $$("h1, h2, h3").findBy(text("Overview")).shouldBe(visible);
    }
}

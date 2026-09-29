package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import net.serenitybdd.annotations.Step;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage {

    // IDs, not names: the registration form on the same page has its own "email" field
    public final SelenideElement emailInput = $("#loginMail");
    public final SelenideElement passwordInput = $("#loginPassword");
    public final SelenideElement submitButton = $(".login-submit button[type='submit']");

    @Step("Open the login page")
    public LoginPage open() {
        Selenide.open("/account/login");
        return this;
    }

    @Step("Enter credentials for {0}")
    public void logInAs(final Customer customer) {
        emailInput.setValue(customer.email());
        passwordInput.setValue(customer.password());
        submitButton.click();
    }
}

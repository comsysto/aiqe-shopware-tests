package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import net.serenitybdd.annotations.Step;

import static com.codeborne.selenide.Selenide.$$;

public class AccountOrderPage {

    // Order number of every listed order, without the "Order number:" label
    public final ElementsCollection orderNumbers = $$(".order-table-header-order-number .order-table-body-value");

    @Step("Open the account order history")
    public AccountOrderPage open() {
        Selenide.open("/account/order");
        return this;
    }
}

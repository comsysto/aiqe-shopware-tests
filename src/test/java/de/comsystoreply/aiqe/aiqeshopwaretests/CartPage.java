package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;

public class CartPage {

    // All line items in the cart
    public final ElementsCollection lineItems = $$(".line-item");

    // Quantity input within a specific line item (0-based index)
    public SelenideElement quantityInput(final int index) {
        return lineItems.get(index).$("input[name='quantity']");
    }

    // Quantity stepper "+" button within a specific line item (0-based index)
    public SelenideElement quantityUpButton(final int index) {
        return lineItems.get(index).$("button.js-btn-plus");
    }

    // Remove button within a specific line item (0-based index)
    public SelenideElement removeButton(final int index) {
        return lineItems.get(index).$("button.line-item-remove-button");
    }

    // "Proceed to checkout" CTA in the cart summary
    public final SelenideElement proceedToCheckout = $("a[href*='/checkout/confirm']");
}

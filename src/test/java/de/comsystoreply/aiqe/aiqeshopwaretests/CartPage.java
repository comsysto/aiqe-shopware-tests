package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import net.serenitybdd.annotations.Step;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.value;
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

    @Step("Increase the quantity of line item {0}")
    public void increaseQuantity(final int index) {
        final var next = Integer.parseInt(quantityInput(index).getValue()) + 1;
        quantityUpButton(index).click();
        quantityInput(index).shouldHave(value(String.valueOf(next)));
    }

    @Step("Remove line item {0}")
    public void removeItem(final int index) {
        final var remaining = lineItems.size() - 1;
        removeButton(index).click();
        lineItems.shouldHave(size(remaining));
    }

    // Not implemented via removeItem(): a step's own internal calls on `this` bypass the @Steps
    // proxy, so this stays self-contained to keep "ensure empty" a single reported step
    @Step("Ensure the cart is empty")
    public void ensureEmpty() {
        Selenide.open("/checkout/cart");
        while (lineItems.size() > 0) {
            final var remaining = lineItems.size() - 1;
            removeButton(0).click();
            lineItems.shouldHave(size(remaining));
        }
    }
}

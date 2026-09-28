package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class CheckoutPage {

    public final ElementsCollection lineItems = $$(".checkout-main .line-item");
    public final SelenideElement termsCheckbox = $("#tos");
    public final SelenideElement submitButton = $("#confirmFormSubmit");

    public CheckoutPage open() {
        Selenide.open("/checkout/confirm");
        return this;
    }

    public OrderSummary readSummary() {
        lineItems.shouldHave(sizeGreaterThan(0));
        final var items = lineItems.asFixedIterable().stream()
                .map(item -> OrderSummaryReader.lineItem(item, Integer.parseInt(item.$("input[name='quantity']").getValue())))
                .toList();

        final var shippingAddress = OrderSummaryReader.address($(".confirm-address-shipping .address"));
        // The confirm page shows "Same as shipping address" instead of repeating the address
        final var billingAddresses = $$(".confirm-address-billing .address");
        final var billingAddress = billingAddresses.isEmpty() ? shippingAddress : OrderSummaryReader.address(billingAddresses.first());

        return new OrderSummary(
                items,
                OrderSummaryReader.subtotal(),
                OrderSummaryReader.shippingCost(),
                OrderSummaryReader.tax(),
                OrderSummaryReader.grandTotal(),
                OrderSummaryReader.text($(".payment-method-input:checked + .payment-method-label strong")),
                OrderSummaryReader.text($(".shipping-method-input:checked + .shipping-method-label strong")),
                billingAddress,
                shippingAddress);
    }

    public void acceptTermsAndConditions() {
        termsCheckbox.click();
        termsCheckbox.shouldBe(checked);
    }

    public void submitOrder() {
        submitButton.click();
    }
}

package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.attributeMatching;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class FinishPage {

    public final ElementsCollection lineItems = $$(".line-item");

    // The visible text is "Your order number: #10000"; the attribute holds just the number
    public String orderNumber() {
        final var element = $(".finish-ordernumber").shouldHave(attributeMatching("data-order-number", ".+"));
        return element.getAttribute("data-order-number");
    }

    public OrderSummary readSummary() {
        lineItems.shouldHave(sizeGreaterThan(0));
        final var items = lineItems.asFixedIterable().stream()
                .map(item -> OrderSummaryReader.lineItem(item, Integer.parseInt(OrderSummaryReader.text(item.$(".line-item-quantity-select-wrapper")))))
                .toList();

        // The methods are "label: value" paragraphs without distinguishing classes; payment comes first
        final var methods = $$(".finish-order-details p");

        return new OrderSummary(
                items,
                OrderSummaryReader.subtotal(),
                OrderSummaryReader.shippingCost(),
                OrderSummaryReader.tax(),
                OrderSummaryReader.grandTotal(),
                valueOf(methods.get(0)),
                valueOf(methods.get(1)),
                OrderSummaryReader.address($(".finish-address-billing .address")),
                OrderSummaryReader.address($(".finish-address-shipping .address")));
    }

    private static String valueOf(final SelenideElement labelledParagraph) {
        final var label = OrderSummaryReader.text(labelledParagraph.$("strong"));
        return OrderSummaryReader.text(labelledParagraph).replace(label, "").strip();
    }
}

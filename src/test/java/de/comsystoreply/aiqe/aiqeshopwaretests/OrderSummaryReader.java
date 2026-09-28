package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.SelenideElement;

import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Parsing shared by the confirm and finish pages, which render line items and the totals panel with
 * the same markup.
 */
final class OrderSummaryReader {

    private static final String TOTALS_VALUES = ".checkout-aside-summary-list dd.checkout-aside-summary-value";

    private OrderSummaryReader() {
        // NOOP
    }

    static String text(final SelenideElement element) {
        return element.getText().replaceAll("\\s+", " ").strip();
    }

    static String address(final SelenideElement element) {
        return element.getText().lines()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining(", "));
    }

    static OrderSummary.LineItem lineItem(final SelenideElement item, final int quantity) {
        final var tax = item.$(".line-item-tax-price");
        return new OrderSummary.LineItem(
                text(item.$(".line-item-label")),
                // Everything up to the first colon is the localized "Product number:" label
                text(item.$(".line-item-product-number")).replaceFirst("^[^:]*:\\s*", ""),
                quantity,
                text(item.$(".line-item-total-price-value")),
                text(tax).replace(text(tax.$(".line-item-tax-price-label")), "").strip());
    }

    // Labels are localized, so the two plain rows are located by position
    static String subtotal() {
        return text($$(TOTALS_VALUES).get(0));
    }

    static String shippingCost() {
        return text($$(TOTALS_VALUES).get(1));
    }

    static String grandTotal() {
        return text($("dd.checkout-aside-summary-total"));
    }

    static String tax() {
        return text($("dd.summary-tax"));
    }
}

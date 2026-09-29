package de.comsystoreply.aiqe.aiqeshopwaretests;

import java.util.List;

/**
 * What the storefront reports about an order, as displayed. Money is kept as the displayed string
 * because the confirm and finish pages format it identically.
 */
public record OrderSummary(
        List<LineItem> lineItems,
        String subtotal,
        String shippingCost,
        String tax,
        String grandTotal,
        String paymentMethod,
        String shippingMethod,
        String billingAddress,
        String shippingAddress) {

    public record LineItem(String name, String productNumber, int quantity, String lineTotal, String lineTax) {
    }
}

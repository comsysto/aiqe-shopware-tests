package de.comsystoreply.aiqe.aiqeshopwaretests;

public record Customer(String email, String password) {

    // Seeded by the dockware/dev image; change here if a future image ships different credentials
    public static final Customer DEMO = new Customer("test@example.com", "shopware");
}

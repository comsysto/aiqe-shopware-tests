package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;

public class DiscoveryRunner {

    public static void main(final String[] args) {
        final var shopUrl = DockwareContainer.start();

        Configuration.baseUrl = shopUrl;
        Configuration.browserSize = "1280x800";
        Configuration.headless = true;

        try {
            crawl();
        } finally {
            DockwareContainer.stop();
        }
    }

    private static void crawl() {
        // TODO: implement in tasks 2.3–4.2
    }
}

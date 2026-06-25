package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;

import java.util.List;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class DiscoveryRunner {

    private static final String NAV_LINK_SELECTOR = "nav.main-navigation-menu a.main-navigation-link:not(.home-link)";

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
        final var navUrls = collectNavUrls();
        // TODO: visit each nav URL and crawl one level deep (task 3.1)
        // TODO: authenticated crawl (task 3.2)
    }

    private static List<String> collectNavUrls() {
        open("/");
        return $$(NAV_LINK_SELECTOR).asFixedIterable().stream()
                .map(el -> el.getAttribute("href"))
                .filter(href -> href != null && !href.isBlank())
                .toList();
    }
}

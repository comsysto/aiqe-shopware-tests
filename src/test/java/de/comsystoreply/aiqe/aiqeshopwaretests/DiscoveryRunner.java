package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;

import java.util.List;
import java.util.Optional;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class DiscoveryRunner {

    private static final String NAV_LINK_SELECTOR = "nav.main-navigation-menu a.main-navigation-link:not(.home-link)";
    private static final String CHILD_LINK_SELECTOR = ".product-box a.product-name, .cms-element-product-listing a, .category-navigation a";

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
        for (final var navUrl : navUrls) {
            // TODO: snapshot navUrl (task 4.x)
            firstChildUrl(navUrl).ifPresent(childUrl -> {
                // TODO: snapshot childUrl (task 4.x)
            });
        }
        // TODO: authenticated crawl (task 3.2)
    }

    private static List<String> collectNavUrls() {
        open("/");
        return $$(NAV_LINK_SELECTOR).asFixedIterable().stream()
                .map(el -> el.getAttribute("href"))
                .filter(href -> href != null && !href.isBlank())
                .toList();
    }

    private static Optional<String> firstChildUrl(final String navUrl) {
        open(navUrl);
        return $$(CHILD_LINK_SELECTOR).asFixedIterable().stream()
                .map(el -> el.getAttribute("href"))
                .filter(href -> href != null && !href.isBlank())
                .findFirst();
    }
}

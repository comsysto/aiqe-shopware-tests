package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class DiscoveryRunner {

    private static final String NAV_LINK_SELECTOR = "nav.main-navigation-menu a.main-navigation-link:not(.home-link)";
    private static final String CHILD_LINK_SELECTOR = ".product-box a.product-name, .cms-element-product-listing a, .category-navigation a";
    private static final String CUSTOMER_EMAIL = "customer@example.com";
    private static final String CUSTOMER_PASSWORD = "shopware";

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
            snapshotPage(navUrl, false);
            firstChildUrl(navUrl).ifPresent(childUrl -> snapshotPage(childUrl, false));
        }
        crawlAuthenticated();
    }

    private static void crawlAuthenticated() {
        open("/account/login");
        $("input[name='email']").setValue(CUSTOMER_EMAIL);
        $("input[name='password']").setValue(CUSTOMER_PASSWORD);
        $("button.btn-primary[type='submit']").click();

        snapshotPage("/account", true);
        snapshotPage("/account/order", true);
    }

    private static void snapshotPage(final String url, final boolean authRequired) {
        open(url);
        final var elements = extractElements();
        // TODO: write JSON snapshot (task 4.1)
        // TODO: write PNG screenshot (task 4.2)
    }

    private static Map<String, List<String>> extractElements() {
        return Map.of(
                "navLinks", collectTexts("nav a"),
                "buttons", collectTexts("button, input[type='submit']"),
                "formFields", collectAttributes("input:not([type='hidden']), select, textarea", "name"),
                "headings", collectTexts("h1, h2, h3")
        );
    }

    private static List<String> collectTexts(final String selector) {
        return $$(selector).asFixedIterable().stream()
                .map(el -> el.getText().strip())
                .filter(text -> !text.isBlank())
                .distinct()
                .toList();
    }

    private static List<String> collectAttributes(final String selector, final String attribute) {
        return $$(selector).asFixedIterable().stream()
                .map(el -> el.getAttribute(attribute))
                .filter(val -> val != null && !val.isBlank())
                .distinct()
                .toList();
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

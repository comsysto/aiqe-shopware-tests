package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class DiscoveryRunner {

    private static final Path OUTPUT_DIR = Path.of("build/discovery");
    private static final String NAV_LINK_SELECTOR = "nav.main-navigation-menu a.main-navigation-link:not(.home-link)";
    private static final String CHILD_LINK_SELECTOR = ".product-box a.product-name, .cms-element-product-listing a, .category-navigation a";
    private static final String CUSTOMER_EMAIL = "customer@example.com";
    private static final String CUSTOMER_PASSWORD = "shopware";
    private static final Set<String> USED_SLUGS = new HashSet<>();

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
        final var slug = toSlug(url);
        final var elements = extractElements();
        writeJson(url, authRequired, elements, slug);
        writeScreenshot(slug);
    }

    private static void writeScreenshot(final String slug) {
        final var src = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(OUTPUT_DIR);
            Files.copy(src.toPath(), OUTPUT_DIR.resolve(slug + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write screenshot for " + slug, e);
        }
    }

    private static void writeJson(final String url, final boolean authRequired,
                                  final Map<String, List<String>> elements, final String slug) {
        final var snapshot = Map.of(
                "url", url,
                "title", Selenide.title(),
                "journey_hint", slug.replaceAll("-\\d+$", ""),
                "auth_required", authRequired,
                "elements", elements
        );

        final var json = new GsonBuilder().setPrettyPrinting().create().toJson(snapshot);

        try {
            Files.createDirectories(OUTPUT_DIR);
            Files.writeString(OUTPUT_DIR.resolve(slug + ".json"), json);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write snapshot JSON for " + url, e);
        }
    }

    static String toSlug(final String url) {
        final String path;
        try {
            path = new URI(url).getPath();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }

        var base = path
                .replaceAll("^/+|/+$", "")
                .replace("/", "-")
                .replaceAll("[^a-zA-Z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (base.isBlank()) {
            base = "home";
        }

        var slug = base;
        var counter = 2;
        while (USED_SLUGS.contains(slug)) {
            slug = base + "-" + counter++;
        }
        USED_SLUGS.add(slug);
        return slug;
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

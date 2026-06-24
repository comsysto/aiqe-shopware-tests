package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

class StorefrontSmokeTest {

    @SuppressWarnings("resource")
    private static final GenericContainer<?> SHOPWARE =
            new GenericContainer<>("dockware/dev:latest")
                    .withExposedPorts(80)
                    .waitingFor(Wait.forLogMessage(".*IS READY.*", 1).withStartupTimeout(java.time.Duration.ofMinutes(3)));

    private final StorefrontPage page = new StorefrontPage();

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");

        if (baseUrl == null) {
            SHOPWARE.start();
            final String shopUrl = "http://" + SHOPWARE.getHost() + ":" + SHOPWARE.getMappedPort(80);
            configureSalesChannelDomain(shopUrl);
            Configuration.baseUrl = shopUrl;
        } else {
            Configuration.baseUrl = baseUrl;
        }

        Configuration.browserSize = "1280x800";
    }

    // Shopware's sales channel is pre-configured for http://localhost (port 80).
    // Update the domain to match the dynamic port assigned by Testcontainers.
    private static void configureSalesChannelDomain(final String shopUrl) {
        try {
            final var sqlResult = SHOPWARE.execInContainer(
                    "mysql", "-h", "127.0.0.1", "-u", "root", "-proot", "shopware", "-e",
                    "UPDATE sales_channel_domain SET url = '" + shopUrl + "' WHERE url = 'http://localhost';"
            );
            if (sqlResult.getExitCode() != 0) {
                throw new RuntimeException("SQL update failed: " + sqlResult.getStderr());
            }
            final var cacheResult = SHOPWARE.execInContainer(
                    "php", "/var/www/html/bin/console", "cache:clear"
            );
            if (cacheResult.getExitCode() != 0) {
                throw new RuntimeException("Cache clear failed: " + cacheResult.getStderr());
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to configure Shopware sales channel domain", e);
        }
    }

    @Test
    @DisplayName("Homepage loads: page title is non-blank and main navigation is visible")
    void homepage_loads() {
        // when
        open("/");

        // then
        assertThat(Selenide.title()).isNotBlank();
        page.mainNavigation.shouldBe(visible);
    }

    @Test
    @DisplayName("Category navigation: clicking first nav link loads a category page")
    void category_navigation_works() {
        // given
        open("/");
        final var firstNavLink = $$("nav.main-navigation-menu a.main-navigation-link:not(.home-link)").first();

        // when
        firstNavLink.click();

        // then
        assertThat(Selenide.webdriver().driver().url()).isNotEqualTo(Configuration.baseUrl + "/");
        $$(".product-box, .cms-element-product-listing").shouldHave(sizeGreaterThan(0));
    }

    @Test
    @DisplayName("Search returns results: searching for 'Shirt' shows at least one product")
    void search_returns_results() {
        // given
        open("/");

        // when
        page.searchInput.setValue("Shirt");
        page.searchButton.click();

        // then
        $$(".product-box").shouldHave(sizeGreaterThan(0));
    }
}

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
                    .waitingFor(Wait.forHttp("/").forStatusCode(200).withStartupTimeout(java.time.Duration.ofMinutes(3)));

    private final StorefrontPage page = new StorefrontPage();

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");

        if (baseUrl == null) {
            SHOPWARE.start();
            Configuration.baseUrl = "http://" + SHOPWARE.getHost() + ":" + SHOPWARE.getMappedPort(80);
        } else {
            Configuration.baseUrl = baseUrl;
        }

        Configuration.browserSize = "1280x800";
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
        final var firstNavLink = $$("nav.main-navigation a").first();

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

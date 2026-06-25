package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

class StorefrontSmokeTest {

    private final StorefrontPage page = new StorefrontPage();

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
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

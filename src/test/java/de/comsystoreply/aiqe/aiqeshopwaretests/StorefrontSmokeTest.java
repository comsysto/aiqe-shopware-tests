package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import net.serenitybdd.annotations.Steps;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SerenityJUnit5Extension.class)
class StorefrontSmokeTest {

    @Steps
    StorefrontPage storefrontPage;

    @BeforeAll
    static void setUp() {
        final var baseUrl = System.getProperty("shopware.baseUrl");
        Configuration.baseUrl = baseUrl != null ? baseUrl : DockwareContainer.start();
        Configuration.browserSize = "1280x800";
    }

    @BeforeEach
    void openHomepage() {
        open("/");
        // Bridges Selenide's externally-managed driver into Serenity, once it exists, and before
        // any @Step runs, so every narrated step (including the first) gets a screenshot
        Serenity.useDriver(WebDriverRunner.getWebDriver());
    }

    @Test
    @DisplayName("Homepage loads: page title is non-blank and main navigation is visible")
    void homepage_loads() {
        // then
        assertThat(Selenide.title()).isNotBlank();
        storefrontPage.mainNavigation.shouldBe(visible);
    }

    @Test
    @DisplayName("Category navigation: clicking first nav link loads a category page")
    void category_navigation_works() {
        // when
        storefrontPage.openFirstCategory();

        // then
        assertThat(Selenide.webdriver().driver().url()).isNotEqualTo(Configuration.baseUrl + "/");
        $$(".product-box, .cms-element-product-listing").shouldHave(sizeGreaterThan(0));
    }

    @Test
    @DisplayName("Search returns results: searching for 'Shirt' shows at least one product")
    void search_returns_results() {
        // when
        storefrontPage.search("Shirt");

        // then
        $$(".product-box").shouldHave(sizeGreaterThan(0));
    }
}

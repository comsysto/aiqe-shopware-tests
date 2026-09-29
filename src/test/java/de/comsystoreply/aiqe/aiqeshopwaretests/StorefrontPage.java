package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.SelenideElement;
import net.serenitybdd.annotations.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class StorefrontPage {

    // Shopware 6 default Storefront theme selectors (dockware/dev ships these out of the box)
    public final SelenideElement mainNavigation = $("nav.main-navigation-menu");
    public final SelenideElement searchInput = $("input[name='search']");
    public final SelenideElement searchButton = $("button.header-search-btn");

    @Step("Open the first category")
    public void openFirstCategory() {
        $$("nav.main-navigation-menu a.main-navigation-link:not(.home-link)").first().click();
    }

    @Step("Search for \"{0}\"")
    public void search(final String term) {
        searchInput.setValue(term);
        searchButton.click();
    }
}

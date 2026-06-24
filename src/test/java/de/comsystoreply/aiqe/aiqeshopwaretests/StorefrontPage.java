package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class StorefrontPage {

    // Shopware 6 default Storefront theme selectors (dockware/dev ships these out of the box)
    public final SelenideElement mainNavigation = $("nav.main-navigation-menu");
    public final SelenideElement searchInput = $("input[name='search']");
    public final SelenideElement searchButton = $("button.header-search-btn");
}

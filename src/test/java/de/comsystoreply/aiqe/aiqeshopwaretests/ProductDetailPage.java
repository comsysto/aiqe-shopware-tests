package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.SelenideElement;
import net.serenitybdd.annotations.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class ProductDetailPage {

    private final SelenideElement name = $(".product-detail-name");
    private final SelenideElement number = $(".product-detail-ordernumber");
    private final SelenideElement buyButton = $("button.btn-buy");

    // Clicks the first product card of whatever listing is currently shown (category or search results)
    @Step("Open the first listed product")
    public ProductDetailPage openFirstListedProduct() {
        $(".product-box a.product-name").click();
        return this;
    }

    @Step("Read the product name")
    public String readName() {
        return name.shouldBe(visible).getText().strip();
    }

    @Step("Read the product number")
    public String readNumber() {
        return number.getText().strip();
    }

    @Step("Add the product to the cart")
    public void addToCart() {
        buyButton.click();
    }
}

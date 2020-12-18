package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class AuthResultsPage {
    public SelenideElement profile() {
        return $(".paywall__profile__title");

    }
}

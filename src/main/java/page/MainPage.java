package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class MainPage {
    private SelenideElement tabLogin = $(".paywall__auth__tabs__item:nth-child(2)");
    private SelenideElement loginInput = $(".paywall__auth__form__row:nth-child(1) .paywall__auth__form__input");
    private SelenideElement passwordInput = $(".js-login-validate > .paywall__auth__form__row:nth-child(2) .paywall__auth__form__input");
    private SelenideElement buttonLogin = $(".paywall__auth__form__submit-wrap > .js-yandex-counter");

    public MainPage enterUsername(String text) {

        if(getWebDriver().getPageSource().contains("Зарегистрироваться")==true){ //метод "getWebDriver()" позволяет использовать код selenium в selenide. Метод "getPageSource()" не содержит в себе задержки "implicit wait" и просто проверяет текст на странице. https://www.youtube.com/watch?v=EoRHq5mhxxQ
            tabLogin.click();
            loginInput.val(text);}
        else {
            loginInput.val(text); }

        return this;
    }

    public MainPage enterPassword(String text) {
        passwordInput
                .val(text)
                .pressEnter();
        return this;
    }

    public void submit() {
        buttonLogin.click();
    }
}

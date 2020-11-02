package page;

import com.codeborne.selenide.Selenide;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class LoginPage {

    public LoginPage open() {

        Selenide.open("/");
        return this;
    }

/*    public boolean CheckRegForm(){
        boolean notExist;
        try{
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            notExist = driver.findElements(by).size()<1;
        }finally{
            driver.manage().timeouts().implicitlyWait(MY_OLD_TIMEOUT, TimeUnit.SECONDS);
        }
        return notExists;
    }*/

    public LoginPage enterUsername(String text) {

        //SelenideElement foo=$(".paywall__auth__form__submit-wrap:nth-child(5) > .paywall__auth__form__submit").shouldHave(Condition.exactText("Зарегистрироваться")).size()==0;

        if(getWebDriver().getPageSource().contains("Зарегистрироваться")==true){ //метод "getWebDriver()" позволяет использовать код selenium в selenide. Метод "getPageSource()" не содержит в себе задержки "implicit wait" и просто проверяет текст на странице. https://www.youtube.com/watch?v=EoRHq5mhxxQ
            $(".paywall__auth__tabs__item:nth-child(2)").click();
            $(".paywall__auth__form__row:nth-child(1) .paywall__auth__form__input").val(text);}
        else {
            $(".paywall__auth__form__row:nth-child(1) .paywall__auth__form__input").val(text); }

        return this;
    }

    public LoginPage enterPassword(String text) {
        $(".js-login-validate > .paywall__auth__form__row:nth-child(2) .paywall__auth__form__input").val(text);
        return this;
    }

    public void submit() {
        $(".paywall__auth__form__submit-wrap > .js-yandex-counter").click();
    }
}

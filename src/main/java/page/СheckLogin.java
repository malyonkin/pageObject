package page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.conditions.ExactText;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

public class СheckLogin{

    public void loginCheck() {
        $(".paywall__profile__title").shouldHave(Condition.exactText("Отключение баннеров")); //Check subscribe
    }

    /*public  void checkOuntLogin(){
        $(By.cssSelector(".paywall__auth__title")).shouldHave(Condition.exactText("Чтобы начать, зарегистрируйтесь или войдите1")); //Check logaut
    }*/
}

package page;

import static com.codeborne.selenide.Selenide.$;

public class ProfileMenu{
    public void clickMenu(){
        $(".topline__auth__link").click();
    }

    public void exit(){
        $(".topline__auth__profile__menu__link > b").click();
    }
}

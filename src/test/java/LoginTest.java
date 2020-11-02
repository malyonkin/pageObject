import com.codeborne.selenide.Condition;
import com.codeborne.selenide.*;
import static com.codeborne.selenide.Selenide.*;

import org.junit.jupiter.api.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import page.LoginPage;
import page.ProfileMenu;
import page.СheckLogin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.notNullValue;

import static config.userConfig.USER_PASSWORD;
import static config.userConfig.USER_LOGIN;

//https://selenide.gitbooks.io/user-guide/content/ru/pageobjects.html
public class LoginTest {
    //создание объектов классов пакета "page"
    СheckLogin check = new СheckLogin();
    ProfileMenu menuUser = new ProfileMenu();
    LoginPage auth = new LoginPage();
    WebDriver driver;

    @BeforeEach //хорошая статья по нотификации jUnit - https://www.vogella.com/tutorials/JUnit/article.html
    public void setup() {
        //Configuration.browser="firefox"; //запуск тестов в браузере "firefox". Можно положить драйвер в папку с проектом (и обращаться по относительному пути). Можно скачивать из Интернета драйвер - https://automation-remarks.com/selenium-webdriver-manager/index.html
        //Configuration.browser = "org.openqa.selenium.safari.SafariDriver"; ////запуск тестов в браузере "safari"
        //Configuration.browser = "opera"; //https://ru.stackoverflow.com/questions/1159095/%D0%9A%D0%B0%D0%BA-%D0%B7%D0%B0%D1%81%D1%82%D0%B0%D0%B2%D0%B8%D1%82%D1%8C-selenide-%D0%BE%D1%82%D0%BA%D1%80%D1%8B%D1%82%D1%8C-%D0%9E%D0%BF%D0%B5%D1%80%D1%83-%D0%B2%D0%BC%D0%B5%D1%81%D1%82%D0%BE-%D0%9B%D0%B8%D1%81%D0%B8%D1%86%D1%8B

        Configuration.baseUrl = "https://auth.rbc.ru/login?tab=enter&from=login_topline"; //сокращаем URL
        Configuration.browserSize = "1366x768";
        Configuration.timeout = 6000;
        Configuration.fastSetValue = true;
        RestAssured.baseURI = "https://auth.rbc.ru/v2"; //URL API
        RestAssured.useRelaxedHTTPSValidation(); //расслабленной проверки HTTP
        //open("https://auth.rbc.ru/login?tab=enter&from=login_topline", LoginPage.class);
        //System.setProperty("chromeoptions.mobileEmulation", "deviceName=iphone X");
        //Configuration.browserVersion; //как автоматически обновлять версии драйверов?
    }

    //@RepeatedTest(2) //кол-во повторений теста
    @org.junit.jupiter.api.Test
    public void APIauthUser() { //авторизация через API
        //Set-Cookie: rat=3zln2EfoVYCPz_9HnBINRpAFmO;
        //Set-Cookie: ra_session=7a016aafcfa7483f86c0028d09d7008f; --он же session_id
        Response response = RestAssured.given().log().body()
                .contentType("application/json; charset=UTF-8")
                .accept(ContentType.JSON)
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .when()
                .then().log().all()
                .statusCode(HttpStatus.SC_OK) //SC_OK проверяет статус 200
                .body("data.session_id",notNullValue()) //Проверка на наличие Token
                .with()
                .post("/user/login/");
    }

    @DisplayName("Успашная авторизация") //имя теста
    @Tag("fast") //используется для запуска разных тестов. В данном случае используется тег "быстрый профиль" (он же короткий)
    //@EnumSource(value = Users.class, names = {"Ivanov", "Petrov"})
    @org.junit.jupiter.api.Test
    public void correctLogin() {
        auth.open().enterUsername(USER_LOGIN); //только в этом месте инициализируем открытие/запуск (один раз для всех тестов ниже) драйвера Chrome
        auth.enterPassword(USER_PASSWORD);
        //sleep(1000);
        auth.submit();
        check.loginCheck(); //выполняется проверка сценария, которая ИМЕННО тестирует наш сценарий
    }

    @org.junit.jupiter.api.Test
    //если у нас появляется ошибка в тесте, остальные тесты (ниже) запускаются, процесс тестирования/компиляции следующих тестов не останавливается
    public void logout(){
        menuUser.clickMenu();
        menuUser.exit();
        $(By.cssSelector(".paywall__auth__title")).shouldHave(Condition.exactText("Чтобы начать, зарегистрируйтесь или войдите1")); //Check logaut. Минус, не скрываем проверку в читабельный/понятный вид
    }

    @Test
    public void correctLoginNext() {
        auth.enterUsername(USER_LOGIN);
        auth.enterPassword(USER_PASSWORD);
        auth.submit();
        check.loginCheck();
    }

    @AfterEach
    public void close() {
        Selenide.closeWindow(); //Selenide.closeWebDriver();
    }
}

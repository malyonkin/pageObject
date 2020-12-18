import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Epic;
import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import page.*;

import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static config.userConfig.USER_LOGIN;
import static config.userConfig.USER_PASSWORD;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
//mvn test -D groups=smoke //запуск тестов только с @Teg - smoke
//https://selenide.gitbooks.io/user-guide/content/ru/pageobjects.html
public class LoginTest {
    //создание объектов классов пакета "page"
    СheckLogin check = new СheckLogin();
    ProfileMenu menuUser = new ProfileMenu();
    LoginPage auth = new LoginPage();

    final RequestSpecification requestSpec = new RequestSpecBuilder() //Спецификация для ЗАПРОСОВ API - сокращение дублирования кода. См тест ниже - .spec(requestSpec)
            .setBaseUri("https://auth.rbc.ru/v2")
            .setContentType(ContentType.JSON)
            .setBasePath("/user/{name}/")
            .build();

    final ResponseSpecification responseSpec = new ResponseSpecBuilder() //Крутой проект - https://github.com/Biloleg/RestAshuredDemo
            .expectStatusCode(200)
            .expectBody("$",hasKey("data")) //Проверка наличия секции "data"
            .expectResponseTime(lessThan(5000L))
            .build();


    @BeforeEach //хорошая статья по нотификации jUnit - https://www.vogella.com/tutorials/JUnit/article.html
    public void setup() { //То, что выполняется до запуска тестов (@Test)
        //Configuration.browser="firefox"; //запуск тестов в браузере "firefox". Можно положить драйвер в папку с проектом (и обращаться по относительному пути). Можно скачивать из Интернета драйвер - https://automation-remarks.com/selenium-webdriver-manager/index.html
        //Configuration.browser = "org.openqa.selenium.safari.SafariDriver"; ////запуск тестов в браузере "safari"
        //Configuration.browser = "opera"; //https://ru.stackoverflow.com/questions/1159095/%D0%9A%D0%B0%D0%BA-%D0%B7%D0%B0%D1%81%D1%82%D0%B0%D0%B2%D0%B8%D1%82%D1%8C-selenide-%D0%BE%D1%82%D0%BA%D1%80%D1%8B%D1%82%D1%8C-%D0%9E%D0%BF%D0%B5%D1%80%D1%83-%D0%B2%D0%BC%D0%B5%D1%81%D1%82%D0%BE-%D0%9B%D0%B8%D1%81%D0%B8%D1%86%D1%8B
        Configuration.baseUrl = "https://auth.rbc.ru/login?tab=enter&from=login_topline"; //сокращаем URL
        Configuration.browserSize = "1366x768";
        Configuration.timeout = 6000;
        Configuration.fastSetValue = true;
        RestAssured.baseURI = "https://auth.rbc.ru/v2"; //URL API
        RestAssured.useRelaxedHTTPSValidation(); //расслабленной проверки HTTP

        RestAssured.filters(new AllureRestAssured()); //ВАЖНО: подключение логирования rest assured к Allure
        //RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter()); //Включение логирования во всех API assured тестах
        //open("https://auth.rbc.ru/login?tab=enter&from=login_topline", LoginPage.class);
        //System.setProperty("chromeoptions.mobileEmulation", "deviceName=iphone X");
        //Configuration.browserVersion; //как автоматически обновлять версии драйверов?
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false)); //подключение Allure к Selenide
    }

    //Используется для авторизации через API. Вынести из теста в main. Пример с json - https://automation-remarks.com/2017/code-generation/index.html
    public Cookie[] APIauthUser() { //Здесь мы берем только куки
        //Set-Cookie: rat=3zln2EfoVYCPz_9HnBINRpAFmO; --он же token
        //Set-Cookie: ra_session=7a016aafcfa7483f86c0028d09d7008f; --он же session_id
        Response response = given().log().cookies()
                .contentType("application/json; charset=UTF-8")
                .accept(ContentType.JSON)
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .when()
                .then().log().all()
                .statusCode(HttpStatus.SC_OK) //SC_OK проверяет статус 200
                .body("data.session_id", notNullValue()) //Проверка на наличие Token
                .with()
                .post("/user/login/");

        String cookieRat = response.getCookie("rat"); //получить Куку из ответа сервера по Имени куки - getCookie("cookieName")
        String cookieRa_session = response.getCookie("ra_session");
        //System.out.println("rat: " + cookieRat);
        //System.out.println("ra_session: " + cookieRa_session);

        Cookie RAT = new Cookie("rat", cookieRat); //сохранение куки в формате - ключ: значение
        //System.out.println("Смотри сюда - rat: " + cookieRat);
        Cookie RA_SESSION = new Cookie("ra_session", cookieRa_session);

        return new Cookie[]{RAT, RA_SESSION};
    }

    //Здесь берём куку. Нужно перенести в main класс
    public String EnterLogin() {
        Response response = given()
                .contentType("application/json; charset=UTF-8")
                .accept(ContentType.JSON)
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .when()
                .then().log().all() //Включаем логирование
                .statusCode(200) //SC_OK проверяет статус 200
                .body("data.session_id", notNullValue()) //ВАЖНО: Обратить внимание на Data.session .Проверка на наличие Token.
                .with()
                .post("/user/login/");
        String cookieRa_session1 = response.getCookie("ra_session");
        System.out.println("Долбанная кука - " + cookieRa_session1);

        return cookieRa_session1;
    }

    @Test
    @DisplayName("API")
    @Tag("smoke")
    @Epic("login")
    public void EnterLogin1() {
        given()
                .contentType("application/json; charset=UTF-8")
                .accept(ContentType.JSON)
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .when()
                .then().log().all() //Включаем логирование
                .statusCode(200) //SC_OK проверяет статус 200
                .body("data.session_id", notNullValue()) //ВАЖНО: Обратить внимание на Data.session .Проверка на наличие Token.
                .with()
                .post("/user/login/");
    }

    @Tag("test")
    @Test
    public void getEmail2(){
        //given().log().body()
        String pattern = "^(.*)@*";
        Pattern r = Pattern.compile(pattern);

        when() //для работы верхней строки .when
                .get("https://www.1secmail.com/api/v1/?action=genRandomMailbox&count=1").
        then()
                .assertThat()
                .statusCode(200)
                //.body("", equalTo("/\A[^@]+@([^@\.]+\.)+[^@\.]+\z/")) //Не используются рег выражения
                .body(matchesPattern((pattern))); //поиск/проверка получения е-майла на наличие символа @
    }

    @Tag("test")
    @Test
    public void EnterLogin2() {
        given() //инфо для отправки нашего запроса. ВАЖНО: дублируется боди с блоком when
                .baseUri("https://auth.rbc.ru/v2")
                .header("content-Type", "application/json")
                .basePath("/user/login/")
                //.param("page",2)
                .log().body()
        .when().log().all() //тип запроса - get, post
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .post()
        .then().log().all() //Делаются проверки над ответом от Сервера
                .statusCode(200) //SC_OK проверяет статус 200
                .body("data.session_id", notNullValue()) //ОЧЕНЬ ВАЖНО: Можно использовать индексацию - data.session_id[1].profile и поддерживаются условия -  data.session_id{it.rate<10}. https://seleniumcamp.com/talk/best-practices-in-api-testing-with-rest-assured/
                .time(lessThan(5000L)); //проверка - если тест будет обрабатывать больше 5 секунт, то статус теста будет failed
    }

    @Tag("test")
    @Test
    public void UsingSpeca() { //С использованием спецификации см. BeforEach
        given() //инфо для отправки нашего запроса. ВАЖНО: дублируется боди с блоком when
                .spec(requestSpec)
                .pathParam("name","login") //смотри requestSpec - параметризация
                .log().body()
                .when().log().all() //тип запроса - get, post
                .body("{\"email\": \"" + USER_LOGIN + "\", \"password\":\"qwerty\"}")
                .post()
                .then().log().all() //Делаются проверки над ответом от Сервера
                .spec(responseSpec)
                .body("data.session_id", notNullValue());
    }

    @Tag("test")
    @Test
    public void APIauthUserGet() { //https://www.baeldung.com/rest-assured-header-cookie-parameter
        given().cookie("session_id", EnterLogin()).when().get("/user/info/") //используем куки/token/auth для запроса
                .then()
                .statusCode(200)
                .and()
                .assertThat()
                .body("data.profile.email", equalTo("rbcuser@yandex.ru")); //ВАЖНО: Обратить внимание на классы, которые перечислены через точку - data.profile...
    }

    @Tag("test")
    @Test
    public void APIauthUserTest1() {
        Response response = given()
                .cookie("session_id", EnterLogin())
                //.cookie("session_id", "34538b1b4ff9450cac9f6b848e407ffe")
                .when()
                .then().log().all() //Включаем логирование
                .statusCode(200) //SC_OK проверяет статус 200
                .body("data.profile.display_name", equalTo("Платный чувак"))
                .with()
                .get("/user/info/");

        /*"data": {
            "profile": {
                "display_name": "Платный чувак",*/

    String responseBody1 = response.getBody().asString();
    JsonPath values1 = new JsonPath(responseBody1);
    String categories = values1.getString("data.profile");
        System.out.println("Показать всё что в DATA - "+categories);

    String usernames = response.jsonPath().getString("data.profile.display_name");
    String userstatus = response.jsonPath().getString("status");
    String usernamesSystem = response.jsonPath().get("data.profile.user_name");
        System.out.println("Имя Пользователя На экране - "+usernames);
        System.out.println("Статус ответа - "+userstatus);
        System.out.println("Имя Пользователя В системе - "+usernamesSystem);
}

    @Tag("test")
    @Test
    public void getEmail(){ //сервер выдачи временного e-mail-а
        //given().queryParam("q", "john").when().get("https://www.1secmail.com/api/v1/?action=genRandomMailbox&count=1")
        when()
                .get("https://www.1secmail.com/api/v1/?action=genRandomMailbox&count=1").
        then().statusCode(200)
                .and()
                .assertThat()
                .body("", notNullValue()); //проверяем, что поле не пустое
    }

    @Tag("test")
    @Test
    public void getEmail1(){ //забираем результаты запроса
        Response response = given()
                .when()
                .get("https://www.1secmail.com/api/v1/?action=genRandomMailbox&count=1");
        ResponseBody body = response.getBody();
        System.out.println("А вот и наш e-mail из BODY: " + body.asString());


    }

    @Tag("test")
    @Test
    public void useCookieWithLoginGUI(){
        Cookie result[] = APIauthUser();
        System.out.println(result[0]);
        System.out.println(result[1]);
        open("/"); //сюда записываем значения КУКи в браузере, что не использовать сценарий авторизации (логин и пароль)
        getWebDriver().manage().addCookie(result[0]); //подставляем куку в браузер
        getWebDriver().manage().addCookie(result[1]);
        refresh(); //Обновление страницы, чтобы подтянулись куки и прошла авторизация WebDriverRunner.getWebDriver().navigate().refresh();
        menuUser.clickMenu();
        menuUser.exit();
        sleep(1000);
    }

    @DisplayName("Успашная авторизация") //имя теста
    @Tag("fast") //используется для запуска разных тестов. В данном случае используется тег "быстрый профиль" (он же короткий)
    //@EnumSource(value = Users.class, names = {"Ivanov", "Petrov"})
    @Test
    public void correctLogin() {
        auth.open().enterUsername(USER_LOGIN); //только в этом месте инициализируем открытие/запуск (один раз для всех тестов ниже) драйвера Chrome
        auth.enterPassword(USER_PASSWORD);
        //sleep(1000);
        auth.submit();
        check.loginCheck(); //выполняется проверка сценария, которая ИМЕННО тестирует наш сценарий
    }

    @Tag("test")
    @Test //Выход из логина
    //если у нас появляется ошибка в тесте, остальные тесты (ниже) запускаются, процесс тестирования/компиляции следующих тестов не останавливается
    public void logout(){
        menuUser.clickMenu();
        menuUser.exit();
        $(".paywall__auth__title").shouldHave(Condition.exactText("Чтобы начать, зарегистрируйтесь или войдите")); //Check logaut. Минус, не скрываем проверку в читабельный/понятный вид
        $(byText("Вход с Apple")).shouldBe(Condition.visible);//byText - поиск по тексту
    }

    @DisplayName("API")
    @Tag("smoke")
    @Epic("login")
    @Test //повторный вход в систему
    public void correctLoginNext() {
        open("/");
        auth.enterUsername(USER_LOGIN);
        auth.enterPassword(USER_PASSWORD);
        auth.submit();
        //Selenide.closeWebDriver();
    }

    @Tag("test")
    @Test //повторный вход в систему
    public void PO_1() {
        open("/", MainPage.class) //https://automation-remarks.com/2016/selenide-shadow-sides/index.html
            .enterUsername(USER_LOGIN)
            .enterPassword(USER_PASSWORD);
    }

    /*@Test //повторный вход в систему
    public void PO_2() {
        var loginpage = LoginPage.open();
        loginpage.enterUsername(USER_LOGIN);
        loginpage.enterPassword(USER_PASSWORD);
    }*/

    @Tag("test")
    @Test //https://selenide.org/documentation/page-objects.html
    public void PO_3() {
        LoginPage loginPage1 = open("/", LoginPage.class)
                .enterUsername(USER_LOGIN)
                .enterPassword(USER_PASSWORD)
                .submit();
        //loginPage1.submit();
        //AuthResultsPage authResultsPage = loginPage1.enterUsername(USER_LOGIN).enterPassword(USER_PASSWORD);
        AuthResultsPage authResultsPage = new AuthResultsPage();
        authResultsPage.profile().should(visible);
        authResultsPage.profile().shouldHave(text("Отключение баннеров"));
    }

    /*@AfterEach
    public void close() {
        Selenide.closeWindow(); //Selenide.closeWebDriver();
    }*/
}

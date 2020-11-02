import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static config.userConfig.USER_LOGIN;
import static org.hamcrest.CoreMatchers.notNullValue;

public class startTests {
    public static void main(String[] args) {
        //RunJUnit5TestsFromJava runner = new RunJUnit5TestsFromJava();
        //runner.runAll();
        System.out.println("test");
    }
}

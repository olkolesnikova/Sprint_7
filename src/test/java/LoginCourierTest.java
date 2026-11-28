import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Courier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginCourierTest {

    private int courierId;

    @BeforeEach

    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.education-services.ru";
    }

    @AfterEach
    public void tearDown() {

        if (courierId != 0) {
            HashMap<String, String> queryForDeletion = new HashMap<>();
            queryForDeletion.put("id", String.valueOf(courierId));

            Response responseForDeletion = given()
                    .header("Content-Type", "application/json")
                    .body(queryForDeletion)
                    .when()
                    .delete("/api/v1/courier/" + courierId);

            responseForDeletion.then().statusCode(200);
        }

    }

    @Test
    @DisplayName("Успешный логин курьера")

    void loginCourierSuccess() {

        Courier courier = new Courier("Alexander_" + UUID.randomUUID().toString().substring(0, 8), "12345", "Alex");

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .body(courier)
                        .when()
                        .post("/api/v1/courier");

        response.then()
                .assertThat().body("ok", equalTo(true))
                .statusCode(201);

        Map<String, String> bodyForLogin = new HashMap<>();
        bodyForLogin.put("login", courier.getLogin());
        bodyForLogin.put("password", courier.getPassword());


        Response responseAfterLogin =
                given()
                        .header("Content-Type", "application/json")
                        .body(bodyForLogin)
                .when()
                        .post("/api/v1/courier/login");

        responseAfterLogin.then()
                .assertThat().body("id", notNullValue())
                .statusCode(200);

        courierId = responseAfterLogin
                .then()
                .extract().path("id");

        System.out.println("courierId: " + courierId);
    }

    @Test
    @DisplayName("Неуспешный логин курьера без логина")

    void loginCourierWithoutLogin() {

        Map<String, String> bodyForLogin = new HashMap<>();
        bodyForLogin.put("password", "12345");

        given()
                .header("Content-Type", "application/json")
                .body(bodyForLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().body("message", equalTo("Недостаточно данных для входа"))
                .statusCode(400);
    }

    @Test
    @DisplayName("Неуспешный логин курьера без пароля")

    void loginCourierWithoutPassword() {

        Map<String, String> bodyForLogin = new HashMap<>();
        bodyForLogin.put("login", "ninja");

        given()
                .header("Content-Type", "application/json")
                .body(bodyForLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().body("message", equalTo("Недостаточно данных для входа"))
                .statusCode(400);
    }

    @Test
    @DisplayName("Логин с неправильными данными")

    void loginWithWrongLogin() {
        Map<String, String> bodyForLogin = new HashMap<>();
        bodyForLogin.put("login", "abcdefghijklmn");
        bodyForLogin.put("password", "1");
        given()
                .header("Content-Type", "application/json")
                .body(bodyForLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().body("message", equalTo("Учетная запись не найдена"))
                .statusCode(404);
    }

}

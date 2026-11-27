import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Courier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreationCourierTest {

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
    @DisplayName("Успешное создание курьера")
    void createCourierSuccess() {

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

        courierId = given()
                .header("Content-Type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .extract().body().path("id");

    }

    @Test
    @DisplayName("Создание курьера с существующим логином")
    void createCourierWithExistLogin() {

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

        courierId = given()
                .header("Content-Type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .extract().body().path("id");

        Response responseForExistLogin =
                given()
                        .header("Content-Type", "application/json")
                        .body(courier)
                        .when()
                        .post("/api/v1/courier");

        responseForExistLogin.then()
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .statusCode(409);
    }

    @Test
    @DisplayName("Создание курьера без логина")
    void createCourierWithoutLogin() {

        Map<String,String> queryWithoutLogin = new HashMap<>();
        queryWithoutLogin.put("password", "12345");
        queryWithoutLogin.put("lastName", "Alexander");
        given()
                .header("Content-Type", "application/json")
                .body(queryWithoutLogin)
                .when()
                .post("/api/v1/courier")
                .then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .statusCode(400);

    }

    @Test
    @DisplayName("Создание курьера без пароля")
    void createCourierWithoutPassword() {

        Map<String,String> queryWithoutPassword = new HashMap<>();
        queryWithoutPassword.put("login", "ninja");
        queryWithoutPassword.put("lastName", "Alexander");
        given()
                .header("Content-Type", "application/json")
                .body(queryWithoutPassword)
                .when()
                .post("/api/v1/courier")
                .then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .statusCode(400);

    }
}

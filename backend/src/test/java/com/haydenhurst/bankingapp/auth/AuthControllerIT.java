package com.haydenhurst.bankingapp.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT {

    @LocalServerPort
    private int port = 8081;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void register_shouldReturnSuccess() {
        Response response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "testuser123@example.com",
                  "password": "Password123!",
                  "fullName": "Test User",
                  "phoneNumber": "5551234567",
                  "address": "123 Test Street",
                  "DOB": "2003-01-01"
                }
                """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body(containsString("successfully"))
                .extract()
                .response();

        System.out.println("Register response: " + response.asString());
    }

    @Test
    void login_shouldReturnJwtToken() {
        Response response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "testuser123@example.com",
                  "password": "Password123!"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body(not(blankOrNullString()))  // assuming you return the token as plain string
                .extract()
                .response();

        String token = response.asString();
        System.out.println("Login token: " + token);
    }
}

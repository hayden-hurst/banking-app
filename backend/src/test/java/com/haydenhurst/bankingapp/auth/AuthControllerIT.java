package com.haydenhurst.bankingapp.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void register_shouldReturnSuccess() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "testuser_" + unique + "@example.com";
        String phoneNumber = "555" + String.format("%07d", Math.abs(unique.hashCode()) % 10_000_000);

        Response response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Test User",
                  "phoneNumber": "%s",
                  "address": "123 Test Street",
                  "DOB": "2003-01-01"
                }
                """.formatted(email, phoneNumber))
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
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "testuser_" + unique + "@example.com";
        String phoneNumber = "555" + String.format("%07d", Math.abs(unique.hashCode()) % 10_000_000);

        // register first
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Test User",
                  "phoneNumber": "%s",
                  "address": "123 Test Street",
                  "DOB": "2003-01-01"
                }
                """.formatted(email, phoneNumber))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

        // then login
        Response response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "Password123!"
                }
                """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body(not(blankOrNullString()))
                .extract()
                .response();

        String token = response.asString();
        System.out.println("Login token: " + token);
    }
}
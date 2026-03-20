package com.haydenhurst.bankingapp.kyc;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KycControllerIT {

    @LocalServerPort
    private int port;

    private static String userToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @Order(1)
    void registerAndLoginUser() {
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "kycuser7@example.com",
                  "password": "Password123!",
                  "fullName": "Kyc User",
                  "phoneNumber": "5558756557",
                  "address": "456 Kyc Street",
                  "DOB": "2001-01-01"
                }
                """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

        Response loginResponse = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "kycuser2@example.com",
                  "password": "Password123!"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        System.out.println("Login response body: " + loginResponse.asString());
        userToken = loginResponse.asString();
    }

    @Test
    @Order(2)
    void createKycProfile_shouldReturnCreated() {
        Response response = given()
                .port(port)
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body("""
        {
          "rawSSN": "123456789",
          "documentType": "PASSPORT",
          "rawDocumentNumber": "A12345678"
        }
        """)
                .when()
                .post("/api/kyc");

        System.out.println("Create KYC status: " + response.statusCode());
        System.out.println("Create KYC body: " + response.asString());

        response.then().statusCode(201);
    }

    @Test
    @Order(3)
    void getMyKycProfile_shouldReturnProfile() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/kyc")
                .then()
                .statusCode(200)
                .body("documentType", equalTo("PASSPORT"))
                .body("status", equalTo("UNVERIFIED"));
    }

    @Test
    @Order(4)
    void getMyKycStatus_shouldReturnStatus() {
        given()
                .port(port)
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/kyc/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("UNVERIFIED"));
    }
}
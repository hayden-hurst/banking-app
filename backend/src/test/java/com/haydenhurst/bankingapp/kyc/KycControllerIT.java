package com.haydenhurst.bankingapp.kyc;

import com.haydenhurst.bankingapp.common.enums.Role;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KycControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KycRepository kycRepository;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void createKycProfile_shouldReturnCreated() {
        TestUserContext user = registerAndLoginUser(false);

        given()
                .port(port)
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(validKycBody())
                .when()
                .post("/api/kyc")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("documentType", equalTo("PASSPORT"))
                .body("status", equalTo("UNVERIFIED"));
    }

    @Test
    void getMyKycProfile_shouldReturnProfile() {
        TestUserContext user = registerAndLoginUser(false);
        createKycForUser(user.token());

        given()
                .port(port)
                .header("Authorization", "Bearer " + user.token())
                .when()
                .get("/api/kyc")
                .then()
                .statusCode(200)
                .body("documentType", equalTo("PASSPORT"))
                .body("status", equalTo("UNVERIFIED"));
    }

    @Test
    void getMyKycStatus_shouldReturnStatus() {
        TestUserContext user = registerAndLoginUser(false);
        createKycForUser(user.token());

        given()
                .port(port)
                .header("Authorization", "Bearer " + user.token())
                .when()
                .get("/api/kyc/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("UNVERIFIED"));
    }

    @Test
    void regularUser_shouldBeForbiddenFromAdminEndpoints() {
        TestUserContext regularUser = registerAndLoginUser(false);
        TestUserContext anotherUser = registerAndLoginUser(false);

        createKycForUser(anotherUser.token());

        Long targetUserId = userRepository.findByEmail(anotherUser.email())
                .orElseThrow(() -> new IllegalStateException("Target user not found"))
                .getId();

        given()
                .port(port)
                .header("Authorization", "Bearer " + regularUser.token())
                .when()
                .get("/api/kyc/admin/{userId}", targetUserId)
                .then()
                .statusCode(403);

        given()
                .port(port)
                .header("Authorization", "Bearer " + regularUser.token())
                .when()
                .get("/api/kyc/admin?status=UNVERIFIED")
                .then()
                .statusCode(403);

        given()
                .port(port)
                .header("Authorization", "Bearer " + regularUser.token())
                .when()
                .post("/api/kyc/admin/{userId}/start-review", targetUserId)
                .then()
                .statusCode(403);

        given()
                .port(port)
                .header("Authorization", "Bearer " + regularUser.token())
                .when()
                .post("/api/kyc/admin/{userId}/approve", targetUserId)
                .then()
                .statusCode(403);

        given()
                .port(port)
                .header("Authorization", "Bearer " + regularUser.token())
                .when()
                .post("/api/kyc/admin/{userId}/deny", targetUserId)
                .then()
                .statusCode(403);
    }

    @Test
    void admin_shouldGetSpecificUserKycProfile() {
        TestUserContext admin = registerAndLoginUser(true);
        TestUserContext customer = registerAndLoginUser(false);

        createKycForUser(customer.token());

        Long customerUserId = userRepository.findByEmail(customer.email())
                .orElseThrow(() -> new IllegalStateException("Customer user not found"))
                .getId();

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .get("/api/kyc/admin/{userId}", customerUserId)
                .then()
                .statusCode(200)
                .body("documentType", equalTo("PASSPORT"))
                .body("status", equalTo("UNVERIFIED"));
    }

    @Test
    void admin_shouldListKycProfilesByStatus() {
        TestUserContext admin = registerAndLoginUser(true);
        TestUserContext customer1 = registerAndLoginUser(false);
        TestUserContext customer2 = registerAndLoginUser(false);

        createKycForUser(customer1.token());
        createKycForUser(customer2.token());

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .get("/api/kyc/admin?status=UNVERIFIED")
                .then()
                .statusCode(200)
                .body("status", org.hamcrest.Matchers.everyItem(equalTo("UNVERIFIED")));
    }

    @Test
    void admin_shouldStartReviewAndApproveKyc() {
        TestUserContext admin = registerAndLoginUser(true);
        TestUserContext customer = registerAndLoginUser(false);

        createKycForUser(customer.token());

        Long customerUserId = userRepository.findByEmail(customer.email())
                .orElseThrow(() -> new IllegalStateException("Customer user not found"))
                .getId();

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .post("/api/kyc/admin/{userId}/start-review", customerUserId)
                .then()
                .statusCode(200)
                .body("status", equalTo("PENDING_REVIEW"));

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .post("/api/kyc/admin/{userId}/approve", customerUserId)
                .then()
                .statusCode(200)
                .body("status", equalTo("VERIFIED"));

        given()
                .port(port)
                .header("Authorization", "Bearer " + customer.token())
                .when()
                .get("/api/kyc/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("VERIFIED"));
    }

    @Test
    void admin_shouldStartReviewAndDenyKyc() {
        TestUserContext admin = registerAndLoginUser(true);
        TestUserContext customer = registerAndLoginUser(false);

        createKycForUser(customer.token());

        Long customerUserId = userRepository.findByEmail(customer.email())
                .orElseThrow(() -> new IllegalStateException("Customer user not found"))
                .getId();

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .post("/api/kyc/admin/{userId}/start-review", customerUserId)
                .then()
                .statusCode(200)
                .body("status", equalTo("PENDING_REVIEW"));

        given()
                .port(port)
                .header("Authorization", "Bearer " + admin.token())
                .when()
                .post("/api/kyc/admin/{userId}/deny", customerUserId)
                .then()
                .statusCode(200)
                .body("status", equalTo("DENIED"));

        given()
                .port(port)
                .header("Authorization", "Bearer " + customer.token())
                .when()
                .get("/api/kyc/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("DENIED"));
    }

    private TestUserContext registerAndLoginUser(boolean makeAdmin) {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "kycuser_" + unique + "@example.com";
        String phone = "555" + String.format("%07d", Math.abs(unique.hashCode()) % 10_000_000);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Kyc User",
                  "phoneNumber": "%s",
                  "address": "456 Kyc Street",
                  "DOB": "2001-01-01"
                }
                """.formatted(email, phone))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

        if (makeAdmin) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Registered user not found"));

            Set<Role> roles = new HashSet<>(user.getRoles());
            roles.add(Role.ADMIN);
            user.setRoles(roles);
            userRepository.save(user);
        }

        Response loginResponse = given()
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
                .extract()
                .response();

        return new TestUserContext(email, loginResponse.asString());
    }

    private void createKycForUser(String token) {
        given()
                .port(port)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(validKycBody())
                .when()
                .post("/api/kyc")
                .then()
                .statusCode(201);
    }

    private String validKycBody() {
        return """
                {
                  "rawSSN": "123456789",
                  "documentType": "PASSPORT",
                  "rawDocumentNumber": "A12345678"
                }
                """;
    }

    private record TestUserContext(String email, String token) {}
}
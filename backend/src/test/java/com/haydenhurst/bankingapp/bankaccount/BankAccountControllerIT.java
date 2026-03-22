package com.haydenhurst.bankingapp.bankaccount;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankAccountControllerIT {

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
    void createBankAccount_shouldReturnCreated_whenKycVerified() {
        TestUserContext ctx = registerLoginAndVerifyKyc();

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "Main Checking",
                          "bankAccountType": "CHECKING"
                        }
                        """)
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("accountNumber.length()", equalTo(20))
                .body("accountNickname", equalTo("Main Checking"))
                .body("type", equalTo("CHECKING"))
                .body("balance", notNullValue());
    }

    @Test
    void getAllBankAccounts_shouldReturnAccountsForAuthenticatedUser() {
        TestUserContext ctx = registerLoginAndVerifyKyc();

        // create first account
        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "Checking One",
                          "bankAccountType": "CHECKING"
                        }
                        """)
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(201);

        // create second account
        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "Savings One",
                          "bankAccountType": "SAVINGS"
                        }
                        """)
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(201);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .when()
                .get("/api/bank-accounts")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("accountNickname", hasItems("Checking One", "Savings One"))
                .body("type", hasItems("CHECKING", "SAVINGS"));
    }

    @Test
    void getBankAccountDetails_shouldReturnAccountDetails() {
        TestUserContext ctx = registerLoginAndVerifyKyc();

        Response createResponse = given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "Daily Spending",
                          "bankAccountType": "CHECKING"
                        }
                        """)
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long accountId = createResponse.jsonPath().getLong("id");

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .when()
                .get("/api/bank-accounts/{accountId}", accountId)
                .then()
                .statusCode(200)
                .body("id", equalTo(accountId.intValue()))
                .body("accountNickname", equalTo("Daily Spending"))
                .body("type", equalTo("CHECKING"))
                .body("status", equalTo("ACTIVE"))
                .body("minimumBalance", notNullValue())
                .body("overdraftLimit", notNullValue());
    }

    @Test
    void createBankAccount_shouldFail_whenKycNotVerified() {
        TestUserContext ctx = registerAndLoginOnly();

        // create KYC but do NOT verify it
        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rawSSN": "123456789",
                          "documentType": "PASSPORT",
                          "rawDocumentNumber": "A12345678"
                        }
                        """)
                .when()
                .post("/api/kyc")
                .then()
                .statusCode(201);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "Blocked Account",
                          "bankAccountType": "CHECKING"
                        }
                        """)
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    private TestUserContext registerAndLoginOnly() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "bankuser_" + unique + "@example.com";
        String phone = "555" + String.format("%07d", Math.abs(unique.hashCode()) % 10_000_000);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Password123!",
                          "fullName": "Bank User",
                          "phoneNumber": "%s",
                          "address": "123 Banking Lane",
                          "DOB": "2000-01-01"
                        }
                        """.formatted(email, phone))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200);

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

    private TestUserContext registerLoginAndVerifyKyc() {
        TestUserContext ctx = registerAndLoginOnly();

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rawSSN": "123456789",
                          "documentType": "PASSPORT",
                          "rawDocumentNumber": "A12345678"
                        }
                        """)
                .when()
                .post("/api/kyc")
                .then()
                .statusCode(201);

        User user = userRepository.findByEmail(ctx.email())
                .orElseThrow(() -> new IllegalStateException("User not found in test setup"));

        Kyc kyc = kycRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("KYC not found in test setup"));

        kyc.setStatus(KycStatus.VERIFIED);
        kycRepository.save(kyc);

        return ctx;
    }

    private record TestUserContext(String email, String token) {}
}
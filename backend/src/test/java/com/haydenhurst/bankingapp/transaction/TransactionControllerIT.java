package com.haydenhurst.bankingapp.transaction;

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
class TransactionControllerIT {

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
    void createDeposit_shouldReturnCreated() {
        TestUserContext ctx = registerLoginAndVerifyKyc();
        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "type": "DEPOSIT",
                          "amount": 250.00,
                          "description": "Initial deposit",
                          "destinationAccountNumber": null
                        }
                        """)
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", accountNumber)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("accountNumber", equalTo(accountNumber))
                .body("type", equalTo("DEPOSIT"))
                .body("amount", notNullValue())
                .body("description", equalTo("Initial deposit"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void createWithdrawal_shouldReturnCreated_whenFundsExist() {
        TestUserContext ctx = registerLoginAndVerifyKyc();
        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        createTransaction(ctx.token(), accountNumber, """
                {
                  "type": "DEPOSIT",
                  "amount": 300.00,
                  "description": "Fund account",
                  "destinationAccountNumber": null
                }
                """).then().statusCode(201);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "type": "WITHDRAWAL",
                          "amount": 75.00,
                          "description": "ATM withdrawal",
                          "destinationAccountNumber": null
                        }
                        """)
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", accountNumber)
                .then()
                .statusCode(201)
                .body("accountNumber", equalTo(accountNumber))
                .body("type", equalTo("WITHDRAWAL"))
                .body("description", equalTo("ATM withdrawal"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void createTransfer_shouldReturnCreated() {
        TestUserContext ctx = registerLoginAndVerifyKyc();

        String sourceAccountNumber = createBankAccount(ctx.token(), "Checking", "CHECKING");
        String destinationAccountNumber = createBankAccount(ctx.token(), "Savings", "SAVINGS");

        createTransaction(ctx.token(), sourceAccountNumber, """
                {
                  "type": "DEPOSIT",
                  "amount": 500.00,
                  "description": "Initial funding",
                  "destinationAccountNumber": null
                }
                """).then().statusCode(201);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "type": "TRANSFER",
                          "amount": 125.00,
                          "description": "Move to savings",
                          "destinationAccountNumber": "%s"
                        }
                        """.formatted(destinationAccountNumber))
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", sourceAccountNumber)
                .then()
                .statusCode(201)
                .body("accountNumber", equalTo(sourceAccountNumber))
                .body("type", equalTo("TRANSFER_OUT"))
                .body("description", equalTo("Move to savings"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void getAllTransactions_shouldReturnTransactionsForAccount() {
        TestUserContext ctx = registerLoginAndVerifyKyc();
        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        createTransaction(ctx.token(), accountNumber, """
                {
                  "type": "DEPOSIT",
                  "amount": 400.00,
                  "description": "Paycheck",
                  "destinationAccountNumber": null
                }
                """).then().statusCode(201);

        createTransaction(ctx.token(), accountNumber, """
                {
                  "type": "WITHDRAWAL",
                  "amount": 50.00,
                  "description": "Groceries",
                  "destinationAccountNumber": null
                }
                """).then().statusCode(201);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .when()
                .get("/api/bank-accounts/{accountNumber}/transactions", accountNumber)
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("accountNumber", everyItem(equalTo(accountNumber)))
                .body("type", hasItems("DEPOSIT", "WITHDRAWAL"))
                .body("description", hasItems("Paycheck", "Groceries"));
    }

    @Test
    void getTransactionDetails_shouldReturnTransaction() {
        TestUserContext ctx = registerLoginAndVerifyKyc();
        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        Response createResponse = createTransaction(ctx.token(), accountNumber, """
                {
                  "type": "DEPOSIT",
                  "amount": 150.00,
                  "description": "Starter deposit",
                  "destinationAccountNumber": null
                }
                """)
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long transactionId = createResponse.jsonPath().getLong("id");

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .when()
                .get("/api/bank-accounts/{accountNumber}/transactions/{transactionId}", accountNumber, transactionId)
                .then()
                .statusCode(200)
                .body("id", equalTo(transactionId.intValue()))
                .body("accountNumber", equalTo(accountNumber))
                .body("type", equalTo("DEPOSIT"))
                .body("description", equalTo("Starter deposit"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void createWithdrawal_shouldFail_whenInsufficientFunds() {
        TestUserContext ctx = registerLoginAndVerifyKyc();
        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "type": "WITHDRAWAL",
                          "amount": 999.00,
                          "description": "Too much",
                          "destinationAccountNumber": null
                        }
                        """)
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", accountNumber)
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    void createTransaction_shouldFail_whenKycNotVerified() {
        TestUserContext ctx = registerLoginAndVerifyKyc();

        String accountNumber = createBankAccount(ctx.token(), "Main Checking", "CHECKING");

        User user = userRepository.findByEmail(ctx.email())
                .orElseThrow(() -> new IllegalStateException("User not found in test setup"));

        Kyc kyc = kycRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("KYC not found in test setup"));

        // simulate losing verified status after account already exists
        kyc.setStatus(KycStatus.UNVERIFIED);
        kycRepository.save(kyc);

        given()
                .port(port)
                .header("Authorization", "Bearer " + ctx.token())
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "type": "DEPOSIT",
                      "amount": 100.00,
                      "description": "Should fail",
                      "destinationAccountNumber": null
                    }
                    """)
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", accountNumber)
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    private Response createTransaction(String token, String accountNumber, String requestBody) {
        return given()
                .port(port)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/bank-accounts/{accountNumber}/transactions", accountNumber);
    }

    private String createBankAccount(String token, String nickname, String accountType) {
        Response response = given()
                .port(port)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "accountNickname": "%s",
                          "bankAccountType": "%s"
                        }
                        """.formatted(nickname, accountType))
                .when()
                .post("/api/bank-accounts")
                .then()
                .statusCode(201)
                .extract()
                .response();

        return response.jsonPath().getString("accountNumber");
    }

    private TestUserContext registerAndLoginOnly() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = "txuser_" + unique + "@example.com";
        String phone = "555" + String.format("%07d", Math.abs(unique.hashCode()) % 10_000_000);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Password123!",
                          "fullName": "Transaction User",
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
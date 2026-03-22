# Banking App

## Project Scope
A full-stack banking application built with **Spring Boot** and **PostgreSQL**, featuring secure authentication, KYC verification, bank account creation & management, and transaction tracking. This project showcases both backend development with RESTful APIs, database interactions, security, encryption, and testing, as well as a frontend for a seamless user experience.

---

## In-Progress Features

- **Bank Account Module** - Allow verified users to create bank accounts and perform transactions.
- **Transaction Module** – Support deposits, withdrawals, transfers, and transaction history retrieval.
- **Frontend Application** - Build a user interface for authentication, KYC submission, and banking operations.

---

## Implemented Features

- **User Authentication** – Secure registration and login with JWT-based authentication.
- **KYC Verification Workflow** - Users can submit KYC information, view their status, and admins can review/approve/deny submissions.
- **Bank Account Module** - Verified users can create bank accounts, retrieve all of their accounts, and view detailed information for a specific account.
- **Sensitive Data Encryption** – Sensitive KYC fields are encrypted before being stored in the database.
- **PostgreSQL Integration** – Persistent relational data storage using PostgreSQL.
- **RESTful API Design** – Structured backend API endpoints for authentication and KYC workflows.
- **Testing** – Unit tests and API integration tests for critical flows.

---

## Security Highlights

- JWT-based authentication for protected API access
- Spring Security role-based authorization
- KYC verification required before bank account or transaction api use
- KYC admin endpoints restricted to users with the `ADMIN` role
- AES/GCM encryption for sensitive KYC data at rest
- BCrypt password hashing for user credentials
- DTO-based request/response boundaries to avoid exposing internal entities
- Validation and centralized exception handling for safer API behavior

---

## Tech Stack

- **Frontend (in progress):** TypeScript, HTML, CSS
- **Backend:** Java 17, Spring Boot, Spring Security, JWT
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Version Control:** Git & GitHub
- **Testing:** Unit Testing (JUnit with Mockito) & API Integration Testing (RestAssured)

---

## Installation & Setup

Follow these steps to set up the project locally:

### [ **1** ] Clone the Repository
```sh
git clone https://github.com/hayden-hurst/banking-app.git
cd banking-app
```

### [ **2** ] Configure the Application
Edit the `src/main/resources/application.properties` file and set your PostgreSQL credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banking_app
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Make sure to also edit JWT secret (must be >= 32 chars):
```properties
jwt.secret=superSecretKeyThatIsExactly32Chars!!
```

Make sure to also configure your encryption key for local development:
```properties
app.encryption.secret=yourBase64Encoded32ByteKeyHere
```

### [ **3** ] Run the Application
```sh
mvn spring-boot:run
```

---

## API Endpoints

### Authentication
| Method | Endpoint                                                      | Description                    |
|--------|---------------------------------------------------------------|--------------------------------|
| `POST` | `/api/auth/register`                                          | Register a new user            |
| `POST` | `/api/auth/login`                                             | Authenticate and receive a JWT |

### KYC
| Method | Endpoint                               | Description                                               |
|--------|----------------------------------------|-----------------------------------------------------------|
| `POST` | `/api/kyc`                             | Create a new KYC profile with initial status `UNVERIFIED` |
| `GET`  | `/api/kyc`                             | Get the authenticated user's KYC profile                  |
| `GET`  | `/api/kyc/status`                      | Get the authenticated user's KYC verification status      |
| `GET`  | `/api/kyc/admin/{userId}`              | ADMIN only: Get KYC profile of specific user              |
| `GET`  | `/api/kyc/admin?status=UNVERIFIED`     | ADMIN only: Get all profiles filtered by status           |
| `POST` | `/api/kyc/admin/{userId}/start-review` | ADMIN only: Set status to `PENDING_REVIEW`                |
| `POST` | `/api/kyc/admin/{userId}/approve`      | ADMIN only: Set status to `VERIFIED`                      | 
| `POST` | `/api/kyc/admin/{userId}/deny`         | ADMIN only: Set status to `DENIED`                        |

### Bank Accounts (Requires KYC status: `VERIFIED`)
| Method | Endpoint                                                       | Description                                        |
|--------|----------------------------------------------------------------|----------------------------------------------------|
| `POST` | `/api/bank-accounts`                                           | Create a new bank account                          |
| `GET`  | `/api/bank-accounts`                                           | Get all bank accounts for the authenticated user   |
| `GET`  | `/api/bank-accounts/{accountId}`                               | Get details for a specific bank account            |

### In Progress - Transactions (Requires KYC status: `VERIFIED`)
| Method | Endpoint                                                       | Description                                        |
|--------|----------------------------------------------------------------|----------------------------------------------------|
| `POST` | `/api/bank-accounts/{accountId}/transactions`                  | Create a transaction (deposit, withdraw, transfer) |
| `GET`  | `/api/bank-accounts/{accountId}/transactions`                  | Get all transactions for a bank account            |
| `GET`  | `/api/bank-accounts/{accountId}/transactions/{transactionId}`  | Get details for a specific transaction             |

---

## Future Improvements

- Add multi-factor authentication (MFA) for enhanced security.
- Support multiple currencies using a third-party exchange rate API (e.g., CurrencyLayer) for accurate conversions.
- Add an admin dashboard for KYC review and user management.
- IP tracking for fraud detection and for account protection (e.g., login from different location/ip must be approved before authorizing)
- Device login detection (e.g., user must approve login on different device)
- Introduce a NoSQL datastore (e.g., Redis for caching or MongoDB for analytics/personalization) to support performance and future product features.

---

## Contact

For any questions or suggestions, reach out via [GitHub Issues](https://github.com/hayden-hurst/banking-app/issues).


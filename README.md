# Banking App

## Project Scope
A full-stack banking application built with **Spring Boot** and **PostgreSQL**, featuring secure authentication, KYC verification, bank account creation & management, and transaction tracking. This project showcases both backend development with RESTful APIs, database interactions, security, encryption, and testing, as well as a frontend for a seamless user experience.

---

## In-Progress Features
- **Frontend Application** - Build a user interface for authentication, KYC submission, and banking operations.

---

## Implemented Features

- **User Authentication** – Secure registration and login with JWT-based authentication.
- **KYC Verification Workflow** - Users can submit KYC information, view their status, and admins can review/approve/deny submissions.
- **Bank Account Module** - Verified users can create bank accounts, retrieve all of their accounts, and view detailed information for a specific account.
- **Transaction Module** – Support deposits, withdrawals, transfers, and transaction history retrieval.
- **Sensitive Data Encryption** – Sensitive KYC fields are encrypted before being stored in the database.
- **PostgreSQL Integration** – Persistent relational data storage using PostgreSQL.
- **RESTful API Design** – Structured backend API endpoints for authentication, KYC, and bank account workflows.
- **Testing** – Unit tests and API integration tests for critical flows.

---

## Security Highlights

- JWT-based authentication for protected API access
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
- **Database:** PostgreSQL with Flyway migrations
- **Build Tool:** Maven
- **Containerization:** Docker and Docker Compose
- **Version Control:** Git & GitHub
- **Testing:** Unit Testing (JUnit with Mockito) & API Integration Testing (RestAssured)

---

## Installation & Setup

### What Docker Does Here

A **Dockerfile** gives instructions for building an image, which packages the application and its runtime. A **container** is a running instance of that image. **Docker Compose** starts the containers together using a configuration file. This makes the application environment more consistent between computers. [Docker Compose introduction](https://docs.docker.com/get-started/docker-concepts/the-basics/what-is-docker-compose/)

This project uses two containers. One runs the Java backend, and the other runs PostgreSQL. The files that control them are `backend/Dockerfile` and `backend/docker-compose.yml`.

### Start the Backend Locally

Install Java 17, Maven, Docker with the Compose plugin, and Git. Run these commands in a terminal.

```bash
git clone https://github.com/hayden-hurst/banking-app.git
cd banking-app/backend
```

Before starting, open `docker-compose.yml` and put double quotes around the existing development encryption key.

```yaml
APP_ENCRYPTION_SECRET: "12345678901234567890123456789012"
```

This is a small setup exercise. YAML is the configuration format Compose reads. Quotes tell it to keep this value as text. Without quotes, Compose can interpret the digits as a number and change the value. That breaks encryption because the application needs a key of the correct length. This public example key is only for local practice with fake data.

Now build the Java application and start the containers.

```bash
mvn clean package -DskipTests
docker compose up --build -d
docker compose ps
docker compose logs --tail=100 backend
```

Maven creates the runnable Java package, called a **JAR**. The Dockerfile copies that JAR into the backend image. `--build` builds the image, `-d` keeps the containers running in the background, `ps` shows their status, and `logs` shows application messages. `-DskipTests` skips running tests for this local setup command.

The backend is available at `http://localhost:8081`. PostgreSQL is available on your computer at port `5433`. Inside the containers, the backend reaches PostgreSQL through the service name `db` on port `5432`. Compose waits until PostgreSQL can accept connections before starting the backend.

Use `docker compose stop` to stop the containers. After Java code changes, run the build and start commands again. The current database storage needs improvement before keeping important data, so use fake records you can recreate. The current Compose configuration also publishes ports beyond your computer's loopback address. Keep this setup on a trusted local network until we work through restricting access.

## Flyway Database Migrations

A **database migration** is a saved, numbered change to the database structure, such as creating a table or adding a column. Saving these changes with the code helps different copies of the application build the same database structure.

Flyway runs the SQL files in `backend/src/main/resources/db/migration/` when the backend starts. The first file, `V1__initial_schema.sql`, creates the application's tables in an empty database. Flyway records completed migrations in a table called `flyway_schema_history` so it can track what has already run. [Flyway migration guide](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/versioned-migrations)

For a later database change, add a new file such as `V2__add_account_index.sql`. Keep migrations that have already been applied unchanged. This preserves the history shared by existing databases. If startup reports that tables already exist but there is no Flyway history, keep any data you need and investigate before changing the database.

Spring profiles are groups of settings. Compose selects the `dev` profile for development. It currently lets Hibernate, the tool that maps Java objects to database tables, change tables automatically. The `prod` profile instead checks that the tables match the Java code after Flyway runs. We should eventually use this checking behavior during development too, so missing migrations are caught earlier.

## Current Deployment Setup

**Deployment** means putting the application onto the machine where it will run. The GitHub Actions file at `.github/workflows/docker-ci.yml` already builds and uploads a backend image when code is pushed to `main`.

It uploads the image to GitHub Container Registry, a place to store container images, under `ghcr.io/hayden-hurst/banking-backend`. It labels the image with `latest` and a short Git commit ID so a build can be connected to its source code. The workflow currently skips running tests. Starting the application on a server is still a manual step.

The production settings read database credentials and encryption keys from **environment variables**, which are values supplied to the running application. These are `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `AES_SECRET`, `JWT_SECRET`, and `JWT_EXPIRATION`. Selecting `SPRING_PROFILES_ACTIVE=prod` activates those settings. A production environment also needs decisions about network access, encrypted connections, and backups.

## Improvements to Learn One at a Time

These are proposed exercises. Each should include an explanation, a small change, and a check that the change works.

1. **Run tests before uploading an image.** A successful build shows that the application can be packaged. Tests check specific behavior. We also need to configure Maven to run the existing integration tests, which check how parts of the application work together. Start with the GitHub Actions introduction, then the Maven integration test guide. [GitHub Actions basics](https://docs.github.com/en/actions/get-started/understand-github-actions) · [Maven Failsafe](https://maven.apache.org/surefire/maven-failsafe-plugin/)
2. **Protect keys and passwords.** A secret committed to Git can remain in its history. Learn how to supply real secrets separately from code and replace exposed secrets. Keep the AES key available when restoring encrypted KYC records, since the records need that key to be read again.
3. **Practice saving and restoring the database.** A Docker volume gives a container a separate place to store data. A backup provides a recoverable copy. Learn both before keeping important data, then prove that a backup can be restored.

## Security Learning Lab — Planned

The long-term goal is to use this project to practice finding security weaknesses, explaining their impact, and fixing them. Planned exercises cover the application, its host computer and network, and physical access to devices and backups. The lab plan and a format for documenting findings are in the security learning guide. [Security learning guide](docs/security/README.md)

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
| Method | Endpoint                             | Description                                        |
|--------|--------------------------------------|----------------------------------------------------|
| `POST` | `/api/bank-accounts`                 | Create a new bank account                          |
| `GET`  | `/api/bank-accounts`                 | Get all bank accounts for the authenticated user   |
| `GET`  | `/api/bank-accounts/{accountNumber}` | Get details for a specific bank account            |

### Transactions (Requires KYC status: `VERIFIED`)
| Method | Endpoint                                                       | Description                                        |
|--------|----------------------------------------------------------------|----------------------------------------------------|
| `POST` | `/api/bank-accounts/{accountNumber}/transactions`                  | Create a transaction (deposit, withdraw, transfer) |
| `GET`  | `/api/bank-accounts/{accountNumber}/transactions`                  | Get all transactions for a bank account            |
| `GET`  | `/api/bank-accounts/{accountNumber}/transactions/{transactionId}`  | Get details for a specific transaction             |

---

## Future Improvements

- Integrate RabbitMQ to offload heavy background tasks, preventing API downtime during high-traffic spikes.
- Add a lightweight Go service to consume queue events, handling file parsing and logging with a minimal memory footprint.
- Log unchangeable financial events to disk to build reliable transaction history and system rollbacks.
- Add multi-factor authentication (MFA) for enhanced security.
- Support multiple currencies using a third-party exchange rate API (e.g., CurrencyLayer) for accurate conversions.
- Add an admin dashboard for KYC review and user management.
- IP tracking for fraud detection and for account protection (e.g., login from different location/ip must be approved before authorizing)
- Device login detection (e.g., user must approve login on different device)
- Introduce a NoSQL datastore (e.g., Redis for caching or MongoDB for analytics/personalization) to support performance and future product features.

---

## Contact

For any questions or suggestions, reach out via [GitHub Issues](https://github.com/hayden-hurst/banking-app/issues).


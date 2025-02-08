# Banking App (Personal Project)

A **Spring Boot-powered** banking application with **PostgreSQL** integration, providing secure authentication, transaction management, and account tracking. Designed as a **personal project** to showcase backend development skills, database interactions, and RESTful API implementation.

---

## Base Features

- **User Authentication** – Secure login, registration, and session management.  
- **Deposit & Withdrawal** – Allows users to perform basic banking transactions.  
- **Transaction History** – Logs and retrieves past transactions.  
- **PostgreSQL Integration** – Persistent data storage for user accounts and transactions.  
- **RESTful API** – Clean and structured API endpoints for account operations.  

---

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Security
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Version Control:** Git & GitHub
- **Testing:** JUnit

---

## Installation & Setup

Follow these steps to set up the project locally:

### **1** Clone the Repository
```sh
git clone https://github.com/hayden-hurst/banking-app.git
cd banking-app
```

### **2** Configure the Database
Edit the `src/main/resources/application.properties` file and set your PostgreSQL credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banking_app
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### **3** Run the Application
```sh
mvn spring-boot:run
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|---------|------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and receive a token |
| `GET` | `/api/accounts/{id}` | Get account details |
| `POST` | `/api/accounts/deposit` | Deposit money into an account |
| `POST` | `/api/accounts/withdraw` | Withdraw money from an account |

---

## Future Improvements

- Add multi-factor authentication (MFA) for enhanced security.  
- Implement scheduled transactions.  
- Support for multiple currencies.  
- Add an admin dashboard for user management.

---

## Contact

For any questions or suggestions, reach out via [GitHub Issues](https://github.com/hayden-hurst/banking-app/issues).


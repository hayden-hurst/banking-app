package com.haydenhurst.bankingapp.model;

import com.haydenhurst.bankingapp.enums.Role;
import com.haydenhurst.bankingapp.enums.UserAccountStatus;
import jakarta.persistence.*;
import org.springframework.cglib.core.Local;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    // move this to service layer -> private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ==================================================
    // Field Declarations
    // ==================================================
        // Essential User Info //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // hashed with BCrypt

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAccountStatus status; // ACTIVE, SUSPENDED, CLOSED

        // Security & Banking Features //
    @Column(nullable = false)
    private boolean accountNonLocked; // prevents login if account is locked

    @Column(nullable = false)
    private boolean twoFactorAuthEnabled;

    @Column(nullable = false)
    private int failedLoginAttempts;

    private LocalDateTime lastLogin;

    // ADMINS will be redirected to their own dashboard upon login,
    // they should have access to review flagged accounts,
    // view user and bank account information like transaction history,
    // and also have the ability to manage the user and bank account status
    // they will NOT be able to edit transaction history or any other account information.
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role roles; // CUSTOMER, ADMIN

    @Column(nullable = true)
    private String passwordResetToken; // temp token for pass recovery (these will also be hashed before being stored on the database)

    // ==================================================
    // Constructors
    // ==================================================
    public User(){
        this.status = UserAccountStatus.ACTIVE;
        this.roles = Role.CUSTOMER;
        this.accountNonLocked = true;
        this.twoFactorAuthEnabled = false;
        this.failedLoginAttempts = 0;
    }
    public User(String email, String password, String fullName, String phoneNumber, LocalDate dob, String address, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.address = address;

    }

    // ==================================================
    // Get / Set
    // ==================================================
        // Essential User Info //
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
        // needs to be hashed in service layer -> encoder.encode(rawPassword);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }
    // move this to service layer -> if (status == AccountStatus.CLOSED){System.out.print("cannot change account status if CLOSED");return;}

        // Security & Banking Features //
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isTwoFactorAuthEnabled() {
        return twoFactorAuthEnabled;
    }

    public void setTwoFactorAuthEnabled(boolean twoFactorAuthEnabled) {
        this.twoFactorAuthEnabled = twoFactorAuthEnabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    // ==================================================
    // Utility Methods
    // ==================================================
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dob=" + dob +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status=" + status +
                ", accountNonLocked=" + accountNonLocked +
                ", twoFactorEnabled=" + twoFactorAuthEnabled +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", lastLogin=" + lastLogin +
                ", roles=" + roles +
                '}';
    }

    // ==================================================
    // Lifecycle Callback Methods
    // ==================================================
    @PrePersist
    protected void onCreateUser() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdateUser() {
        updatedAt = LocalDateTime.now();
    }
}

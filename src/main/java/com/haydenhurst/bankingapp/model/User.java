package com.haydenhurst.bankingapp.model;

import com.haydenhurst.bankingapp.enums.AccountStatus;
import com.haydenhurst.bankingapp.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    // move this to controller layer -> private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
    private AccountStatus status; // ACTIVE, SUSPENDED, CLOSED

        // Security & Banking Features //
    @Column(nullable = false)
    private boolean accountNonLocked; // prevents login if account is locked

    @Column(nullable = false)
    private boolean twoFactorAuthEnabled;

    @Column(nullable = false)
    private int failedLoginAttempts;

    private LocalDateTime lastLogin;

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Column(nullable = true)
    private String passwordResetToken; // temp token for pass recovery


    // ==================================================
    // Constructors
    // ==================================================
    public User(){
        this.accountNonLocked = true;
        this.twoFactorAuthEnabled = false;
        this.failedLoginAttempts = 0;
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        // move this to controller layer -> if (status == AccountStatus.CLOSED){System.out.print("cannot change account status if CLOSED");return;}
        this.status = status;
    }

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

    public void setPassword(String rawPassword) {
        // needs to be done in controller layer -> encoder.encode(rawPassword);
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

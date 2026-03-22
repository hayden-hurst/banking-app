package com.haydenhurst.bankingapp.user.model;

import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import com.haydenhurst.bankingapp.common.enums.Role;
import com.haydenhurst.bankingapp.common.enums.UserAccountStatus;
import com.haydenhurst.bankingapp.common.util.MaskingUtil;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    // ==================================================
    // Field Declarations
    // ==================================================
        // Essential User Info //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String hashedPassword;

    // we will have separate input fields for first, middle, and lastname that combine into this single fullName field for easy access...
    @Column(nullable = false)
    private String fullName; // verified through kyc

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    // date of birth
    @Column(nullable = false)
    private LocalDate dob;  // verified through kyc

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAccountStatus status; // ACTIVE, SUSPENDED, CLOSED

        // Relations //
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Kyc kycProfile; // 'know your customer' data

    @OneToMany(mappedBy = "accountHolder")
    private List<BankAccount> bankAccounts;

    /* Should make separate schema called credit profile which would store credit rating, etc. and tie to the user
    @Column(nullable = false)
    private BigDecimal creditRating;
    */
    // Security & Banking Features //
    @Column(nullable = false)
    private boolean accountNonLocked; // prevents login if account is locked

    @Column(nullable = false)
    private boolean twoFactorAuthEnabled;

    // add biometric Auth such as face recognition or other forms of auth

    @Column(nullable = false)
    private int failedLoginAttempts;

    private LocalDateTime lastLogin;

    // add security questions for password reset, as well as ssn, account number, and other forms of verification.

    // ADMINS will be redirected to their own dashboard upon login,
    // they should have access to review flagged accounts,
    // view user and bank account information like transaction history,
    // and also have the ability to manage the user and bank account status
    // they will NOT be able to edit transaction history or any other account information.
    // make system that allows you to create admin accounts for the banking app
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles; // CUSTOMER, ADMIN, TELLER

    private String passwordResetToken; // temp token for pass recovery (these will also be hashed before being stored on the database)

    // ==================================================
    // Constructors
    // ==================================================
    public User(){
        this.status = UserAccountStatus.ACTIVE;
        this.roles = new HashSet<>(Collections.singleton(Role.CUSTOMER));
        this.accountNonLocked = true;
        this.twoFactorAuthEnabled = false;
        this.failedLoginAttempts = 0;
    }

    // ==================================================
    // Get / Set
    // ==================================================
        // Essential User Info //
    public Long getId() { return id; }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; } // hashed in auth service

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDOB() { return dob; }
    public void setDOB(LocalDate dob) { this.dob = dob; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UserAccountStatus getStatus() { return status; }
    public void setStatus(UserAccountStatus status) { this.status = status; }

        // Security & Banking Features //
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    public boolean isTwoFactorAuthEnabled() { return twoFactorAuthEnabled; }
    public void setTwoFactorAuthEnabled(boolean twoFactorAuthEnabled) { this.twoFactorAuthEnabled = twoFactorAuthEnabled; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Set<Role> getRoles(){ return Collections.unmodifiableSet(roles); }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    // ==================================================
    // Utility Methods
    // ==================================================
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + MaskingUtil.maskEmail(email) +
                ", fullName=" + fullName +
                ", phoneNumber=" + MaskingUtil.maskPhoneNumber(phoneNumber) +
                ", dob=" + dob +
                ", address=" + address +
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return hashedPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // or your logic here
    }

    @Override
    public boolean isEnabled() {
        return status == UserAccountStatus.ACTIVE;
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

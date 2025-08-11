package com.haydenhurst.bankingapp.bankaccount.model;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountStatus;
import com.haydenhurst.bankingapp.transaction.model.Transaction;
import com.haydenhurst.bankingapp.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    // ==================================================
    // Field Declarations
    // ==================================================
        // Essential Account Info //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(length = 20)
    private String accountNickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BankAccountType bankAccountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BankAccountStatus status; // ACTIVE, FROZEN, CLOSED

        // Other fields //
    @Column(nullable = false)
    private BigDecimal minimumBalance;

    @Column(nullable = false)
    private BigDecimal overdraftLimit;

    @Column(nullable = false)
    private BigDecimal interestRate; // implement later

    @Column(nullable = false)
    private BigDecimal creditRating; // implement later

        // Relations //
    @ManyToOne // establish relation with User
    @JoinColumn(name = "user_id", nullable = false)
    private User accountHolder;

    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;

    // ==================================================
    // Constructors
    // ==================================================
    public BankAccount() {
        this.minimumBalance = BigDecimal.valueOf(300); // minimum amount of money allowed to keep an account open
        this.overdraftLimit = BigDecimal.valueOf(50); // amount of overdraft before fees are applied
        this.status = BankAccountStatus.ACTIVE; // Default status
    }

    public BankAccount(User accountHolder, String accountNumber, BigDecimal balance, String currency) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountNickname() { return accountNickname; }
    public void setAccountNickname(String accountNickname) { this.accountNickname = accountNickname; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BankAccountType getAccountType() { return bankAccountType; }
    public void setAccountType(BankAccountType bankAccountType) { this.bankAccountType = bankAccountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BankAccountStatus getStatus() { return status; }
    public void setStatus(BankAccountStatus status) { this.status = status; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Set<Transaction> getTransactions() { return transactions; }
    public void setTransactions(Set<Transaction> transactions) { this.transactions = transactions; }

    // ==================================================
    // Lifecycle Callback Methods
    // ==================================================
    @PrePersist
    public void onCreateAccount() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdateAccount() {
        updatedAt = LocalDateTime.now();
    }
}

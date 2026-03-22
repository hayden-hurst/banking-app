package com.haydenhurst.bankingapp.bankaccount.model;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountStatus;
import com.haydenhurst.bankingapp.transaction.model.Transaction;
import com.haydenhurst.bankingapp.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static java.math.BigDecimal.ZERO;

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
    private BankAccountType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    /* Not implemented yet
    @Column(nullable = false, length = 3)
    private String currency;
    */

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

    /* not implemented yet
    @Column(nullable = false)
    private BigDecimal interestRate;
    */

        // Relations //
    @ManyToOne // establish relation with User
    @JoinColumn(name = "user_id", nullable = false)
    private User accountHolder;

    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    // ==================================================
    // Constructors
    // ==================================================
    public BankAccount() {
        this.minimumBalance = BigDecimal.valueOf(300); // minimum amount of money allowed to keep an account open
        this.overdraftLimit = BigDecimal.valueOf(50); // amount of overdraft before fees are applied
        this.status = BankAccountStatus.ACTIVE; // Default status
        this.balance = ZERO;
    }

    public BankAccount(User accountHolder, String accountNumber, BankAccountType type) {
        this();
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.type = type;
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public Long getId() { return id; }

    public User getAccountHolder() { return accountHolder; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountNickname() { return accountNickname; }
    public void setAccountNickname(String accountNickname) { this.accountNickname = accountNickname; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BankAccountType getType() { return type; }
    public void setType(BankAccountType bankAccountType) { this.type = bankAccountType; }

    public BankAccountStatus getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }

    public BigDecimal getOverdraftLimit() { return overdraftLimit; }

    /*
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    */
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

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

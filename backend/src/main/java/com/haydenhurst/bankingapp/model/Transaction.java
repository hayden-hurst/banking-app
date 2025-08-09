package com.haydenhurst.bankingapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
public class Transaction {

    // ==================================================
    // Field Declarations
    // ==================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @ManyToOne // establish relation with BankAccount
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount bankAccount;

    // ==================================================
    // Constructors
    // ==================================================
    public Transaction() {
    }

    public Transaction(BankAccount bankAccount, BigDecimal amount, String description) {
        this.bankAccount = bankAccount;
        this.amount = amount;
        this.description = description;
    }

    // ==================================================
    // Gets ( once values are set there is no editing )
    // ==================================================
    public Long getId() { return id; }

    public BankAccount getBankAccount() { return bankAccount; }

    public BigDecimal getAmount() { return amount; }

    public String getDescription() { return description; }

    public LocalDateTime getTimestamp() { return timestamp; }

    // ==================================================
    // Lifecycle Callback Methods
    // ==================================================
    @PrePersist
    public void onCreateTransaction() {
        timestamp = LocalDateTime.now();
    }
}

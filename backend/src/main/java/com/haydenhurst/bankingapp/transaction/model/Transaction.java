package com.haydenhurst.bankingapp.transaction.model;

import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import com.haydenhurst.bankingapp.transaction.enums.TransactionStatus;
import com.haydenhurst.bankingapp.transaction.enums.TransactionType;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @ManyToOne // establish relation with BankAccount
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount bankAccount;

    // ==================================================
    // Constructors
    // ==================================================
    public Transaction() {
    }

    public Transaction(BankAccount bankAccount, TransactionType type, BigDecimal amount, String description) {
        this.bankAccount = bankAccount;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public Long getId() { return id; }

    public BankAccount getBankAccount() { return bankAccount; }

    public BigDecimal getAmount() { return amount; }

    public String getDescription() { return description; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public TransactionType getType() { return type; }

    public TransactionStatus getStatus() { return status; }

    public void updateStatus(TransactionStatus status) { this.status = status; }

    // ==================================================
    // Lifecycle Callback Methods
    // ==================================================
    @PrePersist
    public void onCreateTransaction() {
        timestamp = LocalDateTime.now();
        status = TransactionStatus.PENDING;
    }
}

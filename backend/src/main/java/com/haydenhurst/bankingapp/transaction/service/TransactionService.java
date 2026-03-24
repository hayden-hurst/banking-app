package com.haydenhurst.bankingapp.transaction.service;

import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import com.haydenhurst.bankingapp.bankaccount.repository.BankAccountRepository;
import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.transaction.dto.TransactionRequest;
import com.haydenhurst.bankingapp.transaction.dto.TransactionResponse;
import com.haydenhurst.bankingapp.transaction.enums.TransactionRequestType;
import com.haydenhurst.bankingapp.transaction.enums.TransactionStatus;
import com.haydenhurst.bankingapp.transaction.enums.TransactionType;
import com.haydenhurst.bankingapp.transaction.model.Transaction;
import com.haydenhurst.bankingapp.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final KycRepository kycRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public TransactionService(KycRepository kycRepository,
                              TransactionRepository transactionRepository,
                              BankAccountRepository bankAccountRepository) {
        this.kycRepository = kycRepository;
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public TransactionResponse createTransaction(Long userId, String accountNumber, TransactionRequest request) {
        BankAccount sourceAccount = getOwnedAccount(userId, accountNumber);
        verifyUserKyc(userId);
        validateTransactionRequest(request);

        return switch (request.type()) {
            case DEPOSIT -> handleDeposit(sourceAccount, request);
            case WITHDRAWAL -> handleWithdrawal(sourceAccount, request);
            case TRANSFER -> handleTransfer(sourceAccount, request);
        };
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(Long userId, String accountNumber) {
        BankAccount account = getOwnedAccount(userId, accountNumber);

        return transactionRepository.findAllByBankAccountIdOrderByTimestampDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionDetails(Long userId, String accountNumber, Long transactionId) {
        BankAccount account = getOwnedAccount(userId, accountNumber);

        Transaction transaction = transactionRepository.findByIdAndBankAccountId(transactionId, account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return mapToResponse(transaction);
    }

    private BankAccount getOwnedAccount(Long userId, String accountNumber) {
        return bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));
    }

    private void verifyUserKyc(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        if (kyc.getStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException("User must have VERIFIED KYC status before creating a transaction");
        }
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.type() == null) {
            throw new IllegalStateException("Transaction type is required");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Transaction amount must be greater than zero");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalStateException("Transaction description is required");
        }

        if (request.type() == TransactionRequestType.TRANSFER) {
            if (request.destinationAccountNumber() == null || request.destinationAccountNumber().isBlank()) {
                throw new IllegalStateException("Destination account number is required for transfers");
            }
        }
    }

    private TransactionResponse handleDeposit(BankAccount bankAccount, TransactionRequest request) {
        Transaction transaction = createTransactionRecord(
                bankAccount,
                TransactionType.DEPOSIT,
                request.amount(),
                request.description()
        );

        bankAccount.setBalance(bankAccount.getBalance().add(request.amount()));
        transaction.updateStatus(TransactionStatus.APPROVED);

        bankAccountRepository.save(bankAccount);
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    private TransactionResponse handleWithdrawal(BankAccount bankAccount, TransactionRequest request) {
        Transaction transaction = createTransactionRecord(
                bankAccount,
                TransactionType.WITHDRAWAL,
                request.amount(),
                request.description()
        );

        if (bankAccount.getBalance().compareTo(request.amount()) < 0) {
            transaction.updateStatus(TransactionStatus.REJECTED);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Insufficient funds");
        }

        bankAccount.setBalance(bankAccount.getBalance().subtract(request.amount()));
        transaction.updateStatus(TransactionStatus.APPROVED);

        bankAccountRepository.save(bankAccount);
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    private TransactionResponse handleTransfer(BankAccount sourceAccount, TransactionRequest request) {
        BankAccount destinationAccount = bankAccountRepository.findByAccountNumber(request.destinationAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination bank account not found"));

        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalStateException("Cannot transfer to the same account");
        }

        Transaction outgoingTransaction = createTransactionRecord(
                sourceAccount,
                TransactionType.TRANSFER_OUT,
                request.amount(),
                request.description()
        );

        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            outgoingTransaction.updateStatus(TransactionStatus.REJECTED);
            transactionRepository.save(outgoingTransaction);
            throw new IllegalStateException("Insufficient funds");
        }

        Transaction incomingTransaction = createTransactionRecord(
                destinationAccount,
                TransactionType.TRANSFER_IN,
                request.amount(),
                request.description()
        );

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.amount()));

        outgoingTransaction.updateStatus(TransactionStatus.APPROVED);
        incomingTransaction.updateStatus(TransactionStatus.APPROVED);

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        Transaction savedOutgoing = transactionRepository.save(outgoingTransaction);
        transactionRepository.save(incomingTransaction);

        return mapToResponse(savedOutgoing);
    }

    private Transaction createTransactionRecord(BankAccount bankAccount, TransactionType type, BigDecimal amount, String description) {
        return new Transaction(
                bankAccount,
                type,
                amount,
                description.trim()
        );
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getBankAccount().getAccountNumber(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTimestamp(),
                transaction.getStatus()
        );
    }
}
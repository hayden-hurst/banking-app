package com.haydenhurst.bankingapp.transaction;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
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
import com.haydenhurst.bankingapp.transaction.service.TransactionService;
import com.haydenhurst.bankingapp.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private KycRepository kycRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createDeposit_success() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        BankAccount sourceAccount = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(100.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.VERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(50.00),
                TransactionRequestType.DEPOSIT,
                "Paycheck",
                null
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            setField(tx, "id", 10L);
            setField(tx, "timestamp", LocalDateTime.now());
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(userId, accountNumber, request);

        assertNotNull(response);
        assertEquals(TransactionType.DEPOSIT, response.type());
        assertEquals(TransactionStatus.APPROVED, response.status());
        assertEquals(BigDecimal.valueOf(150.00), sourceAccount.getBalance());
        assertEquals(accountNumber, response.accountNumber());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        Transaction savedTx = txCaptor.getValue();
        assertEquals(TransactionType.DEPOSIT, savedTx.getType());
        assertEquals(BigDecimal.valueOf(50.00), savedTx.getAmount());
        assertEquals("Paycheck", savedTx.getDescription());
        assertEquals(TransactionStatus.APPROVED, savedTx.getStatus());
    }

    @Test
    void createWithdrawal_success() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        BankAccount sourceAccount = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(200.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.VERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(75.00),
                TransactionRequestType.WITHDRAWAL,
                "ATM",
                null
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            setField(tx, "id", 11L);
            setField(tx, "timestamp", LocalDateTime.now());
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(userId, accountNumber, request);

        assertEquals(TransactionType.WITHDRAWAL, response.type());
        assertEquals(TransactionStatus.APPROVED, response.status());
        assertEquals(BigDecimal.valueOf(125.00), sourceAccount.getBalance());
    }

    @Test
    void createWithdrawal_throwsWhenInsufficientFunds() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        BankAccount sourceAccount = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(25.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.VERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100.00),
                TransactionRequestType.WITHDRAWAL,
                "Too much",
                null
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> transactionService.createTransaction(userId, accountNumber, request)
        );

        assertEquals("Insufficient funds", ex.getMessage());
        assertEquals(BigDecimal.valueOf(25.00), sourceAccount.getBalance());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertEquals(TransactionStatus.REJECTED, txCaptor.getValue().getStatus());

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void createTransfer_success() {
        Long userId = 1L;
        String sourceAccountNumber = "11111111111111111111";
        String destinationAccountNumber = "22222222222222222222";

        BankAccount sourceAccount = buildBankAccount(userId, sourceAccountNumber, BigDecimal.valueOf(500.00));
        BankAccount destinationAccount = buildBankAccount(userId, destinationAccountNumber, BigDecimal.valueOf(100.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.VERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(125.00),
                TransactionRequestType.TRANSFER,
                "Move money",
                destinationAccountNumber
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(sourceAccountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(bankAccountRepository.findByAccountNumber(destinationAccountNumber))
                .thenReturn(Optional.of(destinationAccount));
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            if (tx.getId() == null) {
                setField(tx, "id", (long) (Math.random() * 1000 + 1));
            }
            setField(tx, "timestamp", LocalDateTime.now());
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(userId, sourceAccountNumber, request);

        assertEquals(TransactionType.TRANSFER_OUT, response.type());
        assertEquals(TransactionStatus.APPROVED, response.status());
        assertEquals(BigDecimal.valueOf(375.00), sourceAccount.getBalance());
        assertEquals(BigDecimal.valueOf(225.00), destinationAccount.getBalance());

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
    }

    @Test
    void createTransfer_throwsWhenDestinationAccountNotFound() {
        Long userId = 1L;
        String sourceAccountNumber = "11111111111111111111";
        String destinationAccountNumber = "99999999999999999999";

        BankAccount sourceAccount = buildBankAccount(userId, sourceAccountNumber, BigDecimal.valueOf(500.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.VERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(125.00),
                TransactionRequestType.TRANSFER,
                "Move money",
                destinationAccountNumber
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(sourceAccountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(bankAccountRepository.findByAccountNumber(destinationAccountNumber))
                .thenReturn(Optional.empty());
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(userId, sourceAccountNumber, request)
        );

        assertEquals("Destination bank account not found", ex.getMessage());
    }

    @Test
    void createTransaction_throwsWhenKycNotVerified() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        BankAccount sourceAccount = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(100.00));
        Kyc kyc = buildKyc(buildUser(userId), KycStatus.UNVERIFIED);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(25.00),
                TransactionRequestType.DEPOSIT,
                "Blocked",
                null
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(sourceAccount));
        when(kycRepository.findByUserId(userId)).thenReturn(Optional.of(kyc));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> transactionService.createTransaction(userId, accountNumber, request)
        );

        assertEquals("User must have VERIFIED KYC status before creating a transaction", ex.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_throwsWhenOwnedAccountNotFound() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(25.00),
                TransactionRequestType.DEPOSIT,
                "Blocked",
                null
        );

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(userId, accountNumber, request)
        );

        assertEquals("Bank account not found", ex.getMessage());
        verify(kycRepository, never()).findByUserId(anyLong());
    }

    @Test
    void getAllTransactions_returnsMappedResponses() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";

        BankAccount account = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(400.00));

        Transaction tx1 = buildTransaction(account, 1L, TransactionType.DEPOSIT, BigDecimal.valueOf(200.00), "Deposit", TransactionStatus.APPROVED);
        Transaction tx2 = buildTransaction(account, 2L, TransactionType.WITHDRAWAL, BigDecimal.valueOf(50.00), "Withdrawal", TransactionStatus.APPROVED);

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findAllByBankAccountIdOrderByTimestampDesc(account.getId()))
                .thenReturn(List.of(tx1, tx2));

        List<TransactionResponse> responses = transactionService.getAllTransactions(userId, accountNumber);

        assertEquals(2, responses.size());
        assertEquals(TransactionType.DEPOSIT, responses.get(0).type());
        assertEquals(TransactionType.WITHDRAWAL, responses.get(1).type());
    }

    @Test
    void getTransactionDetails_returnsMappedResponse() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";
        Long transactionId = 77L;

        BankAccount account = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(400.00));
        Transaction tx = buildTransaction(account, transactionId, TransactionType.DEPOSIT, BigDecimal.valueOf(200.00), "Deposit", TransactionStatus.APPROVED);

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByIdAndBankAccountId(transactionId, account.getId()))
                .thenReturn(Optional.of(tx));

        TransactionResponse response = transactionService.getTransactionDetails(userId, accountNumber, transactionId);

        assertEquals(transactionId, response.id());
        assertEquals(accountNumber, response.accountNumber());
        assertEquals(TransactionType.DEPOSIT, response.type());
        assertEquals(TransactionStatus.APPROVED, response.status());
    }

    @Test
    void getTransactionDetails_throwsWhenTransactionNotFound() {
        Long userId = 1L;
        String accountNumber = "11111111111111111111";
        Long transactionId = 99L;

        BankAccount account = buildBankAccount(userId, accountNumber, BigDecimal.valueOf(400.00));

        when(bankAccountRepository.findByAccountNumberAndAccountHolderId(accountNumber, userId))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByIdAndBankAccountId(transactionId, account.getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionDetails(userId, accountNumber, transactionId)
        );

        assertEquals("Transaction not found", ex.getMessage());
    }

    private User buildUser(Long id) {
        User user = new User();
        setField(user, "id", id);
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPhoneNumber("5551234567");
        user.setAddress("123 Main St");
        user.setDOB(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Kyc buildKyc(User user, KycStatus status) {
        Kyc kyc = new Kyc();
        kyc.setUser(user);
        kyc.setStatus(status);
        return kyc;
    }

    private BankAccount buildBankAccount(Long holderId, String accountNumber, BigDecimal balance) {
        User user = buildUser(holderId);
        BankAccount account = new BankAccount(user, accountNumber, BankAccountType.CHECKING);
        account.setBalance(balance);
        setField(account, "id", 10L);
        setField(account, "createdAt", LocalDateTime.now());
        setField(account, "updatedAt", LocalDateTime.now());
        return account;
    }

    private Transaction buildTransaction(BankAccount account, Long id,
                                         TransactionType type,
                                         BigDecimal amount,
                                         String description,
                                         TransactionStatus status) {
        Transaction transaction = new Transaction(account, type, amount, description);
        setField(transaction, "id", id);
        setField(transaction, "timestamp", LocalDateTime.now());
        transaction.updateStatus(status);
        return transaction;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
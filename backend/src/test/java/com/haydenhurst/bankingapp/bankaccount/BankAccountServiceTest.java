package com.haydenhurst.bankingapp.bankaccount;

import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountDetailsResponse;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountRequest;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountResponse;
import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountStatus;
import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import com.haydenhurst.bankingapp.bankaccount.repository.BankAccountRepository;
import com.haydenhurst.bankingapp.bankaccount.service.BankAccountService;
import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycRepository kycRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    // cant do this until we have mock user with kyc profile that has status VERIFIED
    // we also need a test case for if the user fails account creation due to no kyc profile exists/doesnt have VERIFIED status
    @Test
    void createBankAccount_success() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest("Main Checking", BankAccountType.CHECKING);

        User user = buildUser(holderId);
        Kyc kyc = buildKyc(user, KycStatus.VERIFIED);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(kycRepository.findByUserId(holderId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccountResponse response = bankAccountService.createBankAccount(holderId, request);

        assertNotNull(response);
        assertEquals("Main Checking", response.accountNickname());
        assertEquals(BankAccountType.CHECKING, response.type());
        assertNotNull(response.accountNumber());
        assertEquals(20, response.accountNumber().length());

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());

        BankAccount savedAccount = captor.getValue();
        assertEquals(user, savedAccount.getAccountHolder());
        assertEquals(BankAccountType.CHECKING, savedAccount.getType());
        assertEquals("Main Checking", savedAccount.getAccountNickname());
        assertNotNull(savedAccount.getAccountNumber());
        assertEquals(20, savedAccount.getAccountNumber().length());
        assertEquals(ZERO, savedAccount.getBalance());
    }

    @Test
    void createBankAccount_trimsNickname() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest("   Emergency Fund   ", BankAccountType.SAVINGS);

        User user = buildUser(holderId);
        Kyc kyc = buildKyc(user, KycStatus.VERIFIED);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(kycRepository.findByUserId(holderId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccountResponse response = bankAccountService.createBankAccount(holderId, request);

        assertEquals("Emergency Fund", response.accountNickname());
    }

    @Test
    void createBankAccount_allowsNullNickname() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest(null, BankAccountType.CHECKING);

        User user = buildUser(holderId);
        Kyc kyc = buildKyc(user, KycStatus.VERIFIED);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(kycRepository.findByUserId(holderId)).thenReturn(Optional.of(kyc));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccountResponse response = bankAccountService.createBankAccount(holderId, request);

        assertNotNull(response);
        assertNull(response.accountNickname());
    }

    @Test
    void createBankAccount_throwsWhenUserNotFound() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest("Main Checking", BankAccountType.CHECKING);

        when(userRepository.findById(holderId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.createBankAccount(holderId, request)
        );

        assertEquals("User not found", ex.getMessage());
        verify(kycRepository, never()).findByUserId(anyLong());
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void createBankAccount_throwsWhenKycProfileNotFound() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest("Main Checking", BankAccountType.CHECKING);

        User user = buildUser(holderId);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(kycRepository.findByUserId(holderId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.createBankAccount(holderId, request)
        );

        assertEquals("KYC profile not found", ex.getMessage());
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void createBankAccount_throwsWhenKycNotVerified() {
        Long holderId = 1L;
        BankAccountRequest request = new BankAccountRequest("Main Checking", BankAccountType.CHECKING);

        User user = buildUser(holderId);
        Kyc kyc = buildKyc(user, KycStatus.UNVERIFIED);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(kycRepository.findByUserId(holderId)).thenReturn(Optional.of(kyc));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> bankAccountService.createBankAccount(holderId, request)
        );

        assertEquals("User must have VERIFIED KYC status before creating a bank account", ex.getMessage());
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void getAllBankAccounts_returnsMappedResponses() {
        Long holderId = 1L;

        BankAccount checking = buildBankAccount(holderId, "11111111111111111111", "Main Checking", BankAccountType.CHECKING, BigDecimal.valueOf(1500.00));
        BankAccount savings = buildBankAccount(holderId, "22222222222222222222", "Savings", BankAccountType.SAVINGS, BigDecimal.valueOf(5000.00));

        when(bankAccountRepository.findAllByAccountHolderId(holderId)).thenReturn(List.of(checking, savings));

        List<BankAccountResponse> responses = bankAccountService.getAllBankAccounts(holderId);

        assertEquals(2, responses.size());
        assertEquals("Main Checking", responses.get(0).accountNickname());
        assertEquals(BankAccountType.CHECKING, responses.get(0).type());
        assertEquals("Savings", responses.get(1).accountNickname());
        assertEquals(BankAccountType.SAVINGS, responses.get(1).type());
    }

    @Test
    void getBankAccountDetails_returnsMappedResponse() {
        Long holderId = 1L;
        Long accountId = 10L;

        BankAccount bankAccount = buildBankAccount(holderId, "12345678901234567890", "Daily Use", BankAccountType.CHECKING, BigDecimal.valueOf(2500.00));
        setField(bankAccount, "id", accountId);

        when(bankAccountRepository.findByAccountHolderIdAndId(holderId, accountId))
                .thenReturn(Optional.of(bankAccount));

        BankAccountDetailsResponse response = bankAccountService.getBankAccountDetails(holderId, accountId);

        assertNotNull(response);
        assertEquals(accountId, response.id());
        assertEquals("12345678901234567890", response.accountNumber());
        assertEquals("Daily Use", response.accountNickname());
        assertEquals(BankAccountType.CHECKING, response.type());
        assertEquals(BigDecimal.valueOf(2500.00), response.balance());
        assertEquals(BankAccountStatus.ACTIVE, response.status());
    }

    @Test
    void getBankAccountDetails_throwsWhenAccountNotFound() {
        Long holderId = 1L;
        Long accountId = 99L;

        when(bankAccountRepository.findByAccountHolderIdAndId(holderId, accountId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.getBankAccountDetails(holderId, accountId)
        );

        assertEquals("Bank account not found", ex.getMessage());
    }

    private User buildUser(Long id) {
        User user = new User();
        setField(user, "id", id);
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPhoneNumber("5551234567");
        return user;
    }

    private Kyc buildKyc(User user, KycStatus status) {
        Kyc kyc = new Kyc();
        kyc.setUser(user);
        kyc.setStatus(status);
        return kyc;
    }

    private BankAccount buildBankAccount(Long holderId,
                                         String accountNumber,
                                         String nickname,
                                         BankAccountType type,
                                         BigDecimal balance) {
        User user = buildUser(holderId);
        BankAccount bankAccount = new BankAccount(user, accountNumber, type);
        bankAccount.setAccountNickname(nickname);
        bankAccount.setBalance(balance);
        setField(bankAccount, "id", 10L);
        setField(bankAccount, "createdAt", LocalDateTime.now());
        setField(bankAccount, "updatedAt", LocalDateTime.now());
        return bankAccount;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}

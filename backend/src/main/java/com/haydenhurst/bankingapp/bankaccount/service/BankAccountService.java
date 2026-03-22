package com.haydenhurst.bankingapp.bankaccount.service;

import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountDetailsResponse;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountRequest;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountResponse;
import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import com.haydenhurst.bankingapp.bankaccount.repository.BankAccountRepository;
import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class BankAccountService {
    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public BankAccountService(KycRepository kycRepository, UserRepository userRepository, BankAccountRepository bankAccountRepository) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public BankAccountResponse createBankAccount(Long userId, BankAccountRequest request) {
        // user must exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // KYC profile must exist
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        // user must be KYC verified before creating a bank account
        if (kyc.getStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException("User must have VERIFIED KYC status before creating a bank account");
        }

        // could eventually check for number of bank accounts and their types and set a limit on the number you can have

        // generate unique account number
        String accountNumber = generateUniqueAccountNumber();

        // create new bank account with required fields
        BankAccount bankAccount = new BankAccount(
                user,
                accountNumber,
                request.bankAccountType()
        );

        // nickname is optional
        if (request.accountNickname() != null && !request.accountNickname().isBlank()) {
            bankAccount.setAccountNickname(request.accountNickname().trim());
        }

        // save and map
        BankAccount saved = bankAccountRepository.save(bankAccount);
        return mapToResponse(saved);
    }


    public List<BankAccountResponse> getAllBankAccounts(Long holderId){
        return bankAccountRepository.findAllByAccountHolderId(holderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BankAccountDetailsResponse getBankAccountDetails(Long holderId, Long accountId){
        BankAccount bankAccount = bankAccountRepository.findByAccountHolderIdAndId(holderId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        return mapToDetailsResponse(bankAccount);
    }



    private String generateUniqueAccountNumber(){
        String accountNumber;

        do {
            StringBuilder sb = new StringBuilder(20);

            // first digit starts with 1
            sb.append(SECURE_RANDOM.nextInt(9) + 1);

            // remaining digits are 0-9
            for (int i = 1; i < 20; i++){
                sb.append(SECURE_RANDOM.nextInt(10));
            }

            accountNumber = sb.toString();
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private BankAccountResponse mapToResponse(BankAccount bankAccount) {
        return new BankAccountResponse(
                bankAccount.getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountNickname(),
                bankAccount.getType(),
                bankAccount.getBalance(),
                bankAccount.getCreatedAt()
        );
    }

    private BankAccountDetailsResponse mapToDetailsResponse(BankAccount bankAccount) {
        return new BankAccountDetailsResponse(
                bankAccount.getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountNickname(),
                bankAccount.getType(),
                bankAccount.getBalance(),
                bankAccount.getStatus(),
                bankAccount.getMinimumBalance(),
                bankAccount.getOverdraftLimit(),
                bankAccount.getCreatedAt(),
                bankAccount.getUpdatedAt()
        );
    }
}

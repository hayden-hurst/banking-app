package com.haydenhurst.bankingapp.bankaccount.controller;

import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountDetailsResponse;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountRequest;
import com.haydenhurst.bankingapp.bankaccount.dto.BankAccountResponse;
import com.haydenhurst.bankingapp.bankaccount.service.BankAccountService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {
    private final UserService userService;
    private final BankAccountService bankAccountService;

    @Autowired
    public BankAccountController(UserService userService, BankAccountService bankAccountService){
        this.userService = userService;
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public ResponseEntity<BankAccountResponse> createBankAccount(@Valid @RequestBody BankAccountRequest request){
        User currentUser = userService.getCurrentUser();
        BankAccountResponse response = bankAccountService.createBankAccount(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAllBankAccounts(){
        User currentUser = userService.getCurrentUser();
        List<BankAccountResponse> response = bankAccountService.getAllBankAccounts(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // put some filters in here so that you can view different details about an account
    // e.g. /api/bank-accounts/{accountId}?=
    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountDetailsResponse> getBankAccountDetails(@PathVariable Long accountId){
        User currentUser = userService.getCurrentUser();
        BankAccountDetailsResponse response = bankAccountService.getBankAccountDetails(currentUser.getId(), accountId);
        return ResponseEntity.ok(response);
    }
}

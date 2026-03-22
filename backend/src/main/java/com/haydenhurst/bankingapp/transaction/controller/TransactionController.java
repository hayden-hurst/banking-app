package com.haydenhurst.bankingapp.transaction.controller;

import com.haydenhurst.bankingapp.bankaccount.service.BankAccountService;
import com.haydenhurst.bankingapp.transaction.dto.TransactionRequest;
import com.haydenhurst.bankingapp.transaction.dto.TransactionResponse;
import com.haydenhurst.bankingapp.transaction.service.TransactionService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank-accounts/{accountId}/transactions")
public class TransactionController {
    private final UserService userService;
    private final TransactionService transactionService;


    @Autowired
    public TransactionController(UserService userService, TransactionService transactionService, BankAccountService bankAccountService){
        this.userService = userService;
        this.transactionService = transactionService;
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@PathVariable Long accountId, @Valid @RequestBody TransactionRequest request) {
        User currentUser = userService.getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

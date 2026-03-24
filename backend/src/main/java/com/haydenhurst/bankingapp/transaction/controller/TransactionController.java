package com.haydenhurst.bankingapp.transaction.controller;

import com.haydenhurst.bankingapp.transaction.dto.TransactionRequest;
import com.haydenhurst.bankingapp.transaction.dto.TransactionResponse;
import com.haydenhurst.bankingapp.transaction.service.TransactionService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts/{accountNumber}/transactions")
public class TransactionController {
    private final UserService userService;
    private final TransactionService transactionService;

    public TransactionController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@PathVariable String accountNumber, @Valid @RequestBody TransactionRequest request) {
        User currentUser = userService.getCurrentUser();
        TransactionResponse response =
                transactionService.createTransaction(currentUser.getId(), accountNumber, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @PathVariable String accountNumber) {

        User currentUser = userService.getCurrentUser();
        List<TransactionResponse> response =
                transactionService.getAllTransactions(currentUser.getId(), accountNumber);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionDetails(@PathVariable String accountNumber, @PathVariable Long transactionId) {
        User currentUser = userService.getCurrentUser();
        TransactionResponse response = transactionService.getTransactionDetails(currentUser.getId(), accountNumber, transactionId);

        return ResponseEntity.ok(response);
    }
}

package com.haydenhurst.bankingapp.kyc.controller;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.kyc.dto.KycRequest;
import com.haydenhurst.bankingapp.kyc.dto.KycResponse;
import com.haydenhurst.bankingapp.kyc.dto.KycStatusResponse;
import com.haydenhurst.bankingapp.kyc.service.KycService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kyc")
public class KycController {
    private final KycService kycService;
    private final UserService userService;

    @Autowired
    public KycController(KycService kycService, UserService userService){
        this.kycService = kycService;
        this.userService = userService;
    }
    // ==================================================
    // USER ENDPOINTS
    // ==================================================

    // TODO: Require a digital signature from the user for their KYC profile creation
    // allows user to create a new profile
    // on submit = UNVERIFIED status
    @PostMapping
    public ResponseEntity<KycResponse> createKycProfile(@Valid @RequestBody KycRequest kycRequest){
        User currentUser = userService.getCurrentUser();
        KycResponse response = kycService.createProfile(currentUser.getId(), kycRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // allows user to review their own profile after submitting
    @GetMapping
    public ResponseEntity<KycResponse> getKycProfile() {
        User currentUser = userService.getCurrentUser();
        KycResponse response = kycService.getProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // allows user to view the status of the kyc profile (will show PENDING, UNVERIFIED, VERIFIED)
    @GetMapping("/status")
    public ResponseEntity<KycStatusResponse> getKycStatus(){
        User currentUser = userService.getCurrentUser();
        KycStatusResponse response = kycService.getStatus(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // ==================================================
    // ADMIN ENDPOINTS
    // ==================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<KycResponse> getKycProfileByUserId(@PathVariable Long userId) {
        KycResponse response = kycService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<KycResponse>> getKycProfilesByStatus(@RequestParam KycStatus status) {
        List<KycResponse> response = kycService.getAllByStatus(status);
        return ResponseEntity.ok(response);
    }

    // admin starts review = PENDING_REVIEW status
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{userId}/start-review")
    public ResponseEntity<KycResponse> startKycReview(@PathVariable Long userId) {
        KycResponse response = kycService.startReview(userId);
        return ResponseEntity.ok(response);
    }

    // TODO: Tie the admin that reviewed and verified the KYC profile of a user to that KYC, and also require them to sign their name digitally.

    // admin approves = VERIFIED status
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{userId}/approve")
    public ResponseEntity<KycResponse> approveKyc(@PathVariable Long userId) {
        KycResponse response = kycService.approve(userId);
        return ResponseEntity.ok(response);
    }

    // admin denies = DENIED status
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{userId}/deny")
    public ResponseEntity<KycResponse> denyKyc(@PathVariable Long userId) {
        KycResponse response = kycService.deny(userId);
        return ResponseEntity.ok(response);
    }
}

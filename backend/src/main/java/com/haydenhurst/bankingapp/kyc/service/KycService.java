package com.haydenhurst.bankingapp.kyc.service;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.util.EncryptionUtil;
import com.haydenhurst.bankingapp.kyc.dto.KycRequest;
import com.haydenhurst.bankingapp.kyc.dto.KycResponse;
import com.haydenhurst.bankingapp.kyc.dto.KycStatusResponse;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KycService {
    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Autowired
    public KycService(KycRepository kycRepository, UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public KycResponse createProfile(Long userId, KycRequest kycRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (kycRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Kyc profile already exists for this user");
        }

        // sanitization and normalization goes here before encryption if needed

        String encryptedSsn = encryptionUtil.encrypt(kycRequest.rawSSN());
        String encryptedDocNumber = encryptionUtil.encrypt(kycRequest.rawDocumentNumber());

        Kyc kyc = new Kyc(
                user,
                encryptedSsn,
                kycRequest.documentType(),
                encryptedDocNumber
        );
        Kyc saved = kycRepository.save(kyc);

        return mapToResponse(saved);
    }

    public KycResponse getProfile(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        return mapToResponse(kyc);
    }

    public KycStatusResponse getStatus(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        return new KycStatusResponse(
                kyc.getStatus(),
                kyc.getLastVerifiedOn(),
                kyc.getStatusChangedOn()
        );
    }

    public KycResponse startReview(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        // only allow moving to review if currently UNVERIFIED
        if (kyc.getStatus() != KycStatus.UNVERIFIED) {
            throw new IllegalStateException("KYC profile is not eligible for review");
        }

        kyc.onPendingReview();
        Kyc saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }

    public KycResponse approve(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        // require PENDING_REVIEW before approval
        if (kyc.getStatus() != KycStatus.PENDING_REVIEW) {
            throw new IllegalStateException("KYC profile must be in PENDING_REVIEW before approval");
        }

        kyc.onVerified();
        Kyc saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }

    public KycResponse deny(Long userId) {
        Kyc kyc = kycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));

        // require PENDING_REVIEW before denial
        if (kyc.getStatus() != KycStatus.PENDING_REVIEW) {
            throw new IllegalStateException("KYC profile must be in PENDING_REVIEW before denial");
        }

        kyc.onDenied();
        Kyc saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }

    public List<KycResponse> getAllByStatus(KycStatus status) {
        return kycRepository.findAllByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private KycResponse mapToResponse(Kyc kyc) {
        return new KycResponse(
                kyc.getId(),
                kyc.getDocumentType(),
                kyc.getStatus(),
                kyc.getLastVerifiedOn(),
                kyc.getStatusChangedOn()
        );
    }
}

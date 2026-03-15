package com.haydenhurst.bankingapp.kyc.service;

import com.haydenhurst.bankingapp.common.util.EncryptionUtil;
import com.haydenhurst.bankingapp.kyc.dto.KycRequest;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class KycService {
    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public KycService(KycRepository kycRepository, UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public Kyc createKycProfile(Long userId, KycRequest kycRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (kycRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Kyc profile already exists for this user");
        }

        String encryptedSsn = encryptionUtil.encrypt(kycRequest.rawSSN());
        String encryptedDocNumber = encryptionUtil.encrypt(kycRequest.rawDocumentNumber());

        Kyc kyc = new Kyc(
                user,
                encryptedSsn,
                kycRequest.documentType(),
                encryptedDocNumber
        );

        return kycRepository.save(kyc);
    }

    // TODO: add getKycStatus service method
}

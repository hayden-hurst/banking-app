package com.haydenhurst.bankingapp.kyc;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.exception.ResourceNotFoundException;
import com.haydenhurst.bankingapp.common.util.EncryptionUtil;
import com.haydenhurst.bankingapp.kyc.dto.KycRequest;
import com.haydenhurst.bankingapp.kyc.dto.KycResponse;
import com.haydenhurst.bankingapp.kyc.dto.KycStatusResponse;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.kyc.repository.KycRepository;
import com.haydenhurst.bankingapp.kyc.service.KycService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KycServiceTest {

    @Mock
    private KycRepository kycRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private KycService kycService;

    @Test
    void createProfile_success() {
        User user = buildUser(1L);
        KycRequest request = buildKycRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(kycRepository.findByUser(user)).thenReturn(Optional.empty());
        when(encryptionUtil.encrypt(request.rawSSN())).thenReturn("encrypted_ssn");
        when(encryptionUtil.encrypt(request.rawDocumentNumber())).thenReturn("encrypted_doc");
        when(kycRepository.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycResponse response = kycService.createProfile(1L, request);

        assertNotNull(response);
        assertEquals("PASSPORT", response.documentType());
        assertEquals(KycStatus.UNVERIFIED, response.status());

        verify(encryptionUtil).encrypt(request.rawSSN());
        verify(encryptionUtil).encrypt(request.rawDocumentNumber());

        ArgumentCaptor<Kyc> captor = ArgumentCaptor.forClass(Kyc.class);
        verify(kycRepository).save(captor.capture());

        Kyc saved = captor.getValue();
        assertEquals("encrypted_ssn", saved.getSsnEncrypted());
        assertEquals("encrypted_doc", saved.getDocumentNumberEncrypted());
        assertEquals("PASSPORT", saved.getDocumentType());
        assertEquals(KycStatus.UNVERIFIED, saved.getStatus());
        assertEquals(user, saved.getUser());
    }

    @Test
    void createProfile_throwsWhenUserNotFound() {
        KycRequest request = buildKycRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kycService.createProfile(1L, request));

        verify(kycRepository, never()).save(any());
    }

    @Test
    void createProfile_throwsWhenKycAlreadyExists() {
        User user = buildUser(1L);
        KycRequest request = buildKycRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(kycRepository.findByUser(user)).thenReturn(Optional.of(buildKyc(user, KycStatus.UNVERIFIED)));

        assertThrows(IllegalStateException.class, () -> kycService.createProfile(1L, request));

        verify(kycRepository, never()).save(any());
    }

    @Test
    void getProfile_success() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.PENDING_REVIEW);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));

        KycResponse response = kycService.getProfile(1L);

        assertEquals(KycStatus.PENDING_REVIEW, response.status());
        assertEquals(kyc.getDocumentType(), response.documentType());
    }

    @Test
    void getProfile_throwsWhenNotFound() {
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kycService.getProfile(1L));
    }

    @Test
    void getStatus_success() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.VERIFIED);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));

        KycStatusResponse response = kycService.getStatus(1L);

        assertEquals(KycStatus.VERIFIED, response.status());
        assertEquals(kyc.getLastVerifiedOn(), response.lastVerifiedOn());
        assertEquals(kyc.getStatusChangedOn(), response.statusChangedOn());
    }

    @Test
    void startReview_success() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.UNVERIFIED);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));
        when(kycRepository.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycResponse response = kycService.startReview(1L);

        assertEquals(KycStatus.PENDING_REVIEW, response.status());
    }

    @Test
    void startReview_throwsWhenWrongStatus() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.DENIED);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));

        assertThrows(IllegalStateException.class, () -> kycService.startReview(1L));

        verify(kycRepository, never()).save(any());
    }

    @Test
    void approve_success() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.PENDING_REVIEW);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));
        when(kycRepository.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycResponse response = kycService.approve(1L);

        assertEquals(KycStatus.VERIFIED, response.status());
        assertNotNull(response.lastVerifiedOn());
    }

    @Test
    void approve_throwsWhenWrongStatus() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.UNVERIFIED);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));

        assertThrows(IllegalStateException.class, () -> kycService.approve(1L));
    }

    @Test
    void deny_success() {
        User user = buildUser(1L);
        Kyc kyc = buildKyc(user, KycStatus.PENDING_REVIEW);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(kyc));
        when(kycRepository.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycResponse response = kycService.deny(1L);

        assertEquals(KycStatus.DENIED, response.status());
    }

    @Test
    void getAllByStatus_success() {
        User user1 = buildUser(1L);
        User user2 = buildUser(2L);

        Kyc kyc1 = buildKyc(user1, KycStatus.UNVERIFIED);
        Kyc kyc2 = buildKyc(user2, KycStatus.UNVERIFIED);

        when(kycRepository.findAllByStatus(KycStatus.UNVERIFIED)).thenReturn(List.of(kyc1, kyc2));

        List<KycResponse> responses = kycService.getAllByStatus(KycStatus.UNVERIFIED);

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.status() == KycStatus.UNVERIFIED));
    }

    // ==================================================
    // Helpers
    // ==================================================

    private User buildUser(Long id) {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPhoneNumber("5551234567");
        user.setAddress("123 Main St");
        user.setDOB(LocalDate.of(2000, 1, 1));

        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    private Kyc buildKyc(User user, KycStatus status) {
        Kyc kyc = new Kyc(user, "encrypted_ssn", "PASSPORT", "encrypted_doc");
        kyc.setStatus(status);
        kyc.setStatusChangedOn(LocalDate.now());
        if (status == KycStatus.VERIFIED) {
            kyc.setLastVerifiedOn(LocalDate.now());
        }
        return kyc;
    }

    private KycRequest buildKycRequest() {
        return new KycRequest(
                "123-45-6789",
                "PASSPORT",
                "A12345678"
        );
    }
}

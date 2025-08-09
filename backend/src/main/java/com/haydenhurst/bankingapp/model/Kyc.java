package com.haydenhurst.bankingapp.model;

import com.haydenhurst.bankingapp.enums.KycStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

// know your customer model for info like ssn, photo id, etc.
@Entity
@Table(name="Kyc")
public class Kyc {
    // ==================================================
    // Field Declarations
    // ==================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ssnEncrypted;

    @Column(nullable = false)
    private String idDocumentNumber;

    private LocalDate verifiedOn;

    private LocalDate statusChangedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

        // Relations //
    @OneToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // ==================================================
    // Constructors
    // ==================================================
    public Kyc() {
        this.status = KycStatus.UNVERIFIED;
    }

    public Kyc(String ssnEncrypted, String idDocumentNumber) {
        this.ssnEncrypted = ssnEncrypted;
        this.idDocumentNumber = idDocumentNumber;
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public String getSsnEncrypted(){ return ssnEncrypted; }
    public void setSsnEncrypted(){ this.ssnEncrypted = ssnEncrypted; }

    private String getIdDocumentNumber(){ return idDocumentNumber; }
    private void setIdDocumentNumber(){ this.idDocumentNumber = idDocumentNumber; }

    private KycStatus getKycStatus(){ return status; }
    private void setKycStatus(KycStatus status){ this.status = status; }

    private LocalDate getVerifiedOn(){ return verifiedOn; }
    private void setVerifiedOn(LocalDate verifiedOn){ this.verifiedOn = verifiedOn; }

    private LocalDate getStatusChangedOn(){ return statusChangedOn; }
    private void setStatusChangedOn(LocalDate statusChangedOn){ this.statusChangedOn = statusChangedOn; }
}

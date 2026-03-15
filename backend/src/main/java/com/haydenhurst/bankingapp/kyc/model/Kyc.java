package com.haydenhurst.bankingapp.kyc.model;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.common.util.MaskingUtil;
import com.haydenhurst.bankingapp.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;

// know your customer model for info like ssn, photo id, etc.
@Entity
@Table(name="kyc_profile")
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
    private String documentType;

    @Column(nullable = false)
    private String documentNumber;

    private LocalDate lastVerifiedOn;

    private LocalDate statusChangedOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private KycStatus status; // UNVERIFIED, VERIFIED, PENDING_REVIEW, DENIED

        // Relations //
    @OneToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // ==================================================
    // Constructors
    // ==================================================
    public Kyc() {}

    public Kyc(User user, String ssnEncrypted, String documentType, String documentNumber) {
        this.user = user;

        this.ssnEncrypted = ssnEncrypted;
        this.documentType = documentType;
        this.documentNumber = documentNumber;

        this.status = KycStatus.UNVERIFIED;
        this.statusChangedOn = LocalDate.now();
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public String getSsnEncrypted() { return ssnEncrypted; }
    public void setSsnEncrypted(){ this.ssnEncrypted = ssnEncrypted; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber(){ return documentNumber; }
    public void setDocumentNumber(){ this.documentNumber = documentNumber; }

    public KycStatus getKycStatus(){ return status; }
    public void setKycStatus(KycStatus status){ this.status = status; }

    public LocalDate getLastVerifiedOn(){ return lastVerifiedOn; }
    public void setLastVerifiedOn(LocalDate lastVerifiedOn){ this.lastVerifiedOn = lastVerifiedOn; }

    public LocalDate getStatusChangedOn(){ return statusChangedOn; }
    public void setStatusChangedOn(LocalDate statusChangedOn){ this.statusChangedOn = statusChangedOn; }


    // ==================================================
    // Utility Methods
    // ==================================================
    @Override
    public String toString() {
        return "Kyc{" +
                "id=" + id +
                ", documentType=" + documentType +
                ", documentNumber=" + MaskingUtil.maskAllButLastN(documentNumber, 4) +
                ", lastVerifiedOn=" + lastVerifiedOn +
                ", statusChangedOn=" + statusChangedOn +
                ", status=" + status +
                '}';
    }

    protected void onVerified(){
        status = KycStatus.VERIFIED;
        lastVerifiedOn = LocalDate.now();
        statusChangedOn = LocalDate.now();
    }

    // maybe we could have the system verify kyc and then if info was verified
    // the system it gets sent to a dashboard where it can be manually reviewed.
    protected void onPendingReview(){
        status = KycStatus.PENDING_REVIEW;
        statusChangedOn = LocalDate.now();
    }

    protected void onDenied(){
        status = KycStatus.DENIED;
        statusChangedOn = LocalDate.now();
    }
}
package com.haydenhurst.bankingapp.kyc.model;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
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
    private String documentNumberEncrypted;

    private LocalDate lastVerifiedOn;

    private LocalDate statusChangedOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private KycStatus status; // UNVERIFIED, VERIFIED, PENDING_REVIEW, DENIED

        // Relations //
    @OneToOne
    @JoinColumn(name="user_id", nullable = false, unique = true)
    private User user;

    // ==================================================
    // Constructors
    // ==================================================
    public Kyc() {}

    public Kyc(User user, String ssnEncrypted, String documentType, String documentNumberEncrypted) {
        this.user = user;
        this.ssnEncrypted = ssnEncrypted;
        this.documentType = documentType;
        this.documentNumberEncrypted = documentNumberEncrypted;
        this.status = KycStatus.UNVERIFIED;
        this.statusChangedOn = LocalDate.now();
    }

    // ==================================================
    // Get / Set
    // ==================================================
    public Long getId() { return id; }

    public String getSsnEncrypted() { return ssnEncrypted; }
    public void setSsnEncrypted(String ssnEncrypted){ this.ssnEncrypted = ssnEncrypted; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumberEncrypted() { return documentNumberEncrypted; }
    public void setDocumentNumberEncrypted(String documentNumberEncrypted) { this.documentNumberEncrypted = documentNumberEncrypted; }

    public KycStatus getStatus(){ return status; }
    public void setStatus(KycStatus status){ this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
                ", lastVerifiedOn=" + lastVerifiedOn +
                ", statusChangedOn=" + statusChangedOn +
                ", status=" + status +
                '}';
    }

    // users who are already verified wont be able to create a new kyc request until they need to refresh
    // use lastVerifiedOn date as a check before allowing a new request to be put in
    // users with verified status will now have access to regular banking actions
    public void onVerified(){
        status = KycStatus.VERIFIED;
        lastVerifiedOn = LocalDate.now();
        statusChangedOn = LocalDate.now();
    }

    // admins will see a list of unverified kyc requests and when they click review the status will be set to pending
    // and will be hidden from other admins so that they are all reviewing different profiles
    // gets sent to a dashboard where it can be manually reviewed. (can be accepted or denied by admins)
    public void onPendingReview(){
        status = KycStatus.PENDING_REVIEW;
        statusChangedOn = LocalDate.now();
    }

    // make the user wait (maybe a few days or at least 24 hours) before submitting a request again to prevent flooding.
    // the denied status will last that amount of time, and then they will be set back to UNVERFIED which will allow them to resubmit the profile
    public void onDenied(){
        status = KycStatus.DENIED;
        statusChangedOn = LocalDate.now();
    }
}
package com.haydenhurst.bankingapp.kyc.repository;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc, Long> {
    Optional<Kyc> findByUser(User user);
    Optional<Kyc> findByUserId(Long userId);
    List<Kyc> findAllByStatus(KycStatus status);
}

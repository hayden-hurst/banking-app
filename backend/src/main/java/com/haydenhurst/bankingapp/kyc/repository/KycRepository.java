package com.haydenhurst.bankingapp.kyc.repository;

import com.haydenhurst.bankingapp.kyc.model.Kyc;
import com.haydenhurst.bankingapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycRepository extends JpaRepository<Kyc, Long> {
    Optional<Kyc> findByUser(User user);
}

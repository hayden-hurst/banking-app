package com.haydenhurst.bankingapp.user.repository;

import com.haydenhurst.bankingapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Note to self: Spring Data JPA will essentially reduce boilerplate code for CRUD (Create, Read, Update, Delete) operations
// It does this by automatically generating the necessary SQL
// This interface will be used for writing database queries with Spring Data JPA
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
}

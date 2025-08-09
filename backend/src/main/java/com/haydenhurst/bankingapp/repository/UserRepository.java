package com.haydenhurst.bankingapp.repository;

import com.haydenhurst.bankingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Note to self: Spring Data JPA will essentially reduce boilerplate code for CRUD (Create, Read, Update, Delete) operations
// It does this by automatically generating the necessary SQL
// This interface will be used for writing database queries with Spring Data JPA
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);
}

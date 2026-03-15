package com.haydenhurst.bankingapp.user.service;

import com.haydenhurst.bankingapp.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Business Logic - calculations, transformations, validations
// Profile management (update user info, fetch user details)
// User-specific domain logic (managing user preferences, handling account status changes)
// Any other operation related to user data that doesn't involve authentication

@Service
public class UserService {
    public User getCurrentUser(){
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
            // TODO: implement custom UnauthorizedException and replace runtime exception with
            // throw new UnauthorizedException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new IllegalStateException(
               "Principal is not a User entity. Actual type: " +
                       (principal != null ? principal.getClass().getName() : "null")
        );
    }

}

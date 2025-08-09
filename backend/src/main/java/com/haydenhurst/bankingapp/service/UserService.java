package com.haydenhurst.bankingapp.service;

import com.haydenhurst.bankingapp.dto.LoginRequest;
import com.haydenhurst.bankingapp.dto.SignupRequest;
import com.haydenhurst.bankingapp.model.User;
import com.haydenhurst.bankingapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// Business Logic - calculations, transformations, validations
// Data access via Spring Data JPA in my case
// Transaction management

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    public String registerUser(SignupRequest signupRequest) {
        // first we want to check to make sure the user doesn't already exist with the same email, name, address, phonenumber, etc.
        // then after we verify that this user doesn't already exist we create a new user with the provided info via SignupRequest dto
        // the account will not be verified until they enter their Kyc information which will be used to verify they are who they say they are
        // upon verification they will now be able to own a bank account and send/receive/transfer money.
        User user = userRepository.findByEmail(signupRequest.getEmail());
        user.userRepository.findByPhoneNumber

        if (user == null) {
            User newUser = new User();
            newUser.setEmail(signupRequest.getEmail());
            newUser.setPhoneNumber(signupRequest.getPhoneNumber());
            newUser.setFullName(signupRequest.getFullName());
            newUser.setDOB(signupRequest.getDOB());

            return userRepository.save(newUser);
        }

    }

    public String loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null || !bCryptPasswordEncoder.matches(loginRequest.getPassword(), loginRequest.getPassword())) { // the second parameter needs to be changed to the hashed pass stored in db
            // use generic responses to prevent user enumeration/bruteforce hacking
            throw new IllegalArgumentException("Invalid email or password, please try again.");
        }

        // this is a placeholder return value
        // will generate and use jwt token
        return "Login Successful!";
    }



}

package com.haydenhurst.bankingapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SignupRequest {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String address;

    @JsonProperty("DOB")
    private LocalDate dob;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDOB(){
        return dob;
    }

    public void setDOB(LocalDate dob){
        this.dob = dob;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }
}
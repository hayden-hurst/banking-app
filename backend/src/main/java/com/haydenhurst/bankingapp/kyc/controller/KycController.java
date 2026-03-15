package com.haydenhurst.bankingapp.kyc.controller;

import com.haydenhurst.bankingapp.common.enums.KycStatus;
import com.haydenhurst.bankingapp.kyc.dto.KycRequest;
import com.haydenhurst.bankingapp.kyc.service.KycService;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/kyc")
public class KycController {
    private final KycService kycService;
    private final UserService userService;

    @Autowired
    public KycController(KycService kycService, UserService userService){
        this.kycService = kycService;
        this.userService = userService;
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyKyc(@RequestBody KycRequest kycRequest){
        User currUser = userService.getCurrentUser();
        String response = String.valueOf(kycService.createKycProfile(currUser.getId(), kycRequest));
        return ResponseEntity.ok(response);
    }

    // TODO: add getKycStatus controller method
    /*
    @GetMapping("/status")
    public ResponseEntity<String> getKycStatus(@RequestBody KycStatus kycStatus){
        String response = String.valueOf(kycService.)
        return ResponseEntity.ok(response);
    }
    */

}

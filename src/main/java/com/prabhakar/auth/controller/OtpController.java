package com.prabhakar.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prabhakar.auth.dto.ApiResponse;
import com.prabhakar.auth.otp.OtpEmailService;
import com.prabhakar.auth.otp.OtpGenerator;
import com.prabhakar.auth.otp.OtpRequest;
import com.prabhakar.auth.otp.OtpService;
import com.prabhakar.auth.otp.OtpVerifyRequest;

@RestController
@RequestMapping("/auth")
public class OtpController {

    private final OtpService otpService;
    private final OtpEmailService emailService;

    public OtpController(OtpService otpService, OtpEmailService emailService) {
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/request-otp")
    public ApiResponse requestOtp(@RequestBody OtpRequest request) {

        String otp = OtpGenerator.generateOtp();

        // save OTP in Redis
        otpService.saveOtp(request.getEmail(), otp);

        // send email
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.success("OTP has been sent to your email.");
    }

    @PostMapping("/verify-otp")
    public ApiResponse verifyOtp(@RequestBody OtpVerifyRequest request) {

        String savedOtp = otpService.getOtp(request.getEmail());

        if (savedOtp == null) {
            return ApiResponse.error(400, "OTP_INVALID", "OTP expired or not found");
        }

        if (!savedOtp.equals(request.getOtp())) {
            return ApiResponse.error(400, "OTP_INCORRECT", "Invalid OTP");
        }

        // correct OTP
        otpService.deleteOtp(request.getEmail());
        return ApiResponse.success("OTP verified successfully!");
    }
}


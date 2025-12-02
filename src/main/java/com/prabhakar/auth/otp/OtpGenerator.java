package com.prabhakar.auth.otp;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000); // 0 to 999999
        return String.format("%06d", otp); // always 6 digits, leading zeros allowed
    }
}

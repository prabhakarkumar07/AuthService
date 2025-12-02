package com.prabhakar.auth.otp;

import com.prabhakar.auth.email.EmailService;
import com.prabhakar.auth.email.HtmlTemplateLoader;

import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    private final EmailService emailService;
    private final HtmlTemplateLoader templateLoader;

    public OtpEmailService(EmailService emailService, HtmlTemplateLoader templateLoader) {
        this.emailService = emailService;
        this.templateLoader = templateLoader;
    }

    public void sendOtpEmail(String email, String otp) {
        String html = templateLoader.loadTemplate("otp-email.html");
        html = html.replace("{{OTP}}", otp);

        emailService.sendHtmlEmail(email, "Your OTP Code", html);
    }
}

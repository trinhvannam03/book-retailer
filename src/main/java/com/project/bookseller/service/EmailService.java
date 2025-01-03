package com.project.bookseller.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Data
@RequiredArgsConstructor
@RestController("/email/")
public class EmailService {
    private final JavaMailSender mailSender;


    @GetMapping("/{email}")
    public void sendSimpleEmail(@PathVariable String email) {
        String subject = "Welcome to Our Service!";
        String body = "<h1>Hello!</h1><p>We are excited to have you onboard. Let us know if you need any assistance.</p>";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(email); // The recipient passed via the URL
            helper.setSubject(subject); // A meaningful subject for the email
            helper.setText(body, true); // Set HTML body content
            mailSender.send(mimeMessage); // Send the email
            System.out.println("Email sent successfully to " + email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}


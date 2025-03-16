package com.khaled.smart_diagnosis.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎉 Welcome to Smart Diagnosis, " + firstName + "!");

            String imageUrl = "https://www.shutterstock.com/image-vector/artificial-intelligence-fix-bugs-checks-260nw-2522071531.jpg";

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2 style='color: #2c3e50;'>Welcome to Smart Diagnosis, " + firstName + "!</h2>" +
                    "<p style='color: #34495e;'>We're happy to have you on board. Please ensure you read the guidelines.</p>" +
                    "<img src='" + imageUrl + "' alt='Welcome Image' style='width: 100%; max-width: 400px; border-radius: 10px;'>" +
                    "<p style='color: #34495e;'>To get the best experience, make sure to read our guidelines.</p>" +
                    "<a href='https://yourwebsite.com/guidelines' style='display: inline-block; padding: 10px 20px; color: white; background-color: #3498db; text-decoration: none; border-radius: 5px;'>Read Guidelines</a>" +
                    "<br><br><p>Best regards,<br><b>Smart Diagnosis Team</b></p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("📩 Email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
        }
    }
}

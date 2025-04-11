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
            String imageUrl ="https://www.shutterstock.com/image-vector/artificial-intelligence-fix-bugs-checks-260nw-2522071531.jpg";
            String aboutPageUrl = "http://localhost:4200/about";


            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2 style='color: #2c3e50;'>Welcome to Smart Diagnosis, " + firstName + "!</h2>" +
                    "<p style='color: #34495e; font-size: 16px;'><strong>🚀 Get ready to experience the future of AI-powered medical diagnosis!</strong></p>" +
                    "<p style='color: #34495e;'>We're happy to have you on board. Get to know more about us below.</p>" +
                    "<img src='" + imageUrl + "' alt='Welcome Image' style='width: 100%; max-width: 400px; border-radius: 10px;'>" +
                    "<p style='color: #34495e;'>To understand our mission and values, read the About page.</p>" +
                    "<a href='" + aboutPageUrl + "' style='display: inline-block; padding: 10px 20px; color: white; background-color: #27ae60; text-decoration: none; border-radius: 5px;'>Learn About Us</a>" +
                    "<p style='color: #e74c3c; font-weight: bold;'>⚠️ Please make sure you have read the homepage carefully.</p>" +
                    "<br><br>" +
                    "<p><strong>Best regards,</strong></p>" +
                    "<p><strong>Khaled Ibrahim</strong><br>Medical AI Specialist</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("📩 Email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
        }
    }

    public void sendResetCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔐 Password Reset Code - Smart Diagnosis");

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2 style='color: #2c3e50;'>Reset Your Password</h2>" +
                    "<p style='color: #34495e; font-size: 16px;'>We've received a request to reset your password.</p>" +
                    "<p style='font-size: 20px; font-weight: bold; color: #e67e22;'>🔐 Your Code: <span style='color: #2980b9;'>" + code + "</span></p>" +
                    "<p style='color: #c0392b;'>⚠️ This code will expire in 10 minutes.</p>" +
                    "<br>" +
                    "<p style='color: #7f8c8d;'>If you didn’t request a password reset, please ignore this email.</p>" +
                    "<br>" +
                    "<p><strong>Best regards,</strong></p>" +
                    "<p><strong>Smart Diagnosis Team</strong></p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("📩 Reset code email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Error sending reset code email: " + e.getMessage());
        }
    }
}

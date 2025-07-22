package com.esprit.stageback.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
/*
    public void sendResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Code de réinitialisation de mot de passe");
        message.setText("Votre code de réinitialisation est : " + code);
        mailSender.send(message);
    }*/
public void sendResetCode(String to, String code) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🔐 Password Reset Code");

        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<img src='cid:logoImage' alt='Fuse Logo' style='width: 100px; height: auto; margin-bottom: 20px;'/>" +
                "<h2 style='color: #3B82F6;'>Password Reset Request</h2>" +
                "<p>Hello,</p>" +
                "<p>You requested to reset your password. Please use the following code:</p>" +
                "<h1 style='color: #10B981;'>" + code + "</h1>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "<br/>" +
                "<p style='font-size: 0.9em; color: #888;'>— The Fuse Team</p>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true);

        // Embed the logo image
       ClassPathResource logo = new ClassPathResource("static/images/logo.png");
       helper.addInline("logoImage", logo);

        mailSender.send(message);
        System.out.println("✅ HTML email sent successfully!");
    } catch (MessagingException e) {
        e.printStackTrace();
        throw new RuntimeException("Error sending email: " + e.getMessage());
    }
}

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("mohamedkamel.tounsi@esprit.tn");
        message.setSubject("✅ Test Email depuis Spring Boot");
        message.setText("Ceci est un email de test depuis ton application Spring Boot.");
        message.setFrom("kameltounsi220@gmail.com"); // doit correspondre à spring.mail.username
        mailSender.send(message);

        System.out.println("✅ Email de test envoyé !");
    }

}

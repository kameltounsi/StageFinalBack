package com.esprit.stageback.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
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

    public void sendWelcomeEmail(String to, String fullName, String email, String password, Locale locale) {
        try {
            String subject = messageSource.getMessage("welcome.subject", null, locale);
            String greeting = messageSource.getMessage("welcome.greeting", new Object[]{fullName}, locale);
            String body = messageSource.getMessage("welcome.body", null, locale);
            String details = messageSource.getMessage("welcome.details", null, locale);
            String emailLabel = messageSource.getMessage("welcome.email", null, locale);
            String passwordLabel = messageSource.getMessage("welcome.password", null, locale);
            String footer = messageSource.getMessage("welcome.footer", null, locale);
            String signature = messageSource.getMessage("welcome.signature", null, locale);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);

            // Gérer l’orientation si langue arabe
            String direction = locale.getLanguage().equals("ar") ? "rtl" : "ltr";
            String textAlign = locale.getLanguage().equals("ar") ? "right" : "left";

            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang='" + locale.getLanguage() + "' dir='" + direction + "'>" +
                    "<head><meta charset='UTF-8'></head>" +
                    "<body style='font-family: Arial, sans-serif; padding:20px; background:#f9f9f9; direction:" + direction + "; text-align:" + textAlign + ";'>" +
                    "<div style='max-width:600px;margin:auto;background:white;border-radius:8px;padding:20px;'>" +
                    "<img src='cid:logoImage' alt='Fuse Logo' style='display:block;margin:auto;width:120px;height:auto;'/>" +
                    "<h2 style='color:#3B82F6;text-align:center;'>" + subject + "</h2>" +
                    "<p>" + greeting + "</p>" +
                    "<p>" + body + "</p>" +
                    "<p><b>" + details + "</b></p>" +
                    "<ul>" +
                    "<li><b>" + emailLabel + ":</b> " + email + "</li>" +
                    "<li><b>" + passwordLabel + ":</b> " + password + "</li>" +
                    "</ul>" +
                    "<p>" + footer + "</p>" +
                    "<br/>" +
                    "<p style='color:#666;'>" + signature + "</p>" +
                    "</div></body></html>";

            helper.setText(htmlContent, true);

            // Logo
            ClassPathResource logo = new ClassPathResource("static/images/logo.png");
            helper.addInline("logoImage", logo);

            mailSender.send(message);

            System.out.println("✅ Welcome email sent in " + locale.getLanguage());
        } catch (Exception e) {
            throw new RuntimeException("Error sending welcome email: " + e.getMessage());
        }
    }





}

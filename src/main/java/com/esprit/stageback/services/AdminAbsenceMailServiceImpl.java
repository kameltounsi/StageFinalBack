package com.esprit.stageback.services;

import com.esprit.stageback.entities.User;
import com.esprit.stageback.services.AdminAbsenceMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAbsenceMailServiceImpl implements AdminAbsenceMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public void sendUnjustifiedAlert(User student, int unjustifiedCount) {
        if (student.getEmail() == null || student.getEmail().isBlank()) {
            return; // rien à envoyer
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(student.getEmail());
            helper.setSubject("Attendance alert: repeated unjustified absences");

            String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#0f172a">
                  <p>Hello <strong>%s</strong>,</p>
                  <p>Our records indicate you have <strong>%d unjustified absences</strong>.</p>
                  <p>Please visit the administration office as soon as possible to explain or justify these absences.</p>
                  <p style="margin-top:16px">Best regards,<br/>Administration</p>
                </div>
                """.formatted(student.getFullName(), unjustifiedCount);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to build/send email: " + e.getMessage(), e);
        }
    }
}

package com.esprit.stageback.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final MailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendPlainText(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }

    @Override
    public void sendHtml(String to, String subject, Map<String, Object> model) {
        try {
            MimeMessage mime = javaMailSender.createMimeMessage();

            // multipart/related impératif pour inline images
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);

            // Contexte Thymeleaf
            Context ctx = new Context(Locale.ENGLISH);
            ctx.setVariable("schoolName", "Fuse Learning");
            if (model != null) model.forEach(ctx::setVariable);

            final String LOGO_CID = "logo";
            ctx.setVariable("logoCid", LOGO_CID);

            // Résolution du logo
            Resource logo = resolveLogoResource();
            if (logo != null && logo.exists()) {
                try (var in = logo.getInputStream()) {
                    byte[] bytes = in.readAllBytes();
                    log.info("✅ Logo trouvé ({} bytes). Path = {}", bytes.length, logo.getDescription());
                    helper.addInline(LOGO_CID, logo, "image/png");
                }
            } else {
                log.error("❌ Aucun logo trouvé dans le classpath.");
            }

            String html = templateEngine.process("absence-alert", ctx);
            log.debug("HTML (début) = {}", html.substring(0, Math.min(300, html.length())));
            if (html.contains("cid:" + LOGO_CID)) {
                log.info("✅ Le HTML contient bien cid:{}.", LOGO_CID);
            } else {
                log.warn("⚠️ Le HTML ne contient PAS cid:{} → l'image ne s'affichera pas.", LOGO_CID);
            }

            helper.setText(html, true);

            // Dump .eml pour debug
            try (var fos = new java.io.FileOutputStream(
                    java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "last-mail.eml").toFile())) {
                mime.saveChanges();
                mime.writeTo(fos);
                log.info("🧪 Message EML écrit: {}/last-mail.eml", System.getProperty("java.io.tmpdir"));
            } catch (Exception dumpEx) {
                log.warn("Impossible d'écrire le dump EML: {}", dumpEx.toString());
            }

            javaMailSender.send(mime);
            log.info("📨 Mail envoyé.");
        } catch (Exception e) {
            log.error("❌ Erreur envoi mail: {}", e.getMessage(), e);
            throw new RuntimeException("Mail send failed: " + e.getMessage(), e);
        }
    }

    private Resource resolveLogoResource() {
        // Teste plusieurs emplacements possibles
        ClassPathResource p1 = new ClassPathResource("static/images/logo.png");
        if (p1.exists()) return p1;

        ClassPathResource p2 = new ClassPathResource("static.images/logo.png");
        if (p2.exists()) return p2;

        ClassPathResource p3 = new ClassPathResource("mail/logo.png");
        if (p3.exists()) return p3;

        return null;
    }
}

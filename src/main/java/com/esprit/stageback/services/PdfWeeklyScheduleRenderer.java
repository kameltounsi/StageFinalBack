// src/main/java/com/esprit/stageback/services/PdfWeeklyScheduleRenderer.java
package com.esprit.stageback.services;

import com.esprit.stageback.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PdfWeeklyScheduleRenderer {

    private final SpringTemplateEngine templateEngine;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] renderTrainerWeekly(Map<String, Object> model) {
        // Ensure generation timestamp is always present
        model.putIfAbsent("now", TS_FMT.format(LocalDateTime.now()));

        // Expose convenient fallbacks in case template or model doesn’t carry the full entity
        Object tObj = model.get("trainer");
        if (tObj instanceof User t) {
            if (t.getFullName() != null)    model.put("trainerName", t.getFullName());
            if (t.getSpecialite() != null)  model.put("trainerSpecialty", t.getSpecialite());
            if (t.getEmail() != null)       model.put("trainerEmail", t.getEmail());
        }

        // Thymeleaf context in ENGLISH so EEEE is Monday/Tuesday...
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariables(model);

        // IMPORTANT: template name WITHOUT "templates/" and WITHOUT ".html"
        String html = templateEngine.process("trainer-weekly", ctx);

        // --- Convert HTML to PDF ---
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // If you use Flying Saucer / OpenHTMLToPDF, adapt this part accordingly.
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            // Base URI to resolve <img src="images/logo.png"> from classpath:/static/images/...
            String baseUri = Objects.requireNonNull(getClass().getResource("/static/")).toExternalForm();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF render failed", e);
        }
    }
}

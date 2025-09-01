package com.esprit.stageback.services;

import java.util.Map;

public interface MailService {
    void sendPlainText(String to, String subject, String body);
    void sendHtml(String to, String subject, Map<String, Object> model);
}

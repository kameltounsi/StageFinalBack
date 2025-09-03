// src/main/java/com/esprit/stageback/services/SubjectCatalog.java
package com.esprit.stageback.services;

import java.util.List;
import java.util.Map;

public final class SubjectCatalog {
    private SubjectCatalog() {}

    // ⚠️ même mapping que sur le front
    public static final Map<String, List<String>> BY_SPECIALITY = Map.ofEntries(
            Map.entry("Cybersecurity & Ethical Hacking", List.of(
                    "Réseaux & Protocoles Sécurisés", "Tests d’intrusion (Pentest)", "Gestion des vulnérabilités"
            )),
            Map.entry("Web Development", List.of(
                    "Frontend (Angular/React)", "Backend (Spring/Node)", "Bases de données & SQL"
            )),
            Map.entry("Mobile Application Development", List.of(
                    "Android (Kotlin/Java)", "iOS (SwiftUI)", "Cross-platform (Flutter)"
            )),
            Map.entry("Graphic Design & Multimedia", List.of("Design UI/UX", "Suite Adobe (PS/AI/PR)", "Motion Graphics")),
            Map.entry("Digital Marketing & Social Media Management", List.of("Stratégie Social Media", "SEO/SEA & Analytics", "Content Marketing")),
            Map.entry("Electrical Installation & Building Wiring", List.of("Schémas & Normes électriques", "Tableaux & Protections", "Dépannage & sécurité")),
            Map.entry("Plumbing & Sanitary Installations", List.of("Réseaux d’eau & évacuation", "Matériaux & raccords", "Maintenance & étanchéité")),
            Map.entry("Masonry & Concrete Works", List.of("Matériaux & dosages béton", "Coffrage & ferraillage", "Techniques de maçonnerie")),
            Map.entry("Carpentry & Woodworking", List.of("Conception & traçage", "Assemblages & usinage", "Finition & sécurité")),
            Map.entry("HVAC Systems", List.of("Thermodynamique appliquée", "Climatisation & froid", "Chauffage & ventilation")),
            Map.entry("Accounting & Financial Management", List.of("Comptabilité générale", "Analyse financière", "Fiscalité & TVA")),
            Map.entry("Human Resources Management", List.of("Recrutement & onboarding", "Droit du travail", "GPEC & formation")),
            Map.entry("Office Administration & Secretarial Studies", List.of("Bureautique avancée", "Gestion documentaire", "Communication professionnelle")),
            Map.entry("Sales & Commercial Techniques", List.of("Techniques de vente", "Négociation & CRM", "Merchandising")),
            Map.entry("Logistics & Supply Chain Management", List.of("Gestion des stocks", "Transport & douane", "Planification (MRP/DRP)"))
    );

    public static List<String> subjectsFor(String speciality) {
        return BY_SPECIALITY.getOrDefault(speciality, List.of());
    }
}

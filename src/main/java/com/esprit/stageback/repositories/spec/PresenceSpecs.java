// src/main/java/com/esprit/stageback/repositories/spec/PresenceSpecs.java
package com.esprit.stageback.repositories.spec;

import com.esprit.stageback.entities.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PresenceSpecs {

    public static Specification<Presence> dateBetween(LocalDate start, LocalDate end) {
        return (root, q, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            var et = root.get("emploiTemps");
            if (start != null && end != null) {
                return cb.between(et.get("date"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(et.get("date"), start);
            } else {
                return cb.lessThanOrEqualTo(et.get("date"), end);
            }
        };
    }

    public static Specification<Presence> groupId(Long groupId) {
        return (root, q, cb) -> {
            if (groupId == null) return cb.conjunction();
            return cb.equal(root.get("emploiTemps").get("groupe").get("id"), groupId);
        };
    }

    public static Specification<Presence> studentId(Long studentId) {
        return (root, q, cb) -> {
            if (studentId == null) return cb.conjunction();
            return cb.equal(root.get("etudiant").get("id"), studentId);
        };
    }

    public static Specification<Presence> statut(StatutPresence statut) {
        return (root, q, cb) -> {
            if (statut == null) return cb.conjunction();
            return cb.equal(root.get("statut"), statut);
        };
    }

    public static Specification<Presence> justified(Boolean justified) {
        return (root, q, cb) -> {
            if (justified == null) return cb.conjunction();
            return justified ? cb.isTrue(root.get("justified")) : cb.isFalse(root.get("justified"));
        };
    }

    /** q: fulltext simple sur studentName/email/course/room/groupName */
    public static Specification<Presence> q(String qstr) {
        return (root, query, cb) -> {
            if (qstr == null || qstr.isBlank()) return cb.conjunction();
            String like = "%" + qstr.toLowerCase().trim() + "%";
            var et = root.get("emploiTemps");
            var student = root.get("etudiant");
            var group = et.get("groupe");

            return cb.or(
                    cb.like(cb.lower(student.get("fullName")), like),
                    cb.like(cb.lower(student.get("email")), like),
                    cb.like(cb.lower(et.get("matiere")), like),
                    cb.like(cb.lower(et.get("salle")), like),
                    cb.like(cb.lower(group.get("nom")), like)
            );
        };
    }
}

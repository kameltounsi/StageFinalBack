// src/main/java/com/esprit/stageback/repositories/spec/CourseFileSpecs.java
package com.esprit.stageback.repositories.spec;

import com.esprit.stageback.entities.CourseFile;
import com.esprit.stageback.entities.Groupe;
import com.esprit.stageback.entities.User;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public final class CourseFileSpecs {

    private CourseFileSpecs() {}

    public static Specification<CourseFile> groupeIdEq(Long gid) {
        return (root, cq, cb) -> gid == null ? cb.conjunction() : cb.equal(root.get("groupeId"), gid);
    }

    /** ⬇️ NOUVEAU : matche par ID OU par email du user ciblé */
    public static Specification<CourseFile> trainerMatches(Long trainerId) {
        return (root, cq, cb) -> {
            if (trainerId == null) return cb.conjunction();

            var sub = cq.subquery(Long.class);
            var u = sub.from(User.class);

            @SuppressWarnings("unchecked")
            Expression<String> userEmail = cb.lower(cb.trim(u.get("email")));
            @SuppressWarnings("unchecked")
            Expression<String> cfEmail   = cb.lower(cb.trim(root.get("trainerEmail")));

            sub.select(u.get("id"))
                    .where(
                            cb.equal(u.get("id"), trainerId),
                            cb.or(
                                    cb.equal(root.get("trainerId"), u.get("id")),
                                    cb.and(cb.isNotNull(root.get("trainerEmail")),
                                            cb.equal(cfEmail, userEmail))
                            )
                    );

            return cb.exists(sub);
        };
    }

    public static Specification<CourseFile> subjectEqIgnoreCase(String subject) {
        return (root, cq, cb) -> {
            if (subject == null || subject.isBlank()) return cb.conjunction();
            @SuppressWarnings("unchecked")
            Expression<String> col = cb.lower(cb.trim(root.get("subject")));
            return cb.equal(col, subject.trim().toLowerCase());
        };
    }

    public static Specification<CourseFile> titleContainsIgnoreCase(String q) {
        return (root, cq, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            @SuppressWarnings("unchecked")
            Expression<String> col = cb.lower(cb.trim(root.get("title")));
            return cb.like(col, "%" + q.trim().toLowerCase() + "%");
        };
    }

    /** Spécialité côté TRAINER */
    public static Specification<CourseFile> trainerSpecialiteEqIgnoreCase(String spec) {
        return (root, cq, cb) -> {
            if (spec == null || spec.isBlank()) return cb.conjunction();
            var sub = cq.subquery(Long.class);
            var u = sub.from(User.class);
            @SuppressWarnings("unchecked")
            Expression<String> uSpec = cb.lower(cb.trim(u.get("specialite")));
            sub.select(u.get("id"))
                    .where(
                            cb.isNotNull(root.get("trainerId")),
                            cb.equal(u.get("id"), root.get("trainerId")),
                            cb.equal(uSpec, spec.trim().toLowerCase())
                    );
            return cb.exists(sub);
        };
    }

    /** Spécialité côté GROUPE */
    public static Specification<CourseFile> groupSpecialiteEqIgnoreCase(String spec) {
        return (root, cq, cb) -> {
            if (spec == null || spec.isBlank()) return cb.conjunction();
            var sub = cq.subquery(Long.class);
            var g = sub.from(Groupe.class);
            @SuppressWarnings("unchecked")
            Expression<String> gSpec = cb.lower(cb.trim(g.get("specialite")));
            sub.select(g.get("id"))
                    .where(
                            cb.isNotNull(root.get("groupeId")),
                            cb.equal(g.get("id"), root.get("groupeId")),
                            cb.equal(gSpec, spec.trim().toLowerCase())
                    );
            return cb.exists(sub);
        };
    }

    /** Spécialité valide si TRAINER OU GROUPE match */
    public static Specification<CourseFile> anySpecialiteEqIgnoreCase(String spec) {
        return Specification.where(trainerSpecialiteEqIgnoreCase(spec))
                .or(groupSpecialiteEqIgnoreCase(spec));
    }
}

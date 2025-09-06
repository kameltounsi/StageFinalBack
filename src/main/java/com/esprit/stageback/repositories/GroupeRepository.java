package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupeRepository extends JpaRepository<Groupe, Long> {
    @Query("select g from Groupe g left join fetch g.students where g.id = :id")
    Optional<Groupe> findByIdWithStudents(@Param("id") Long id);

    /** Groupes où l’utilisateur est formateur */
    @Query("select distinct g from Groupe g join g.trainers t where t.id = :trainerId")
    List<Groupe> findByTrainers_Id(Long trainerId);
    @Query("select distinct trim(lower(g.specialite)) from Groupe g where g.specialite is not null order by 1")
    List<String> findDistinctSpecialites();

    @Query("""
           select g from Groupe g
           where (:specialite is null or lower(trim(g.specialite)) = lower(trim(:specialite)))
           order by g.nom asc
           """)
    List<Groupe> findBySpecialiteIgnoreCase(@Param("specialite") String specialite);
    // For trainer groups list
    List<Groupe> findByIdInOrderByNomAsc(Collection<Long> ids);

    // Optional helper to fetch a student list of a group (if needed elsewhere)
    @Query("""
        select e from User e
        where e.role = 'STUDENT' and e.studentGroupe.id = :groupeId
        order by lower(e.fullName)
    """)
    List<com.esprit.stageback.entities.User> findStudentsOfGroup(Long groupeId);

    Optional<Groupe> findBySpecialiteIgnoreCaseAndNomIgnoreCase(String specialite, String nom);
    long countByStudentCapacityLessThanEqual(int value);
    // Groups taught by trainer (by email)
    @Query("""
        select g from User u
        join u.trainerGroupes g
        where lower(u.email) = lower(:email)
        order by lower(g.nom)
    """)
    List<Groupe> findGroupsForTrainerEmail(@Param("email") String email);

    // If you need a flat row for quick list:
    @Query("""
        select g.id from User u
        join u.trainerGroupes g
        where lower(u.email) = lower(:email)
        order by g.id
    """)
    List<Long> findGroupIdsForTrainerEmail(@Param("email") String email);
    @Query("select g from Groupe g " +
            "where lower(g.specialite) = lower(:spec) " +
            "and (lower(g.nom) like lower(concat(:base, '%')))")
    List<Groupe> findBySpecialiteAndBaseNom(@Param("spec") String specialite,
                                            @Param("base") String baseNom);
}

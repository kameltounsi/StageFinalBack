package com.esprit.stageback.repositories;



import com.esprit.stageback.entities.Roles;
import com.esprit.stageback.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndSpecialite(Roles role, String specialite);
   /* // Students sans groupe
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.studentGroupe IS NULL AND u.specialite = :specialite")
    List<User> findAvailableStudentsBySpecialite(String specialite);

    // Trainers avec moins de 4 groupes
    @Query("SELECT u FROM User u WHERE u.role = 'TRAINER' AND size(u.trainerGroupes) < 4 AND u.specialite = :specialite")
    List<User> findAvailableTrainersBySpecialite(String specialite);
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.studentGroupe " +
            "LEFT JOIN FETCH u.trainerGroupes")
    List<User> findAllWithGroups();
*/
   // Students sans groupe
   @Query("SELECT u FROM User u " +
           "WHERE u.role = 'STUDENT' " +
           "AND u.studentGroupe IS NULL " +
           "AND LOWER(TRIM(u.specialite)) = LOWER(TRIM(:specialite))")
   List<User> findAvailableStudentsBySpecialite(@Param("specialite") String specialite);

    // Trainers avec moins de 4 groupes
    @Query("SELECT u FROM User u " +
            "WHERE u.role = 'TRAINER' " +
            "AND size(u.trainerGroupes) < 4 " +
            "AND LOWER(TRIM(u.specialite)) = LOWER(TRIM(:specialite))")
    List<User> findAvailableTrainersBySpecialite(@Param("specialite") String specialite);

    // Charger tous les users avec leurs groupes
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.studentGroupe " +
            "LEFT JOIN FETCH u.trainerGroupes")
    List<User> findAllWithGroups();

    @Query("SELECT t FROM User t JOIN t.trainerGroupes g WHERE g.id = :groupeId")
    List<User> findTrainersByGroupeId(@Param("groupeId") Long groupeId);
}

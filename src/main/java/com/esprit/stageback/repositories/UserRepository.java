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

    boolean existsByEmail(String email);
    @Query("select u.id from User u where lower(u.email) = lower(:email)")
    Optional<Long> findIdByEmailIgnoreCase(@Param("email") String email);

    List<User> findByRoleAndSpecialite(Roles role, String specialite);

    @Query("select u.fullName from User u where u.id = :id")
    Optional<String> findFullNameById(@Param("id") Long id);
    @Query("""
           SELECT u FROM User u
           WHERE u.role = 'STUDENT'
             AND u.studentGroupe IS NULL
             AND LOWER(TRIM(u.specialite)) = LOWER(TRIM(:specialite))
           """)
    List<User> findAvailableStudentsBySpecialite(@Param("specialite") String specialite);

    @Query("""
           SELECT u FROM User u
           WHERE u.role = 'TRAINER'
             AND size(u.trainerGroupes) < 4
             AND LOWER(TRIM(u.specialite)) = LOWER(TRIM(:specialite))
           """)
    List<User> findAvailableTrainersBySpecialite(@Param("specialite") String specialite);

    @Query("""
           SELECT DISTINCT u FROM User u
           LEFT JOIN FETCH u.studentGroupe
           LEFT JOIN FETCH u.trainerGroupes
           """)
    List<User> findAllWithGroups();

    @Query("SELECT t FROM User t JOIN t.trainerGroupes g WHERE g.id = :groupeId")
    List<User> findTrainersByGroupeId(@Param("groupeId") Long groupeId);
    // NEW: fetch the student's group id (assumes User has a ManyToOne<Group> groupe)
    @Query("""
        select g.id from User u
        join u.studentGroupe g
        where lower(u.email) = lower(:email)
    """)
    Optional<Long> findGroupIdByEmailIgnoreCase(@Param("email") String email);
    // src/main/java/com/esprit/stageback/repositories/UserRepository.java
    @Query("""
    select g.id from User u
    join u.trainerGroupes g
    where lower(u.email) = lower(:email)
""")
    List<Long> findTrainerGroupIdsByEmailIgnoreCase(@Param("email") String email);

}

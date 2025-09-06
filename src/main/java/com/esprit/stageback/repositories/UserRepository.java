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

    @Query("""
        select g.id from User u
        join u.studentGroupe g
        where lower(u.email) = lower(:email)
    """)
    Optional<Long> findGroupIdByEmailIgnoreCase(@Param("email") String email);

    @Query("""
        select g.id from User u
        join u.trainerGroupes g
        where lower(u.email) = lower(:email)
    """)
    List<Long> findTrainerGroupIdsByEmailIgnoreCase(@Param("email") String email);

    @Query("""
           SELECT u FROM User u
           WHERE u.role = 'STUDENT'
             AND u.studentGroupe.id = :groupeId
           ORDER BY LOWER(u.fullName) ASC
           """)
    List<User> findStudentsByGroupeId(@Param("groupeId") Long groupeId);

    @Query("""
       select g.id from User u
       join u.studentGroupe g
       where u.id = :userId
       """)
    Optional<Long> findStudentGroupIdByUserId(@Param("userId") Long userId);

    @Query("""
        select g.id from User u
        join u.studentGroupe g
        where lower(u.email) = lower(:email)
    """)
    Optional<Long> findStudentGroupIdByEmail(@Param("email") String email);

    @Query("""
       select new com.esprit.stageback.dto.TrainerDTO(u.id, u.fullName, u.email, u.specialite)
       from User u
       where u.role = 'TRAINER'
       order by lower(u.fullName)
       """)
    List<com.esprit.stageback.dto.TrainerDTO> findAllTrainerSummaries();
    long countByRole(Roles role);
    long countByRoleAndStudentGroupeIsNull(Roles role);
    long countByStudentGroupe_Id(Long groupId);
    @Query("""
           SELECT DISTINCT u
           FROM User u
           LEFT JOIN FETCH u.trainerGroupes
           WHERE u.role = 'TRAINER'
           """)
    List<User> findAllTrainersWithGroups();
    // List distinct trainer specialités (you already have this)
    @Query("""
   select distinct lower(trim(u.specialite))
   from User u
   where u.role = 'TRAINER'
     and u.specialite is not null
     and trim(u.specialite) <> ''
   order by lower(trim(u.specialite)) asc
""")
    List<String> findDistinctTrainerSpecialites();

    // NEW: fetch trainers with their groups for a given specialité
    @Query("""
   select distinct u from User u
   left join fetch u.trainerGroupes tg
   where u.role = 'TRAINER'
     and lower(trim(u.specialite)) = lower(trim(:specialite))
   order by lower(u.fullName)
""")

    List<User> findTrainersWithGroupsBySpecialite(@Param("specialite") String specialite);
    @Query("""
   select new com.esprit.stageback.dto.SpecialiteCountDTO(
       lower(trim(u.specialite)),
       count(u)
   )
   from User u
   where u.role = 'STUDENT'
     and u.specialite is not null
     and trim(u.specialite) <> ''
   group by lower(trim(u.specialite))
   order by lower(trim(u.specialite))
""")
    List<com.esprit.stageback.dto.SpecialiteCountDTO> countStudentsBySpecialite();

}

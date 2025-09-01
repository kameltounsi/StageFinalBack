// src/main/java/com/esprit/stageback/repositories/GroupeAdminViewRepository.java
package com.esprit.stageback.repositories;

import com.esprit.stageback.entities.Groupe;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupeAdminViewRepository extends Repository<Groupe, Long> {

    @Query("select distinct g.specialite from Groupe g where g.specialite is not null order by g.specialite asc")
    List<String> listSpecialites();

    @Query("select g from Groupe g where (:specialite is null or g.specialite = :specialite) order by g.nom asc")
    List<Groupe> bySpecialite(@Param("specialite") String specialite);
}

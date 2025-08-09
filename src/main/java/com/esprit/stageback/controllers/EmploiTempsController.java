package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.services.EmploiTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import com.esprit.stageback.dto.EmploiTempsDTO;
import static com.esprit.stageback.mappers.EmploiTempsMapper.toDTO;
@RestController
@RequestMapping("/api/plannings")
@RequiredArgsConstructor
public class EmploiTempsController {

    private final EmploiTempsService emploiTempsService;
/*
    // ➕ Ajouter un emploi du temps pour un groupe
    @PostMapping("/{groupeId}/add")
    public ResponseEntity<EmploiTemps> ajouterEmploi(
            @PathVariable Long groupeId,
            @RequestBody EmploiTemps emploiTemps
    ) {
        return ResponseEntity.ok(emploiTempsService.ajouterEmploi(groupeId, emploiTemps));
    }
*/

    @PostMapping("/{groupeId}/add")
    public ResponseEntity<EmploiTempsDTO> ajouterEmploi(
            @PathVariable Long groupeId,
            @RequestBody EmploiTemps emploiTemps
    ) {
        EmploiTemps saved = emploiTempsService.ajouterEmploi(groupeId, emploiTemps);
        return ResponseEntity.status(201).body(toDTO(saved));
    }
    // 📅 Récupérer le planning d’un groupe
    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<EmploiTemps>> getPlanningByGroupe(@PathVariable Long groupeId) {
        return ResponseEntity.ok(emploiTempsService.getPlanningByGroupe(groupeId));
    }

    // 📅 Récupérer le planning de la semaine courante pour tous les groupes
    @GetMapping("/semaine")
    public ResponseEntity<List<EmploiTemps>> getPlanningSemaine(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(emploiTempsService.getPlanningBetweenDates(startDate, endDate));
    }

  /*  // ❌ Supprimer un emploi du temps
    @DeleteMapping("/{emploiId}/delete")
    public ResponseEntity<String> deleteEmploi(@PathVariable Long emploiId) {
        emploiTempsService.supprimerEmploi(emploiId);
        return ResponseEntity.ok("Emploi du temps supprimé avec succès !");
    }*/
  @DeleteMapping("/{emploiId}/delete")
  public ResponseEntity<Void> deleteEmploi(@PathVariable Long emploiId) {
      emploiTempsService.supprimerEmploi(emploiId);
      return ResponseEntity.noContent().build(); // 204, pas de body
  }

}

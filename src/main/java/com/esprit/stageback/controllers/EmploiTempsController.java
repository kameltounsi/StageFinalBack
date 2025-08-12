package com.esprit.stageback.controllers;

import com.esprit.stageback.entities.EmploiTemps;
import com.esprit.stageback.services.EmploiTempsService;
import com.esprit.stageback.services.PlanningPdfService;
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
    private final PlanningPdfService pdfService;
    @PostMapping("/{groupeId}/add")
    public ResponseEntity<EmploiTempsDTO> ajouterEmploi(
            @PathVariable Long groupeId,
            @RequestBody EmploiTemps emploiTemps
    ) {
        EmploiTemps saved = emploiTempsService.ajouterEmploi(groupeId, emploiTemps);
        return ResponseEntity.status(201).body(toDTO(saved));
    }
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
  @DeleteMapping("/{emploiId}/delete")
  public ResponseEntity<Void> deleteEmploi(@PathVariable Long emploiId) {
      emploiTempsService.supprimerEmploi(emploiId);
      return ResponseEntity.noContent().build(); // 204, pas de body
  }
  /*
    @GetMapping("/groupe/{groupeId}/pdf")
    public ResponseEntity<byte[]> exportPlanningPdf(
            @PathVariable Long groupeId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        byte[] pdf = pdfService.buildPlanningPdf(groupeId, start, end);
        String filename = "planning_" + groupeId + "_" + start + "_" + end + ".pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
*/
  // EmploiTempsController.java
  @GetMapping(value = "/groupe/{groupeId}/pdf", produces = "application/pdf")
  public ResponseEntity<byte[]> exportPlanningPdf(
          @PathVariable Long groupeId,
          @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
          @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

      byte[] pdf = pdfService.buildPlanningPdf(groupeId, startDate, endDate);
      String filename = "planning_" + groupeId + "_" + startDate + "_" + endDate + ".pdf";

      return ResponseEntity.ok()
              .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
              .body(pdf); // produces=application/pdf fait déjà le bon Content-Type
  }


}

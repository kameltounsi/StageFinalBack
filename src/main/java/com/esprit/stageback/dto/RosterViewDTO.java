package com.esprit.stageback.dto;
// src/main/java/com/esprit/stageback/dto/RosterViewDTO.java
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RosterViewDTO {
    private Long emploiId;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Long groupeId;
    private String groupeNom;
    private String matiere;
    private List<RosterRowDTO> rows;
}

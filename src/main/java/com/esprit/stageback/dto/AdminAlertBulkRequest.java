// src/main/java/com/esprit/stageback/dto/AdminAlertBulkRequest.java
package com.esprit.stageback.dto;

import lombok.Data;

@Data
public class AdminAlertBulkRequest {
    // Filtres (mêmes noms que le front)
    private String specialite;
    private Long groupId;
    private String start; // yyyy-MM-dd
    private String end;   // yyyy-MM-dd

    // Tri
    private String sortBy; // total|unjustified|name
    private String dir;    // asc|desc

    // Seuil (défaut 5 si null)
    private Integer minUnjustified;

    // Mail optionnel
    private String subject;
    private String message;
}

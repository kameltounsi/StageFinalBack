// src/main/java/com/esprit/stageback/dto/requests/SendBulkAlertRequest.java
package com.esprit.stageback.dto;

import lombok.Data;

@Data
public class SendBulkAlertRequest {
    private String specialite; // optionnel
    private Long groupId;      // optionnel
    private String start;      // yyyy-MM-dd optionnel
    private String end;        // yyyy-MM-dd optionnel
    private Integer minUnjustified; // ex: 5
}

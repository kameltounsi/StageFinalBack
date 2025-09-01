// src/main/java/com/esprit/stageback/dto/requests/SendAlertRequest.java
package com.esprit.stageback.dto;

import lombok.Data;

@Data
public class SendAlertRequest {
    private Long studentId;
    private Integer minUnjustified; // ex: 5
}

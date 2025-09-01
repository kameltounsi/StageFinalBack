// src/main/java/com/esprit/stageback/dto/AdminAbsencesSummaryRow.java
package com.esprit.stageback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AdminAbsencesSummaryRow {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long groupId;
    private String groupName;
    private String specialite;
    private long totalAbsences;
    private long unjustifiedAbsences;
}

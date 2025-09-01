// src/main/java/com/esprit/stageback/dto/AdminAbsenceSummaryRow.java
package com.esprit.stageback.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAbsenceSummaryRow {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long groupId;
    private String groupName;
    private String specialite;
    private long totalAbsences;
    private long unjustifiedAbsences;
}

package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceMarkDTO {
    private Long studentId;
    private boolean present;
}

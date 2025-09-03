package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubjectTeacherDTO {
    private String teacherName;
    private String teacherAvatarUrl; // peut être null
}

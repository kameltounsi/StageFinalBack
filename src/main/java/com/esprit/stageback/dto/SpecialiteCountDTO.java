package com.esprit.stageback.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialiteCountDTO {
    private String specialite;
    private long count;
}

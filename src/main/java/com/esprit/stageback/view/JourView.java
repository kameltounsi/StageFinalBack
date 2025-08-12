package com.esprit.stageback.view;
import lombok.*;
import java.util.List;
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class JourView {
    private String label;          // ex: "Lundi 11/08"
    private List<SeanceView> seances;
}

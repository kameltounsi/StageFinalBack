package com.esprit.stageback.dto;
import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class AdminOverviewDTO {
    private Students students;
    private Groups groups;
    private Claims claims;
    private int promotionReadyGroups;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Students {
        public int total;
        public int unassigned;
        public int newThisMonth; // set 0 if you don't track createdAt
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Groups {
        public int total;
        public int lowCapacity; // studentCapacity <= 3
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Claims {
        public int pending; // we’ll return total if you don’t track status
    }
}

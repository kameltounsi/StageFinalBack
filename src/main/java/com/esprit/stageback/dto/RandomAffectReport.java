package com.esprit.stageback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RandomAffectReport {
    private String specialite;
    private String niveau;
    private int totalStudents;
    private int totalGroups;
    private Map<String, Integer> assignedPerGroup;
}

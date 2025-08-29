package com.esprit.stageback.dto;

import java.time.LocalDate;

public record SessionDTO(Long id, LocalDate date, String heureDebut, String heureFin, String matiere, String salle) {}

package com.esprit.stageback.dto;

import java.util.List;

public record GroupDTO(Long id, String nom, String specialite, List<StudentDTO> students) {}

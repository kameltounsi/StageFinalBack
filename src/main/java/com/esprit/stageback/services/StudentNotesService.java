// src/main/java/com/esprit/stageback/services/StudentNotesService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.StudentNoteDTO;

import java.util.List;

public interface StudentNotesService {
    List<StudentNoteDTO> myNotes(String studentEmail);
}

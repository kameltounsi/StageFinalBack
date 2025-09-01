// src/main/java/com/esprit/stageback/services/AdminAbsenceMailService.java
package com.esprit.stageback.services;

import com.esprit.stageback.entities.User;

public interface AdminAbsenceMailService {
    void sendUnjustifiedAlert(User student, int unjustifiedCount);
}

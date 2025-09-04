// src/main/java/com/esprit/stageback/services/AdminResultsService.java
package com.esprit.stageback.services;

import com.esprit.stageback.dto.AdminApplyResultsRequest;
import com.esprit.stageback.dto.AdminApplyResultsResponse;
import com.esprit.stageback.dto.AdminGroupResultsPreviewDTO;

public interface AdminResultsService {
    AdminGroupResultsPreviewDTO previewGroupResults(Long groupeId);
    AdminApplyResultsResponse applyGroupResults(AdminApplyResultsRequest req);
}

package com.esprit.stageback.dto;

import java.util.List;

public record ManagePresenceDTO(TrainerDTO trainer, GroupDTO group, List<SessionDTO> sessions) {}

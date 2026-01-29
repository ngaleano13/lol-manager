package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.FreeAgentDTO;
import java.util.List;

public interface PlayerService {
    void updateDiscord(Long userId, String discordUser);

    void updatePlayerRanks(Long userId);

    void updatePlayerRoles(Long userId, String primary, String secondary);

    void toggleLookingForTeam(Long userId);

    List<FreeAgentDTO> getPlayers(Boolean lookingForTeam, String role, String rank);
}